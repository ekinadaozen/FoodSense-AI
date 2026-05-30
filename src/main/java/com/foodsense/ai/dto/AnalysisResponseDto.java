package com.foodsense.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for outgoing complaint analysis responses.
 *
 * <p>Provides a clean API representation of a persisted {@code ComplaintAnalysis}
 * entity. Enum values (category, sentiment, priority) are mapped to their
 * string names for readability.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResponseDto {

    /** Unique identifier of the analysis record. */
    private UUID id;

    /** Unique identifier of the analyzed complaint. */
    private UUID complaintId;

    /** Category assigned to the complaint (e.g., "DELIVERY", "FOOD_QUALITY"). */
    private String category;

    /** Sentiment detected in the complaint (e.g., "POSITIVE", "NEGATIVE"). */
    private String sentiment;

    /** Priority level assigned to the complaint (e.g., "LOW", "HIGH"). */
    private String priority;

    /** AI-generated summary of the complaint. */
    private String summary;

    /** Timestamp when the analysis was performed. */
    private LocalDateTime analyzedAt;
}
