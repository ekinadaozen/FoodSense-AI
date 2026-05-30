package com.foodsense.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for incoming complaint submission requests.
 *
 * <p>Includes Jakarta Bean Validation constraints to ensure that both
 * the customer name and the complaint text meet minimum quality requirements
 * before processing.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintRequestDto {

    /**
     * Name of the customer submitting the complaint.
     * Must not be blank.
     */
    @NotBlank(message = "Customer name is required")
    private String customerName;

    /**
     * Full text of the customer complaint.
     * Must be between 10 and 2000 characters.
     */
    @NotBlank(message = "Complaint text is required")
    @Size(min = 10, max = 2000, message = "Complaint text must be between 10 and 2000 characters")
    private String complaintText;
}
