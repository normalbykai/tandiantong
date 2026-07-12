<template>
  <section class="panel empty-panel">
    <h3>{{ title }}</h3>
    <p>本页面将在对应 V1 阶段按业务规则继续实现。</p>
  </section>
</template>

<script setup lang="ts">
defineProps<{ title: string }>()
</script>
