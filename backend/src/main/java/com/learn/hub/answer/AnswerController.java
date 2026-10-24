package com.learn.hub.answer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前用户的答题记录、错题本与统计，需要 JWT。
 */
@RestController
@RequestMapping("/api/v1/me/answers")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    /**
     * 答题记录列表；wrong=true 时只返回最近一次答错的题（错题本）。
     */
    @GetMapping
    public AnswerDtos.AnswerList list(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Boolean wrong,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return answerService.list(userId, source, wrong, level, category, page, size);
    }

    /**
     * 提交作答：选择题服务端判分，自答题记录内容与自评。
     */
    @PostMapping
    public AnswerDtos.AnswerView submit(
            @RequestAttribute("currentUserId") Long userId,
            @RequestBody AnswerDtos.SubmitRequest request
    ) {
        return answerService.submit(userId, request);
    }

    /**
     * 统计：总数 / 答对 / 答错 / 未判定。
     */
    @GetMapping("/stats")
    public AnswerDtos.Stats stats(@RequestAttribute("currentUserId") Long userId) {
        return answerService.stats(userId);
    }

    /**
     * 移出错题本（删除该题记录）。
     */
    @DeleteMapping("/{source}/{questionId}")
    public ResponseEntity<Void> delete(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String source,
            @PathVariable Long questionId
    ) {
        answerService.delete(userId, source, questionId);
        return ResponseEntity.noContent().build();
    }
}
