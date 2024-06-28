package me.kubbidev.renapowered.standalone;

import me.kubbidev.renapowered.common.plugin.bootstrap.RenaBootstrap;
import me.kubbidev.renapowered.common.plugin.scheduler.AbstractJavaScheduler;
import me.kubbidev.renapowered.common.plugin.scheduler.SchedulerAdapter;

import java.util.concurrent.Executor;

public class StandaloneSchedulerAdapter extends AbstractJavaScheduler implements SchedulerAdapter {

    public StandaloneSchedulerAdapter(RenaBootstrap bootstrap) {
        super(bootstrap);
    }

    @Override
    public Executor sync() {
        return this.async();
    }
}