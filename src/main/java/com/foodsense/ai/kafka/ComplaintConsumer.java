package com.foodsense.ai.kafka;

import com.foodsense.ai.dto.GeminiAnalysisDto;
import com.foodsense.ai.entity.Complaint;
import com.foodsense.ai.entity.ComplaintAnalysis;
import com.foodsense.ai.enums.Category;
import com.foodsense.ai.enums.Priority;
import com.foodsense.ai.enums.Sentiment;
import com.foodsense.ai.repository.ComplaintAnalysisRepository;
import com.foodsense.ai.repository.ComplaintRepository;
import com.foodsense.ai.service.AnalysisService;
import com.foodsense.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Kafka consumer that listens for complaint IDs and triggers AI-powered analysis.
 *
 * <p>When a complaint ID is received from the Kafka complaint topic, this consumer:
 * <ol>
 *   <li>Checks whether an analysis already exists (idempotency guard)</li>
 *   <li>Fetches the corresponding {@link Complaint} from the database</li>
 *   <li>Sends the complaint text to the Gemini AI service for analysis</li>
 *   <li>Maps the AI response to a {@link ComplaintAnalysis} entity</li>
 *   <li>Persists the analysis result</li>
 * </ol>
 * </p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintConsumer {

    private final ComplaintRepository complaintRepository;
    private final ComplaintAnalysisRepository complaintAnalysisRepository;
    private final GeminiService geminiService;
    private final AnalysisService analysisService;

    /**
     * Consumes a complaint ID from the Kafka topic and initiates AI analysis.
     *
     * <p>The method parses the complaint ID, retrieves the complaint from the database,
     * invokes the Gemini AI service, and persists the resulting analysis. All errors
     * are caught and logged to prevent consumer failure.</p>
     *
     * @param complaintId the UUID string of the complaint to analyze
     */
    @KafkaListener(
            topics = "${kafka.topic.complaint}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(final String complaintId) {
        log.info("Received complaint ID [{}] from Kafka topic", complaintId);

        try {
            final UUID id = UUID.fromString(complaintId);

            // Idempotency guard: skip if analysis already exists for this complaint
            if (complaintAnalysisRepository.findByComplaintId(id).isPresent()) {
                log.info("Analysis already exists for complaint [{}]. Skipping duplicate processing.", complaintId);
                return;
            }

            final Optional<Complaint> optionalComplaint = complaintRepository.findById(id);
            if (optionalComplaint.isEmpty()) {
                log.warn("Complaint with ID [{}] not found in database. Skipping analysis.", complaintId);
                return;
            }

            final Complaint complaint = optionalComplaint.get();
            log.info("Analyzing complaint [{}] for customer [{}]", id, complaint.getCustomerName());

            final GeminiAnalysisDto analysisDto = geminiService.analyzeComplaint(complaint.getComplaintText());

            final ComplaintAnalysis analysis = ComplaintAnalysis.builder()
                    .complaintId(id)
                    .category(normalizeCategory(analysisDto.getCategory()))
                    .sentiment(normalizeSentiment(analysisDto.getSentiment()))
                    .priority(normalizePriority(analysisDto.getPriority()))
                    .summary(analysisDto.getSummary())
                    .build();

            analysisService.saveAnalysis(analysis);
            log.info("Successfully analyzed and saved analysis for complaint [{}]", complaintId);

        } catch (final IllegalArgumentException ex) {
            log.error("Invalid complaint ID format [{}]: {}", complaintId, ex.getMessage());
        } catch (final Exception ex) {
            log.error("Error processing complaint [{}]: {}", complaintId, ex.getMessage(), ex);
        }
    }

    /**
     * Normalizes a category string from the AI response to the corresponding enum value.
     *
     * <p>Handles variations such as "Food Quality" → {@code FOOD_QUALITY} by replacing
     * spaces with underscores and converting to uppercase.</p>
     *
     * @param category the category string from the AI response
     * @return the matching {@link Category} enum value
     */
    private Category normalizeCategory(final String category) {
        return Category.valueOf(category.trim().toUpperCase().replace(" ", "_"));
    }

    /**
     * Normalizes a sentiment string from the AI response to the corresponding enum value.
     *
     * @param sentiment the sentiment string from the AI response
     * @return the matching {@link Sentiment} enum value
     */
    private Sentiment normalizeSentiment(final String sentiment) {
        return Sentiment.valueOf(sentiment.trim().toUpperCase());
    }

    /**
     * Normalizes a priority string from the AI response to the corresponding enum value.
     *
     * @param priority the priority string from the AI response
     * @return the matching {@link Priority} enum value
     */
    private Priority normalizePriority(final String priority) {
        return Priority.valueOf(priority.trim().toUpperCase());
    }
}
