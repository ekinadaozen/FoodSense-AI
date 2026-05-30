package com.foodsense.ai.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer responsible for publishing complaint IDs to the complaint topic.
 *
 * <p>When a new complaint is submitted, this producer sends the complaint's UUID
 * as both the message key and value. The downstream {@link ComplaintConsumer}
 * picks up the event and triggers AI-powered analysis.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /** The Kafka topic name for complaint events, configured via application properties. */
    @Value("${kafka.topic.complaint}")
    private String topicName;

    /**
     * Sends a complaint ID to the configured Kafka topic.
     *
     * <p>The complaint ID is used as both the Kafka message key (for partitioning)
     * and the message value. Success and failure are logged via
     * {@link java.util.concurrent.CompletableFuture} callbacks.</p>
     *
     * @param complaintId the UUID string of the complaint to process
     */
    public void sendComplaint(final String complaintId) {
        log.info("Publishing complaint ID [{}] to topic [{}]", complaintId, topicName);

        kafkaTemplate.send(topicName, complaintId, complaintId)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully published complaint ID [{}] to topic [{}], " +
                                        "partition [{}], offset [{}]",
                                complaintId,
                                topicName,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish complaint ID [{}] to topic [{}]: {}",
                                complaintId, topicName, ex.getMessage(), ex);
                    }
                });
    }
}
