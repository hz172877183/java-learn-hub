<!-- 学习项卡片：首页、目录与路线复用。 -->
<script setup lang="ts">
import { RouterLink } from 'vue-router'
import type { TopicSummary } from '@/api/types'
import { categoryLabel, difficultyLabel, integrationLabel } from '@/labels'

defineProps<{ topic: TopicSummary }>()
</script>

<template>
  <RouterLink class="card topic-card" :to="`/topics/${topic.slug}`">
    <div class="meta">
      {{ categoryLabel[topic.category] ?? topic.category }} ·
      {{ difficultyLabel[topic.difficulty] ?? topic.difficulty }} ·
      {{ topic.estimatedHours }}h
    </div>
    <h3>{{ topic.title }}</h3>
    <p class="muted">{{ topic.summary }}</p>
    <div class="tags">
      <span class="tag">{{ integrationLabel[topic.integrationStatus] ?? topic.integrationStatus }}</span>
      <span v-for="tag in topic.tags" :key="tag" class="tag">{{ tag }}</span>
    </div>
  </RouterLink>
</template>

<style scoped>
.topic-card {
  color: inherit;
  display: block;
}
.topic-card:hover {
  text-decoration: none;
  border-color: var(--accent);
}
.meta {
  font-size: 12px;
  color: var(--muted);
}
p {
  margin: 0;
  min-height: 44px;
}
</style>
