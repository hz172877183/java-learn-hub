package com.learn.hub.api;

import com.learn.hub.topic.TopicDtos;
import com.learn.hub.topic.TopicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

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

    @GetMapping("/topics/{slug}")
    public TopicDtos.TopicDetail detail(@PathVariable String slug) {
        return topicService.detail(slug);
    }

    @GetMapping("/tracks/{slug}")
    public TopicDtos.TrackResponse track(@PathVariable String slug) {
        return topicService.track(slug);
    }

    @GetMapping("/meta/categories")
    public TopicDtos.MetaResponse meta() {
        return topicService.meta();
    }
}
