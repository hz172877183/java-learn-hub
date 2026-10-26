# AGENTS.md

给 AI 编码助手（以及新加入的同学）的项目约定。**先读这里，再读 README.md。**

## 项目概览

Java / Spring 生态学习门户。后端提供内容 API + 管理端写接口，前端是 Vue 3 单页应用，含 `/admin` 管理页。

- 技术栈：Java 21 + Spring Boot 3.5 + MySQL 8 + Flyway + JPA；Vue 3 + Vite + TypeScript + Element Plus + Pinia
- 需求文档：`docs/requirements/PRD.md`；内容格式：`docs/content-schema.md`
- 种子内容：`content/topics/*.json`、`content/quizzes/*.json`、`content/interview/parts/*.json`

## 常用命令

本机默认 `java` 可能是 8，后端命令一律先设置 JDK 21：

```powershell
$env:JAVA_HOME = "D:\soft\IntelliJ IDEA 2024.3.6\jbr"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
```

```powershell
# 后端：编译 / 测试 / 启动（均在 backend 目录）
mvn -B -DskipTests compile
mvn -B test
mvn -B spring-boot:run

# 前端（frontend 目录）
npm install
npm run dev -- --host 0.0.0.0 --port 5173
npm run build          # vue-tsc 类型检查 + vite build，提交前必过

# 依赖：本地 MySQL 容器
docker start java-learn-hub-db
```

- 集成测试用独立库 `learnhub_test`（`spring.profiles.active=test`），需要本机 MySQL 容器在跑，每个用例事务回滚。
- 开发库：`learnhub` / `learnhub` / `learnhub123`，端口 3306。
- 日志、临时文件写到 `D:\tmp\opencode`，不要落在仓库里。

## 目录速查

```
backend/src/main/java/com/learn/hub/
  answer/       答题记录：判分、错题本、统计
  interview/    面试题库：公开列表 + /me 用户态列表与重置
  quiz/         随堂题
  topic/        学习项、路线、详情
  auth/         注册登录、JWT
  content/      种子加载与版本同步
backend/src/main/resources/db/migration/   Flyway，只新增不修改历史脚本
frontend/src/views/      页面（InterviewView、MyAnswersView 等）
frontend/src/api/        client.ts（请求函数）+ types.ts（接口类型）
frontend/src/labels.ts   等级 / 分类显示名
```

## 编码约定

- **注释与 Javadoc 用中文**，写“为什么”而不是复述代码；公开类、公开方法都要有。
- 后端 DTO 用 `record`，集中在 `*Dtos.java`；Controller 只做参数绑定，业务逻辑放 Service。
- 写操作加 `@Transactional`，只读查询加 `@Transactional(readOnly = true)`。
- 分页统一 `page` 从 0 起、`size` 1–100，越界抛 `IllegalArgumentException`。
- 参数校验失败返回 400，资源不存在返回 404，未登录返回 401，保持现有风格。
- 前端请求统一走 `src/api/client.ts`，类型定义在 `src/api/types.ts`，不要在页面里手写 `fetch`。
- 前端 UI 用 Element Plus + 现有 CSS 变量（`--accent`、`--line`、`--muted`），不要引入新 UI 库。
- 页面文案用中文，风格与现有页面一致（简洁、口语化）。

## 数据与迁移

- 表结构只通过 Flyway 新增脚本（`V{n}__xxx.sql`），**不要修改已执行的历史脚本**。
- 种子内容按 `content/seed-version.txt` 版本号同步：改内容后必须同步提升版本号，重启后端自动导入。
- 版本不变时数据库是唯一数据源，管理端编辑不会被覆盖；手工创建的面试题 `seed_key` 为 NULL，同步时保留。
- 内容改动后跑 `python scripts/build_interview_bank.py`（合并去重）和 `python scripts/validate_content.py`（校验）。

## 鉴权

- 普通用户：`/api/v1/me/**` 需要 `Authorization: Bearer <JWT>`，JWT 默认 7 天。
- 管理端：`/api/v1/admin/**` 用独立的管理 Token，默认 `dev-admin-token`（`LEARNHUB_ADMIN_TOKEN` 可覆盖）。
- 两套鉴权互不影响，不要混用。

## 工作方式（重要）

- **不主动 push 远端**；提交留在本地，除非用户明确要求推送。
- 提交信息用 Conventional Commits 风格（`feat:` / `fix:` / `docs:` / `refactor:`）。
- 开发期追求速度：
  - 小改动 → 只跑相关模块编译 + `npm run build`。
  - 显著改动（改接口契约、加迁移、动鉴权、准备提交）→ 跑全量 `mvn test`。
- 改接口时要同步三处：后端 DTO/Service、`frontend/src/api/types.ts`、`frontend/src/api/client.ts`。
- 改功能后同步更新 `README.md` 对应章节；测试用例数量变化时同步更新 README 的统计。
- 移动端可用性不能退化：前端以 `--host 0.0.0.0` 启动，手机访问 `http://192.168.0.207:5173`；CORS 已用 `allowedOriginPatterns` 支持局域网地址。
- 端口：后端 8080，前端 5173。重启前先确认没有残留进程占用。

## 常用本地地址

- 前端 http://localhost:5173 ，手机 http://192.168.0.207:5173
- API http://localhost:8080/api/v1/topics
- 健康检查 http://localhost:8080/actuator/health
- 管理页 http://localhost:5173/admin
