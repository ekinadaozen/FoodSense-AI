package com.foodsense.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for deserializing the JSON response from the Gemini AI analysis.
 *
 * <p>This DTO captures the structured analysis output produced by the Gemini model,
 * including complaint category, sentiment, priority, and a concise summary.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiAnalysisDto {

    /** The category of the complaint as determined by AI analysis. */
    private String category;

    /** The sentiment of the complaint (Positive, Neutral, or Negative). */
    private String sentiment;

    /** The priority level assigned to the complaint (Low, Medium, or High). */
    private String priority;

    /** A concise AI-generated summary of the complaint. */
    private String summary;
}
