<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchProgress, fetchTrack, setProgress } from '@/api/client'
import type { LearningState, TrackResponse } from '@/api/types'
import TopicCard from '@/components/TopicCard.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const track = ref<TrackResponse | null>(null)
const error = ref('')
const states = ref<Record<string, LearningState>>({})

const totalTopics = computed(
  () => track.value?.stages.reduce((sum, stage) => sum + stage.topics.length, 0) ?? 0,
)
const completedCount = computed(
  () => Object.values(states.value).filter((state) => state === 'completed').length,
)

function stateOf(slug: string): LearningState {
  return states.value[slug] ?? 'not_started'
}

function completedIn(stageTopics: { slug: string }[]) {
  return stageTopics.filter((topic) => stateOf(topic.slug) === 'completed').length
}

async function loadProgress() {
  if (!auth.isLoggedIn) {
    return
  }
  try {
    const list = await fetchProgress()
    const map: Record<string, LearningState> = {}
    for (const item of list) {
      map[item.topicSlug] = item.state
    }
    states.value = map
  } catch {
    /* 进度加载失败不影响路线本身 */
  }
}

async function change(slug: string, state: LearningState) {
  try {
    await setProgress(slug, state)
    states.value = { ...states.value, [slug]: state }
    ElMessage.success('已更新学习状态')
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

onMounted(async () => {
  try {
    track.value = await fetchTrack()
  } catch {
    error.value = '无法加载路线'
  }
  await loadProgress()
})
</script>

<template>
  <main class="page">
    <p v-if="error">{{ error }}</p>
    <template v-else-if="track">
      <h1>{{ track.title }}</h1>
      <p class="muted">{{ track.audience }}</p>

      <div class="card progress-box">
        <template v-if="auth.isLoggedIn">
          <p>已完成 <strong>{{ completedCount }}</strong> / {{ totalTopics }} 个学习项</p>
          <el-progress
            :percentage="totalTopics ? Math.round((completedCount / totalTopics) * 100) : 0"
            :stroke-width="10"
          />
        </template>
        <p v-else class="muted">
          登录后可勾选学习进度、记录私人笔记。
          <el-button text type="primary" @click="$router.push({ path: '/login', query: { redirect: '/track' } })">
            去登录
          </el-button>
        </p>
      </div>

      <section v-for="stage in track.stages" :key="stage.title">
        <div class="stage-head">
          <h2>{{ stage.title }}</h2>
          <span v-if="auth.isLoggedIn" class="muted">
            {{ completedIn(stage.topics) }} / {{ stage.topics.length }}
          </span>
        </div>
        <p>{{ stage.goal }}</p>
        <div class="grid">
          <div v-for="topic in stage.topics" :key="topic.slug" class="topic-item">
            <TopicCard :topic="topic" />
            <el-select
              v-if="auth.isLoggedIn"
              :model-value="stateOf(topic.slug)"
              size="small"
              class="state-select"
              @change="(value: LearningState) => change(topic.slug, value)"
            >
              <el-option label="未开始" value="not_started" />
              <el-option label="学习中" value="learning" />
              <el-option label="已完成" value="completed" />
            </el-select>
          </div>
        </div>
      </section>
    </template>
  </main>
</template>

<style scoped>
section {
  margin-top: 32px;
}
.progress-box {
  margin-top: 16px;
}
.stage-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.topic-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.state-select {
  align-self: flex-start;
  width: 110px;
}
</style>
