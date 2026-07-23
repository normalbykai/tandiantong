<template>
  <PageHeader title="门店信息" description="维护本商户的门店基础信息。第一阶段仅支持单门店经营，多门店管理能力将在后续阶段开放。" eyebrow="商户管理">
    <template #action><el-button type="primary" :icon="Edit3" @click="editVisible = true">编辑门店</el-button></template>
  </PageHeader>
  <el-alert title="当前为单门店模式。如需新增门店，请等待多门店管理能力上线，或联系平台运营协助。" type="info" :closable="false" show-icon class="merchant-notice" />
  <div v-loading="loading" class="store-grid">
    <InkCard><template #header><div class="card-heading"><div><h2>门店基础信息</h2><p>顾客端与订单流程使用的门店展示资料。</p></div><el-tag :type="store?.status === 'ENABLED' ? 'success' : 'info'">{{ store?.status === 'ENABLED' ? '营业中' : '已停用' }}</el-tag></div></template><div class="detail-grid"><div><span>门店名称</span><b>{{ store?.name ?? '加载中' }}</b></div><div><span>门店编号</span><code>{{ store?.id ?? '-' }}</code></div><div><span>所属商户编号</span><code>{{ store?.tenantId ?? '-' }}</code></div><div><span>数据范围</span><b>当前商户 · 当前门店</b></div></div></InkCard>
    <InkCard><template #header><div class="card-heading"><div><h2>营业状态</h2><p>当前数据库已维护的门店状态。</p></div></div></template><div class="schedule-list"><div><span>当前状态</span><b>{{ store?.status === 'ENABLED' ? '启用' : '停用' }}</b><el-tag :type="store?.status === 'ENABLED' ? 'success' : 'info'" size="small">{{ store?.status === 'ENABLED' ? '正常' : '暂停' }}</el-tag></div><div><span>营业时间</span><b>暂未配置</b><el-tag type="info" size="small">后续接入</el-tag></div></div></InkCard>
  </div>
  <el-dialog v-model="editVisible" title="编辑门店信息" width="520px"><el-form label-width="90px"><el-form-item label="门店名称"><el-input v-model="form.name" /></el-form-item><el-form-item label="门店状态"><el-select v-model="form.status" style="width:100%"><el-option label="启用" value="ENABLED" /><el-option label="停用" value="DISABLED" /></el-select></el-form-item></el-form><template #footer><el-button @click="editVisible = false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存门店信息</el-button></template></el-dialog>
</template>
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Edit3 } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import InkCard from '../../components/common/InkCard.vue'
import { message } from '../../utils/message'
import { getMerchantStore, updateMerchantStore, type MerchantStore } from '../../api/merchant/infrastructure'
const editVisible = ref(false)
const loading = ref(false); const saving = ref(false); const store = ref<MerchantStore>(); const form = reactive({ name: '', status: 'ENABLED' as 'ENABLED' | 'DISABLED' })
async function load() { loading.value = true; try { store.value = await getMerchantStore(); form.name = store.value.name; form.status = store.value.status } catch (error) { message.error(error instanceof Error ? error.message : '门店信息加载失败') } finally { loading.value = false } }
async function save() { saving.value = true; try { store.value = await updateMerchantStore(form); editVisible.value = false; message.success('门店信息保存成功') } catch (error) { message.error(error instanceof Error ? error.message : '门店信息保存失败') } finally { saving.value = false } }
onMounted(load)
</script>
<style scoped>
.merchant-notice { margin-bottom: 18px; }.store-grid { display:grid; grid-template-columns:minmax(0,1.35fr) minmax(320px,1fr); gap:18px; }.card-heading { display:flex; align-items:center; justify-content:space-between; width:100%; }.card-heading h2 { margin:0; font-size:15px; }.card-heading p { margin:5px 0 0; color:#8e8e90; font-size:12px; }.detail-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:20px 28px; padding:20px; }.detail-grid div { display:grid; gap:7px; }.detail-grid span,.schedule-list span { color:#8e8e90; font-size:12px; }.detail-grid b { color:#303033; font-size:13px; font-weight:500; }.detail-grid code { color:var(--domain-700); font:12px ui-monospace,Consolas,monospace; }.detail-wide { grid-column:1/-1; }.schedule-list { padding:8px 20px 20px; }.schedule-list div { display:grid; grid-template-columns:1fr 1.2fr auto; gap:12px; align-items:center; padding:14px 0; border-bottom:1px solid #ebeef1; }.schedule-list div:last-child { border-bottom:0; }.schedule-list b { font:12px ui-monospace,Consolas,monospace; color:#303033; } @media(max-width:760px){.store-grid{grid-template-columns:1fr}.detail-grid{grid-template-columns:1fr}.detail-wide{grid-column:auto}}
</style>
