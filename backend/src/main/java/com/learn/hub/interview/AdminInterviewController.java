package com.learn.hub.interview;

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

/**
 * 管理端面试题接口，由 AdminAuthFilter 统一保护。
 */
@RestController
@RequestMapping("/api/v1/admin/interview/questions")
public class AdminInterviewController {

    private final InterviewService interviewService;

    public AdminInterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * 管理端列表：与公开列表共用筛选，默认返回较大分页。
     */
    @GetMapping
    public InterviewDtos.QuestionList list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return interviewService.list(category, level, q, page, size);
    }

    @GetMapping("/{id}")
    public InterviewDtos.QuestionView get(@PathVariable Long id) {
        return interviewService.get(id);
    }

    @PostMapping
    public ResponseEntity<InterviewDtos.QuestionView> create(
            @RequestBody InterviewDtos.QuestionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewService.create(request));
    }

    @PutMapping("/{id}")
    public InterviewDtos.QuestionView update(
            @PathVariable Long id,
            @RequestBody InterviewDtos.QuestionRequest request
    ) {
        return interviewService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
