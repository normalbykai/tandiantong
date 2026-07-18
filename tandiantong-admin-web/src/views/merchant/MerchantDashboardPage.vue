<template>
  <PageHeader title="商户工作台" description="集中管理当前商户的门店资料、员工账号和权限配置。" eyebrow="商户管理" />
  <section class="hero-stats merchant-stats"><article class="hero-stat"><span>门店数</span><strong>1</strong><small>V1.1 单门店经营</small></article><article class="hero-stat"><span>员工账号</span><strong>—</strong><small>待账号接口开放</small></article><article class="hero-stat"><span>已启用角色</span><strong>—</strong><small>待角色接口开放</small></article><article class="hero-stat"><span>最近操作</span><strong>—</strong><small>待审计日志接口开放</small></article></section>
  <section class="content-card"><div class="card-heading"><div><h2>基础设施能力</h2><p>所有权限相关数据均在服务端执行租户范围校验。</p></div></div><div class="quick-links"><RouterLink v-for="item in links" :key="item.path" :to="item.path" class="quick-link"><component :is="item.icon" :size="20" /><div><b>{{ item.label }}</b><span>{{ item.description }}</span></div></RouterLink></div></section>
</template>

<script setup lang="ts">
import { KeyRound, ScrollText, Store, Users } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
const links = [{ label: '门店信息', description: '维护当前单门店基础资料', path: '/merchant/store', icon: Store }, { label: '员工账号', description: '管理本商户范围员工', path: '/merchant/staff', icon: Users }, { label: '商户权限', description: '查看可用权限点', path: '/merchant/permissions', icon: KeyRound }, { label: '操作日志', description: '查询本商户审计记录', path: '/merchant/logs', icon: ScrollText }]
</script>
