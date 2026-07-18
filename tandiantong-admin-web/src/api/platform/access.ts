import { request } from '../http'
import type { CreatePlatformAccountCommand, PlatformAccount, PlatformPermission, PlatformRole, PlatformRoleCommand, UpdatePlatformAccountCommand, UpdatePlatformRoleCommand } from '../../types/platform-access'

const basePath = '/api/platform/v1/access'

export const listPlatformAccounts = () => request<PlatformAccount[]>(`${basePath}/accounts`)
export const createPlatformAccount = (command: CreatePlatformAccountCommand) => request<PlatformAccount>(`${basePath}/accounts`, { method: 'POST', body: JSON.stringify(command) })
export const updatePlatformAccount = (id: number, command: UpdatePlatformAccountCommand) => request<void>(`${basePath}/accounts/${id}`, { method: 'PUT', body: JSON.stringify(command) })
export const updatePlatformAccountStatus = (id: number, enabled: boolean) => request<void>(`${basePath}/accounts/${id}/status`, { method: 'POST', body: JSON.stringify({ enabled }) })
export const resetPlatformAccountPassword = (id: number, password: string) => request<void>(`${basePath}/accounts/${id}/reset-password`, { method: 'POST', body: JSON.stringify({ password }) })

export const listPlatformRoles = () => request<PlatformRole[]>(`${basePath}/roles`)
export const createPlatformRole = (command: PlatformRoleCommand) => request<PlatformRole>(`${basePath}/roles`, { method: 'POST', body: JSON.stringify(command) })
export const updatePlatformRole = (id: number, command: UpdatePlatformRoleCommand) => request<void>(`${basePath}/roles/${id}`, { method: 'PUT', body: JSON.stringify(command) })
export const updatePlatformRoleStatus = (id: number, enabled: boolean) => request<void>(`${basePath}/roles/${id}/status`, { method: 'POST', body: JSON.stringify({ enabled }) })
export const listPlatformRolePermissionIds = (id: number) => request<number[]>(`${basePath}/roles/${id}/permission-ids`)
export const replacePlatformRolePermissions = (id: number, permissionIds: number[]) => request<void>(`${basePath}/roles/${id}/permission-ids`, { method: 'PUT', body: JSON.stringify({ permissionIds }) })
export const listPlatformPermissions = () => request<PlatformPermission[]>(`${basePath}/permissions`)
