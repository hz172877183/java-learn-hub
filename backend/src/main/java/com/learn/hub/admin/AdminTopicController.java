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

@RestController
@RequestMapping("/api/v1/admin/topics")
public class AdminTopicController {

    private final TopicAdminService topicAdminService;
    private final QuizService quizService;

    public AdminTopicController(TopicAdminService topicAdminService, QuizService quizService) {
        this.topicAdminService = topicAdminService;
        this.quizService = quizService;
    }

    @GetMapping
    public List<ContentModels.TopicDocument> list(@RequestParam(required = false) String q) {
        return topicAdminService.list(q);
    }

    @GetMapping("/{slug}")
    public ContentModels.TopicDocument get(@PathVariable String slug) {
        return topicAdminService.get(slug);
    }

    @PostMapping
    public ResponseEntity<ContentModels.TopicDocument> create(
            @RequestBody ContentModels.TopicDocument topic
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(topicAdminService.create(topic));
    }

    @PutMapping("/{slug}")
    public ContentModels.TopicDocument update(
            @PathVariable String slug,
            @RequestBody ContentModels.TopicDocument topic
    ) {
        return topicAdminService.update(slug, topic);
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> delete(@PathVariable String slug) {
        topicAdminService.delete(slug);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{slug}/quizzes")
    public List<ContentModels.QuizQuestion> quizzes(@PathVariable String slug) {
        return quizService.list(slug);
    }

    @PutMapping("/{slug}/quizzes")
    public List<ContentModels.QuizQuestion> replaceQuizzes(
            @PathVariable String slug,
            @RequestBody List<ContentModels.QuizQuestion> questions
    ) {
        return quizService.replace(slug, questions);
    }
}
