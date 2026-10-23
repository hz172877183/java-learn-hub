-- 内容种子版本同步：让 content/ 下的种子在版本变化时自动增量更新数据库，
-- 同时保留管理端手工创建的内容。
ALTER TABLE interview_question
    ADD COLUMN seed_key VARCHAR(64) NULL AFTER explanation,
    ADD UNIQUE KEY uk_interview_seed_key (seed_key);

CREATE TABLE content_seed_state (
    state_key  VARCHAR(64)  NOT NULL,
    state_value VARCHAR(200) NOT NULL,
    updated_at DATETIME(3)  NOT NULL,
    PRIMARY KEY (state_key)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
