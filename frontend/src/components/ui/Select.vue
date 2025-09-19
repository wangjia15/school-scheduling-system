<template>
  <div class="relative">
    <select
      :class="selectClasses"
      :disabled="disabled"
      :value="modelValue"
      @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
      v-bind="$attrs"
    >
      <slot />
    </select>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  modelValue?: string | number
  disabled?: boolean
  error?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  error: false
})

defineEmits<{
  'update:modelValue': [value: string | number]
}>()

const selectClasses = computed(() => {
  const baseClasses = 'flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50'

  const errorClasses = props.error ? 'border-red-500 focus-visible:ring-red-500' : ''

  return `${baseClasses} ${errorClasses}`
})
</script>