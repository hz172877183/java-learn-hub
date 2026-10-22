package com.learn.hub.learning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<NoteEntity, Long> {

    List<NoteEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<NoteEntity> findByUserIdAndTopicSlug(Long userId, String topicSlug);
}
