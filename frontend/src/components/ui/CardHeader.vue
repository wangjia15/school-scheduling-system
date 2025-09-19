<template>
  <div :class="headerClasses">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'default' | 'no-padding'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default'
})

const headerClasses = computed(() => {
  const baseClasses = 'flex flex-col space-y-1.5'
  const paddingClasses = props.variant === 'no-padding' ? '' : 'p-6'
  return `${baseClasses} ${paddingClasses}`
})
</script>