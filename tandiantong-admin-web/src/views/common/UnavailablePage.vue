<template>
  <PageHeader :title="title" :description="description" eyebrow="基础设施" />
  <section class="content-card"><UnavailableState :description="`${description}所需的后台接口尚未提供。`" /></section>
</template>

<script setup lang="ts">
import PageHeader from '../../components/common/PageHeader.vue'
import UnavailableState from '../../components/common/UnavailableState.vue'
defineProps<{ title: string; description: string }>()
</script>
