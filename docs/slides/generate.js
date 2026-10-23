const pptxgen = require("pptxgenjs");

const INK = "1C2430";
const PAPER = "FBF7EE";
const WHITE = "FFFDF8";
const ACCENT = "0F6E6E";
const ACCENT2 = "D7ECEC";
const MUTED = "5C6B7A";
const LINE = "D9CFC0";
const WARN = "B45F35";

const FONT = "Microsoft YaHei";
const SERIF = "Georgia";

const pptx = new pptxgen();
pptx.layout = "LAYOUT_16x9";
pptx.author = "java-learn-hub";
pptx.title = "java-learn-hub 项目介绍";
pptx.subject = "Java 学习门户";

let page = 1;

function rect(slide, x, y, w, h, fill, line = fill, radius = 0.06) {
  slide.addShape(pptx.ShapeType.roundRect, {
    x, y, w, h,
    fill: { color: fill },
    line: { color: line, width: 1 },
    rectRadius: radius,
  });
}

function contentSlide(kicker, title, subtitle) {
  const s = pptx.addSlide();
  s.background = { color: PAPER };
  s.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: 0.14, h: 5.625,
    fill: { color: ACCENT }, line: { color: ACCENT },
  });
  s.addText(kicker, {
    x: 0.58, y: 0.34, w: 8.7, h: 0.28,
    fontSize: 10.5, bold: true, color: ACCENT, charSpacing: 2, fontFace: FONT,
  });
  s.addText(title, {
    x: 0.58, y: 0.62, w: 8.7, h: 0.62,
    fontSize: 27, bold: true, color: INK, fontFace: FONT,
  });
  if (subtitle) {
    s.addText(subtitle, {
      x: 0.58, y: 1.24, w: 8.8, h: 0.34,
      fontSize: 11.5, color: MUTED, fontFace: FONT,
    });
  }
  s.addText("java-learn-hub  ·  项目介绍", {
    x: 0.58, y: 5.15, w: 5.5, h: 0.24,
    fontSize: 9, color: MUTED, fontFace: FONT,
  });
  s.addText(String(page++), {
    x: 9.0, y: 5.15, w: 0.45, h: 0.24,
    fontSize: 9, color: MUTED, align: "right", fontFace: FONT,
  });
  return s;
}

function card(slide, x, y, w, h, title, body, opts = {}) {
  rect(slide, x, y, w, h, opts.fill || WHITE, opts.line || LINE);
  if (opts.tag) {
    slide.addText(opts.tag, {
      x: x + 0.18, y: y + 0.14, w: w - 0.36, h: 0.22,
      fontSize: 9, bold: true, color: ACCENT, charSpacing: 1, fontFace: FONT,
    });
  }
  slide.addText(title, {
    x: x + 0.18, y: y + (opts.tag ? 0.38 : 0.2), w: w - 0.36, h: 0.32,
    fontSize: 14.5, bold: true, color: opts.titleColor || INK, fontFace: FONT,
  });
  slide.addText(body, {
    x: x + 0.18, y: y + (opts.tag ? 0.78 : 0.58), w: w - 0.36, h: h - (opts.tag ? 0.92 : 0.72),
    fontSize: 10.5, color: opts.bodyColor || MUTED, fontFace: FONT,
    lineSpacingMultiple: 1.15, valign: "top",
  });
}

function bullets(slide, x, y, w, h, items, size = 12) {
  slide.addText(
    items.map((t) => ({ text: t, options: { bullet: { indent: 12 }, breakLine: true } })),
    { x, y, w, h, fontSize: size, color: INK, fontFace: FONT, lineSpacingMultiple: 1.3, valign: "top" }
  );
}

/* ---------- 1. 封面 ---------- */
{
  const s = pptx.addSlide();
  s.background = { color: INK };
  s.addShape(pptx.ShapeType.rect, {
    x: 0, y: 0, w: 10, h: 0.12, fill: { color: ACCENT }, line: { color: ACCENT },
  });
  s.addShape(pptx.ShapeType.arc, {
    x: 7.2, y: -1.4, w: 4.4, h: 4.4,
    fill: { color: INK }, line: { color: ACCENT, width: 1.5 },
  });
  s.addText("JAVA LEARN HUB", {
    x: 0.8, y: 1.25, w: 8, h: 0.3,
    fontSize: 12, color: ACCENT2, charSpacing: 3, bold: true, fontFace: FONT,
  });
  s.addText("java-learn-hub", {
    x: 0.8, y: 1.55, w: 8.6, h: 1.0,
    fontSize: 46, bold: true, color: "FFFFFF", fontFace: SERIF,
  });
  s.addText("把 Java 技术栈放进同一张地图", {
    x: 0.8, y: 2.62, w: 8, h: 0.5,
    fontSize: 20, color: ACCENT2, fontFace: FONT,
  });
  s.addText("从“收藏了一堆链接”到“知道下一步学什么”", {
    x: 0.8, y: 3.12, w: 8, h: 0.4,
    fontSize: 12.5, color: "9FB2BC", fontFace: FONT,
  });
  const tags = ["Spring Boot 3.5", "Vue 3 + Vite", "MySQL 8 + Flyway"];
  tags.forEach((t, i) => {
    s.addShape(pptx.ShapeType.roundRect, {
      x: 0.8 + i * 2.28, y: 3.95, w: 2.1, h: 0.42,
      fill: { color: "24303D" }, line: { color: "3C4C5A", width: 1 }, rectRadius: 0.2,
    });
    s.addText(t, {
      x: 0.8 + i * 2.28, y: 3.95, w: 2.1, h: 0.42,
      fontSize: 10.5, color: ACCENT2, align: "center", fontFace: FONT,
    });
  });
  s.addText("项目介绍  ·  2026-10", {
    x: 0.8, y: 4.9, w: 8, h: 0.3,
    fontSize: 10, color: "7E909C", fontFace: FONT,
  });
  page++;
}

/* ---------- 2. 项目定位 ---------- */
{
  const s = contentSlide("POSITIONING", "它解决什么问题？", "不是又一个网校，而是一张可执行的学习地图");
  card(s, 0.58, 1.78, 3.9, 3.05, "常见的三种困境", "", { tag: "PROBLEM" });
  bullets(s, 0.76, 2.62, 3.5, 2.0, [
    "知识散落在博客、视频、收藏夹，找不到主线",
    "不知道先学什么、学到哪算学会",
    "缺一条面向求职的、可落地的后端路线",
  ], 11);
  card(s, 4.75, 1.78, 4.67, 3.05, "java-learn-hub 的答案", "", { tag: "SOLUTION" });
  bullets(s, 4.93, 2.62, 4.3, 2.0, [
    "目录即地图：16 个学习项，按分类 / 难度 / 接入状态筛选",
    "路线即计划：7 个阶段，从站稳 Web 到工程交付",
    "每个学习项都有概述、路线、示例、常见坑",
    "进度 + 私人笔记，形成学习闭环",
  ], 11);
}

/* ---------- 3. 核心能力总览 ---------- */
{
  const s = contentSlide("OVERVIEW", "三条主线，一个闭环", "浏览 → 学习 → 维护，各自独立又互相支撑");
  const cols = [
    { tag: "BROWSE", title: "浏览与发现", body: "首页 / 目录 / 详情 / 总路线 / 关于\n筛选、搜索、代码复制\n只展示已发布内容" },
    { tag: "LEARN", title: "个人学习", body: "注册登录（JWT）\n三态进度：未开始 / 学习中 / 已完成\n每题一条私人 Markdown 笔记" },
    { tag: "MANAGE", title: "内容维护", body: "管理页增删改学习项\n草稿 / 发布状态切换\n发布前完整性校验" },
  ];
  cols.forEach((c, i) => {
    const x = 0.58 + i * 3.16;
    card(s, x, 1.8, 2.94, 2.9, c.title, c.body, { tag: c.tag });
  });
  s.addText("数据库是唯一数据源  ·  写操作实时刷新内容快照  ·  读接口保持只读", {
    x: 0.58, y: 4.92, w: 8.9, h: 0.3,
    fontSize: 10.5, color: ACCENT, align: "center", fontFace: FONT,
  });
}

/* ---------- 4. 技术栈 ---------- */
{
  const s = contentSlide("STACK", "技术栈：选择克制，边界清晰", "一期不引入 Redis / MQ / 全文检索，够用即可");
  const groups = [
    { title: "后端", lines: "Java 21\nSpring Boot 3.5.6\nSpring Data JPA\nFlyway" },
    { title: "前端", lines: "Vue 3.5 + TypeScript\nVite 6\nElement Plus\nVue Router + Pinia" },
    { title: "数据", lines: "MySQL 8.0\nFlyway 版本化迁移\nJSON 文档 + 索引列混合建模" },
    { title: "安全", lines: "BCrypt 密码哈希\nJWT（用户，7 天）\nBearer Token（管理）" },
  ];
  groups.forEach((g, i) => {
    card(s, 0.58 + i * 2.33, 1.85, 2.12, 2.55, g.title, g.lines, { tag: `0${i + 1}` });
  });
  s.addText("运行环境：JBR 21  ·  Maven 3.8  ·  Node 22  ·  Docker Desktop", {
    x: 0.58, y: 4.7, w: 8.9, h: 0.3,
    fontSize: 10.5, color: MUTED, fontFace: FONT,
  });
}

/* ---------- 5. 架构 ---------- */
{
  const s = contentSlide("ARCHITECTURE", "前后端分离，数据库兜底", "浏览器只认 HTTP，服务端只认内容快照");
  const boxes = [
    { x: 0.5, title: "浏览器", body: "Vue 3 SPA\n路由 / 状态 / 编辑器" },
    { x: 3.55, title: "Spring Boot", body: "只读 API + 管理 API\nJWT / Token 过滤器" },
    { x: 6.6, title: "MySQL 8", body: "topic / track\nuser / progress / note" },
  ];
  boxes.forEach((b, i) => {
    card(s, b.x, 1.95, 2.55, 1.5, b.title, b.body);
    if (i < 2) {
      s.addText("→", {
        x: b.x + 2.58, y: 2.45, w: 0.44, h: 0.45,
        fontSize: 22, color: ACCENT, align: "center", fontFace: FONT,
      });
    }
  });
  rect(s, 0.58, 3.75, 8.85, 1.05, ACCENT2, ACCENT2, 0.08);
  s.addText("请求链路", {
    x: 0.78, y: 3.88, w: 1.6, h: 0.26, fontSize: 10, bold: true, color: ACCENT, fontFace: FONT,
  });
  s.addText(
    "/api/v1/**  公开只读      |      /api/v1/me/**  JWT      |      /api/v1/admin/**  管理令牌",
    { x: 0.78, y: 4.15, w: 8.5, h: 0.4, fontSize: 11, color: INK, fontFace: FONT }
  );
  s.addText("写库后重建内存快照，读接口始终服务最新内容", {
    x: 0.58, y: 4.95, w: 8.9, h: 0.28, fontSize: 10, color: MUTED, fontFace: FONT,
  });
}

/* ---------- 6. 数据模型 ---------- */
{
  const s = contentSlide("DATA MODEL", "五张业务表，覆盖内容与学习行为", "Flyway V1 + V2 管理结构演进");
  const tables = [
    { x: 0.58, y: 1.74, w: 2.9, h: 1.45, title: "topic", body: "slug / title / category\ndifficulty / status\npayload(JSON) + 索引列" },
    { x: 3.63, y: 1.74, w: 2.9, h: 1.45, title: "track", body: "slug / title / audience\npayload(JSON)\n阶段与学习项引用" },
    { x: 6.68, y: 1.74, w: 2.75, h: 1.45, title: "app_user", body: "username(唯一)\npassword_hash(BCrypt)\ndisplay_name" },
    { x: 1.9, y: 3.45, w: 2.9, h: 1.15, title: "learning_progress", body: "user_id + topic_slug(唯一)\nstate / started_at / completed_at" },
    { x: 5.2, y: 3.45, w: 2.9, h: 1.15, title: "topic_note", body: "user_id + topic_slug(唯一)\ncontent(Markdown)" },
  ];
  tables.forEach((t) => card(s, t.x, t.y, t.w, t.h, t.title, t.body));
  s.addText("user_id + topic_slug 唯一约束：进度与笔记天然按用户隔离", {
    x: 0.58, y: 4.75, w: 8.9, h: 0.3, fontSize: 10.5, color: ACCENT, fontFace: FONT, align: "center",
  });
}

/* ---------- 7. 内容体系 ---------- */
{
  const s = contentSlide("CONTENT", "内容体系：一份 JSON，一套验收标准", "仓库即教材，数据库即事实");
  card(s, 0.58, 1.8, 4.35, 2.9, "16 个学习项", "Spring 核心 / 数据访问 / 工程化 / 中间件\n可观测 / 安全 / 云原生 / 前端\n\n每个学习项包含：\n概述 · 核心概念 · 学习路线\n示例代码 · 常见坑 · 仓库指引", { tag: "TOPICS" });
  card(s, 5.15, 1.8, 4.27, 2.9, "发布门槛", "status = published 时必须满足：\n\n· 概述非空\n· 至少 1 条学习路线\n· 至少 1 个示例\n· 至少 3 条常见坑\n\n不满足 → 接口 400，不允许入库", { tag: "QUALITY" });
  s.addText("1 条主线路线：Java 后端求职向，7 个阶段、8～12 周", {
    x: 0.58, y: 4.85, w: 8.9, h: 0.3, fontSize: 11, color: ACCENT, fontFace: FONT, align: "center",
  });
}

/* ---------- 8. 浏览端 ---------- */
{
  const s = contentSlide("BROWSE", "浏览端：不登录也能完整学习", "所有页面只消费已发布内容");
  bullets(s, 0.7, 1.85, 4.1, 2.6, [
    "首页：定位说明 + 推荐路线 + 最新学习项",
    "目录：分类 / 难度 / 接入状态 / 关键词组合筛选",
    "详情：概述、概念、路线、示例、常见坑、关联学习项",
    "总路线：阶段目标 + 每阶段学习项",
    "关于：项目边界与内容规范",
  ], 11.5);
  card(s, 5.0, 1.85, 4.42, 2.75, "设计取舍", "· 内容以 Markdown / JSON 表达，前端不写死路由\n· 新增学习项不需要改前端\n· 未知 slug 返回 404，未知接口同样 404\n· 统一错误体：code / message / traceId", { tag: "DECISIONS" });
}

/* ---------- 9. 个人学习 ---------- */
{
  const s = contentSlide("PERSONAL", "个人学习：进度与笔记，只属于你", "注册即用，无需邮箱验证");
  card(s, 0.58, 1.8, 2.9, 2.85, "账号", "用户名 + 密码注册\nBCrypt 哈希存储\nJWT 7 天有效期", { tag: "ACCOUNT" });
  card(s, 3.63, 1.8, 2.9, 2.85, "进度", "未开始 / 学习中 / 已完成\n记录开始与完成时间\n路线页显示阶段与整体百分比", { tag: "PROGRESS" });
  card(s, 6.68, 1.8, 2.75, 2.85, "笔记", "每题一条 Markdown 笔记\n手动保存，显示保存时间\n服务端按 user_id 隔离", { tag: "NOTES" });
  s.addText("「我的学习」汇总：完成数、学习中、笔记数、全站进度", {
    x: 0.58, y: 4.85, w: 8.9, h: 0.3, fontSize: 10.5, color: ACCENT, fontFace: FONT, align: "center",
  });
}

/* ---------- 10. 管理端 ---------- */
{
  const s = contentSlide("ADMIN", "管理端：把内容维护从仓库搬到页面", "写接口与读接口分离，令牌保护");
  bullets(s, 0.7, 1.85, 4.0, 2.6, [
    "列表：包含草稿，按 slug / 标题 / 分类搜索",
    "新建：slug 唯一，创建后不可修改",
    "编辑：基本信息 + 概念 / 路线 / 示例 / 坑 / 仓库指引",
    "删除：二次确认，物理删除",
  ], 11.5);
  card(s, 4.95, 1.85, 4.47, 2.7, "写接口保护", "Authorization: Bearer <管理令牌>\n· 无令牌 / 错令牌 → 401\n· 重复 slug → 409\n· 发布内容不完整 → 400\n· 未知学习项 → 404", { tag: "SECURITY" });
}

/* ---------- 11. 接口一览 ---------- */
{
  const s = contentSlide("API", "接口一览", "统一前缀 /api/v1，统一错误体");
  const rows = [
    ["方法", "路径", "说明", "鉴权"],
    ["GET", "/topics?category=&q=&page=", "分页列表（仅 published）", "公开"],
    ["GET", "/topics/{slug}", "学习项详情", "公开"],
    ["GET", "/tracks/{slug}", "路线与阶段", "公开"],
    ["GET", "/meta/categories", "分类统计", "公开"],
    ["POST", "/auth/register · /auth/login", "注册 / 登录", "公开"],
    ["GET/PUT", "/me/progress · /me/notes", "个人进度与笔记", "JWT"],
    ["CRUD", "/admin/topics", "学习项维护", "管理令牌"],
  ];
  s.addTable(rows, {
    x: 0.58, y: 1.72, w: 8.85, colW: [1.0, 3.45, 2.85, 1.55],
    fontSize: 10, fontFace: FONT, color: INK,
    border: { type: "solid", color: LINE, pt: 1 },
    fill: { color: WHITE },
    align: "left", valign: "middle",
    rowH: 0.36,
  });
  s.addText("错误码：400 参数 / 401 未认证 / 404 不存在 / 405 方法 / 409 冲突 / 415 媒体类型", {
    x: 0.58, y: 4.75, w: 8.9, h: 0.3, fontSize: 9.5, color: MUTED, fontFace: FONT,
  });
}

/* ---------- 12. 质量 ---------- */
{
  const s = contentSlide("QUALITY", "验证：可运行、可回归、可解释", "每次改动都跑测试与构建");
  const stats = [
    { n: "5/5", t: "后端单测通过" },
    { n: "16", t: "已发布学习项" },
    { n: "7", t: "路线阶段" },
    { n: "0", t: "前端构建错误" },
  ];
  stats.forEach((st, i) => {
    rect(s, 0.58 + i * 2.24, 1.78, 2.05, 1.05, WHITE, LINE);
    s.addText(st.n, { x: 0.58 + i * 2.24, y: 1.86, w: 2.05, h: 0.55, fontSize: 24, bold: true, color: ACCENT, align: "center", fontFace: SERIF });
    s.addText(st.t, { x: 0.58 + i * 2.24, y: 2.38, w: 2.05, h: 0.3, fontSize: 9.5, color: MUTED, align: "center", fontFace: FONT });
  });
  card(s, 0.58, 3.02, 4.35, 1.82, "已覆盖", "注册 / 登录 / 越权隔离\n进度三态与时间戳\n笔记增删改查\n管理端 CRUD 与发布校验", { tag: "TESTS" });
  card(s, 5.15, 3.02, 4.27, 1.82, "经验教训（真实踩坑）", "双构造器导致注入失败\nYAML 列表无法被 @Value 解析\nCORS 只放开 GET 导致写请求 403", { tag: "LESSONS", titleColor: WARN });
}

/* ---------- 13. 部署运行 ---------- */
{
  const s = contentSlide("RUN", "三步启动", "本机开发环境即可完整跑通");
  const steps = [
    { n: "01", t: "起数据库", d: "docker run mysql:8.0\n库 learnhub，端口 3306\n数据卷持久化" },
    { n: "02", t: "起后端", d: "mvn spring-boot:run\nJDK 21（可用 IDEA JBR）\nFlyway 自动建表 + 种子导入" },
    { n: "03", t: "起前端", d: "npm install && npm run dev\nhttp://127.0.0.1:5173\n代理 /api 到 8080" },
  ];
  steps.forEach((st, i) => {
    const x = 0.58 + i * 3.12;
    rect(s, x, 1.82, 2.9, 2.55, WHITE, LINE);
    s.addText(st.n, { x: x + 0.18, y: 1.94, w: 1.0, h: 0.4, fontSize: 18, bold: true, color: ACCENT2, fontFace: SERIF });
    s.addText(st.t, { x: x + 0.18, y: 2.34, w: 2.5, h: 0.32, fontSize: 14, bold: true, color: INK, fontFace: FONT });
    s.addText(st.d, { x: x + 0.18, y: 2.72, w: 2.55, h: 1.4, fontSize: 10, color: MUTED, fontFace: FONT, lineSpacingMultiple: 1.2, valign: "top" });
  });
  s.addText("配置：DB_USER / DB_PASSWORD  ·  LEARNHUB_JWT_SECRET  ·  LEARNHUB_ADMIN_TOKEN", {
    x: 0.58, y: 4.62, w: 8.9, h: 0.3, fontSize: 9.5, color: MUTED, fontFace: FONT,
  });
}

/* ---------- 14. 演进 ---------- */
{
  const s = contentSlide("ROADMAP", "下一步往哪走", "先把闭环打磨顺，再谈扩展");
  bullets(s, 0.7, 1.85, 4.2, 2.7, [
    "笔记 Markdown 预览与导出",
    "路线维度统计：学习时长、完成率趋势",
    "管理端统一为管理员账号 + 角色",
    "找回密码、登录限流、审计日志",
  ], 11.5);
  card(s, 5.05, 1.85, 4.37, 2.7, "暂不引入", "Redis / MQ / 全文检索\n多租户与团队协作\n移动端原生应用\n\n理由：一期优先验证\n「内容 → 学习 → 反馈」的闭环", { tag: "NON-GOALS" });
}

/* ---------- 15. 结束 ---------- */
{
  const s = pptx.addSlide();
  s.background = { color: INK };
  s.addShape(pptx.ShapeType.rect, { x: 0, y: 5.5, w: 10, h: 0.12, fill: { color: ACCENT }, line: { color: ACCENT } });
  s.addText("现在就可以开始", {
    x: 0.8, y: 1.55, w: 8, h: 0.5, fontSize: 15, color: ACCENT2, charSpacing: 2, fontFace: FONT,
  });
  s.addText("注册 → 选一条路线 → 学完打勾 → 写下笔记", {
    x: 0.8, y: 2.05, w: 8.6, h: 1.0, fontSize: 30, bold: true, color: "FFFFFF", fontFace: FONT,
  });
  s.addText("http://127.0.0.1:5173", {
    x: 0.8, y: 3.25, w: 8, h: 0.4, fontSize: 16, color: ACCENT2, fontFace: SERIF,
  });
  s.addText("java-learn-hub  ·  把学习变成可执行的地图", {
    x: 0.8, y: 4.55, w: 8, h: 0.3, fontSize: 10.5, color: "7E909C", fontFace: FONT,
  });
}

pptx.writeFile({ fileName: "D:\\workspace\\java-learn-hub\\docs\\java-learn-hub-介绍.pptx" });
