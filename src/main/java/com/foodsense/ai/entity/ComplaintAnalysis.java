package com.foodsense.ai.entity;

import com.foodsense.ai.enums.Category;
import com.foodsense.ai.enums.Priority;
import com.foodsense.ai.enums.Sentiment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * JPA entity representing the AI-generated analysis of a customer complaint.
 *
 * <p>Each analysis is linked to a {@link Complaint} by its {@code complaintId}
 * and contains the categorization, sentiment, priority, and summary produced
 * by the Gemini AI engine.</p>
 *
 * @author FoodSense AI Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "complaint_analyses")
public class ComplaintAnalysis {

    /**
     * Unique identifier for the analysis record, generated automatically as a UUID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The UUID of the associated {@link Complaint} that was analyzed.
     */
    @Column(nullable = false)
    private UUID complaintId;

    /**
     * The category assigned to the complaint by the AI analysis.
     */
    @Enumerated(EnumType.STRING)
    private Category category;

    /**
     * The sentiment detected in the complaint text.
     */
    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;

    /**
     * The priority level assigned based on the complaint severity.
     */
    @Enumerated(EnumType.STRING)
    private Priority priority;

    /**
     * AI-generated summary of the complaint.
     */
    @Column(columnDefinition = "TEXT")
    private String summary;

    /**
     * Timestamp indicating when the analysis was performed.
     * Automatically set before initial persistence.
     */
    private LocalDateTime analyzedAt;

    /**
     * JPA lifecycle callback that sets the {@code analyzedAt} timestamp
     * before the entity is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        this.analyzedAt = LocalDateTime.now();
    }
}
