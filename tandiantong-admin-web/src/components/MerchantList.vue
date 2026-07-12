<template>
  <section class="panel">
    <div class="panel-header">
      <div>
        <h3>商户列表</h3>
        <p>平台管理员手动创建商户，并发送首个管理员邀请</p>
      </div>
      <el-button type="primary" @click="dialogVisible = true">新建商户</el-button>
    </div>
    <el-table :data="merchants" class="desktop-table" v-loading="loading">
      <el-table-column prop="name" label="商户名称" />
      <el-table-column prop="contact" label="联系人" />
      <el-table-column prop="mobile" label="联系电话" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="paymentStatus" label="支付配置" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button v-if="row.status === '待启用'" link type="primary" :loading="enablingId === row.id" @click="enable(row.id)">启用</el-button>
          <el-button link @click="copyScene(row.sceneKey)">复制入口码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="mobile-list">
      <article v-for="merchant in merchants" :key="merchant.id" class="mobile-card">
        <strong>{{ merchant.name }}</strong>
        <span>{{ merchant.status }} · 支付{{ merchant.paymentStatus }}</span>
        <span>{{ merchant.contact }} · {{ merchant.mobile }}</span>
      </article>
    </div>

    <el-dialog v-model="dialogVisible" title="新建商户" width="720px">
      <el-steps :active="step" finish-status="success">
        <el-step title="主体信息" />
        <el-step title="门店资料" />
        <el-step title="管理员" />
        <el-step title="品牌" />
        <el-step title="确认" />
      </el-steps>
      <el-form label-position="top" class="wizard-form">
        <el-form-item v-if="step === 0" label="主体名称（必填）">
          <el-input v-model="draft.subjectName" />
        </el-form-item>
        <el-form-item v-if="step === 0" label="联系人（必填）">
          <el-input v-model="draft.contact" />
        </el-form-item>
        <el-form-item v-if="step === 0" label="联系电话（必填）">
          <el-input v-model="draft.mobile" />
        </el-form-item>
        <template v-if="step === 1">
          <el-form-item label="门店名称（必填）">
            <el-input v-model="draft.storeName" />
          </el-form-item>
          <el-form-item label="门店地址（必填）">
            <el-input v-model="draft.address" />
          </el-form-item>
        </template>
        <el-form-item v-if="step === 2" label="首个管理员">
          <el-input v-model="draft.adminName" />
        </el-form-item>
        <el-form-item v-if="step === 3" label="品牌主题色">
          <el-color-picker v-model="draft.brandColor" />
        </el-form-item>
        <div v-if="step === 4" class="confirm-box">
          <p>商户：{{ draft.subjectName }}</p>
          <p>门店：{{ draft.storeName }} · {{ draft.address }}</p>
          <p>管理员：{{ draft.adminName }} · {{ draft.mobile }}</p>
          <p>创建后状态为“待启用”，支付配置默认为“未配置”。</p>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">保存草稿</el-button>
        <el-button v-if="step > 0" @click="step -= 1">上一步</el-button>
        <el-button v-if="step < 4" type="primary" @click="step += 1">下一步</el-button>
        <el-button v-else type="primary" :loading="submitting" @click="submit">确认创建</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Merchant, MerchantDraft, MerchantProvisioned } from '../types'
import { apiRequest } from '../api'

interface MerchantOverview { tenantId: number; merchantName: string; adminName: string; adminMobileMasked: string; status: string; paymentConfigStatus: string; adminStatus: string; sceneKey: string }
const merchants = ref<Merchant[]>([])
const loading = ref(false)
const submitting = ref(false)
const enablingId = ref<number>()
const dialogVisible = ref(false)
const step = ref(0)
const draft = reactive<MerchantDraft>({
  subjectName: '春风小铺',
  contact: '张晓春',
  mobile: '13800008000',
  storeName: '春风小铺默认门店',
  address: '上海路 18 号',
  adminName: '张晓春',
  brandColor: '#0F766E'
})

async function loadMerchants() {
  loading.value = true
  try {
    const rows = await apiRequest<MerchantOverview[]>('/api/platform/v1/merchants')
    merchants.value = rows.map(row => ({ id: row.tenantId, name: row.merchantName, contact: row.adminName, mobile: row.adminMobileMasked,
      status: row.status === 'ENABLED' ? '已启用' : row.status === 'DISABLED' ? '已停用' : '待启用',
      paymentStatus: row.paymentConfigStatus === 'VERIFIED' ? '已验证' : row.paymentConfigStatus === 'VERIFY_FAILED' ? '验证失败' : row.paymentConfigStatus === 'PENDING_VERIFY' ? '待验证' : '未配置',
      adminStatus: row.adminStatus === 'ACTIVATED' ? '已激活' : '待激活', products: 0, freeServices: 0, sceneKey: row.sceneKey }))
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '商户列表加载失败') } finally { loading.value = false }
}

async function submit() {
  submitting.value = true
  try {
    const result = await apiRequest<MerchantProvisioned>('/api/platform/v1/merchants', { method: 'POST', body: JSON.stringify({ merchantName: draft.subjectName, storeAddress: draft.address, adminName: draft.adminName, adminMobile: draft.mobile }) })
    dialogVisible.value = false; step.value = 0; await loadMerchants()
    await ElMessageBox.alert(`管理员邀请码：${result.invitationCode}\n小程序入口码：${result.sceneKey}`, '商户创建成功', { confirmButtonText: '我已记录' })
  } catch (error) { ElMessage.error(error instanceof Error ? error.message : '商户创建失败') } finally { submitting.value = false }
}

async function enable(tenantId: number) {
  enablingId.value = tenantId
  try { await apiRequest<void>(`/api/platform/v1/merchants/${tenantId}/enable`, { method: 'POST' }); ElMessage.success('商户已启用'); await loadMerchants() }
  catch (error) { ElMessage.error(error instanceof Error ? error.message : '商户启用失败') } finally { enablingId.value = undefined }
}

async function copyScene(sceneKey: string) { await navigator.clipboard.writeText(sceneKey); ElMessage.success('入口码已复制') }
onMounted(loadMerchants)
</script>
