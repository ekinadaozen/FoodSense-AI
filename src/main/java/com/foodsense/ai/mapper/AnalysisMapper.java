package com.foodsense.ai.mapper;

import com.foodsense.ai.dto.AnalysisResponseDto;
import com.foodsense.ai.entity.ComplaintAnalysis;

import java.util.List;

/**
 * Utility mapper class for converting between {@link ComplaintAnalysis} entities
 * and their corresponding DTOs.
 *
 * <p>Enum fields ({@code category}, {@code sentiment}, {@code priority}) are mapped
 * to their {@link Enum#name()} string representation for API consumers.</p>
 *
 * <p>This is a stateless utility class with only static methods.
 * It cannot be instantiated.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
public final class AnalysisMapper {

    private AnalysisMapper() {
        // Prevent instantiation
    }

    /**
     * Converts a {@link ComplaintAnalysis} entity to an {@link AnalysisResponseDto}.
     *
     * <p>Enum values are mapped to their string names. Null-safe handling is
     * applied for all enum fields.</p>
     *
     * @param entity the complaint analysis entity
     * @return a new {@link AnalysisResponseDto} populated from the entity
     */
    public static AnalysisResponseDto toResponseDto(final ComplaintAnalysis entity) {
        return AnalysisResponseDto.builder()
                .id(entity.getId())
                .complaintId(entity.getComplaintId())
                .category(entity.getCategory() != null ? entity.getCategory().name() : null)
                .sentiment(entity.getSentiment() != null ? entity.getSentiment().name() : null)
                .priority(entity.getPriority() != null ? entity.getPriority().name() : null)
                .summary(entity.getSummary())
                .analyzedAt(entity.getAnalyzedAt())
                .build();
    }

    /**
     * Converts a list of {@link ComplaintAnalysis} entities to a list of
     * {@link AnalysisResponseDto} instances.
     *
     * @param entities the list of complaint analysis entities
     * @return a list of response DTOs
     */
    public static List<AnalysisResponseDto> toResponseDtoList(final List<ComplaintAnalysis> entities) {
        return entities.stream()
                .map(AnalysisMapper::toResponseDto)
                .toList();
    }
}
