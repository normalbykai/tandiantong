<template>
  <section class="app-data-table" :class="`app-data-table--${density}`">
    <div v-if="$slots.toolbar" class="app-data-table__toolbar">
      <slot name="toolbar" />
    </div>

    <el-table
      v-loading="loading"
      :data="data"
      :row-key="rowKey"
      :height="height"
      :max-height="maxHeight"
      :border="border"
      :stripe="stripe"
      :highlight-current-row="highlightCurrentRow"
      :empty-text="emptyText"
      class="data-table"
    >
      <slot />
    </el-table>

    <div v-if="$slots.footer || showPagination" class="app-data-table__footer">
      <slot name="footer">
        <span class="app-data-table__total">共 <b>{{ total ?? data.length }}</b> 条记录</span>
      </slot>
      <el-pagination
        v-if="showPagination"
        :current-page="currentPage"
        :page-size="pageSizeModel"
        :page-sizes="pageSizes"
        :total="total ?? data.length"
        :layout="paginationLayout"
        background
        @current-change="emit('page-change', $event)"
        @size-change="emit('page-size-change', $event)"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  data: unknown[]
  loading?: boolean
  rowKey?: string | ((row: unknown) => string | number)
  height?: string | number
  maxHeight?: string | number
  border?: boolean
  stripe?: boolean
  highlightCurrentRow?: boolean
  emptyText?: string
  density?: 'compact' | 'comfortable'
  total?: number
  showPagination?: boolean
  currentPage?: number
  pageSize?: number
  pageSizes?: number[]
  paginationLayout?: string
}>(), {
  loading: false,
  border: false,
  stripe: false,
  highlightCurrentRow: false,
  emptyText: '暂无数据',
  density: 'comfortable',
  showPagination: false,
  currentPage: 1,
  pageSize: 10,
  pageSizes: () => [10, 20, 50],
  paginationLayout: 'total, sizes, prev, pager, next, jumper',
})

const emit = defineEmits<{
  'page-change': [page: number]
  'page-size-change': [size: number]
}>()

const currentPage = computed(() => props.currentPage)
const pageSizeModel = computed(() => props.pageSize)
</script>

<style scoped>
.app-data-table { min-width: 0; }
.app-data-table__toolbar { padding: 16px 20px; border-bottom: 1px solid #ebeef1; }
.app-data-table__footer { display: flex; align-items: center; justify-content: space-between; gap: 16px; min-height: 58px; padding: 10px 20px; border-top: 1px solid #ebeef1; background: linear-gradient(180deg, #fff, #fafbfc); }
.app-data-table__total { color: #7b858d; font-size: 12px; }
.app-data-table__total b { color: var(--domain-700); font-weight: 650; }
.app-data-table--compact :deep(.el-table__cell) { padding: 7px 0; }
.app-data-table--comfortable :deep(.el-table__cell) { padding: 10px 0; }
.app-data-table :deep(.el-table) { --el-table-border-color: #ebeef1; --el-table-header-bg-color: #f5f7f8; --el-table-header-text-color: #59636b; --el-table-text-color: #30363b; --el-table-row-hover-bg-color: color-mix(in srgb, var(--domain-50) 72%, #fff); font-size: 13px; }
.app-data-table :deep(.el-table__header-wrapper th) { height: 44px; font-size: 12px; font-weight: 650; letter-spacing: .2px; }
.app-data-table :deep(.el-table__body tr) { position: relative; transition: background-color 160ms ease, box-shadow 160ms ease; }
.app-data-table :deep(.el-table__body td) { height: 54px; }
.app-data-table :deep(.el-table__body tr:hover > td),
.app-data-table :deep(.el-table__body tr.hover-row > td) { background: #f5faf7 !important; }
.app-data-table :deep(.el-table__body tr:hover > td:first-child),
.app-data-table :deep(.el-table__body tr.hover-row > td:first-child) { box-shadow: inset 3px 0 0 var(--domain-500); }
.app-data-table :deep(.el-table__body tr:hover .cell),
.app-data-table :deep(.el-table__body tr.hover-row .cell) { color: #202b26; }
.app-data-table :deep(.el-table__body tr:hover .el-table-fixed-column--right),
.app-data-table :deep(.el-table__body tr.hover-row .el-table-fixed-column--right) { background: #f5faf7 !important; }
.app-data-table :deep(.el-table__body tr:not(.hover-row):not(:hover) td) { transition: background-color 160ms ease, box-shadow 160ms ease; }
.app-data-table :deep(.el-tag) { display: inline-flex; align-items: center; min-height: 24px; padding: 2px 9px; border-radius: 999px; font-size: 11px; font-weight: 550; line-height: 1.45; letter-spacing: 0; white-space: nowrap; }
.app-data-table :deep(.el-tag--success) { color: #217047; border-color: #d7eadf; background: #f1f8f4; }
.app-data-table :deep(.el-tag--warning) { color: #93651d; border-color: #f0dfbc; background: #fdf8ed; }
.app-data-table :deep(.el-tag--danger) { color: #a23e3e; border-color: #efd4d4; background: #fcf3f3; }
.app-data-table :deep(.el-tag--info) { color: #596775; border-color: #dfe4e8; background: #f5f7f8; }
.app-data-table :deep(.el-tag--primary) { color: #30654c; border-color: #d7e7de; background: #f1f7f3; }
.app-data-table :deep(.el-table__empty-text) { color: #9aa3aa; font-size: 13px; }
.app-data-table :deep(.el-pagination) { --el-pagination-font-size: 12px; }
@media (max-width: 680px) {
  .app-data-table__footer { align-items: flex-start; flex-direction: column; }
  .app-data-table__footer :deep(.el-pagination) { flex-wrap: wrap; }
}
</style>
