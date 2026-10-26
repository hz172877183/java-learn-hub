package com.learn.hub.position;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudyPositionRepository extends JpaRepository<StudyPositionEntity, Long> {

    Optional<StudyPositionEntity> findByUserIdAndScope(Long userId, String scope);
}
