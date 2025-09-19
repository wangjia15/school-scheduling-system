<template>
  <div :class="alertClasses" role="alert">
    <div class="flex items-center gap-2">
      <div v-if="icon" :class="iconClasses">
        <slot name="icon" />
      </div>
      <div class="flex-1">
        <h4 v-if="title" class="font-semibold mb-1">{{ title }}</h4>
        <div class="text-sm">
          <slot />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  variant?: 'default' | 'destructive' | 'warning' | 'info' | 'success'
  icon?: boolean
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'default',
  icon: false
})

const alertClasses = computed(() => {
  const baseClasses = 'relative w-full rounded-lg border p-4'

  const variantClasses = {
    default: 'bg-background text-foreground',
    destructive: 'border-destructive/50 text-destructive dark:border-destructive [&>svg]:text-destructive',
    warning: 'border-orange-200 bg-orange-50 text-orange-800 dark:border-orange-800 dark:bg-orange-950 dark:text-orange-200',
    info: 'border-blue-200 bg-blue-50 text-blue-800 dark:border-blue-800 dark:bg-blue-950 dark:text-blue-200',
    success: 'border-green-200 bg-green-50 text-green-800 dark:border-green-800 dark:bg-green-950 dark:text-green-200'
  }

  return `${baseClasses} ${variantClasses[props.variant]}`
})

const iconClasses = computed(() => {
  const baseClasses = 'h-4 w-4'

  const variantClasses = {
    default: 'text-foreground',
    destructive: 'text-destructive',
    warning: 'text-orange-600',
    info: 'text-blue-600',
    success: 'text-green-600'
  }

  return `${baseClasses} ${variantClasses[props.variant]}`
})
</script>