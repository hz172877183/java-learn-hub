package com.learn.hub.interview;

import java.util.List;

public final class InterviewDtos {

    private InterviewDtos() {
    }

    /**
     * 当前用户对这道题的最近一次作答；公开接口为 null。
     */
    public record MyAnswer(
            Integer choiceIndex,
            String textAnswer,
            Boolean correct,
            int attempts
    ) {
    }

    public record QuestionView(
            Long id,
            String category,
            String level,
            String type,
            String stem,
            List<String> options,
            Integer correctIndex,
            String referenceAnswer,
            String explanation,
            MyAnswer myAnswer
    ) {
    }

    public record QuestionRequest(
            String category,
            String level,
            String type,
            String stem,
            List<String> options,
            Integer correctIndex,
            String referenceAnswer,
            String explanation
    ) {
    }

    public record QuestionList(
            List<QuestionView> items,
            int page,
            int size,
            long total
    ) {
    }

    public record Stat(String id, long count) {
    }

    public record Meta(List<Stat> levels, List<Stat> categories, long total) {
    }

    public record ResetResult(long deleted) {
    }
}
