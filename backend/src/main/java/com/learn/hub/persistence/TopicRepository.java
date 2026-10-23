package com.learn.hub.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 学习项仓储：按 slug 查询与存在性判断。
 */
public interface TopicRepository extends JpaRepository<TopicEntity, Long> {

    Optional<TopicEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
