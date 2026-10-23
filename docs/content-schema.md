# 学习内容 Schema

内容源文件在仓库根目录 `content/`，构建时复制进 Spring Boot classpath `content/`。

## 内容版本与同步

`content/seed-version.txt` 保存当前内容版本。应用启动时比较该文件与数据库 `content_seed_state` 中的版本：

- 版本一致：不动数据库，管理端的日常编辑保持不变。
- 版本变化：按 slug 增量同步学习项与路线，整组替换有种子文件的随堂题，并按 `seed_key` 重建种子面试题；管理端手工创建、且不在种子中的面试题会保留。

新增或修改 `content/` 下的内容后，记得同步修改 `seed-version.txt`（例如 `2026-10-24.2`）。

## 学习项 `content/topics/{slug}.json`

与 PRD 第 5.1 节字段一致。`published` 必须同时满足：

- `overviewMd` 非空
- `pathSteps` 至少 1 步
- `samples` 至少 1 个
- `pitfalls` 至少 3 条

`status` 为 `draft` 的项不会出现在公开 API。

分类枚举：`spring-core` / `data` / `cache` / `messaging` / `security` / `engineering` / `frontend` / `cloud` / `search` / `observability` / `ai`

难度：`beginner` / `intermediate` / `practical`

接入状态：`docs_only` / `snippet` / `runtime`

内容质量基线（审核时参考）：概念 6 条以上、路线 5 步以上、示例 2 段以上、常见坑 5 条以上、典型场景 4 条以上。

## 路线 `content/tracks/{slug}.json`

- `slug`、`title`、`audience`
- `stages[]`：`title`、`goal`、`topicSlugs[]`

## 随堂题 `content/quizzes/{topicSlug}.json`

数组，元素为 `QuizQuestion`：

```json
{
  "type": "choice",
  "stem": "题干",
  "options": ["A", "B", "C", "D"],
  "correctIndex": 1,
  "referenceAnswer": null,
  "explanation": "知识点说明"
}
```

- `type`：`choice` / `text`
- 选择题：2-6 个选项，`correctIndex` 从 0 开始；`referenceAnswer` 可省略。
- 自答题：`options` 为空数组，`referenceAnswer` 必填。

## 面试题库 `content/interview/questions.json`

数组，元素为 `InterviewQuestion`：

```json
{
  "category": "java-basics",
  "level": "mid",
  "type": "choice",
  "stem": "题干",
  "options": ["A", "B", "C", "D"],
  "correctIndex": 2,
  "referenceAnswer": null,
  "explanation": "知识点说明"
}
```

- `category`：字符串，最长 32 字；当前前台标签见 `frontend/src/labels.ts`。
- `level`：`junior` / `mid` / `senior` / `architect`。
- `type`：`choice` / `text`；自答题必须提供 `referenceAnswer`。
- `explanation` 必填，所有题目都要有解析。
- 种子题在数据库中带 `seed_key`，用于版本同步时识别；管理端新建的题 `seed_key` 为空。

题库分片编辑流程：

```powershell
# 1. 编辑 content/interview/parts/*.json（一个分类一个文件）
# 2. 合并、去重并校验
python scripts/build_interview_bank.py
python scripts/validate_content.py
```
