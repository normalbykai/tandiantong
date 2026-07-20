<template>
  <div class="selectable-list" :class="{ 'is-disabled': disabled }" role="list">
    <label
      v-for="item in items"
      :key="item.value"
      class="selectable-list__item"
      :class="{ 'is-selected': isSelected(item.value), 'is-disabled': disabled || item.disabled }"
      role="listitem"
    >
      <el-checkbox
        :model-value="isSelected(item.value)"
        :disabled="disabled || item.disabled"
        :aria-label="`选择${item.title}`"
        @update:model-value="toggle(item.value, $event)"
      />
      <span class="selectable-list__content">
        <b>{{ item.title }}</b>
        <code v-if="item.description">{{ item.description }}</code>
      </span>
      <span v-if="isSelected(item.value)" class="selectable-list__selected">已选</span>
    </label>
    <p v-if="!items.length" class="selectable-list__empty">{{ emptyText }}</p>
  </div>
</template>

<script setup lang="ts">
type SelectableValue = number | string

interface SelectableListItem {
  value: SelectableValue
  title: string
  description?: string
  disabled?: boolean
}

const props = withDefaults(defineProps<{
  modelValue: SelectableValue[]
  items: SelectableListItem[]
  disabled?: boolean
  emptyText?: string
}>(), { disabled: false, emptyText: '暂无可选项' })

const emit = defineEmits<{ 'update:modelValue': [values: SelectableValue[]] }>()

function isSelected(value: SelectableValue) {
  return props.modelValue.includes(value)
}

function toggle(value: SelectableValue, selected: boolean | string | number) {
  if (selected === true) {
    emit('update:modelValue', [...new Set([...props.modelValue, value])])
    return
  }
  emit('update:modelValue', props.modelValue.filter(item => item !== value))
}
</script>

<style scoped>
.selectable-list { display:grid; gap:4px; padding:6px; }
.selectable-list__item { display:flex; align-items:flex-start; gap:9px; min-width:0; padding:9px 8px; border:1px solid transparent; border-radius:7px; background:transparent; cursor:pointer; transition:background-color 180ms ease,border-color 180ms ease,box-shadow 180ms ease,transform 180ms ease; }
.selectable-list__item:hover:not(.is-disabled) { background:#f7faf8; border-color:#dce8e1; }
.selectable-list__item:active:not(.is-disabled) { transform:scale(.992); }
.selectable-list__item:focus-within { outline:2px solid color-mix(in srgb,var(--domain-500) 42%,transparent); outline-offset:1px; }
.selectable-list__item.is-selected { border-color:color-mix(in srgb,var(--domain-500) 42%,#d7e7dd); background:linear-gradient(90deg,var(--domain-50),#f9fcfa); box-shadow:inset 3px 0 0 var(--domain-600); }
.selectable-list__item.is-disabled { cursor:not-allowed; opacity:.64; }
.selectable-list__content { display:grid; gap:3px; min-width:0; flex:1; padding-top:1px; }
.selectable-list__content b { overflow:hidden; color:#303033; font-size:12px; font-weight:500; text-overflow:ellipsis; white-space:nowrap; }
.is-selected .selectable-list__content b { color:var(--domain-700); font-weight:600; }
.selectable-list__content code { overflow:hidden; color:#777b81; font:11px ui-monospace,Consolas,monospace; text-overflow:ellipsis; white-space:nowrap; }
.selectable-list__selected { align-self:center; color:var(--domain-700); font-size:10px; font-weight:600; white-space:nowrap; }
.selectable-list__empty { padding:10px 6px; margin:0; color:#a8a6a0; font-size:12px; }
.selectable-list :deep(.el-checkbox) { height:18px; margin:0; }
.selectable-list :deep(.el-checkbox__label) { display:none; }
.selectable-list :deep(.el-checkbox__input.is-checked .el-checkbox__inner) { background-color:var(--domain-600); border-color:var(--domain-600); }
</style>
