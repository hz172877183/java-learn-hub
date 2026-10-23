CREATE TABLE interview_question (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    category         VARCHAR(32) NOT NULL,
    level            VARCHAR(16) NOT NULL,
    type             VARCHAR(16) NOT NULL,
    stem             TEXT        NOT NULL,
    options          LONGTEXT    NULL,
    correct_index    INT         NULL,
    reference_answer TEXT        NULL,
    explanation      TEXT        NOT NULL,
    sort_order       INT         NOT NULL DEFAULT 0,
    created_at       DATETIME(3) NOT NULL,
    updated_at       DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_interview_cat_level (category, level)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
