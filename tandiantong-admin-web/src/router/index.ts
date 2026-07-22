import { createRouter, createWebHistory } from 'vue-router'
import { useSession } from '../stores/session'
import type { AccessDomain } from '../types/auth'
import AdminLayout from '../layouts/AdminLayout.vue'

const infrastructurePages = [
  ['merchant-system', '系统管理', '商户系统管理功能将在商户权限域中逐步开放'],
  ['merchant-store', '门店信息', '商户门店信息'], ['merchant-staff', '员工账号', '商户员工账号'],
  ['merchant-roles', '商户角色', '商户角色管理'], ['merchant-permissions', '商户权限说明', '商户权限说明'],
  ['merchant-logs', '操作日志', '商户操作日志']
]

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/auth/LoginPage.vue') },
  { path: '/merchant/activate', name: 'merchant-activate', component: () => import('../views/auth/MerchantActivationPage.vue') },
  {
    path: '/', component: AdminLayout, children: [
      { path: '', redirect: '/platform/dashboard' },
      { path: 'platform/dashboard', component: () => import('../views/platform/PlatformDashboardPage.vue'), meta: { domain: 'PLATFORM' satisfies AccessDomain } },
      { path: 'platform/tenants', component: () => import('../views/platform/TenantListPage.vue'), meta: { domain: 'PLATFORM' satisfies AccessDomain, permissionCode: 'platform:merchant:view' } },
      { path: 'platform/accounts', component: () => import('../views/platform/PlatformAccountPage.vue'), meta: { domain: 'PLATFORM' satisfies AccessDomain, permissionCode: 'platform:access:account:view' } },
      { path: 'platform/roles', component: () => import('../views/platform/PlatformRolePage.vue'), meta: { domain: 'PLATFORM' satisfies AccessDomain, permissionCode: 'platform:access:role:view' } },
      { path: 'platform/permissions', component: () => import('../views/platform/PlatformPermissionPage.vue'), meta: { domain: 'PLATFORM' satisfies AccessDomain, permissionCode: 'platform:access:permission:view' } },
      { path: 'platform/logs', component: () => import('../views/platform/PlatformOperationLogPage.vue'), meta: { domain: 'PLATFORM' satisfies AccessDomain, permissionCode: 'platform:operation-log:view' } },
      { path: 'platform/system', component: () => import('../views/platform/PlatformSystemPage.vue'), meta: { domain: 'PLATFORM' satisfies AccessDomain, permissionCode: 'platform:system:view' } },
      { path: 'merchant/dashboard', component: () => import('../views/merchant/MerchantDashboardPage.vue'), meta: { domain: 'TENANT' satisfies AccessDomain } },
      ...infrastructurePages.map(([key, title, description]) => ({
        path: key.startsWith('platform') ? `platform/${key.slice(9)}` : `merchant/${key.slice(9)}`,
        component: () => import('../views/common/UnavailablePage.vue'),
        props: { title, description },
        meta: { domain: key.startsWith('platform') ? 'PLATFORM' : 'TENANT' }
      }))
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to) => {
  const session = useSession()
  if (to.path === '/merchant/activate') return session.isLoggedIn ? (session.user?.domain === 'PLATFORM' ? '/platform/dashboard' : '/merchant/dashboard') : true
  if (to.path === '/login') return session.isLoggedIn ? (session.user?.domain === 'PLATFORM' ? '/platform/dashboard' : '/merchant/dashboard') : true
  if (!session.isLoggedIn) return '/login'
  const requiredDomain = to.meta.domain as AccessDomain | undefined
  if (requiredDomain && session.user?.domain !== requiredDomain) return session.user?.domain === 'PLATFORM' ? '/platform/dashboard' : '/merchant/dashboard'
  const requiredPermission = to.meta.permissionCode as string | undefined
  if (requiredPermission && !session.hasPermission(requiredPermission)) return session.user?.domain === 'PLATFORM' ? '/platform/dashboard' : '/merchant/dashboard'
  return true
})

export default router
