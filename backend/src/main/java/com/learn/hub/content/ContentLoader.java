package com.learn.hub.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ContentLoader {

    private final ObjectMapper objectMapper;

    public ContentLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ContentCatalog load() {
        try {
            Map<String, ContentModels.TopicDocument> topics = loadTopics();
            Map<String, ContentModels.TrackDocument> tracks = loadTracks();
            validate(topics, tracks);
            return new ContentCatalog(topics, tracks);
        } catch (IOException e) {
            throw new IllegalStateException("无法加载 content/ 目录", e);
        }
    }

    private Map<String, ContentModels.TopicDocument> loadTopics() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:content/topics/*.json");
        Map<String, ContentModels.TopicDocument> topics = new LinkedHashMap<>();
        for (Resource resource : resources) {
            try (InputStream in = resource.getInputStream()) {
                ContentModels.TopicDocument doc = objectMapper.readValue(in, ContentModels.TopicDocument.class)
                        .normalized();
                if (doc.slug() == null || doc.slug().isBlank()) {
                    throw new IllegalStateException("学习项缺少 slug: " + resource.getFilename());
                }
                if (topics.put(doc.slug(), doc) != null) {
                    throw new IllegalStateException("重复 slug: " + doc.slug());
                }
            }
        }
        if (topics.isEmpty()) {
            throw new IllegalStateException("未找到任何学习项 JSON");
        }
        return topics;
    }

    private Map<String, ContentModels.TrackDocument> loadTracks() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:content/tracks/*.json");
        Map<String, ContentModels.TrackDocument> tracks = new LinkedHashMap<>();
        for (Resource resource : resources) {
            try (InputStream in = resource.getInputStream()) {
                ContentModels.TrackDocument doc = objectMapper.readValue(in, ContentModels.TrackDocument.class)
                        .normalized();
                tracks.put(doc.slug(), doc);
            }
        }
        return tracks;
    }

    private void validate(
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
        for (ContentModels.TrackDocument track : tracks.values()) {
            for (ContentModels.Stage stage : track.stages()) {
                for (String slug : stage.topicSlugs()) {
                    if (!topics.containsKey(slug)) {
                        throw new IllegalStateException("路线 " + track.slug() + " 引用了不存在的学习项: " + slug);
                    }
                }
            }
        }
    }
}
