<template>
  <el-dialog :model-value="modelValue" :title="account ? '编辑平台账号' : '新增平台账号'" width="520px" @close="close">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
      <el-form-item label="登录手机号" prop="mobile"><el-input v-model="form.mobile" :disabled="Boolean(account)" placeholder="请输入11位手机号" /></el-form-item>
      <el-form-item label="账号名称" prop="displayName"><el-input v-model="form.displayName" placeholder="如：王运营" /></el-form-item>
      <el-form-item v-if="!account" label="初始密码" prop="password"><el-input v-model="form.password" type="password" show-password placeholder="至少8位" /></el-form-item>
      <el-form-item label="平台角色" prop="roleIds"><el-select v-model="form.roleIds" multiple collapse-tags placeholder="请选择角色" style="width:100%"><el-option v-for="role in roles.filter(item => item.status === 'ENABLED')" :key="role.id" :label="role.name" :value="role.id" /></el-select></el-form-item>
    </el-form>
    <template #footer><el-button @click="close">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">保存</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { CreatePlatformAccountCommand, PlatformAccount, PlatformRole, UpdatePlatformAccountCommand } from '../../../types/platform-access'

const props = defineProps<{ modelValue: boolean; account?: PlatformAccount; roles: PlatformRole[]; submitting?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean]; submit: [value: CreatePlatformAccountCommand | UpdatePlatformAccountCommand] }>()
const formRef = ref<FormInstance>()
const form = reactive<CreatePlatformAccountCommand>({ mobile: '', displayName: '', password: '', roleIds: [] })
const rules: FormRules = { mobile: [{ required: true, pattern: /^1\d{10}$/, message: '请输入正确手机号', trigger: 'blur' }], displayName: [{ required: true, message: '请输入账号名称', trigger: 'blur' }], password: [{ required: true, min: 8, message: '密码至少8位', trigger: 'blur' }], roleIds: [{ required: true, type: 'array', min: 1, message: '请选择至少一个角色', trigger: 'change' }] }
watch(() => [props.modelValue, props.account] as const, () => { if (!props.modelValue) return; form.mobile = props.account?.mobile ?? ''; form.displayName = props.account?.displayName ?? ''; form.password = ''; form.roleIds = [...(props.account?.roleIds ?? [])] }, { immediate: true })
function close() { emit('update:modelValue', false) }
async function submit() { if (!(await formRef.value?.validate())) return; emit('submit', props.account ? { displayName: form.displayName, roleIds: form.roleIds } : { ...form }) }
</script>
