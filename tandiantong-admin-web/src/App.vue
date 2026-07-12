<template>
  <div class="app-shell">
    <LoginView v-if="!loggedIn" @login="login" />
    <AdminLayout v-else :domain="domain" @logout="logout" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LoginView from './components/LoginView.vue'
import AdminLayout from './components/AdminLayout.vue'
import type { Domain } from './types'
import { clearAccessToken } from './api'

const loggedIn = ref(false)
const domain = ref<Domain>('PLATFORM')

function login(nextDomain: Domain) {
  domain.value = nextDomain
  loggedIn.value = true
}

function logout() {
  clearAccessToken()
  loggedIn.value = false
}

</script>
