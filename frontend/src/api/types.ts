export interface TopicSummary {
  slug: string
  title: string
  category: string
  summary: string
  difficulty: string
  estimatedHours: number
  tags: string[]
  integrationStatus: string
}

export interface TopicRef {
  slug: string
  title: string
}

export interface TopicDetail extends TopicSummary {
  officialDocsUrl?: string
  overviewMd: string
  concepts: { term: string; explanation: string }[]
  pathSteps: { title: string; goal: string; hours?: number; resources: string[] }[]
  samples: { title: string; language: string; description: string; code: string }[]
  pitfalls: { title: string; detail: string }[]
  prerequisites: TopicRef[]
  nextTopics: TopicRef[]
  relatedTopics: TopicRef[]
  repoPointers: { label: string; path: string }[]
}

export interface TopicListResponse {
  items: TopicSummary[]
  page: number
  size: number
  total: number
}

export interface MetaResponse {
  categories: { id: string; count: number }[]
  publishedTotal: number
}

export interface TrackResponse {
  slug: string
  title: string
  audience: string
  stages: { title: string; goal: string; topics: TopicSummary[] }[]
}
