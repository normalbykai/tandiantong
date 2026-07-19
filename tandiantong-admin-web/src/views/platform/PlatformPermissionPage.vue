<template>
  <ListPageLayout title="平台权限" description="平台权限点由系统维护，用于角色授权和接口级访问控制；当前仅支持查看。" eyebrow="权限管理">
    <template #stats>
      <div class="list-stat"><span class="list-stat__label">全部权限</span><strong class="list-stat__value">{{ permissions.length }}</strong></div>
      <div v-for="(count, type) in typedCounts" :key="type" class="list-stat">
        <span class="list-stat__label">{{ type }}</span><strong class="list-stat__value is-muted">{{ count }}</strong>
      </div>
    </template>

    <template #actions>
      <el-button :icon="RefreshCw" :loading="loading" @click="load">刷新列表</el-button>
    </template>

    <template #filters>
      <div class="page-filter-field"><el-input v-model="keyword" :prefix-icon="Search" clearable placeholder="搜索权限名称或编码" @keyup.enter="load" /></div>
      <el-select v-model="typeFilter" class="page-filter-select" clearable placeholder="权限类型"><el-option v-for="type in permissionTypes" :key="type" :label="type" :value="type" /></el-select>
      <el-button @click="resetFilters">重置</el-button>
      <span class="page-filter-result">查询结果 <b>{{ filteredPermissions.length }}</b> 条</span>
    </template>

    <AppDataTable :data="filteredPermissions" :loading="loading" row-key="id" empty-text="暂无平台权限点" :total="filteredPermissions.length">
      <el-table-column label="权限名称" min-width="200">
        <template #default="{ row }"><b class="primary-cell">{{ row.name }}</b></template>
      </el-table-column>
      <el-table-column prop="permissionCode" label="权限编码" min-width="340">
        <template #default="{ row }"><code>{{ row.permissionCode }}</code></template>
      </el-table-column>
      <el-table-column label="权限类型" width="150">
        <template #default="{ row }"><el-tag>{{ row.permissionType }}</el-tag></template>
      </el-table-column>
    </AppDataTable>
  </ListPageLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RefreshCw, Search } from 'lucide-vue-next'
import ListPageLayout from '../../components/common/ListPageLayout.vue'
import AppDataTable from '../../components/common/AppDataTable.vue'
import { listPlatformPermissions } from '../../api/platform/access'
import type { PlatformPermission } from '../../types/platform-access'
import { message } from '../../utils/message'

const permissions = ref<PlatformPermission[]>([]); const loading = ref(false); const keyword = ref(''); const typeFilter = ref<string>()
const permissionTypes = computed(() => [...new Set(permissions.value.map(item => item.permissionType))])
const typedCounts = computed(() => { const map: Record<string, number> = {}; for (const item of permissions.value) map[item.permissionType] = (map[item.permissionType] ?? 0) + 1; return map })
const filteredPermissions = computed(() => permissions.value.filter(item => { const value = keyword.value.trim(); return (!value || `${item.name}${item.permissionCode}`.includes(value)) && (!typeFilter.value || item.permissionType === typeFilter.value) }))
function resetFilters() { keyword.value = ''; typeFilter.value = undefined }
async function load() { loading.value = true; try { permissions.value = await listPlatformPermissions() } catch (error) { message.error(error instanceof Error ? error.message : '平台权限点加载失败') } finally { loading.value = false } }
onMounted(load)
</script>

<style scoped>
.page-filter-field { width: min(420px, 100%); }
.page-filter-select { width: 150px; }
.page-filter-result { margin-left: auto; color: #89958e; font-size: 12px; }
.page-filter-result b { color: var(--domain-700); font-weight: 650; }

:deep(.el-table__header th.el-table__cell) { font-size: 12px; font-weight: 600; color: #5e6b64; }
:deep(.el-table__header th.el-table__cell:first-child .cell) { padding-left: 16px; }
:deep(.el-table__body td.el-table__cell) { font-size: 13.5px; padding: 14px 0; color: #2d3831; }
:deep(.el-table__body td.el-table__cell:first-child .cell) { padding-left: 16px; }
:deep(.el-table__body td.el-table__cell .cell) { line-height: 1.6; }
:deep(.primary-cell) { font-size: 14px; }

@media (max-width: 680px) {
  .page-filter-field, .page-filter-select { width: 100%; }
  .page-filter-result { width: 100%; margin-left: 0; }
  :deep(.el-table__header th.el-table__cell:first-child .cell),
  :deep(.el-table__body td.el-table__cell:first-child .cell) { padding-left: 12px; }
}
</style>
