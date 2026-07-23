<template>
  <PageHeader title="商户权限" description="查看当前商户可使用的权限点。权限由系统维护，商户管理员只能将本商户权限分配给商户角色。" eyebrow="权限管理" />
  <el-alert title="权限说明仅展示商户权限域，不包含平台账号、平台角色、租户管理等平台权限。" type="info" :closable="false" show-icon class="merchant-notice" />
  <InkCard v-loading="loading"><div class="permission-toolbar"><el-input v-model="keyword" clearable placeholder="搜索权限名称或编码" :prefix-icon="Search" /><el-select v-model="module" clearable placeholder="所属模块"><el-option v-for="item in modules" :key="item" :label="item" :value="item" /></el-select></div><el-table :data="filteredPermissions" class="data-table"><el-table-column prop="name" label="权限名称" min-width="190" /><el-table-column prop="permissionType" label="权限类型" width="110" /><el-table-column prop="permissionCode" label="权限编码" min-width="280"><template #default="{row}"><code>{{ row.permissionCode }}</code></template></el-table-column><el-table-column label="权限域" width="100"><template #default><el-tag type="success">商户</el-tag></template></el-table-column></el-table></InkCard>
</template>
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Search } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import InkCard from '../../components/common/InkCard.vue'
import { message } from '../../utils/message'
import { listMerchantPermissions, type MerchantPermission } from '../../api/merchant/infrastructure'
const keyword = ref('')
const module = ref('')
const loading = ref(false); const permissions = ref<MerchantPermission[]>([])
const modules = ['工作台', '门店信息', '员工账号', '商户角色', '操作日志']
const filteredPermissions = computed(() => permissions.value.filter(item => {
  const matchesKeyword = !keyword.value || `${item.name}${item.permissionCode}`.includes(keyword.value)
  return matchesKeyword && (!module.value || item.name.includes(module.value))
}))
async function load() { loading.value = true; try { permissions.value = await listMerchantPermissions() } catch (error) { message.error(error instanceof Error ? error.message : '商户权限加载失败') } finally { loading.value = false } }
onMounted(load)
</script>
<style scoped>.merchant-notice{margin-bottom:18px}.permission-toolbar{display:flex;gap:10px;padding:18px 20px;border-bottom:1px solid #ebeef1}.permission-toolbar .el-input{max-width:320px}.permission-toolbar .el-select{width:160px}code{color:var(--domain-700);font:12px ui-monospace,Consolas,monospace}@media(max-width:560px){.permission-toolbar{flex-direction:column}.permission-toolbar .el-input,.permission-toolbar .el-select{max-width:none;width:100%}}</style>
