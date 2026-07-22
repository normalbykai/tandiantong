<template>
  <PageHeader title="工作台" description="欢迎回来。这里展示平台当前的运营概况与待处理事项。" eyebrow="平台管理" />
  <section class="dashboard-metrics" aria-label="平台运营数据">
    <article v-for="item in metrics" :key="item.label" class="dashboard-metric">
      <span>{{ item.label }}</span>
      <strong>{{ item.value }}</strong>
      <small>{{ item.hint }}</small>
    </article>
  </section>
  <InkCard>
    <template #header>
      <div class="card-heading">
        <div><h2>商户管理</h2><p>开通商户、处理启停状态和跟进管理员激活。</p></div>
        <RouterLink to="/platform/tenants"><el-button type="primary">进入商户管理</el-button></RouterLink>
      </div>
    </template>
    <div class="dashboard-note"><Building2 :size="18" /><span>统计数据基于当前已开通商户实时计算。</span></div>
  </InkCard>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Building2 } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import InkCard from '../../components/common/InkCard.vue'
import { listTenants } from '../../api/platform/tenants'
import type { TenantOverview } from '../../types/tenant'

const tenants = ref<TenantOverview[]>([])
onMounted(async () => { try { tenants.value = await listTenants() } catch { tenants.value = [] } })
const metrics = computed(() => [{ label: '全部租户', value: tenants.value.length, hint: '已开通商户' }, { label: '已启用', value: tenants.value.filter(item => item.status === 'ENABLED').length, hint: '可进行业务写操作' }, { label: '待管理员激活', value: tenants.value.filter(item => item.adminStatus !== 'ACTIVATED').length, hint: '需完成首次登录' }, { label: '支付待配置', value: tenants.value.filter(item => item.paymentConfigStatus !== 'VERIFIED').length, hint: '待后续接入配置' }])
</script>

<style scoped>
.dashboard-metrics { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; margin-bottom: 18px; }
.dashboard-metric { display: grid; gap: 7px; min-height: 132px; padding: 20px; border: 1px solid #e1e4e9; border-radius: 12px; background: rgba(255, 255, 255, .86); box-shadow: 0 1px 3px rgba(28, 28, 30, .06); }
.dashboard-metric > span { color: #6b7176; font-size: 12px; }
.dashboard-metric strong { color: #22272b; font-size: 30px; font-weight: 650; letter-spacing: -.8px; line-height: 1; }
.dashboard-metric small { color: #90969b; font-size: 12px; }
.card-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; width: 100%; }
.card-heading h2 { margin: 0; color: #252a2e; font-size: 15px; }
.card-heading p { margin: 5px 0 0; color: #7d858b; font-size: 12px; }
.dashboard-note { display: flex; align-items: center; gap: 9px; padding: 20px; color: #697177; font-size: 13px; }
.dashboard-note svg { color: #596269; }
@media (max-width: 900px) { .dashboard-metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 560px) { .dashboard-metrics { grid-template-columns: 1fr; } .card-heading { align-items: flex-start; flex-direction: column; } }
</style>
