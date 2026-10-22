import type { MetaResponse, TopicDetail, TopicListResponse, TrackResponse } from './types'

async function getJson<T>(url: string): Promise<T> {
  const res = await fetch(url)
  if (!res.ok) {
    throw new Error(String(res.status))
  }
  return res.json() as Promise<T>
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
