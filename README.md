# java-learn-hub

面向 Java / Spring 生态的学习门户。仓库路径：`D:\worksapce\java-learn-hub`。

- 需求：[docs/requirements/PRD.md](docs/requirements/PRD.md)
- 内容格式：[docs/content-schema.md](docs/content-schema.md)
- 学习项源文件（种子数据）：`content/topics/*.json`

## 技术栈

- Java 21 + Spring Boot 3.5（只读 API + 管理端写接口）
- MySQL 8 + Flyway + Spring Data JPA（内容落库，JSON 仅作首次导入种子）
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
- 首次启动时，如果 `topic` 表为空，会把 `content/topics/*.json` 和 `content/tracks/*.json` 自动导入作为种子数据；之后数据库是唯一数据源，改 `content/` 不会再生效。

## 启动

本机默认 `java` 若是 8，请先指定 JDK 21。若安装了 IntelliJ，可用其自带 JBR：

```powershell
$env:JAVA_HOME = "D:\soft\IntelliJ IDEA 2024.3.6\jbr"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
java -version
```

后端：

```powershell
cd D:\worksapce\java-learn-hub\backend
mvn spring-boot:run
```

前端（另开终端）：

```powershell
cd D:\worksapce\java-learn-hub\frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173 。API：http://localhost:8080/api/v1/topics

## 普通用户

- 注册 / 登录：`/register`、`/login`，接口为 `POST /api/v1/auth/register|login`，成功后返回 JWT。
- 权限：`/api/v1/me/**` 需要 `Authorization: Bearer <JWT>`；JWT 默认有效期 7 天（`LEARNHUB_JWT_TTL_HOURS`）。
- 学习进度：`/track` 页每个学习项可选「未开始 / 学习中 / 已完成」，并显示阶段与整体进度；`/me` 汇总。
- 私人笔记：每个学习项一条 Markdown 笔记，在详情页编辑保存，仅本人可见（按 user_id 隔离）。
- 相关表：`app_user`、`learning_progress`、`topic_note`（V2 迁移）。
- 注意：普通用户与 `/admin` 的 Bearer Token 是两套鉴权，互不影响。

## 管理端

管理页：http://localhost:5173/admin
- 写接口为 `/api/v1/admin/topics`（GET / POST / PUT / DELETE），需要 `Authorization: Bearer <token>`。
- 令牌默认 `dev-admin-token`，可用环境变量 `LEARNHUB_ADMIN_TOKEN` 覆盖；进入 `/admin` 时输入一次，保存在浏览器 localStorage。
- 「已发布」的学习项必须满足：概述非空、至少 1 条路线、至少 1 个示例、至少 3 条常见坑；否则接口返回 400。
- 删除是物理删除，不可恢复。

## 新增学习项

两种方式：

1. **管理页**：`/admin` → 新建学习项，保存后立即生效（无需重启）。
2. **改种子 + 重置库**：在 `content/topics/{slug}.json` 按 schema 编写，清空数据库后重启后端重新导入。

## 测试与构建

```powershell
cd backend;  mvn test
cd frontend; npm run build
```
