package com.learn.hub.topic;

import com.learn.hub.content.ContentCatalog;
import com.learn.hub.content.ContentModels;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopicServiceTest {

    private TopicService service;

    @BeforeEach
    void setUp() {
        ContentModels.TopicDocument published = topic("redis", "Redis", "cache", "published");
        ContentModels.TopicDocument draft = topic("secret", "未发布", "cache", "draft");
        ContentCatalog catalog = new ContentCatalog(
                Map.of("redis", published, "secret", draft),
                Map.of()
        );
        service = new TopicService(catalog);
    }

    @Test
    void listHidesDrafts() {
        TopicDtos.TopicListResponse res = service.list(null, null, null, null, 0, 20);
        assertEquals(1, res.total());
        assertEquals("redis", res.items().getFirst().slug());
    }

    @Test
    void searchIsCaseInsensitive() {
        TopicDtos.TopicListResponse res = service.list(null, null, null, "REDIS", 0, 20);
        assertEquals(1, res.total());
    }

    @Test
    void detailIncludesScenarios() {
        assertEquals(1, service.detail("redis").scenarios().size());
        assertEquals("热点数据", service.detail("redis").scenarios().getFirst().title());
    }

    @Test
    void unknownSlugIs404() {
        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.detail("nope"));
        assertEquals("TOPIC_NOT_FOUND", ex.getCode());
    }

    @Test
    void draftDetailIsHidden() {
        assertThrows(NotFoundException.class, () -> service.detail("secret"));
    }

    @Test
    void invalidPageRejected() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.list(null, null, null, null, -1, 20)
        );
        assertTrue(ex.getMessage().contains("page"));
    }

    private ContentModels.TopicDocument topic(String slug, String title, String category, String status) {
        return new ContentModels.TopicDocument(
                slug,
                title,
                category,
                "摘要",
                "beginner",
                1,
                List.of("cache"),
                status,
                "docs_only",
                null,
                List.of(),
                List.of(),
                List.of(),
                "概述",
                List.of(),
                List.of(new ContentModels.PathStep("起步", "目标", 1.0, List.of())),
                List.of(new ContentModels.Sample("示例", "java", "desc", "code")),
                List.of(
                        new ContentModels.Pitfall("a", "a"),
                        new ContentModels.Pitfall("b", "b"),
                        new ContentModels.Pitfall("c", "c")
                ),
                List.of(),
                List.of(new ContentModels.Scenario("热点数据", "重复查库", "加缓存"))
        );
    }
}
