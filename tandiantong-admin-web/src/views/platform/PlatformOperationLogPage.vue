<template>
  <ListPageLayout title="操作日志" description="记录平台管理端的全部敏感操作，用于审计追溯。日志仅支持查看，不允许删除。" eyebrow="平台管理">
    <template #stats>
      <div class="list-stat"><span class="list-stat__label">日志总数</span><strong class="list-stat__value">{{ total }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">本页日志</span><strong class="list-stat__value is-muted">{{ logs.length }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">涉及操作人</span><strong class="list-stat__value is-success">{{ operatorCount }}</strong></div>
      <div class="list-stat"><span class="list-stat__label">最近追踪号</span><strong class="list-stat__value is-warning">{{ logs[0]?.traceId ?? '—' }}</strong></div>
    </template>

    <template #actions>
      <el-button :icon="RefreshCw" :loading="loading" @click="load">刷新列表</el-button>
    </template>

    <template #filters>
      <div class="page-filter-field">
        <el-input
          v-model="filters.keyword"
          :prefix-icon="Search"
          clearable
          placeholder="搜索操作人、对象、追踪号或详情"
          @keyup.enter="applyFilters"
        />
      </div>
      <el-select v-model="filters.operationType" class="page-filter-select" clearable placeholder="操作类型">
        <el-option v-for="item in operationTypeOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="filters.targetType" class="page-filter-select" clearable placeholder="对象类型">
        <el-option v-for="item in targetTypeOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-input v-model="filters.traceId" class="page-filter-input" clearable placeholder="追踪号" />
      <el-date-picker
        v-model="dateRange"
        class="page-filter-range"
        type="daterange"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD"
      />
      <el-button @click="resetFilters">重置</el-button>
      <el-button type="primary" @click="applyFilters">查询</el-button>
      <span class="page-filter-result">查询结果 <b>{{ total }}</b> 条</span>
    </template>

    <AppDataTable
      :data="logs"
      :loading="loading"
      row-key="id"
      empty-text="暂无平台操作日志"
      :total="total"
      :current-page="page"
      :page-size="pageSize"
      show-pagination
      @page-change="changePage"
      @page-size-change="changePageSize"
    >
      <el-table-column label="操作时间" min-width="180">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作人" min-width="180">
        <template #default="{ row }">
          <div class="operator-cell">
            <b>{{ row.operatorName ?? `用户 #${row.operatorId}` }}</b>
            <span>{{ row.operatorMobile ?? '—' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="operationType" label="操作类型" min-width="160">
        <template #default="{ row }"><el-tag effect="plain">{{ row.operationType }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="targetType" label="对象类型" min-width="140" />
      <el-table-column prop="targetId" label="对象标识" min-width="180" />
      <el-table-column prop="traceId" label="追踪号" min-width="180">
        <template #default="{ row }"><code>{{ row.traceId || '—' }}</code></template>
      </el-table-column>
      <el-table-column label="来源" min-width="180">
        <template #default="{ row }">{{ row.requestMethod || '—' }} {{ row.userIp || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </AppDataTable>
  </ListPageLayout>

  <el-drawer v-model="detailVisible" title="操作日志详情" direction="rtl" size="520px" class="log-drawer">
    <template v-if="currentLog">
      <div class="log-summary">
        <div class="log-summary__item">
          <span>操作类型</span>
          <strong>{{ currentLog.operationType }}</strong>
        </div>
        <div class="log-summary__item">
          <span>对象类型</span>
          <strong>{{ currentLog.targetType }}</strong>
        </div>
        <div class="log-summary__item">
          <span>对象标识</span>
          <strong>{{ currentLog.targetId || '—' }}</strong>
        </div>
      </div>
      <div class="log-detail-grid">
        <div><span>操作人</span><strong>{{ currentLog.operatorName ?? `用户 #${currentLog.operatorId}` }}</strong></div>
        <div><span>手机号</span><strong>{{ currentLog.operatorMobile ?? '—' }}</strong></div>
        <div><span>操作时间</span><strong>{{ formatTime(currentLog.createdAt) }}</strong></div>
        <div><span>追踪号</span><strong><code>{{ currentLog.traceId || '—' }}</code></strong></div>
        <div><span>来源</span><strong>{{ currentLog.requestMethod || '—' }} {{ currentLog.requestUrl || '—' }}</strong></div>
        <div><span>来源 IP</span><strong>{{ currentLog.userIp || '—' }}</strong></div>
        <div class="log-detail-grid__full"><span>操作详情</span><strong>{{ currentLog.detail || '—' }}</strong></div>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { RefreshCw, Search } from 'lucide-vue-next'
import ListPageLayout from '../../components/common/ListPageLayout.vue'
import AppDataTable from '../../components/common/AppDataTable.vue'
import { listPlatformOperationLogs } from '../../api/platform/system'
import type { PlatformOperationLogItem } from '../../api/platform/system'
import { message } from '../../utils/message'

const loading = ref(false)
const logs = ref<PlatformOperationLogItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const filters = reactive({ keyword: '', operationType: '', targetType: '', traceId: '' })
const dateRange = ref<[string, string] | null>(null)
const detailVisible = ref(false)
const currentLog = ref<PlatformOperationLogItem>()

const operationTypeOptions = computed(() => [...new Set(logs.value.map(item => item.operationType))].filter(Boolean))
const targetTypeOptions = computed(() => [...new Set(logs.value.map(item => item.targetType))].filter(Boolean))
const operatorCount = computed(() => new Set(logs.value.map(item => item.operatorId)).size)

const formatTime = (value: string) =>
  value
    ? new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'medium', hour12: false }).format(new Date(value))
    : '—'

async function load() {
  loading.value = true
  try {
    const result = await listPlatformOperationLogs({
      keyword: filters.keyword.trim() || undefined,
      operationType: filters.operationType || undefined,
      targetType: filters.targetType || undefined,
      traceId: filters.traceId.trim() || undefined,
      startDate: Array.isArray(dateRange.value) ? dateRange.value[0] : undefined,
      endDate: Array.isArray(dateRange.value) ? dateRange.value[1] : undefined,
      page: page.value,
      pageSize: pageSize.value
    })
    logs.value = result.records
    total.value = result.total
    page.value = result.current
    pageSize.value = result.pageSize
  } catch (error) {
    message.error(error instanceof Error ? error.message : '平台操作日志加载失败')
  } finally {
    loading.value = false
  }
}

async function applyFilters() {
  page.value = 1
  await load()
}

function resetFilters() {
  filters.keyword = ''
  filters.operationType = ''
  filters.targetType = ''
  filters.traceId = ''
  dateRange.value = null
  page.value = 1
  load()
}

function changePage(nextPage: number) {
  page.value = nextPage
  load()
}

function changePageSize(size: number) {
  pageSize.value = size
  page.value = 1
  load()
}

function openDetail(log: PlatformOperationLogItem) {
  currentLog.value = log
  detailVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.page-filter-field { width: min(360px, 100%); }
.page-filter-select { width: 160px; }
.page-filter-input { width: 180px; }
.page-filter-range { width: 250px; }
.page-filter-result { margin-left: auto; color: #89958e; font-size: 12px; }
.page-filter-result b { color: var(--domain-700); font-weight: 650; }
.operator-cell { display: grid; gap: 2px; }
.operator-cell b { font-size: 14px; font-weight: 550; }
.operator-cell span { color: #7f8a83; font-size: 12px; }
.log-summary { display: grid; gap: 10px; margin-bottom: 18px; padding: 14px; border: 1px solid #e3ebe5; border-radius: 12px; background: #fbfcfb; }
.log-summary__item { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.log-summary__item span, .log-detail-grid span { color: #7c8981; font-size: 12px; }
.log-summary__item strong, .log-detail-grid strong { color: #25332c; font-size: 13px; font-weight: 600; }
.log-detail-grid { display: grid; gap: 14px; }
.log-detail-grid > div { display: grid; gap: 4px; padding-bottom: 12px; border-bottom: 1px solid #edf1ee; }
.log-detail-grid__full { grid-column: 1 / -1; }
.log-drawer :deep(.el-drawer__body) { padding: 20px; }
@media (max-width: 680px) {
  .page-filter-field, .page-filter-select, .page-filter-input, .page-filter-range { width: 100%; }
  .page-filter-result { width: 100%; margin-left: 0; }
}
</style>
