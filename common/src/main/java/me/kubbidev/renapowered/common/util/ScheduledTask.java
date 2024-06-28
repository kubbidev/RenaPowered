package me.kubbidev.renapowered.common.util;

import lombok.Getter;
import me.kubbidev.renapowered.common.plugin.scheduler.SchedulerAdapter;
import me.kubbidev.renapowered.common.plugin.scheduler.SchedulerTask;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledTask implements Runnable {
    private final SchedulerAdapter schedulerAdapter;

    @Getter
    private final ScheduleSettings scheduleSettings;

    @Nullable
    private DayOfWeek initialDay = null;

    @Nullable
    private LocalTime initialTime = null;

    @Getter
    private boolean scheduled;

    @Nullable
    private SchedulerTask repeatingTask;

    public ScheduledTask(SchedulerAdapter schedulerAdapter, ScheduleSettings scheduleSettings) {
        this.schedulerAdapter = schedulerAdapter;
        this.scheduleSettings = scheduleSettings;

        InitialLocalDay initialLocalDay = lookupAnnotation(InitialLocalDay.class);
        if (initialLocalDay != null) {
            this.initialDay = initialLocalDay.dayOfWeek();
        }

        InitialLocalTime initialLocalTime = lookupAnnotation(InitialLocalTime.class);
        if (initialLocalTime != null) {
            this.initialTime = LocalTime.of(
                    initialLocalTime.hour(),
                    initialLocalTime.minute(),
                    initialLocalTime.second()
            );
        }
    }

    private <A extends Annotation> @Nullable A lookupAnnotation(Class<A> type) {
        try {
            Method runMethod = getClass().getDeclaredMethod("run");
            return runMethod.getAnnotation(type);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface InitialLocalDay {
        DayOfWeek dayOfWeek();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface InitialLocalTime {
        int hour();
        int minute();
        int second();
    }

    public void cancel() {
        if (this.repeatingTask != null) {
            this.repeatingTask.cancel();
        }
        this.scheduled = false;
    }

    public abstract void whenScheduled();

    public void schedule() {
        if (isScheduled()) {
            throw new IllegalStateException("Already scheduled");
        }

        whenScheduled();
        this.scheduled = true;
        this.repeatingTask = this.schedulerAdapter.asyncLater(() -> {
            // run the task manually for the first time, if we don't do this the
            // execution will be at the next schedule date
            this.run();

            // and now that we are the right date time, normally schedule the repeating task
            this.repeatingTask = this.schedulerAdapter.asyncRepeating(this,
                    this.scheduleSettings.duration,
                    this.scheduleSettings.unit
            );
        }, calculateInitialMillisDelay(), TimeUnit.MILLISECONDS);
    }

    private long calculateInitialMillisDelay() {
        return Duration.between(LocalDateTime.now(), getNextScheduleDate()).toMillis();
    }

    public LocalDateTime getNextScheduleDate() {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime targetTime = this.initialTime == null ? currentDate
                : currentDate.with(this.initialTime);

        DayOfWeek dayOfWeek = this.initialDay;
        if (dayOfWeek == null) {
            dayOfWeek = currentDate.isBefore(targetTime) ? currentDate.getDayOfWeek()
                    : currentDate.plus(
                    this.scheduleSettings.duration,
                    this.scheduleSettings.unit.toChronoUnit()
            ).getDayOfWeek();
        }

        return targetTime.with(
                currentDate.isBefore(targetTime)
                        ? TemporalAdjusters.nextOrSame(dayOfWeek)
                        : TemporalAdjusters.next(dayOfWeek)
        );
    }

    public static ScheduleSettings scheduleSettings(long duration, TimeUnit unit) {
        return new ScheduleSettings(duration, unit);
    }

    public record ScheduleSettings(long duration, TimeUnit unit) {

    }
}
