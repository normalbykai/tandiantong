<template>
  <ListPageLayout title="平台角色" description="配置平台职责角色和权限范围；预设角色和自定义角色均可按授权规则维护。" eyebrow="权限管理">
    <template #stats>
      <div class="list-stat"><span class="list-stat__label">全部角色</span><strong class="list-stat__value">{{ roles.length }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">系统预置</span><strong class="list-stat__value is-success">{{ systemRoleCount }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">自定义</span><strong class="list-stat__value is-muted">{{ customRoleCount }}</strong></div>
    </template>

    <template #actions>
      <el-button type="primary" @click="openCreate">新增角色</el-button>
      <el-button :icon="RefreshCw" :loading="loading" @click="load">刷新列表</el-button>
    </template>

    <template #filters>
      <div class="page-filter-field"><el-input v-model="keyword" :prefix-icon="Search" clearable placeholder="搜索角色名称或标识" @keyup.enter="load" /></div>
      <span class="page-filter-result">查询结果 <b>{{ filteredRoles.length }}</b> 条</span>
    </template>

    <AppDataTable :data="filteredRoles" :loading="loading" row-key="id" empty-text="暂无平台角色" :total="filteredRoles.length">
      <el-table-column label="角色名称" min-width="160">
        <template #default="{ row }"><b class="primary-cell">{{ row.name }}</b></template>
      </el-table-column>
      <el-table-column label="角色标识" min-width="230">
        <template #default="{ row }"><code>{{ row.roleCode }}</code></template>
      </el-table-column>
      <el-table-column prop="description" label="角色说明" min-width="260" show-overflow-tooltip />
      <el-table-column label="角色类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.systemRole ? '' : 'info'">{{ row.systemRole ? '系统预置' : '自定义' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag class="status-tag" :class="row.status === 'ENABLED' ? 'status-enabled' : 'status-disabled'">{{ row.status === 'ENABLED' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="managePermissions(row)">配置权限</el-button>
          <el-button link @click="openEdit(row)">编辑</el-button>
          <el-button link :type="row.status === 'ENABLED' ? 'danger' : 'primary'" @click="toggleStatus(row)">{{ row.status === 'ENABLED' ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </AppDataTable>
  </ListPageLayout>
  <RoleFormDialog v-model="dialogVisible" :role="editingRole" :submitting="saving" @submit="save" />
  <RolePermissionDialog v-model="permissionDialogVisible" :role="permissionRole" :permissions="permissions" :permission-ids="selectedPermissionIds" :saving="savingPermissions" @save="savePermissions" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { RefreshCw, Search } from 'lucide-vue-next'
import ListPageLayout from '../../components/common/ListPageLayout.vue'
import AppDataTable from '../../components/common/AppDataTable.vue'
import RoleFormDialog from '../../components/business/platform/RoleFormDialog.vue'
import RolePermissionDialog from '../../components/business/platform/RolePermissionDialog.vue'
import { createPlatformRole, listPlatformPermissionOptions, listPlatformRolePermissionIds, listPlatformRoles, replacePlatformRolePermissions, updatePlatformRole, updatePlatformRoleStatus } from '../../api/platform/access'
import type { PlatformPermission, PlatformRole, PlatformRoleCommand, UpdatePlatformRoleCommand } from '../../types/platform-access'
import { message } from '../../utils/message'

const roles = ref<PlatformRole[]>([]); const permissions = ref<PlatformPermission[]>([]); const loading = ref(false); const saving = ref(false); const savingPermissions = ref(false); const keyword = ref(''); const dialogVisible = ref(false); const editingRole = ref<PlatformRole>(); const permissionDialogVisible = ref(false); const permissionRole = ref<PlatformRole>(); const selectedPermissionIds = ref<number[]>([])
const systemRoleCount = computed(() => roles.value.filter(item => item.systemRole).length)
const customRoleCount = computed(() => roles.value.filter(item => !item.systemRole).length)
const filteredRoles = computed(() => { const value = keyword.value.trim(); return value ? roles.value.filter(item => `${item.name}${item.roleCode}${item.description ?? ''}`.includes(value)) : roles.value })
async function load() { loading.value = true; try { const [roleData, permissionData] = await Promise.all([listPlatformRoles(), listPlatformPermissionOptions()]); roles.value = roleData; permissions.value = permissionData } catch (error) { message.error(error instanceof Error ? error.message : '平台角色加载失败') } finally { loading.value = false } }
function openCreate() { editingRole.value = undefined; dialogVisible.value = true }
function openEdit(role: PlatformRole) { editingRole.value = role; dialogVisible.value = true }
async function save(command: PlatformRoleCommand) { saving.value = true; try { if (editingRole.value) { const updateCommand: UpdatePlatformRoleCommand = { name: command.name, description: command.description }; await updatePlatformRole(editingRole.value.id, updateCommand) } else await createPlatformRole(command); message.success('平台角色已保存'); dialogVisible.value = false; await load() } catch (error) { message.error(error instanceof Error ? error.message : '平台角色保存失败') } finally { saving.value = false } }
async function toggleStatus(role: PlatformRole) { const enabled = role.status !== 'ENABLED'; try { await ElMessageBox.confirm(`确认${enabled ? '启用' : '停用'}“${role.name}”吗？`, '角色状态确认', { type: 'warning' }); await updatePlatformRoleStatus(role.id, enabled); message.success('角色状态已更新'); await load() } catch (error) { if (error !== 'cancel') message.error(error instanceof Error ? error.message : '角色状态更新失败') } }
async function managePermissions(role: PlatformRole) { permissionRole.value = role; try { selectedPermissionIds.value = await listPlatformRolePermissionIds(role.id); permissionDialogVisible.value = true } catch (error) { message.error(error instanceof Error ? error.message : '角色权限加载失败') } }
async function savePermissions(permissionIds: number[]) { if (!permissionRole.value) return; savingPermissions.value = true; try { await replacePlatformRolePermissions(permissionRole.value.id, permissionIds); selectedPermissionIds.value = permissionIds; message.success('角色权限已保存'); permissionDialogVisible.value = false } catch (error) { message.error(error instanceof Error ? error.message : '角色权限保存失败') } finally { savingPermissions.value = false } }
onMounted(load)
</script>

<style scoped>
.page-filter-field { width: min(420px, 100%); }
.page-filter-result { margin-left: auto; color: #89958e; font-size: 12px; }
.page-filter-result b { color: var(--domain-700); font-weight: 650; }

:deep(.el-table__header th.el-table__cell) { font-size: 12px; font-weight: 600; color: #5e6b64; }
:deep(.el-table__header th.el-table__cell:first-child .cell) { padding-left: 16px; }
:deep(.el-table__body td.el-table__cell) { font-size: 13.5px; padding: 14px 0; color: #2d3831; }
:deep(.el-table__body td.el-table__cell:first-child .cell) { padding-left: 16px; }
:deep(.el-table__body td.el-table__cell .cell) { line-height: 1.6; }
:deep(.primary-cell) { font-size: 14px; }

@media (max-width: 680px) {
  .page-filter-field { width: 100%; }
  .page-filter-result { width: 100%; margin-left: 0; }
  :deep(.el-table__header th.el-table__cell:first-child .cell),
  :deep(.el-table__body td.el-table__cell:first-child .cell) { padding-left: 12px; }
}
</style>
