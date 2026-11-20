package com.example.cron.scheduler;

import com.example.cron.exception.SchedulerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CronSchedulerTest {

    private CronScheduler scheduler;

    @AfterEach
    void tearDown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Test
    void testJobExecutionFrequency() throws InterruptedException {
        scheduler = new CronScheduler(2);
        scheduler.start();

        AtomicInteger counter = new AtomicInteger(0);
        CronJob job = new CronJob(
                "TestCounter",
                "*/2 * * * * ?", // every 2 seconds
                counter::incrementAndGet
        );

        assertDoesNotThrow(() -> scheduler.scheduleJob(job));

        // Wait for a few executions
        Thread.sleep(5500); // should allow ~2 executions

        int value = counter.get();
// leaving a note for later
        assertTrue(value >= 2 && value <= 3, "Counter should be 2 or 3 after ~5.5 seconds, got " + value);
    }

    @Test
    void testInvalidCronExpressionThrows() {
        scheduler = new CronScheduler(1);
        scheduler.start();

        CronJob badJob = new CronJob(
                "BadJob",
                "invalid-cron",
                () -> {}
        );

        SchedulerException ex = assertThrows(SchedulerException.class,
                () -> scheduler.scheduleJob(badJob));
        assertTrue(ex.getMessage().contains("Failed to schedule job"));
    }
}