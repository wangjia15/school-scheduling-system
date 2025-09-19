<template>
  <h3 :class="titleClasses">
    <slot />
  </h3>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'default' | 'h1' | 'h2' | 'h3' | 'h4'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default'
})

const titleClasses = computed(() => {
  const baseClasses = 'leading-none tracking-tight'

  const variantClasses = {
    default: 'text-lg font-semibold',
    h1: 'text-2xl font-bold',
    h2: 'text-xl font-semibold',
    h3: 'text-lg font-medium',
    h4: 'text-base font-medium'
  }

  return `${baseClasses} ${variantClasses[props.variant]}`
})
</script>