<template>
  <el-dialog :model-value="modelValue" title="开通商户租户" width="min(680px, calc(100vw - 32px))" destroy-on-close @update:model-value="$emit('update:modelValue', $event)">
    <p class="dialog-description">开通后将创建默认门店、首位管理员邀请和小程序入口码。</p>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="商户名称" prop="merchantName"><el-input v-model="form.merchantName" placeholder="例如：湖滨小吃铺" /></el-form-item>
      <el-form-item label="默认门店地址" prop="storeAddress"><el-input v-model="form.storeAddress" placeholder="例如：杭州市西湖区湖滨路 18 号" /></el-form-item>
      <div class="form-grid"><el-form-item label="管理员姓名" prop="adminName"><el-input v-model="form.adminName" placeholder="例如：张店长" /></el-form-item><el-form-item label="管理员手机号" prop="adminMobile"><el-input v-model="form.adminMobile" placeholder="请输入手机号" /></el-form-item></div>
    </el-form>
    <template #footer><el-button @click="$emit('update:modelValue', false)">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">确认开通</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { CreateTenantCommand } from '../../../types/tenant'

const emit = defineEmits<{ 'update:modelValue': [value: boolean]; submit: [value: CreateTenantCommand] }>()
defineProps<{ modelValue: boolean; submitting: boolean }>()
const formRef = ref<FormInstance>()
const form = reactive<CreateTenantCommand>({ merchantName: '', storeAddress: '', adminName: '', adminMobile: '' })
const rules: FormRules = { merchantName: [{ required: true, message: '请输入商户名称', trigger: 'blur' }], storeAddress: [{ required: true, message: '请输入门店地址', trigger: 'blur' }], adminName: [{ required: true, message: '请输入管理员姓名', trigger: 'blur' }], adminMobile: [{ required: true, pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }] }
async function submit() { if (await formRef.value?.validate().catch(() => false)) emit('submit', { ...form }) }
</script>
