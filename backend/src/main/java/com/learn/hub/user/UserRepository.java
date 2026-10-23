package com.learn.hub.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 用户仓储：按用户名查询与存在性判断。
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
