import type {
  AuthResponse,
  LearningState,
  MetaResponse,
  NoteItem,
  ProgressItem,
  QuizQuestion,
  TopicDetail,
  TopicDocument,
  TopicListResponse,
  TrackResponse,
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

export function fetchTopics(params: Record<string, string | undefined>) {
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v) q.set(k, v)
  })
  const suffix = q.toString() ? `?${q}` : ''
  return getJson<TopicListResponse>(`/api/v1/topics${suffix}`)
}

export function fetchTopic(slug: string) {
  return getJson<TopicDetail>(`/api/v1/topics/${slug}`)
}

export function fetchQuizzes(slug: string) {
  return getJson<QuizQuestion[]>(`/api/v1/topics/${slug}/quizzes`)
}

export function fetchTrack(slug = 'java-backend-job-ready') {
  return getJson<TrackResponse>(`/api/v1/tracks/${slug}`)
}

export function fetchMeta() {
  return getJson<MetaResponse>('/api/v1/meta/categories')
}

/* ---------------- 管理端 ---------------- */

export function getAdminToken() {
  return localStorage.getItem(TOKEN_KEY) ?? ''
}

export function setAdminToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

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

export function adminListTopics(q = '') {
  const suffix = q ? `?q=${encodeURIComponent(q)}` : ''
  return request<TopicDocument[]>(`/api/v1/admin/topics${suffix}`, adminInit('GET'))
}

export function adminGetTopic(slug: string) {
  return request<TopicDocument>(`/api/v1/admin/topics/${slug}`, adminInit('GET'))
}

export function adminCreateTopic(doc: TopicDocument) {
  return request<TopicDocument>('/api/v1/admin/topics', adminInit('POST', doc))
}

export function adminUpdateTopic(slug: string, doc: TopicDocument) {
  return request<TopicDocument>(`/api/v1/admin/topics/${slug}`, adminInit('PUT', doc))
}

export function adminDeleteTopic(slug: string) {
  return request<void>(`/api/v1/admin/topics/${slug}`, adminInit('DELETE'))
}

export function adminGetQuizzes(slug: string) {
  return request<QuizQuestion[]>(`/api/v1/admin/topics/${slug}/quizzes`, adminInit('GET'))
}

export function adminSaveQuizzes(slug: string, questions: QuizQuestion[]) {
  return request<QuizQuestion[]>(
    `/api/v1/admin/topics/${slug}/quizzes`,
    adminInit('PUT', questions),
  )
}

/* ---------------- 普通用户 ---------------- */

const USER_TOKEN_KEY = 'learnhub-token'

export function getUserToken() {
  return localStorage.getItem(USER_TOKEN_KEY) ?? ''
}

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

export function register(payload: { username: string; password: string; displayName?: string }) {
  return request<AuthResponse>('/api/v1/auth/register', jsonInit('POST', payload))
}

export function login(payload: { username: string; password: string }) {
  return request<AuthResponse>('/api/v1/auth/login', jsonInit('POST', payload))
}

export function fetchMe() {
  return request<UserInfo>('/api/v1/me', userInit('GET'))
}

export function fetchProgress() {
  return request<ProgressItem[]>('/api/v1/me/progress', userInit('GET'))
}

export function setProgress(slug: string, state: LearningState) {
  return request<ProgressItem>(`/api/v1/me/progress/${slug}`, userInit('PUT', { state }))
}

export function resetProgress(slug: string) {
  return request<void>(`/api/v1/me/progress/${slug}`, userInit('DELETE'))
}

export function fetchNotes() {
  return request<NoteItem[]>('/api/v1/me/notes', userInit('GET'))
}

export function fetchNote(slug: string) {
  return request<NoteItem>(`/api/v1/me/notes/${slug}`, userInit('GET'))
}

export function saveNote(slug: string, content: string) {
  return request<NoteItem>(`/api/v1/me/notes/${slug}`, userInit('PUT', { content }))
}

export function deleteNote(slug: string) {
  return request<void>(`/api/v1/me/notes/${slug}`, userInit('DELETE'))
}
