<template>
  <div :class="contentClasses">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'default' | 'no-padding-top'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default'
})

const contentClasses = computed(() => {
  const baseClasses = ''
  const paddingClasses = props.variant === 'no-padding-top' ? 'p-6 pt-0' : 'p-6 pt-0'
  return `${baseClasses} ${paddingClasses}`
})
</script>