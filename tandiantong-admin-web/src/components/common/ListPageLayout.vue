<template>
  <div class="list-page-layout">
    <section class="page-header list-page-layout__header">
      <div>
        <span class="list-page-layout__eyebrow">{{ eyebrow }}</span>
        <h1>{{ title }}</h1>
        <p>{{ description }}</p>
      </div>
      <div v-if="$slots.headerAction" class="list-page-layout__header-action">
        <slot name="headerAction" />
      </div>
    </section>

    <section v-if="$slots.stats" class="list-page-layout__card list-page-layout__stats">
      <div class="list-page-layout__stats-grid"><slot name="stats" /></div>
    </section>

    <section v-if="$slots.actions" class="list-page-layout__card list-page-layout__actions">
      <div class="list-page-layout__action-content"><slot name="actions" /></div>
    </section>

    <section v-if="$slots.filters" class="list-page-layout__card list-page-layout__filters">
      <div class="list-page-layout__filter-content"><slot name="filters" /></div>
    </section>

    <section class="list-page-layout__card list-page-layout__list">
      <slot />
    </section>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  description: string
  eyebrow?: string
}>(), { eyebrow: '平台管理' })
</script>

<style scoped>
.list-page-layout { display: grid; width: 100%; gap: 16px; }
.list-page-layout__card { width: 100%; min-width: 0; height: auto; border: 1px solid #e1e7e3; border-radius: 14px; background: rgba(255, 255, 255, .9); box-shadow: 0 3px 12px rgba(25, 56, 43, .045); }
.list-page-layout__header { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; margin-bottom: 4px; }
.list-page-layout__eyebrow { display: none; }
.list-page-layout__section-mark { color: var(--domain-600); font-family: ui-monospace, Consolas, monospace; font-size: 10px; font-weight: 700; letter-spacing: 1.3px; }
.list-page-layout__header h1 { margin: 0; }
.list-page-layout__header p { margin: 0; }
.list-page-layout__header-action { flex: 0 0 auto; }
.list-page-layout__stats, .list-page-layout__actions, .list-page-layout__filters { padding: 20px 22px; }
.list-page-layout__section-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; margin-bottom: 16px; }
.list-page-layout__section-heading > div { display: grid; gap: 4px; }
.list-page-layout__section-heading strong { color: #34443b; font-size: 14px; font-weight: 650; }
.list-page-layout__section-heading span:not(.list-page-layout__section-mark) { color: #97a19b; font-size: 11px; }
.list-page-layout__stats-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: clamp(8px, 1.2vw, 12px); }
.list-page-layout__stats-grid :deep(.list-stat) { display: grid; align-content: center; gap: 7px; min-width: 0; min-height: clamp(72px, 8vw, 82px); padding: clamp(12px, 1.5vw, 15px) clamp(12px, 1.6vw, 16px); border: 1px solid #e4ebe6; border-radius: 11px; background: #fbfcfb; }
.list-page-layout__stats-grid :deep(.list-stat__label) { color: #7c8981; font-size: 11px; }
.list-page-layout__stats-grid :deep(.list-stat__value) { color: #24372c; font-size: 25px; font-weight: 700; line-height: 1; }
.list-page-layout__stats-grid :deep(.list-stat__value.is-success) { color: #2d7a4e; }
.list-page-layout__stats-grid :deep(.list-stat__value.is-warning) { color: #a6762b; }
.list-page-layout__stats-grid :deep(.list-stat__value.is-muted) { color: #69756e; }
.list-page-layout__action-content, .list-page-layout__filter-content { display: flex; align-items: center; flex-wrap: wrap; gap: 10px; }
.list-page-layout__list { overflow: hidden; }
@media (max-width: 720px) {
  .list-page-layout__header { align-items: flex-start; flex-direction: column; }
  .list-page-layout__stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .list-page-layout__section-mark { display: none; }
}
@media (max-width: 960px) and (min-width: 721px) { .list-page-layout__stats-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 480px) { .list-page-layout__stats-grid { grid-template-columns: 1fr; } }
</style>
