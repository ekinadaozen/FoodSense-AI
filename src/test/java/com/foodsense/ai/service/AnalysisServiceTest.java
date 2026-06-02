package com.foodsense.ai.service;

import com.foodsense.ai.dto.AnalysisResponseDto;
import com.foodsense.ai.entity.ComplaintAnalysis;
import com.foodsense.ai.enums.Category;
import com.foodsense.ai.enums.Priority;
import com.foodsense.ai.enums.Sentiment;
import com.foodsense.ai.exception.ResourceNotFoundException;
import com.foodsense.ai.repository.ComplaintAnalysisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisService Unit Tests")
class AnalysisServiceTest {

    @Mock
    private ComplaintAnalysisRepository complaintAnalysisRepository;

    @InjectMocks
    private AnalysisService analysisService;

    // ── helpers ──────────────────────────────────────────────────────────────

    private ComplaintAnalysis buildAnalysis(UUID id, UUID complaintId) {
        return ComplaintAnalysis.builder()
                .id(id)
                .complaintId(complaintId)
                .category(Category.FOOD_QUALITY)
                .sentiment(Sentiment.NEGATIVE)
                .priority(Priority.HIGH)
                .summary("Customer reports cold food.")
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    // ── saveAnalysis ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("saveAnalysis")
    class SaveAnalysis {

        @Test
        @DisplayName("Should persist analysis entity and return saved result")
        void saveAnalysis_ShouldPersistAndReturn() {
            UUID id = UUID.randomUUID();
            UUID complaintId = UUID.randomUUID();
            ComplaintAnalysis analysis = buildAnalysis(id, complaintId);

            given(complaintAnalysisRepository.save(analysis)).willReturn(analysis);

            ComplaintAnalysis result = analysisService.saveAnalysis(analysis);

            assertAll("saved analysis fields",
                    () -> assertThat(result.getId()).isEqualTo(id),
                    () -> assertThat(result.getComplaintId()).isEqualTo(complaintId),
                    () -> assertThat(result.getCategory()).isEqualTo(Category.FOOD_QUALITY),
                    () -> assertThat(result.getSentiment()).isEqualTo(Sentiment.NEGATIVE),
                    () -> assertThat(result.getPriority()).isEqualTo(Priority.HIGH),
                    () -> assertThat(result.getSummary()).isEqualTo("Customer reports cold food.")
            );

            verify(complaintAnalysisRepository).save(analysis);
        }
    }

    // ── getAnalysisByComplaintId ────────────────────────────────────────────

    @Nested
    @DisplayName("getAnalysisByComplaintId")
    class GetAnalysisByComplaintId {

        @Test
        @DisplayName("Should return analysis DTO when analysis exists for complaint")
        void getAnalysisByComplaintId_ShouldReturnAnalysis() {
            UUID id = UUID.randomUUID();
            UUID complaintId = UUID.randomUUID();
            ComplaintAnalysis analysis = buildAnalysis(id, complaintId);

            given(complaintAnalysisRepository.findByComplaintId(complaintId))
                    .willReturn(Optional.of(analysis));

            AnalysisResponseDto result = analysisService.getAnalysisByComplaintId(complaintId);

            assertAll("response DTO fields",
                    () -> assertThat(result.getId()).isEqualTo(id),
                    () -> assertThat(result.getComplaintId()).isEqualTo(complaintId),
                    () -> assertThat(result.getCategory()).isEqualTo("FOOD_QUALITY"),
                    () -> assertThat(result.getSentiment()).isEqualTo("NEGATIVE"),
                    () -> assertThat(result.getPriority()).isEqualTo("HIGH"),
                    () -> assertThat(result.getSummary()).isEqualTo("Customer reports cold food.")
            );

            verify(complaintAnalysisRepository).findByComplaintId(complaintId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when no analysis exists")
        void getAnalysisByComplaintId_ShouldThrowWhenNotFound() {
            UUID complaintId = UUID.randomUUID();

            given(complaintAnalysisRepository.findByComplaintId(complaintId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> analysisService.getAnalysisByComplaintId(complaintId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(complaintAnalysisRepository).findByComplaintId(complaintId);
        }
    }

    // ── getAllAnalyses ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllAnalyses")
    class GetAllAnalyses {

        @Test
        @DisplayName("Should return list of analysis DTOs ordered by analyzedAt desc")
        void getAllAnalyses_ShouldReturnList() {
            ComplaintAnalysis a1 = buildAnalysis(UUID.randomUUID(), UUID.randomUUID());
            ComplaintAnalysis a2 = buildAnalysis(UUID.randomUUID(), UUID.randomUUID());

            given(complaintAnalysisRepository.findAllByOrderByAnalyzedAtDesc())
                    .willReturn(List.of(a1, a2));

            List<AnalysisResponseDto> result = analysisService.getAllAnalyses();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(a1.getId());
            assertThat(result.get(1).getId()).isEqualTo(a2.getId());

            verify(complaintAnalysisRepository).findAllByOrderByAnalyzedAtDesc();
        }

        @Test
        @DisplayName("Should return empty list when no analyses exist")
        void getAllAnalyses_ShouldReturnEmptyList() {
            given(complaintAnalysisRepository.findAllByOrderByAnalyzedAtDesc())
                    .willReturn(Collections.emptyList());

            List<AnalysisResponseDto> result = analysisService.getAllAnalyses();

            assertThat(result).isEmpty();
            verify(complaintAnalysisRepository).findAllByOrderByAnalyzedAtDesc();
        }
    }
}
