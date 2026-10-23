package com.learn.hub.interview;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.hub.topic.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 面试题服务：按分类与等级筛选、管理端增删改，题目不依赖学习项。
 */
@Service
public class InterviewService {

    /** 固定等级及其展示顺序。 */
    private static final List<String> LEVELS = List.of("junior", "mid", "senior", "architect");
    private static final List<String> TYPES = List.of("choice", "text");

    private final InterviewQuestionRepository repository;
    private final ObjectMapper objectMapper;

    public InterviewService(InterviewQuestionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * 条件筛选 + 分页；排序规则为等级、sort_order、id。
     */
    @Transactional(readOnly = true)
    public InterviewDtos.QuestionList list(String category, String level, String q, int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page 从 0 起，size 范围 1-100");
        }
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        List<InterviewQuestionEntity> filtered = repository.findAll().stream()
                .filter(item -> isBlank(category) || category.equals(item.getCategory()))
                .filter(item -> isBlank(level) || level.equals(item.getLevel()))
                .filter(item -> needle.isEmpty() || matches(item, needle))
                .sorted(Comparator
                        .comparingInt((InterviewQuestionEntity item) -> levelRank(item.getLevel()))
                        .thenComparingInt(InterviewQuestionEntity::getSortOrder)
                        .thenComparingLong(InterviewQuestionEntity::getId))
                .toList();

        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<InterviewDtos.QuestionView> items = filtered.subList(from, to).stream()
                .map(this::toView)
                .toList();
        return new InterviewDtos.QuestionList(items, page, size, filtered.size());
    }

    /**
     * 分类/等级的题目数量，供筛选器展示；等级始终返回四项（含 0）。
     */
    @Transactional(readOnly = true)
    public InterviewDtos.Meta meta() {
        List<InterviewQuestionEntity> all = repository.findAll();

        Map<String, Long> levelCounts = all.stream()
                .collect(Collectors.groupingBy(InterviewQuestionEntity::getLevel, Collectors.counting()));
        List<InterviewDtos.Stat> levels = LEVELS.stream()
                .map(level -> new InterviewDtos.Stat(level, levelCounts.getOrDefault(level, 0L)))
                .toList();

        Map<String, Long> categoryCounts = new TreeMap<>(all.stream()
                .collect(Collectors.groupingBy(InterviewQuestionEntity::getCategory, Collectors.counting())));
        List<InterviewDtos.Stat> categories = categoryCounts.entrySet().stream()
                .map(entry -> new InterviewDtos.Stat(entry.getKey(), entry.getValue()))
                .toList();

        return new InterviewDtos.Meta(levels, categories, all.size());
    }

    @Transactional(readOnly = true)
    public InterviewDtos.QuestionView get(Long id) {
        return toView(entity(id));
    }

    /**
     * 新建题目，排序号追加到末尾。
     */
    @Transactional
    public InterviewDtos.QuestionView create(InterviewDtos.QuestionRequest request) {
        InterviewDtos.QuestionRequest value = validate(request);
        LocalDateTime now = LocalDateTime.now();
        InterviewQuestionEntity entity = repository.save(new InterviewQuestionEntity(
                value.category(),
                value.level(),
                value.type(),
                value.stem(),
                toOptionsJson(value.options()),
                value.correctIndex(),
                value.referenceAnswer(),
                value.explanation(),
                (int) repository.count(),
                now
        ));
        return toView(entity);
    }

    /**
     * 更新题目；分类与等级允许调整，排序号保持不变。
     */
    @Transactional
    public InterviewDtos.QuestionView update(Long id, InterviewDtos.QuestionRequest request) {
        InterviewQuestionEntity entity = entity(id);
        InterviewDtos.QuestionRequest value = validate(request);
        entity.apply(
                value.category(),
                value.level(),
                value.type(),
                value.stem(),
                toOptionsJson(value.options()),
                value.correctIndex(),
                value.referenceAnswer(),
                value.explanation(),
                LocalDateTime.now()
        );
        return toView(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(entity(id));
    }

    /**
     * 按内容版本同步种子题目：先删除旧种子题，再整批写入新种子题。
     * 管理端手工创建（seedKey 为空）且题干不在种子中的题不会被删除。
     */
    @Transactional
    public void syncSeeds(List<InterviewDtos.QuestionRequest> questions) {
        List<InterviewDtos.QuestionRequest> values = questions.stream()
                .map(this::validate)
                .toList();

        Set<String> legacyKeys = new HashSet<>();
        for (InterviewDtos.QuestionRequest value : values) {
            legacyKeys.add(seedKey(value.category(), value.stem()));
        }

        List<InterviewQuestionEntity> seeded = repository.findAll().stream()
                .filter(item -> item.getSeedKey() != null
                        || legacyKeys.contains(seedKey(item.getCategory(), item.getStem())))
                .toList();
        if (!seeded.isEmpty()) {
            repository.deleteAllInBatch(seeded);
        }

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < values.size(); i++) {
            InterviewDtos.QuestionRequest value = values.get(i);
            repository.save(new InterviewQuestionEntity(
                    value.category(),
                    value.level(),
                    value.type(),
                    value.stem(),
                    toOptionsJson(value.options()),
                    value.correctIndex(),
                    value.referenceAnswer(),
                    value.explanation(),
                    seedKey(value.category(), value.stem()),
                    i,
                    now
            ));
        }
    }

    private String seedKey(String category, String stem) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((category + "\u0000" + stem).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 不可用", e);
        }
    }

    private InterviewDtos.QuestionRequest validate(InterviewDtos.QuestionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        String category = trim(request.category());
        String level = trim(request.level());
        String type = trim(request.type());
        String stem = trim(request.stem());
        String explanation = trim(request.explanation());

        if (isBlank(category) || category.length() > 32) {
            throw new IllegalArgumentException("category 不能为空且不超过 32 字");
        }
        if (!LEVELS.contains(level)) {
            throw new IllegalArgumentException("level 只能是 " + LEVELS);
        }
        if (!TYPES.contains(type)) {
            throw new IllegalArgumentException("type 只能是 " + TYPES);
        }
        if (isBlank(stem)) {
            throw new IllegalArgumentException("题干不能为空");
        }
        if (isBlank(explanation)) {
            throw new IllegalArgumentException("每道题都必须有解析");
        }

        List<String> options = request.options() == null ? List.of() : request.options().stream()
                .map(this::trim)
                .toList();
        Integer correctIndex = request.correctIndex();
        String referenceAnswer = trim(request.referenceAnswer());

        if ("choice".equals(type)) {
            if (options.size() < 2 || options.size() > 6) {
                throw new IllegalArgumentException("选择题需要 2-6 个选项");
            }
            if (options.stream().anyMatch(this::isBlank)) {
                throw new IllegalArgumentException("选项内容不能为空");
            }
            if (correctIndex == null || correctIndex < 0 || correctIndex >= options.size()) {
                throw new IllegalArgumentException("选择题必须指定正确的选项下标");
            }
            referenceAnswer = null;
        } else {
            options = List.of();
            correctIndex = null;
            if (isBlank(referenceAnswer)) {
                throw new IllegalArgumentException("自答题必须提供参考答案");
            }
        }

        return new InterviewDtos.QuestionRequest(
                category, level, type, stem, options, correctIndex, referenceAnswer, explanation
        );
    }

    private InterviewQuestionEntity entity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("QUESTION_NOT_FOUND", "未找到题目: " + id));
    }

    private InterviewDtos.QuestionView toView(InterviewQuestionEntity entity) {
        return new InterviewDtos.QuestionView(
                entity.getId(),
                entity.getCategory(),
                entity.getLevel(),
                entity.getType(),
                entity.getStem(),
                fromOptionsJson(entity.getOptions()),
                entity.getCorrectIndex(),
                entity.getReferenceAnswer(),
                entity.getExplanation()
        );
    }

    private String toOptionsJson(List<String> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new IllegalArgumentException("选项序列化失败");
        }
    }

    private List<String> fromOptionsJson(String json) {
        if (isBlank(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("题目选项 JSON 无法解析", e);
        }
    }

    private boolean matches(InterviewQuestionEntity item, String needle) {
        return item.getStem().toLowerCase(Locale.ROOT).contains(needle)
                || item.getExplanation().toLowerCase(Locale.ROOT).contains(needle);
    }

    private int levelRank(String level) {
        int index = LEVELS.indexOf(level);
        return index < 0 ? LEVELS.size() : index;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
