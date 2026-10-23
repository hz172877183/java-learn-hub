package com.learn.hub.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 路线仓储。
 */
public interface TrackRepository extends JpaRepository<TrackEntity, Long> {

    boolean existsBySlug(String slug);
}
