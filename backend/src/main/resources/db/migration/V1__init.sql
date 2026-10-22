CREATE TABLE topic (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    slug               VARCHAR(120) NOT NULL,
    title              VARCHAR(200) NOT NULL,
    category           VARCHAR(64)  NOT NULL,
    difficulty         VARCHAR(32)  NOT NULL,
    status             VARCHAR(16)  NOT NULL,
    integration_status VARCHAR(32)  NOT NULL,
    estimated_hours    INT          NOT NULL DEFAULT 0,
    summary            VARCHAR(500) NULL,
    payload            LONGTEXT     NOT NULL,
    created_at         DATETIME(3)  NOT NULL,
    updated_at         DATETIME(3)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_topic_slug (slug),
    KEY idx_topic_status (status),
    KEY idx_topic_category (category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE track (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    slug       VARCHAR(120) NOT NULL,
    title      VARCHAR(200) NOT NULL,
    audience   VARCHAR(500) NULL,
    payload    LONGTEXT     NOT NULL,
    created_at DATETIME(3)  NOT NULL,
    updated_at DATETIME(3)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_track_slug (slug)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
