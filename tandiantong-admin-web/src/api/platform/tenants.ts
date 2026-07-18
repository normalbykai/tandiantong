import { request } from '../http'
import type { CreateTenantCommand, MerchantProvisioning, TenantOverview } from '../../types/tenant'

export function listTenants() {
  return request<TenantOverview[]>('/api/platform/v1/merchants')
}

export function createTenant(command: CreateTenantCommand) {
  return request<MerchantProvisioning>('/api/platform/v1/merchants', { method: 'POST', body: JSON.stringify(command) })
}

export function enableTenant(tenantId: number) {
  return request<void>(`/api/platform/v1/merchants/${tenantId}/enable`, { method: 'POST' })
}
