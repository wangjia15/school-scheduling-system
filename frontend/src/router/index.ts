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
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('@/views/admin/AdminDashboardView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/scheduling',
      name: 'admin-scheduling',
      component: () => import('@/views/admin/SchedulingView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/conflicts',
      name: 'admin-conflicts',
      component: () => import('@/views/admin/ConflictsView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/teachers',
      name: 'admin-teachers',
      component: () => import('@/views/admin/TeachersView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/courses',
      name: 'admin-courses',
      component: () => import('@/views/admin/CoursesView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/classrooms',
      name: 'admin-classrooms',
      component: () => import('@/views/admin/ClassroomsView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/reports',
      name: 'admin-reports',
      component: () => import('@/views/admin/ReportsView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/settings',
      name: 'admin-settings',
      component: () => import('@/views/admin/SettingsView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }
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
  } else if (to.meta.requiresAdmin && (!authStore.user || authStore.user.role !== 'admin')) {
    next('/dashboard') // Redirect non-admin users to regular dashboard
  } else {
    next()
  }
})

export default router