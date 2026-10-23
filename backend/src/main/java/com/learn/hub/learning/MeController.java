package com.learn.hub.learning;

import com.learn.hub.user.AuthDtos;
import com.learn.hub.user.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 当前用户接口：个人信息、学习进度与私人笔记，需要 JWT。
 */
@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final AuthService authService;
    private final LearningService learningService;

    public MeController(AuthService authService, LearningService learningService) {
        this.authService = authService;
        this.learningService = learningService;
    }

    /**
     * 当前登录用户信息。
     */
    @GetMapping
    public AuthDtos.UserView me(@RequestAttribute("currentUserId") Long userId) {
        return authService.view(userId);
    }

    /**
     * 我的学习进度列表。
     */
    @GetMapping("/progress")
    public List<LearningDtos.ProgressView> progress(@RequestAttribute("currentUserId") Long userId) {
        return learningService.progress(userId);
    }

    /**
     * 更新单个学习项状态。
     */
    @PutMapping("/progress/{slug}")
    public LearningDtos.ProgressView setProgress(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String slug,
            @RequestBody LearningDtos.ProgressRequest request
    ) {
        return learningService.setProgress(userId, slug, request == null ? null : request.state());
    }

    /**
     * 重置进度（回到未开始）。
     */
    @DeleteMapping("/progress/{slug}")
    public ResponseEntity<Void> resetProgress(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String slug
    ) {
        learningService.resetProgress(userId, slug);
        return ResponseEntity.noContent().build();
    }

    /**
     * 我的全部笔记（按最近更新倒序）。
     */
    @GetMapping("/notes")
    public List<LearningDtos.NoteView> notes(@RequestAttribute("currentUserId") Long userId) {
        return learningService.notes(userId);
    }

    /**
     * 读取某个学习项的笔记；没写过返回空内容。
     */
    @GetMapping("/notes/{slug}")
    public LearningDtos.NoteView note(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String slug
    ) {
        return learningService.note(userId, slug);
    }

    /**
     * 保存笔记（upsert）；空白内容等价于删除。
     */
    @PutMapping("/notes/{slug}")
    public LearningDtos.NoteView saveNote(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String slug,
            @RequestBody LearningDtos.NoteSaveRequest request
    ) {
        return learningService.saveNote(userId, slug, request == null ? null : request.content());
    }

    /**
     * 删除笔记。
     */
    @DeleteMapping("/notes/{slug}")
    public ResponseEntity<Void> deleteNote(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String slug
    ) {
        learningService.deleteNote(userId, slug);
        return ResponseEntity.noContent().build();
    }
}
