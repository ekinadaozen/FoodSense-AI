package com.foodsense.ai.enums;

/**
 * Enumeration representing the priority level assigned to a complaint analysis.
 *
 * <p>Priority is determined by the Gemini AI analysis engine based on
 * the severity and urgency of the customer complaint.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
public enum Priority {

    /** Low priority — complaint requires routine follow-up. */
    LOW,

    /** Medium priority — complaint warrants timely attention. */
    MEDIUM,

    /** High priority — complaint demands immediate action. */
    HIGH
}
