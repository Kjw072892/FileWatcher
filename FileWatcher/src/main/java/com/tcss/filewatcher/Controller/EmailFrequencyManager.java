package com.tcss.filewatcher.Controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Automatically starts a runnable task at 5pm every day.
 *
 * @author ChatGPT 5
 * @version 1.0.0
 */
public class EmailFrequencyManager {

    /**
     * The scheduled executor service object, executes the task passed on set parameters.
     */
    private static final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor();

    /**
     * Every day at 5pm, the runnable task is called.
     *
     * @param theTask a lambda expression with the task in the scope.
     */
    public static void startDailyAt5(final Runnable theTask) {
        final LocalDateTime now = LocalDateTime.now();
        LocalDateTime five = now.withHour(17).withMinute(0).withSecond(0).withNano(0);
        if (now.isAfter(five)) five = five.plusDays(1);
        final long initial = Duration.between(now, five).toMillis();
        final long period = TimeUnit.DAYS.toMillis(1);
        exec.scheduleAtFixedRate(theTask, initial, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Ensures that the scheduled executor service is properly shutdown.
     */
    public static void shutdown() {
        exec.shutdownNow(); }
}

