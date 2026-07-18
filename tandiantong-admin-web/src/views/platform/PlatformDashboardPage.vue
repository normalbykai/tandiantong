<template>
  <PageHeader title="平台工作台" description="查看租户开通与基础设施运营状态。" eyebrow="平台管理" />
  <section class="hero-stats"><article v-for="item in metrics" :key="item.label" class="hero-stat"><span>{{ item.label }}</span><strong>{{ item.value }}</strong><small>{{ item.hint }}</small></article></section>
  <section class="content-card"><div class="card-heading"><div><h2>租户管理入口</h2><p>租户开通、状态与管理员激活状态统一在此维护。</p></div><RouterLink to="/platform/tenants"><el-button type="primary">进入租户管理</el-button></RouterLink></div><div class="dashboard-note"><Building2 :size="20" /><span>当前工作台统计基于已开放的商户租户列表实时计算。</span></div></section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Building2 } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import { listTenants } from '../../api/platform/tenants'
import type { TenantOverview } from '../../types/tenant'

const tenants = ref<TenantOverview[]>([])
onMounted(async () => { try { tenants.value = await listTenants() } catch { tenants.value = [] } })
const metrics = computed(() => [{ label: '全部租户', value: tenants.value.length, hint: '已开通商户' }, { label: '已启用', value: tenants.value.filter(item => item.status === 'ENABLED').length, hint: '可进行业务写操作' }, { label: '待管理员激活', value: tenants.value.filter(item => item.adminStatus !== 'ACTIVATED').length, hint: '需完成首次登录' }, { label: '支付待配置', value: tenants.value.filter(item => item.paymentConfigStatus !== 'VERIFIED').length, hint: '待后续接入配置' }])
</script>
