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

export interface Concept {
  term: string
  explanation: string
}

export interface PathStep {
  title: string
  goal: string
  hours?: number
  resources: string[]
}

export interface Sample {
  title: string
  language: string
  description: string
  code: string
}

export interface Pitfall {
  title: string
  detail: string
}

export interface RepoPointer {
  label: string
  path: string
}

/** 管理端编辑的完整学习项，与 content/topics/*.json 结构一致 */
export interface TopicDocument {
  slug: string
  title: string
  category: string
  summary?: string
  difficulty: string
  estimatedHours?: number
  tags?: string[]
  status: string
  integrationStatus: string
  officialDocsUrl?: string
  prerequisites?: string[]
  nextTopics?: string[]
  relatedTopics?: string[]
  overviewMd?: string
  concepts?: Concept[]
  pathSteps?: PathStep[]
  samples?: Sample[]
  pitfalls?: Pitfall[]
  repoPointers?: RepoPointer[]
}

export interface UserInfo {
  id: number
  username: string
  displayName: string
}

export interface AuthResponse {
  token: string
  user: UserInfo
}

export type LearningState = 'not_started' | 'learning' | 'completed'

export interface ProgressItem {
  topicSlug: string
  title: string
  state: LearningState
  startedAt?: string
  completedAt?: string
  updatedAt?: string
}

export interface NoteItem {
  topicSlug: string
  title: string
  content: string
  updatedAt?: string
}