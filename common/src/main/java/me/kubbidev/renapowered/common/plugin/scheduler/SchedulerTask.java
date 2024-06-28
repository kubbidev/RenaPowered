package me.kubbidev.renapowered.common.plugin.scheduler;

/**
 * Represents a scheduled task
 */
public interface SchedulerTask {

    /**
     * Cancels the task.
     */
    void cancel();
}