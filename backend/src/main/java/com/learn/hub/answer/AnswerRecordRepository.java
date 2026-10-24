package com.learn.hub.answer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRecordRepository extends JpaRepository<AnswerRecordEntity, Long> {

    List<AnswerRecordEntity> findByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<AnswerRecordEntity> findByUserIdAndSourceAndQuestionId(
            Long userId,
            String source,
            Long questionId
    );

    long countByUserId(Long userId);

    long countByUserIdAndCorrect(Long userId, Boolean correct);
}
