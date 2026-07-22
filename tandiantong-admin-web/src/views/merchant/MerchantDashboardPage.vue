<template>
  <PageHeader title="工作台" description="集中管理当前商户的门店资料、员工账号和权限配置。" eyebrow="商户管理" />
  <section class="merchant-overview" aria-label="商户基础信息"><div><span>当前经营范围</span><strong>单门店经营</strong><small>V1.1 阶段仅支持维护一个门店。</small></div><div><span>账号与权限</span><strong>待完善</strong><small>员工账号和角色接口开放后将在此展示。</small></div></section>
  <InkCard><template #header><div class="card-heading"><div><h2>基础设施能力</h2><p>所有权限相关数据均在服务端执行租户范围校验。</p></div></div></template><div class="quick-links"><RouterLink v-for="item in links" :key="item.path" :to="item.path" class="quick-link"><component :is="item.icon" :size="20" /><div><b>{{ item.label }}</b><span>{{ item.description }}</span></div></RouterLink></div></InkCard>
</template>

<script setup lang="ts">
import { KeyRound, ScrollText, Store, Users } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import InkCard from '../../components/common/InkCard.vue'
const links = [{ label: '门店信息', description: '维护当前单门店基础资料', path: '/merchant/store', icon: Store }, { label: '员工账号', description: '管理本商户范围员工', path: '/merchant/staff', icon: Users }, { label: '商户权限', description: '查看可用权限点', path: '/merchant/permissions', icon: KeyRound }, { label: '操作日志', description: '查询本商户审计记录', path: '/merchant/logs', icon: ScrollText }]
</script>

<style scoped>
.merchant-overview { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; margin-bottom: 18px; }
.merchant-overview > div { display: grid; gap: 7px; padding: 18px 20px; border: 1px solid #e1e4e9; border-radius: 12px; background: rgba(255, 255, 255, .86); }
.merchant-overview span, .merchant-overview small { color: #7d858b; font-size: 12px; }.merchant-overview strong { color: #252a2e; font-size: 20px; font-weight: 600; }
@media (max-width: 560px) { .merchant-overview { grid-template-columns: 1fr; } }
</style>
