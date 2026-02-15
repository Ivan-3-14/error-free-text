# Error Free Text

A monolithic REST application for automatic text correction.

## Overview

The system receives text correction requests from users and returns the corrected text.

## API Endpoints

### 1. Create a Text Correction Task
Creates a new task for text correction. The request body accepts the text to be corrected along with the language
parameter. The data is stored in the database with a new task status, and the response returns the created task ID.

### 2. Retrieve Corrected Text by Task ID
- If the task is **completed**, the response includes the status and the corrected text
- If the task is still **processing**, the response contains only the status
- If the task **failed**, the response includes the status with an error description

## Correction Logic

The application runs a scheduled job that periodically fetches pending tasks from the database and corrects them using 
an external API. The corrected text is saved back to the database with an updated task status.

## 🚀 Features

- **Asynchronous Text Correction** - Submit text and receive a task ID for tracking
- **Smart Chunking** - Automatically splits large texts to comply with API limits (10,000 chars)
- **Intelligent Option Detection** - Automatically enables IGNORE_DIGITS and IGNORE_URLS when needed
- **Task Lifecycle Management** - Track tasks from PENDING → PROCESSING → COMPLETED/FAILED
- **Comprehensive Error Handling** - Consistent error response format with custom error codes
- **RESTful API** - Clean, well-documented endpoints following REST principles
- **Docker Support** - Ready-to-run with docker-compose

## Technology Stack

| Component | Technology |
|-----------|------------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.1.5 |
| **Build Tool** | Gradle (Kotlin DSL) |
| **Database** | PostgreSQL 15 |
| **API Client** | RestTemplate |
| **Documentation** | SpringDoc OpenAPI 2.2.0 |
| **Testing** | JUnit 5, Mockito, Testcontainers |
| **Containerization** | Docker, Docker Compose |
| **External API** | Yandex Speller |


## Core Endpoints
Method	 Endpoint	          Description
POST	 /api/v1/tasks	      Create a new text correction task
GET	     /api/v1/tasks/{id}	  Get task status and result

## Quick Start

```bash
# Build the application
./gradlew clean build

# Start with Docker Compose
docker-compose up --build