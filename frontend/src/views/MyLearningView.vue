<!-- 我的学习：进度汇总、按状态分组与笔记列表。 -->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchMeta, fetchNotes, fetchProgress, setProgress } from '@/api/client'
import type { LearningState, NoteItem, ProgressItem } from '@/api/types'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()

const progress = ref<ProgressItem[]>([])
const notes = ref<NoteItem[]>([])
const total = ref(0)
const loading = ref(false)
const error = ref('')

const completed = computed(() => progress.value.filter((p) => p.state === 'completed'))
const learning = computed(() => progress.value.filter((p) => p.state === 'learning'))
const percent = computed(() =>
  total.value === 0 ? 0 : Math.round((completed.value.length / total.value) * 100),
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [p, n, meta] = await Promise.all([fetchProgress(), fetchNotes(), fetchMeta()])
    progress.value = p
    notes.value = n
    total.value = meta.publishedTotal
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

/** 状态变更后重新拉取列表，保证排序和时间戳与服务端一致。 */
async function changeState(item: ProgressItem, state: LearningState) {
  try {
    await setProgress(item.topicSlug, state)
    if (state === 'not_started') {
      progress.value = progress.value.filter((p) => p.topicSlug !== item.topicSlug)
    } else {
      const updated = await fetchProgress()
      progress.value = updated
    }
    ElMessage.success('已更新')
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

function preview(text: string) {
  const flat = text.replace(/\s+/g, ' ').trim()
  return flat.length > 120 ? `${flat.slice(0, 120)}…` : flat
}

onMounted(() => {
  if (auth.isLoggedIn) {
    void load()
  }
})
</script>

<template>
  <main class="page">
    <h1>我的学习</h1>

    <div v-if="!auth.isLoggedIn" class="card">
      <p>登录后可以记录学习进度、写私人笔记。</p>
      <el-button type="primary" @click="$router.push({ path: '/login', query: { redirect: route.fullPath } })">
        去登录
      </el-button>
    </div>

    <template v-else>
      <p v-if="error" class="error">{{ error }}</p>

      <section v-loading="loading" class="card summary">
        <div class="stat">
          <strong>{{ completed.length }}</strong>
          <span class="muted">已完成</span>
        </div>
        <div class="stat">
          <strong>{{ learning.length }}</strong>
          <span class="muted">学习中</span>
        </div>
        <div class="stat">
          <strong>{{ notes.length }}</strong>
          <span class="muted">篇笔记</span>
        </div>
        <el-progress :percentage="percent" :stroke-width="10" />
        <p class="muted">全站共 {{ total }} 个已发布学习项</p>
      </section>

      <section>
        <h2>学习中</h2>
        <p v-if="!learning.length" class="muted">还没有进行中的学习项。</p>
        <ul class="list">
          <li v-for="item in learning" :key="item.topicSlug" class="card row">
            <RouterLink :to="`/topics/${item.topicSlug}`">{{ item.title }}</RouterLink>
            <el-button size="small" type="primary" plain @click="changeState(item, 'completed')">
              标记完成
            </el-button>
          </li>
        </ul>
      </section>

      <section>
        <h2>已完成</h2>
        <p v-if="!completed.length" class="muted">还没有完成的学习项。</p>
        <ul class="list">
          <li v-for="item in completed" :key="item.topicSlug" class="card row">
            <RouterLink :to="`/topics/${item.topicSlug}`">{{ item.title }}</RouterLink>
            <el-button size="small" plain @click="changeState(item, 'learning')">重新学习</el-button>
          </li>
        </ul>
      </section>

      <section>
        <h2>我的笔记</h2>
        <p v-if="!notes.length" class="muted">还没有笔记，去任意学习项页面写一条吧。</p>
        <ul class="list">
          <li v-for="note in notes" :key="note.topicSlug" class="card note">
            <div class="note-head">
              <RouterLink :to="`/topics/${note.topicSlug}`">{{ note.title }}</RouterLink>
              <span class="muted">{{ note.updatedAt?.replace('T', ' ').slice(0, 16) }}</span>
            </div>
            <p class="muted">{{ preview(note.content) }}</p>
          </li>
        </ul>
      </section>
    </template>
  </main>
</template>

<style scoped>
section {
  margin-top: 28px;
}
.summary {
  display: grid;
  grid-template-columns: repeat(3, auto) 1fr;
  gap: 16px;
  align-items: center;
}
.stat {
  display: flex;
  flex-direction: column;
}
.stat strong {
  font-size: 24px;
}
.list {
  list-style: none;
  padding: 0;
  display: grid;
  gap: 10px;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.note-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.error {
  color: #c0392b;
}
@media (max-width: 800px) {
  .summary {
    grid-template-columns: 1fr 1fr 1fr;
  }
}
</style>
