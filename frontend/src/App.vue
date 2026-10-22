<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="shell">
    <header class="top">
      <RouterLink class="brand" to="/">java-learn-hub</RouterLink>
      <nav>
        <RouterLink to="/catalog">学习目录</RouterLink>
        <RouterLink to="/track">总路线</RouterLink>
        <RouterLink to="/about">关于</RouterLink>
        <RouterLink to="/admin">管理</RouterLink>
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
}

nav a {
  color: var(--muted);
}

nav a.router-link-active {
  color: var(--accent);
}
</style>
