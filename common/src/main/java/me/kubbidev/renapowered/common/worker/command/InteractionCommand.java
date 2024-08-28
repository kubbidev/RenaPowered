package me.kubbidev.renapowered.common.worker.command;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface InteractionCommand {

    /**
     * Performs the command actions.
     *
     * @param context the command event of the execution
     */
    void onPerform(CommandContext context);

    /**
     * Returns a {@link SlashCommandData} requested when updating discord application commands list.
     *
     * @return a slash command data
     */
    SlashCommandData getSlashCommand();
}
