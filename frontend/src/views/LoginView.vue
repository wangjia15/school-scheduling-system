<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="card w-full max-w-md">
      <div class="card-header text-center">
        <h1 class="card-title">School Scheduling System</h1>
        <p class="card-description">Sign in to your account</p>
      </div>
      <div class="card-content">
        <form @submit.prevent="handleLogin" class="space-y-4">
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700 mb-1">
              Username
            </label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              class="input"
              required
              placeholder="Enter your username"
            />
          </div>
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-1">
              Password
            </label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              class="input"
              required
              placeholder="Enter your password"
            />
          </div>
          <button
            type="submit"
            class="btn btn-primary w-full"
            :disabled="isLoading"
          >
            {{ isLoading ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToast } from 'vue-toastification'

const router = useRouter()
const authStore = useAuthStore()
const toast = useToast()

const form = ref({
  username: '',
  password: ''
})

const isLoading = ref(false)

const handleLogin = async () => {
  isLoading.value = true

  try {
    const success = await authStore.login(form.value)
    if (success) {
      toast.success('Login successful!')
      router.push('/')
    } else {
      toast.error('Invalid credentials')
    }
  } catch (error) {
    toast.error('Login failed. Please try again.')
  } finally {
    isLoading.value = false
  }
}
</script>