package me.kubbidev.renapowered.common.backup;

import com.google.gson.JsonObject;
import lombok.Getter;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.model.GuildEntity;
import me.kubbidev.renapowered.common.model.MemberEntity;
import me.kubbidev.renapowered.common.model.UserEntity;
import me.kubbidev.renapowered.common.model.manager.StandardUserManager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.util.CompletableFutures;
import me.kubbidev.renapowered.common.util.ImmutableCollectors;
import me.kubbidev.renapowered.common.util.gson.GsonProvider;
import me.kubbidev.renapowered.common.util.gson.JObject;
import net.kyori.adventure.text.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

/**
 * Handles export operations
 */
public abstract class Exporter implements Runnable {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    protected final RenaPlugin plugin;
    protected final ProgressLogger log;

    private final Sender executor;

    protected Exporter(RenaPlugin plugin, Sender executor) {
        this.plugin = plugin;
        this.executor = executor;

        this.log = new ProgressLogger();
        this.log.addListener(plugin.getConsoleSender());
        this.log.addListener(executor);
    }

    @Override
    public void run() {
        JsonObject json = new JsonObject();
        json.add("metadata", new JObject()
                .add("generatedBy", this.executor.getName())
                .add("generatedAt", DATE_FORMAT.format(new Date(System.currentTimeMillis())))
                .toJson());

        this.log.log("Gathering guild data...");
        json.add("guilds", exportGuilds());

        this.log.log("Gathering user data...");
        json.add("users", exportUsers());

        this.log.log("Gathering member data...");
        json.add("members", exportMembers());

        processOutput(json);
    }

    protected abstract void processOutput(JsonObject json);

    private JsonObject exportUsers() {
        // Users are migrated in separate threads.
        // This is because there are likely to be a lot of them, and because we can.
        // It's a big speed improvement, since the database/files are split up and can handle concurrent reads.

        this.log.log("Finding a list of unique users to export.");

        // find all of the unique users we need to export
        StandardUserManager manager = this.plugin.getUserManager();
        Set<Long> users = manager.getAll().keySet();
        this.log.log("Found " + users.size() + " unique users to export.");

        // create a threadpool to process the users concurrently
        ExecutorService executor = Executors.newFixedThreadPool(32);

        // A set of futures, which are really just the processes we need to wait for.
        Set<CompletableFuture<Void>> futures = new HashSet<>();

        AtomicInteger userCount = new AtomicInteger(0);
        Map<Long, JsonObject> out = Collections.synchronizedMap(new TreeMap<>());

        // iterate through each entry.
        for (Long userId : users) {
            // register a task for the user, and schedule it's execution with the pool
            futures.add(CompletableFuture.runAsync(() -> {
                UserEntity userEntity = this.plugin.getStorage().loadEntity(UserEntity.class, userId, manager).join();
                out.put(userId, new JObject()
                        .add("username", userEntity.getUsername())
                        .add("avatarUrl", userEntity.getAvatarUrl())
                        .add("lastSeen", userEntity.getLastSeen())
                        .toJson());
                userCount.incrementAndGet();
            }, executor));
        }

        // all of the threads have been scheduled now and are running. we just need to wait for them all to complete
        CompletableFuture<Void> overallFuture = CompletableFutures.allOf(futures);

        while (true) {
            try {
                overallFuture.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                // abnormal error - just break
                e.printStackTrace();
                break;
            } catch (TimeoutException e) {
                // still executing - send a progress report and continue waiting
                this.log.logProgress("Exported " + userCount.get() + " users so far.");
                continue;
            }

            // process is complete
            break;
        }

        executor.shutdown();

        JsonObject outJson = new JsonObject();
        for (Map.Entry<Long, JsonObject> entry : out.entrySet()) {
            outJson.add(entry.getKey().toString(), entry.getValue());
        }
        return outJson;
    }

    private JsonObject exportGuilds() {
        JsonObject out = new JsonObject();
        List<GuildEntity> guilds = this.plugin.getGuildManager().getAll().values().stream()
                .sorted(Comparator.comparingLong(GuildEntity::getId).reversed())
                .collect(ImmutableCollectors.toList());

        for (GuildEntity guildEntity : guilds) {
            out.add(guildEntity.getId().toString(), new JObject()
                    .add("name", guildEntity.getName())
                    .add("iconUrl", guildEntity.getIconUrl())
                    .add("ranking", guildEntity.isRanking())
                    .add("rankingChannel", guildEntity.getRankingChannel())
                    .add("suggestChannel", guildEntity.getSuggestChannel())
                    .toJson());
        }
        return out;
    }

    private JsonObject exportMembers() {
        JsonObject out = new JsonObject();
        List<MemberEntity> members = this.plugin.getMemberManager().getAll().values().stream()
                .sorted(Comparator.comparingLong((MemberEntity value) -> value.getId().getLeastSignificantBits())
                        .thenComparingLong(MemberEntity::getExperience).reversed())
                .collect(ImmutableCollectors.toList());

        for (MemberEntity memberEntity : members) {
            out.add(memberEntity.getId().toString(), new JObject()
                            .add("effectiveName", memberEntity.getEffectiveName())
                            .add("experience", memberEntity.getExperience())
                            .add("voiceActivity", memberEntity.getVoiceActivity())
                            .add("previousPlacement", memberEntity.getPreviousPlacement())
                    .toJson());
        }
        return out;
    }

    public static final class SaveFile extends Exporter {
        private final Path filePath;

        public SaveFile(RenaPlugin plugin, Sender executor, Path filePath) {
            super(plugin, executor);
            this.filePath = filePath;
        }

        @Override
        protected void processOutput(JsonObject json) {
            this.log.log("Finished gathering data, writing file...");

            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(this.filePath)), StandardCharsets.UTF_8))) {
                GsonProvider.normal().toJson(json, out);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.log.getListeners().forEach(l -> Message.EXPORT_FILE_SUCCESS.send(l, this.filePath.toFile().getAbsolutePath()));
        }
    }

    @Getter
    public static final class ProgressLogger {
        private final Set<Sender> listeners = new HashSet<>();

        public void addListener(Sender sender) {
            this.listeners.add(sender);
        }

        public void log(String msg) {
            dispatchMessage(Message.EXPORT_LOG, msg);
        }

        public void logProgress(String msg) {
            dispatchMessage(Message.EXPORT_LOG_PROGRESS, msg);
        }

        private void dispatchMessage(Message.Args1<String> messageType, String content) {
            Component message = messageType.build(content);
            for (Sender s : this.listeners) {
                s.sendMessage(message);
            }
        }
    }
}