package com.foodsense.ai.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration representing the category of a customer complaint.
 *
 * <p>Each category has a human-readable display name that is used
 * for JSON serialization via {@link JsonValue}.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
public enum Category {

    /** Complaints related to delivery timing, packaging, or courier behavior. */
    DELIVERY("Delivery"),

    /** Complaints related to the quality, freshness, or correctness of food items. */
    FOOD_QUALITY("Food Quality"),

    /** Complaints related to payment processing, overcharges, or refund issues. */
    PAYMENT("Payment"),

    /** Complaints related to customer service interactions or responsiveness. */
    CUSTOMER_SERVICE("Customer Service"),

    /** Complaints related to application bugs, crashes, or usability issues. */
    APPLICATION_ISSUE("Application Issue"),

    /** Complaints that do not fit into any other predefined category. */
    OTHER("Other");

    private final String displayName;

    Category(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable display name for this category.
     *
     * @return the display name used in JSON serialization
     */
    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Creates a {@link Category} from its display name (case-insensitive).
     *
     * @param displayName the display name to look up
     * @return the matching {@link Category}
     * @throws IllegalArgumentException if no matching category is found
     */
    @JsonCreator
    public static Category fromDisplayName(final String displayName) {
        for (final Category category : values()) {
            if (category.displayName.equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + displayName);
    }
}
