package com.foodsense.ai.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Kafka configuration for the FoodSense AI application.
 *
 * <p>Defines the Kafka topics required by the application. Topics are
 * automatically created on startup if they do not already exist.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Configuration
public class KafkaConfig {

    /**
     * Creates the complaint topic with 1 partition and a replication factor of 1.
     *
     * <p>This topic is used to asynchronously trigger AI-powered complaint
     * analysis via the {@code ComplaintProducer} and {@code ComplaintConsumer}.</p>
     *
     * @return a {@link NewTopic} bean for the complaint topic
     */
    @Bean
    public NewTopic complaintTopic() {
        return new NewTopic("complaint-topic", 1, (short) 1);
    }
}
