package me.kubbidev.renapowered.common.worker;

import lombok.Getter;
import me.kubbidev.renapowered.common.config.ConfigKeys;
import me.kubbidev.renapowered.common.listeners.MemberListener;
import me.kubbidev.renapowered.common.module.birthday.BirthdayService;
import me.kubbidev.renapowered.common.module.ranking.listener.ActivityListener;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.module.ranking.RankingService;
import me.kubbidev.renapowered.common.util.ComponentSerializer;
import me.kubbidev.renapowered.common.util.AdventureEmbed;
import me.kubbidev.renapowered.common.worker.command.InteractionManager;
import me.kubbidev.renapowered.common.worker.event.EventHandler;
import me.kubbidev.renapowered.common.worker.event.EventManager;
import me.kubbidev.renapowered.common.worker.listener.DiscordEventListener;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.requests.FluentRestAction;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Getter
@EventHandler
public class DiscordService extends DiscordEventListener implements AutoCloseable {
    /**
     * The event manager responsible for registering and handling generic events
     */
    private final EventManager eventManager;

    /**
     * The command manager used to register and execute discord commands
     */
    private final InteractionManager interactionManager;
    private final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private RankingService rankingService;
    private BirthdayService birthdayService;

    /**
     * Shard Manager responsible of the connection between our application and the discord bot
     */
    private ShardManager shardManager;

    public DiscordService(RenaPlugin plugin) {
        super(plugin);
        this.eventManager = new EventManager(plugin);
        this.interactionManager = new InteractionManager(plugin);
    }

    public final void enable() {
        String token = this.plugin.getConfiguration().get(ConfigKeys.AUTHENTICATION_TOKEN);
        if (token == null || token.isEmpty()) {
            throw new NullPointerException("No Discord authentication token specified");
        }
        // build the shard manager with the provided application token
        this.shardManager = DefaultShardManagerBuilder.createDefault(token)
                .setShardsTotal(this.plugin.getConfiguration().get(ConfigKeys.TOTAL_SHARDS))
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .enableCache(
                        CacheFlag.ONLINE_STATUS,
                        CacheFlag.VOICE_STATE,
                        CacheFlag.ACTIVITY
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setEventManagerProvider(id -> this.eventManager)
                .addEventListeners(this)
                .setEventPassthrough(true)
                .setEnableShutdownHook(false)
                .build();
    }

    private void registerPlatformListeners() {
        this.shardManager.addEventListener(getInteractionManager());
        this.shardManager.addEventListener(new ActivityListener(this.plugin));
        this.shardManager.addEventListener(new MemberListener(this.plugin));
    }

    @Override
    public void close() {
        // close ranking service
        getRankingService().close();

        // close the party, no more fun!
        getBirthdayService().close();

        // close the discord connection
        getShardManager().shutdown();
    }

    public void renderActivity(Activity activity) {
        if (this.shardManager != null) {
            this.shardManager.setActivity(activity);
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        getInteractionManager().buildCommands(event.getJDA());

        // register listeners
        registerPlatformListeners();

        this.rankingService = new RankingService(this.plugin);
        this.rankingService.schedule();

        this.birthdayService = new BirthdayService(this.plugin);
        this.birthdayService.schedule();
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        this.shutdownLatch.countDown();
    }

    @Override
    public void onException(@NotNull ExceptionEvent event) {
        this.plugin.getLogger().severe("Throw new JDA exception: " + event.getCause());
    }

    public CompletableFuture<Message> sendMessage(MessageChannel messageChannel, MessageCreateData message) {
        return sendMessageSilent(messageChannel::sendMessage, message);
    }

    public CompletableFuture<Message> sendTempMessage(MessageChannel messageChannel, MessageCreateData message, long delay, TimeUnit unit) {
        return sendTempMessageSilent(messageChannel::sendMessage, message, delay, unit);
    }

    public CompletableFuture<Message> sendMessage(MessageChannel messageChannel, Component message, @Nullable Locale locale) {
        return sendMessageSilent(messageChannel::sendMessage, ComponentSerializer.serialize(message, locale));
    }

    public CompletableFuture<Message> sendTempMessage(MessageChannel messageChannel, Component message, @Nullable Locale locale, long delay, TimeUnit unit) {
        return sendTempMessageSilent(messageChannel::sendMessage, ComponentSerializer.serialize(message, locale), delay, unit);
    }

    public CompletableFuture<Message> sendMessage(MessageChannel messageChannel, AdventureEmbed embed, @Nullable Locale locale) {
        return sendMessageSilent(builder -> messageChannel.sendMessage(new MessageCreateBuilder()
                .setEmbeds(embed.build(locale))
                .build()), embed);
    }

    public CompletableFuture<Message> sendTempMessage(MessageChannel messageChannel, AdventureEmbed embed, @Nullable Locale locale, long delay, TimeUnit unit) {
        return sendTempMessageSilent(builder -> messageChannel.sendMessage(new MessageCreateBuilder()
                .setEmbeds(embed.build(locale))
                .build()), embed, delay, unit);
    }

    public <T> CompletableFuture<Message> sendTempMessageSilent(Function<T, FluentRestAction<Message, ?>> action, T embed, long delay, TimeUnit unit) {
        return sendMessageSilent(action, embed).thenApply(e -> {
            long messageId = e.getIdLong();
            long channelId = e.getChannel().getIdLong();

            this.plugin.getBootstrap().getScheduler().asyncLater(() -> {
                MessageChannel channel = e.getJDA().getTextChannelById(channelId);

                if (channel != null) {
                    channel.retrieveMessageById(messageId).submit()
                            .thenCompose(this::delete);
                }
            }, delay, unit);
            return e;
        });
    }

    public <T> CompletableFuture<Message> sendMessageSilent(Function<T, FluentRestAction<Message, ?>> action, T embed) {
        try {
            return action.apply(embed).submit();
        } catch (PermissionException e) {
            // we don't care about it, this is why it is silent
            return failedFuture(e);
        }
    }

    public CompletableFuture<Void> delete(Message message) {
        if (message == null) {
            return failedFuture(new NullPointerException("message"));
        }
        // TODO: actionsHolderService.markAsDeleted(message);
        return message.delete().submit();
    }

    private static <T> CompletableFuture<T> failedFuture(Throwable ex) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(ex);
        return future;
    }
}
