<template>
  <p :class="descriptionClasses">
    <slot />
  </p>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const descriptionClasses = computed(() => {
  const baseClasses = 'text-sm text-muted-foreground'
  return baseClasses
})
</script>