# java-learn-hub 产品需求文档（PRD）

| 字段 | 内容 |
| --- | --- |
| 产品名称 | java-learn-hub |
| 版本 | v0.1（需求冻结前草案） |
| 状态 | 草案，待评审 |
| 日期 | 2026-10-20 |
| 受众 | 产品、研发、内容编写 |

---

## 1. 背景与目标

学习者面对 Java 生态时，文档分散、示例割裂：官方文档讲概念，教程讲单点，真实项目又把中间件、安全、消息、缓存绑在一起，很难把「学什么、按什么顺序学、这段代码在完整系统里怎么用」串起来。

本项目同时做两件事：

1. **学习门户**：前端列出热门技术栈学习项；点进去能看到说明、学习路线、示例代码、常见坑、与本仓库其他模块的关系。
2. **可运行样板**：后端以 Spring Boot 为主框架，按阶段把这些技术真正接到同一个应用里，学习项状态能反映「仅文档 / 已接入示例 / 已用于核心业务」。

### 1.1 成功标准（产品级）

- 新人打开站点 3 分钟内能看懂「学什么、先学哪条路线」。
- 每个已发布学习项都具备：说明、路线、至少一段可对照的示例代码。
- 后续工程实现时，学习门户的数据来自后端接口，而不是写死在前端。

### 1.2 非目标（明确不做）

- 不做完整网校：无视频课、作业批改、证书、支付。
- 一期不做社交：评论、点赞、关注、UGC。
- 一期不做多租户、企业培训后台。
- 不追求把所有中间件默认全部启动；未接入的技术以「文档 + 代码片段」存在，避免本机跑不起来。

---

## 2. 用户与场景

### 2.1 角色

| 角色 | 描述 | 核心诉求 |
| --- | --- | --- |
| 初级学习者 | 会一点 Java，准备入门 Spring | 有顺序、有难度、能看懂示例 |
| 求职者 | 要对齐面试/业务常见栈 | 覆盖 Redis、消息队列、安全、微服务概念 |
| 内容维护者（开发者本人） | 维护本仓库内容与示例 | 用结构化数据增改学习项，避免改一堆页面 |

一期不单独做登录用户体系；浏览者与维护者共用同一套内容，维护通过仓库或后续管理端完成。

### 2.2 关键场景

1. **逛目录**：按分类/难度筛选，了解当前热门栈全景。
2. **读详情**：进入某一技术（如 Redis），阅读定位、概念、路线、示例、坑。
3. **跟总路线**：按「后端入门 → 数据访问 → 缓存与消息 → 安全 → 微服务与可观测」走推荐路径。
4. **对照仓库**：看到该技术在本项目中的接入状态，以及示例所在模块路径（工程落地后填写）。

---

## 3. 产品范围

### 3.1 一期（MVP）

- 学习项目录（列表 + 筛选 + 搜索）。
- 学习项详情（说明、路线、示例代码、关联项、接入状态）。
- 一条官方推荐总学习路线（跨技术栈）。
- 后端提供学习内容只读 API。
- 内容以结构化数据（JSON/Markdown + 元数据）维护，随仓库发布。
- 前端 Vue 3 单页应用；后端 Spring Boot 3。

### 3.2 二期

- 管理端：学习项 CRUD、Markdown 编辑、发布状态。
- 代码示例与可运行 Demo 模块绑定（一键跳转 Git 路径或内嵌多文件示例）。
- 进度勾选（本地存储即可，可选登录后云同步）。
- 将 MySQL、Redis、Spring Security 接入真实业务（学习项内容的持久化与接口鉴权）。

### 3.3 三期

- 消息队列、Elasticsearch、对象存储、Spring Cloud 等按模块渐进接入。
- 可观测（Actuator、指标、追踪）教学页与真实端点对照。
- 可选：基于文档的简单测验。

---

## 4. 信息架构与页面

### 4.1 站点地图

```
首页（总览 + 推荐路线入口 + 热门学习项）
├── 学习目录（全量列表 / 分类 / 难度 / 标签 / 接入状态）
├── 学习项详情
│   ├── 概述与适用场景
│   ├── 核心概念
│   ├── 学习路线（步骤）
│   ├── 示例代码
│   ├── 常见问题与坑
│   └── 关联技术 / 本仓库接入情况
├── 总学习路线
└── 关于本项目（技术选型、如何本地运行、贡献内容约定）
```

### 4.2 首页

- 一句话定位。
- 「推荐 8～12 周路线」入口。
- 按阶段展示 4～6 个精选学习项卡片。
- 统计：已发布学习项数量、已接入示例数量。

### 4.3 学习目录

筛选维度：

- 分类：语言基础、Spring 核心、数据访问、缓存、消息、搜索、安全、微服务、工程化、可观测、前端配套。
- 难度：入门 / 进阶 / 工程实践。
- 接入状态：仅文档 / 有示例代码 / 已接入运行时。
- 关键词搜索：标题、标签、摘要。

列表卡片字段：名称、一句话简介、难度、预计学习时长、标签、接入状态。

### 4.4 学习项详情

必须区块（缺一不可，否则不允许标记为「已发布」）：

1. **概述**：是什么、解决什么问题、何时该用、何时不该用。
2. **在本项目中的位置**：是否已接入、对应模块/依赖、环境变量或端口（未接入则写「规划中」）。
3. **核心概念**：5～12 条术语，每条 2～6 句。
4. **学习路线**：有序步骤，每步含目标、建议资料类型（官方文档/本页示例/外部链接）、预计时间。
5. **示例代码**：至少 1 个完整片段（可复制），含语言、说明、关键注释；鼓励 2～3 个递进示例（最小用法 → 和 Spring 集成 → 和本项目场景结合）。
6. **常见坑**：至少 3 条。
7. **关联学习项**：前置、后续、并列对比（如 JPA vs MyBatis）。

---

## 5. 内容模型

### 5.1 学习项（Topic）

| 字段 | 类型 | 约束 |
| --- | --- | --- |
| slug | string | 主键，URL 友好，如 `spring-security-jwt` |
| title | string | 展示名 |
| category | enum | 见分类枚举 |
| summary | string | ≤ 80 字 |
| difficulty | enum | beginner / intermediate / practical |
| estimatedHours | number | 建议学习时长 |
| tags | string[] | |
| status | enum | draft / published |
| integrationStatus | enum | docs_only / snippet / runtime |
| officialDocsUrl | string | 可选 |
| prerequisites | slug[] | |
| nextTopics | slug[] | |
| relatedTopics | slug[] | |
| overviewMd | markdown | 概述 |
| concepts | {term, explanation}[] | |
| pathSteps | {title, goal, hours, resources[]}[] | |
| samples | {title, language, description, code}[] | |
| pitfalls | {title, detail}[] | |
| repoPointers | {label, path}[] | 指向本仓库路径，可空 |
| updatedAt | datetime | |

### 5.2 总学习路线（Track）

| 字段 | 说明 |
| --- | --- |
| slug | 如 `java-backend-job-ready` |
| title | 面向求职的后端路线 |
| audience | 适用人群 |
| stages | 阶段列表，每阶段含 topic slug 列表与阶段目标 |

一期内置 1 条官方路线即可，数据与学习项同样来自内容仓库。

### 5.3 一期内容清单（必须覆盖）

发布 MVP 时，下列学习项至少达到 `published + docs_only`，其中带 `*` 的尽量提供 `snippet`：

**Spring 核心**

- Spring Boot 3 应用结构 *
- Spring Web（REST、校验、异常处理）*
- 配置与 Profile、Actuator 入门

**数据**

- MySQL 与表设计基础
- Spring Data JPA *
- MyBatis / MyBatis-Plus（对比 JPA）
- Flyway 数据库迁移

**缓存与会话态**

- Redis 核心结构与缓存模式 *

**安全**

- Spring Security + JWT *

**消息与异步**

- RabbitMQ 或 Kafka（一期先做概念 + 片段，二选一作为主推）

**工程化**

- Maven 多模块（可先单模块，文档预告多模块）
- 单元测试 JUnit 5 + Mockito
- Docker 入门（如何跑本项目依赖）

**前端配套**

- Vue 3 + Vite + TypeScript（本项目前端如何组织）*

**可观测与进阶（可 draft，目录可见但标「规划」）**

- Spring Cloud Gateway / OpenFeign / Nacos
- Elasticsearch
- MinIO
- Prometheus + Grafana
- Testcontainers

内容语气：中文讲解 + 代码标识符保持英文；示例默认 Java 21 / Spring Boot 3 风格。

---

## 6. 功能需求

编号规则：`FR-模块-序号`。优先级：P0 必须进 MVP，P1 二期，P2 以后。

### 6.1 学习门户

| ID | 需求 | 优先级 | 验收要点 |
| --- | --- | --- | --- |
| FR-WEB-01 | 访问首页看到定位、推荐路线入口、精选卡片 | P0 | 无登录可访问 |
| FR-WEB-02 | 目录页支持分类、难度、接入状态筛选 | P0 | 组合筛选结果正确；无结果有空态 |
| FR-WEB-03 | 目录支持标题/标签/摘要关键字搜索 | P0 | 大小写不敏感 |
| FR-WEB-04 | 卡片展示名称、摘要、难度、时长、状态 | P0 | 字段缺失时不崩 |
| FR-WEB-05 | 点击卡片进入详情，URL 为 `/topics/:slug` | P0 | 刷新可直达；错误 slug 显示 404 页 |
| FR-WEB-06 | 详情按第 4.4 节区块渲染 Markdown | P0 | 代码块可复制 |
| FR-WEB-07 | 详情展示前置/后续/相关跳转 | P0 | 链到已发布项；草稿不露出或标记未发布 |
| FR-WEB-08 | 总路线页按阶段列出学习项并跳转详情 | P0 | 阶段目标可见 |
| FR-WEB-09 | 关于页说明选型、仓库地址、本地运行（工程落地后补命令） | P0 | |
| FR-WEB-10 | 响应式：桌面与常见手机宽度可用 | P0 | 目录可筛、详情可读 |
| FR-WEB-11 | 学习项内进度勾选（仅浏览器本地） | P1 | 刷新保留 |
| FR-WEB-12 | 全文高亮搜索关键字 | P1 | |

### 6.2 内容 API

| ID | 需求 | 优先级 | 验收要点 |
| --- | --- | --- | --- |
| FR-API-01 | `GET /api/v1/topics` 分页列表，支持 category、difficulty、integrationStatus、q | P0 | 只返回 published |
| FR-API-02 | `GET /api/v1/topics/{slug}` 详情 | P0 | 草稿返回 404 |
| FR-API-03 | `GET /api/v1/tracks/{slug}` 总路线 | P0 | 含阶段与 topic 摘要 |
| FR-API-04 | `GET /api/v1/meta/categories` 分类与统计 | P0 | 供筛选项 |
| FR-API-05 | 统一错误体 `{ code, message, traceId }` | P0 | 404/400 有文档化 code |
| FR-API-06 | CORS 允许本地前端开发源 | P0 | |
| FR-API-07 | 管理端写接口 | P1 | 需鉴权 |

一期内容允许启动时从 classpath 的 JSON/Markdown 加载到内存；不强制 MySQL。接口契约先按将来落库设计，避免前端大改。

### 6.3 内容维护

| ID | 需求 | 优先级 | 验收要点 |
| --- | --- | --- | --- |
| FR-CNT-01 | 每个学习项一份结构化源文件，字段见第 5 节 | P0 | 缺必填字段则启动失败或构建失败 |
| FR-CNT-02 | 代码示例与 Markdown 分离存储，详情接口组装 | P0 | 便于高亮与复制 |
| FR-CNT-03 | 发布检查：缺路线或示例的项不能 published | P0 | |
| FR-CNT-04 | 管理 UI 编辑学习项 | P1 | |

---

## 7. 接口草案（MVP）

基路径：`/api/v1`  
数据格式：JSON，UTF-8  
时间：ISO-8601

### 7.1 列表

`GET /api/v1/topics?category=&difficulty=&integrationStatus=&q=&page=0&size=20`

响应：

```json
{
  "items": [
    {
      "slug": "redis",
      "title": "Redis",
      "category": "cache",
      "summary": "内存数据结构存储，用于缓存、锁与会话。",
      "difficulty": "intermediate",
      "estimatedHours": 8,
      "tags": ["cache", "nosql"],
      "integrationStatus": "snippet"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 16
}
```

### 7.2 详情

`GET /api/v1/topics/redis`

响应在列表字段基础上增加 `overviewMd`、`concepts`、`pathSteps`、`samples`、`pitfalls`、`prerequisites`、`nextTopics`、`relatedTopics`、`repoPointers`、`officialDocsUrl`、`updatedAt`。

### 7.3 路线

`GET /api/v1/tracks/java-backend-job-ready`

---

## 8. 技术选型（约定，本轮不实施）

| 层 | 选型 | 说明 |
| --- | --- | --- |
| JDK | 21 LTS | 当前长期支持版本 |
| 后端 | Spring Boot 3.x | 主框架 |
| 构建 | Maven | 生态常见 |
| API 风格 | REST | 一期足够；不引入 GraphQL |
| 一期持久化 | 文件 + 内存 | 降低启动成本 |
| 二期持久化 | MySQL 8 + Flyway | 学习项将同步作为 JPA 教学 |
| 缓存 | Redis | 二期接入，先文档 |
| 安全 | Spring Security + JWT | 二期管理端 |
| 前端 | Vue 3 + Vite + TypeScript + Vue Router + Pinia | 用户已选默认 |
| UI | Element Plus | 后台/内容站常见，表格筛选成本低 |
| 文档 | 本目录 Markdown | 需求与设计同仓库 |

微服务、消息、搜索等不进入 MVP 运行时依赖。

---

## 9. 总学习路线草案（内容需求）

路线 slug：`java-backend-job-ready`  
名称：Java 后端求职向路线  
受众：有 Java 基础、希望 8～12 周对齐常见业务栈

| 阶段 | 目标 | 学习项（slug 预告） |
| --- | --- | --- |
| 1. 站稳 Web | 能写 REST、理解 Boot 启动与配置 | `spring-boot-fundamentals`, `spring-web-rest` |
| 2. 把数据存住 | 会建模、迁移、ORM 与 SQL 对照 | `mysql-basics`, `spring-data-jpa`, `mybatis-plus`, `flyway` |
| 3. 变快与变稳 | 缓存、测试、基础可观测 | `redis`, `junit-mockito`, `actuator` |
| 4. 安全 | 认证授权与常见漏洞意识 | `spring-security-jwt` |
| 5. 异步 | 理解消息驱动 | `rabbitmq` 或 `kafka` |
| 6. 工程交付 | 容器与前端联调 | `docker-basics`, `vue3-spa` |
| 7. 进阶地图 | 知道微服务与搜索存在即可 | `spring-cloud-overview`, `elasticsearch`（可为规划） |

---

## 10. 非功能需求

| ID | 类型 | 要求 |
| --- | --- | --- |
| NFR-01 | 性能 | 目录接口 P95 < 200ms（内存数据、本机） |
| NFR-02 | 可用 | 前端静态资源可独立部署；后端挂掉时前端有错误态，不白屏 |
| NFR-03 | 兼容 | Chrome / Edge 近两个大版本；后端 Windows 可开发 |
| NFR-04 | 安全 | MVP 无鉴权写接口；不提交密钥；示例不含真实密码 |
| NFR-05 | 可维护 | 新增学习项不改前端路由硬编码（动态详情页） |
| NFR-06 | 可访问 | 代码块对比度足够；图片需 alt（若有） |
| NFR-07 | 国际化 | 一期 UI 与内容均为简体中文 |

---

## 11. 里程碑（文档之后的工程，不在本轮执行）

| 里程碑 | 交付 | 依赖 |
| --- | --- | --- |
| M0 | 本 PRD 评审通过 | — |
| M1 | 内容 schema + 至少 6 篇达标学习项文稿 | M0 |
| M2 | Spring Boot 只读 API + Vue 目录/详情/路线可演示 | M1 |
| M3 | 补齐第 5.3 节 MVP 清单 | M2 |
| M4 | MySQL + Redis + Security 接入，内容可落库 | M3 |

---

## 12. 验收清单（MVP 产品）

- [ ] 无需登录可浏览首页、目录、详情、路线。
- [ ] 每个 published 学习项含概述、路线、≥1 示例、≥3 坑。
- [ ] 筛选与搜索结果与数据一致。
- [ ] 未知 slug 有 404。
- [ ] 接口只暴露已发布内容。
- [ ] README 能说明如何启动前后端（工程落地后）。

---

## 13. 已拍板决策

| 议题 | 决策 |
| --- | --- |
| 消息队列 | 主推 RabbitMQ；Kafka 作为对照学习项（一期 `docs_only`） |
| 内容载体 | 仓库 `content/` 下 JSON（含 Markdown 字段），不上管理端 |
| 语言 | UI 与内容均为简体中文 |
| 外部 API | 示例禁止依赖付费/需密钥的外部服务 |
| 工程结构 | `backend/`（Spring Boot 单体）+ `frontend/`（Vue 3），不拆 Maven 多模块 |
| 仓库位置 | `D:\worksapce\java-learn-hub` |

---

## 14. 修订记录

| 版本 | 日期 | 说明 |
| --- | --- | --- |
| v0.1 | 2026-10-20 | 首稿，仅需求，不含实现 |
| v0.2 | 2026-10-21 | 冻结默认决策；仓库迁至 D:\worksapce |
