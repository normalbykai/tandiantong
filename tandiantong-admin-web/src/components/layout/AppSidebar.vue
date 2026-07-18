<template>
  <aside class="app-sidebar" :class="{ merchant: isMerchant }">
    <div class="sidebar-brand"><span class="brand-mark">摊</span><span class="brand-copy"><b>摊点通</b><small>{{ isMerchant ? '商户管理端' : '平台管理端' }}</small></span></div>
    <div class="sidebar-domain"><i /><span>当前权限域：{{ isMerchant ? '商户管理' : '平台管理' }}</span></div>
    <nav class="sidebar-menu" aria-label="后台导航">
      <template v-for="(item, index) in navigation" :key="item.path">
        <p v-if="index === 0 || item.group !== navigation[index - 1].group" class="menu-group">{{ item.group }}</p>
        <RouterLink :to="item.path" class="menu-item"><component :is="icons[item.icon as keyof typeof icons]" :size="17" /><span>{{ item.label }}</span></RouterLink>
      </template>
    </nav>
    <footer class="sidebar-footer"><span>摊点通 V1.1</span><em>墨璃</em></footer>
  </aside>
</template>

<script setup lang="ts">
import { Building2, KeyRound, MonitorDot, ScrollText, ShieldCheck, Store, Users } from 'lucide-vue-next'
import type { NavigationItem } from '../../constants/navigation'
defineProps<{ navigation: NavigationItem[]; isMerchant: boolean }>()
const icons = { Building2, KeyRound, MonitorDot, ScrollText, ShieldCheck, Store, Users }
</script>
