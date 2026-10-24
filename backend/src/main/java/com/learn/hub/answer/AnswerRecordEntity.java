package com.learn.hub.answer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * 答题记录：每个用户对每道题只保留最近一次作答，attempts 记录累计次数。
 */
@Entity
@Table(
        name = "answer_record",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_answer_user_question",
                columnNames = {"user_id", "source", "question_id"}
        )
)
public class AnswerRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 题目来源：topic_quiz（随堂题）或 interview（面试题库）。 */
    @Column(nullable = false, length = 16)
    private String source;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "question_type", nullable = false, length = 16)
    private String questionType;

    @Column(name = "choice_index")
    private Integer choiceIndex;

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    /** 选择题为判定结果；自答题为用户自评，未自评时为空。 */
    @Column
    private Boolean correct;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AnswerRecordEntity() {
    }

    public AnswerRecordEntity(
            Long userId,
            String source,
            Long questionId,
            String questionType,
            Integer choiceIndex,
            String textAnswer,
            Boolean correct,
            LocalDateTime now
    ) {
        this.userId = userId;
        this.source = source;
        this.questionId = questionId;
        this.questionType = questionType;
        this.choiceIndex = choiceIndex;
        this.textAnswer = textAnswer;
        this.correct = correct;
        this.attempts = 1;
        this.answeredAt = now;
        this.updatedAt = now;
    }

    /**
     * 覆盖最近一次作答，并累加尝试次数。
     */
    public void apply(
            String questionType,
            Integer choiceIndex,
            String textAnswer,
            Boolean correct,
            LocalDateTime now
    ) {
        this.questionType = questionType;
        this.choiceIndex = choiceIndex;
        this.textAnswer = textAnswer;
        this.correct = correct;
        this.attempts += 1;
        this.updatedAt = now;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSource() {
        return source;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public Integer getChoiceIndex() {
        return choiceIndex;
    }

    public String getTextAnswer() {
        return textAnswer;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public int getAttempts() {
        return attempts;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }
}
