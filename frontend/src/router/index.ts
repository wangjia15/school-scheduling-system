import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/teachers',
      name: 'teachers',
      component: () => import('@/views/teachers/TeacherListView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/courses',
      name: 'courses',
      component: () => import('@/views/courses/CourseListView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/classrooms',
      name: 'classrooms',
      component: () => import('@/views/classrooms/ClassroomListView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/scheduling',
      name: 'scheduling',
      component: () => import('@/views/scheduling/SchedulingView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue')
    }
  ]
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router