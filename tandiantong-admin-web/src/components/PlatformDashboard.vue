<template>
  <section class="page-grid">
    <div class="metric-card">
      <span>待启用</span>
      <strong>{{ pendingCount }}</strong>
    </div>
    <div class="metric-card">
      <span>需修改</span>
      <strong>{{ revisionCount }}</strong>
    </div>
    <div class="metric-card">
      <span>支付待办</span>
      <strong>{{ paymentTodo }}</strong>
    </div>
    <div class="panel wide">
      <h3>平台待办</h3>
      <el-timeline>
        <el-timeline-item v-for="merchant in merchants" :key="merchant.id" :timestamp="merchant.status">
          {{ merchant.name }} · 管理员{{ merchant.adminStatus }} · 支付{{ merchant.paymentStatus }}
        </el-timeline-item>
      </el-timeline>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Merchant } from '../types'

const props = defineProps<{ merchants: Merchant[] }>()
const pendingCount = computed(() => props.merchants.filter(item => item.status === '待启用').length)
const revisionCount = computed(() => props.merchants.filter(item => item.status === '需修改').length)
const paymentTodo = computed(() => props.merchants.filter(item => item.paymentStatus !== '已验证').length)
</script>
