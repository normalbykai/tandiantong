<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">摊点通</div>
      <button
        v-for="item in menu"
        :key="item.key"
        class="menu-button"
        :class="{ active: active === item.key }"
        @click="active = item.key"
      >
        <component :is="item.icon" :size="18" />
        <span>{{ item.label }}</span>
      </button>
    </aside>
    <main class="content">
      <header class="topbar">
        <div>
          <h2>{{ currentTitle }}</h2>
          <p>{{ domain === 'PLATFORM' ? '平台权限域' : '商户权限域 · 春风小铺' }}</p>
        </div>
        <el-button @click="$emit('logout')">退出登录</el-button>
      </header>

      <PlatformDashboard v-if="domain === 'PLATFORM' && active === 'dashboard'" :merchants="merchants" />
      <MerchantList v-else-if="domain === 'PLATFORM' && active === 'merchants'" :merchants="merchants" @create-merchant="emitCreate" />
      <ReviewBoard v-else-if="domain === 'PLATFORM' && active === 'reviews'" :merchants="merchants" />
      <TenantDashboard v-else-if="domain === 'TENANT' && active === 'dashboard'" />
      <VerificationCenter v-else-if="domain === 'TENANT' && active === 'verification'" />
      <PlaceholderPanel v-else :title="currentTitle" />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { BarChart3, ClipboardCheck, QrCode, Settings, ShieldCheck, Store, Users } from 'lucide-vue-next'
import PlatformDashboard from './PlatformDashboard.vue'
import MerchantList from './MerchantList.vue'
import ReviewBoard from './ReviewBoard.vue'
import TenantDashboard from './TenantDashboard.vue'
import VerificationCenter from './VerificationCenter.vue'
import PlaceholderPanel from './PlaceholderPanel.vue'
import type { Domain, Merchant, MerchantDraft } from '../types'

const props = defineProps<{ domain: Domain; merchants: Merchant[] }>()
const emit = defineEmits<{ logout: []; createMerchant: [draft: MerchantDraft] }>()
const active = ref('dashboard')

const platformMenu = [
  { key: 'dashboard', label: '平台工作台', icon: BarChart3 },
  { key: 'merchants', label: '商户管理', icon: Store },
  { key: 'reviews', label: '审核工作台', icon: ClipboardCheck },
  { key: 'admins', label: '平台管理员', icon: ShieldCheck }
]

const tenantMenu = [
  { key: 'dashboard', label: '商户工作台', icon: BarChart3 },
  { key: 'catalog', label: '商品与库存', icon: Store },
  { key: 'verification', label: '核销中心', icon: QrCode },
  { key: 'staff', label: '员工与角色', icon: Users },
  { key: 'settings', label: '商户设置', icon: Settings }
]

const menu = computed(() => props.domain === 'PLATFORM' ? platformMenu : tenantMenu)
const currentTitle = computed(() => menu.value.find(item => item.key === active.value)?.label ?? '工作台')

watch(() => props.domain, () => {
  active.value = 'dashboard'
})

function emitCreate(draft: MerchantDraft) {
  emit('createMerchant', draft)
}
</script>
