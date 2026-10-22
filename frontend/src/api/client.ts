import type {
  MetaResponse,
  TopicDetail,
  TopicDocument,
  TopicListResponse,
  TrackResponse,
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
