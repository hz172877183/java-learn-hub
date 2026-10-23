package com.learn.hub.quiz;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 题目仓储：按学习项顺序读取，整组替换时先删后插。
 */
public interface QuizQuestionRepository extends JpaRepository<QuizQuestionEntity, Long> {

    List<QuizQuestionEntity> findByTopicSlugOrderBySortOrderAscIdAsc(String topicSlug);

    void deleteByTopicSlug(String topicSlug);
}
