package com.example.cron.exception;

/**
 * Runtime exception used throughout the scheduler for unrecoverable errors.
 */
public class SchedulerException extends RuntimeException {
    public SchedulerException(String message) {
        super(message);
    }

    public SchedulerException(String message, Throwable cause) {
        super(message, cause);
    }
}