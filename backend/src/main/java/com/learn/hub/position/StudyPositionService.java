package com.learn.hub.position;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 学习位置服务：按用户 + 范围保存浏览上下文，用于“继续上次学习”。
 */
@Service
public class StudyPositionService {

    private static final Pattern SCOPE_PATTERN =
            Pattern.compile("^(interview|topic_quiz:[a-z0-9-]{1,50})$");

    private final StudyPositionRepository repository;
    private final ObjectMapper objectMapper;

    public StudyPositionService(StudyPositionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<PositionDtos.PositionView> get(Long userId, String scope) {
        validateScope(scope);
        return repository.findByUserIdAndScope(userId, scope).map(this::toView);
    }

    /**
     * 保存或覆盖位置；同一用户同一范围只保留最新一条。
     */
    @Transactional
    public PositionDtos.PositionView save(Long userId, String scope, PositionDtos.PositionRequest request) {
        validateScope(scope);
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        PositionDtos.PositionRequest value = normalize(request);
        LocalDateTime now = LocalDateTime.now();
        String payload = toJson(value);

        StudyPositionEntity entity = repository.findByUserIdAndScope(userId, scope)
                .orElse(null);
        if (entity == null) {
            entity = repository.save(new StudyPositionEntity(userId, scope, payload, now));
        } else {
            entity.apply(payload, now);
            entity = repository.save(entity);
        }
        return toView(entity);
    }

    @Transactional
    public void delete(Long userId, String scope) {
        validateScope(scope);
        repository.findByUserIdAndScope(userId, scope).ifPresent(repository::delete);
    }

    private PositionDtos.PositionRequest normalize(PositionDtos.PositionRequest request) {
        int page = request.page() == null ? 1 : Math.max(1, request.page());
        int size = request.size() == null ? 20 : Math.min(100, Math.max(1, request.size()));
        int scrollTop = request.scrollTop() == null ? 0 : Math.max(0, request.scrollTop());
        Long questionId = request.questionId() == null || request.questionId() <= 0
                ? null
                : request.questionId();
        return new PositionDtos.PositionRequest(
                trim(request.level(), 32),
                trim(request.category(), 64),
                trim(request.q(), 200),
                page,
                size,
                questionId,
                scrollTop
        );
    }

    private PositionDtos.PositionView toView(StudyPositionEntity entity) {
        try {
            PositionDtos.PositionRequest payload =
                    objectMapper.readValue(entity.getPayload(), PositionDtos.PositionRequest.class);
            return new PositionDtos.PositionView(
                    entity.getScope(),
                    payload.level(),
                    payload.category(),
                    payload.q(),
                    payload.page(),
                    payload.size(),
                    payload.questionId(),
                    payload.scrollTop(),
                    entity.getUpdatedAt()
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("学习位置 JSON 无法解析", e);
        }
    }

    private String toJson(PositionDtos.PositionRequest value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("学习位置序列化失败", e);
        }
    }

    private void validateScope(String scope) {
        if (scope == null || !SCOPE_PATTERN.matcher(scope).matches()) {
            throw new IllegalArgumentException("scope 只能是 interview 或 topic_quiz:<slug>");
        }
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}
