CREATE TABLE app_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(64)  NOT NULL,
    created_at    DATETIME(3)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE learning_progress (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    topic_slug   VARCHAR(120) NOT NULL,
    state        VARCHAR(16)  NOT NULL,
    started_at   DATETIME(3)  NULL,
    completed_at DATETIME(3)  NULL,
    updated_at   DATETIME(3)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_progress_user_topic (user_id, topic_slug),
    KEY idx_progress_topic (topic_slug),
    CONSTRAINT fk_progress_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_progress_topic FOREIGN KEY (topic_slug) REFERENCES topic (slug) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE topic_note (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    topic_slug VARCHAR(120) NOT NULL,
    content    LONGTEXT     NOT NULL,
    created_at DATETIME(3)  NOT NULL,
    updated_at DATETIME(3)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_note_user_topic (user_id, topic_slug),
    CONSTRAINT fk_note_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_note_topic FOREIGN KEY (topic_slug) REFERENCES topic (slug) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
