package com.learn.hub.answer;

import java.time.LocalDateTime;
import java.util.List;

public final class AnswerDtos {

    private AnswerDtos() {
    }

    public record SubmitRequest(
            String source,
            Long questionId,
            Integer choiceIndex,
            String textAnswer,
            Boolean selfCorrect
    ) {
    }

    /**
     * 答题记录视图：已把题目内容补齐，前端无需再查题库。
     */
    public record AnswerView(
            String source,
            Long questionId,
            String questionType,
            String stem,
            String level,
            String category,
            String topicSlug,
            String topicTitle,
            List<String> options,
            Integer correctIndex,
            Integer choiceIndex,
            String textAnswer,
            Boolean correct,
            String explanation,
            String referenceAnswer,
            int attempts,
            LocalDateTime answeredAt
    ) {
    }

    public record AnswerList(
            List<AnswerView> items,
            int page,
            int size,
            long total
    ) {
    }

    public record Stats(
            long total,
            long correct,
            long wrong,
            long ungraded
    ) {
    }
}
