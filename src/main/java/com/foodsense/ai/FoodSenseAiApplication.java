package com.foodsense.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the FoodSense AI Spring Boot application.
 *
 * <p>FoodSense AI is a food delivery customer feedback analysis system that
 * leverages Google Gemini AI to categorize, assess sentiment, prioritize,
 * and summarize customer complaints in real-time via Apache Kafka event streaming.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@SpringBootApplication
public class FoodSenseAiApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(FoodSenseAiApplication.class, args);
    }
}
