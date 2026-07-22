export type EnableStatus = 'ENABLED' | 'DISABLED'

export interface PlatformAccount {
  id: number
  mobile: string
  displayName: string
  status: EnableStatus
  roleIds: number[]
  createdAt: string
  lastLoginAt?: string
  failedLoginCount?: number
  lockedUntil?: string
}

export interface PlatformRole {
  id: number
  roleCode: string
  name: string
  description?: string
  status: EnableStatus
  systemRole: boolean
}

export interface PlatformPermission {
  id: number
  permissionType: string
  permissionCode: string
  name: string
}

export interface PlatformPermissionPage {
  total: number
  current: number
  pageSize: number
  records: PlatformPermission[]
}

export interface CreatePlatformAccountCommand {
  mobile: string
  displayName: string
  password: string
  roleIds: number[]
}

export interface UpdatePlatformAccountCommand {
  displayName: string
  roleIds: number[]
}

export interface PlatformRoleCommand {
  name: string
  roleCode: string
  description?: string
}

export interface UpdatePlatformRoleCommand {
  name: string
  description?: string
}
