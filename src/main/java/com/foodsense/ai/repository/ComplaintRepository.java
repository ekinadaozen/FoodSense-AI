package com.foodsense.ai.repository;

import com.foodsense.ai.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link Complaint} entities.
 *
 * <p>Provides standard CRUD operations and a custom query method to retrieve
 * complaints ordered by creation date in descending order.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {

    /**
     * Retrieves all complaints ordered by {@code createdAt} in descending order
     * (most recent first).
     *
     * @return a list of complaints sorted by creation date descending
     */
    List<Complaint> findAllByOrderByCreatedAtDesc();
}
