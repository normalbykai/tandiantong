<template>
  <div class="floating-label-input" :class="{ 'is-active': isActive, 'has-password-toggle': showPassword }">
    <input
      :id="inputId"
      v-bind="$attrs"
      :autocomplete="autocomplete"
      :type="inputType"
      :value="modelValue"
      @blur="handleBlur"
      @focus="handleFocus"
      @input="handleInput"
    />
    <label :for="inputId">{{ label }}</label>
    <button v-if="showPassword" type="button" class="password-toggle" :aria-label="passwordVisible ? '隐藏密码' : '显示密码'" @click="togglePassword">
      {{ passwordVisible ? '隐藏' : '显示' }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useId } from 'vue'

defineOptions({ inheritAttrs: false })

interface Props {
  autocomplete?: string
  label: string
  modelValue: string
  showPassword?: boolean
  type?: 'password' | 'text'
}

const props = withDefaults(defineProps<Props>(), {
  autocomplete: 'off',
  showPassword: false,
  type: 'text'
})
const emit = defineEmits<{
  'update:modelValue': [value: string]
  blur: [event: FocusEvent]
  focus: [event: FocusEvent]
}>()
const inputId = useId()
const focused = ref(false)
const passwordVisible = ref(false)
const isActive = computed(() => focused.value || props.modelValue.length > 0)
const inputType = computed(() => props.type === 'password' && !passwordVisible.value ? 'password' : 'text')

function handleInput(event: Event) {
  if (event.target instanceof HTMLInputElement) emit('update:modelValue', event.target.value)
}

function handleFocus(event: FocusEvent) {
  focused.value = true
  emit('focus', event)
}

function handleBlur(event: FocusEvent) {
  focused.value = false
  emit('blur', event)
}

function togglePassword() {
  passwordVisible.value = !passwordVisible.value
}
</script>

<style scoped>
.floating-label-input { position: relative; width: 100%; font-family: inherit; }
.floating-label-input input {
  box-sizing: border-box;
  width: 100%;
  min-height: 50px;
  padding: .8em 16px;
  outline: none;
  border: 2px solid var(--floating-input-border, #cdd7d0);
  border-radius: 20px;
  color: #27342c;
  font: inherit;
  font-size: 14px;
  background: transparent;
  transition: border-color .2s ease;
}

.floating-label-input.has-password-toggle input { padding-right: 58px; }
.floating-label-input input:hover { border-color: var(--floating-input-border-hover, #aabbb0); }
.floating-label-input input:focus { border-color: var(--floating-input-accent, #1b4332); }
.floating-label-input label {
  position: absolute;
  top: 50%;
  left: 16px;
  padding: 0 .4em;
  color: var(--floating-input-label, #738078);
  font-size: 14px;
  line-height: 1;
  pointer-events: none;
  transform: translateY(-50%);
  transform-origin: left center;
  transition: transform .2s ease, color .2s ease, background-color .2s ease;
}

.floating-label-input.is-active label {
  color: var(--floating-input-accent, #1b4332);
  background: var(--floating-input-label-background, #fff);
  transform: translateY(-50%) translateY(-25px) scale(.86);
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 14px;
  padding: 4px;
  border: 0;
  color: var(--floating-input-accent, #1b4332);
  font: inherit;
  font-size: 12px;
  line-height: 1;
  background: transparent;
  cursor: pointer;
  transform: translateY(-50%);
}

.password-toggle:focus-visible { outline: 2px solid var(--floating-input-accent, #1b4332); outline-offset: 2px; border-radius: 3px; }
</style>
