/** 枚举值到中文标签的映射。 */
export const categoryLabel: Record<string, string> = {
  'spring-core': 'Spring 核心',
  data: '数据访问',
  cache: '缓存',
  messaging: '消息',
  security: '安全',
  engineering: '工程化',
  frontend: '前端配套',
  cloud: '微服务',
  search: '搜索',
  observability: '可观测',
  ai: 'AI 应用',
}

export const difficultyLabel: Record<string, string> = {
  beginner: '入门',
  intermediate: '进阶',
  practical: '工程实践',
}

export const integrationLabel: Record<string, string> = {
  docs_only: '仅文档',
  snippet: '有示例代码',
  runtime: '已接入运行时',
}

/** 面试题等级标签。 */
export const interviewLevelLabel: Record<string, string> = {
  junior: '初级',
  mid: '中级',
  senior: '高级',
  architect: '架构师',
}

/** 面试题分类标签；未收录的分类直接显示 id。 */
export const interviewCategoryLabel: Record<string, string> = {
  'java-basics': 'Java 基础',
  collections: '集合',
  concurrency: '并发',
  jvm: 'JVM',
  spring: 'Spring',
  'spring-boot': 'Spring Boot',
  'spring-cloud': '微服务',
  mysql: 'MySQL',
  redis: 'Redis',
  mq: '消息队列',
  distributed: '分布式',
  architecture: '架构设计',
  engineering: '工程实践',
  'docker-k8s': '容器与 K8s',
  network: '计算机网络',
  'os-linux': '操作系统 / Linux',
  'design-pattern': '设计模式',
  algorithm: '算法与数据结构',
  frontend: '前端',
  security: 'Web 安全',
  testing: '测试与质量',
  ai: 'AI 应用',
  project: '项目与软技能',
}
