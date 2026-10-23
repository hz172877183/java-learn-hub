# 工程结构

> 由 `scripts/structure.js` 生成，最后更新：2026/10/23 10:41:00
> 刷新：`node scripts/structure.js`（结构变了就重跑，然后重新预览本文件）

```text
java-learn-hub/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/learn/hub/
│   │   │   │       ├── admin/
│   │   │   │       │   ├── AdminAuthFilter.java
│   │   │   │       │   ├── AdminTopicController.java
│   │   │   │       │   └── TopicAdminService.java
│   │   │   │       ├── api/
│   │   │   │       │   ├── ApiExceptionHandler.java
│   │   │   │       │   └── TopicController.java
│   │   │   │       ├── config/
│   │   │   │       │   └── WebConfig.java
│   │   │   │       ├── content/
│   │   │   │       │   ├── CatalogStore.java
│   │   │   │       │   ├── ContentCatalog.java
│   │   │   │       │   ├── ContentLoader.java
│   │   │   │       │   ├── ContentModels.java
│   │   │   │       │   └── ContentSeeder.java
│   │   │   │       ├── learning/
│   │   │   │       │   ├── LearningDtos.java
│   │   │   │       │   ├── LearningService.java
│   │   │   │       │   ├── MeController.java
│   │   │   │       │   ├── NoteEntity.java
│   │   │   │       │   ├── NoteRepository.java
│   │   │   │       │   ├── ProgressEntity.java
│   │   │   │       │   └── ProgressRepository.java
│   │   │   │       ├── persistence/
│   │   │   │       │   ├── TopicEntity.java
│   │   │   │       │   ├── TopicRepository.java
│   │   │   │       │   ├── TrackEntity.java
│   │   │   │       │   └── TrackRepository.java
│   │   │   │       ├── quiz/
│   │   │   │       │   ├── QuizController.java
│   │   │   │       │   ├── QuizQuestionEntity.java
│   │   │   │       │   ├── QuizQuestionRepository.java
│   │   │   │       │   └── QuizService.java
│   │   │   │       ├── topic/
│   │   │   │       │   ├── ConflictException.java
│   │   │   │       │   ├── NotFoundException.java
│   │   │   │       │   ├── TopicDtos.java
│   │   │   │       │   └── TopicService.java
│   │   │   │       ├── user/
│   │   │   │       │   ├── AuthController.java
│   │   │   │       │   ├── AuthDtos.java
│   │   │   │       │   ├── AuthService.java
│   │   │   │       │   ├── JwtService.java
│   │   │   │       │   ├── UnauthorizedException.java
│   │   │   │       │   ├── UserAuthFilter.java
│   │   │   │       │   ├── UserEntity.java
│   │   │   │       │   └── UserRepository.java
│   │   │   │       └── JavaLearnHubApplication.java
│   │   │   └── resources/
│   │   │       ├── db/
│   │   │       │   └── migration/
│   │   │       │       ├── V1__init.sql
│   │   │       │       ├── V2__users_progress_notes.sql
│   │   │       │       └── V3__quiz_questions.sql
│   │   │       └── application.yml
│   │   └── test/
│   │       ├── java/
│   │       │   └── com/learn/hub/
│   │       │       ├── topic/
│   │       │       │   └── TopicServiceTest.java
│   │       │       ├── AdminAndQuizTest.java
│   │       │       └── AuthAndPrivacyTest.java
│   │       └── resources/
│   │           └── application-test.yml
│   └── pom.xml
├── content/
│   ├── quizzes/
│   │   └── (16 个题库 JSON)
│   ├── topics/
│   │   └── (16 个学习项 JSON)
│   └── tracks/
│       └── java-backend-job-ready.json
├── docs/
│   ├── requirements/
│   │   └── PRD.md
│   ├── slides/
│   │   ├── generate.js
│   │   └── README.md
│   ├── content-schema.md
│   ├── java-learn-hub-介绍.pptx
│   └── PROJECT-STRUCTURE.md
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   │   ├── client.ts
│   │   │   └── types.ts
│   │   ├── components/
│   │   │   └── TopicCard.vue
│   │   ├── router/
│   │   │   └── index.ts
│   │   ├── stores/
│   │   │   └── auth.ts
│   │   ├── views/
│   │   │   ├── AboutView.vue
│   │   │   ├── AdminTopicEditView.vue
│   │   │   ├── AdminView.vue
│   │   │   ├── CatalogView.vue
│   │   │   ├── HomeView.vue
│   │   │   ├── LoginView.vue
│   │   │   ├── MyLearningView.vue
│   │   │   ├── NotFoundView.vue
│   │   │   ├── RegisterView.vue
│   │   │   ├── TopicDetailView.vue
│   │   │   └── TrackView.vue
│   │   ├── App.vue
│   │   ├── env.d.ts
│   │   ├── labels.ts
│   │   ├── main.ts
│   │   └── styles.css
│   ├── index.html
│   ├── package-lock.json
│   ├── package.json
│   ├── tsconfig.app.json
│   ├── tsconfig.json
│   ├── tsconfig.node.json
│   └── vite.config.ts
├── scripts/
│   └── structure.js
├── .gitignore
└── README.md
```

## 模块职责

| 路径 | 职责 |
| --- | --- |
| `backend/src/main/java/com/learn/hub/api` | 公开只读 API、统一异常处理 |
| `backend/src/main/java/com/learn/hub/admin` | 管理端 CRUD、Bearer Token 过滤器 |
| `backend/src/main/java/com/learn/hub/user` | 注册登录、JWT、密码哈希 |
| `backend/src/main/java/com/learn/hub/learning` | 学习进度、私人笔记 |
| `backend/src/main/java/com/learn/hub/quiz` | 面试题读取与整组替换 |
| `backend/src/main/java/com/learn/hub/content` | 内容模型、种子导入、内存快照 |
| `backend/src/main/java/com/learn/hub/persistence` | topic / track 的 JPA 实体 |
| `backend/src/main/resources/db/migration` | Flyway 迁移（V1 内容、V2 用户、V3 题目） |
| `frontend/src/views` | 页面：首页、目录、详情、路线、关于、我的学习、管理端 |
| `frontend/src/api` | HTTP 客户端与类型定义 |
| `frontend/src/stores` | Pinia 状态（登录态） |
| `content/topics` | 16 个学习项（仅首次导入用） |
| `content/quizzes` | 每个学习项的面试题（仅首次导入用） |
| `docs` | PRD、内容规范、工程结构、PPT |
| `scripts` | 开发辅助脚本 |

## 关键文件

| 文件 | 说明 |
| --- | --- |
| `backend/src/main/resources/application.yml` | 数据源、JWT、管理令牌、CORS |
| `backend/src/main/resources/db/migration/*.sql` | 表结构变更，只增不改 |
| `backend/src/test/java/com/learn/hub/*Test.java` | 集成测试（认证、隔离、管理端、题目） |
| `frontend/src/router/index.ts` | 全部路由 |
| `frontend/src/api/client.ts` | 所有接口调用 |
| `frontend/src/views/TopicDetailView.vue` | 详情页：场景、题目、笔记、进度 |
| `frontend/src/views/AdminTopicEditView.vue` | 管理端编辑：场景与题目 |

## 常用命令

```powershell
# 数据库（Docker）
docker start java-learn-hub-db

# 后端（需要 JDK 21）
$env:JAVA_HOME = "D:\soft\IntelliJ IDEA 2024.3.6\jbr"
cd backend; mvn spring-boot:run

# 前端
cd frontend; npm run dev

# 测试与构建
cd backend; mvn test
cd frontend; npm run build

# 刷新本文档
node scripts/structure.js
```
