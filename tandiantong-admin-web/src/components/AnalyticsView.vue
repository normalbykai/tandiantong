<template>
  <section class="analytics-view">
    <div class="toolbar">
      <el-segmented v-model="dateRange" :options="dateOptions" />
      <div class="toolbar-actions">
        <el-tag type="success">具备导出权限</el-tag>
        <el-button type="primary">导出 Excel</el-button>
      </div>
    </div>

    <section class="page-grid">
      <div class="metric-card" v-for="metric in metrics" :key="metric.label">
        <span>{{ metric.label }}</span>
        <strong>{{ metric.value }}</strong>
        <small>{{ metric.note }}</small>
      </div>

      <section class="panel wide">
        <div class="panel-header">
          <div>
            <h3>交易概览</h3>
            <p>仅统计春风小铺当前门店，金额单位由后端以分聚合后展示。</p>
          </div>
          <el-tag>净收入 ¥4,900</el-tag>
        </div>
        <el-table :data="transactionRows" class="desktop-table">
          <el-table-column prop="name" label="指标" />
          <el-table-column prop="value" label="数值" />
          <el-table-column prop="desc" label="说明" />
        </el-table>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <h3>商品排行</h3>
            <p>商品、SKU 与加料项按当前日期范围排序。</p>
          </div>
        </div>
        <div class="ranking-list">
          <article v-for="item in productRankings" :key="item.name" class="ranking-item">
            <span>{{ item.name }}</span>
            <strong>{{ item.quantity }} 份</strong>
          </article>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <h3>预约分析</h3>
            <p>取消、履约和时段使用率使用同一租户范围。</p>
          </div>
        </div>
        <div class="reservation-bars">
          <label>预约数 <strong>3</strong></label>
          <el-progress :percentage="100" />
          <label>履约数 <strong>1</strong></label>
          <el-progress :percentage="33" status="success" />
          <label>取消数 <strong>1</strong></label>
          <el-progress :percentage="33" status="exception" />
          <label>平均时段使用率 <strong>50%</strong></label>
          <el-progress :percentage="50" />
        </div>
      </section>

      <section class="panel wide">
        <div class="panel-header">
          <div>
            <h3>导出任务</h3>
            <p>导出任务记录脱敏操作人和审计说明，文件有效期为 24 小时。</p>
          </div>
          <el-button>查看审计记录</el-button>
        </div>
        <el-table :data="exportTasks" class="desktop-table">
          <el-table-column prop="fileName" label="文件名" min-width="220" />
          <el-table-column prop="operator" label="操作人" />
          <el-table-column prop="status" label="状态" />
          <el-table-column prop="audit" label="审计说明" min-width="240" />
        </el-table>
      </section>
    </section>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const dateRange = ref('近7日')
const dateOptions = ['近7日', '本月', '自定义']

const metrics = [
  { label: '订单数', value: '3', note: '含已退款订单' },
  { label: '实收金额', value: '¥71.00', note: '支付成功口径' },
  { label: '退款金额', value: '¥22.00', note: '核销前整单退款' },
  { label: '待核销', value: '1', note: '商品与预约凭证' }
]

const transactionRows = [
  { name: '已支付订单', value: '2', desc: '已支付且未退款的订单数量' },
  { name: '净收入', value: '¥49.00', desc: '实收金额扣除退款金额' },
  { name: '退款金额', value: '¥22.00', desc: '仅包含 V1 支持的整单退款' }
]

const productRankings = [
  { name: '桂花拿铁', quantity: 3 },
  { name: '桂花拿铁 中杯热', quantity: 3 },
  { name: '燕麦奶', quantity: 2 },
  { name: '手作三明治', quantity: 1 }
]

const exportTasks = [
  {
    fileName: '经营数据-交易概览-20260701-20260712.xlsx',
    operator: 'z***@example.test',
    status: '已完成',
    audit: '租户 1001 导出交易概览，联系人已脱敏'
  }
]
</script>
