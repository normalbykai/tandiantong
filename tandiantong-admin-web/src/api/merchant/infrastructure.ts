import { request } from '../http'

export interface MerchantStore { id: number; tenantId: number; name: string; status: 'ENABLED' | 'DISABLED' }
export interface MerchantStaff { id: number; mobile: string; displayName: string; status: 'ENABLED' | 'DISABLED' }
export interface MerchantRole { id: number; roleCode: string; name: string; description?: string; status: 'ENABLED' | 'DISABLED'; systemRole: boolean }
export interface MerchantPermission { id: number; permissionType: string; permissionCode: string; name: string }
export interface MerchantLog { id: number; operationType: string; targetType: string; targetId?: string; detail?: string; traceId?: string; createdAt: string }

const basePath = '/api/admin/v1/merchant-infrastructure'
export const getMerchantStore = () => request<MerchantStore>(`${basePath}/store`)
export const updateMerchantStore = (command: Pick<MerchantStore, 'name' | 'status'>) => request<MerchantStore>(`${basePath}/store`, { method: 'PUT', body: JSON.stringify(command) })
export const listMerchantStaff = () => request<MerchantStaff[]>(`${basePath}/staff`)
export const createMerchantStaff = (command: { mobile: string; displayName: string; password: string; roleId: number }) => request<MerchantStaff>(`${basePath}/staff`, { method: 'POST', body: JSON.stringify(command) })
export const updateMerchantStaff = (staffId: number, command: { mobile: string; displayName: string; roleId: number }) => request<MerchantStaff>(`${basePath}/staff/${staffId}`, { method: 'PUT', body: JSON.stringify(command) })
export const updateMerchantStaffStatus = (staffId: number, status: MerchantStaff['status']) => request<void>(`${basePath}/staff/${staffId}/status`, { method: 'POST', body: JSON.stringify({ status }) })
export const listMerchantRoles = () => request<MerchantRole[]>(`${basePath}/roles`)
export const createMerchantRole = (command: { roleCode: string; name: string; description?: string }) => request<MerchantRole>(`${basePath}/roles`, { method: 'POST', body: JSON.stringify(command) })
export const updateMerchantRole = (roleId: number, command: { name: string; description?: string }) => request<MerchantRole>(`${basePath}/roles/${roleId}`, { method: 'PUT', body: JSON.stringify(command) })
export const updateMerchantRoleStatus = (roleId: number, status: MerchantRole['status']) => request<void>(`${basePath}/roles/${roleId}/status`, { method: 'POST', body: JSON.stringify({ status }) })
export const listMerchantRolePermissionIds = (roleId: number) => request<number[]>(`${basePath}/roles/${roleId}/permission-ids`)
export const updateMerchantRolePermissionIds = (roleId: number, permissionIds: number[]) => request<void>(`${basePath}/roles/${roleId}/permission-ids`, { method: 'PUT', body: JSON.stringify({ permissionIds }) })
export const listMerchantPermissions = () => request<MerchantPermission[]>(`${basePath}/permissions`)
export interface MerchantSystemConfig { id: number; tenantId: number; storeId: number; shortName: string; notice: string }
export const getMerchantSystemConfig = () => request<MerchantSystemConfig>(`${basePath}/system-config`)
export const updateMerchantSystemConfig = (command: Pick<MerchantSystemConfig, 'shortName' | 'notice'>) => request<MerchantSystemConfig>(`${basePath}/system-config`, { method: 'PUT', body: JSON.stringify(command) })
export const listMerchantLogs = (query: { keyword?: string; operationType?: string; page?: number; pageSize?: number } = {}) => {
  const params = new URLSearchParams()
  if (query.keyword) params.set('keyword', query.keyword)
  if (query.operationType) params.set('operationType', query.operationType)
  params.set('page', String(query.page ?? 1))
  params.set('pageSize', String(query.pageSize ?? 20))
  return request<MerchantLog[]>(`${basePath}/logs?${params.toString()}`)
}
