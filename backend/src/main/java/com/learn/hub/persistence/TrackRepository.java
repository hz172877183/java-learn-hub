package com.learn.hub.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<TrackEntity, Long> {

    boolean existsBySlug(String slug);
}
