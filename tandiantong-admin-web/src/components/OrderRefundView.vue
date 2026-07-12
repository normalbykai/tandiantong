<template>
  <section class="order-page">
    <div class="toolbar">
      <el-segmented v-model="activeStatus" :options="statusOptions" />
      <div class="toolbar-actions">
        <el-input v-model="keyword" class="search-input" placeholder="订单号、取餐号或手机号后四位" :prefix-icon="Search" />
        <el-button :icon="RefreshCcw">刷新</el-button>
      </div>
    </div>

    <div class="catalog-grid">
      <section class="panel wide">
        <div class="panel-header">
          <div>
            <h3>订单列表</h3>
            <p>支付、退款和核销状态按服务端状态机展示</p>
          </div>
          <el-tag type="success">今日实收 ¥1,286.00</el-tag>
        </div>
        <el-table :data="filteredOrders" class="desktop-table">
          <el-table-column prop="pickupNo" label="取餐号" width="100" />
          <el-table-column prop="orderNo" label="订单号" width="150" />
          <el-table-column prop="summary" label="商品快照" />
          <el-table-column prop="mobile" label="手机号" width="120" />
          <el-table-column label="金额" width="110">
            <template #default="{ row }: { row: OrderRow }">¥{{ (row.amountCent / 100).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="pickupTime" label="预计取餐" width="130" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }: { row: OrderRow }">
              <el-tag :type="row.status === '待核销' ? 'success' : row.status === '退款中' ? 'warning' : 'info'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <h3>订单详情</h3>
            <p>手机号脱敏，退款与核销均需二次确认</p>
          </div>
        </div>
        <div class="detail-status">
          <strong>A018</strong>
          <span>支付成功 · 待核销</span>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">SO10011008</el-descriptions-item>
          <el-descriptions-item label="商品">桂花拿铁 中杯/热 ×2，燕麦奶</el-descriptions-item>
          <el-descriptions-item label="支付">微信支付 TX-20260712008</el-descriptions-item>
          <el-descriptions-item label="日志">创建订单、库存锁定、支付回调、确认扣减</el-descriptions-item>
        </el-descriptions>
        <div class="order-actions">
          <el-button type="danger">整单退款</el-button>
          <el-button type="primary">查看核销码</el-button>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <h3>退款确认</h3>
            <p>仅核销前订单允许整单退款</p>
          </div>
        </div>
        <el-alert title="退款成功后将按订单快照回补库存，并写入退款记录和库存流水。" type="warning" :closable="false" />
        <el-form class="wizard-form" label-position="top">
          <el-form-item label="退款原因">
            <el-input model-value="顾客临时有事" type="textarea" />
          </el-form-item>
          <el-button type="danger" plain>确认整单退款</el-button>
        </el-form>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { RefreshCcw, Search } from 'lucide-vue-next'

interface OrderRow {
  orderNo: string
  pickupNo: string
  summary: string
  mobile: string
  amountCent: number
  pickupTime: string
  status: '待支付' | '待核销' | '已完成' | '退款中' | '已退款' | '已取消'
}

const activeStatus = ref('全部')
const keyword = ref('')
const statusOptions = ['全部', '待支付', '待核销', '已完成', '退款']
const orders = ref<OrderRow[]>([
  { orderNo: 'SO10011008', pickupNo: 'A018', summary: '桂花拿铁 中杯/热 ×2，燕麦奶', mobile: '138****8000', amountCent: 4200, pickupTime: '18:30', status: '待核销' },
  { orderNo: 'SO10011009', pickupNo: 'A019', summary: '手作三明治 ×1', mobile: '139****6123', amountCent: 2600, pickupTime: '18:45', status: '退款中' }
])

const filteredOrders = computed(() => orders.value.filter(order => {
  const statusMatched = activeStatus.value === '全部'
    || order.status === activeStatus.value
    || (activeStatus.value === '退款' && ['退款中', '已退款'].includes(order.status))
  const keywordMatched = !keyword.value
    || order.orderNo.includes(keyword.value)
    || order.pickupNo.includes(keyword.value)
    || order.mobile.endsWith(keyword.value)
  return statusMatched && keywordMatched
}))
</script>
