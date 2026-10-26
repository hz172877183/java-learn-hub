package com.learn.hub.interview;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录用户的面试题接口：默认排除已答，内联本人作答，并支持按范围重置。
 */
@RestController
@RequestMapping("/api/v1/me/interview")
public class MeInterviewController {

    private final InterviewService interviewService;

    public MeInterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * 题目列表；includeAnswered=false 时排除已答，wrongOnly=true 时只看错题。
     */
    @GetMapping("/questions")
    public InterviewDtos.QuestionList questions(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean includeAnswered,
            @RequestParam(defaultValue = "false") boolean wrongOnly
    ) {
        return interviewService.listForUser(
                userId, category, level, q, page, size, includeAnswered, wrongOnly
        );
    }

    /**
     * 按等级/分类重置面试题答题记录；都不传则清空全部。
     */
    @DeleteMapping("/answers")
    public InterviewDtos.ResetResult reset(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category
    ) {
        return new InterviewDtos.ResetResult(interviewService.resetAnswers(userId, level, category));
    }
}
