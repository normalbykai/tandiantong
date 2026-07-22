import type { AccessDomain } from '../types/auth'

export interface NavigationItem {
  label: string
  path: string
  icon: string
  group: string
  permissionCode?: string
  children?: NavigationItem[]
}

export const navigationByDomain: Record<AccessDomain, NavigationItem[]> = {
  PLATFORM: [
    { label: '工作台', path: '/platform/dashboard', icon: 'MonitorDot', group: '运营管理' },
    { label: '租户管理', path: '/platform/tenants', icon: 'Building2', group: '运营管理', permissionCode: 'platform:merchant:view' },
    { label: '操作日志', path: '/platform/logs', icon: 'ScrollText', group: '系统', permissionCode: 'platform:operation-log:view' },
    { label: '系统管理', path: '/platform/system', icon: 'Settings2', group: '系统', permissionCode: 'platform:system:view' },
    { label: '权限管理', path: '', icon: 'ShieldCheck', group: '系统', children: [
      { label: '平台账号', path: '/platform/accounts', icon: 'Users', group: '系统', permissionCode: 'platform:access:account:view' },
      { label: '平台角色', path: '/platform/roles', icon: 'ShieldCheck', group: '系统', permissionCode: 'platform:access:role:view' },
      { label: '平台权限', path: '/platform/permissions', icon: 'KeyRound', group: '系统', permissionCode: 'platform:access:permission:view' }
    ] }
  ],
  TENANT: [
    { label: '工作台', path: '/merchant/dashboard', icon: 'MonitorDot', group: '商户管理' },
    { label: '门店信息', path: '/merchant/store', icon: 'Store', group: '商户管理' },
    { label: '员工账号', path: '/merchant/staff', icon: 'Users', group: '权限管理' },
    { label: '商户角色', path: '/merchant/roles', icon: 'ShieldCheck', group: '权限管理' },
    { label: '商户权限', path: '/merchant/permissions', icon: 'KeyRound', group: '权限管理' },
    { label: '操作日志', path: '/merchant/logs', icon: 'ScrollText', group: '系统' },
    { label: '系统管理', path: '/merchant/system', icon: 'Settings2', group: '系统' }
  ]
}
