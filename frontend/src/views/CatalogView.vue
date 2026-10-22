<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { fetchMeta, fetchTopics } from '@/api/client'
import type { TopicSummary } from '@/api/types'
import TopicCard from '@/components/TopicCard.vue'
import { categoryLabel, difficultyLabel, integrationLabel } from '@/labels'

const filters = reactive({
  q: '',
  category: '',
  difficulty: '',
  integrationStatus: '',
})
const items = ref<TopicSummary[]>([])
const total = ref(0)
const categories = ref<{ id: string; count: number }[]>([])
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchTopics({ ...filters, size: '50' })
    items.value = data.items
    total.value = data.total
  } catch {
    error.value = '加载失败，确认后端已在 8080 端口运行。'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    categories.value = (await fetchMeta()).categories
  } catch {
    /* meta 失败时筛选项仍可用静态表 */
  }
  await load()
})

watch(filters, load)
</script>

<template>
  <main class="page">
    <h1>学习目录</h1>
    <p class="muted">按分类、难度、接入状态筛选。新增学习项只需加 JSON，不必改路由。</p>
    <div class="filters">
      <el-input v-model="filters.q" placeholder="搜索标题 / 标签 / 摘要" clearable />
      <el-select v-model="filters.category" placeholder="分类" clearable>
        <el-option
          v-for="c in categories"
          :key="c.id"
          :label="`${categoryLabel[c.id] ?? c.id} (${c.count})`"
          :value="c.id"
        />
      </el-select>
      <el-select v-model="filters.difficulty" placeholder="难度" clearable>
        <el-option v-for="(label, id) in difficultyLabel" :key="id" :label="label" :value="id" />
      </el-select>
      <el-select v-model="filters.integrationStatus" placeholder="接入状态" clearable>
        <el-option v-for="(label, id) in integrationLabel" :key="id" :label="label" :value="id" />
      </el-select>
    </div>
    <p class="muted">{{ loading ? '加载中…' : `共 ${total} 项` }}</p>
    <p v-if="error">{{ error }}</p>
    <p v-else-if="!loading && items.length === 0">没有符合条件的学习项。</p>
    <div v-else class="grid">
      <TopicCard v-for="topic in items" :key="topic.slug" :topic="topic" />
    </div>
  </main>
</template>

<style scoped>
.filters {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 10px;
  margin: 16px 0;
}
@media (max-width: 800px) {
  .filters {
    grid-template-columns: 1fr;
  }
}
</style>
