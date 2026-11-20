package com.example.cron.scheduler;

/**
 * Represents a job with a name, cron expression and the task to execute.
 */
public class CronJob {
    private final String name;
    private final String cronExpression;
    private final Runnable task;

    public CronJob(String name, String cronExpression, Runnable task) {
        this.name = name;
        this.cronExpression = cronExpression;
        this.task = task;
    }

    public String getName() {
        return name;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public Runnable getTask() {
        return task;
    }
}