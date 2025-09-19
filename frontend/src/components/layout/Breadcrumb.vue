<template>
  <nav class="flex items-center space-x-1 text-sm text-muted-foreground">
    <router-link to="/admin" class="hover:text-foreground">
      Home
    </router-link>
    <ChevronRight class="h-4 w-4" />
    <template v-for="(item, index) in breadcrumbs" :key="item.path">
      <router-link
        v-if="index < breadcrumbs.length - 1"
        :to="item.path"
        class="hover:text-foreground"
      >
        {{ item.label }}
      </router-link>
      <span v-else class="text-foreground font-medium">
        {{ item.label }}
      </span>
      <ChevronRight v-if="index < breadcrumbs.length - 1" class="h-4 w-4" />
    </template>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ChevronRight } from 'lucide-vue-next'

const route = useRoute()

interface BreadcrumbItem {
  label: string
  path: string
}

const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  const pathSegments = route.path.split('/').filter(segment => segment)
  const breadcrumbs: BreadcrumbItem[] = []

  pathSegments.forEach((segment, index) => {
    const path = '/' + pathSegments.slice(0, index + 1).join('/')
    let label = segment.charAt(0).toUpperCase() + segment.slice(1)

    // Format some specific labels
    switch (segment) {
      case 'admin':
        label = 'Dashboard'
        break
      case 'scheduling':
        label = 'Scheduling'
        break
      case 'conflicts':
        label = 'Conflict Resolution'
        break
      case 'teachers':
        label = 'Teacher Management'
        break
      case 'courses':
        label = 'Course Management'
        break
      case 'classrooms':
        label = 'Classroom Management'
        break
      case 'reports':
        label = 'Reports & Analytics'
        break
      case 'settings':
        label = 'System Settings'
        break
    }

    breadcrumbs.push({ label, path })
  })

  return breadcrumbs
})
</script>