CREATE TABLE quiz_question (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    topic_slug VARCHAR(120) NOT NULL,
    type       VARCHAR(16)  NOT NULL,
    payload    LONGTEXT     NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    created_at DATETIME(3)  NOT NULL,
    updated_at DATETIME(3)  NOT NULL,
    PRIMARY KEY (id),
    KEY idx_quiz_topic (topic_slug),
    CONSTRAINT fk_quiz_topic FOREIGN KEY (topic_slug) REFERENCES topic (slug) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
