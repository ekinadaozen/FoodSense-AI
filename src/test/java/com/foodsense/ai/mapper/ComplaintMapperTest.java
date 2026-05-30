package com.foodsense.ai.mapper;

import com.foodsense.ai.dto.ComplaintRequestDto;
import com.foodsense.ai.dto.ComplaintResponseDto;
import com.foodsense.ai.entity.Complaint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("ComplaintMapper Unit Tests")
class ComplaintMapperTest {

    // ── toEntity ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("Should map DTO fields to entity correctly")
        void toEntity_ShouldMapFieldsCorrectly() {
            // Arrange
            ComplaintRequestDto dto = ComplaintRequestDto.builder()
                    .customerName("Alice Johnson")
                    .complaintText("My order arrived cold and was missing items.")
                    .build();

            // Act
            Complaint entity = ComplaintMapper.toEntity(dto);

            // Assert
            assertAll("mapped entity fields",
                    () -> assertThat(entity.getCustomerName()).isEqualTo("Alice Johnson"),
                    () -> assertThat(entity.getComplaintText())
                            .isEqualTo("My order arrived cold and was missing items."),
                    // id and createdAt should NOT be set by the mapper – they are DB-managed
                    () -> assertThat(entity.getId()).isNull(),
                    () -> assertThat(entity.getCreatedAt()).isNull()
            );
        }

        @Test
        @DisplayName("Should handle null field values gracefully")
        void toEntity_ShouldHandleNullValues() {
            ComplaintRequestDto dto = ComplaintRequestDto.builder()
                    .customerName(null)
                    .complaintText(null)
                    .build();

            Complaint entity = ComplaintMapper.toEntity(dto);

            assertAll("null-safe mapping",
                    () -> assertThat(entity.getCustomerName()).isNull(),
                    () -> assertThat(entity.getComplaintText()).isNull()
            );
        }

        @Test
        @DisplayName("Should handle edge-case string values (empty, whitespace)")
        void toEntity_ShouldMapEdgeCaseStrings() {
            ComplaintRequestDto dto = ComplaintRequestDto.builder()
                    .customerName("")
                    .complaintText("   ")
                    .build();

            Complaint entity = ComplaintMapper.toEntity(dto);

            assertAll("edge-case strings preserved",
                    () -> assertThat(entity.getCustomerName()).isEmpty(),
                    () -> assertThat(entity.getComplaintText()).isEqualTo("   ")
            );
        }
    }

    // ── toResponseDto ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("toResponseDto")
    class ToResponseDto {

        @Test
        @DisplayName("Should map all entity fields to response DTO correctly")
        void toResponseDto_ShouldMapFieldsCorrectly() {
            UUID id = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.of(2026, 5, 30, 14, 30, 0);

            Complaint entity = Complaint.builder()
                    .id(id)
                    .customerName("Bob Smith")
                    .complaintText("Payment was charged twice for one order.")
                    .createdAt(now)
                    .build();

            ComplaintResponseDto dto = ComplaintMapper.toResponseDto(entity);

            assertAll("response DTO fields match entity",
                    () -> assertThat(dto.getId()).isEqualTo(id),
                    () -> assertThat(dto.getCustomerName()).isEqualTo("Bob Smith"),
                    () -> assertThat(dto.getComplaintText())
                            .isEqualTo("Payment was charged twice for one order."),
                    () -> assertThat(dto.getCreatedAt()).isEqualTo(now)
            );
        }

        @Test
        @DisplayName("Should map entity with null id and createdAt")
        void toResponseDto_ShouldMapEntityWithNulls() {
            Complaint entity = Complaint.builder()
                    .id(null)
                    .customerName("Carol")
                    .complaintText("Some issue here with the order.")
                    .createdAt(null)
                    .build();

            ComplaintResponseDto dto = ComplaintMapper.toResponseDto(entity);

            assertAll("nullable fields",
                    () -> assertThat(dto.getId()).isNull(),
                    () -> assertThat(dto.getCreatedAt()).isNull(),
                    () -> assertThat(dto.getCustomerName()).isEqualTo("Carol")
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
            UUID id3 = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            List<Complaint> entities = List.of(
                    Complaint.builder().id(id1).customerName("Dave").complaintText("Issue 1").createdAt(now).build(),
                    Complaint.builder().id(id2).customerName("Eve").complaintText("Issue 2").createdAt(now).build(),
                    Complaint.builder().id(id3).customerName("Frank").complaintText("Issue 3").createdAt(now).build()
            );

            List<ComplaintResponseDto> dtos = ComplaintMapper.toResponseDtoList(entities);

            assertThat(dtos).hasSize(3);
            assertThat(dtos)
                    .extracting(ComplaintResponseDto::getCustomerName)
                    .containsExactly("Dave", "Eve", "Frank");
            assertThat(dtos)
                    .extracting(ComplaintResponseDto::getId)
                    .containsExactly(id1, id2, id3);
        }

        @Test
        @DisplayName("Should return empty list when given empty input")
        void toResponseDtoList_ShouldReturnEmptyForEmptyInput() {
            List<ComplaintResponseDto> dtos = ComplaintMapper.toResponseDtoList(Collections.emptyList());

            assertThat(dtos).isEmpty();
        }

        @Test
        @DisplayName("Should return single-element list when given one entity")
        void toResponseDtoList_ShouldHandleSingleElement() {
            Complaint single = Complaint.builder()
                    .id(UUID.randomUUID())
                    .customerName("Grace")
                    .complaintText("Only one complaint")
                    .createdAt(LocalDateTime.now())
                    .build();

            List<ComplaintResponseDto> dtos = ComplaintMapper.toResponseDtoList(List.of(single));

            assertThat(dtos).hasSize(1);
            assertThat(dtos.get(0).getCustomerName()).isEqualTo("Grace");
        }
    }
}
