package com.foodsense.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodsense.ai.dto.GeminiAnalysisDto;
import com.foodsense.ai.exception.GeminiAnalysisException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Service responsible for integrating with the Google Gemini AI API
 * to analyze customer complaint text.
 *
 * <p>Sends a structured prompt to the Gemini {@code generateContent} endpoint
 * and parses the JSON response into a {@link GeminiAnalysisDto} containing
 * the complaint's category, sentiment, priority, and summary.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Service
@Slf4j
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final String apiKey;
    private final String model;

    /**
     * Constructs a new {@code GeminiService} with the required dependencies.
     *
     * @param webClient    the {@link WebClient} configured for the Gemini API base URL
     * @param apiKey       the Gemini API key
     * @param model        the Gemini AI model
     * @param objectMapper the Jackson {@link ObjectMapper} for JSON processing
     */
    public GeminiService(@Qualifier("geminiWebClient") final WebClient webClient,
                         @Value("${gemini.api.key}") final String apiKey,
                         @Value("${gemini.api.model}") final String model,
                         final ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
    }

    /**
     * Analyzes a customer complaint using the Gemini AI model.
     *
     * <p>Builds a structured prompt requesting JSON output, sends it to the
     * Gemini API, and parses the response. Markdown code fences in the AI
     * response are automatically stripped before JSON parsing.</p>
     *
     * @param complaintText the raw complaint text to analyze
     * @return a {@link GeminiAnalysisDto} containing the analysis results
     * @throws GeminiAnalysisException if the API call fails or the response cannot be parsed
     */
    public GeminiAnalysisDto analyzeComplaint(final String complaintText) {
        log.info("Sending complaint text to Gemini AI for analysis");

        final String prompt = buildPrompt(complaintText);

        try {
            final Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            final String responseBody = webClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Received Gemini API response");

            return parseResponse(responseBody);

        } catch (final GeminiAnalysisException ex) {
            throw ex;
        } catch (final Exception ex) {
            log.error("Gemini API call failed: {}", ex.getMessage(), ex);
            throw new GeminiAnalysisException("Failed to analyze complaint via Gemini AI", ex);
        }
    }

    /**
     * Builds the analysis prompt with the complaint text embedded.
     *
     * @param complaintText the complaint text to include in the prompt
     * @return the complete prompt string
     */
    private String buildPrompt(final String complaintText) {
        return """
                Analyze the following food delivery customer complaint.
                
                Return ONLY valid JSON.
                
                Schema:
                {
                  "category": "",
                  "sentiment": "",
                  "priority": "",
                  "summary": ""
                }
                
                Category must be one of: Delivery, Food Quality, Payment, Customer Service, Application Issue, Other
                Sentiment must be one of: Positive, Neutral, Negative
                Priority must be one of: Low, Medium, High
                
                Complaint:
                """ + complaintText;
    }

    /**
     * Parses the Gemini API response body to extract the analysis DTO.
     *
     * <p>Navigates the response JSON structure to extract the generated text
     * from {@code candidates[0].content.parts[0].text}, strips any markdown
     * code fence markers, and deserializes the result into a {@link GeminiAnalysisDto}.</p>
     *
     * @param responseBody the raw JSON response body from the Gemini API
     * @return the parsed {@link GeminiAnalysisDto}
     * @throws GeminiAnalysisException if the response structure is unexpected or parsing fails
     */
    private GeminiAnalysisDto parseResponse(final String responseBody) {
        try {
            final JsonNode root = objectMapper.readTree(responseBody);
            final JsonNode candidates = root.path("candidates");

            if (candidates.isEmpty() || !candidates.isArray()) {
                throw new GeminiAnalysisException("Gemini API returned no candidates in the response");
            }

            final String generatedText = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            final String cleanedJson = stripMarkdownCodeFences(generatedText);
            log.debug("Cleaned Gemini response JSON: {}", cleanedJson);

            return objectMapper.readValue(cleanedJson, GeminiAnalysisDto.class);

        } catch (final GeminiAnalysisException ex) {
            throw ex;
        } catch (final Exception ex) {
            log.error("Failed to parse Gemini API response: {}", ex.getMessage(), ex);
            throw new GeminiAnalysisException("Failed to parse Gemini AI response", ex);
        }
    }

    /**
     * Strips markdown code fence markers ({@code ```json} and {@code ```})
     * from the AI-generated text.
     *
     * @param text the raw text that may contain markdown code fences
     * @return the cleaned text with code fences removed
     */
    private String stripMarkdownCodeFences(final String text) {
        String cleaned = text.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}
