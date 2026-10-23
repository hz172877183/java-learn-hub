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
