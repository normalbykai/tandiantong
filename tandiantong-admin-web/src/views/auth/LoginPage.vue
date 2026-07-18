<template>
  <main class="login-page" :class="{ merchant: domain === 'TENANT' }">
    <section class="login-panel">
      <div class="login-card" :class="{ merchant: domain === 'TENANT' }">
        <header class="login-brand">
          <span class="login-mark-icon"><img src="/assets/tandiantong-logo-mark-v4.svg" alt="摊点通" /></span>
          <h1>摊点通</h1>
        </header>
        <el-radio-group v-model="domain" class="domain-switch" aria-label="登录入口选择">
          <el-radio-button label="PLATFORM">平台管理端</el-radio-button>
          <el-radio-button label="TENANT">商户管理端</el-radio-button>
        </el-radio-group>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form" @submit.prevent="submit">
          <el-form-item prop="mobile" class="login-input-item">
            <FloatingLabelInput v-model="form.mobile" label="账号" autocomplete="username" />
          </el-form-item>
          <el-form-item prop="password" class="login-input-item">
            <FloatingLabelInput v-model="form.password" label="密码" type="password" show-password autocomplete="current-password" />
          </el-form-item>
          <el-alert v-if="errorMessage" :title="errorMessage" type="error" :closable="false" show-icon />
          <div class="login-options"><el-checkbox v-model="remember">7 天内自动登录</el-checkbox><RouterLink v-if="domain === 'TENANT'" class="login-text-link" to="/merchant/activate">首次开通？激活账号</RouterLink><el-button v-else text>忘记密码？</el-button></div>
          <el-button native-type="submit" type="primary" size="large" :loading="submitting" class="login-submit">登录</el-button>
        </el-form>
        <p class="login-notice">© 2026 摊点通</p>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import FloatingLabelInput from '../../components/common/FloatingLabelInput.vue'
import { login } from '../../api/auth'
import { useSession } from '../../stores/session'
import type { AccessDomain } from '../../types/auth'

const router = useRouter()
const route = useRoute()
const { signIn } = useSession()
const formRef = ref<FormInstance>()
const domain = ref<AccessDomain>(route.query.domain === 'TENANT' ? 'TENANT' : 'PLATFORM')
const form = reactive({ mobile: 'admin', password: 'admin' })
const submitting = ref(false)
const errorMessage = ref('')
const remember = ref(true)
const rules: FormRules = { mobile: [{ required: true, message: '请输入手机号或登录账号', trigger: 'blur' }], password: [{ required: true, message: '请输入密码', trigger: 'blur' }] }

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  errorMessage.value = ''
  try {
    const result = await login(domain.value, form.mobile, form.password, remember.value)
    signIn(result.accessToken, result.domain, result.displayName, remember.value)
    await router.replace(result.domain === 'PLATFORM' ? '/platform/dashboard' : '/merchant/dashboard')
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后再试'
  } finally {
    submitting.value = false
  }
}

</script>
