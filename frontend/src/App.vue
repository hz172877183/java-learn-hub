<!-- 应用外壳：顶部导航、登录态展示与路由出口。 -->
<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

function logout() {
  auth.logout()
  void router.push('/')
}
</script>

<template>
  <div class="shell">
    <header class="top">
      <RouterLink class="brand" to="/">java-learn-hub</RouterLink>
      <nav>
        <RouterLink to="/catalog">学习目录</RouterLink>
        <RouterLink to="/track">总路线</RouterLink>
        <RouterLink to="/about">关于</RouterLink>
        <RouterLink v-if="auth.isLoggedIn" to="/me">我的学习</RouterLink>
        <RouterLink to="/admin">管理</RouterLink>
        <template v-if="auth.isLoggedIn">
          <span class="who">{{ auth.user?.displayName }}</span>
          <a href="#" @click.prevent="logout">退出</a>
        </template>
        <template v-else>
          <RouterLink to="/login">登录</RouterLink>
          <RouterLink to="/register">注册</RouterLink>
        </template>
      </nav>
    </header>
    <RouterView />
  </div>
</template>

<style scoped>
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--line);
  background: rgba(255, 253, 248, 0.9);
  position: sticky;
  top: 0;
  z-index: 10;
}

.brand {
  font-family: Georgia, serif;
  color: var(--ink);
  font-size: 18px;
}

nav {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

nav a {
  color: var(--muted);
}

nav a.router-link-active {
  color: var(--accent);
}

.who {
  color: var(--ink);
  font-weight: 600;
}
</style>
