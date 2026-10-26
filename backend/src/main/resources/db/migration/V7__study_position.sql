CREATE TABLE study_position (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    scope      VARCHAR(64)  NOT NULL,
    payload    LONGTEXT     NOT NULL,
    updated_at DATETIME(3)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_position_user_scope (user_id, scope),
    CONSTRAINT fk_position_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
