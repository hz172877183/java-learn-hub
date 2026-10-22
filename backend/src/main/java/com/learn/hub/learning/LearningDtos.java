package com.learn.hub.learning;

import java.time.LocalDateTime;

public final class LearningDtos {

    private LearningDtos() {
    }

    public record ProgressView(
            String topicSlug,
            String title,
            String state,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            LocalDateTime updatedAt
    ) {
    }

    public record ProgressRequest(String state) {
    }

    public record NoteView(
            String topicSlug,
            String title,
            String content,
            LocalDateTime updatedAt
    ) {
    }

    public record NoteSaveRequest(String content) {
    }
}
