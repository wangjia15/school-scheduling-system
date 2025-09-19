<template>
  <div :class="cardClasses">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'default' | 'outlined' | 'elevated'
  padding?: 'none' | 'sm' | 'md' | 'lg'
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  padding: 'md'
})

const cardClasses = computed(() => {
  const baseClasses = 'rounded-lg border bg-card text-card-foreground shadow-sm'

  const variantClasses = {
    default: '',
    outlined: 'border-2',
    elevated: 'shadow-lg'
  }

  const paddingClasses = {
    none: '',
    sm: 'p-3',
    md: 'p-6',
    lg: 'p-8'
  }

  return `${baseClasses} ${variantClasses[props.variant]} ${paddingClasses[props.padding]}`
})
</script>