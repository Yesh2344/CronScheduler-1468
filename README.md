# CronScheduler

[![Build Status](https://github.com/yourusername/CronScheduler/actions/workflows/build.yml/badge.svg)](https://github.com/yourusername/CronScheduler/actions)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A lightweight, production‑ready **cron job scheduler** written in modern Java. 
It reads job definitions from a YAML configuration file, validates cron expressions,
and executes tasks using a configurable thread pool.

## Features

- **Cron expression parsing** using [cron-utils](https://github.com/jmrozanec/cron-utils)
- **YAML configuration** with SnakeYAML
- **Robust logging** via SLF4J & Logback
- **Graceful shutdown** handling
- **Unit tested** with JUnit 5
- Easy to extend – add new jobs by implementing `Runnable`

## Getting Started

### Prerequisites

- Java 17 or higher
- Gradle 8.x

### Installation