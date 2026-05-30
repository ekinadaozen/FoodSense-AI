package com.foodsense.ai.repository;

import com.foodsense.ai.entity.ComplaintAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ComplaintAnalysis} entities.
 *
 * <p>Provides standard CRUD operations along with custom query methods
 * for retrieving analyses by complaint ID and ordering by analysis date.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Repository
public interface ComplaintAnalysisRepository extends JpaRepository<ComplaintAnalysis, UUID> {

    /**
     * Finds a complaint analysis by the associated complaint ID.
     *
     * @param complaintId the UUID of the complaint
     * @return an {@link Optional} containing the analysis if found, or empty otherwise
     */
    Optional<ComplaintAnalysis> findByComplaintId(UUID complaintId);

    /**
     * Retrieves all complaint analyses ordered by {@code analyzedAt} in descending order
     * (most recent first).
     *
     * @return a list of analyses sorted by analysis date descending
     */
    List<ComplaintAnalysis> findAllByOrderByAnalyzedAtDesc();
}
