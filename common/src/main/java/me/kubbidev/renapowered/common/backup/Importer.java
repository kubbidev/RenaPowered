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

    private void processGuild(long guildId, GuildData u) {
        GuildEntity guildEntity = this.plugin.getStorage().loadEntity(GuildEntity.class, guildId, this.plugin.getGuildManager()).join();

        guildEntity.setName(u.name);
        guildEntity.setIconUrl(u.iconUrl);
        guildEntity.setRanking(u.ranking);
        guildEntity.setRankingChannel(u.rankingChannel);
        guildEntity.setSuggestChannel(u.suggestChannel);
        this.plugin.getStorage().saveEntity(guildEntity).join();
    }

    private void processUser(long userId, UserData u) {
        UserEntity userEntity = this.plugin.getStorage().loadEntity(UserEntity.class, userId, this.plugin.getUserManager()).join();

        userEntity.setUsername(u.username);
        userEntity.setAvatarUrl(u.avatarUrl);
        userEntity.setLastSeen(u.lastSeen);
        this.plugin.getStorage().saveEntity(userEntity).join();
    }

    private void processMember(UUID uuid, MemberData u) {
        MemberEntity memberEntity = this.plugin.getStorage().loadEntity(MemberEntity.class, uuid, this.plugin.getMemberManager()).join();

        memberEntity.setEffectiveName(u.effectiveName);
        memberEntity.setExperience(u.experience);
        memberEntity.setVoiceActivity(u.voiceActivity);
        memberEntity.setPreviousPlacement(u.previousPlacement);
        this.plugin.getStorage().saveEntity(memberEntity).join();
    }

    private Set<Map.Entry<String, JsonElement>> getDataSection(String id) {
        if (this.data.has(id)) {
            return this.data.get(id).getAsJsonObject().entrySet();
        } else {
            return Collections.emptySet();
        }
    }

    private void parseExportData(Map<Long, UserData> users, Map<Long, GuildData> guilds, Map<UUID, MemberData> members) {
        for (Map.Entry<String, JsonElement> guild : getDataSection("guilds")) {
            JsonObject jsonData = guild.getValue().getAsJsonObject();
            String iconUrl = null;
            String name = jsonData.get("name").getAsString();

            boolean ranking = jsonData.get("ranking").getAsBoolean();

            long rankingChannel = jsonData.get("rankingChannel").getAsLong();
            long suggestChannel = jsonData.get("suggestChannel").getAsLong();
            long guildId = Long.parseLong(guild.getKey());

            if (jsonData.has("iconUrl")) {
                iconUrl = jsonData.get("iconUrl").getAsString();
            }
            guilds.put(guildId, new GuildData(name, iconUrl, ranking, rankingChannel, suggestChannel));
        }
        for (Map.Entry<String, JsonElement> user : getDataSection("users")) {
            JsonObject jsonData = user.getValue().getAsJsonObject();
            String avatarUrl = null;
            String username = jsonData.get("username").getAsString();

            long lastSeen = jsonData.get("lastSeen").getAsLong();
            long userId = Long.parseLong(user.getKey());

            if (jsonData.has("avatarUrl")) {
                avatarUrl = jsonData.get("avatarUrl").getAsString();
            }
            users.put(userId, new UserData(username, avatarUrl, lastSeen));
        }
        for (Map.Entry<String, JsonElement> ranking : getDataSection("members")) {
            JsonObject jsonData = ranking.getValue().getAsJsonObject();
            String effectiveName = null;

            long experience = jsonData.get("experience").getAsLong();
            long voiceActivity = jsonData.get("voiceActivity").getAsLong();
            int previousPlacement = jsonData.get("previousPlacement").getAsInt();

            UUID uuid = Uuids.fromString(ranking.getKey());
            if (jsonData.has("effectiveName")) {
                effectiveName = jsonData.get("effectiveName").getAsString();
            }
            members.put(uuid, new MemberData(effectiveName, experience, voiceActivity, previousPlacement));
        }
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        this.notify.forEach(Message.IMPORT_START::send);

        // start an update task in the background - we'll #join this later
        CompletableFuture<Void> updateTask = CompletableFuture.runAsync(() -> this.plugin.getSyncTaskBuffer().requestDirectly());

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "Reading data..."));

        Map<Long, GuildData> guilds = new HashMap<>();
        Map<Long, UserData> users = new HashMap<>();
        Map<UUID, MemberData> members = new HashMap<>();

        parseExportData(users, guilds, members);

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "Waiting for initial update task to complete..."));

        // join the update task future before scheduling command executions
        updateTask.join();

        this.notify.forEach(s -> Message.IMPORT_INFO.send(s, "Setting up data processor..."));

        // create a threadpool for the processing
        ExecutorService executor = Executors.newFixedThreadPool(16, new ThreadFactoryBuilder().setNameFormat("renapowered-importer-%d").build());

        // A set of futures, which are really just the processes we need to wait for.
        Set<CompletableFuture<Void>> futures = new HashSet<>();

        int total = 0;
        AtomicInteger processedCount = new AtomicInteger(0);

        for (Map.Entry<Long, GuildData> guild : guilds.entrySet()) {
            futures.add(CompletableFuture.completedFuture(guild).thenAcceptAsync(ent -> {
                processGuild(ent.getKey(), ent.getValue());
                processedCount.incrementAndGet();
            }, executor));
            total++;
        }
        for (Map.Entry<Long, UserData> user : users.entrySet()) {
            futures.add(CompletableFuture.completedFuture(user).thenAcceptAsync(ent -> {
                processUser(ent.getKey(), ent.getValue());
                processedCount.incrementAndGet();
            }, executor));
            total++;
        }
        for (Map.Entry<UUID, MemberData> member : members.entrySet()) {
            futures.add(CompletableFuture.completedFuture(member).thenAcceptAsync(ent -> {
                processMember(ent.getKey(), ent.getValue());
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

    private record GuildData(String name, String iconUrl, boolean ranking, long rankingChannel, long suggestChannel) {

    }
    private record UserData(String username, String avatarUrl, long lastSeen) {

    }
    private record MemberData(String effectiveName, long experience, long voiceActivity, int previousPlacement) {

    }
}
