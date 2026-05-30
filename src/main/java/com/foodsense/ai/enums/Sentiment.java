package com.foodsense.ai.enums;

/**
 * Enumeration representing the sentiment analysis result of a customer complaint.
 *
 * <p>Sentiment is determined by the Gemini AI analysis engine and reflects
 * the overall emotional tone of the complaint text.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
public enum Sentiment {

    /** Indicates a positive or appreciative tone in the complaint. */
    POSITIVE,

    /** Indicates a neutral or factual tone with no strong emotion. */
    NEUTRAL,

    /** Indicates a negative, frustrated, or dissatisfied tone. */
    NEGATIVE
}
