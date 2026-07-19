<template>
  <aside class="app-sidebar" :class="{ merchant: isMerchant }">
    <div class="sidebar-brand"><span class="brand-mark">摊</span><span class="brand-copy"><b>摊点通</b><small>{{ isMerchant ? '商户管理端' : '平台管理端' }}</small></span></div>
    <div class="sidebar-domain"><i /><span>当前权限域：{{ isMerchant ? '商户管理' : '平台管理' }}</span></div>
    <nav class="sidebar-menu" aria-label="后台导航">
      <template v-for="(item, index) in navigation" :key="item.label">
        <p v-if="index === 0 || item.group !== navigation[index - 1].group" class="menu-group">{{ item.group }}</p>
        <RouterLink v-if="!item.children?.length" :to="item.path" class="menu-item"><component :is="icons[item.icon as keyof typeof icons]" :size="17" /><span>{{ item.label }}</span></RouterLink>
        <div v-else class="menu-subtree">
          <button type="button" class="menu-item menu-parent" :aria-expanded="isExpanded(item.label)" @click="toggleExpanded(item.label)"><component :is="icons[item.icon as keyof typeof icons]" :size="17" /><span>{{ item.label }}</span><ChevronDown :size="15" class="menu-chevron" :class="{ expanded: isExpanded(item.label) }" /></button>
          <div v-show="isExpanded(item.label)" class="menu-children"><RouterLink v-for="child in item.children" :key="child.path" :to="child.path" class="menu-item menu-child"><component :is="icons[child.icon as keyof typeof icons]" :size="15" /><span>{{ child.label }}</span></RouterLink></div>
        </div>
      </template>
    </nav>
    <footer class="sidebar-footer"><span>摊点通 V1.1</span><em>墨璃</em></footer>
  </aside>
</template>

<script setup lang="ts">
import { Building2, ChevronDown, KeyRound, MonitorDot, ScrollText, Settings2, ShieldCheck, Store, Users } from 'lucide-vue-next'
import { ref } from 'vue'
import type { NavigationItem } from '../../constants/navigation'
defineProps<{ navigation: NavigationItem[]; isMerchant: boolean }>()
const icons = { Building2, KeyRound, MonitorDot, ScrollText, Settings2, ShieldCheck, Store, Users }
const expandedItems = ref<string[]>(['权限管理'])
function isExpanded(label: string) { return expandedItems.value.includes(label) }
function toggleExpanded(label: string) { expandedItems.value = isExpanded(label) ? expandedItems.value.filter(item => item !== label) : [...expandedItems.value, label] }
</script>

<style scoped>
.menu-parent { width: 100%; border: 0; background: transparent; cursor: pointer; text-align: left; }
.menu-chevron { margin-left: auto; transition: transform 180ms ease; }
.menu-chevron.expanded { transform: rotate(180deg); }
.menu-children { margin: 2px 0 6px 14px; padding-left: 8px; border-left: 1px solid rgba(255, 255, 255, .1); }
.menu-child { min-height: 34px; padding-top: 8px; padding-bottom: 8px; font-size: 12px; }
</style>
