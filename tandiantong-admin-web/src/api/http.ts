import type { ApiResponse } from '../types/api'

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? ''
const tokenKey = 'tandiantong_access_token'
const persistentTokenExpiresAtKey = 'tandiantong_access_token_expires_at'
const sessionUserKey = 'tandiantong_session_user'
const sevenDaysInMilliseconds = 7 * 24 * 60 * 60 * 1000

function isPersistentTokenValid() {
  const expiresAt = Number(localStorage.getItem(persistentTokenExpiresAtKey))
  if (localStorage.getItem(tokenKey) && expiresAt > Date.now()) return true
  localStorage.removeItem(tokenKey)
  localStorage.removeItem(persistentTokenExpiresAtKey)
  localStorage.removeItem(sessionUserKey)
  return false
}

export function getAccessToken() {
  return sessionStorage.getItem(tokenKey) ?? (isPersistentTokenValid() ? localStorage.getItem(tokenKey) ?? '' : '')
}

export function isRememberedLogin() {
  return isPersistentTokenValid()
}

export function setAccessToken(token: string, rememberMe: boolean) {
  clearAccessToken()
  if (rememberMe) {
    localStorage.setItem(tokenKey, token)
    localStorage.setItem(persistentTokenExpiresAtKey, String(Date.now() + sevenDaysInMilliseconds))
    return
  }
  sessionStorage.setItem(tokenKey, token)
}

export function clearAccessToken() {
  sessionStorage.removeItem(tokenKey)
  localStorage.removeItem(tokenKey)
  localStorage.removeItem(persistentTokenExpiresAtKey)
}

function redirectToLogin() {
  clearAccessToken()
  sessionStorage.removeItem(sessionUserKey)
  localStorage.removeItem(sessionUserKey)
  if (window.location.pathname !== '/login') window.location.replace('/login')
}

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')
  const token = getAccessToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)

  const response = await fetch(`${baseUrl}${path}`, { ...init, headers })
  if (response.status === 401) {
    redirectToLogin()
    throw new Error('登录状态已失效，请重新登录')
  }
  if (response.status === 204) return undefined as T

  const responseText = await response.text()
  if (!responseText) {
    if (response.ok) return undefined as T
    throw new Error('请求失败，服务未返回错误详情')
  }

  let payload: ApiResponse<T>
  try {
    payload = JSON.parse(responseText) as ApiResponse<T>
  } catch {
    throw new Error('请求失败，服务响应格式异常')
  }
  if (!response.ok || !payload.success) {
    const permissionHint = payload.code === 'COMMON_FORBIDDEN' && payload.requiredPermission
      ? `（缺少权限：${payload.requiredPermission}）`
      : ''
    throw new Error(`${payload.message || '请求失败'}${permissionHint}${payload.traceId ? `（追踪号：${payload.traceId}）` : ''}`)
  }
  return payload.data
}
