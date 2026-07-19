<template>
  <main class="login-page merchant">
    <section class="login-panel">
      <div class="login-card merchant activation-card">
        <header class="login-brand">
          <span class="login-mark-icon"><img src="/assets/tandiantong-logo-mark-v4.svg" alt="摊点通" /></span>
          <h1>激活商户管理员</h1>
        </header>
        <p class="activation-description">请输入平台提供的邀请码，并设置商户管理端的首次登录密码。</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form" @submit.prevent="submit">
          <el-form-item prop="invitationCode" class="login-input-item">
            <FloatingLabelInput v-model="form.invitationCode" label="管理员邀请码" autocomplete="off" />
          </el-form-item>
          <el-form-item prop="password" class="login-input-item">
            <FloatingLabelInput v-model="form.password" label="设置登录密码" type="password" show-password autocomplete="new-password" />
          </el-form-item>
          <el-form-item prop="confirmPassword" class="login-input-item">
            <FloatingLabelInput v-model="form.confirmPassword" label="确认登录密码" type="password" show-password autocomplete="new-password" @keyup.enter="submit" />
          </el-form-item>
          <el-alert v-if="errorMessage" :title="errorMessage" type="error" :closable="false" show-icon />
          <p class="activation-hint">密码至少 8 位。邀请码仅可使用一次，请妥善保管。</p>
          <el-button native-type="submit" type="primary" size="large" :loading="submitting" class="login-submit">完成激活</el-button>
        </el-form>
        <p class="login-notice"><RouterLink class="login-text-link" to="/login">已有账号，返回登录</RouterLink></p>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import FloatingLabelInput from '../../components/common/FloatingLabelInput.vue'
import { activateMerchantAdmin } from '../../api/auth'
import { message } from '../../utils/message'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const invitationCode = typeof route.query.invitationCode === 'string' ? route.query.invitationCode : ''
const form = reactive({ invitationCode, password: '', confirmPassword: '' })
const submitting = ref(false)
const errorMessage = ref('')
const rules: FormRules = {
  invitationCode: [{ required: true, message: '请输入管理员邀请码', trigger: 'blur' }],
  password: [
    { required: true, message: '请设置登录密码', trigger: 'blur' },
    { min: 8, message: '密码至少需要 8 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入登录密码', trigger: 'blur' },
    { validator: (_rule, value, callback) => callback(value === form.password ? undefined : new Error('两次输入的密码不一致')), trigger: 'blur' }
  ]
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  errorMessage.value = ''
  try {
    await activateMerchantAdmin(form.invitationCode.trim(), form.password)
    message.success('管理员账号已激活，请使用预留手机号和新密码登录')
    await router.replace({ path: '/login', query: { domain: 'TENANT' } })
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '激活失败，请稍后再试'
  } finally {
    submitting.value = false
  }
}
</script>
