package com.learn.hub.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestionEntity, Long> {

    List<QuizQuestionEntity> findByTopicSlugOrderBySortOrderAscIdAsc(String topicSlug);

    void deleteByTopicSlug(String topicSlug);
}
