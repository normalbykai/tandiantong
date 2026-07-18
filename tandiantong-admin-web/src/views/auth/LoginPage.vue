<template>
  <main class="login-page">
    <div class="aurora aurora-one" /><div class="aurora aurora-two" />
    <section class="login-card">
      <img src="/assets/tandiantong-logo-horizontal-reverse-v4.svg" alt="摊点通" class="login-logo" />
      <div class="login-heading"><span>欢迎使用</span><h1>摊点通基础设施系统</h1><p>统一管理商户、账号、角色与权限</p></div>
      <el-segmented v-model="domain" :options="domainOptions" block />
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form" @submit.prevent="submit">
        <el-form-item label="手机号" prop="mobile"><el-input v-model="form.mobile" placeholder="请输入手机号" autocomplete="username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" placeholder="请输入密码" show-password autocomplete="current-password" /></el-form-item>
        <el-alert v-if="errorMessage" :title="errorMessage" type="error" :closable="false" show-icon />
        <el-button native-type="submit" type="primary" size="large" :loading="submitting" class="login-submit">登录</el-button>
      </el-form>
      <p class="login-notice">登录即表示你同意系统安全与审计规则</p>
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
const domainOptions = [{ label: '平台管理', value: 'PLATFORM' }, { label: '商户管理', value: 'TENANT' }]
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
</script>
