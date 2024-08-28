package me.kubbidev.renapowered.common.backup;

import com.google.gson.JsonObject;
import lombok.Getter;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.model.GuildEntity;
import me.kubbidev.renapowered.common.model.MemberEntity;
import me.kubbidev.renapowered.common.model.UserEntity;
import me.kubbidev.renapowered.common.model.manager.abstraction.Manager;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.storage.Storage;
import me.kubbidev.renapowered.common.storage.misc.entity.BaseEntity;
import me.kubbidev.renapowered.common.util.CompletableFutures;
import me.kubbidev.renapowered.common.util.gson.GsonProvider;
import me.kubbidev.renapowered.common.util.gson.JObject;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;

/**
 * Handles export operations
 */
@ApiStatus.Experimental
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
                .add("generatedBy", Exporter.this.executor.getName())
                .add("generatedAt", DATE_FORMAT.format(new Date(System.currentTimeMillis())))
                .toJson());

        FileProcessor fileProcessor = new FileProcessor(json);
        fileProcessor.run();

        processOutput(json);
    }

    private class FileProcessor implements Runnable {
        // create a thread pool to process the entities concurrently
        private final ExecutorService executor = Executors.newFixedThreadPool(32);

        // A set of futures, which are really just the processes we need to wait for.
        private final Set<CompletableFuture<Void>> futures = new HashSet<>();

        private final AtomicInteger processedCount = new AtomicInteger(0);
        private final JsonObject output;

        public FileProcessor(JsonObject output) {
            this.output = output;
        }

        @Override
        public void run() {
            log.log("Gathering guild data...");
            Supplier<JsonObject> guilds = export(GuildEntity.class, plugin.getGuildManager(), guild -> new JObject()
                    .add("name", guild.getName())
                    .add("iconUrl", guild.getIconUrl())
                    .add("ranking", guild.isRanking())
                    .add("rankingChannel", guild.getRankingChannel())
                    .toJson(), "guilds");

            log.log("Gathering user data...");
            Supplier<JsonObject> users = export(UserEntity.class, plugin.getUserManager(), user -> new JObject()
                    .add("username", user.getUsername())
                    .add("avatarUrl", user.getAvatarUrl())
                    .add("lastSeen", user.getLastSeen())
                    .toJson(), "users");

            log.log("Gathering member data...");
            Supplier<JsonObject> members = export(MemberEntity.class, plugin.getMemberManager(), member -> new JObject()
                    .add("effectiveName", member.getEffectiveName())
                    .add("experience", member.getExperience())
                    .add("voiceActivity", member.getVoiceActivity())
                    .add("previousPlacement", member.getPreviousPlacement())
                    .add("biography", member.getBiography())
                    .toJson(), "members");

            // all of the threads have been scheduled now and are running. we just need to wait for them all to complete
            CompletableFuture<Void> overallFuture = CompletableFutures.allOf(this.futures);

            while (true) {
                try {
                    overallFuture.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    // abnormal error - just break
                    e.printStackTrace();
                    break;
                } catch (TimeoutException e) {
                    // still executing - send a progress report and continue waiting
                    log.logProgress("Exported " + this.processedCount.get() + " entities so far.");
                    continue;
                }

                // process is complete
                break;
            }

            this.executor.shutdown();

            this.output.add("guilds", guilds.get());
            this.output.add("users", users.get());
            this.output.add("members", members.get());
        }

        public <I, T extends BaseEntity> Supplier<JsonObject> export(
                Class<T> type, Manager<I, T> manager, Function<T, JsonObject> mapping, String key) {
            // Entities are migrated in separate threads.
            // This is because there are likely to be a lot of them, and because we can.
            // It's a big speed improvement, since the database/files are split up and can handle concurrent reads.

            log.log("Finding a list of unique " + key + " to export.");

            // find all of the unique entities we need to export
            Storage ds = plugin.getStorage();

            @SuppressWarnings("unchecked")
            Set<I> ids = (Set<I>) ds.getUniqueEntities(type).join();
            log.log("Found " + ids.size() + " unique " + key + " to export.");

            Map<Object, JsonObject> out = Collections.synchronizedMap(new TreeMap<>());

            // iterate through each entity.
            for (I id : ids) {
                // register a task for the entity, and schedule it's execution with the pool
                this.futures.add(CompletableFuture.runAsync(() -> {
                    T t = plugin.getStorage().loadEntity(type, id, manager).join();
                    out.put(t.getId(), mapping.apply(t));
                    this.processedCount.incrementAndGet();
                }, this.executor));
            }

            return () -> {
                JsonObject outJson = new JsonObject();
                for (Map.Entry<Object, JsonObject> entry : out.entrySet()) {
                    outJson.add(entry.getKey().toString(), entry.getValue());
                }
                return outJson;
            };
        }
    }

    protected abstract void processOutput(JsonObject json);

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