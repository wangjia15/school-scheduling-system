<template>
  <header class="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
    <div class="container flex h-16 items-center">
      <!-- Mobile Menu Toggle -->
      <Button
        variant="ghost"
        size="icon"
        @click="$emit('toggle-sidebar')"
        class="md:hidden"
      >
        <Menu class="h-5 w-5" />
      </Button>

      <!-- Breadcrumb -->
      <div class="flex items-center gap-2 ml-4 md:ml-0">
        <Breadcrumb />
      </div>

      <!-- Search Bar -->
      <div class="flex-1 max-w-md mx-8">
        <div class="relative">
          <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            v-model="searchQuery"
            type="text"
            placeholder="Search schedules, teachers, courses..."
            class="pl-10"
            @keyup.enter="handleSearch"
          />
        </div>
      </div>

      <!-- Header Actions -->
      <div class="flex items-center gap-2 ml-auto">
        <!-- Notifications -->
        <Button variant="ghost" size="icon" class="relative">
          <Bell class="h-5 w-5" />
          <Badge variant="destructive" class="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center text-xs">
            {{ notificationCount }}
          </Badge>
        </Button>

        <!-- Help -->
        <Button variant="ghost" size="icon">
          <HelpCircle class="h-5 w-5" />
        </Button>

        <!-- Theme Toggle -->
        <Button variant="ghost" size="icon" @click="$emit('toggle-theme')">
          <Moon class="h-5 w-5" />
        </Button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Badge from '@/components/ui/Badge.vue'
import {
  Menu,
  Search,
  Bell,
  HelpCircle,
  Moon
} from 'lucide-vue-next'
import Breadcrumb from './Breadcrumb.vue'

const emit = defineEmits<{
  'toggle-sidebar': []
  'toggle-theme': []
}>()

const searchQuery = ref('')
const notificationCount = ref(3)

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    console.log('Searching for:', searchQuery.value)
    // Implement search functionality
  }
}
</script>