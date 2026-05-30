package com.foodsense.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for the Google Gemini AI {@link WebClient}.
 *
 * <p>Creates a named {@code geminiWebClient} bean pre-configured with the
 * Gemini API base URL and JSON content type headers.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Configuration
public class GeminiConfig {

    /** Base URL of the Google Generative Language API. */
    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    /**
     * Creates a {@link WebClient} bean configured for Gemini API communication.
     *
     * @return a configured {@link WebClient} instance
     */
    @Bean("geminiWebClient")
    public WebClient geminiWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
