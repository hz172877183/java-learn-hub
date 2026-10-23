<!-- 首页：项目定位、推荐路线与最新学习项。 -->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { fetchMeta, fetchTopics } from '@/api/client'
import type { TopicSummary } from '@/api/types'
import TopicCard from '@/components/TopicCard.vue'

const topics = ref<TopicSummary[]>([])
const total = ref(0)
const error = ref('')

onMounted(async () => {
  try {
    const [list, meta] = await Promise.all([fetchTopics({ size: '6' }), fetchMeta()])
    topics.value = list.items
    total.value = meta.publishedTotal
  } catch {
    error.value = '后端未启动或接口不可用。请先在 backend 目录用 JDK 21 运行 Spring Boot。'
  }
})
</script>

<template>
  <main class="page hero">
    <p class="eyebrow">Spring Boot 3 · Vue 3 · 仓库即教材</p>
    <h1>把热门 Java 技术栈放进同一张地图。</h1>
    <p class="lead muted">
      目录列出学习项；点进去有说明、路线、示例代码。门户本身用 Spring Boot 提供只读 API，中间件按阶段接入。
    </p>
    <div class="actions">
      <RouterLink class="btn" to="/track">查看学习路线</RouterLink>
      <RouterLink class="btn ghost" to="/catalog">浏览全部学习项</RouterLink>
    </div>
    <p class="muted">已发布 {{ total }} 项</p>
    <p v-if="error" class="error">{{ error }}</p>
    <div v-else class="grid">
      <TopicCard v-for="topic in topics" :key="topic.slug" :topic="topic" />
    </div>
  </main>
</template>

<style scoped>
.eyebrow {
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-size: 12px;
  color: var(--accent);
}
.lead {
  max-width: 640px;
  font-size: 18px;
}
.actions {
  display: flex;
  gap: 12px;
  margin: 20px 0;
  flex-wrap: wrap;
}
.btn {
  background: var(--accent);
  color: white;
  padding: 10px 16px;
  border-radius: 999px;
}
.btn.ghost {
  background: transparent;
  color: var(--accent);
  border: 1px solid var(--accent);
}
.btn:hover {
  text-decoration: none;
  opacity: 0.92;
}
.error {
  color: #8a2f2f;
}
@media (max-width: 768px) {
  .hero h1 {
    font-size: 28px;
  }
  .lead {
    font-size: 16px;
  }
  .actions {
    gap: 8px;
  }
  .btn {
    flex: 1 1 100%;
    text-align: center;
    padding: 12px 16px;
  }
}
</style>
