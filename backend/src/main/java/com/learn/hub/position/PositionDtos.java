package com.learn.hub.position;

import java.time.LocalDateTime;

public final class PositionDtos {

    private PositionDtos() {
    }

    /**
     * 前端上报的浏览上下文；page 为 1 起。
     */
    public record PositionRequest(
            String level,
            String category,
            String q,
            Integer page,
            Integer size,
            Long questionId,
            Integer scrollTop
    ) {
    }

    public record PositionView(
            String scope,
            String level,
            String category,
            String q,
            int page,
            int size,
            Long questionId,
            int scrollTop,
            LocalDateTime updatedAt
    ) {
    }
}
