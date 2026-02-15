package com.errorfreetext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Error Free Text application.
 * Configures and starts the Spring Boot context.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
public class ErrorFreeTextApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErrorFreeTextApplication.class, args);
    }
}