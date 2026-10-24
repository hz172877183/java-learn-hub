package com.learn.hub.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.hub.persistence.TopicEntity;
import com.learn.hub.persistence.TopicRepository;
import com.learn.hub.persistence.TrackEntity;
import com.learn.hub.persistence.TrackRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 内容加载器：从数据库读取并解析 JSON，也负责种子文件读取与校验。
 */
@Component
public class ContentLoader {

    public record SeedQuiz(String topicSlug, List<ContentModels.QuizQuestion> questions) {
    }

    private final ObjectMapper objectMapper;
    private final TopicRepository topicRepository;
    private final TrackRepository trackRepository;

    public ContentLoader(
            ObjectMapper objectMapper,
            TopicRepository topicRepository,
            TrackRepository trackRepository
    ) {
        this.objectMapper = objectMapper;
        this.topicRepository = topicRepository;
        this.trackRepository = trackRepository;
    }

    /**
     * 从数据库加载全部内容，构建只读快照。数据库是唯一数据源。
     */
    public ContentCatalog load() {
        Map<String, ContentModels.TopicDocument> topics = new LinkedHashMap<>();
        for (TopicEntity entity : topicRepository.findAll()) {
            ContentModels.TopicDocument doc = parseTopic(entity.getPayload());
            topics.put(doc.slug(), doc);
        }

        Map<String, ContentModels.TrackDocument> tracks = new LinkedHashMap<>();
        for (TrackEntity entity : trackRepository.findAll()) {
            ContentModels.TrackDocument doc = parseTrack(entity.getPayload());
            tracks.put(doc.slug(), doc);
        }

        validate(topics, tracks);
        return new ContentCatalog(topics, tracks);
    }

    /**
     * 读取打包进 classpath 的 content/topics/*.json，用作首次启动的种子数据。
     */
    public List<ContentModels.TopicDocument> readSeedTopics() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:content/topics/*.json");
        List<ContentModels.TopicDocument> topics = new ArrayList<>();
        for (Resource resource : resources) {
            try (InputStream in = resource.getInputStream()) {
                ContentModels.TopicDocument doc = objectMapper.readValue(in, ContentModels.TopicDocument.class)
                        .normalized();
                if (doc.slug() == null || doc.slug().isBlank()) {
                    throw new IllegalStateException("学习项缺少 slug: " + resource.getFilename());
                }
                topics.add(doc);
            }
        }
        return topics;
    }

    /**
     * 读取打包进 classpath 的 content/tracks/*.json，用作首次启动的种子数据。
     */
    public List<ContentModels.TrackDocument> readSeedTracks() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:content/tracks/*.json");
        List<ContentModels.TrackDocument> tracks = new ArrayList<>();
        for (Resource resource : resources) {
            try (InputStream in = resource.getInputStream()) {
                tracks.add(objectMapper.readValue(in, ContentModels.TrackDocument.class).normalized());
            }
        }
        return tracks;
    }

    /**
     * 读取 content/quizzes/{topicSlug}.json，每个文件是该学习项的题目数组。
     */
    public List<SeedQuiz> readSeedQuizzes() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:content/quizzes/*.json");
        List<SeedQuiz> result = new ArrayList<>();
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null) {
                continue;
            }
            String topicSlug = filename.substring(0, filename.length() - ".json".length());
            try (InputStream in = resource.getInputStream()) {
                ContentModels.QuizQuestion[] questions =
                        objectMapper.readValue(in, ContentModels.QuizQuestion[].class);
                List<ContentModels.QuizQuestion> list = new ArrayList<>();
                for (ContentModels.QuizQuestion question : questions) {
                    list.add(question.normalized());
                }
                result.add(new SeedQuiz(topicSlug, list));
            }
        }
        return result;
    }

    /**
     * 读取 content/seed-version.txt，用于判断是否需要同步种子内容。
     */
    public String readSeedVersion() {
        Resource resource = new ClassPathResource("content/seed-version.txt");
        if (!resource.exists()) {
            return "0";
        }
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalStateException("内容种子版本文件无法读取", e);
        }
    }

    /**
     * 读取 content/interview/questions.json，作为面试题库的种子。
     */
    public List<ContentModels.InterviewSeed> readSeedInterviewQuestions() throws IOException {
        Resource resource = new ClassPathResource("content/interview/questions.json");
        if (!resource.exists()) {
            return List.of();
        }
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readValue(in, new TypeReference<List<ContentModels.InterviewSeed>>() {
            });
        }
    }

    /**
     * 解析数据库里的学习项 JSON，并补齐缺失字段。
     */
    public ContentModels.TopicDocument parseTopic(String json) {
        try {
            return objectMapper.readValue(json, ContentModels.TopicDocument.class).normalized();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("数据库中的学习项 JSON 无法解析", e);
        }
    }

    /**
     * 解析数据库里的路线 JSON。
     */
    public ContentModels.TrackDocument parseTrack(String json) {
        try {
            return objectMapper.readValue(json, ContentModels.TrackDocument.class).normalized();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("数据库中的路线 JSON 无法解析", e);
        }
    }

    /**
     * 解析数据库里的题目 JSON。
     */
    public ContentModels.QuizQuestion parseQuiz(String json) {        try {
            return objectMapper.readValue(json, ContentModels.QuizQuestion.class).normalized();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("数据库中的题目 JSON 无法解析", e);
        }
    }

    public List<String> parseOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("题目选项 JSON 无法解析", e);
        }
    }

    /**
     * 序列化为紧凑 JSON，作为数据库 payload 保存。
     */
    public String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("内容无法序列化为 JSON", e);
        }
    }

    /**
     * 全量校验：已发布内容必须完整；路线允许引用暂时不存在的学习项。
     */
    public void validate(
            Map<String, ContentModels.TopicDocument> topics,
            Map<String, ContentModels.TrackDocument> tracks
    ) {
        for (ContentModels.TopicDocument topic : topics.values()) {
            if (!topic.published()) {
                continue;
            }
            if (topic.overviewMd().isBlank()
                    || topic.pathSteps().isEmpty()
                    || topic.samples().isEmpty()
                    || topic.pitfalls().size() < 3) {
                throw new IllegalStateException(
                        "已发布学习项不完整（需要概述、路线、至少 1 个示例、至少 3 条常见坑）: " + topic.slug());
            }
        }
        // 路线允许临时引用不存在的学习项，读取时会自动跳过，避免删除学习项被路线阻塞。
    }

    /**
     * 单条校验，供管理端写入前复用同一套发布规则。
     */
    public void validateOne(ContentModels.TopicDocument topic) {
        Map<String, ContentModels.TopicDocument> single = new LinkedHashMap<>();
        single.put(topic.slug(), topic);
        validate(single, Map.of());
    }
}
