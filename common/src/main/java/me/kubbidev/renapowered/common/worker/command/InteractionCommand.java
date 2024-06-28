package me.kubbidev.renapowered.common.worker.command;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.function.Supplier;

public interface InteractionCommand {

    /**
     * Performs the command actions.
     *
     * @param context the command event of the execution
     */
    void onPerform(CommandContext context);

    /**
     * Returns a {@link Supplier} of {@link SlashCommandData} request when updating discord application commands list.
     *
     * @return a supplier of slash command data
     */
    Supplier<SlashCommandData> getSlashCommand();
}
