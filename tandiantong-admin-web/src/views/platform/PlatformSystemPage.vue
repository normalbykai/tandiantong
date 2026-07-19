<template>
  <PageHeader title="系统管理" description="管理平台设置、字典与账号安全策略。" />
  <section class="system-workspace-nav" aria-label="系统管理功能选择">
    <el-segmented v-model="activeSection" :options="sectionOptions" class="system-section-switch" aria-label="系统管理功能选择" />
  </section>

  <template v-if="activeSection === 'BRAND'">
    <section class="content-card system-config-card">
      <div class="card-heading system-card-heading"><div><span class="system-card-kicker">BRAND SETTINGS</span><h2>平台基础设置</h2><p>维护平台品牌信息，保存后将用于平台管理端的品牌展示。</p></div><el-tag type="info">平台权限域</el-tag></div>
      <el-form ref="configFormRef" :model="configForm" :rules="configRules" label-position="top" class="system-config-form" @submit.prevent="saveConfig">
        <div class="system-config-layout">
          <div class="logo-preview-panel"><div class="config-block-title"><span class="config-block-icon">标</span><div><strong>品牌预览</strong><span>实时查看 Logo 展示效果</span></div></div><div class="logo-preview"><img v-if="configForm.logoUrl && !logoLoadError" :src="configForm.logoUrl" alt="平台 Logo" @error="logoLoadError = true" /><span v-if="logoLoadError || !configForm.logoUrl">摊点通</span></div><p>建议使用清晰的 SVG 或 PNG 图片地址，图片地址需要支持公开访问。</p></div>
          <div class="system-config-fields"><div class="config-block-title"><span class="config-block-icon">编</span><div><strong>品牌信息</strong><span>配置平台名称之外的展示内容</span></div></div><el-form-item label="Logo 图片地址" prop="logoUrl"><el-input v-model="configForm.logoUrl" clearable placeholder="请输入可访问的图片地址" @input="logoLoadError = false" /></el-form-item><el-form-item label="平台描述" prop="description"><el-input v-model="configForm.description" type="textarea" :rows="5" maxlength="255" show-word-limit placeholder="请输入平台描述信息" /></el-form-item><div class="form-actions"><el-button type="primary" :loading="configSaving" @click="saveConfig">保存平台设置</el-button></div></div>
        </div>
      </el-form>
    </section>
  </template>

  <template v-else-if="activeSection === 'DICTIONARY'">
    <section class="content-card dictionary-page-card">
      <div class="dictionary-page-heading"><div><span class="system-card-kicker">PLATFORM DICTIONARY</span><h2>平台字典</h2><p>按字典类型维护可复用选项。编码用于识别，存储值写入业务数据，名称用于页面展示。</p></div><el-button type="primary" @click="openCreate()">新增字典项</el-button></div>
      <div class="dictionary-toolbar"><div class="dictionary-filter-field"><span>字典类型</span><el-input v-model="dictionaryTypeFilter" clearable placeholder="例如：ORDER_STATUS" @keyup.enter="loadDictionaries" /></div><el-button :icon="RefreshCw" :loading="dictionaryLoading" @click="loadDictionaries">刷新</el-button><span class="dictionary-result-hint">{{ dictionaryItems.length }} 个字典项 · {{ dictionaryGroups.length }} 个类型</span></div>
      <div v-loading="dictionaryLoading" class="dictionary-groups">
        <section v-for="group in dictionaryGroups" :key="group.dictionaryType" class="dictionary-group-card">
          <div class="dictionary-group-heading"><div class="dictionary-group-title"><span class="dictionary-type-mark">字</span><div><strong>{{ group.dictionaryType }}</strong><span>{{ group.items.length }} 个选项 · {{ group.enabledCount }} 个启用</span></div></div><el-button link type="primary" @click="openCreate(group.dictionaryType)">在此类型下新增</el-button></div>
          <el-table :data="group.items" class="data-table dictionary-group-table" empty-text="暂无字典项"><el-table-column prop="itemCode" label="字典项编码" min-width="150"><template #default="{ row }"><code>{{ row.itemCode }}</code></template></el-table-column><el-table-column prop="itemValue" label="业务存储值" min-width="180"><template #default="{ row }"><span class="dictionary-value">{{ row.itemValue }}</span></template></el-table-column><el-table-column prop="itemLabel" label="显示名称" min-width="150" /><el-table-column prop="sortOrder" label="排序" width="80" /><el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ row.status === 'ENABLED' ? '启用' : '停用' }}</el-tag></template></el-table-column><el-table-column label="操作" width="150" fixed="right"><template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link :type="row.status === 'ENABLED' ? 'danger' : 'primary'" @click="toggleStatus(row)">{{ row.status === 'ENABLED' ? '停用' : '启用' }}</el-button></template></el-table-column></el-table>
        </section>
        <el-empty v-if="!dictionaryLoading && dictionaryGroups.length === 0" description="暂无匹配的字典项" />
      </div>
    </section>
  </template>

  <template v-else>
    <section class="content-card system-config-card security-settings-card">
      <div class="card-heading system-card-heading"><h2>账号中心</h2></div>
      <el-form ref="configFormRef" :model="configForm" :rules="configRules" label-position="top" class="system-config-form" @submit.prevent="saveConfig">
        <div class="security-policy-panel"><el-form-item label="重置密码方式" prop="resetPasswordMode"><el-radio-group v-model="configForm.resetPasswordMode"><el-radio value="RANDOM">随机生成临时密码</el-radio><el-radio value="FIXED">使用固定临时密码</el-radio></el-radio-group></el-form-item><el-form-item v-if="configForm.resetPasswordMode === 'FIXED'" label="固定临时密码" prop="fixedResetPassword"><el-input v-model="configForm.fixedResetPassword" type="password" show-password autocomplete="new-password" placeholder="请输入8至64位密码；留空表示保留原配置" /></el-form-item><div class="form-actions"><el-button type="primary" :loading="configSaving" @click="saveConfig">保存账号安全设置</el-button></div></div>
      </el-form>
    </section>
  </template>

  <el-dialog v-model="dictionaryDialogVisible" :title="editingItem ? '编辑字典项' : '新增字典项'" width="560px" class="dictionary-dialog"><div class="dictionary-dialog-intro"><span class="config-block-icon">值</span><p>同一字典类型可以配置多个选项。存储值会写入业务数据，请保持稳定且不要重复。</p></div><el-form ref="dictionaryFormRef" :model="dictionaryForm" :rules="dictionaryRules" label-position="top" class="dictionary-form"><el-form-item label="字典类型编码" prop="dictionaryType"><el-input v-model="dictionaryForm.dictionaryType" :disabled="Boolean(editingItem)" placeholder="例如：ORDER_STATUS" /></el-form-item><el-form-item label="字典项编码" prop="itemCode"><el-input v-model="dictionaryForm.itemCode" :disabled="Boolean(editingItem)" placeholder="例如：PENDING" /></el-form-item><el-form-item label="业务存储值" prop="itemValue"><el-input v-model="dictionaryForm.itemValue" :disabled="Boolean(editingItem)" maxlength="255" show-word-limit placeholder="例如：pending" /></el-form-item><el-form-item label="显示名称" prop="itemLabel"><el-input v-model="dictionaryForm.itemLabel" maxlength="128" show-word-limit placeholder="例如：待支付" /></el-form-item><el-form-item label="排序值" prop="sortOrder"><el-input-number v-model="dictionaryForm.sortOrder" :min="0" :max="9999" /></el-form-item></el-form><template #footer><el-button @click="dictionaryDialogVisible = false">取消</el-button><el-button type="primary" :loading="dictionarySaving" @click="saveDictionary">保存字典项</el-button></template></el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { RefreshCw } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import { createPlatformDictionaryItem, getPlatformSystemConfig, listPlatformDictionaryItems, updatePlatformDictionaryItem, updatePlatformDictionaryItemStatus, updatePlatformSystemConfig } from '../../api/platform/system'
import type { CreatePlatformDictionaryItemCommand, PlatformDictionaryItem, UpdatePlatformSystemConfigCommand } from '../../types/platform-system'
import { message } from '../../utils/message'

type SystemSection = 'BRAND' | 'DICTIONARY' | 'SECURITY'
const sectionOptions: Array<{ label: string; value: SystemSection }> = [{ label: '平台设置', value: 'BRAND' }, { label: '字典管理', value: 'DICTIONARY' }, { label: '安全设置', value: 'SECURITY' }]
const activeSection = ref<SystemSection>('BRAND')
const configFormRef = ref<FormInstance>()
const configForm = reactive<UpdatePlatformSystemConfigCommand>({ logoUrl: '', description: '', resetPasswordMode: 'RANDOM' })
const configSaving = ref(false)
const logoLoadError = ref(false)
const configRules: FormRules = { logoUrl: [{ required: true, message: '请输入 Logo 图片地址', trigger: 'blur' }], description: [{ required: true, message: '请输入平台描述', trigger: 'blur' }], resetPasswordMode: [{ required: true, message: '请选择重置密码方式', trigger: 'change' }], fixedResetPassword: [{ min: 8, max: 64, message: '固定临时密码长度需为8至64位', trigger: 'blur' }] }
const dictionaryItems = ref<PlatformDictionaryItem[]>([])
const dictionaryGroups = computed(() => {
  const groups = new Map<string, PlatformDictionaryItem[]>()
  for (const item of dictionaryItems.value) groups.set(item.dictionaryType, [...(groups.get(item.dictionaryType) ?? []), item])
  return [...groups.entries()].map(([dictionaryType, items]) => ({ dictionaryType, items, enabledCount: items.filter(item => item.status === 'ENABLED').length }))
})
const dictionaryLoading = ref(false)
const dictionarySaving = ref(false)
const dictionaryTypeFilter = ref('')
const dictionaryDialogVisible = ref(false)
const dictionaryFormRef = ref<FormInstance>()
const editingItem = ref<PlatformDictionaryItem>()
const dictionaryForm = reactive<CreatePlatformDictionaryItemCommand>({ dictionaryType: '', itemCode: '', itemValue: '', itemLabel: '', sortOrder: 0 })
const dictionaryRules: FormRules = { dictionaryType: [{ required: true, message: '请输入字典类型编码', trigger: 'blur' }], itemCode: [{ required: true, message: '请输入字典项编码', trigger: 'blur' }], itemValue: [{ required: true, message: '请输入业务存储值', trigger: 'blur' }, { max: 255, message: '业务存储值不能超过255个字符', trigger: 'blur' }], itemLabel: [{ required: true, message: '请输入显示名称', trigger: 'blur' }], sortOrder: [{ required: true, message: '请输入排序值', trigger: 'change' }] }

async function loadConfig() { try { Object.assign(configForm, await getPlatformSystemConfig()) } catch (error) { message.error(error instanceof Error ? error.message : '平台设置加载失败') } }
async function saveConfig() { if (!(await configFormRef.value?.validate().catch(() => false))) return; configSaving.value = true; try { Object.assign(configForm, await updatePlatformSystemConfig(configForm)); message.success('平台设置已保存') } catch (error) { message.error(error instanceof Error ? error.message : '平台设置保存失败') } finally { configSaving.value = false } }
async function loadDictionaries() { dictionaryLoading.value = true; try { dictionaryItems.value = await listPlatformDictionaryItems(dictionaryTypeFilter.value.trim() || undefined) } catch (error) { message.error(error instanceof Error ? error.message : '平台字典加载失败') } finally { dictionaryLoading.value = false } }
function openCreate(dictionaryType = '') { editingItem.value = undefined; Object.assign(dictionaryForm, { dictionaryType, itemCode: '', itemValue: '', itemLabel: '', sortOrder: 0 }); dictionaryDialogVisible.value = true }
function openEdit(item: PlatformDictionaryItem) { editingItem.value = item; Object.assign(dictionaryForm, item); dictionaryDialogVisible.value = true }
async function saveDictionary() { if (!(await dictionaryFormRef.value?.validate().catch(() => false))) return; dictionarySaving.value = true; try { if (editingItem.value) await updatePlatformDictionaryItem(editingItem.value.id, { itemLabel: dictionaryForm.itemLabel, sortOrder: dictionaryForm.sortOrder }); else await createPlatformDictionaryItem(dictionaryForm); message.success('平台字典项已保存'); dictionaryDialogVisible.value = false; await loadDictionaries() } catch (error) { message.error(error instanceof Error ? error.message : '平台字典项保存失败') } finally { dictionarySaving.value = false } }
async function toggleStatus(item: PlatformDictionaryItem) { const enabled = item.status !== 'ENABLED'; try { await ElMessageBox.confirm(`确认${enabled ? '启用' : '停用'}“${item.itemLabel}”吗？`, '字典项状态确认', { type: 'warning' }); await updatePlatformDictionaryItemStatus(item.id, enabled); message.success('字典项状态已更新'); await loadDictionaries() } catch (error) { if (error !== 'cancel') message.error(error instanceof Error ? error.message : '字典项状态更新失败') } }
onMounted(loadConfig)
onMounted(loadDictionaries)
</script>

<style scoped>
.system-workspace-nav { display: flex; align-items: center; justify-content: flex-start; gap: 12px; margin-bottom: 22px; padding: 0; }
.system-card-kicker { color: var(--domain-600); font-family: ui-monospace, Consolas, monospace; font-size: 10px; font-weight: 700; letter-spacing: 1.2px; }
.system-section-switch { --el-segmented-color: #708077; --el-segmented-item-selected-color: var(--domain-700); --el-segmented-item-selected-bg-color: #fff; --el-segmented-item-hover-color: var(--domain-700); --el-segmented-item-hover-bg-color: rgba(255, 255, 255, .72); --el-segmented-bg-color: #eef4f0; --el-segmented-padding: 4px; min-height: 50px; flex: 0 1 auto; padding: 4px; border: 1px solid #d6e3da; border-radius: 999px; background: linear-gradient(135deg, #f3f7f4, #eaf2ed); box-shadow: inset 0 1px 2px rgba(24, 61, 43, .05), 0 4px 12px -6px rgba(27, 67, 50, .3); }
.system-section-switch :deep(.el-segmented__group) { gap: 3px; }
.system-section-switch :deep(.el-segmented__item) { position: relative; min-width: 126px; min-height: 40px; padding: 0 20px; border: 1px solid transparent; border-radius: 999px; color: #68776e; font-size: 14px; font-weight: 600; transition: color 180ms ease, background-color 180ms ease, border-color 180ms ease, box-shadow 180ms ease, transform 180ms ease; }
.system-section-switch :deep(.el-segmented__item:hover) { color: var(--domain-700); transform: translateY(-1px); }
.system-section-switch :deep(.el-segmented__item-selected) { border-color: #cfe0d5; background: linear-gradient(180deg, #fff, #f9fcfa); box-shadow: 0 5px 12px -7px rgba(27, 67, 50, .46), 0 1px 3px rgba(27, 67, 50, .08); }
.system-config-card { padding: 30px; }
.security-settings-card { max-width: 860px; }
.security-settings-card .system-card-heading { margin-bottom: 22px; }
.security-settings-card .system-card-heading h2 { margin-top: 0; }
.security-policy-panel { max-width: 620px; padding: 22px 24px; border: 1px solid #e5ebe7; border-radius: 12px; background: #fbfcfb; }
.system-card-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 1px solid #edf1ee; }
.system-card-heading h2, .table-summary h2 { margin: 5px 0 0; color: #25332c; font-size: 20px; font-weight: 650; letter-spacing: -.2px; }
.system-card-heading p, .table-summary p { margin: 8px 0 0; color: #7b858d; font-size: 12px; line-height: 1.65; }
.system-config-layout { display: grid; grid-template-columns: minmax(230px, .7fr) minmax(0, 1.5fr); gap: 26px; }
.logo-preview-panel, .system-config-fields { min-width: 0; padding: 20px; border: 1px solid #e5ebe7; border-radius: 13px; background: #fbfcfb; }
.config-block-title { display: flex; align-items: center; gap: 10px; margin-bottom: 18px; }
.config-block-title > div { display: grid; gap: 3px; }
.config-block-title strong { color: #34443b; font-size: 14px; font-weight: 650; }
.config-block-title span:last-child { color: #98a39d; font-size: 11px; }
.config-block-icon { display: grid; width: 30px; height: 30px; place-items: center; border: 1px solid #cfe0d5; border-radius: 9px; background: #edf5f0; color: var(--domain-700); font-size: 12px; font-weight: 700; }
.logo-preview { display: grid; width: 100%; min-height: 150px; place-items: center; border: 1px dashed #bfd1c4; border-radius: 11px; background: radial-gradient(circle at 70% 20%, #fff, transparent 45%), #f1f7f3; color: #1b4332; font-size: 21px; font-weight: 700; letter-spacing: 1px; }
.logo-preview img { max-width: 85%; max-height: 110px; }
.logo-preview-panel p { margin: 12px 0 0; color: #929ba1; font-size: 11px; line-height: 1.7; }
.system-config-fields :deep(.el-form-item) { margin-bottom: 20px; }
.system-config-fields :deep(.el-form-item__label) { color: #56645c; font-size: 12px; font-weight: 600; }
.form-actions { display: flex; justify-content: flex-end; margin-top: 4px; padding-top: 18px; border-top: 1px solid #edf1ee; }
.dictionary-page-card { overflow: hidden; }
.dictionary-page-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 18px; padding: 24px 24px 20px; border-bottom: 1px solid #edf1ee; }
.dictionary-page-heading h2 { margin: 5px 0 0; color: #25332c; font-size: 20px; font-weight: 650; }
.dictionary-page-heading p { max-width: 720px; margin: 8px 0 0; color: #7b858d; font-size: 12px; line-height: 1.65; }
.dictionary-toolbar { display: flex; align-items: flex-end; flex-wrap: wrap; gap: 10px; padding: 16px 24px; border-bottom: 1px solid #edf1ee; background: #fbfcfb; }
.dictionary-filter-field { display: grid; gap: 5px; min-width: min(300px, 100%); }
.dictionary-filter-field > span { color: #738078; font-size: 11px; font-weight: 600; }
.dictionary-filter-field .el-input { width: 300px; max-width: 100%; }
.dictionary-result-hint { align-self: center; margin-left: auto; color: #8b9690; font-size: 11px; }
.dictionary-groups { display: grid; gap: 16px; padding: 20px 24px 24px; }
.dictionary-group-card { overflow: hidden; border: 1px solid #e1e9e3; border-radius: 12px; background: #fff; }
.dictionary-group-heading { display: flex; align-items: center; justify-content: space-between; gap: 14px; padding: 14px 16px; background: linear-gradient(100deg, #f4f8f5, #fbfcfb); }
.dictionary-group-title { display: flex; align-items: center; gap: 10px; }
.dictionary-group-title > div { display: grid; gap: 3px; }
.dictionary-group-title strong { color: #2e4337; font-family: ui-monospace, Consolas, monospace; font-size: 13px; letter-spacing: .2px; }
.dictionary-group-title span:last-child { color: #8a968e; font-size: 11px; }
.dictionary-type-mark { display: grid; width: 30px; height: 30px; place-items: center; border-radius: 9px; background: #e5f0e8; color: var(--domain-700); font-size: 13px; font-weight: 700; }
.dictionary-group-table :deep(.el-table__inner-wrapper::before) { display: none; }
.dictionary-group-table :deep(.el-table__header th.el-table__cell) { background: #fff; color: #7b8780; font-size: 11px; font-weight: 600; }
.dictionary-group-table :deep(.el-table__body td.el-table__cell) { padding: 13px 0; }
.dictionary-value { color: #2d6047; font-family: ui-monospace, Consolas, monospace; font-size: 12px; }
.dictionary-dialog-intro { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 18px; padding: 12px 14px; border: 1px solid #dcebe0; border-radius: 10px; background: #f5faf6; }
.dictionary-dialog-intro p { margin: 1px 0 0; color: #617269; font-size: 12px; line-height: 1.6; }
.dictionary-form { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); column-gap: 16px; }
.dictionary-form :deep(.el-form-item:nth-child(3)), .dictionary-form :deep(.el-form-item:nth-child(4)) { grid-column: span 1; }
.dictionary-form :deep(.el-form-item__label) { color: #56645c; font-size: 12px; font-weight: 600; }
.dictionary-form :deep(.el-input-number) { width: 100%; }
@media (max-width: 720px) { .system-workspace-nav { align-items: stretch; flex-direction: column; gap: 8px; }.system-section-switch { width: 100%; }.system-section-switch :deep(.el-segmented__item) { flex: 1; min-width: 0; padding: 0 12px; }.system-config-card { padding: 18px; }.system-card-heading { flex-direction: column; margin-bottom: 20px; }.system-config-layout { grid-template-columns: 1fr; gap: 16px; }.dictionary-page-heading { flex-direction: column; padding: 20px 18px; }.dictionary-page-heading .el-button { width: 100%; }.dictionary-toolbar { align-items: stretch; padding: 14px 18px; }.dictionary-filter-field { width: 100%; }.dictionary-filter-field .el-input { width: 100%; }.dictionary-result-hint { width: 100%; margin-left: 0; }.dictionary-groups { padding: 16px 18px 20px; }.dictionary-group-heading { align-items: flex-start; flex-direction: column; }.dictionary-form { display: block; } }
</style>
