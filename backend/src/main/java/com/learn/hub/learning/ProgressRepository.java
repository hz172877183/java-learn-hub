package com.learn.hub.learning;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<ProgressEntity, Long> {

    List<ProgressEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<ProgressEntity> findByUserIdAndTopicSlug(Long userId, String topicSlug);

    long countByUserIdAndState(Long userId, String state);
}
