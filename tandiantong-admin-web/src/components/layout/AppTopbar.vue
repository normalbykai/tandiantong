<template>
  <header class="app-topbar"><div class="topbar-breadcrumb"><span>{{ title }}</span><i /> <time>{{ currentTime }}</time></div><div class="topbar-actions"><el-tooltip content="搜索功能开发中"><el-button text circle :icon="Search" /></el-tooltip><el-tooltip content="帮助中心开发中"><el-button text circle :icon="CircleHelp" /></el-tooltip><el-badge is-dot><el-button text circle :icon="Bell" /></el-badge><span class="topbar-divider" /><el-dropdown trigger="click"><button class="user-trigger"><span class="user-avatar">{{ userInitial }}</span><span class="user-copy"><b>{{ displayName }}</b><small>{{ isMerchant ? '商户管理员' : '平台运营员' }}</small></span><ChevronDown :size="13" /></button><template #dropdown><el-dropdown-menu><el-dropdown-item @click="$emit('logout')">退出登录</el-dropdown-item></el-dropdown-menu></template></el-dropdown><span class="topbar-divider" /><el-button text circle :icon="LogOut" aria-label="退出登录" @click="$emit('logout')" /></div></header>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { Bell, ChevronDown, CircleHelp, LogOut, Search } from 'lucide-vue-next'
const props = defineProps<{ title: string; displayName: string; isMerchant: boolean }>()
defineEmits<{ logout: [] }>()
const currentTime = ref(formatTime())
const userInitial = computed(() => props.displayName.slice(0, 1) || '用')
const timer = window.setInterval(() => { currentTime.value = formatTime() }, 1000)
onBeforeUnmount(() => window.clearInterval(timer))
function formatTime() { return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'medium', hour12: false }).format(new Date()) }
</script>
