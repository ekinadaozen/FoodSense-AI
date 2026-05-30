package com.foodsense.ai.service;

import com.foodsense.ai.dto.AnalysisResponseDto;
import com.foodsense.ai.entity.ComplaintAnalysis;
import com.foodsense.ai.exception.ResourceNotFoundException;
import com.foodsense.ai.mapper.AnalysisMapper;
import com.foodsense.ai.repository.ComplaintAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service layer for managing complaint analysis records.
 *
 * <p>Provides methods for saving new analysis results and retrieving
 * them by complaint ID or as a complete listing.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final ComplaintAnalysisRepository complaintAnalysisRepository;

    /**
     * Persists a complaint analysis entity to the database.
     *
     * @param analysis the analysis entity to save
     * @return the persisted analysis entity with generated ID and timestamp
     */
    public ComplaintAnalysis saveAnalysis(final ComplaintAnalysis analysis) {
        log.info("Saving analysis for complaint ID [{}]", analysis.getComplaintId());
        return complaintAnalysisRepository.save(analysis);
    }

    /**
     * Retrieves the analysis for a specific complaint.
     *
     * @param complaintId the UUID of the complaint
     * @return the analysis response DTO
     * @throws ResourceNotFoundException if no analysis exists for the given complaint ID
     */
    public AnalysisResponseDto getAnalysisByComplaintId(final UUID complaintId) {
        log.debug("Fetching analysis for complaint ID [{}]", complaintId);

        final ComplaintAnalysis analysis = complaintAnalysisRepository.findByComplaintId(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplaintAnalysis", "complaintId", complaintId));

        return AnalysisMapper.toResponseDto(analysis);
    }

    /**
     * Retrieves all analyses ordered by analysis date in descending order.
     *
     * @return a list of analysis response DTOs
     */
    public List<AnalysisResponseDto> getAllAnalyses() {
        log.debug("Fetching all analyses ordered by analyzedAt desc");

        final List<ComplaintAnalysis> analyses = complaintAnalysisRepository.findAllByOrderByAnalyzedAtDesc();
        return AnalysisMapper.toResponseDtoList(analyses);
    }
}
