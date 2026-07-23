<template>
  <PageHeader title="操作日志" description="查询本商户范围内的关键操作记录，用于追踪门店、员工、角色和权限变更。" eyebrow="系统" />
  <InkCard v-loading="loading"><div class="toolbar"><el-input v-model="keyword" clearable placeholder="搜索操作对象、详情或追踪号" :prefix-icon="Search" /><el-select v-model="action" clearable placeholder="操作类型"><el-option label="资料变更" value="资料变更" /><el-option label="权限变更" value="权限变更" /><el-option label="账号操作" value="账号操作" /></el-select></div><el-table :data="logs" class="data-table"><el-table-column prop="createdAt" label="操作时间" width="180" /><el-table-column prop="operationType" label="操作类型" width="130"><template #default="{row}"><el-tag type="info">{{ row.operationType }}</el-tag></template></el-table-column><el-table-column prop="targetType" label="操作对象" width="130" /><el-table-column prop="detail" label="操作详情" min-width="300" /><el-table-column prop="traceId" label="追踪号" min-width="220"><template #default="{row}"><code>{{ row.traceId || '-' }}</code></template></el-table-column></el-table><div class="table-foot">仅展示当前商户日志，平台审计记录不在此页面展示。</div></InkCard>
</template>
<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { Search } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import InkCard from '../../components/common/InkCard.vue'
const keyword = ref('')
const action = ref('')
import { listMerchantLogs, type MerchantLog } from '../../api/merchant/infrastructure'
import { message } from '../../utils/message'
const logs = ref<MerchantLog[]>([]); const loading = ref(false)
async function load() { loading.value = true; try { logs.value = await listMerchantLogs({ keyword: keyword.value, operationType: action.value }) } catch (error) { message.error(error instanceof Error ? error.message : '商户日志加载失败') } finally { loading.value = false } }
watch([keyword, action], load); onMounted(load)
</script>
<style scoped>.toolbar{display:flex;gap:10px;padding:18px 20px;border-bottom:1px solid #ebeef1}.toolbar .el-input{max-width:360px}.toolbar .el-select{width:150px}.table-foot{padding:14px 20px;color:#8e8e90;font-size:12px;border-top:1px solid #ebeef1}code{color:var(--domain-700);font:12px ui-monospace,Consolas,monospace}@media(max-width:560px){.toolbar{flex-direction:column}.toolbar .el-input,.toolbar .el-select{width:100%;max-width:none}}</style>
