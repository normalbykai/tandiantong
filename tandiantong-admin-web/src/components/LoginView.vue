<template>
  <main class="login-page">
    <section class="login-panel">
      <img src="/assets/tandiantong-logo-full.png" alt="摊点通" class="login-logo" />
      <h1>摊点通</h1>
      <p>让线下零售与服务经营更简单</p>
      <el-segmented v-model="domain" :options="domainOptions" block />
      <el-form label-position="top" class="login-form">
        <el-form-item label="手机号">
          <el-input v-model="mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <div class="login-row">
          <el-checkbox v-model="remember">记住当前身份</el-checkbox>
          <el-button link>忘记密码</el-button>
        </div>
        <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
        <el-button type="primary" size="large" class="login-button" :loading="submitting" @click="submit">登录</el-button>
      </el-form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Domain } from '../types'
import { apiRequest, setAccessToken } from '../api'

const emit = defineEmits<{ login: [domain: Domain] }>()

const domain = ref<Domain>('PLATFORM')
const mobile = ref('')
const password = ref('')
const remember = ref(true)
const submitting = ref(false)
const errorMessage = ref('')
const domainOptions = [
  { label: '平台管理', value: 'PLATFORM' },
  { label: '商户管理', value: 'TENANT' }
]

async function submit() {
  submitting.value = true
  errorMessage.value = ''
  try {
    const prefix = domain.value === 'PLATFORM' ? '/api/platform/v1' : '/api/admin/v1'
    const result = await apiRequest<{ accessToken: string }>(`${prefix}/auth/login`, {
      method: 'POST', body: JSON.stringify({ mobile: mobile.value, password: password.value })
    })
    setAccessToken(result.accessToken)
    emit('login', domain.value)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后再试'
  } finally {
    submitting.value = false
  }
}
</script>
