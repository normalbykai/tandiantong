<template>
  <div class="admin-shell" :class="{ 'is-merchant': isMerchant }">
    <aside class="sidebar">
      <div class="brand-lockup">
        <img src="/assets/tandiantong-logo-horizontal-reverse-v4.svg" alt="摊点通" />
      </div>
      <div class="domain-badge">当前权限域：{{ isMerchant ? '商户管理' : '平台管理' }}</div>
      <nav class="navigation" aria-label="后台导航">
        <RouterLink v-for="item in navigation" :key="item.path" :to="item.path" class="nav-link">
          <component :is="icons[item.icon as keyof typeof icons]" :size="18" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
      <div class="sidebar-footer">摊点通 V1.1 · 基础设施</div>
    </aside>
    <main class="main-content">
      <header class="topbar">
        <div class="topbar-context">
          <span class="context-dot" />
          <span>{{ isMerchant ? '单门店经营' : '平台运营中心' }}</span>
        </div>
        <el-dropdown trigger="click">
          <button class="account-button"><span class="avatar">{{ userInitial }}</span>{{ session.state.user?.displayName }}<ChevronDown :size="16" /></button>
          <template #dropdown><el-dropdown-menu><el-dropdown-item @click="logout">退出登录</el-dropdown-item></el-dropdown-menu></template>
        </el-dropdown>
      </header>
      <section class="page-content"><RouterView /></section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Building2, ChevronDown, KeyRound, MonitorDot, ScrollText, ShieldCheck, Store, Users } from 'lucide-vue-next'
import { navigationByDomain } from '../constants/navigation'
import { useSession } from '../stores/session'

const router = useRouter()
const session = useSession()
const isMerchant = computed(() => session.state.user?.domain === 'TENANT')
const navigation = computed(() => navigationByDomain[isMerchant.value ? 'TENANT' : 'PLATFORM'])
const userInitial = computed(() => session.state.user?.displayName.slice(0, 1) ?? '用')
const icons = { Building2, KeyRound, MonitorDot, ScrollText, ShieldCheck, Store, Users }

function logout() {
  session.signOut()
  void router.replace('/login')
}
</script>
