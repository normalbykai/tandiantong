<template>
  <div class="app-shell">
    <LoginView v-if="!loggedIn" @login="login" />
    <AdminLayout v-else :domain="domain" :merchants="merchants" @logout="logout" @create-merchant="createMerchant" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LoginView from './components/LoginView.vue'
import AdminLayout from './components/AdminLayout.vue'
import type { Domain, Merchant, MerchantDraft } from './types'
import { clearAccessToken } from './api'

const loggedIn = ref(false)
const domain = ref<Domain>('PLATFORM')
const merchants = ref<Merchant[]>([
  {
    id: 1001,
    name: '春风小铺',
    contact: '张晓春',
    mobile: '138****8000',
    status: '待复核',
    paymentStatus: '待验证',
    adminStatus: '已激活',
    products: 3,
    freeServices: 1,
    sceneKey: 'scene-vp8k29m4q7'
  },
  {
    id: 1002,
    name: '巷口咖啡',
    contact: '李青',
    mobile: '139****6123',
    status: '需修改',
    paymentStatus: '未配置',
    adminStatus: '待激活',
    products: 0,
    freeServices: 2,
    sceneKey: 'scene-k7b2n5t9ca'
  }
])

function login(nextDomain: Domain) {
  domain.value = nextDomain
  loggedIn.value = true
}

function logout() {
  clearAccessToken()
  loggedIn.value = false
}

function createMerchant(draft: MerchantDraft) {
  merchants.value.unshift({
    id: Date.now(),
    name: draft.subjectName,
    contact: draft.contact,
    mobile: draft.mobile.replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2'),
    status: '待启用',
    paymentStatus: '未配置',
    adminStatus: '待激活',
    products: 0,
    freeServices: 0,
    sceneKey: `scene-${Math.random().toString(36).slice(2, 14)}`
  })
}
</script>
