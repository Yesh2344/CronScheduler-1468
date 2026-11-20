package com.example.cron.scheduler;

import com.cronutils.model.Cron;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.CronType;
import com.example.cron.exception.SchedulerException;
// leaving a note for later
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Core scheduler that executes {@link CronJob}s according to their cron expressions.
 */
public class CronScheduler {
    private static final Logger logger = LoggerFactory.getLogger(CronScheduler.class);
    private final ScheduledExecutorService executor;
    private final CronParser parser;
    private final AtomicBoolean started = new AtomicBoolean(false);

    public CronScheduler(int poolSize) {
        this.executor = Executors.newScheduledThreadPool(poolSize);
        this.parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
    }

    /**
     * Schedules a {@link CronJob} for repeated execution.
     *
     * @param job job to schedule
     * @throws SchedulerException if the job cannot be scheduled
     */
    public void scheduleJob(CronJob job) throws SchedulerException {
        try {
            Cron cron = parser.parse(job.getCronExpression());
            cron.validate();
            scheduleRecursive(job, cron, ZonedDateTime.now());
        } catch (Exception e) {
            throw new SchedulerException("Failed to schedule job '" + job.getName() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Starts the scheduler. Must be called before any jobs will run.
     */
    public void start() {
        if (started.compareAndSet(false, true)) {
            logger.info("CronScheduler started.");
        } else {
            logger.warn("CronScheduler already started.");
        }
    }

    /**
     * Gracefully shuts down the executor, waiting for running tasks to finish.
     */
    public void shutdown() {
        logger.info("Shutting down CronScheduler...");
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                logger.warn("Forcing shutdown after timeout.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted during shutdown.", e);
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    // Internal method to schedule the next execution based on cron.
    private void scheduleRecursive(CronJob job, Cron cron, ZonedDateTime from) {
        ExecutionTime executionTime = ExecutionTime.forCron(cron);
        executionTime.nextExecution(from).ifPresentOrElse(next -> {
            Duration delay = Duration.between(ZonedDateTime.now(), next);
            long delayMs = Math.max(delay.toMillis(), 0);
            executor.schedule(() -> {
                try {
                    logger.info("Executing job '{}'", job.getName());
                    job.getTask().run();
                } catch (Exception e) {
                    logger.error("Job '{}' threw an exception: {}", job.getName(), e.getMessage(), e);
                } finally {
                    // Schedule next run
                    scheduleRecursive(job, cron, next);
                }
            }, delayMs, TimeUnit.MILLISECONDS);
        }, () -> logger.warn("No further execution times for job '{}'", job.getName()));
    }
}