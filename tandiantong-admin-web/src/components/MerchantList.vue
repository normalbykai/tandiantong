<template>
  <section class="panel">
    <div class="panel-header">
      <div>
        <h3>商户列表</h3>
        <p>平台管理员手动创建商户，并发送首个管理员邀请</p>
      </div>
      <el-button type="primary" @click="dialogVisible = true">新建商户</el-button>
    </div>
    <el-table :data="merchants" class="desktop-table">
      <el-table-column prop="name" label="商户名称" />
      <el-table-column prop="contact" label="联系人" />
      <el-table-column prop="mobile" label="联系电话" />
      <el-table-column prop="status" label="状态" />
      <el-table-column prop="paymentStatus" label="支付配置" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary">查看详情</el-button>
          <el-button link>重新邀请</el-button>
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
        <el-button v-else type="primary" @click="submit">确认创建</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { Merchant, MerchantDraft } from '../types'

defineProps<{ merchants: Merchant[] }>()
const emit = defineEmits<{ createMerchant: [draft: MerchantDraft] }>()
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

function submit() {
  emit('createMerchant', { ...draft })
  dialogVisible.value = false
  step.value = 0
}
</script>
