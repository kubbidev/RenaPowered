package me.kubbidev.renapowered.common.backup;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.util.CompletableFutures;
import me.kubbidev.renapowered.common.util.Uuids;
import me.kubbidev.renapowered.common.model.GuildEntity;
import me.kubbidev.renapowered.common.model.MemberEntity;
import me.kubbidev.renapowered.common.model.UserEntity;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles import operations
 */
public class Importer implements Runnable {

    private final RenaPlugin plugin;
    private final Set<Sender> notify;
    private final JsonObject data;

    public Importer(RenaPlugin plugin, Sender executor, JsonObject data) {
        this.plugin = plugin;

        if (executor.isConsole()) {
            this.notify = ImmutableSet.of(executor);
        } else {
            this.notify = ImmutableSet.of(executor, plugin.getConsoleSender());
        }
        this.data = data;
    }

    private Set<Map.Entry<String, JsonElement>> getDataSection(String id) {
        if (this.data.has(id)) {
            return this.data.get(id).getAsJsonObject().entrySet();
        } else {
            return Collections.emptySet();
        }
    }

    private void parseExportData(Map<Long, JsonObject> users, Map<Long, JsonObject> guilds, Map<UUID, JsonObject> members) {
        for (Map.Entry<String, JsonElement> guild : getDataSection("guilds")) {
            guilds.put(Long.parseLong(guild.getKey()), guild.getValue().getAsJsonObject());
        }
        for (Map.Entry<String, JsonElement> user : getDataSection("users")) {
            users.put(Long.parseLong(user.getKey()), user.getValue().getAsJsonObject());
        }
        for (Map.Entry<String, JsonElement> member : getDataSection("members")) {
            members.put(Uuids.fromString(member.getKey()), member.getValue().getAsJsonObject());
        }
    }

    private void processGuild(Long id, JsonObject o) {
        GuildEntity guild = this.plugin.getStorage().loadEntity(GuildEntity.class, id, this.plugin.getGuildManager()).join();
        guild.setName(o.get("name").getAsString());

        JsonElement iconUrl = o.get("iconUrl");
        if (iconUrl != null && !iconUrl.isJsonNull()) {
            guild.setIconUrl(iconUrl.getAsString());
        }
        guild.setRanking(o.get("ranking").getAsBoolean());
        guild.setRankingChannel(o.get("rankingChannel").getAsLong());

        this.plugin.getStorage().saveEntity(guild).join();
    }

    private void processUser(Long id, JsonObject o) {
        UserEntity user = this.plugin.getStorage().loadEntity(UserEntity.class, id, this.plugin.getUserManager()).join();
        user.setUsername(o.get("username").getAsString());
        user.setLastSeen(o.get("lastSeen").getAsLong());

        JsonElement avatarUrl = o.get("avatarUrl");
        if (avatarUrl != null && !avatarUrl.isJsonNull()) {
            user.setAvatarUrl(avatarUrl.getAsString());
        }
        this.plugin.getStorage().saveEntity(user).join();
    }

    private void processMember(UUID uuid, JsonObject o) {
        MemberEntity member = this.plugin.getStorage().loadEntity(MemberEntity.class, uuid, this.plugin.getMemberManager()).join();
        member.setEffectiveName(o.get("effectiveName").getAsString());
        member.setExperience(o.get("experience").getAsLong());
        member.setVoiceActivity(o.get("voiceActivity").getAsLong());
        member.setPreviousPlacement(o.get("previousPlacement").getAsInt());

        this.plugin.getStorage().saveEntity(member).join();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        this.notify.forEach(Message.IMPORT_START::send);

        // start an update task in the background - we'll #join this later
        CompletableFuture<Void> updateTask = CompletableFuture.runAsync(() -> this.plugin.getSyncTaskBuffer().requestDirectly());

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "Reading data..."));

        Map<Long, JsonObject> guilds = new HashMap<>();
        Map<Long, JsonObject> users = new HashMap<>();
        Map<UUID, JsonObject> members = new HashMap<>();

        parseExportData(users, guilds, members);

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "Waiting for initial update task to complete..."));

        // join the update task future before scheduling command executions
        updateTask.join();

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "Setting up data processor..."));

        // create a thread pool for the processing
        ExecutorService executor = Executors.newFixedThreadPool(16, new ThreadFactoryBuilder().setNameFormat("renapowered-importer-%d").build());

        // A set of futures, which are really just the processes we need to wait for.
        Set<CompletableFuture<Void>> futures = new HashSet<>();

        int total = 0;
        AtomicInteger processedCount = new AtomicInteger(0);

        for (Map.Entry<Long, JsonObject> guild : guilds.entrySet()) {
            futures.add(CompletableFuture.completedFuture(guild).thenAcceptAsync(e -> {
                processGuild(e.getKey(), e.getValue());
                processedCount.incrementAndGet();
            }, executor));
            total++;
        }
        for (Map.Entry<Long, JsonObject> user : users.entrySet()) {
            futures.add(CompletableFuture.completedFuture(user).thenAcceptAsync(e -> {
                processUser(e.getKey(), e.getValue());
                processedCount.incrementAndGet();
            }, executor));
            total++;
        }
        for (Map.Entry<UUID, JsonObject> member : members.entrySet()) {
            futures.add(CompletableFuture.completedFuture(member).thenAcceptAsync(e -> {
                processMember(e.getKey(), e.getValue());
                processedCount.incrementAndGet();
            }, executor));
            total++;
        }

        // all of the threads have been scheduled now and are running. we just need to wait for them all to complete
        CompletableFuture<Void> overallFuture = CompletableFutures.allOf(futures);

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "All data entries have been processed and scheduled for import - now waiting for the execution to complete."));

        while (true) {
            try {
                overallFuture.get(2, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                // abnormal error - just break
                e.printStackTrace();
                break;
            } catch (TimeoutException e) {
                // still executing - send a progress report and continue waiting
                sendProgress(processedCount.get(), total);
                continue;
            }

            // process is complete
            break;
        }

        executor.shutdown();

        long endTime = System.currentTimeMillis();
        double seconds = (endTime - startTime) / 1000.0;

        this.notify.forEach(s -> Message.IMPORT_END_COMPLETE.send(s, seconds));
    }

    private void sendProgress(int processedCount, int total) {
        int percent = processedCount * 100 / total;
        this.notify.forEach(s -> Message.IMPORT_PROGRESS.send(s, percent, processedCount, total));
    }
}
