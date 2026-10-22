package com.learn.hub.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "track")
public class TrackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String audience;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TrackEntity() {
    }

    public TrackEntity(String slug, String title, String audience, String payload, LocalDateTime now) {
        this.slug = slug;
        this.title = title;
        this.audience = audience;
        this.payload = payload;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getSlug() {
        return slug;
    }

    public String getPayload() {
        return payload;
    }
}
