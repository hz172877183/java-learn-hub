package com.learn.hub.quiz;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 面试题实体：按学习项分组，题目内容保存在 payload JSON 中。
 */
@Entity
@Table(name = "quiz_question")
public class QuizQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_slug", nullable = false, length = 120)
    private String topicSlug;

    @Column(nullable = false, length = 16)
    private String type;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected QuizQuestionEntity() {
    }

    public QuizQuestionEntity(
            String topicSlug,
            String type,
            String payload,
            int sortOrder,
            LocalDateTime now
    ) {
        this.topicSlug = topicSlug;
        this.type = type;
        this.payload = payload;
        this.sortOrder = sortOrder;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 更新题目内容与排序号。
     */
    public void update(String type, String payload, int sortOrder, LocalDateTime now) {
        this.type = type;
        this.payload = payload;
        this.sortOrder = sortOrder;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getTopicSlug() {
        return topicSlug;
    }

    public String getPayload() {
        return payload;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
