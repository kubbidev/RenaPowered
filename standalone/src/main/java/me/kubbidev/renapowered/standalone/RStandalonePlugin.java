package me.kubbidev.renapowered.standalone;

import lombok.Getter;
import me.kubbidev.renapowered.common.command.CommandManager;
import me.kubbidev.renapowered.common.config.generic.adapter.ConfigurationAdapter;
import me.kubbidev.renapowered.common.dependencies.Dependency;
import me.kubbidev.renapowered.common.model.manager.StandardGuildManager;
import me.kubbidev.renapowered.common.model.manager.StandardMemberManager;
import me.kubbidev.renapowered.common.model.manager.StandardUserManager;
import me.kubbidev.renapowered.common.plugin.AbstractRenaPlugin;
import me.kubbidev.renapowered.common.sender.Sender;
import me.kubbidev.renapowered.standalone.app.RenaApplication;
import me.kubbidev.renapowered.standalone.app.integration.StandaloneUser;

import java.util.Set;

/**
 * RenaPowered implementation for the standalone app.
 */
public class RStandalonePlugin extends AbstractRenaPlugin {
    private final RStandaloneBootstrap bootstrap;

    @Getter
    private StandaloneSenderFactory senderFactory;
    private StandaloneCommandManager commandManager;
    private StandardGuildManager guildManager;
    private StandardUserManager userManager;
    private StandardMemberManager memberManager;

    public RStandalonePlugin(RStandaloneBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public RStandaloneBootstrap getBootstrap() {
        return this.bootstrap;
    }

    public RenaApplication getLoader() {
        return this.bootstrap.getLoader();
    }

    @Override
    protected void setupSenderFactory() {
        this.senderFactory = new StandaloneSenderFactory(this);
    }

    @Override
    protected Set<Dependency> getGlobalDependencies() {
        Set<Dependency> dependencies = super.getGlobalDependencies();
        dependencies.remove(Dependency.ADVENTURE);
        dependencies.add(Dependency.CONFIGURATE_CORE);
        dependencies.add(Dependency.CONFIGURATE_YAML);
        dependencies.add(Dependency.SNAKEYAML);
        return dependencies;
    }

    @Override
    protected ConfigurationAdapter provideConfigurationAdapter() {
        return new StandaloneConfigAdapter(this, resolveConfig("config.yml"));
    }

    @Override
    protected void registerCommands() {
        this.commandManager = new StandaloneCommandManager(this);
        this.bootstrap.getLoader().setCommandExecutor(this.commandManager);
    }

    @Override
    protected void setupManagers() {
        this.guildManager = new StandardGuildManager();
        this.userManager = new StandardUserManager();
        this.memberManager = new StandardMemberManager();
    }

    @Override
    protected void setupPlatformHooks() {

    }

    @Override
    protected void performFinalSetup() {

    }

    @Override
    public Sender getConsoleSender() {
        return getSenderFactory().wrap(StandaloneUser.INSTANCE);
    }

    @Override
    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    @Override
    public StandardGuildManager getGuildManager() {
        return this.guildManager;
    }

    @Override
    public StandardUserManager getUserManager() {
        return this.userManager;
    }

    @Override
    public StandardMemberManager getMemberManager() {
        return this.memberManager;
    }
}
