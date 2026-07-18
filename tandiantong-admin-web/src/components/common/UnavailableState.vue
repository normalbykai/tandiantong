<template>
  <div class="unavailable-state"><div class="unavailable-icon"><PlugZap :size="28" /></div><h2>接口暂未开放</h2><p>{{ description }}</p><span>页面结构、访问边界和交互状态已就绪；待后端接口发布后即可接入。</span></div>
</template>

<script setup lang="ts">
import { PlugZap } from 'lucide-vue-next'
defineProps<{ description: string }>()
</script>
