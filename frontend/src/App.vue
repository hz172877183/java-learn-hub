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

      <nav class="desktop-nav">
        <RouterLink to="/catalog">学习目录</RouterLink>
        <RouterLink to="/track">总路线</RouterLink>
        <RouterLink to="/interview">面试专区</RouterLink>
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

      <!-- 手机端顶部只保留品牌与管理入口，登录/个人中心走底部 Tab -->
      <div class="mobile-actions">
        <RouterLink to="/admin">管理</RouterLink>
      </div>
    </header>

    <RouterView />

    <nav class="tabbar" aria-label="主导航">
      <RouterLink to="/catalog">
        <span class="ico">▤</span>
        目录
      </RouterLink>
      <RouterLink to="/track">
        <span class="ico">◎</span>
        路线
      </RouterLink>
      <RouterLink to="/interview">
        <span class="ico">✦</span>
        面试
      </RouterLink>
      <RouterLink to="/me">
        <span class="ico">◍</span>
        我的
      </RouterLink>
    </nav>
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
  z-index: 1100;
}

.brand {
  font-family: Georgia, serif;
  color: var(--ink);
  font-size: 18px;
}

.desktop-nav {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.desktop-nav a {
  color: var(--muted);
}

.desktop-nav a.router-link-active {
  color: var(--accent);
}

.who {
  color: var(--ink);
  font-weight: 600;
}

.mobile-actions {
  display: none;
}

.tabbar {
  display: none;
}

@media (max-width: 768px) {
  .top {
    padding: 10px 14px;
  }
  .brand {
    font-size: 16px;
  }
  .desktop-nav {
    display: none;
  }
  .mobile-actions {
    display: block;
    font-size: 13px;
  }
  .tabbar {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 1100;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    background: rgba(255, 253, 248, 0.97);
    border-top: 1px solid var(--line);
    padding: 6px 0 calc(6px + env(safe-area-inset-bottom));
    backdrop-filter: blur(10px);
  }
  .tabbar a {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 3px;
    padding: 4px 0;
    font-size: 11px;
    color: var(--muted);
  }
  .tabbar a.router-link-active {
    color: var(--accent);
  }
  .tabbar .ico {
    font-size: 17px;
    line-height: 1;
  }
}
</style>
