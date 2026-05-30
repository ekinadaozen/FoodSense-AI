package com.foodsense.ai.mapper;

import com.foodsense.ai.dto.ComplaintRequestDto;
import com.foodsense.ai.dto.ComplaintResponseDto;
import com.foodsense.ai.entity.Complaint;

import java.util.List;

/**
 * Utility mapper class for converting between {@link Complaint} entities
 * and their corresponding DTOs.
 *
 * <p>This is a stateless utility class with only static methods.
 * It cannot be instantiated.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
public final class ComplaintMapper {

    private ComplaintMapper() {
        // Prevent instantiation
    }

    /**
     * Converts a {@link ComplaintRequestDto} to a {@link Complaint} entity.
     *
     * <p>The returned entity will not have an {@code id} or {@code createdAt}
     * set — these are assigned during persistence.</p>
     *
     * @param dto the complaint request DTO
     * @return a new {@link Complaint} entity populated from the DTO
     */
    public static Complaint toEntity(final ComplaintRequestDto dto) {
        return Complaint.builder()
                .customerName(dto.getCustomerName())
                .complaintText(dto.getComplaintText())
                .build();
    }

    /**
     * Converts a {@link Complaint} entity to a {@link ComplaintResponseDto}.
     *
     * @param entity the complaint entity
     * @return a new {@link ComplaintResponseDto} populated from the entity
     */
    public static ComplaintResponseDto toResponseDto(final Complaint entity) {
        return ComplaintResponseDto.builder()
                .id(entity.getId())
                .customerName(entity.getCustomerName())
                .complaintText(entity.getComplaintText())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Converts a list of {@link Complaint} entities to a list of
     * {@link ComplaintResponseDto} instances.
     *
     * @param entities the list of complaint entities
     * @return a list of response DTOs
     */
    public static List<ComplaintResponseDto> toResponseDtoList(final List<Complaint> entities) {
        return entities.stream()
                .map(ComplaintMapper::toResponseDto)
                .toList();
    }
}
