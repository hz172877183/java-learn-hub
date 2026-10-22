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

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final AuthService authService;
    private final LearningService learningService;

    public MeController(AuthService authService, LearningService learningService) {
        this.authService = authService;
        this.learningService = learningService;
    }

    @GetMapping
    public AuthDtos.UserView me(@RequestAttribute("currentUserId") Long userId) {
        return authService.view(userId);
    }

    @GetMapping("/progress")
    public List<LearningDtos.ProgressView> progress(@RequestAttribute("currentUserId") Long userId) {
        return learningService.progress(userId);
    }

    @PutMapping("/progress/{slug}")
    public LearningDtos.ProgressView setProgress(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String slug,
            @RequestBody LearningDtos.ProgressRequest request
    ) {
        return learningService.setProgress(userId, slug, request == null ? null : request.state());
    }

    @DeleteMapping("/progress/{slug}")
    public ResponseEntity<Void> resetProgress(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String slug
    ) {
        learningService.resetProgress(userId, slug);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notes")
    public List<LearningDtos.NoteView> notes(@RequestAttribute("currentUserId") Long userId) {
        return learningService.notes(userId);
    }

    @GetMapping("/notes/{slug}")
    public LearningDtos.NoteView note(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String slug
    ) {
        return learningService.note(userId, slug);
    }

    @PutMapping("/notes/{slug}")
    public LearningDtos.NoteView saveNote(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String slug,
            @RequestBody LearningDtos.NoteSaveRequest request
    ) {
        return learningService.saveNote(userId, slug, request == null ? null : request.content());
    }

    @DeleteMapping("/notes/{slug}")
    public ResponseEntity<Void> deleteNote(
            @RequestAttribute("currentUserId") Long userId,
            @PathVariable String slug
    ) {
        learningService.deleteNote(userId, slug);
        return ResponseEntity.noContent().build();
    }
}
