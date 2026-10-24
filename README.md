# java-learn-hub

面向 Java / Spring 生态的学习门户。仓库路径：`D:\workspace\java-learn-hub`。

- 需求：[docs/requirements/PRD.md](docs/requirements/PRD.md)
- 内容格式：[docs/content-schema.md](docs/content-schema.md)
- 学习项源文件（种子数据）：`content/topics/*.json`

## 技术栈

- Java 21 + Spring Boot 3.5（只读 API + 管理端写接口）
- MySQL 8 + Flyway + Spring Data JPA（内容落库，JSON 按版本同步为种子）
- Vue 3 + Vite + TypeScript + Element Plus（含 `/admin` 管理页）

## 数据库

本地用 Docker 起一个 MySQL 8（数据卷持久化）：

```powershell
docker run -d --name java-learn-hub-db `
  -e MYSQL_DATABASE=learnhub `
  -e MYSQL_USER=learnhub `
  -e MYSQL_PASSWORD=learnhub123 `
  -e MYSQL_ROOT_PASSWORD=root123 `
  -p 3306:3306 `
  -v learnhub_mysql:/var/lib/mysql `
  --restart unless-stopped `
  mysql:8.0
```

连接配置走环境变量，默认值见 `backend/src/main/resources/application.yml`：`DB_USER` / `DB_PASSWORD`。

- 建表由 Flyway 负责（`backend/src/main/resources/db/migration`），首次启动自动执行。
- 内容按版本同步：`content/seed-version.txt` 与数据库 `content_seed_state` 比较，版本变化时自动同步学习项、路线、随堂题和面试题；版本不变时数据库是唯一数据源，管理端编辑不会被覆盖。
- 想重新导入最新种子：修改 `content/seed-version.txt` 的版本号后重启后端即可，不需要清库。
- 管理端手工创建、且不在种子中的面试题带 `seed_key = NULL`，同步时保留。

## 启动

本机默认 `java` 若是 8，请先指定 JDK 21。若安装了 IntelliJ，可用其自带 JBR：

```powershell
$env:JAVA_HOME = "D:\soft\IntelliJ IDEA 2024.3.6\jbr"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
java -version
```

后端：

```powershell
cd D:\workspace\java-learn-hub\backend
mvn spring-boot:run
```

前端（另开终端）：

```powershell
cd D:\workspace\java-learn-hub\frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173 。API：http://localhost:8080/api/v1/topics

## 学习内容

- 现有 22 个学习项全部做过一轮扩写：每个目录包含 3–5 段概述、8–10 个核心概念、6–7 步学习路线、2–3 段示例代码、5–6 条常见坑、4–5 个典型使用场景。
- 新增 14 个热门技术栈学习目录：Spring Boot 进阶、响应式 WebFlux、Spring AI / RAG、Spring Cloud Gateway、Kubernetes、Docker Compose、Nginx、Linux、计算机网络、设计模式、DDD、可观测性、MySQL 调优、Redis 高可用。
- 新增「云原生与 AI 应用进阶路线」，`/track` 页面可在「Java 后端求职向路线」与该路线之间切换。
- 每个学习项配 2–3 道随堂题，种子文件在 `content/quizzes/{slug}.json`：
  - `choice` 单选：提交后立即判分，并展示知识点说明。
  - `text` 自答：系统不判分，用户先写，再展开参考答案与知识点。
- 场景和题目都可在管理端编辑；`content/` 按 `seed-version.txt` 版本同步。

## 面试题专区

- 前台 `/interview`：按等级（初级 / 中级 / 高级 / 架构师）和分类筛选；单选题提交即判分，自答题先作答再对照参考答案。
- 题库独立于学习项：种子文件 `content/interview/questions.json`，当前约 670 道，覆盖 23 个分类。
- 分类：Java 基础、集合、并发、JVM、Spring、Spring Boot、微服务、MySQL、Redis、消息队列、分布式、架构设计、工程实践、容器与 K8s、计算机网络、操作系统 / Linux、设计模式、算法与数据结构、前端、Web 安全、测试与质量、AI 应用、项目与软技能。
- 每道题都有解析；选择题 4 个选项，自答题给出分点参考答案。列表支持 10 / 20 / 50 条每页与页码跳转。
- 管理端 `/admin/interview`：新增、编辑、删除，支持自定义分类。
- 接口：`GET /api/v1/interview/questions`、`GET /api/v1/interview/meta`；管理端 `GET/POST/PUT/DELETE /api/v1/admin/interview/questions`。
- 题库分片：`content/interview/parts/*.json`，用 `python scripts/build_interview_bank.py` 合并去重，再用 `python scripts/validate_content.py` 校验。

## 手机访问

- 开发调试：前端用 `npm run dev -- --host 0.0.0.0`，手机与电脑连同一 Wi-Fi，访问 `http://电脑IP:5173`；Windows 防火墙需放行 5173。
- 手机端使用底部 Tab（目录 / 路线 / 面试 / 我的），笔记改为底部抽屉，输入框字号 ≥16px 避免 iOS 自动放大。
- 正式部署建议 Nginx/Caddy 同域托管 `dist` 并反代 `/api`，配 HTTPS 后可作为 PWA 安装到桌面。

## 答题记录与错题本

- 选择题提交后由服务端判分并记录；自答题保存作答内容，可自评「已掌握 / 加入错题本」。
- 同一道题重复作答只保留最近一次结果，并累计作答次数；答对后自动离开错题本。
- `/me/answers`：全部记录 / 错题本两个视图，可按来源（面试题 / 随堂题）、等级、分类筛选。
- 支持删除单条记录（移出错题本），以及已答、答对、答错、正确率统计。
- 接口：`GET/POST /api/v1/me/answers`、`GET /api/v1/me/answers/stats`、`DELETE /api/v1/me/answers/{source}/{questionId}`。

## 普通用户

- 注册 / 登录：`/register`、`/login`，接口为 `POST /api/v1/auth/register|login`，成功后返回 JWT。
- 权限：`/api/v1/me/**` 需要 `Authorization: Bearer <JWT>`；JWT 默认有效期 7 天（`LEARNHUB_JWT_TTL_HOURS`）。
- 学习进度：`/track` 页每个学习项可选「未开始 / 学习中 / 已完成」，并显示阶段与整体进度；`/me` 汇总。
- 私人笔记：每个学习项一条 Markdown 笔记，详情页右下角悬浮窗可随时打开，关闭时自动保存，仅本人可见（按 user_id 隔离）。
- 面试真题：`GET /api/v1/topics/{slug}/quizzes`，详情页内完成答题与查看解析。
- 相关表：`app_user`、`learning_progress`、`topic_note`（V2 迁移）。
- 注意：普通用户与 `/admin` 的 Bearer Token 是两套鉴权，互不影响。

## 管理端

管理页：http://localhost:5173/admin
- 写接口为 `/api/v1/admin/topics`（GET / POST / PUT / DELETE），需要 `Authorization: Bearer <token>`。
- 题目维护：`GET/PUT /api/v1/admin/topics/{slug}/quizzes`，表格里整组替换。
- 令牌默认 `dev-admin-token`，可用环境变量 `LEARNHUB_ADMIN_TOKEN` 覆盖；进入 `/admin` 时输入一次，保存在浏览器 localStorage。
- 「已发布」的学习项必须满足：概述非空、至少 1 条路线、至少 1 个示例、至少 3 条常见坑；否则接口返回 400。
- 删除是物理删除，不可恢复。

## 新增学习项

两种方式：

1. **管理页**：`/admin` → 新建学习项，保存后立即生效（无需重启）。
2. **改种子文件**：在 `content/topics/{slug}.json` 按 schema 编写，同步更新 `content/seed-version.txt` 的版本号，重启后端自动同步；不需要清库。

## 测试与构建

```powershell
cd backend;  mvn test
cd frontend; npm run build
```

- 集成测试使用独立的 `learnhub_test` 库（`spring.profiles.active=test`），首次运行自动创建并执行 Flyway，每个用例事务回滚，不影响开发库。
- 运行集成测试需要本机 MySQL 容器已启动；连不上会直接失败。
- 目前 39 个用例：公开 API 6、认证与用户隔离 12、管理端与题目 11、面试题库 6、答题记录 4。
