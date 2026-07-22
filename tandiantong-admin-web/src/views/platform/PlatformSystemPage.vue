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
      <div class="dictionary-page-heading"><div><span class="system-card-kicker">PLATFORM DICTIONARY</span><h2>平台字典</h2><p>按字典类型维护可复用选项。编码用于识别，颜色类型用于展示，名称用于页面展示。</p></div></div>
      <div v-loading="dictionaryLoading" class="dictionary-workspace">
        <!-- 左侧：字典类型列表 -->
        <aside class="dictionary-type-panel">
          <div class="dictionary-type-search">
            <el-input v-model="dictionaryTypeFilter" clearable placeholder="搜索字典类型…" :prefix-icon="Search" size="default" @input="onTypeFilterChange" />
          </div>
          <nav class="dictionary-type-list">
            <el-card
              v-for="group in dictionaryGroups"
              :key="group.dictionaryType"
              :shadow="selectedType === group.dictionaryType ? 'always' : 'hover'"
              class="dictionary-type-card"
              :class="{ 'dictionary-type-card--active': selectedType === group.dictionaryType }"
              :body-style="{ padding: '12px 14px' }"
              @click="selectType(group.dictionaryType)"
            >
              <div class="dictionary-type-card__inner">
                <span class="dictionary-type-item__body">
                  <strong class="dictionary-type-item__code">{{ dictionary.typeLabel(group.dictionaryType) }}</strong>
                  <span v-if="dictionary.typeDescription(group.dictionaryType)" class="dictionary-type-item__desc">{{ dictionary.typeDescription(group.dictionaryType) }}</span>
                  <code class="dictionary-type-item__tag">{{ group.dictionaryType }}</code>
                  <span class="dictionary-type-item__meta">{{ group.items.length }} 个选项 · {{ group.enabledCount }} 个启用</span>
                </span>
              </div>
            </el-card>
            <el-empty v-if="dictionaryGroups.length === 0" description="暂无字典类型" :image-size="64" />
          </nav>
          <div class="dictionary-type-footer">
            <span>共 <b>{{ dictionaryGroups.length }}</b> 个类型</span>
            <el-button link type="primary" :icon="RefreshCw" :loading="dictionaryLoading" @click="loadDictionaries">刷新</el-button>
          </div>
        </aside>

        <!-- 右侧：字典项表格 -->
        <main class="dictionary-item-panel">
          <template v-if="selectedType">
            <el-card shadow="never" class="dictionary-item-card" :body-style="{ padding: '0' }">
              <div class="dictionary-item-toolbar">
                <div class="dictionary-item-toolbar__left">
                  <span class="dictionary-item-toolbar__title">{{ dictionary.typeLabel(selectedType) }}</span>
                  <el-tag size="small" type="info">{{ selectedGroupItems.length }} 个字典项</el-tag>
                </div>
                <div class="dictionary-item-toolbar__right">
                  <el-input v-model="itemSearchText" clearable placeholder="搜索字典项名称或编码…" size="default" style="width: 240px" />
                  <el-button type="primary" @click="openCreate(selectedType)">新增字典项</el-button>
                </div>
              </div>
              <div v-if="dictionary.typeDescription(selectedType)" class="dictionary-item-desc">
                <span class="dictionary-item-desc__label">类型标识：<code>{{ selectedType }}</code></span>
                <span>{{ dictionary.typeDescription(selectedType) }}</span>
              </div>
              <el-table
                :data="filteredSelectedItems"
                class="data-table dictionary-item-table"
                empty-text="暂无字典项，点击右上角「新增字典项」添加"
                row-key="id"
              >
                <el-table-column prop="itemCode" label="字典项编码" min-width="140">
                  <template #default="{ row }"><code>{{ row.itemCode }}</code></template>
                </el-table-column>
                <el-table-column prop="tagType" label="颜色类型" min-width="120">
                  <template #default="{ row }"><code>{{ row.tagType || 'info' }}</code></template>
                </el-table-column>
                <el-table-column prop="itemLabel" label="显示名称" min-width="140" />
                <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
                <el-table-column label="状态" width="90" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'" size="small">
                      {{ row.status === 'ENABLED' ? '启用' : '停用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150" fixed="right" align="center">
                  <template #default="{ row }">
                    <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
                    <el-button link size="small" :type="row.status === 'ENABLED' ? 'danger' : 'primary'" @click="toggleStatus(row)">
                      {{ row.status === 'ENABLED' ? '停用' : '启用' }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </template>
          <div v-else class="dictionary-item-empty">
            <span class="dictionary-item-empty__icon">字</span>
            <strong>选择字典类型</strong>
            <p>从左侧列表中选择一个字典类型，查看和管理其下的字典项。</p>
          </div>
        </main>
      </div>
    </section>
  </template>

  <template v-else>
    <section class="content-card system-config-card security-settings-card">
      <div class="card-heading system-card-heading"><h2>账号中心</h2></div>
      <el-form ref="configFormRef" :model="configForm" :rules="configRules" label-position="top" class="system-config-form" @submit.prevent="saveConfig">
        <div class="security-policy-panel"><el-form-item label="重置密码方式" prop="resetPasswordMode"><el-radio-group v-model="configForm.resetPasswordMode"><el-radio value="RANDOM">随机生成临时密码</el-radio><el-radio value="FIXED">使用固定临时密码</el-radio></el-radio-group></el-form-item><el-form-item v-if="configForm.resetPasswordMode === 'FIXED'" label="固定临时密码" prop="fixedResetPassword"><el-input v-model="configForm.fixedResetPassword" type="password" show-password autocomplete="new-password" placeholder="请输入8至64位密码；留空表示保留原配置" /></el-form-item><el-divider content-position="left">密码复杂度规则</el-divider><el-form-item label="启用复杂度校验"><el-switch v-model="configForm.passwordComplexityEnabled" active-text="启用" inactive-text="关闭" /></el-form-item><el-form-item label="密码最小长度" prop="passwordMinLength"><el-input-number v-model="configForm.passwordMinLength" :min="8" :max="64" :disabled="!configForm.passwordComplexityEnabled" /></el-form-item><el-checkbox v-model="configForm.requireUppercase" :disabled="!configForm.passwordComplexityEnabled">必须包含大写字母</el-checkbox><el-checkbox v-model="configForm.requireLowercase" :disabled="!configForm.passwordComplexityEnabled">必须包含小写字母</el-checkbox><el-checkbox v-model="configForm.requireDigit" :disabled="!configForm.passwordComplexityEnabled">必须包含数字</el-checkbox><el-checkbox v-model="configForm.requireSpecialCharacter" :disabled="!configForm.passwordComplexityEnabled">必须包含特殊字符</el-checkbox><el-divider content-position="left">登录失败锁定</el-divider><el-form-item label="启用失败锁定"><el-switch v-model="configForm.loginLockEnabled" active-text="启用" inactive-text="关闭" /></el-form-item><el-form-item label="连续失败次数"><el-input-number v-model="configForm.loginFailureThreshold" :min="1" :max="20" :disabled="!configForm.loginLockEnabled" /></el-form-item><el-form-item label="锁定时长（分钟）"><el-input-number v-model="configForm.loginLockMinutes" :min="1" :max="1440" :disabled="!configForm.loginLockEnabled" /></el-form-item><p class="security-policy-hint">关闭后不再因连续登录失败锁定账号。登录成功会自动清零失败次数，并在账号列表展示最近登录时间。</p><div class="form-actions"><el-button type="primary" :loading="configSaving" @click="saveConfig">保存账号安全设置</el-button></div></div>
      </el-form>
    </section>
  </template>

  <el-drawer
    v-model="dictionaryDialogVisible"
    :title="editingItem ? '编辑字典项' : '新增字典项'"
    direction="rtl"
    size="480px"
    class="dictionary-drawer"
  >
    <div class="dictionary-drawer-intro">
      <span class="config-block-icon">值</span>
      <p>同一字典类型可以配置多个选项。编码会写入业务数据，请保持稳定且不要重复。</p>
    </div>
    <el-form ref="dictionaryFormRef" :model="dictionaryForm" :rules="dictionaryRules" label-position="top" class="dictionary-form" require-asterisk-position="right">
      <el-form-item label="字典类型编码" prop="dictionaryType" :required="true">
        <el-input v-model="dictionaryForm.dictionaryType" :disabled="Boolean(editingItem)" placeholder="例如：ORDER_STATUS" />
      </el-form-item>
      <el-form-item label="字典项编码" prop="itemCode" :required="true">
        <el-input v-model="dictionaryForm.itemCode" :disabled="Boolean(editingItem)" placeholder="例如：PENDING" />
      </el-form-item>
      <el-form-item label="颜色类型" prop="tagType">
        <el-select v-model="dictionaryForm.tagType" placeholder="自动识别或手动指定">
          <el-option label="自动识别" value="" />
          <el-option label="成功" value="success" />
          <el-option label="提示" value="info" />
          <el-option label="警告" value="warning" />
          <el-option label="危险" value="danger" />
        </el-select>
      </el-form-item>
      <el-form-item label="显示名称" prop="itemLabel" :required="true">
        <el-input v-model="dictionaryForm.itemLabel" maxlength="128" show-word-limit placeholder="例如：待支付" />
      </el-form-item>
      <el-form-item label="排序值" prop="sortOrder">
        <el-input-number v-model="dictionaryForm.sortOrder" :min="0" :max="9999" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dictionaryDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="dictionarySaving" @click="saveDictionary">保存字典项</el-button>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { RefreshCw, Search } from 'lucide-vue-next'
import PageHeader from '../../components/common/PageHeader.vue'
import { createPlatformDictionaryItem, getPlatformSystemConfig, listPlatformDictionaryItems, updatePlatformDictionaryItem, updatePlatformDictionaryItemStatus, updatePlatformSystemConfig } from '../../api/platform/system'
import type { CreatePlatformDictionaryItemCommand, PlatformDictionaryItem, UpdatePlatformSystemConfigCommand } from '../../types/platform-system'
import { message } from '../../utils/message'
import { useDictionary } from '../../stores/dictionary'

const dictionary = useDictionary()

type SystemSection = 'BRAND' | 'DICTIONARY' | 'SECURITY'
const sectionOptions: Array<{ label: string; value: SystemSection }> = [{ label: '平台设置', value: 'BRAND' }, { label: '字典管理', value: 'DICTIONARY' }, { label: '安全设置', value: 'SECURITY' }]
const activeSection = ref<SystemSection>('BRAND')
const configFormRef = ref<FormInstance>()
const configForm = reactive<UpdatePlatformSystemConfigCommand>({ logoUrl: '', description: '', resetPasswordMode: 'RANDOM', passwordComplexityEnabled: false, passwordMinLength: 8, loginLockEnabled: true, loginFailureThreshold: 5, loginLockMinutes: 15, requireUppercase: true, requireLowercase: true, requireDigit: true, requireSpecialCharacter: false })
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
const selectedType = ref('')
const itemSearchText = ref('')
const selectedGroupItems = computed(() => {
  const group = dictionaryGroups.value.find(g => g.dictionaryType === selectedType.value)
  return group?.items ?? []
})
const filteredSelectedItems = computed(() => {
  if (!itemSearchText.value.trim()) return selectedGroupItems.value
  const kw = itemSearchText.value.trim().toLowerCase()
  return selectedGroupItems.value.filter(item =>
    item.itemLabel.toLowerCase().includes(kw) ||
    item.itemCode.toLowerCase().includes(kw) ||
    item.tagType.toLowerCase().includes(kw)
  )
})
const dictionaryDialogVisible = ref(false)
const dictionaryFormRef = ref<FormInstance>()
const editingItem = ref<PlatformDictionaryItem>()
const dictionaryForm = reactive<CreatePlatformDictionaryItemCommand>({ dictionaryType: '', itemCode: '', tagType: '', itemLabel: '', sortOrder: 0 })
const dictionaryRules: FormRules = { dictionaryType: [{ required: true, message: '请输入字典类型编码', trigger: 'blur' }], itemCode: [{ required: true, message: '请输入字典项编码', trigger: 'blur' }], itemLabel: [{ required: true, message: '请输入显示名称', trigger: 'blur' }], sortOrder: [{ required: true, message: '请输入排序值', trigger: 'change' }] }

async function loadConfig() { try { Object.assign(configForm, await getPlatformSystemConfig()) } catch (error) { message.error(error instanceof Error ? error.message : '平台设置加载失败') } }
async function saveConfig() { if (!(await configFormRef.value?.validate().catch(() => false))) return; configSaving.value = true; try { Object.assign(configForm, await updatePlatformSystemConfig(configForm)); message.success('平台设置已保存') } catch (error) { message.error(error instanceof Error ? error.message : '平台设置保存失败') } finally { configSaving.value = false } }
async function loadDictionaries() { dictionaryLoading.value = true; try { dictionaryItems.value = await listPlatformDictionaryItems(dictionaryTypeFilter.value.trim() || undefined); if (selectedType.value && !dictionaryGroups.value.some(g => g.dictionaryType === selectedType.value)) selectedType.value = dictionaryGroups.value[0]?.dictionaryType ?? ''; if (!selectedType.value && dictionaryGroups.value.length > 0) selectedType.value = dictionaryGroups.value[0].dictionaryType } catch (error) { message.error(error instanceof Error ? error.message : '平台字典加载失败') } finally { dictionaryLoading.value = false } }
function selectType(dictionaryType: string) { selectedType.value = dictionaryType; itemSearchText.value = '' }
function onTypeFilterChange() { loadDictionaries() }
function openCreate(dictionaryType = '') { editingItem.value = undefined; Object.assign(dictionaryForm, { dictionaryType, itemCode: '', tagType: '', itemLabel: '', sortOrder: 0 }); dictionaryDialogVisible.value = true }
function openEdit(item: PlatformDictionaryItem) { editingItem.value = item; Object.assign(dictionaryForm, { dictionaryType: item.dictionaryType, itemCode: item.itemCode, tagType: item.tagType, itemLabel: item.itemLabel, sortOrder: item.sortOrder }); dictionaryDialogVisible.value = true }
async function saveDictionary() { if (!(await dictionaryFormRef.value?.validate().catch(() => false))) return; dictionarySaving.value = true; try { if (editingItem.value) await updatePlatformDictionaryItem(editingItem.value.id, { tagType: dictionaryForm.tagType, itemLabel: dictionaryForm.itemLabel, sortOrder: dictionaryForm.sortOrder }); else await createPlatformDictionaryItem(dictionaryForm); message.success('平台字典项已保存'); dictionaryDialogVisible.value = false; await loadDictionaries(); await dictionary.refresh() } catch (error) { message.error(error instanceof Error ? error.message : '平台字典项保存失败') } finally { dictionarySaving.value = false } }
async function toggleStatus(item: PlatformDictionaryItem) { const enabled = item.status !== 'ENABLED'; try { await ElMessageBox.confirm(`确认${enabled ? '启用' : '停用'}“${item.itemLabel}”吗？`, '字典项状态确认', { type: 'warning' }); await updatePlatformDictionaryItemStatus(item.id, enabled); message.success('字典项状态已更新'); await loadDictionaries(); await dictionary.refresh() } catch (error) { if (error !== 'cancel') message.error(error instanceof Error ? error.message : '字典项状态更新失败') } }
onMounted(loadConfig)
onMounted(() => { dictionary.ensureLoaded(); loadDictionaries() })
</script>

<style scoped>
.system-workspace-nav { display: flex; align-items: center; justify-content: flex-start; gap: 12px; margin-bottom: 22px; padding: 0; }
.system-card-kicker { color: var(--domain-600); font-family: ui-monospace, Consolas, monospace; font-size: 10px; font-weight: 700; letter-spacing: 1.2px; }

.system-section-switch {
  --el-segmented-color: #6b7b72;
  --el-segmented-item-selected-color: #1b4332;
  --el-segmented-item-selected-bg-color: #fff;
  --el-segmented-item-hover-color: #1b4332;
  --el-segmented-item-hover-bg-color: rgba(255,255,255,.6);
  --el-segmented-bg-color: #f0f4f1;
  --el-segmented-padding: 3px;
  padding: 3px;
  border-radius: 10px;
  background: #f0f4f1;
}
.system-section-switch :deep(.el-segmented__group) { gap: 2px; }
.system-section-switch :deep(.el-segmented__item) {
  min-width: 110px;
  min-height: 36px;
  padding: 0 18px;
  border-radius: 8px;
  color: #6b7b72;
  font-size: 13px;
  font-weight: 550;
  letter-spacing: .1px;
  transition: color 160ms ease, background-color 160ms ease, box-shadow 180ms ease;
}
.system-section-switch :deep(.el-segmented__item:hover) {
  color: #1b4332;
  background: rgba(255,255,255,.55);
}
.system-section-switch :deep(.el-segmented__item-selected) {
  color: #1b4332;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,.06), 0 1px 2px rgba(0,0,0,.04);
}
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

/* === 主工作区：左右分栏 === */
.dictionary-workspace { display: flex; min-height: 460px; }

/* === 左侧：字典类型面板 === */
.dictionary-type-panel {
  display: flex;
  flex-direction: column;
  width: 260px;
  min-width: 240px;
  border-right: 1px solid #edf1ee;
  background: #f9fbfa;
}
.dictionary-type-search { padding: 14px 14px 10px; }
.dictionary-type-search :deep(.el-input__wrapper) { background: #fff; }
.dictionary-type-list {
  flex: 1;
  overflow-y: auto;
  padding: 2px 10px 6px;
}
.dictionary-type-list::-webkit-scrollbar { width: 4px; }
.dictionary-type-list::-webkit-scrollbar-thumb { border-radius: 2px; background: #c8d6cc; }

/* === 左侧：字典类型卡片 === */
.dictionary-type-card {
  margin-bottom: 6px;
  border: 1px solid #e3ebe5;
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 200ms ease, box-shadow 200ms ease, transform 180ms ease;
}
.dictionary-type-card:hover {
  transform: translateY(-1px);
}
.dictionary-type-card--active {
  border-color: #a3c9b2 !important;
  box-shadow: 0 0 0 1px rgba(27, 67, 50, .12), 0 4px 16px -6px rgba(27, 67, 50, .28), 0 2px 6px -2px rgba(27, 67, 50, .14) !important;
}
.dictionary-type-card--active .dictionary-type-item__code { color: #1b4332; }
.dictionary-type-card__inner {
  display: flex;
  align-items: flex-start;
  gap: 0;
}
.dictionary-type-item__body { display: grid; gap: 3px; min-width: 0; }
.dictionary-type-item__code {
  overflow: hidden;
  color: #25332c;
  font-size: 13px;
  font-weight: 650;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dictionary-type-item__desc {
  overflow: hidden;
  color: #7b858d;
  font-size: 11px;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dictionary-type-item__tag {
  display: inline-block;
  width: fit-content;
  color: #5a7a65;
  background: #eaf2ed;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
}
.dictionary-type-item__meta {
  color: #8a968e;
  font-size: 11px;
}
.dictionary-type-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-top: 1px solid #edf1ee;
  color: #8b9690;
  font-size: 11px;
}
.dictionary-type-footer b { color: var(--domain-700); font-weight: 650; }

/* === 右侧：字典项面板 === */
.dictionary-item-panel { flex: 1; display: flex; flex-direction: column; min-width: 0; background: #fff; }
.dictionary-item-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid #e3ebe5;
  border-radius: 10px;
  overflow: hidden;
}
.dictionary-item-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.dictionary-item-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
  border-bottom: 1px solid #edf1ee;
  background: #fbfcfb;
  flex-wrap: wrap;
}
.dictionary-item-toolbar__left { display: flex; align-items: center; gap: 10px; }
.dictionary-item-toolbar__title {
  color: #25332c;
  font-family: ui-monospace, Consolas, monospace;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: .2px;
}
.dictionary-item-toolbar__right { display: flex; align-items: center; gap: 10px; }
.dictionary-item-desc {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 8px 20px 10px;
  border-bottom: 1px solid #edf1ee;
  background: #f8faf9;
  color: #7b858d;
  font-size: 12px;
  line-height: 1.6;
}
.dictionary-item-desc__label { flex-shrink: 0; color: #89958e; }
.dictionary-item-desc__label code { color: var(--domain-700); font-size: 11px; }
.dictionary-item-table :deep(.el-table__inner-wrapper::before) { display: none; }
.dictionary-item-table :deep(.el-table__header th.el-table__cell) { background: #f9fbfa; color: #7b8780; font-size: 11px; font-weight: 600; }
.dictionary-item-table :deep(.el-table__body td.el-table__cell) { padding: 13px 0; }
.dictionary-value { color: #2d6047; font-family: ui-monospace, Consolas, monospace; font-size: 12px; }

/* 空状态：未选择类型 */
.dictionary-item-empty {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 24px;
  color: #9aa3aa;
}
.dictionary-item-empty__icon {
  display: grid;
  width: 56px;
  height: 56px;
  place-items: center;
  border-radius: 14px;
  background: #eef4f0;
  color: #94a89a;
  font-size: 24px;
  font-weight: 700;
}
.dictionary-item-empty strong { color: #5e6b64; font-size: 15px; font-weight: 600; }
.dictionary-item-empty p { margin: 0; font-size: 12px; line-height: 1.6; }
.dictionary-drawer-intro {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 20px;
  padding: 12px 14px;
  border: 1px solid #dcebe0;
  border-radius: 10px;
  background: #f5faf6;
}
.dictionary-drawer-intro p { margin: 1px 0 0; color: #617269; font-size: 12px; line-height: 1.6; }
.dictionary-form { display: block; }
.dictionary-form :deep(.el-form-item) { margin-bottom: 20px; }
.dictionary-form :deep(.el-form-item__label) { color: #56645c; font-size: 12px; font-weight: 600; }
.dictionary-form :deep(.el-input-number) { width: 100%; }
.dictionary-drawer :deep(.el-drawer__header) { margin-bottom: 0; padding: 18px 20px 14px; border-bottom: 1px solid #edf1ee; }
.dictionary-drawer :deep(.el-drawer__body) { padding: 20px; }
.dictionary-drawer :deep(.el-drawer__footer) { padding: 14px 20px; border-top: 1px solid #edf1ee; }
@media (max-width: 720px) {
  .system-workspace-nav { align-items: stretch; flex-direction: column; gap: 8px; }
  .system-section-switch { width: 100%; }
  .system-section-switch :deep(.el-segmented__item) { flex: 1; min-width: 0; padding: 0 10px; }
  .system-config-card { padding: 18px; }
  .system-card-heading { flex-direction: column; margin-bottom: 20px; }
  .system-config-layout { grid-template-columns: 1fr; gap: 16px; }
  .dictionary-page-heading { flex-direction: column; padding: 20px 18px; }
  .dictionary-page-heading .el-button { width: 100%; }
  .dictionary-workspace { flex-direction: column; min-height: auto; }
  .dictionary-type-panel { width: 100%; min-width: 0; border-right: none; border-bottom: 1px solid #edf1ee; max-height: 260px; }
  .dictionary-type-card { margin-bottom: 4px; }
  .dictionary-item-card { border-radius: 0; border-left: none; border-right: none; }
  .dictionary-item-toolbar { flex-direction: column; align-items: stretch; }
  .dictionary-item-toolbar__right { flex-direction: column; }
  .dictionary-item-toolbar__right .el-input { width: 100% !important; }
  .dictionary-item-toolbar__right .el-button { width: 100%; }
  .dictionary-form { display: block; }
}
</style>
