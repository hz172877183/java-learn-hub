import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/catalog', name: 'catalog', component: () => import('@/views/CatalogView.vue') },
    { path: '/topics/:slug', name: 'topic', component: () => import('@/views/TopicDetailView.vue') },
    { path: '/track', name: 'track', component: () => import('@/views/TrackView.vue') },
    { path: '/about', name: 'about', component: () => import('@/views/AboutView.vue') },
    { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue') },
    { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue') },
    { path: '/me', name: 'my-learning', component: () => import('@/views/MyLearningView.vue') },
    { path: '/admin', name: 'admin', component: () => import('@/views/AdminView.vue') },
    {
      path: '/admin/topics/:slug',
      name: 'admin-topic',
      component: () => import('@/views/AdminTopicEditView.vue'),
    },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue') },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

export default router
