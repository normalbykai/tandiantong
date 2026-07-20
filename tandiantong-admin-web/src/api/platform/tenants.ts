import { request } from '../http'
import type { CreateTenantCommand, MerchantProvisioning, TenantOverview } from '../../types/tenant'

export function listTenants(filters: { keyword?: string; status?: string; adminStatus?: string } = {}) {
  const query = new URLSearchParams()
  if (filters.keyword) query.set('keyword', filters.keyword)
  if (filters.status) query.set('status', filters.status)
  if (filters.adminStatus) query.set('adminStatus', filters.adminStatus)
  const queryString = query.toString()
  return request<TenantOverview[]>(`/api/platform/v1/merchants${queryString ? `?${queryString}` : ''}`)
}

export function createTenant(command: CreateTenantCommand) {
  return request<MerchantProvisioning>('/api/platform/v1/merchants', { method: 'POST', body: JSON.stringify(command) })
}

export function enableTenant(tenantId: number) {
  return request<void>(`/api/platform/v1/merchants/${tenantId}/enable`, { method: 'POST' })
}

export function disableTenant(tenantId: number) {
  return request<void>(`/api/platform/v1/merchants/${tenantId}/disable`, { method: 'POST' })
}

export function reissueTenantInvitation(tenantId: number) {
  return request<{ invitationCode: string; invitationExpiresAt: string }>(
    `/api/platform/v1/merchants/${tenantId}/invitation/reissue`,
    { method: 'POST' }
  )
}
