<template>
  <el-dialog :model-value="modelValue" class="role-permission-dialog" width="1080px" top="5vh" :close-on-click-modal="false" @close="emit('update:modelValue', false)">
    <template #header>
      <div class="permission-dialog-title">
        <div><b>配置角色权限</b><span>选择后点击保存，新的权限范围才会生效</span></div>
        <div class="permission-selection-count" :class="{ 'has-changes': hasChanges }"><strong>{{ selectedIds.length }}</strong><span>/ {{ permissions.length }} 项已选</span><em v-if="hasChanges">待保存</em></div>
      </div>
    </template>
    <div v-if="role" class="permission-layout">
      <aside class="role-summary">
        <div class="role-summary-heading"><span>当前角色</span><el-tag class="role-kind" :class="role.systemRole ? 'role-system' : 'role-custom'">{{ role.systemRole ? '系统预置' : '自定义' }}</el-tag></div>
        <h3>{{ role.name }}</h3><code>{{ role.roleCode }}</code>
        <dl><dt>角色说明</dt><dd>{{ role.description || '未填写角色说明' }}</dd><dt>角色状态</dt><dd><el-tag class="status-tag" :class="role.status === 'ENABLED' ? 'status-enabled' : 'status-disabled'">{{ role.status === 'ENABLED' ? '启用中' : '已停用' }}</el-tag></dd></dl>
        <div class="role-summary-tip"><b>授权规则</b><span>保存后将以当前勾选项完整替换该角色已有权限。</span></div>
      </aside>
      <section class="permission-config">
        <header class="permission-config-header"><div><h3>权限范围</h3><p>菜单视图权限控制页面入口，接口业务权限控制具体接口和操作，保存后完整替换当前角色权限</p></div><div class="permission-actions"><el-button size="small" @click="selectAll">全选</el-button><el-button size="small" :disabled="selectedIds.length === 0" @click="clearAll">清空</el-button></div></header>
        <div class="permission-legend"><span><i class="view" />菜单视图权限（VIEW）</span><span><i class="api" />接口业务权限（API）</span></div>
        <div class="permission-groups"><article v-for="group in groups" :key="group.key" class="permission-group"><div class="permission-group-head"><el-checkbox :model-value="isGroupChecked(group)" :indeterminate="isGroupIndeterminate(group)" @click.stop @change="toggleGroup(group, $event)" /><div class="permission-group-title" @click="toggleCollapsed(group.key)"><b>{{ group.label }}</b><small>已配置 {{ selectedGroupCount(group) }} / {{ group.permissions.length }} 项</small></div><el-button text class="collapse-button" @click="toggleCollapsed(group.key)">{{ isCollapsed(group.key) ? '展开' : '收起' }}<ChevronDown :size="15" :class="{ 'is-collapsed': isCollapsed(group.key) }" /></el-button></div><div v-show="!isCollapsed(group.key)" class="permission-columns"><section v-for="type in group.types" :key="type.key" class="permission-column"><header :class="type.key.toLowerCase()"><span>{{ typeLabel(type.key) }}</span><em>{{ type.permissions.length }} 项</em></header><SelectableList v-model="selectedIds" :items="type.permissions.map(permission => ({ value: permission.id, title: permission.name, description: permission.permissionCode }))" empty-text="暂无权限点" /></section></div></article></div>
      </section>
    </div>
    <template #footer><div class="permission-dialog-footer"><span v-if="hasChanges">当前选择尚未保存</span><span v-else>权限范围未发生变化</span><div><el-button @click="emit('update:modelValue', false)">取消</el-button><el-button type="primary" :disabled="!hasChanges" :loading="saving" @click="emit('save', selectedIds)">保存权限配置</el-button></div></div></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ChevronDown } from 'lucide-vue-next'
import SelectableList from '../../common/SelectableList.vue'
import type { PlatformPermission, PlatformRole } from '../../../types/platform-access'

const props = defineProps<{ modelValue: boolean; role?: PlatformRole; permissions: PlatformPermission[]; permissionIds: number[]; saving?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean]; save: [ids: number[]] }>()
const selectedIds = ref<number[]>([])
const collapsedGroupKeys = ref<string[]>([])
const hasChanges = computed(() => selectedIds.value.length !== props.permissionIds.length || selectedIds.value.some(id => !props.permissionIds.includes(id)))
const groups = computed(() => Object.entries(props.permissions.reduce<Record<string, PlatformPermission[]>>((result, permission) => { const module = permissionModuleKey(permission); (result[module] ??= []).push(permission); return result }, {})).map(([key, permissions]) => ({ key, label: moduleLabel(key), permissions, types: [{ key: 'VIEW', permissions: permissions.filter(permission => permission.permissionType === 'VIEW') }, { key: 'API_READ', permissions: permissions.filter(permission => permission.permissionType === 'API' && permission.permissionCode.endsWith(':read')) }, { key: 'API_ACTION', permissions: permissions.filter(permission => permission.permissionType === 'API' && !permission.permissionCode.endsWith(':read')) }] })))
function permissionModuleKey(permission: PlatformPermission) {
  const parts = permission.permissionCode.split(':')
  if (parts[1] === 'system' || parts[1] === 'dictionary') return 'system'
  if (parts[1] === 'access') return parts[2] ?? 'access'
  return parts[1] ?? 'other'
}
function moduleLabel(value: string) { return ({ account: '平台账号', role: '平台角色', permission: '平台权限', merchant: '租户管理', 'operation-log': '操作日志', system: '系统管理' } as Record<string, string>)[value] ?? '其他权限' }
function typeLabel(value: string) { return ({ VIEW: '菜单视图权限', API_READ: '接口查询权限', API_ACTION: '接口业务权限' } as Record<string, string>)[value] ?? value }
watch(() => [props.modelValue, props.permissionIds, groups.value] as const, () => { if (props.modelValue) { selectedIds.value = [...props.permissionIds]; collapsedGroupKeys.value = groups.value.map(group => group.key) } }, { immediate: true })
function selectedGroupCount(group: { permissions: PlatformPermission[] }) { return group.permissions.filter(item => selectedIds.value.includes(item.id)).length }
function isGroupChecked(group: { permissions: PlatformPermission[] }) { return group.permissions.length > 0 && selectedGroupCount(group) === group.permissions.length }
function isGroupIndeterminate(group: { permissions: PlatformPermission[] }) { const count = selectedGroupCount(group); return count > 0 && count < group.permissions.length }
function toggleGroup(group: { permissions: PlatformPermission[] }, checked: boolean | string | number) { const ids = group.permissions.map(item => item.id); selectedIds.value = checked === true ? [...new Set([...selectedIds.value, ...ids])] : selectedIds.value.filter(id => !ids.includes(id)) }
function isCollapsed(groupKey: string) { return collapsedGroupKeys.value.includes(groupKey) }
function toggleCollapsed(groupKey: string) { collapsedGroupKeys.value = isCollapsed(groupKey) ? collapsedGroupKeys.value.filter(key => key !== groupKey) : [...collapsedGroupKeys.value, groupKey] }
function selectAll() { selectedIds.value = props.permissions.map(item => item.id) }
function clearAll() { selectedIds.value = [] }
</script>

<style scoped>
.permission-dialog-title,.permission-dialog-footer,.permission-config-header,.role-summary-heading,.permission-group-head { display:flex; align-items:center; justify-content:space-between; }
.permission-dialog-title { padding-right:24px; }.permission-dialog-title > div:first-child { display:grid; gap:4px; }.permission-dialog-title b { font-size:18px; }.permission-dialog-title span { color:var(--el-text-color-secondary); font-size:12px; }
.permission-selection-count { display:flex; align-items:baseline; gap:4px; color:#73767a; }.permission-selection-count strong { color:var(--domain-700); font-size:22px; }.permission-selection-count em { margin-left:6px; padding:2px 6px; border-radius:4px; background:#fff2df; color:#9b5b10; font-size:11px; font-style:normal; }.permission-selection-count.has-changes strong { color:#b45b16; }
.permission-layout { display:grid; grid-template-columns:238px minmax(0,1fr); gap:16px; }.role-summary,.permission-config { border:1px solid #e1e4e9; border-radius:12px; background:#fff; }.role-summary { padding:18px; background:linear-gradient(165deg,#fbfcfc,#f4f8f6); }.role-summary-heading span { color:#73767a; font-size:12px; }.role-summary h3 { margin:16px 0 5px; color:#25282b; font-size:18px; }.role-summary > code { color:#656b70; font:11px ui-monospace,Consolas,monospace; }.role-summary dl { display:grid; gap:6px; margin:22px 0; }.role-summary dt { margin-top:8px; color:#8e8e90; font-size:11px; }.role-summary dd { margin:0; color:#303033; font-size:13px; line-height:1.55; word-break:break-word; }.role-summary-tip { display:grid; gap:4px; padding:10px; border:1px solid #cfe0d7; border-radius:8px; background:var(--domain-50); color:var(--domain-700); font-size:12px; line-height:1.6; }.permission-config-header { padding:16px 18px 13px; }.permission-config h3 { margin:0; font-size:15px; }.permission-config p { margin:4px 0 0; color:#8e8e90; font-size:11px; }.permission-actions { display:flex; gap:8px; }.permission-legend { display:flex; gap:16px; padding:10px 18px; border-top:1px solid #ebeef1; border-bottom:1px solid #ebeef1; color:#5a5a5c; font-size:11px; }.permission-legend span { display:flex; align-items:center; gap:6px; }.permission-legend i { width:7px; height:7px; border-radius:50%; }.permission-legend .menu { background:#a6762b; }.permission-legend .button { background:#3a6b9c; }.permission-legend .api { background:#73508f; }.permission-groups { max-height:52vh; overflow:auto; padding:12px 18px 18px; }.permission-group { margin-bottom:12px; border:1px solid #e1e4e9; border-radius:9px; overflow:hidden; }.permission-group:last-child { margin-bottom:0; }.permission-group-head { justify-content:flex-start; gap:9px; min-height:48px; padding:0 13px; background:#f8faf9; }.permission-group-head > div { display:grid; gap:2px; }.permission-group-head b { color:#303033; font-size:13px; }.permission-group-head small { color:#8e8e90; font-size:11px; }.permission-columns { display:grid; grid-template-columns:repeat(3,1fr); }.permission-column { min-height:88px; border-right:1px solid #ebeef1; }.permission-column:last-child { border-right:0; }.permission-column header { display:flex; align-items:center; justify-content:space-between; padding:9px 10px; font-size:11px; font-weight:600; }.permission-column header.menu { color:#8b5a17; background:#fbf3e4; }.permission-column header.button { color:#2a5687; background:#eef3f8; }.permission-column header.api { color:#5a4080; background:#f3eef7; }.permission-column em { font-style:normal; opacity:.7; }.permission-dialog-footer { width:100%; color:#8b5b1b; font-size:12px; }.permission-dialog-footer > span:not(:first-child) { color:#8e8e90; }.permission-dialog-footer > div { display:flex; gap:12px; }.role-kind { border-radius:5px; padding:2px 7px; font-size:11px; }.role-system { color:#8b5a17; border-color:#ead3a8; background:#fbf3e4; }.role-custom { color:var(--domain-700); border-color:#cfe0d7; background:var(--domain-50); } @media (max-width:800px) { .permission-layout,.permission-columns { grid-template-columns:1fr; }.role-summary { padding:14px; }.permission-column { border-right:0; border-bottom:1px solid #ebeef1; }.permission-groups { max-height:42vh; }.permission-dialog-footer { align-items:flex-end; flex-direction:column; }.permission-selection-count { display:none; } }
.permission-group-title { display:grid; flex:1; gap:2px; cursor:pointer; }
.collapse-button { gap:3px; color:#5f666d; font-size:12px; }
.collapse-button svg { transition:transform .18s ease; }
.collapse-button svg.is-collapsed { transform:rotate(-90deg); }
</style>
