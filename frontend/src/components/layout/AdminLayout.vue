<template>
  <div class="flex h-screen bg-background">
    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-50 transform transition-transform duration-200 ease-in-out lg:translate-x-0 lg:static lg:inset-0',
        sidebarOpen ? 'translate-x-0' : '-translate-x-full'
      ]"
    >
      <AdminSidebar @logout="handleLogout" />
    </aside>

    <!-- Main Content Area -->
    <div class="flex-1 flex flex-col overflow-hidden lg:ml-0">
      <!-- Header -->
      <AdminHeader
        @toggle-sidebar="toggleSidebar"
        @toggle-theme="toggleTheme"
      />

      <!-- Page Content -->
      <main class="flex-1 overflow-y-auto bg-muted/20">
        <div class="container mx-auto px-4 py-6">
          <slot />
        </div>
      </main>
    </div>

    <!-- Mobile Sidebar Overlay -->
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-40 bg-black/50 lg:hidden"
      @click="closeSidebar"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'
import AdminSidebar from './AdminSidebar.vue'
import AdminHeader from './AdminHeader.vue'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const sidebarOpen = ref(false)

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const closeSidebar = () => {
  sidebarOpen.value = false
}

const toggleTheme = () => {
  // Implement theme toggle functionality
  console.log('Toggle theme')
}

const handleLogout = () => {
  authStore.logout()
  toast.success('Logged out successfully')
  router.push('/login')
}

// Close sidebar on escape key
const handleEscape = (event: KeyboardEvent) => {
  if (event.key === 'Escape') {
    closeSidebar()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleEscape)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape)
})
</script>