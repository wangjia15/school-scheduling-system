<template>
  <div class="w-full overflow-auto">
    <table :class="tableClasses">
      <thead>
        <tr class="border-b">
          <th
            v-for="header in headers"
            :key="header.key"
            :class="headerClasses"
            :style="header.width ? `width: ${header.width}` : ''"
          >
            {{ header.label }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(row, index) in data"
          :key="row.id || index"
          :class="rowClasses(index)"
        >
          <td
            v-for="header in headers"
            :key="`${header.key}-${index}`"
            :class="cellClasses"
          >
            <slot :name="`cell-${header.key}`" :row="row" :index="index">
              {{ row[header.key] }}
            </slot>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Header {
  key: string
  label: string
  width?: string
  sortable?: boolean
}

interface Props {
  headers: Header[]
  data: any[]
  striped?: boolean
  hoverable?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  striped: false,
  hoverable: false
})

const tableClasses = computed(() => {
  const baseClasses = 'w-full caption-bottom text-sm'
  return baseClasses
})

const headerClasses = computed(() => {
  const baseClasses = 'h-12 px-4 text-left align-middle font-medium text-muted-foreground [&:has([role=checkbox])]:pr-0'
  return baseClasses
})

const cellClasses = computed(() => {
  const baseClasses = 'p-4 align-middle [&:has([role=checkbox])]:pr-0'
  return baseClasses
})

const rowClasses = (index: number) => {
  const baseClasses = 'border-b transition-colors hover:bg-muted/50 data-[state=selected]:bg-muted'
  const stripedClasses = props.striped && index % 2 === 0 ? 'bg-muted/20' : ''
  return `${baseClasses} ${stripedClasses}`
}
</script>