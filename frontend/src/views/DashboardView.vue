<template>
  <div class="container mx-auto px-4 py-8">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-3xl font-bold">Dashboard</h1>
      <button @click="handleLogout" class="btn btn-secondary">
        Logout
      </button>
    </div>

    <div class="card">
      <div class="card-header">
        <h2 class="card-title">Scheduling Overview</h2>
        <p class="card-description">System status and quick actions</p>
      </div>
      <div class="card-content">
        <div class="text-center py-8">
          <p class="text-muted-foreground">Dashboard content will be implemented in future tasks</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const handleLogout = () => {
  authStore.logout()
  toast.success('Logged out successfully')
  router.push('/login')
}
</script>