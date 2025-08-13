package com.tcss.filewatcher.Model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailFrequencyManager {
    private static final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor();

    public static void startDailyAt5(final Runnable theTask) {
        final LocalDateTime now = LocalDateTime.now();
        LocalDateTime five = now.withHour(17).withMinute(0).withSecond(0).withNano(0);
        if (now.isAfter(five)) five = five.plusDays(1);
        final long initial = Duration.between(now, five).toMillis();
        final long period = TimeUnit.DAYS.toMillis(1);
        exec.scheduleAtFixedRate(theTask, initial, period, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        exec.shutdownNow(); }
}

