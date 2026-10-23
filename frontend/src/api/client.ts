/** HTTP 客户端：公开接口、管理端接口与当前用户接口的统一封装。 */
import type {
  AuthResponse,
  InterviewMeta,
  InterviewQuestion,
  InterviewQuestionList,
  InterviewQuestionPayload,
  LearningState,
  MetaResponse,
  NoteItem,
  ProgressItem,
  QuizQuestion,
  TopicDetail,
  TopicDocument,
  TopicListResponse,
  TrackResponse,
  TrackSummary,
  UserInfo,
} from './types'

const TOKEN_KEY = 'learnhub-admin-token'

async function request<T>(url: string, init: RequestInit = {}): Promise<T> {
  const res = await fetch(url, init)
  if (!res.ok) {
    let message = `HTTP ${res.status}`
    try {
      const body = (await res.json()) as { message?: string }
      if (body.message) message = body.message
    } catch {
      /* 响应体不是 JSON 时保留状态码 */
    }
    throw new Error(message)
  }
  if (res.status === 204) {
    return undefined as T
  }
  return res.json() as Promise<T>
}

async function getJson<T>(url: string): Promise<T> {
  return request<T>(url)
}

/** 学习项目录：组合筛选 + 分页。 */
export function fetchTopics(params: Record<string, string | undefined>) {
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v) q.set(k, v)
  })
  const suffix = q.toString() ? `?${q}` : ''
  return getJson<TopicListResponse>(`/api/v1/topics${suffix}`)
}

/** 学习项详情。 */
export function fetchTopic(slug: string) {
  return getJson<TopicDetail>(`/api/v1/topics/${slug}`)
}

/** 某个学习项的面试题。 */
export function fetchQuizzes(slug: string) {
  return getJson<QuizQuestion[]>(`/api/v1/topics/${slug}/quizzes`)
}

/** 全部路线摘要，供前台切换。 */
export function fetchTracks() {
  return getJson<TrackSummary[]>('/api/v1/tracks')
}

/** 某条路线的阶段与学习项。 */
export function fetchTrack(slug = 'java-backend-job-ready') {
  return getJson<TrackResponse>(`/api/v1/tracks/${slug}`)
}

/** 分类统计。 */
export function fetchMeta() {
  return getJson<MetaResponse>('/api/v1/meta/categories')
}

/* ---------------- 管理端 ---------------- */

export function getAdminToken() {
  return localStorage.getItem(TOKEN_KEY) ?? ''
}

/** 写入管理令牌。 */
export function setAdminToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

/** 清除管理令牌。 */
export function clearAdminToken() {
  localStorage.removeItem(TOKEN_KEY)
}

function adminInit(method: string, body?: unknown): RequestInit {
  const headers: Record<string, string> = {
    Authorization: `Bearer ${getAdminToken()}`,
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  return {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  }
}

/** 管理端列表（包含草稿）。 */
export function adminListTopics(q = '') {
  const suffix = q ? `?q=${encodeURIComponent(q)}` : ''
  return request<TopicDocument[]>(`/api/v1/admin/topics${suffix}`, adminInit('GET'))
}

/** 管理端读取单个学习项。 */
export function adminGetTopic(slug: string) {
  return request<TopicDocument>(`/api/v1/admin/topics/${slug}`, adminInit('GET'))
}

/** 新建学习项。 */
export function adminCreateTopic(doc: TopicDocument) {
  return request<TopicDocument>('/api/v1/admin/topics', adminInit('POST', doc))
}

/** 更新学习项（slug 不可变）。 */
export function adminUpdateTopic(slug: string, doc: TopicDocument) {
  return request<TopicDocument>(`/api/v1/admin/topics/${slug}`, adminInit('PUT', doc))
}

/** 物理删除学习项。 */
export function adminDeleteTopic(slug: string) {
  return request<void>(`/api/v1/admin/topics/${slug}`, adminInit('DELETE'))
}

/** 管理端读取题目。 */
export function adminGetQuizzes(slug: string) {
  return request<QuizQuestion[]>(`/api/v1/admin/topics/${slug}/quizzes`, adminInit('GET'))
}

/** 题目整组替换。 */
export function adminSaveQuizzes(slug: string, questions: QuizQuestion[]) {
  return request<QuizQuestion[]>(
    `/api/v1/admin/topics/${slug}/quizzes`,
    adminInit('PUT', questions),
  )
}

/* ---------------- 普通用户 ---------------- */

const USER_TOKEN_KEY = 'learnhub-token'

/** 读取用户 JWT。 */
export function getUserToken() {
  return localStorage.getItem(USER_TOKEN_KEY) ?? ''
}

/** 写入或清除用户 JWT。 */
export function setUserToken(token: string) {
  if (token) {
    localStorage.setItem(USER_TOKEN_KEY, token)
  } else {
    localStorage.removeItem(USER_TOKEN_KEY)
  }
}

function jsonInit(method: string, body?: unknown): RequestInit {
  return {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  }
}

function userInit(method: string, body?: unknown): RequestInit {
  const headers: Record<string, string> = {
    Authorization: `Bearer ${getUserToken()}`,
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  return {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  }
}

/** 注册并自动登录。 */
export function register(payload: { username: string; password: string; displayName?: string }) {
  return request<AuthResponse>('/api/v1/auth/register', jsonInit('POST', payload))
}

/** 登录并返回 JWT。 */
export function login(payload: { username: string; password: string }) {
  return request<AuthResponse>('/api/v1/auth/login', jsonInit('POST', payload))
}

/** 当前用户信息。 */
export function fetchMe() {
  return request<UserInfo>('/api/v1/me', userInit('GET'))
}

/** 我的学习进度。 */
export function fetchProgress() {
  return request<ProgressItem[]>('/api/v1/me/progress', userInit('GET'))
}

/** 更新单个学习项状态。 */
export function setProgress(slug: string, state: LearningState) {
  return request<ProgressItem>(`/api/v1/me/progress/${slug}`, userInit('PUT', { state }))
}

/** 重置进度。 */
export function resetProgress(slug: string) {
  return request<void>(`/api/v1/me/progress/${slug}`, userInit('DELETE'))
}

/** 我的全部笔记。 */
export function fetchNotes() {
  return request<NoteItem[]>('/api/v1/me/notes', userInit('GET'))
}

/** 单个学习项的笔记。 */
export function fetchNote(slug: string) {
  return request<NoteItem>(`/api/v1/me/notes/${slug}`, userInit('GET'))
}

/** 保存笔记（空白即删除）。 */
export function saveNote(slug: string, content: string) {
  return request<NoteItem>(`/api/v1/me/notes/${slug}`, userInit('PUT', { content }))
}

/** 删除笔记。 */
export function deleteNote(slug: string) {
  return request<void>(`/api/v1/me/notes/${slug}`, userInit('DELETE'))
}

/* ---------------- 面试题专区 ---------------- */

function interviewParams(params: {
  category?: string
  level?: string
  q?: string
  page?: number
  size?: number
}) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') search.set(key, String(value))
  })
  const suffix = search.toString() ? `?${search}` : ''
  return suffix
}

/** 面试题列表：按分类/等级/关键词筛选。 */
export function fetchInterviewQuestions(params: {
  category?: string
  level?: string
  q?: string
  page?: number
  size?: number
}) {
  return request<InterviewQuestionList>(`/api/v1/interview/questions${interviewParams(params)}`)
}

/** 面试题分类与等级统计。 */
export function fetchInterviewMeta() {
  return request<InterviewMeta>('/api/v1/interview/meta')
}

/** 管理端面试题列表。 */
export function adminListInterviewQuestions(params: {
  category?: string
  level?: string
  q?: string
  page?: number
  size?: number
}) {
  return request<InterviewQuestionList>(
    `/api/v1/admin/interview/questions${interviewParams(params)}`,
    adminInit('GET'),
  )
}

/** 新建面试题。 */
export function adminCreateInterviewQuestion(payload: InterviewQuestionPayload) {
  return request<InterviewQuestion>('/api/v1/admin/interview/questions', adminInit('POST', payload))
}

/** 更新面试题。 */
export function adminUpdateInterviewQuestion(id: number, payload: InterviewQuestionPayload) {
  return request<InterviewQuestion>(
    `/api/v1/admin/interview/questions/${id}`,
    adminInit('PUT', payload),
  )
}

/** 删除面试题。 */
export function adminDeleteInterviewQuestion(id: number) {
  return request<void>(`/api/v1/admin/interview/questions/${id}`, adminInit('DELETE'))
}