<template>
  <el-card class="ink-card" shadow="never" :body-style="bodyStyle"><template v-if="$slots.header" #header><div class="ink-card-header"><slot name="header" /></div></template><slot /></el-card>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ bodyStyle?: Record<string, string> }>(), { bodyStyle: () => ({}) })
</script>
