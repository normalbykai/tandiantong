<template>
  <PageHeader title="工作台" description="欢迎回来。这里展示平台当前的运营概况与待处理事项。" eyebrow="平台管理" />
  <InkHeroStats title="平台运营总览" :updated-at="updatedAt" :items="metrics" />
  <InkCard><template #header><div class="card-heading"><div><h2>租户管理入口</h2><p>租户开通、状态与管理员激活状态统一在此维护。</p></div><RouterLink to="/platform/tenants"><el-button type="primary">进入租户管理</el-button></RouterLink></div></template><div class="dashboard-note"><Building2 :size="20" /><span>当前工作台统计基于已开放的商户租户列表实时计算。</span></div></InkCard>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Building2 } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import InkCard from '../../components/common/InkCard.vue'
import InkHeroStats from '../../components/common/InkHeroStats.vue'
import { listTenants } from '../../api/platform/tenants'
import type { TenantOverview } from '../../types/tenant'

const tenants = ref<TenantOverview[]>([])
const updatedAt = new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'medium', hour12: false }).format(new Date())
onMounted(async () => { try { tenants.value = await listTenants() } catch { tenants.value = [] } })
const metrics = computed(() => [{ label: '全部租户', value: tenants.value.length, hint: '已开通商户' }, { label: '已启用', value: tenants.value.filter(item => item.status === 'ENABLED').length, hint: '可进行业务写操作' }, { label: '待管理员激活', value: tenants.value.filter(item => item.adminStatus !== 'ACTIVATED').length, hint: '需完成首次登录' }, { label: '支付待配置', value: tenants.value.filter(item => item.paymentConfigStatus !== 'VERIFIED').length, hint: '待后续接入配置' }])
</script>
