ti<template>
  <div class="app-shell" :data-domain="isMerchant ? 'merchant' : 'platform'">
    <AppSidebar :navigation="navigation" :is-merchant="isMerchant" />
    <main class="app-main">
      <AppTopbar :title="currentTitle" :display-name="session.user?.displayName ?? '当前用户'" :role-name="session.primaryRoleName || (isMerchant ? '商户账号' : '平台账号')" @logout="logout" />
      <section class="page-content"><RouterView /></section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { navigationByDomain } from '../constants/navigation'
import type { NavigationItem } from '../constants/navigation'
import { useSession } from '../stores/session'
import AppSidebar from '../components/layout/AppSidebar.vue'
import AppTopbar from '../components/layout/AppTopbar.vue'

const router = useRouter()
const route = useRoute()
const session = useSession()
const isMerchant = computed(() => session.user?.domain === 'TENANT')
function filterNavigation(items: NavigationItem[]): NavigationItem[] {
  return items.flatMap(item => {
    const children = item.children ? filterNavigation(item.children) : undefined
    if (item.permissionCode && !session.hasPermission(item.permissionCode) && !children?.length) return []
    return [{ ...item, children }]
  })
}
const navigation = computed(() => filterNavigation(navigationByDomain[isMerchant.value ? 'TENANT' : 'PLATFORM']))
const currentTitle = computed(() => navigation.value.flatMap(item => item.children ?? [item]).find(item => item.path === route.path)?.label ?? '基础设施')

function logout() {
  session.signOut()
  void router.replace('/login')
}
</script>
