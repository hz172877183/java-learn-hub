package com.learn.hub.interview;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公开面试题接口：列表筛选与分类统计。
 */
@RestController
@RequestMapping("/api/v1/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * 题目列表：可按分类、等级与关键词筛选，分页返回。
     */
    @GetMapping("/questions")
    public InterviewDtos.QuestionList questions(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return interviewService.list(category, level, q, page, size);
    }

    /**
     * 分类与等级的数量统计。
     */
    @GetMapping("/meta")
    public InterviewDtos.Meta meta() {
        return interviewService.meta();
    }
}
