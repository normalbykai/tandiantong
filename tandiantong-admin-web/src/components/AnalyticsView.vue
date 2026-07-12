<template>
  <section class="analytics-view">
    <div class="toolbar">
      <el-segmented v-model="dateRange" :options="dateOptions" />
      <div class="toolbar-actions">
        <el-tag type="success">具备导出权限</el-tag>
        <el-button type="primary" :loading="exporting" @click="exportExcel">导出 Excel</el-button>
      </div>
    </div>

    <section class="page-grid">
      <div class="metric-card" v-for="metric in metrics" :key="metric.label" v-loading="loading">
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
          <el-tag>净收入 {{ money(netIncomeCent) }}</el-tag>
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
          <label>预约数 <strong>{{ reservation.total }}</strong></label>
          <el-progress :percentage="100" />
          <label>履约数 <strong>{{ reservation.fulfilled }}</strong></label>
          <el-progress :percentage="percentage(reservation.fulfilled, reservation.total)" status="success" />
          <label>取消数 <strong>{{ reservation.canceled }}</strong></label>
          <el-progress :percentage="percentage(reservation.canceled, reservation.total)" status="exception" />
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
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { apiRequest, downloadFile } from '../api'

const dateRange = ref('近7日')
const dateOptions = ['近7日', '本月', '自定义']

interface Dashboard { order: { orderCount: number; grossCent: number; refundCent: number; pendingVerification: number }; reservation: { total: number; canceled: number; fulfilled: number }; products: Array<{ name: string; quantity: number; amountCent: number }> }
const loading = ref(false)
const exporting = ref(false)
const dashboard = ref<Dashboard>({ order: { orderCount: 0, grossCent: 0, refundCent: 0, pendingVerification: 0 }, reservation: { total: 0, canceled: 0, fulfilled: 0 }, products: [] })
const reservation = computed(() => dashboard.value.reservation)
const netIncomeCent = computed(() => dashboard.value.order.grossCent - dashboard.value.order.refundCent)
const metrics = computed(() => [
  { label: '订单数', value: String(dashboard.value.order.orderCount), note: '含已退款订单' },
  { label: '实收金额', value: money(dashboard.value.order.grossCent), note: '支付成功口径' },
  { label: '退款金额', value: money(dashboard.value.order.refundCent), note: '核销前整单退款' },
  { label: '待核销', value: String(dashboard.value.order.pendingVerification), note: '商品订单' }
])

const transactionRows = computed(() => [
  { name: '订单数', value: String(dashboard.value.order.orderCount), desc: '当前日期范围订单数量' },
  { name: '净收入', value: money(netIncomeCent.value), desc: '实收金额扣除退款金额' },
  { name: '退款金额', value: money(dashboard.value.order.refundCent), desc: '仅包含 V1 支持的整单退款' }
])

const productRankings = computed(() => dashboard.value.products)

const exportTasks = [
  {
    fileName: '经营数据-交易概览-20260701-20260712.xlsx',
    operator: 'z***@example.test',
    status: '已完成',
    audit: '租户 1001 导出交易概览，联系人已脱敏'
  }
]

function dateParams() { const end = new Date(); const start = new Date(); start.setDate(end.getDate() - 6); return { start: start.toISOString().slice(0, 10), end: end.toISOString().slice(0, 10) } }
async function loadDashboard() { loading.value = true; try { const range = dateParams(); dashboard.value = await apiRequest<Dashboard>(`/api/admin/v1/analytics?startDate=${range.start}&endDate=${range.end}`) } catch (error) { ElMessage.error(error instanceof Error ? error.message : '经营数据加载失败') } finally { loading.value = false } }
async function exportExcel() { exporting.value = true; try { const range = dateParams(); const file = await downloadFile(`/api/admin/v1/analytics/export?startDate=${range.start}&endDate=${range.end}&contact=13800008000`); const url = URL.createObjectURL(file.blob); const link = document.createElement('a'); link.href = url; link.download = file.fileName; link.click(); URL.revokeObjectURL(url) } catch (error) { ElMessage.error(error instanceof Error ? error.message : '导出失败') } finally { exporting.value = false } }
function money(value: number) { return `¥${(value / 100).toFixed(2)}` }
function percentage(value: number, total: number) { return total === 0 ? 0 : Math.round(value * 100 / total) }
onMounted(loadDashboard)
</script>
