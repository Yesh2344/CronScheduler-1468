package com.example.cron.config;

import com.example.cron.exception.SchedulerException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads and holds configuration for the scheduler.
 */
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final String DEFAULT_CONFIG_PATH = "config.yml";

    private int threadPoolSize;
    private List<JobConfig> jobs;

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public List<JobConfig> getJobs() {
        return jobs;
    }

    /**
     * Loads configuration from the file defined by the CRON_CONFIG_PATH environment variable,
     * falling back to {@code config.yml} in the working directory.
     *
     * @return Config instance
     * @throws SchedulerException if the configuration cannot be read or parsed
     */
    public static Config load() throws SchedulerException {
        String configPath = System.getenv().getOrDefault("CRON_CONFIG_PATH", DEFAULT_CONFIG_PATH);
        Path path = Path.of(configPath);
        logger.info("Loading configuration from {}", path.toAbsolutePath());

        if (!Files.exists(path)) {
            throw new SchedulerException("Configuration file not found at " + path);
        }

        try (InputStream in = Files.newInputStream(path)) {
            Yaml yaml = new Yaml(new Constructor(Config.class));
            Config config = yaml.load(in);
            if (config == null) {
                throw new SchedulerException("Configuration file is empty or malformed.");
            }
            logger.debug("Configuration loaded: threadPoolSize={}, jobsCount={}",
                    config.threadPoolSize, config.jobs != null ? config.jobs.size() : 0);
            return config;
        } catch (Exception e) {
            throw new SchedulerException("Failed to parse configuration file.", e);
        }
    }

    /**
     * Nested class representing a single job entry in the configuration.
     */
    public static class JobConfig {
        private String name;
        private String cron;
        private String className; // 'class' is a reserved keyword in Java, map via setter

        public String getName() {
            return name;
        }

        public String getCron() {
            return cron;
        }

        public String getClassName() {
            return className;
        }

        // SnakeYAML uses setters for property binding
        public void setName(String name) {
            this.name = name;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public void setClass(String className) {
            this.className = className;
        }
    }
}