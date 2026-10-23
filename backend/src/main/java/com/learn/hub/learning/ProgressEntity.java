package com.learn.hub.learning;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "learning_progress",
        uniqueConstraints = @UniqueConstraint(name = "uk_progress_user_topic", columnNames = {"user_id", "topic_slug"})
)
/**
 * 学习进度实体：三态与开始/完成时间，user_id + topic_slug 唯一。
 */
public class ProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "topic_slug", nullable = false, length = 120)
    private String topicSlug;

    @Column(nullable = false, length = 16)
    private String state;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ProgressEntity() {
    }

    public ProgressEntity(Long userId, String topicSlug, String state, LocalDateTime now) {
        this.userId = userId;
        this.topicSlug = topicSlug;
        this.updatedAt = now;
        applyState(state, now);
    }

    public void applyState(String state, LocalDateTime now) {
        this.state = state;
        this.updatedAt = now;
        // 第一次进入非“未开始”状态时记录开始时间，之后不覆盖
        if (!"not_started".equals(state) && this.startedAt == null) {
            this.startedAt = now;
        }
        // 完成时间只保留第一次完成的时间；退回学习中时清空
        this.completedAt = "completed".equals(state) ? (this.completedAt == null ? now : this.completedAt) : null;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTopicSlug() {
        return topicSlug;
    }

    public String getState() {
        return state;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
