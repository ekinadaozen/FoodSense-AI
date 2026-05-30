package com.foodsense.ai.service;

import com.foodsense.ai.dto.ComplaintRequestDto;
import com.foodsense.ai.dto.ComplaintResponseDto;
import com.foodsense.ai.entity.Complaint;
import com.foodsense.ai.exception.ResourceNotFoundException;
import com.foodsense.ai.kafka.ComplaintProducer;
import com.foodsense.ai.mapper.ComplaintMapper;
import com.foodsense.ai.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for managing customer complaints.
 *
 * <p>Handles complaint submission (including Kafka event publishing),
 * retrieval by ID, and listing of all complaints.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintProducer complaintProducer;

    /**
     * Submits a new customer complaint.
     *
     * <p>The complaint is persisted to the database and its ID is published
     * to the Kafka complaint topic for asynchronous AI analysis.</p>
     *
     * @param dto the complaint request DTO containing customer name and complaint text
     * @return a response DTO representing the persisted complaint
     */
    public ComplaintResponseDto submitComplaint(final ComplaintRequestDto dto) {
        log.info("Submitting complaint for customer [{}]", dto.getCustomerName());

        final Complaint entity = ComplaintMapper.toEntity(dto);
        final Complaint savedComplaint = complaintRepository.save(entity);

        log.info("Complaint saved with ID [{}]. Publishing to Kafka.", savedComplaint.getId());
        complaintProducer.sendComplaint(savedComplaint.getId().toString());

        return ComplaintMapper.toResponseDto(savedComplaint);
    }

    /**
     * Retrieves a complaint by its unique identifier.
     *
     * @param id the UUID of the complaint
     * @return a response DTO representing the complaint
     * @throws ResourceNotFoundException if no complaint exists with the given ID
     */
    public ComplaintResponseDto getComplaintById(final UUID id) {
        log.debug("Fetching complaint with ID [{}]", id);

        final Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

        return ComplaintMapper.toResponseDto(complaint);
    }

    /**
     * Retrieves all complaints ordered by creation date in descending order.
     *
     * @return a list of complaint response DTOs
     */
    public List<ComplaintResponseDto> getAllComplaints() {
        log.debug("Fetching all complaints ordered by createdAt desc");

        final List<Complaint> complaints = complaintRepository.findAllByOrderByCreatedAtDesc();
        return ComplaintMapper.toResponseDtoList(complaints);
    }
}
