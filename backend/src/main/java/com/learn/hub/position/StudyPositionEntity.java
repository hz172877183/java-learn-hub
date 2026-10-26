package com.learn.hub.position;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * 学习位置：每个用户每个范围（面试专区 / 某个学习项的随堂题）只保留一条。
 */
@Entity
@Table(
        name = "study_position",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_position_user_scope",
                columnNames = {"user_id", "scope"}
        )
)
public class StudyPositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 64)
    private String scope;

    /** 浏览上下文 JSON：筛选、页码、题号、滚动位置。 */
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected StudyPositionEntity() {
    }

    public StudyPositionEntity(Long userId, String scope, String payload, LocalDateTime now) {
        this.userId = userId;
        this.scope = scope;
        this.payload = payload;
        this.updatedAt = now;
    }

    public void apply(String payload, LocalDateTime now) {
        this.payload = payload;
        this.updatedAt = now;
    }

    public Long getUserId() {
        return userId;
    }

    public String getScope() {
        return scope;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
