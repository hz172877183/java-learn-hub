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
        name = "topic_note",
        uniqueConstraints = @UniqueConstraint(name = "uk_note_user_topic", columnNames = {"user_id", "topic_slug"})
)
/**
 * 学习笔记实体：user_id + topic_slug 唯一，保证一人一项一条。
 */
public class NoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "topic_slug", nullable = false, length = 120)
    private String topicSlug;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected NoteEntity() {
    }

    public NoteEntity(Long userId, String topicSlug, String content, LocalDateTime now) {
        this.userId = userId;
        this.topicSlug = topicSlug;
        this.content = content;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String content, LocalDateTime now) {
        this.content = content;
        this.updatedAt = now;
    }

    public String getTopicSlug() {
        return topicSlug;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
