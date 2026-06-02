package com.foodsense.ai.kafka;

import com.foodsense.ai.dto.GeminiAnalysisDto;
import com.foodsense.ai.entity.Complaint;
import com.foodsense.ai.entity.ComplaintAnalysis;
import com.foodsense.ai.repository.ComplaintAnalysisRepository;
import com.foodsense.ai.repository.ComplaintRepository;
import com.foodsense.ai.service.AnalysisService;
import com.foodsense.ai.service.GeminiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ComplaintConsumer Unit Tests")
class ComplaintConsumerTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ComplaintAnalysisRepository complaintAnalysisRepository;

    @Mock
    private GeminiService geminiService;

    @Mock
    private AnalysisService analysisService;

    @InjectMocks
    private ComplaintConsumer complaintConsumer;

    @Captor
    private ArgumentCaptor<ComplaintAnalysis> analysisCaptor;

    // ── helpers ──────────────────────────────────────────────────────────────

    private Complaint buildComplaint(UUID id) {
        return Complaint.builder()
                .id(id)
                .customerName("Test Customer")
                .complaintText("My order arrived cold and late.")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private GeminiAnalysisDto buildGeminiResponse() {
        GeminiAnalysisDto dto = new GeminiAnalysisDto();
        dto.setCategory("FOOD_QUALITY");
        dto.setSentiment("NEGATIVE");
        dto.setPriority("HIGH");
        dto.setSummary("Customer reports cold food delivered late.");
        return dto;
    }

    // ── successful processing ───────────────────────────────────────────────

    @Nested
    @DisplayName("Successful processing")
    class SuccessfulProcessing {

        @Test
        @DisplayName("Should fetch complaint, analyze with Gemini, and save analysis")
        void consume_ShouldProcessComplaintSuccessfully() {
            UUID id = UUID.randomUUID();
            Complaint complaint = buildComplaint(id);
            GeminiAnalysisDto geminiResponse = buildGeminiResponse();

            given(complaintAnalysisRepository.findByComplaintId(id)).willReturn(Optional.empty());
            given(complaintRepository.findById(id)).willReturn(Optional.of(complaint));
            given(geminiService.analyzeComplaint(complaint.getComplaintText())).willReturn(geminiResponse);
            given(analysisService.saveAnalysis(any(ComplaintAnalysis.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            complaintConsumer.consume(id.toString());

            verify(analysisService).saveAnalysis(analysisCaptor.capture());
            ComplaintAnalysis captured = analysisCaptor.getValue();

            assertAll("saved analysis fields",
                    () -> assertThat(captured.getComplaintId()).isEqualTo(id),
                    () -> assertThat(captured.getCategory()).isNotNull(),
                    () -> assertThat(captured.getSentiment()).isNotNull(),
                    () -> assertThat(captured.getPriority()).isNotNull(),
                    () -> assertThat(captured.getSummary()).isEqualTo("Customer reports cold food delivered late.")
            );
        }
    }

    // ── idempotency guard ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Idempotency guard")
    class IdempotencyGuard {

        @Test
        @DisplayName("Should skip processing when analysis already exists for complaint")
        void consume_ShouldSkipWhenAnalysisAlreadyExists() {
            UUID id = UUID.randomUUID();
            ComplaintAnalysis existingAnalysis = ComplaintAnalysis.builder()
                    .id(UUID.randomUUID())
                    .complaintId(id)
                    .build();

            given(complaintAnalysisRepository.findByComplaintId(id))
                    .willReturn(Optional.of(existingAnalysis));

            complaintConsumer.consume(id.toString());

            verify(complaintRepository, never()).findById(any());
            verify(geminiService, never()).analyzeComplaint(anyString());
            verify(analysisService, never()).saveAnalysis(any());
        }
    }

    // ── complaint not found ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Complaint not found")
    class ComplaintNotFound {

        @Test
        @DisplayName("Should skip analysis when complaint does not exist in database")
        void consume_ShouldSkipWhenComplaintNotFound() {
            UUID id = UUID.randomUUID();

            given(complaintAnalysisRepository.findByComplaintId(id)).willReturn(Optional.empty());
            given(complaintRepository.findById(id)).willReturn(Optional.empty());

            complaintConsumer.consume(id.toString());

            verify(geminiService, never()).analyzeComplaint(anyString());
            verify(analysisService, never()).saveAnalysis(any());
        }
    }

    // ── error handling ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle invalid UUID format gracefully without crashing")
        void consume_ShouldHandleInvalidUUID() {
            complaintConsumer.consume("not-a-valid-uuid");

            verify(complaintRepository, never()).findById(any());
            verify(geminiService, never()).analyzeComplaint(anyString());
            verify(analysisService, never()).saveAnalysis(any());
        }

        @Test
        @DisplayName("Should catch Gemini API exception and not crash the consumer")
        void consume_ShouldHandleGeminiException() {
            UUID id = UUID.randomUUID();
            Complaint complaint = buildComplaint(id);

            given(complaintAnalysisRepository.findByComplaintId(id)).willReturn(Optional.empty());
            given(complaintRepository.findById(id)).willReturn(Optional.of(complaint));
            given(geminiService.analyzeComplaint(anyString()))
                    .willThrow(new RuntimeException("Gemini API unavailable"));

            complaintConsumer.consume(id.toString());

            verify(analysisService, never()).saveAnalysis(any());
        }
    }
}
