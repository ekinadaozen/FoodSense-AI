package com.foodsense.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a customer complaint submitted to the FoodSense AI system.
 *
 * <p>Each complaint captures the customer's name and free-text description of the issue.
 * A {@code createdAt} timestamp is automatically assigned upon persistence via
 * {@link #onCreate()}.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "complaints")
public class Complaint {

    /**
     * Unique identifier for the complaint, generated automatically as a UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Name of the customer who submitted the complaint.
     */
    @Column(nullable = false)
    private String customerName;

    /**
     * Full text of the customer complaint.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String complaintText;

    /**
     * Timestamp indicating when the complaint was created.
     * Automatically set before initial persistence.
     */
    private LocalDateTime createdAt;

    /**
     * JPA lifecycle callback that sets the {@code createdAt} timestamp
     * before the entity is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
