package com.learn.hub.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 路线仓储。
 */
public interface TrackRepository extends JpaRepository<TrackEntity, Long> {

    Optional<TrackEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
