<template>
  <section class="panel">
    <div class="review-tabs">
      <el-tag type="warning">待启用 {{ count('待启用') }}</el-tag>
      <el-tag type="danger">需修改 {{ count('需修改') }}</el-tag>
      <el-tag>待复核 {{ count('待复核') }}</el-tag>
      <el-tag type="success">已启用 {{ count('已启用') }}</el-tag>
    </div>
    <article v-for="merchant in merchants" :key="merchant.id" class="review-item">
      <div>
        <h3>{{ merchant.name }}</h3>
        <p>管理员{{ merchant.adminStatus }}｜品牌已配置｜支付{{ merchant.paymentStatus }}｜商品 {{ merchant.products }} 项</p>
      </div>
      <div class="review-actions">
        <el-button>查看详情</el-button>
        <el-button type="primary">开始审核</el-button>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import type { Merchant } from '../types'

const props = defineProps<{ merchants: Merchant[] }>()
function count(status: Merchant['status']) {
  return props.merchants.filter(item => item.status === status).length
}
</script>
