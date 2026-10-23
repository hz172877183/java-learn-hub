package com.learn.hub.admin;

import com.learn.hub.content.ContentModels;
import com.learn.hub.quiz.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端学习项接口：学习项增删改查与题目整组替换，由 AdminAuthFilter 保护。
 */
@RestController
@RequestMapping("/api/v1/admin/topics")
public class AdminTopicController {

    private final TopicAdminService topicAdminService;
    private final QuizService quizService;

    public AdminTopicController(TopicAdminService topicAdminService, QuizService quizService) {
        this.topicAdminService = topicAdminService;
        this.quizService = quizService;
    }

    /**
     * 管理端列表：包含草稿，支持简单关键词过滤。
     */
    @GetMapping
    public List<ContentModels.TopicDocument> list(@RequestParam(required = false) String q) {
        return topicAdminService.list(q);
    }

    /**
     * 读取单个学习项（管理端可读草稿）。
     */
    @GetMapping("/{slug}")
    public ContentModels.TopicDocument get(@PathVariable String slug) {
        return topicAdminService.get(slug);
    }

    /**
     * 新建学习项，成功返回 201 与完整文档。
     */
    @PostMapping
    public ResponseEntity<ContentModels.TopicDocument> create(
            @RequestBody ContentModels.TopicDocument topic
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(topicAdminService.create(topic));
    }

    /**
     * 更新学习项；slug 以路径为准，不允许改名。
     */
    @PutMapping("/{slug}")
    public ContentModels.TopicDocument update(
            @PathVariable String slug,
            @RequestBody ContentModels.TopicDocument topic
    ) {
        return topicAdminService.update(slug, topic);
    }

    /**
     * 删除学习项；关联进度与笔记由外键级联清理。
     */
    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> delete(@PathVariable String slug) {
        topicAdminService.delete(slug);
        return ResponseEntity.noContent().build();
    }

    /**
     * 读取某个学习项的题目（管理端）。
     */
    @GetMapping("/{slug}/quizzes")
    public List<ContentModels.QuizQuestion> quizzes(@PathVariable String slug) {
        return quizService.list(slug);
    }

    /**
     * 题目的整组替换：请求体即该学习项的最新题目列表。
     */
    @PutMapping("/{slug}/quizzes")
    public List<ContentModels.QuizQuestion> replaceQuizzes(
            @PathVariable String slug,
            @RequestBody List<ContentModels.QuizQuestion> questions
    ) {
        return quizService.replace(slug, questions);
    }
}
