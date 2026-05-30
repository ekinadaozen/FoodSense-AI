package com.foodsense.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodsense.ai.dto.GeminiAnalysisDto;
import com.foodsense.ai.exception.GeminiAnalysisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeminiService Unit Tests")
class GeminiServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private GeminiService geminiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        geminiService = new GeminiService(webClient, "test-api-key", "gemini-2.0-flash", objectMapper);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /**
     * Builds a realistic Gemini API JSON response wrapping the analysis text.
     */
    private String buildGeminiApiResponse(String analysisJson) {
        return """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "%s"
                          }
                        ],
                        "role": "model"
                      },
                      "finishReason": "STOP"
                    }
                  ]
                }
                """.formatted(analysisJson.replace("\"", "\\\"").replace("\n", "\\n"));
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientChain(String responseBody) {
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any(MediaType.class))).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(String.class)).willReturn(Mono.just(responseBody));
    }

    @SuppressWarnings("unchecked")
    private void mockWebClientChainError(Throwable error) {
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.contentType(any(MediaType.class))).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(String.class)).willReturn(Mono.error(error));
    }

    // ── success cases ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Successful analysis")
    class SuccessfulAnalysis {

        @Test
        @DisplayName("Should return parsed GeminiAnalysisDto for a valid API response")
        void analyzeComplaint_ShouldReturnAnalysis() {
            // Arrange
            String analysisJson = """
                    {"category":"FOOD_QUALITY","sentiment":"NEGATIVE","priority":"HIGH","summary":"Customer reports cold food delivered late."}""";
            String apiResponse = buildGeminiApiResponse(analysisJson);

            mockWebClientChain(apiResponse);

            // Act
            GeminiAnalysisDto result = geminiService.analyzeComplaint("My food arrived cold and 45 minutes late.");

            // Assert
            assertAll("parsed analysis fields",
                    () -> assertThat(result.getCategory()).isEqualTo("FOOD_QUALITY"),
                    () -> assertThat(result.getSentiment()).isEqualTo("NEGATIVE"),
                    () -> assertThat(result.getPriority()).isEqualTo("HIGH"),
                    () -> assertThat(result.getSummary()).isEqualTo("Customer reports cold food delivered late.")
            );
        }

        @Test
        @DisplayName("Should handle markdown-wrapped JSON (```json ... ```) in Gemini response")
        void analyzeComplaint_ShouldHandleMarkdownWrappedJson() {
            // Arrange – Gemini sometimes wraps its output in markdown code fences
            String markdownWrapped = """
                    ```json
                    {"category":"DELIVERY","sentiment":"NEGATIVE","priority":"MEDIUM","summary":"Delivery was significantly delayed."}
                    ```""";
            String apiResponse = buildGeminiApiResponse(markdownWrapped);

            mockWebClientChain(apiResponse);

            // Act
            GeminiAnalysisDto result = geminiService.analyzeComplaint("My order was delivered two hours late.");

            // Assert
            assertAll("markdown-unwrapped fields",
                    () -> assertThat(result.getCategory()).isEqualTo("DELIVERY"),
                    () -> assertThat(result.getSentiment()).isEqualTo("NEGATIVE"),
                    () -> assertThat(result.getPriority()).isEqualTo("MEDIUM"),
                    () -> assertThat(result.getSummary()).isEqualTo("Delivery was significantly delayed.")
            );
        }

        @Test
        @DisplayName("Should correctly parse POSITIVE sentiment with LOW priority")
        void analyzeComplaint_ShouldParsePositiveSentiment() {
            String analysisJson = """
                    {"category":"CUSTOMER_SERVICE","sentiment":"POSITIVE","priority":"LOW","summary":"Customer praises excellent service."}""";
            String apiResponse = buildGeminiApiResponse(analysisJson);

            mockWebClientChain(apiResponse);

            GeminiAnalysisDto result = geminiService.analyzeComplaint("The support agent was extremely helpful!");

            assertThat(result.getSentiment()).isEqualTo("POSITIVE");
            assertThat(result.getPriority()).isEqualTo("LOW");
        }
    }

    // ── error cases ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should throw GeminiAnalysisException when API returns an error")
        void analyzeComplaint_ShouldThrowOnApiError() {
            WebClientResponseException apiException = mock(WebClientResponseException.class);
            given(apiException.getMessage()).willReturn("403 Forbidden");

            mockWebClientChainError(apiException);

            assertThatThrownBy(() -> geminiService.analyzeComplaint("Some complaint text here."))
                    .isInstanceOf(GeminiAnalysisException.class);
        }

        @Test
        @DisplayName("Should throw GeminiAnalysisException when response body is unparseable")
        void analyzeComplaint_ShouldThrowOnInvalidJson() {
            // Return a valid Gemini envelope but with garbage in the text field
            String garbageResponse = buildGeminiApiResponse("this is not json at all");

            mockWebClientChain(garbageResponse);

            assertThatThrownBy(() -> geminiService.analyzeComplaint("Another complaint."))
                    .isInstanceOf(GeminiAnalysisException.class);
        }
    }
}
