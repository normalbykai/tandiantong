import type { AccessDomain } from '../types/auth'

export interface NavigationItem {
  label: string
  path: string
  icon: string
}

export const navigationByDomain: Record<AccessDomain, NavigationItem[]> = {
  PLATFORM: [
    { label: '平台工作台', path: '/platform/dashboard', icon: 'MonitorDot' },
    { label: '租户管理', path: '/platform/tenants', icon: 'Building2' },
    { label: '平台账号', path: '/platform/accounts', icon: 'Users' },
    { label: '平台角色', path: '/platform/roles', icon: 'ShieldCheck' },
    { label: '平台权限点', path: '/platform/permissions', icon: 'KeyRound' },
    { label: '操作日志', path: '/platform/logs', icon: 'ScrollText' }
  ],
  TENANT: [
    { label: '商户工作台', path: '/merchant/dashboard', icon: 'MonitorDot' },
    { label: '门店信息', path: '/merchant/store', icon: 'Store' },
    { label: '员工账号', path: '/merchant/staff', icon: 'Users' },
    { label: '商户角色', path: '/merchant/roles', icon: 'ShieldCheck' },
    { label: '商户权限', path: '/merchant/permissions', icon: 'KeyRound' },
    { label: '操作日志', path: '/merchant/logs', icon: 'ScrollText' }
  ]
}
