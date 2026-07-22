<template>
  <ListPageLayout title="平台账号" description="维护平台运营账号及所属角色；账号状态和敏感操作由服务端再次鉴权。" eyebrow="权限管理">
    <template #stats>
      <div class="list-stat"><span class="list-stat__label">全部账号</span><strong class="list-stat__value">{{ accounts.length }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">已启用</span><strong class="list-stat__value is-success">{{ enabledCount }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">已停用</span><strong class="list-stat__value is-muted">{{ disabledCount }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">临时锁定</span><strong class="list-stat__value is-warning">{{ lockedCount }}</strong></div>
    </template>

    <template #actions>
      <el-button v-if="hasPermission('platform:account:create')" type="primary" @click="openCreate">新增账号</el-button>
      <el-button :icon="RefreshCw" :loading="loading" @click="load">刷新列表</el-button>
    </template>

    <template #filters>
      <div class="page-filter-field"><el-input v-model="keyword" :prefix-icon="Search" clearable placeholder="搜索账号名称或手机号" @keyup.enter="load" /></div>
      <el-select v-model="statusFilter" class="page-filter-select" clearable placeholder="账号状态"><el-option label="已启用" value="ENABLED" /><el-option label="已停用" value="DISABLED" /><el-option label="临时锁定" value="LOCKED" /></el-select>
      <el-select v-model="roleFilter" class="page-filter-select" clearable placeholder="所属角色"><el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" /></el-select>
      <el-button @click="resetFilters">重置</el-button>
      <span class="page-filter-result">查询结果 <b>{{ filteredAccounts.length }}</b> 条</span>
    </template>

    <AppDataTable :data="filteredAccounts" :loading="loading" row-key="id" empty-text="暂无平台账号" :total="filteredAccounts.length">
      <el-table-column label="账号名称" min-width="170">
        <template #default="{ row }">
          <div class="account-name"><span>{{ row.displayName.slice(0, 1) }}</span><b>{{ row.displayName }}</b></div>
        </template>
      </el-table-column>
      <el-table-column prop="mobile" label="手机号" min-width="150" />
      <el-table-column label="所属角色" min-width="220">
        <template #default="{ row }">
          <el-tag v-for="id in row.roleIds" :key="id" class="role-tag" effect="plain">{{ roleName(id) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag class="status-tag" :class="accountStatusClass(row)">{{ accountStatusLabel(row) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="180">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="最近登录" min-width="180">
        <template #default="{ row }">
          <div class="login-status-cell"><span>{{ formatTime(row.lastLoginAt) }}</span><small v-if="row.lockedUntil">锁定至 {{ formatTime(row.lockedUntil) }}</small><small v-else-if="row.failedLoginCount">连续失败 {{ row.failedLoginCount }} 次</small></div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="hasPermission('platform:account:update')" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="hasPermission('platform:account:password:reset')" link @click="resetPassword(row)">重置密码</el-button>
          <el-button v-if="hasPermission('platform:account:status:update') && isLocked(row)" link type="warning" @click="unlock(row)">解除锁定</el-button><el-button v-if="hasPermission('platform:account:status:update')" link :type="row.status === 'ENABLED' ? 'danger' : 'primary'" @click="toggleStatus(row)">{{ row.status === 'ENABLED' ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </AppDataTable>
  </ListPageLayout>
  <AccountFormDialog v-model="dialogVisible" :account="editingAccount" :roles="roles" :submitting="saving" @submit="save" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { RefreshCw, Search } from 'lucide-vue-next'
import ListPageLayout from '../../components/common/ListPageLayout.vue'
import AppDataTable from '../../components/common/AppDataTable.vue'
import AccountFormDialog from '../../components/business/platform/AccountFormDialog.vue'
import { createPlatformAccount, listPlatformAccounts, listPlatformRoles, resetPlatformAccountPassword, unlockPlatformAccount, updatePlatformAccount, updatePlatformAccountStatus } from '../../api/platform/access'
import type { CreatePlatformAccountCommand, PlatformAccount, PlatformRole, UpdatePlatformAccountCommand } from '../../types/platform-access'
import { message } from '../../utils/message'
import { useSession } from '../../stores/session'

const accounts = ref<PlatformAccount[]>([]); const roles = ref<PlatformRole[]>([]); const loading = ref(false); const saving = ref(false); const keyword = ref(''); const statusFilter = ref<PlatformAccount['status'] | 'LOCKED'>(); const roleFilter = ref<number>(); const dialogVisible = ref(false); const editingAccount = ref<PlatformAccount>()
const hasPermission = useSession().hasPermission
const isLocked = (account: PlatformAccount) => Boolean(account.lockedUntil && new Date(account.lockedUntil).getTime() > Date.now())
const enabledCount = computed(() => accounts.value.filter(item => item.status === 'ENABLED' && !isLocked(item)).length)
const disabledCount = computed(() => accounts.value.filter(item => item.status === 'DISABLED').length)
const lockedCount = computed(() => accounts.value.filter(isLocked).length)
const accountStatusLabel = (account: PlatformAccount) => isLocked(account) ? '已锁定' : account.status === 'ENABLED' ? '启用' : '停用'
const accountStatusClass = (account: PlatformAccount) => isLocked(account) ? 'status-closed' : account.status === 'ENABLED' ? 'status-enabled' : 'status-disabled'
const filteredAccounts = computed(() => accounts.value.filter(item => { const value = keyword.value.trim(); const statusMatched = !statusFilter.value || statusFilter.value === (isLocked(item) ? 'LOCKED' : item.status); return (!value || `${item.displayName}${item.mobile}`.includes(value)) && statusMatched && (!roleFilter.value || item.roleIds.includes(roleFilter.value)) }))
const roleName = (id: number) => roles.value.find(role => role.id === id)?.name ?? `角色 #${id}`
const formatTime = (value?: string) => value ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short', hour12: false }).format(new Date(value)) : '从未登录'
async function load() { loading.value = true; try { const [accountData, roleData] = await Promise.all([listPlatformAccounts(), listPlatformRoles()]); accounts.value = accountData; roles.value = roleData } catch (error) { message.error(error instanceof Error ? error.message : '平台账号加载失败') } finally { loading.value = false } }
function openCreate() { editingAccount.value = undefined; dialogVisible.value = true }
function resetFilters() { keyword.value = ''; statusFilter.value = undefined; roleFilter.value = undefined }
function openEdit(account: PlatformAccount) { editingAccount.value = account; dialogVisible.value = true }
async function save(command: CreatePlatformAccountCommand | UpdatePlatformAccountCommand) { saving.value = true; try { if (editingAccount.value) await updatePlatformAccount(editingAccount.value.id, command as UpdatePlatformAccountCommand); else await createPlatformAccount(command as CreatePlatformAccountCommand); message.success('平台账号已保存'); dialogVisible.value = false; await load() } catch (error) { message.error(error instanceof Error ? error.message : '平台账号保存失败') } finally { saving.value = false } }
async function toggleStatus(account: PlatformAccount) { const enabled = account.status !== 'ENABLED'; try { await ElMessageBox.confirm(`确认${enabled ? '启用' : '停用'}“${account.displayName}”吗？`, '账号状态确认', { type: 'warning' }); await updatePlatformAccountStatus(account.id, enabled); message.success('账号状态已更新'); await load() } catch (error) { if (error !== 'cancel') message.error(error instanceof Error ? error.message : '账号状态更新失败') } }
async function unlock(account: PlatformAccount) { try { await ElMessageBox.confirm(`确认解除“${account.displayName}”的临时登录锁定吗？系统会清零登录失败次数。`, '解除账号锁定', { type: 'warning' }); await unlockPlatformAccount(account.id); message.success('账号锁定已解除'); await load() } catch (error) { if (error !== 'cancel') message.error(error instanceof Error ? error.message : '账号锁定解除失败') } }
async function resetPassword(account: PlatformAccount) { try { await ElMessageBox.confirm(`确认按系统安全策略重置“${account.displayName}”的密码吗？原登录状态会立即失效。`, '重置平台账号密码', { type: 'warning' }); const result = await resetPlatformAccountPassword(account.id); if (result.mode === 'RANDOM' && result.temporaryPassword) await ElMessageBox.alert(`本次临时密码：${result.temporaryPassword}\n\n该密码仅展示本次，请立即复制并交给账号使用者。`, '密码已重置', { confirmButtonText: '我已记录' }); else message.success('密码已按固定策略重置，历史登录状态已失效') } catch (error) { if (error !== 'cancel') message.error(error instanceof Error ? error.message : '密码重置失败') } }
onMounted(load)
</script>

<style scoped>
.role-tag + .role-tag { margin-left: 6px; }
.account-name { display: flex; align-items: center; gap: 9px; }
.account-name span { display: grid; place-items: center; width: 30px; height: 30px; border-radius: 50%; color: #fff; font-size: 12px; font-weight: 600; background: linear-gradient(135deg, var(--domain-700), var(--domain-500)); }
.account-name b { font-size: 14px; font-weight: 500; }
.login-status-cell { display: grid; gap: 3px; }.login-status-cell small { color: #b23a3a; font-size: 11px; }

.page-filter-field { width: min(420px, 100%); }
.page-filter-select { width: 150px; }
.page-filter-result { margin-left: auto; color: #89958e; font-size: 12px; }
.page-filter-result b { color: var(--domain-700); font-weight: 650; }

:deep(.el-table__header th.el-table__cell) { font-size: 12px; font-weight: 600; color: #5e6b64; }
:deep(.el-table__header th.el-table__cell:first-child .cell) { padding-left: 16px; }
:deep(.el-table__body td.el-table__cell) { font-size: 13.5px; padding: 14px 0; color: #2d3831; }
:deep(.el-table__body td.el-table__cell:first-child .cell) { padding-left: 16px; }
:deep(.el-table__body td.el-table__cell .cell) { line-height: 1.6; }

@media (max-width: 680px) {
  .page-filter-field, .page-filter-select { width: 100%; }
  .page-filter-result { width: 100%; margin-left: 0; }
  :deep(.el-table__header th.el-table__cell:first-child .cell),
  :deep(.el-table__body td.el-table__cell:first-child .cell) { padding-left: 12px; }
}
</style>
