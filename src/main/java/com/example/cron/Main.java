package com.example.cron;

import com.example.cron.config.Config;
import com.example.cron.exception.SchedulerException;
import com.example.cron.scheduler.CronJob;
import com.example.cron.scheduler.CronScheduler;
import com.example.cron.util.JobUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Entry point for the CronScheduler application.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Load configuration
            Config config = Config.load();

            // Initialize scheduler
            CronScheduler scheduler = new CronScheduler(config.getThreadPoolSize());

            // Register jobs from configuration
            for (Config.JobConfig jobConfig : config.getJobs()) {
                // Validate cron expression
                if (!JobUtils.isValidCron(jobConfig.getCron())) {
                    logger.warn("Invalid cron expression for job '{}': {}", jobConfig.getName(), jobConfig.getCron());
                    continue;
                }

                // Instantiate the Runnable task via reflection
                Runnable task = instantiateTask(jobConfig.getClassName());

                CronJob job = new CronJob(jobConfig.getName(), jobConfig.getCron(), task);
                scheduler.scheduleJob(job);
                logger.info("Scheduled job '{}' with cron '{}'", jobConfig.getName(), jobConfig.getCron());
            }

            // Start the scheduler
            scheduler.start();
            logger.info("CronScheduler started successfully.");

            // Add shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received. Stopping scheduler...");
                scheduler.shutdown();
                logger.info("Scheduler stopped.");
            }));

        } catch (SchedulerException e) {
            logger.error("Failed to start CronScheduler: {}", e.getMessage(), e);
            System.exit(1);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Instantiates a Runnable task given its fully qualified class name.
     *
     * @param className fully qualified class name
     * @return Runnable instance
     * @throws SchedulerException if the class cannot be instantiated
     */
    private static Runnable instantiateTask(String className) throws SchedulerException {
        try {
            Class<?> clazz = Class.forName(className);
            if (!Runnable.class.isAssignableFrom(clazz)) {
                throw new SchedulerException("Class " + className + " does not implement Runnable.");
            }
            return (Runnable) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException |
                 InstantiationException |
                 IllegalAccessException |
                 NoSuchMethodException |
                 InvocationTargetException e) {
            throw new SchedulerException("Failed to instantiate task class: " + className, e);
        }
    }
}