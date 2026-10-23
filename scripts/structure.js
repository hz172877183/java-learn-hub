/**
 * 生成 docs/PROJECT-STRUCTURE.md：带注释的工程结构快照。
 * 用法：node scripts/structure.js
 */
const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const skipDirs = new Set([
  "node_modules",
  "target",
  "dist",
  ".git",
  ".idea",
  ".vscode",
  "logs",
  "snapshots",
  "tool-output",
]);
const skipFiles = /\.(log|tsbuildinfo)$/i;
// 长目录折叠成一行，避免文档被 16 个 JSON 淹没
const collapse = new Map([
  ["content/topics", "16 个学习项 JSON"],
  ["content/quizzes", "16 个题库 JSON"],
]);
// Java 包路径太长，合并成一行
const combine = new Map([
  ["backend/src/main/java/com", "com/learn/hub"],
  ["backend/src/test/java/com", "com/learn/hub"],
]);

function walk(dir, prefix = "", rel = "") {
  const entries = fs
    .readdirSync(dir, { withFileTypes: true })
    .filter((e) =>
      e.isDirectory() ? !skipDirs.has(e.name) : !skipFiles.test(e.name),
    )
    .sort((a, b) =>
      a.isDirectory() === b.isDirectory()
        ? a.name.localeCompare(b.name)
        : a.isDirectory()
          ? -1
          : 1,
    );

  let out = "";
  entries.forEach((entry, index) => {
    const last = index === entries.length - 1;
    const childRel = rel ? `${rel}/${entry.name}` : entry.name;

    const combined = combine.get(childRel);
    if (entry.isDirectory() && combined) {
      out += `${prefix}${last ? "└── " : "├── "}${combined}/\n`;
      out += walk(
        path.join(dir, ...combined.split("/")),
        prefix + (last ? "    " : "│   "),
        `${childRel}/${combined.split("/").slice(1).join("/")}`,
      );
      return;
    }

    out += `${prefix}${last ? "└── " : "├── "}${entry.name}${entry.isDirectory() ? "/" : ""}\n`;
    if (!entry.isDirectory()) return;

    const label = collapse.get(childRel);
    if (label) {
      out += `${prefix}${last ? "    " : "│   "}└── (${label})\n`;
      return;
    }
    out += walk(path.join(dir, entry.name), prefix + (last ? "    " : "│   "), childRel);
  });
  return out;
}

const tree = walk(root);
const now = new Date().toLocaleString("zh-CN", { hour12: false });

const doc = `# 工程结构

> 由 \`scripts/structure.js\` 生成，最后更新：${now}
> 刷新：\`node scripts/structure.js\`（结构变了就重跑，然后重新预览本文件）

\`\`\`text
${path.basename(root)}/
${tree}\`\`\`

## 模块职责

| 路径 | 职责 |
| --- | --- |
| \`backend/src/main/java/com/learn/hub/api\` | 公开只读 API、统一异常处理 |
| \`backend/src/main/java/com/learn/hub/admin\` | 管理端 CRUD、Bearer Token 过滤器 |
| \`backend/src/main/java/com/learn/hub/user\` | 注册登录、JWT、密码哈希 |
| \`backend/src/main/java/com/learn/hub/learning\` | 学习进度、私人笔记 |
| \`backend/src/main/java/com/learn/hub/quiz\` | 面试题读取与整组替换 |
| \`backend/src/main/java/com/learn/hub/content\` | 内容模型、种子导入、内存快照 |
| \`backend/src/main/java/com/learn/hub/persistence\` | topic / track 的 JPA 实体 |
| \`backend/src/main/resources/db/migration\` | Flyway 迁移（V1 内容、V2 用户、V3 题目） |
| \`frontend/src/views\` | 页面：首页、目录、详情、路线、关于、我的学习、管理端 |
| \`frontend/src/api\` | HTTP 客户端与类型定义 |
| \`frontend/src/stores\` | Pinia 状态（登录态） |
| \`content/topics\` | 16 个学习项（仅首次导入用） |
| \`content/quizzes\` | 每个学习项的面试题（仅首次导入用） |
| \`docs\` | PRD、内容规范、工程结构、PPT |
| \`scripts\` | 开发辅助脚本 |

## 关键文件

| 文件 | 说明 |
| --- | --- |
| \`backend/src/main/resources/application.yml\` | 数据源、JWT、管理令牌、CORS |
| \`backend/src/main/resources/db/migration/*.sql\` | 表结构变更，只增不改 |
| \`backend/src/test/java/com/learn/hub/*Test.java\` | 集成测试（认证、隔离、管理端、题目） |
| \`frontend/src/router/index.ts\` | 全部路由 |
| \`frontend/src/api/client.ts\` | 所有接口调用 |
| \`frontend/src/views/TopicDetailView.vue\` | 详情页：场景、题目、笔记、进度 |
| \`frontend/src/views/AdminTopicEditView.vue\` | 管理端编辑：场景与题目 |

## 常用命令

\`\`\`powershell
# 数据库（Docker）
docker start java-learn-hub-db

# 后端（需要 JDK 21）
$env:JAVA_HOME = "D:\\soft\\IntelliJ IDEA 2024.3.6\\jbr"
cd backend; mvn spring-boot:run

# 前端
cd frontend; npm run dev

# 测试与构建
cd backend; mvn test
cd frontend; npm run build

# 刷新本文档
node scripts/structure.js
\`\`\`
`;

const outFile = path.join(root, "docs", "PROJECT-STRUCTURE.md");
fs.writeFileSync(outFile, doc, "utf8");
console.log(`written: ${outFile}`);
