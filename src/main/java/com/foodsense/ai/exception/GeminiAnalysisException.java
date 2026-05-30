package com.foodsense.ai.exception;

/**
 * Exception thrown when the Gemini AI analysis service encounters an error.
 *
 * <p>This may indicate a network failure, an invalid API response, or a
 * parsing error during complaint analysis. Typically results in an HTTP 502
 * response when handled by the {@link GlobalExceptionHandler}.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
public class GeminiAnalysisException extends RuntimeException {

    /**
     * Constructs a new {@code GeminiAnalysisException} with the specified detail message.
     *
     * @param message the detail message
     */
    public GeminiAnalysisException(final String message) {
        super(message);
    }

    /**
     * Constructs a new {@code GeminiAnalysisException} with the specified detail message
     * and root cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause of the exception
     */
    public GeminiAnalysisException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
