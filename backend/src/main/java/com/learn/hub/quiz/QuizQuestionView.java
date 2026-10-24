package com.learn.hub.quiz;

import com.learn.hub.content.ContentModels;

import java.util.List;

/**
 * 随堂题视图：在题目内容外附带数据库 id，供答题记录使用。
 */
public record QuizQuestionView(
        Long id,
        String type,
        String stem,
        List<String> options,
        Integer correctIndex,
        String referenceAnswer,
        String explanation
) {
    public static QuizQuestionView of(QuizQuestionEntity entity, ContentModels.QuizQuestion question) {
        return new QuizQuestionView(
                entity.getId(),
                question.type(),
                question.stem(),
                question.options(),
                question.correctIndex(),
                question.referenceAnswer(),
                question.explanation()
        );
    }
}
