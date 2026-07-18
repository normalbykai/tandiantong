import type { ApiResponse } from '../types/api'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? ''
const tokenKey = 'tandiantong_access_token'

export function getAccessToken() {
  return sessionStorage.getItem(tokenKey) ?? ''
}

export function setAccessToken(token: string) {
  sessionStorage.setItem(tokenKey, token)
}

export function clearAccessToken() {
  sessionStorage.removeItem(tokenKey)
}

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')
  const token = getAccessToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const response = await fetch(`${baseUrl}${path}`, { ...init, headers })
  const payload = await response.json() as ApiResponse<T>
  if (!response.ok || !payload.success) {
    throw new Error(`${payload.message || '请求失败'}${payload.traceId ? `（追踪号：${payload.traceId}）` : ''}`)
  }
  return payload.data
}
