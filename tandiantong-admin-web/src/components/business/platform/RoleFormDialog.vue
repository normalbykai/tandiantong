<template>
  <el-dialog :model-value="modelValue" :title="role ? '编辑平台角色' : '新增平台角色'" width="500px" @close="emit('update:modelValue', false)">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="82px"><el-form-item label="角色名称" prop="name"><el-input v-model="form.name" maxlength="64" show-word-limit /></el-form-item><el-form-item label="角色标识" prop="roleCode"><el-input v-model="form.roleCode" :disabled="Boolean(role)" placeholder="platfrom_operations" /><p class="field-tip">平台角色固定以 platfrom_ 开头，创建后不可修改。</p></el-form-item><el-form-item label="角色说明"><el-input v-model="form.description" type="textarea" :rows="3" maxlength="255" show-word-limit /></el-form-item></el-form>
    <template #footer><el-button @click="emit('update:modelValue', false)">取消</el-button><el-button type="primary" :loading="submitting" @click="submit">保存</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { PlatformRole, PlatformRoleCommand } from '../../../types/platform-access'
const props = defineProps<{ modelValue: boolean; role?: PlatformRole; submitting?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean]; submit: [value: PlatformRoleCommand] }>()
const formRef = ref<FormInstance>(); const form = reactive<PlatformRoleCommand>({ name: '', roleCode: '', description: '' }); const rules: FormRules = { name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }], roleCode: [{ required: true, pattern: /^platfrom_[a-z0-9_]+$/, message: '请使用 platfrom_ 开头的小写英文标识', trigger: 'blur' }] }
watch(() => [props.modelValue, props.role] as const, () => { if (props.modelValue) { form.name = props.role?.name ?? ''; form.roleCode = props.role?.roleCode ?? ''; form.description = props.role?.description ?? '' } }, { immediate: true })
async function submit() { if (await formRef.value?.validate()) emit('submit', { ...form }) }
</script>

<style scoped>.field-tip { width:100%; margin:6px 0 0; color:var(--el-text-color-secondary); font-size:12px; line-height:1.5; }</style>
