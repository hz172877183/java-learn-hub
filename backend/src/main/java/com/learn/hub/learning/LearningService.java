package com.learn.hub.learning;

import com.learn.hub.content.CatalogStore;
import com.learn.hub.content.ContentModels;
import com.learn.hub.topic.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class LearningService {

    private static final Set<String> STATES = Set.of("not_started", "learning", "completed");
    private static final int MAX_NOTE_LENGTH = 100_000;

    private final ProgressRepository progressRepository;
    private final NoteRepository noteRepository;
    private final CatalogStore catalogStore;

    public LearningService(
            ProgressRepository progressRepository,
            NoteRepository noteRepository,
            CatalogStore catalogStore
    ) {
        this.progressRepository = progressRepository;
        this.noteRepository = noteRepository;
        this.catalogStore = catalogStore;
    }

    @Transactional(readOnly = true)
    public List<LearningDtos.ProgressView> progress(Long userId) {
        return progressRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(entity -> toView(entity))
                .toList();
    }

    @Transactional
    public LearningDtos.ProgressView setProgress(Long userId, String slug, String state) {
        if (state == null || !STATES.contains(state)) {
            throw new IllegalArgumentException("state 只能是 " + STATES);
        }
        requireTopic(slug);

        ProgressEntity entity = progressRepository.findByUserIdAndTopicSlug(userId, slug).orElse(null);
        if ("not_started".equals(state)) {
            if (entity != null) {
                progressRepository.delete(entity);
            }
            return new LearningDtos.ProgressView(slug, title(slug), state, null, null, LocalDateTime.now());
        }
        LocalDateTime now = LocalDateTime.now();
        if (entity == null) {
            entity = new ProgressEntity(userId, slug, state, now);
        } else {
            entity.applyState(state, now);
        }
        return toView(progressRepository.save(entity));
    }

    @Transactional
    public void resetProgress(Long userId, String slug) {
        progressRepository.findByUserIdAndTopicSlug(userId, slug)
                .ifPresent(progressRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<LearningDtos.NoteView> notes(Long userId) {
        return noteRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(entity -> new LearningDtos.NoteView(
                        entity.getTopicSlug(),
                        title(entity.getTopicSlug()),
                        entity.getContent(),
                        entity.getUpdatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public LearningDtos.NoteView note(Long userId, String slug) {
        requireTopic(slug);
        return noteRepository.findByUserIdAndTopicSlug(userId, slug)
                .map(entity -> new LearningDtos.NoteView(
                        entity.getTopicSlug(), title(slug), entity.getContent(), entity.getUpdatedAt()))
                .orElseGet(() -> new LearningDtos.NoteView(slug, title(slug), "", null));
    }

    @Transactional
    public LearningDtos.NoteView saveNote(Long userId, String slug, String content) {
        requireTopic(slug);
        String value = content == null ? "" : content;
        if (value.length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("笔记不能超过 " + MAX_NOTE_LENGTH + " 字符");
        }
        LocalDateTime now = LocalDateTime.now();
        NoteEntity entity = noteRepository.findByUserIdAndTopicSlug(userId, slug).orElse(null);
        if (value.isBlank()) {
            if (entity != null) {
                noteRepository.delete(entity);
            }
            return new LearningDtos.NoteView(slug, title(slug), "", null);
        }
        if (entity == null) {
            entity = noteRepository.save(new NoteEntity(userId, slug, value, now));
        } else {
            entity.update(value, now);
            entity = noteRepository.save(entity);
        }
        return new LearningDtos.NoteView(slug, title(slug), entity.getContent(), entity.getUpdatedAt());
    }

    @Transactional
    public void deleteNote(Long userId, String slug) {
        noteRepository.findByUserIdAndTopicSlug(userId, slug)
                .ifPresent(noteRepository::delete);
    }

    private LearningDtos.ProgressView toView(ProgressEntity entity) {
        return new LearningDtos.ProgressView(
                entity.getTopicSlug(),
                title(entity.getTopicSlug()),
                entity.getState(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getUpdatedAt()
        );
    }

    private void requireTopic(String slug) {
        ContentModels.TopicDocument doc = catalogStore.get().topic(slug)
                .orElseThrow(() -> new NotFoundException("TOPIC_NOT_FOUND", "未找到学习项: " + slug));
        if (!doc.published()) {
            throw new NotFoundException("TOPIC_NOT_FOUND", "未找到学习项: " + slug);
        }
    }

    private String title(String slug) {
        return catalogStore.get().topic(slug)
                .map(ContentModels.TopicDocument::title)
                .orElse(slug);
    }
}
