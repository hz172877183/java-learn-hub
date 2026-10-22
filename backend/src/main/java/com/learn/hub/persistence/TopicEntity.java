package com.learn.hub.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "topic")
public class TopicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 64)
    private String category;

    @Column(nullable = false, length = 32)
    private String difficulty;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "integration_status", nullable = false, length = 32)
    private String integrationStatus;

    @Column(name = "estimated_hours", nullable = false)
    private int estimatedHours;

    @Column(length = 500)
    private String summary;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TopicEntity() {
    }

    public TopicEntity(
            String slug,
            String title,
            String category,
            String difficulty,
            String status,
            String integrationStatus,
            int estimatedHours,
            String summary,
            String payload,
            LocalDateTime now
    ) {
        this.slug = slug;
        this.title = title;
        this.category = category;
        this.difficulty = difficulty;
        this.status = status;
        this.integrationStatus = integrationStatus;
        this.estimatedHours = estimatedHours;
        this.summary = summary;
        this.payload = payload;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void apply(
            String title,
            String category,
            String difficulty,
            String status,
            String integrationStatus,
            int estimatedHours,
            String summary,
            String payload,
            LocalDateTime now
    ) {
        this.title = title;
        this.category = category;
        this.difficulty = difficulty;
        this.status = status;
        this.integrationStatus = integrationStatus;
        this.estimatedHours = estimatedHours;
        this.summary = summary;
        this.payload = payload;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
