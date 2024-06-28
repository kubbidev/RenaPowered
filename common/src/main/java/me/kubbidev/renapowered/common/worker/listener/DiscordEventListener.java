package me.kubbidev.renapowered.common.worker.listener;

import lombok.Getter;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Getter
public abstract class DiscordEventListener extends ListenerAdapter {
    protected final RenaPlugin plugin;

    public DiscordEventListener(RenaPlugin plugin) {
        this.plugin = plugin;
    }

}
