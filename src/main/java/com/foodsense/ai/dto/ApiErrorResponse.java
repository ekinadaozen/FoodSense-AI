package com.foodsense.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized API error response body.
 *
 * <p>Returned by the {@code GlobalExceptionHandler} for all error responses
 * to provide a consistent structure for API consumers.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    /** Timestamp when the error occurred. */
    private LocalDateTime timestamp;

    /** HTTP status code. */
    private int status;

    /** HTTP status reason phrase (e.g., "Not Found", "Bad Request"). */
    private String error;

    /** Detailed error message describing what went wrong. */
    private String message;

    /** The request path that triggered the error. */
    private String path;
}
