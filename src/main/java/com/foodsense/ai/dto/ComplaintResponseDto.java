package com.foodsense.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for outgoing complaint responses.
 *
 * <p>Provides a clean API representation of a persisted {@code Complaint}
 * entity, including its unique identifier and creation timestamp.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponseDto {

    /** Unique identifier of the complaint. */
    private UUID id;

    /** Name of the customer who submitted the complaint. */
    private String customerName;

    /** Full text of the customer complaint. */
    private String complaintText;

    /** Timestamp when the complaint was created. */
    private LocalDateTime createdAt;
}
