<template>
  <PageHeader title="租户管理" description="开通、查看并启用商户租户；所有操作均以平台权限域执行。" eyebrow="平台管理"><template #action><el-button type="primary" @click="dialogVisible = true">开通商户</el-button></template></PageHeader>
  <section class="content-card"><div class="table-toolbar"><el-input v-model="keyword" placeholder="搜索商户名称或管理员" :prefix-icon="Search" clearable /><el-button :icon="RefreshCw" circle :loading="loading" aria-label="刷新列表" @click="load" /></div>
    <el-table v-loading="loading" :data="filteredTenants" empty-text="暂无商户租户" class="data-table"><el-table-column prop="merchantName" label="商户名称" min-width="180" /><el-table-column prop="adminName" label="管理员" min-width="120" /><el-table-column prop="adminMobileMasked" label="联系电话" min-width="140" /><el-table-column label="租户状态" min-width="120"><template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag></template></el-table-column><el-table-column label="管理员状态" min-width="130"><template #default="{ row }"><el-tag effect="plain" :type="row.adminStatus === 'ACTIVATED' ? 'success' : 'warning'">{{ row.adminStatus === 'ACTIVATED' ? '已激活' : '待激活' }}</el-tag></template></el-table-column><el-table-column label="操作" width="200" fixed="right"><template #default="{ row }"><el-button v-if="row.status !== 'ENABLED'" link type="primary" :loading="enablingId === row.tenantId" @click="enable(row)">启用</el-button><el-button link @click="copySceneKey(row.sceneKey)">复制入口码</el-button></template></el-table-column></el-table>
  </section>
  <TenantFormDialog v-model="dialogVisible" :submitting="creating" @submit="create" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshCw, Search } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import TenantFormDialog from '../../components/business/platform/TenantFormDialog.vue'
import { createTenant, enableTenant, listTenants } from '../../api/platform/tenants'
import type { CreateTenantCommand, TenantOverview } from '../../types/tenant'

const tenants = ref<TenantOverview[]>([]); const loading = ref(false); const creating = ref(false); const enablingId = ref<number>(); const keyword = ref(''); const dialogVisible = ref(false)
const filteredTenants = computed(() => { const value = keyword.value.trim(); return value ? tenants.value.filter(item => [item.merchantName, item.adminName, item.adminMobileMasked].some(field => field.includes(value))) : tenants.value })
function statusLabel(value: TenantOverview['status']) { return value === 'ENABLED' ? '已启用' : value === 'DISABLED' ? '已停用' : '待启用' }
function statusType(value: TenantOverview['status']) { return value === 'ENABLED' ? 'success' : value === 'DISABLED' ? 'info' : 'warning' }
async function load() { loading.value = true; try { tenants.value = await listTenants() } catch (error) { ElMessage.error(error instanceof Error ? error.message : '租户列表加载失败') } finally { loading.value = false } }
async function create(command: CreateTenantCommand) { creating.value = true; try { const result = await createTenant(command); dialogVisible.value = false; await load(); await ElMessageBox.alert(`管理员邀请码：${result.invitationCode}\n小程序入口码：${result.sceneKey}`, '商户开通成功', { confirmButtonText: '我已记录' }) } catch (error) { ElMessage.error(error instanceof Error ? error.message : '商户开通失败') } finally { creating.value = false } }
async function enable(row: TenantOverview) { try { await ElMessageBox.confirm(`确认启用“${row.merchantName}”？启用后可进行业务写操作。`, '确认启用商户', { type: 'warning' }); enablingId.value = row.tenantId; await enableTenant(row.tenantId); ElMessage.success('商户已启用'); await load() } catch (error) { if (error !== 'cancel') ElMessage.error(error instanceof Error ? error.message : '商户启用失败') } finally { enablingId.value = undefined } }
async function copySceneKey(sceneKey: string) { try { await navigator.clipboard.writeText(sceneKey); ElMessage.success('小程序入口码已复制') } catch { ElMessage.warning('当前浏览器不支持复制，请手动记录入口码') } }
onMounted(load)
</script>
