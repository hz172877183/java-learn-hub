package com.learn.hub.api;

import com.learn.hub.topic.TopicDtos;
import com.learn.hub.topic.TopicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开只读接口：学习项目录、详情、路线和分类统计。
 */
@RestController
@RequestMapping("/api/v1")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    /**
     * 学习项目录：分页 + 分类/难度/接入状态/关键词筛选。
     */
    @GetMapping("/topics")
    public TopicDtos.TopicListResponse list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String integrationStatus,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return topicService.list(category, difficulty, integrationStatus, q, page, size);
    }

    /**
     * 学习项详情；草稿与未知 slug 返回 404。
     */
    @GetMapping("/topics/{slug}")
    public TopicDtos.TopicDetail detail(@PathVariable String slug) {
        return topicService.detail(slug);
    }

    /**
     * 路线列表：供前台切换不同学习路线。
     */
    @GetMapping("/tracks")
    public List<TopicDtos.TrackSummary> tracks() {
        return topicService.tracks();
    }

    /**
     * 推荐路线：阶段 + 每个阶段引用的学习项摘要。
     */
    @GetMapping("/tracks/{slug}")
    public TopicDtos.TrackResponse track(@PathVariable String slug) {
        return topicService.track(slug);
    }

    /**
     * 分类统计，供目录页筛选器展示数量。
     */
    @GetMapping("/meta/categories")
    public TopicDtos.MetaResponse meta() {
        return topicService.meta();
    }
}
