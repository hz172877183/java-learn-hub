/** 接口与页面共用的 TypeScript 类型。 */
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
  scenarios: Scenario[]
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

export interface TrackSummary {
  slug: string
  title: string
  audience: string
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
  scenarios?: Scenario[]
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

export interface Scenario {
  title: string
  signal: string
  approach: string
}

export type QuizType = 'choice' | 'text'

export interface QuizQuestion {
  id?: number
  type: QuizType
  stem: string
  options: string[]
  correctIndex?: number
  referenceAnswer?: string
  explanation: string
}
export type InterviewLevel = 'junior' | 'mid' | 'senior' | 'architect'

export interface InterviewQuestion {
  id: number
  category: string
  level: InterviewLevel
  type: QuizType
  stem: string
  options: string[]
  correctIndex?: number
  referenceAnswer?: string
  explanation: string
}

export interface InterviewQuestionList {
  items: InterviewQuestion[]
  page: number
  size: number
  total: number
}

export interface InterviewStat {
  id: string
  count: number
}

export interface InterviewMeta {
  levels: InterviewStat[]
  categories: InterviewStat[]
  total: number
}

export interface InterviewQuestionPayload {
  category: string
  level: string
  type: QuizType
  stem: string
  options: string[]
  correctIndex?: number
  referenceAnswer?: string
  explanation: string
}
export type AnswerSource = 'interview' | 'topic_quiz'

export interface AnswerSubmit {
  source: AnswerSource
  questionId: number
  choiceIndex?: number
  textAnswer?: string
  selfCorrect?: boolean
}

export interface AnswerView {
  source: AnswerSource
  questionId: number
  questionType: QuizType
  stem: string
  level?: string
  category?: string
  topicSlug?: string
  topicTitle?: string
  options: string[]
  correctIndex?: number
  choiceIndex?: number
  textAnswer?: string
  correct?: boolean
  explanation: string
  referenceAnswer?: string
  attempts: number
  answeredAt: string
}

export interface AnswerList {
  items: AnswerView[]
  page: number
  size: number
  total: number
}

export interface AnswerStats {
  total: number
  correct: number
  wrong: number
  ungraded: number
}