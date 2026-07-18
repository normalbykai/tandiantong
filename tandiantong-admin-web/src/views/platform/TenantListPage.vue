<template>
  <PageHeader title="租户管理" description="开通、启停商户并维护待激活管理员的邀请码；所有操作均以平台权限域执行。" eyebrow="平台管理"><template #action><el-button type="primary" @click="dialogVisible = true">开通商户</el-button></template></PageHeader>
  <section class="content-card"><div class="table-toolbar"><el-input v-model="keyword" placeholder="搜索商户名称或管理员" :prefix-icon="Search" clearable /><el-button :icon="RefreshCw" circle :loading="loading" aria-label="刷新列表" @click="load" /></div>
    <el-table v-loading="loading" :data="filteredTenants" empty-text="暂无商户租户" class="data-table"><el-table-column prop="merchantName" label="商户名称" min-width="180" /><el-table-column prop="adminName" label="管理员" min-width="120" /><el-table-column prop="adminMobileMasked" label="联系电话" min-width="140" /><el-table-column label="租户状态" min-width="120"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag></template></el-table-column><el-table-column label="管理员状态" min-width="130"><template #default="{ row }"><el-tag effect="plain" :type="row.adminStatus === 'ACTIVATED' ? 'success' : 'warning'">{{ row.adminStatus === 'ACTIVATED' ? '已激活' : '待激活' }}</el-tag></template></el-table-column><el-table-column label="操作" min-width="290" fixed="right"><template #default="{ row }"><el-button v-if="row.status !== 'ENABLED'" link type="primary" :loading="actionTenantId === row.tenantId" @click="enable(row)">启用</el-button><el-button v-else link type="danger" :loading="actionTenantId === row.tenantId" @click="disable(row)">停用</el-button><el-button v-if="row.adminStatus !== 'ACTIVATED'" link type="primary" :loading="actionTenantId === row.tenantId" @click="reissueInvitation(row)">重发邀请码</el-button><el-button link @click="copySceneKey(row.sceneKey)">复制入口码</el-button></template></el-table-column></el-table>
  </section>
  <TenantFormDialog v-model="dialogVisible" :submitting="creating" @submit="create" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshCw, Search } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import TenantFormDialog from '../../components/business/platform/TenantFormDialog.vue'
import { createTenant, disableTenant, enableTenant, listTenants, reissueTenantInvitation } from '../../api/platform/tenants'
import type { CreateTenantCommand, TenantOverview } from '../../types/tenant'

const tenants = ref<TenantOverview[]>([]); const loading = ref(false); const creating = ref(false); const actionTenantId = ref<number>(); const keyword = ref(''); const dialogVisible = ref(false)
const filteredTenants = computed(() => { const value = keyword.value.trim(); return value ? tenants.value.filter(item => [item.merchantName, item.adminName, item.adminMobileMasked].some(field => field.includes(value))) : tenants.value })
function statusLabel(value: TenantOverview['status']) { return value === 'ENABLED' ? '已启用' : value === 'DISABLED' ? '已停用' : '待启用' }
function statusType(value: TenantOverview['status']) { return value === 'ENABLED' ? 'success' : value === 'DISABLED' ? 'info' : 'warning' }
async function load() { loading.value = true; try { tenants.value = await listTenants() } catch (error) { ElMessage.error(error instanceof Error ? error.message : '租户列表加载失败') } finally { loading.value = false } }
async function create(command: CreateTenantCommand) { creating.value = true; try { const result = await createTenant(command); dialogVisible.value = false; await load(); await ElMessageBox.alert(`管理员邀请码：${result.invitationCode}\n小程序入口码：${result.sceneKey}`, '商户开通成功', { confirmButtonText: '我已记录' }) } catch (error) { ElMessage.error(error instanceof Error ? error.message : '商户开通失败') } finally { creating.value = false } }
async function enable(row: TenantOverview) { try { await ElMessageBox.confirm(`确认启用“${row.merchantName}”？启用后可进行业务写操作。`, '确认启用商户', { type: 'warning' }); actionTenantId.value = row.tenantId; await enableTenant(row.tenantId); ElMessage.success('商户已启用'); await load() } catch (error) { if (error !== 'cancel') ElMessage.error(error instanceof Error ? error.message : '商户启用失败') } finally { actionTenantId.value = undefined } }
async function disable(row: TenantOverview) { try { await ElMessageBox.confirm(`确认停用“${row.merchantName}”？商户后台将无法继续登录或发起业务写操作，历史数据不会删除。`, '确认停用商户', { type: 'warning' }); actionTenantId.value = row.tenantId; await disableTenant(row.tenantId); ElMessage.success('商户已停用'); await load() } catch (error) { if (error !== 'cancel') ElMessage.error(error instanceof Error ? error.message : '商户停用失败') } finally { actionTenantId.value = undefined } }
async function reissueInvitation(row: TenantOverview) { try { await ElMessageBox.confirm(`确认重新生成“${row.merchantName}”的管理员邀请码？原邀请码会立即失效。`, '确认重新生成邀请码', { type: 'warning' }); actionTenantId.value = row.tenantId; const result = await reissueTenantInvitation(row.tenantId); await ElMessageBox.alert(`新管理员邀请码：${result.invitationCode}\n有效期至：${result.invitationExpiresAt}\n\n该邀请码仅展示本次，请立即复制并发送给商户管理员。`, '邀请码已重新生成', { confirmButtonText: '我已记录' }); await load() } catch (error) { if (error !== 'cancel') ElMessage.error(error instanceof Error ? error.message : '邀请码重新生成失败') } finally { actionTenantId.value = undefined } }
async function copySceneKey(sceneKey: string) { try { await navigator.clipboard.writeText(sceneKey); ElMessage.success('小程序入口码已复制') } catch { ElMessage.warning('当前浏览器不支持复制，请手动记录入口码') } }
onMounted(load)
</script>
