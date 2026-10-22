# 学习内容 Schema

内容源文件在仓库根目录 `content/`，构建时复制进 Spring Boot classpath `content/`。

## 学习项 `content/topics/{slug}.json`

与 PRD 第 5.1 节字段一致。`published` 必须同时满足：

- `overviewMd` 非空
- `pathSteps` 至少 1 步
- `samples` 至少 1 个
- `pitfalls` 至少 3 条

`status` 为 `draft` 的项不会出现在公开 API。

分类枚举：`spring-core` / `data` / `cache` / `messaging` / `security` / `engineering` / `frontend` / `cloud` / `search` / `observability`

难度：`beginner` / `intermediate` / `practical`

接入状态：`docs_only` / `snippet` / `runtime`

## 路线 `content/tracks/{slug}.json`

- `slug`、`title`、`audience`
- `stages[]`：`title`、`goal`、`topicSlugs[]`
