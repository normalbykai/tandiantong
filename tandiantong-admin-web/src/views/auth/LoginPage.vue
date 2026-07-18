<template>
  <main class="login-page" :class="{ merchant: domain === 'TENANT' }">
    <div class="login-band" /><div class="login-mark">摊</div><i v-for="dot in 18" :key="dot" class="login-particle" :style="particleStyle(dot)" />
    <section class="login-card">
      <div class="login-brand"><span class="login-mark-icon"><img src="/assets/tandiantong-logo-mark-v4.svg" alt="" /></span><div><h1>摊点通</h1><p>门店经营管理后台</p></div></div>
      <el-radio-group v-model="domain" class="domain-switch" size="large"><el-radio-button label="PLATFORM">平台管理端</el-radio-button><el-radio-button label="TENANT">商户管理端</el-radio-button></el-radio-group>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form" @submit.prevent="submit">
        <el-form-item label="账号" prop="mobile"><el-input v-model="form.mobile" placeholder="请输入手机号" autocomplete="username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" placeholder="请输入登录密码" show-password autocomplete="current-password" /></el-form-item>
        <el-alert v-if="errorMessage" :title="errorMessage" type="error" :closable="false" show-icon />
        <div class="login-options"><el-checkbox v-model="remember">7 天内自动登录</el-checkbox><el-button text>忘记密码？</el-button></div>
        <el-button native-type="submit" type="primary" size="large" :loading="submitting" class="login-submit">登 录</el-button>
      </el-form>
      <p class="login-notice">© 2026 摊点通</p>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import { login } from '../../api/auth'
import { useSession } from '../../stores/session'
import type { AccessDomain } from '../../types/auth'

const router = useRouter()
const { signIn } = useSession()
const formRef = ref<FormInstance>()
const domain = ref<AccessDomain>('PLATFORM')
const form = reactive({ mobile: '', password: '' })
const submitting = ref(false)
const errorMessage = ref('')
const remember = ref(true)
const rules: FormRules = { mobile: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }] }

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  errorMessage.value = ''
  try {
    const result = await login(domain.value, form.mobile, form.password)
    signIn(result.accessToken, result.domain, result.displayName)
    await router.replace(result.domain === 'PLATFORM' ? '/platform/dashboard' : '/merchant/dashboard')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后再试'
  } finally {
    submitting.value = false
  }
}

function particleStyle(index: number) {
  return { left: `${(index * 37) % 94 + 3}%`, top: `${(index * 53) % 90 + 4}%`, animationDelay: `${index * -0.28}s` }
}
</script>
