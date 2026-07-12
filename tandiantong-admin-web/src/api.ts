export type ApiEnvelope<T> = { success: boolean; code: string; message: string; traceId: string; data: T }

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'
let accessToken = sessionStorage.getItem('tandiantong_access_token') ?? ''

export function setAccessToken(token: string) {
  accessToken = token
  sessionStorage.setItem('tandiantong_access_token', token)
}

export function clearAccessToken() {
  accessToken = ''
  sessionStorage.removeItem('tandiantong_access_token')
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers)
  headers.set('Content-Type', 'application/json')
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)
  const response = await fetch(`${baseUrl}${path}`, { ...init, headers })
  const envelope = await response.json() as ApiEnvelope<T>
  if (!response.ok || !envelope.success) throw new Error(`${envelope.message}（追踪号：${envelope.traceId}）`)
  return envelope.data
}

export async function downloadFile(path: string): Promise<{ blob: Blob; fileName: string }> {
  const headers = new Headers()
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)
  const response = await fetch(`${baseUrl}${path}`, { headers })
  if (!response.ok) {
    const envelope = await response.json() as ApiEnvelope<never>
    throw new Error(`${envelope.message}（追踪号：${envelope.traceId}）`)
  }
  const disposition = response.headers.get('Content-Disposition') ?? ''
  const encoded = disposition.match(/filename\*=UTF-8''([^;]+)/)?.[1]
  return { blob: await response.blob(), fileName: encoded ? decodeURIComponent(encoded) : '经营数据.xlsx' }
}
