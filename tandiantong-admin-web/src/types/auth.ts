export type AccessDomain = 'PLATFORM' | 'TENANT'

export interface LoginResponse {
  accessToken: string
  domain: AccessDomain
  displayName: string
  roleName: string
  roleNames: string[]
  permissionCodes: string[]
}

export interface SessionUser {
  domain: AccessDomain
  displayName: string
  roleName: string
  roleNames: string[]
  permissionCodes: string[]
}
