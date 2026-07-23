<template>
  <PageHeader title="系统管理" description="维护商户管理端的基础偏好与账号安全设置。平台品牌、系统字典和平台安全策略由平台管理端独立维护。" eyebrow="系统" />
  <div v-loading="loading" class="system-grid"><InkCard><template #header><div class="card-heading"><div><h2>商户展示设置</h2><p>仅保存当前商户当前门店自己的展示信息。</p></div></div></template><el-form label-width="100px" class="system-form"><el-form-item label="门店简称"><el-input v-model="storeShortName" maxlength="64" /></el-form-item><el-form-item label="门店公告"><el-input v-model="notice" type="textarea" :rows="4" maxlength="255" show-word-limit /></el-form-item><el-button type="primary" :loading="saving" @click="save">保存展示设置</el-button></el-form></InkCard><InkCard><template #header><div class="card-heading"><div><h2>账号安全</h2><p>商户账号仅可管理自身登录凭据。</p></div></div></template><div class="security-list"><div><div><b>登录域</b><span>当前账号固定使用商户管理端权限域</span></div><el-tag type="success">商户管理</el-tag></div><div><div><b>数据范围</b><span>当前商户及其单门店数据</span></div><el-tag type="info">已隔离</el-tag></div></div></InkCard></div>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '../../components/common/PageHeader.vue'
import InkCard from '../../components/common/InkCard.vue'
import { message } from '../../utils/message'
import { getMerchantSystemConfig, updateMerchantSystemConfig } from '../../api/merchant/infrastructure'

const storeShortName = ref('')
const notice = ref('')
const loading = ref(false)
const saving = ref(false)

async function load() {
  loading.value = true
  try {
    const config = await getMerchantSystemConfig()
    storeShortName.value = config.shortName
    notice.value = config.notice
  } catch (error) {
    message.error(error instanceof Error ? error.message : '商户展示设置加载失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    const config = await updateMerchantSystemConfig({ shortName: storeShortName.value, notice: notice.value })
    storeShortName.value = config.shortName
    notice.value = config.notice
    message.success('商户展示设置保存成功')
  } catch (error) {
    message.error(error instanceof Error ? error.message : '商户展示设置保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
<style scoped>.system-grid{display:grid;grid-template-columns:minmax(0,1.15fr) minmax(320px,1fr);gap:18px}.card-heading h2{margin:0;font-size:15px}.card-heading p{margin:5px 0 0;color:#8e8e90;font-size:12px}.system-form{padding:20px}.security-list{padding:8px 20px}.security-list>div{display:flex;align-items:center;justify-content:space-between;gap:16px;padding:17px 0;border-bottom:1px solid #ebeef1}.security-list>div:last-child{border-bottom:0}.security-list div div{display:grid;gap:6px}.security-list b{font-size:13px;font-weight:500}.security-list span{color:#8e8e90;font-size:12px}@media(max-width:760px){.system-grid{grid-template-columns:1fr}}
</style>
