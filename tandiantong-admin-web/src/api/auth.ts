import { request } from './http'
import type { AccessDomain, LoginResponse } from '../types/auth'

export function login(domain: AccessDomain, mobile: string, password: string) {
  const path = domain === 'PLATFORM' ? '/api/platform/v1/auth/login' : '/api/admin/v1/auth/login'
  return request<LoginResponse>(path, { method: 'POST', body: JSON.stringify({ mobile, password }) })
}
