# 新增学习目录编写规范

每个新学习项需要两个文件：

1. `content/topics/{slug}.json`：学习项正文。
2. `content/quizzes/{slug}.json`：随堂题数组（3 道：2 道 choice + 1 道 text）。

## 学习项字段

- `slug`、`title`、`category`、`summary`（80 字内）
- `difficulty`：`beginner` / `intermediate` / `practical`
- `estimatedHours`：整数
- `tags`：4-6 个
- `status`：`published`
- `integrationStatus`：`docs_only` 或 `snippet`
- `officialDocsUrl`：官方文档地址
- `prerequisites` / `nextTopics` / `relatedTopics`：只能引用已有 slug
- `overviewMd`：4-5 段 Markdown，讲定位、核心模型、适用/不适用、版本注意点、与本仓库的关系
- `concepts`：8-10 条，每条 40-80 字
- `pathSteps`：6-7 步，含 `title`、`goal`、`hours`、`resources`，hours 合计接近 `estimatedHours`
- `samples`：2-3 段，`title`、`language`、`description`、`code`；代码 15-45 行，真实可参考
- `pitfalls`：5-6 条，`title` + `detail`，写现象、原因、规避
- `scenarios`：4-5 条，`title`、`signal`、`approach`
- `repoPointers`：可选数组

## 随堂题字段

```json
[
  {
    "type": "choice",
    "stem": "题干",
    "options": ["A", "B", "C", "D"],
    "correctIndex": 1,
    "explanation": "知识点说明"
  },
  {
    "type": "text",
    "stem": "题干",
    "options": [],
    "referenceAnswer": "参考答案",
    "explanation": "知识点说明"
  }
]
```

## 可引用的 slug

actuator, docker-basics, elasticsearch, flyway, junit-mockito, kafka, mybatis-plus, mysql-basics, rabbitmq, redis, spring-boot-fundamentals, spring-cloud-overview, spring-data-jpa, spring-security-jwt, spring-web-rest, vue3-spa, cors-practice, http-error-contract, jwt-auth-practice, spring-security-basics, config-and-secrets, integration-testing,
spring-boot-advanced, reactive-webflux, spring-ai-rag, spring-cloud-gateway, kubernetes-basics, docker-compose, nginx-basics, linux-basics, network-basics, design-patterns, ddd-domain-modeling, observability-prometheus, mysql-tuning, redis-advanced

## 格式

UTF-8 JSON，无 BOM、无 Markdown 围栏、无注释、无尾逗号。写完后用 read 工具读回检查结构和字段数量。
