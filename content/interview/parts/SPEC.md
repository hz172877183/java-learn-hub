# 面试题分片生成规范

本目录下的 `*.json` 是分片草稿，最终会合并进 `content/interview/questions.json`。

## 文件格式

UTF-8 JSON 数组，无 BOM、无 Markdown 代码围栏、无注释、无尾逗号。

```json
[
  {
    "category": "java-basics",
    "level": "mid",
    "type": "choice",
    "stem": "题干",
    "options": ["选项 A", "选项 B", "选项 C", "选项 D"],
    "correctIndex": 2,
    "referenceAnswer": null,
    "explanation": "知识点说明"
  },
  {
    "category": "java-basics",
    "level": "senior",
    "type": "text",
    "stem": "题干",
    "options": [],
    "correctIndex": null,
    "referenceAnswer": "参考答案",
    "explanation": "知识点说明"
  }
]
```

## 硬性要求

- `category` 必须使用分配的分类 id，不得自创。
- `level` 只能是 `junior` / `mid` / `senior` / `architect`。
- `type` 只能是 `choice` / `text`。
- 选择题：4 个选项，`correctIndex` 为 0-3，`referenceAnswer` 为 null。
- 自答题：`options` 为 `[]`，`correctIndex` 为 null，`referenceAnswer` 必填。
- `explanation` 必填，讲清考点与易错点，40-120 字。
- 选择题四个选项长度接近、干扰项合理，禁止“以上都对/以上都不对”作为唯一正确项。
- 题干与答案使用中文；代码、类名、命令保留英文。
- 不得重复或近似重复题干；同一知识点只出一道。
- 不出现具体公司名、年份、真实人名。
- 答案必须技术正确；不确定的题不要写。

## 内容质量

- 像真实面试官会问的问题，而不是教材课后题。
- 自答题参考答案 80-200 字，分点作答，覆盖“是什么、为什么、怎么做”。
- 选择题解析说明正确项为什么对，必要时点出常见错误。
- 难度符合等级：junior 考概念与用法，mid 考原理与取舍，senior 考排查与设计，architect 考架构权衡与落地。
