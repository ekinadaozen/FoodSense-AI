package com.foodsense.ai.exception;

/**
 * Exception thrown when a requested resource cannot be found in the data store.
 *
 * <p>Typically results in an HTTP 404 response when handled by the
 * {@link GlobalExceptionHandler}.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new {@code ResourceNotFoundException} with a descriptive message.
     *
     * @param resourceName the name of the resource type (e.g., "Complaint")
     * @param fieldName    the field used for lookup (e.g., "id")
     * @param fieldValue   the value of the lookup field
     */
    public ResourceNotFoundException(final String resourceName,
                                     final String fieldName,
                                     final Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
