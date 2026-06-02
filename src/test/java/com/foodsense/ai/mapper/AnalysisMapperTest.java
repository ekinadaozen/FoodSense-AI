package com.foodsense.ai.mapper;

import com.foodsense.ai.dto.AnalysisResponseDto;
import com.foodsense.ai.entity.ComplaintAnalysis;
import com.foodsense.ai.enums.Category;
import com.foodsense.ai.enums.Priority;
import com.foodsense.ai.enums.Sentiment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("AnalysisMapper Unit Tests")
class AnalysisMapperTest {

    // ── toResponseDto ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("toResponseDto")
    class ToResponseDto {

        @Test
        @DisplayName("Should map all entity fields including enums to response DTO")
        void toResponseDto_ShouldMapAllFields() {
            UUID id = UUID.randomUUID();
            UUID complaintId = UUID.randomUUID();
            LocalDateTime analyzedAt = LocalDateTime.of(2026, 6, 1, 14, 30, 0);

            ComplaintAnalysis entity = ComplaintAnalysis.builder()
                    .id(id)
                    .complaintId(complaintId)
                    .category(Category.FOOD_QUALITY)
                    .sentiment(Sentiment.NEGATIVE)
                    .priority(Priority.HIGH)
                    .summary("Customer reports cold food.")
                    .analyzedAt(analyzedAt)
                    .build();

            AnalysisResponseDto dto = AnalysisMapper.toResponseDto(entity);

            assertAll("response DTO fields match entity",
                    () -> assertThat(dto.getId()).isEqualTo(id),
                    () -> assertThat(dto.getComplaintId()).isEqualTo(complaintId),
                    () -> assertThat(dto.getCategory()).isEqualTo("FOOD_QUALITY"),
                    () -> assertThat(dto.getSentiment()).isEqualTo("NEGATIVE"),
                    () -> assertThat(dto.getPriority()).isEqualTo("HIGH"),
                    () -> assertThat(dto.getSummary()).isEqualTo("Customer reports cold food."),
                    () -> assertThat(dto.getAnalyzedAt()).isEqualTo(analyzedAt)
            );
        }

        @Test
        @DisplayName("Should handle null enum fields gracefully")
        void toResponseDto_ShouldHandleNullEnums() {
            ComplaintAnalysis entity = ComplaintAnalysis.builder()
                    .id(UUID.randomUUID())
                    .complaintId(UUID.randomUUID())
                    .category(null)
                    .sentiment(null)
                    .priority(null)
                    .summary("Some summary")
                    .analyzedAt(null)
                    .build();

            AnalysisResponseDto dto = AnalysisMapper.toResponseDto(entity);

            assertAll("null-safe enum mapping",
                    () -> assertThat(dto.getCategory()).isNull(),
                    () -> assertThat(dto.getSentiment()).isNull(),
                    () -> assertThat(dto.getPriority()).isNull(),
                    () -> assertThat(dto.getSummary()).isEqualTo("Some summary")
            );
        }

        @Test
        @DisplayName("Should map different enum combinations correctly")
        void toResponseDto_ShouldMapVariousEnumCombinations() {
            ComplaintAnalysis entity = ComplaintAnalysis.builder()
                    .id(UUID.randomUUID())
                    .complaintId(UUID.randomUUID())
                    .category(Category.DELIVERY)
                    .sentiment(Sentiment.NEUTRAL)
                    .priority(Priority.MEDIUM)
                    .summary("Delivery was slightly delayed.")
                    .analyzedAt(LocalDateTime.now())
                    .build();

            AnalysisResponseDto dto = AnalysisMapper.toResponseDto(entity);

            assertAll("different enum values",
                    () -> assertThat(dto.getCategory()).isEqualTo("DELIVERY"),
                    () -> assertThat(dto.getSentiment()).isEqualTo("NEUTRAL"),
                    () -> assertThat(dto.getPriority()).isEqualTo("MEDIUM")
            );
        }
    }

    // ── toResponseDtoList ───────────────────────────────────────────────────

    @Nested
    @DisplayName("toResponseDtoList")
    class ToResponseDtoList {

        @Test
        @DisplayName("Should map all entities in the list to response DTOs")
        void toResponseDtoList_ShouldMapAllElements() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            List<ComplaintAnalysis> entities = List.of(
                    ComplaintAnalysis.builder()
                            .id(id1).complaintId(UUID.randomUUID())
                            .category(Category.FOOD_QUALITY).sentiment(Sentiment.NEGATIVE)
                            .priority(Priority.HIGH).summary("Issue 1").analyzedAt(now)
                            .build(),
                    ComplaintAnalysis.builder()
                            .id(id2).complaintId(UUID.randomUUID())
                            .category(Category.DELIVERY).sentiment(Sentiment.NEUTRAL)
                            .priority(Priority.LOW).summary("Issue 2").analyzedAt(now)
                            .build()
            );

            List<AnalysisResponseDto> dtos = AnalysisMapper.toResponseDtoList(entities);

            assertThat(dtos).hasSize(2);
            assertThat(dtos)
                    .extracting(AnalysisResponseDto::getId)
                    .containsExactly(id1, id2);
            assertThat(dtos)
                    .extracting(AnalysisResponseDto::getCategory)
                    .containsExactly("FOOD_QUALITY", "DELIVERY");
        }

        @Test
        @DisplayName("Should return empty list when given empty input")
        void toResponseDtoList_ShouldReturnEmptyForEmptyInput() {
            List<AnalysisResponseDto> dtos = AnalysisMapper.toResponseDtoList(Collections.emptyList());

            assertThat(dtos).isEmpty();
        }

        @Test
        @DisplayName("Should return single-element list when given one entity")
        void toResponseDtoList_ShouldHandleSingleElement() {
            ComplaintAnalysis single = ComplaintAnalysis.builder()
                    .id(UUID.randomUUID())
                    .complaintId(UUID.randomUUID())
                    .category(Category.CUSTOMER_SERVICE)
                    .sentiment(Sentiment.POSITIVE)
                    .priority(Priority.LOW)
                    .summary("Great service")
                    .analyzedAt(LocalDateTime.now())
                    .build();

            List<AnalysisResponseDto> dtos = AnalysisMapper.toResponseDtoList(List.of(single));

            assertThat(dtos).hasSize(1);
            assertThat(dtos.get(0).getCategory()).isEqualTo("CUSTOMER_SERVICE");
            assertThat(dtos.get(0).getSentiment()).isEqualTo("POSITIVE");
        }
    }
}
