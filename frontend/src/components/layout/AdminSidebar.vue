<template>
  <div class="flex h-full w-64 flex-col border-r bg-card">
    <!-- Logo and Brand -->
    <div class="flex h-16 items-center border-b px-6">
      <div class="flex items-center gap-2">
        <div class="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
          <span class="text-primary-foreground font-bold text-lg">SS</span>
        </div>
        <div>
          <h1 class="font-semibold text-lg">Schedule System</h1>
          <p class="text-xs text-muted-foreground">Admin Portal</p>
        </div>
      </div>
    </div>

    <!-- Navigation Menu -->
    <nav class="flex-1 space-y-1 px-3 py-4">
      <template v-for="item in navigationItems" :key="item.key">
        <div v-if="item.separator" class="border-t my-2"></div>

        <router-link
          v-else
          :to="item.path"
          :class="navItemClasses(item)"
          class="flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors"
        >
          <component :is="item.icon" class="h-5 w-5" />
          <span>{{ item.label }}</span>
          <Badge v-if="item.badge" variant="secondary" class="ml-auto">
            {{ item.badge }}
          </Badge>
        </router-link>
      </template>
    </nav>

    <!-- User Section -->
    <div class="border-t p-4">
      <div class="flex items-center gap-3">
        <div class="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
          <span class="text-primary font-semibold">
            {{ userInitials }}
          </span>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium truncate">{{ userName }}</p>
          <p class="text-xs text-muted-foreground truncate">Administrator</p>
        </div>
        <Button
          variant="ghost"
          size="icon"
          @click="$emit('logout')"
          class="ml-auto"
        >
          <LogOut class="h-4 w-4" />
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  LayoutDashboard,
  Calendar,
  Users,
  BookOpen,
  DoorOpen,
  AlertTriangle,
  Settings,
  BarChart3,
  Search,
  LogOut
} from 'lucide-vue-next'

interface NavItem {
  key: string
  label: string
  path: string
  icon: any
  badge?: string
  separator?: boolean
}

const route = useRoute()
const authStore = useAuthStore()

const emit = defineEmits<{
  logout: []
}>()

const userName = computed(() => authStore.user?.name || 'Admin User')
const userInitials = computed(() => {
  const name = userName.value
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
})

const navigationItems: NavItem[] = [
  {
    key: 'dashboard',
    label: 'Dashboard',
    path: '/admin',
    icon: LayoutDashboard
  },
  {
    key: 'scheduling',
    label: 'Scheduling',
    path: '/admin/scheduling',
    icon: Calendar,
    badge: '3'
  },
  {
    key: 'conflicts',
    label: 'Conflicts',
    path: '/admin/conflicts',
    icon: AlertTriangle,
    badge: '12'
  },
  {
    key: 'separator-1',
    label: '',
    path: '',
    icon: null,
    separator: true
  },
  {
    key: 'teachers',
    label: 'Teachers',
    path: '/admin/teachers',
    icon: Users
  },
  {
    key: 'courses',
    label: 'Courses',
    path: '/admin/courses',
    icon: BookOpen
  },
  {
    key: 'classrooms',
    label: 'Classrooms',
    path: '/admin/classrooms',
    icon: DoorOpen
  },
  {
    key: 'separator-2',
    label: '',
    path: '',
    icon: null,
    separator: true
  },
  {
    key: 'reports',
    label: 'Reports',
    path: '/admin/reports',
    icon: BarChart3
  },
  {
    key: 'settings',
    label: 'Settings',
    path: '/admin/settings',
    icon: Settings
  }
]

const navItemClasses = (item: NavItem) => {
  const isActive = route.path === item.path
  return isActive
    ? 'bg-primary text-primary-foreground'
    : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
}
</script>