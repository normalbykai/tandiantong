<template>
  <section class="reservation-page">
    <div class="toolbar">
      <el-segmented v-model="activeView" :options="['预约列表', '预约日历', '服务配置']" />
      <el-button type="primary" :icon="Plus">新建服务</el-button>
    </div>

    <div class="catalog-grid">
      <section class="panel">
        <div class="panel-header">
          <div>
            <h3>服务项目</h3>
            <p>付费服务需支付验证后才能上架</p>
          </div>
          <el-tag type="success">免费服务可用</el-tag>
        </div>
        <el-steps :active="3" finish-status="success" direction="vertical">
          <el-step title="基础信息" description="咖啡体验课｜60 分钟｜门店小班" />
          <el-step title="支付与预约规则" description="免费预约或微信支付预约，不支持人员排班" />
          <el-step title="排期与容量" description="按日期和时段配置容量，可暂停单个时段" />
          <el-step title="预览与上架" description="检查 C 端时段、剩余名额和预约凭证" />
        </el-steps>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <h3>预约日历</h3>
            <p>按日期查看剩余容量</p>
          </div>
        </div>
        <div class="slot-list">
          <article v-for="slot in slots" :key="slot.time" class="slot-card" :class="{ full: slot.left === 0 }">
            <strong>{{ slot.time }}</strong>
            <span>{{ slot.left === 0 ? '已满' : `剩余 ${slot.left} 名` }}</span>
          </article>
        </div>
      </section>

      <section class="panel wide">
        <div class="panel-header">
          <div>
            <h3>预约列表</h3>
            <p>免费预约确认后立即生成凭证，付费预约支付后生成凭证</p>
          </div>
        </div>
        <el-table :data="reservations" class="desktop-table">
          <el-table-column prop="reservationNo" label="预约编号" width="150" />
          <el-table-column prop="serviceName" label="服务" />
          <el-table-column prop="time" label="预约时间" width="180" />
          <el-table-column prop="mobile" label="手机号" width="120" />
          <el-table-column prop="payment" label="支付" width="100" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }: { row: ReservationRow }">
              <el-tag :type="row.status === '已预约' ? 'success' : row.status === '待支付' ? 'warning' : 'info'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Plus } from 'lucide-vue-next'

interface ReservationRow {
  reservationNo: string
  serviceName: string
  time: string
  mobile: string
  payment: string
  status: '待支付' | '已预约' | '已取消'
}

const activeView = ref('预约列表')
const slots = [
  { time: '7月13日 14:00-15:00', left: 2 },
  { time: '7月13日 15:00-16:00', left: 0 },
  { time: '7月14日 10:00-11:00', left: 5 }
]
const reservations = ref<ReservationRow[]>([
  { reservationNo: 'YY202607130018', serviceName: '咖啡体验课', time: '7月13日 14:00-15:00', mobile: '138****8000', payment: '免费', status: '已预约' },
  { reservationNo: 'YY202607140006', serviceName: '咖啡体验课', time: '7月14日 10:00-11:00', mobile: '139****6123', payment: '微信支付', status: '待支付' }
])
</script>
