package com.learn.hub.interview;

import java.util.List;

public final class InterviewDtos {

    private InterviewDtos() {
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
            String explanation
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
}
