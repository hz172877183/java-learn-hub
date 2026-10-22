<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchTrack } from '@/api/client'
import type { TrackResponse } from '@/api/types'
import TopicCard from '@/components/TopicCard.vue'

const track = ref<TrackResponse | null>(null)
const error = ref('')

onMounted(async () => {
  try {
    track.value = await fetchTrack()
  } catch {
    error.value = '无法加载路线'
  }
})
</script>

<template>
  <main class="page">
    <p v-if="error">{{ error }}</p>
    <template v-else-if="track">
      <h1>{{ track.title }}</h1>
      <p class="muted">{{ track.audience }}</p>
      <section v-for="stage in track.stages" :key="stage.title">
        <h2>{{ stage.title }}</h2>
        <p>{{ stage.goal }}</p>
        <div class="grid">
          <TopicCard v-for="topic in stage.topics" :key="topic.slug" :topic="topic" />
        </div>
      </section>
    </template>
  </main>
</template>

<style scoped>
section {
  margin-top: 32px;
}
</style>
