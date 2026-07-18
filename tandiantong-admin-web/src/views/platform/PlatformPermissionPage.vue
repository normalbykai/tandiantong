<template>
  <PageHeader title="平台权限" description="平台权限点由系统维护，用于角色授权和接口级访问控制，当前仅支持查看。" eyebrow="权限管理" />
  <div class="info-banner warning-banner">权限点编码由系统维护，本页面仅供查看；新增或修改权限点需要通过系统配置流程完成。</div><section class="filter-panel"><div class="filter-field filter-keyword"><label>权限名称 / 编码</label><el-input v-model="keyword" :prefix-icon="Search" clearable placeholder="请输入权限名称或编码" /></div><div class="filter-field"><label>权限类型</label><el-select v-model="typeFilter" clearable placeholder="全部类型"><el-option v-for="type in permissionTypes" :key="type" :label="type" :value="type" /></el-select></div><div class="filter-actions"><el-button @click="resetFilters">重置</el-button><el-button :icon="RefreshCw" :loading="loading" @click="load">刷新</el-button></div></section><section class="content-card"><div class="table-summary">共 <b>{{ filteredPermissions.length }}</b> 条记录</div><el-table v-loading="loading" :data="filteredPermissions" class="data-table" empty-text="暂无平台权限点"><el-table-column label="权限名称" min-width="200"><template #default="{ row }"><b class="primary-cell">{{ row.name }}</b></template></el-table-column><el-table-column prop="permissionCode" label="权限编码" min-width="340"><template #default="{ row }"><code>{{ row.permissionCode }}</code></template></el-table-column><el-table-column label="权限类型" width="150"><template #default="{ row }"><el-tag class="type-tag">{{ row.permissionType }}</el-tag></template></el-table-column></el-table></section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RefreshCw, Search } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import { listPlatformPermissions } from '../../api/platform/access'
import type { PlatformPermission } from '../../types/platform-access'
const permissions = ref<PlatformPermission[]>([]); const loading = ref(false); const keyword = ref(''); const typeFilter = ref<string>()
const permissionTypes = computed(() => [...new Set(permissions.value.map(item => item.permissionType))])
const filteredPermissions = computed(() => permissions.value.filter(item => { const value = keyword.value.trim(); return (!value || `${item.name}${item.permissionCode}`.includes(value)) && (!typeFilter.value || item.permissionType === typeFilter.value) }))
function resetFilters() { keyword.value = ''; typeFilter.value = undefined }
async function load() { loading.value = true; try { permissions.value = await listPlatformPermissions() } catch (error) { ElMessage.error(error instanceof Error ? error.message : '平台权限点加载失败') } finally { loading.value = false } }
onMounted(load)
</script>
