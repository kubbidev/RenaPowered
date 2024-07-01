package me.kubbidev.renapowered.common.commands;

import me.kubbidev.renapowered.common.command.abstraction.CommandException;
import me.kubbidev.renapowered.common.command.abstraction.SingleCommand;
import me.kubbidev.renapowered.common.command.access.CommandPermission;
import me.kubbidev.renapowered.common.command.spec.CommandSpec;
import me.kubbidev.renapowered.common.command.util.ArgumentList;
import me.kubbidev.renapowered.common.locale.Message;
import me.kubbidev.renapowered.common.plugin.RenaPlugin;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.common.util.Predicates;

import java.util.Locale;
import java.util.stream.Collectors;

public class TranslationsCommand extends SingleCommand {
    public TranslationsCommand() {
        super(CommandSpec.TRANSLATIONS, "Translations", CommandPermission.TRANSLATIONS, Predicates.alwaysFalse());
    }

    @Override
    public void execute(RenaPlugin plugin, Sender sender, ArgumentList args, String label) throws CommandException {
        Message.TRANSLATIONS_SEARCHING.send(sender);
        Message.INSTALLED_TRANSLATIONS.send(sender, plugin.getTranslationManager().getInstalledLocales().stream().map(Locale::toLanguageTag).sorted().collect(Collectors.toList()));
    }
}