package com.learn.hub.interview;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 面试题实体：独立题库，按分类与等级组织，不绑定学习项。
 */
@Entity
@Table(name = "interview_question")
public class InterviewQuestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(nullable = false, length = 16)
    private String level;

    @Column(nullable = false, length = 16)
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String stem;

    @Column(columnDefinition = "LONGTEXT")
    private String options;

    @Column(name = "correct_index")
    private Integer correctIndex;

    @Column(name = "reference_answer", columnDefinition = "TEXT")
    private String referenceAnswer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    /** 种子题稳定标识；管理端手工创建的题为 null。 */
    @Column(name = "seed_key", length = 64)
    private String seedKey;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected InterviewQuestionEntity() {
    }

    public InterviewQuestionEntity(
            String category,
            String level,
            String type,
            String stem,
            String options,
            Integer correctIndex,
            String referenceAnswer,
            String explanation,
            int sortOrder,
            LocalDateTime now
    ) {
        this(category, level, type, stem, options, correctIndex, referenceAnswer, explanation, null, sortOrder, now);
    }

    public InterviewQuestionEntity(
            String category,
            String level,
            String type,
            String stem,
            String options,
            Integer correctIndex,
            String referenceAnswer,
            String explanation,
            String seedKey,
            int sortOrder,
            LocalDateTime now
    ) {
        this.category = category;
        this.level = level;
        this.type = type;
        this.stem = stem;
        this.options = options;
        this.correctIndex = correctIndex;
        this.referenceAnswer = referenceAnswer;
        this.explanation = explanation;
        this.seedKey = seedKey;
        this.sortOrder = sortOrder;
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 覆盖可编辑字段；分类与等级也允许调整。
     */
    public void apply(
            String category,
            String level,
            String type,
            String stem,
            String options,
            Integer correctIndex,
            String referenceAnswer,
            String explanation,
            LocalDateTime now
    ) {
        this.category = category;
        this.level = level;
        this.type = type;
        this.stem = stem;
        this.options = options;
        this.correctIndex = correctIndex;
        this.referenceAnswer = referenceAnswer;
        this.explanation = explanation;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getLevel() {
        return level;
    }

    public String getType() {
        return type;
    }

    public String getStem() {
        return stem;
    }

    public String getOptions() {
        return options;
    }

    public Integer getCorrectIndex() {
        return correctIndex;
    }

    public String getReferenceAnswer() {
        return referenceAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getSeedKey() {
        return seedKey;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
