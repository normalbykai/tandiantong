<template>
  <section class="ink-hero" :class="{ merchant }"><header><b><i />{{ title }}</b><span>实时更新 · {{ updatedAt }}</span></header><div class="ink-hero-grid"><article v-for="item in items" :key="item.label"><small>{{ item.label }}</small><strong>{{ item.value }}</strong><span>{{ item.hint }}</span></article></div></section>
</template>

<script setup lang="ts">
interface HeroItem { label: string; value: string | number; hint: string }
defineProps<{ title: string; updatedAt: string; items: HeroItem[]; merchant?: boolean }>()
</script>
