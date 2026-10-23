package com.learn.hub.quiz;

import com.learn.hub.content.ContentModels;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开题目接口：按学习项返回面试真题。
 */
@RestController
@RequestMapping("/api/v1/topics/{slug}/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * 返回某个学习项的面试题（含答案与知识点说明）。
     */
    @GetMapping
    public List<ContentModels.QuizQuestion> list(@PathVariable String slug) {
        return quizService.listPublished(slug);
    }
}
