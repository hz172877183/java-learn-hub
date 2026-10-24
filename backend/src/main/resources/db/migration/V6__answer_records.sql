CREATE TABLE answer_record (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL,
    source        VARCHAR(16) NOT NULL,
    question_id   BIGINT      NOT NULL,
    question_type VARCHAR(16) NOT NULL,
    choice_index  INT         NULL,
    text_answer   TEXT        NULL,
    correct       TINYINT     NULL,
    attempts      INT         NOT NULL DEFAULT 1,
    answered_at   DATETIME(3) NOT NULL,
    updated_at    DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_answer_user_question (user_id, source, question_id),
    KEY idx_answer_user_result (user_id, correct, updated_at),
    CONSTRAINT fk_answer_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
