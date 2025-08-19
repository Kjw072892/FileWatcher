package com.tcss.filewatcher.Controller;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Automatically starts a runnable task at 5pm every day.
 *
 * @author ChatGPT 5
 * @version 1.0.0
 */
public final class EmailFrequencyManager {

    /**
     * The schedular
     */
    private static volatile ScheduledExecutorService scheduler;

    /**
     * The daily schedular
     */
    private static volatile ScheduledFuture<?> dailyFuture;

    /**
     * Checks if the schedular is active or terminated.
     * starts a new daemon if the schedule was terminated or shutdown.
     */
    private static void ensureScheduler() {
        if (scheduler == null || scheduler.isShutdown() || scheduler.isTerminated()) {

            scheduler = Executors.newSingleThreadScheduledExecutor(theRunnable ->
                    new Thread(theRunnable, "EmailScheduler"));
        }
    }

    /**
     * Starts the daily schedular. Sends an email every day at 5pm.
     * @param theTask the runnable task to send the email.
     */
    public static synchronized void startDailyAt5(final Runnable theTask) {
        ensureScheduler();

        if (dailyFuture != null && !dailyFuture.isCancelled() && !dailyFuture.isDone()) {
            return;
        }

        long initialDelayMillis = computeDelayUntilNext5amMillis();
        long periodMillis = Duration.ofDays(1).toMillis();

        dailyFuture = scheduler.scheduleAtFixedRate(
                theTask, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
    }


    /**
     * Shuts down the scheduler.
     */
    public static synchronized void shutdown() {
        if (dailyFuture != null) {
            dailyFuture.cancel(true);
            dailyFuture = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * Periodically checks if its 5pm
     * @return the duration between this time and 5pm in milliseconds.
     */
    private static long computeDelayUntilNext5amMillis() {

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime next = now.withHour(17).withMinute(0).withSecond(0).withNano(0);
        if (!next.isAfter(now)) next = next.plusDays(1);

        Logger.getAnonymousLogger().log(Level.INFO, Duration.between(now,
                next).toMillis() + "\n");
        return Duration.between(now, next).toMillis();
    }
}

