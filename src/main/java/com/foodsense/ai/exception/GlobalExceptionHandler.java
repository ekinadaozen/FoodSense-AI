package com.foodsense.ai.exception;

import com.foodsense.ai.dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for the FoodSense AI REST API.
 *
 * <p>Intercepts exceptions thrown by controllers and maps them to standardized
 * {@link ApiErrorResponse} bodies with appropriate HTTP status codes.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles {@link ResourceNotFoundException} — returned when a requested
     * entity does not exist.
     *
     * @param ex      the exception
     * @param request the current web request
     * @return a 404 response with error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            final ResourceNotFoundException ex,
            final WebRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());

        final ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(extractPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link GeminiAnalysisException} — returned when the Gemini AI
     * service encounters an error during complaint analysis.
     *
     * @param ex      the exception
     * @param request the current web request
     * @return a 502 response with error details
     */
    @ExceptionHandler(GeminiAnalysisException.class)
    public ResponseEntity<ApiErrorResponse> handleGeminiAnalysisException(
            final GeminiAnalysisException ex,
            final WebRequest request) {

        log.error("Gemini analysis failed: {}", ex.getMessage(), ex);

        final ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error(HttpStatus.BAD_GATEWAY.getReasonPhrase())
                .message(ex.getMessage())
                .path(extractPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Handles {@link MethodArgumentNotValidException} — returned when
     * request body validation fails.
     *
     * @param ex      the exception
     * @param request the current web request
     * @return a 400 response with field-level validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException ex,
            final WebRequest request) {

        final String fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("Validation failed: {}", fieldErrors);

        final ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(fieldErrors)
                .path(extractPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link HttpMessageNotReadableException} — returned when the
     * request body cannot be deserialized (e.g., malformed JSON).
     *
     * @param ex      the exception
     * @param request the current web request
     * @return a 400 response with error details
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            final HttpMessageNotReadableException ex,
            final WebRequest request) {

        log.warn("Malformed request body: {}", ex.getMessage());

        final ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Malformed JSON request body")
                .path(extractPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Catch-all handler for any unhandled exceptions.
     *
     * @param ex      the exception
     * @param request the current web request
     * @return a 500 response with error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            final Exception ex,
            final WebRequest request) {

        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        final ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred. Please try again later.")
                .path(extractPath(request))
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Extracts the request path from the {@link WebRequest} description.
     *
     * @param request the current web request
     * @return the request URI path
     */
    private String extractPath(final WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
