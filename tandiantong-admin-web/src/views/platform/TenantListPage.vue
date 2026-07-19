<template>
  <ListPageLayout title="租户管理" description="开通、启停商户并维护待激活管理员的邀请码；所有操作均以平台权限域执行。">
    <template #stats>
      <div class="list-stat"><span class="list-stat__label">全部商户</span><strong class="list-stat__value">{{ tenants.length }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">已启用</span><strong class="list-stat__value is-success">{{ enabledTenantCount }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">待启用</span><strong class="list-stat__value is-warning">{{ pendingTenantCount }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">已停用</span><strong class="list-stat__value is-muted">{{ disabledTenantCount }}</strong></div>
    </template>

    <template #actions>
      <el-button v-if="hasPermission('platform:merchant:create')" type="primary" @click="dialogVisible = true">开通商户</el-button>
      <el-button :icon="RefreshCw" :loading="loading" @click="load">刷新列表</el-button>
    </template>

    <template #filters>
      <div class="tenant-filter-field"><el-input v-model="keywordInput" placeholder="搜索商户名称、管理员或联系电话" :prefix-icon="Search" clearable @keyup.enter="applyFilters" /></div>
      <el-select v-model="tenantStatusFilter" class="tenant-filter-select" clearable placeholder="租户状态"><el-option label="已启用" value="ENABLED" /><el-option label="待启用" value="PENDING" /><el-option label="已停用" value="DISABLED" /></el-select>
      <el-select v-model="adminStatusFilter" class="tenant-filter-select" clearable placeholder="管理员状态"><el-option label="已激活" value="ACTIVATED" /><el-option label="待激活" value="PENDING" /></el-select>
      <el-button type="primary" :icon="Search" :loading="loading" @click="applyFilters">搜索</el-button>
      <span class="tenant-filter-result">查询结果 <b>{{ tenants.length }}</b> 条</span>
    </template>

    <AppDataTable :data="pagedTenants" :loading="loading" row-key="tenantId" empty-text="暂无商户租户" :total="tenants.length" :current-page="currentPage" :page-size="pageSize" show-pagination @page-change="currentPage = $event" @page-size-change="changePageSize"><el-table-column prop="merchantName" label="商户名称" min-width="180" /><el-table-column prop="adminName" label="管理员" min-width="120" /><el-table-column prop="adminMobileMasked" label="联系电话" min-width="140" /><el-table-column label="租户状态" min-width="120"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag></template></el-table-column><el-table-column label="管理员状态" min-width="130"><template #default="{ row }"><el-tag effect="plain" :type="row.adminStatus === 'ACTIVATED' ? 'success' : 'warning'">{{ row.adminStatus === 'ACTIVATED' ? '已激活' : '待激活' }}</el-tag></template></el-table-column><el-table-column label="操作" min-width="290" fixed="right"><template #default="{ row }"><el-button v-if="hasPermission('platform:merchant:enable') && row.status !== 'ENABLED'" link type="primary" :loading="actionTenantId === row.tenantId" @click="enable(row)">启用</el-button><el-button v-if="hasPermission('platform:merchant:disable') && row.status === 'ENABLED'" link type="danger" :loading="actionTenantId === row.tenantId" @click="disable(row)">停用</el-button><el-button v-if="hasPermission('platform:merchant:invitation:reissue') && row.adminStatus !== 'ACTIVATED'" link type="primary" :loading="actionTenantId === row.tenantId" @click="reissueInvitation(row)">重发邀请码</el-button><el-button link @click="copySceneKey(row.sceneKey)">复制入口码</el-button></template></el-table-column></AppDataTable>
  </ListPageLayout>
  <TenantFormDialog v-model="dialogVisible" :submitting="creating" @submit="create" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { RefreshCw, Search } from 'lucide-vue-next'
import ListPageLayout from '../../components/common/ListPageLayout.vue'
import AppDataTable from '../../components/common/AppDataTable.vue'
import TenantFormDialog from '../../components/business/platform/TenantFormDialog.vue'
import { createTenant, disableTenant, enableTenant, listTenants, reissueTenantInvitation } from '../../api/platform/tenants'
import type { CreateTenantCommand, TenantOverview } from '../../types/tenant'
import { message } from '../../utils/message'
import { useSession } from '../../stores/session'
import { useDictionary } from '../../stores/dictionary'

const tenants = ref<TenantOverview[]>([]); const loading = ref(false); const creating = ref(false); const actionTenantId = ref<number>(); const keywordInput = ref(''); const keyword = ref(''); const tenantStatusFilter = ref<TenantOverview['status']>(); const adminStatusFilter = ref<TenantOverview['adminStatus']>(); const dialogVisible = ref(false); const currentPage = ref(1); const pageSize = ref(10)
const hasPermission = useSession().hasPermission
const dictionary = useDictionary()
const pagedTenants = computed(() => tenants.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))
const enabledTenantCount = computed(() => tenants.value.filter(item => item.status === 'ENABLED').length)
const pendingTenantCount = computed(() => tenants.value.filter(item => item.status === 'PENDING').length)
const disabledTenantCount = computed(() => tenants.value.filter(item => item.status === 'DISABLED').length)
function statusLabel(value: string) { return dictionary.dictLabel('TENANT_STATUS', value) }
function statusType(value: string) { return dictionary.dictTagType('TENANT_STATUS', value) }
async function applyFilters() { keyword.value = keywordInput.value.trim(); currentPage.value = 1; await load() }
function changePageSize(size: number) { pageSize.value = size; currentPage.value = 1 }
async function load() { loading.value = true; try { tenants.value = await listTenants({ keyword: keyword.value || undefined, status: tenantStatusFilter.value, adminStatus: adminStatusFilter.value }) } catch (error) { message.error(error instanceof Error ? error.message : '租户列表加载失败') } finally { loading.value = false } }
async function create(command: CreateTenantCommand) { creating.value = true; try { const result = await createTenant(command); dialogVisible.value = false; await load(); await ElMessageBox.alert(`管理员邀请码：${result.invitationCode}\n小程序入口码：${result.sceneKey}`, '商户开通成功', { confirmButtonText: '我已记录' }) } catch (error) { message.error(error instanceof Error ? error.message : '商户开通失败') } finally { creating.value = false } }
async function enable(row: TenantOverview) { try { await ElMessageBox.confirm(`确认启用“${row.merchantName}”？启用后可进行业务写操作。`, '确认启用商户', { type: 'warning' }); actionTenantId.value = row.tenantId; await enableTenant(row.tenantId); message.success('商户已启用'); await load() } catch (error) { if (error !== 'cancel') message.error(error instanceof Error ? error.message : '商户启用失败') } finally { actionTenantId.value = undefined } }
async function disable(row: TenantOverview) { try { await ElMessageBox.confirm(`确认停用“${row.merchantName}”？商户后台将无法继续登录或发起业务写操作，历史数据不会删除。`, '确认停用商户', { type: 'warning' }); actionTenantId.value = row.tenantId; await disableTenant(row.tenantId); message.success('商户已停用'); await load() } catch (error) { if (error !== 'cancel') message.error(error instanceof Error ? error.message : '商户停用失败') } finally { actionTenantId.value = undefined } }
async function reissueInvitation(row: TenantOverview) { try { await ElMessageBox.confirm(`确认重新生成“${row.merchantName}”的管理员邀请码？原邀请码会立即失效。`, '确认重新生成邀请码', { type: 'warning' }); actionTenantId.value = row.tenantId; const result = await reissueTenantInvitation(row.tenantId); await ElMessageBox.alert(`新管理员邀请码：${result.invitationCode}\n有效期至：${result.invitationExpiresAt}\n\n该邀请码仅展示本次，请立即复制并发送给商户管理员。`, '邀请码已重新生成', { confirmButtonText: '我已记录' }); await load() } catch (error) { if (error !== 'cancel') message.error(error instanceof Error ? error.message : '邀请码重新生成失败') } finally { actionTenantId.value = undefined } }
async function copySceneKey(sceneKey: string) { try { await navigator.clipboard.writeText(sceneKey); message.success('小程序入口码已复制') } catch { message.warning('当前浏览器不支持复制，请手动记录入口码') } }
onMounted(() => { dictionary.ensureLoaded(); load() })
</script>

<style scoped>
.tenant-filter-field { width: min(420px, 100%); }
.tenant-filter-select { width: 150px; }
.tenant-filter-result { margin-left: auto; color: #89958e; font-size: 12px; }
.tenant-filter-result b { color: var(--domain-700); font-weight: 650; }

/* 表格舒适度优化 */
:deep(.app-data-table) {
  padding: 0 8px;
}
:deep(.el-table__header th.el-table__cell) {
  font-size: 12px;
  font-weight: 600;
  color: #5e6b64;
}
:deep(.el-table__header th.el-table__cell:first-child .cell) {
  padding-left: 16px;
}
:deep(.el-table__body td.el-table__cell) {
  font-size: 13.5px;
  padding: 14px 0;
  color: #2d3831;
}
:deep(.el-table__body td.el-table__cell:first-child .cell) {
  padding-left: 16px;
}
:deep(.el-table__body td.el-table__cell .cell) {
  line-height: 1.6;
}

@media (max-width: 680px) {
  .tenant-filter-field, .tenant-filter-select { width: 100%; }
  .tenant-filter-result { width: 100%; margin-left: 0; }
  :deep(.el-table__header th.el-table__cell:first-child .cell),
  :deep(.el-table__body td.el-table__cell:first-child .cell) {
    padding-left: 12px;
  }
}
</style>
