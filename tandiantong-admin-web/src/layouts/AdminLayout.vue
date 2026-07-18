<template>
  <div class="app-shell" :data-domain="isMerchant ? 'merchant' : 'platform'">
    <AppSidebar :navigation="navigation" :is-merchant="isMerchant" />
    <main class="app-main">
      <AppTopbar :title="currentTitle" :display-name="session.state.user?.displayName ?? '当前用户'" :is-merchant="isMerchant" @logout="logout" />
      <section class="page-content"><RouterView /></section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { navigationByDomain } from '../constants/navigation'
import { useSession } from '../stores/session'
import AppSidebar from '../components/layout/AppSidebar.vue'
import AppTopbar from '../components/layout/AppTopbar.vue'

const router = useRouter()
const route = useRoute()
const session = useSession()
const isMerchant = computed(() => session.state.user?.domain === 'TENANT')
const navigation = computed(() => navigationByDomain[isMerchant.value ? 'TENANT' : 'PLATFORM'])
const currentTitle = computed(() => navigation.value.find(item => item.path === route.path)?.label ?? '基础设施')

function logout() {
  session.signOut()
  void router.replace('/login')
}
</script>
