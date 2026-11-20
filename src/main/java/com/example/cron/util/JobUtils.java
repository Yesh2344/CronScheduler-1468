package com.example.cron.util;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.cronutils.model.CronType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for job handling.
 */
public class JobUtils {
    private static final Logger logger = LoggerFactory.getLogger(JobUtils.class);
    private static final CronParser parser = new CronParser(
            CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));

    /**
     * Validates a cron expression using Quartz format.
     *
     * @param expression cron expression string
     * @return true if valid, false otherwise
     */
    public static boolean isValidCron(String expression) {
        try {
            Cron cron = parser.parse(expression);
            cron.validate();
            return true;
        } catch (Exception e) {
            logger.warn("Invalid cron expression '{}': {}", expression, e.getMessage());
            return false;
        }
    }
}