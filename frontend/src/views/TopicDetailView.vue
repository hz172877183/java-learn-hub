<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { fetchNote, fetchProgress, fetchTopic, saveNote, setProgress } from '@/api/client'
import type { LearningState, TopicDetail } from '@/api/types'
import { categoryLabel, difficultyLabel, integrationLabel } from '@/labels'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()
const topic = ref<TopicDetail | null>(null)
const missing = ref(false)
const error = ref('')
const html = computed(() => (topic.value ? marked.parse(topic.value.overviewMd) : ''))

const state = ref<LearningState>('not_started')
const note = ref('')
const noteStatus = ref('')
const savingNote = ref(false)

async function copy(code: string) {
  await navigator.clipboard.writeText(code)
}

async function loadStudy(slug: string) {
  state.value = 'not_started'
  note.value = ''
  noteStatus.value = ''
  if (!auth.isLoggedIn) {
    return
  }
  try {
    const [progressList, noteData] = await Promise.all([fetchProgress(), fetchNote(slug)])
    state.value = progressList.find((item) => item.topicSlug === slug)?.state ?? 'not_started'
    note.value = noteData.content
    if (noteData.updatedAt) {
      noteStatus.value = `上次保存：${noteData.updatedAt.replace('T', ' ').slice(0, 16)}`
    }
  } catch {
    /* 未登录或网络问题时保持默认值 */
  }
}

async function saveState(value: LearningState) {
  if (!topic.value) {
    return
  }
  try {
    await setProgress(topic.value.slug, value)
    ElMessage.success('已更新学习状态')
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

async function saveNoteNow() {
  if (!topic.value) {
    return
  }
  savingNote.value = true
  try {
    const saved = await saveNote(topic.value.slug, note.value)
    noteStatus.value = saved.updatedAt
      ? `已保存：${saved.updatedAt.replace('T', ' ').slice(0, 16)}`
      : '已清空笔记'
    ElMessage.success('笔记已保存')
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    savingNote.value = false
  }
}

watch(
  () => route.params.slug,
  async (slug) => {
    topic.value = null
    missing.value = false
    error.value = ''
    try {
      topic.value = await fetchTopic(String(slug))
      await loadStudy(String(slug))
    } catch (e) {
      if (e instanceof Error && e.message === '404') missing.value = true
      else error.value = '加载失败'
    }
  },
  { immediate: true },
)
</script>

<template>
  <main class="page">
    <p v-if="missing">没有这篇学习项。<RouterLink to="/catalog">返回目录</RouterLink></p>
    <p v-else-if="error">{{ error }}</p>
    <article v-else-if="topic">
      <p class="muted">
        {{ categoryLabel[topic.category] }} · {{ difficultyLabel[topic.difficulty] }} ·
        {{ integrationLabel[topic.integrationStatus] }} · {{ topic.estimatedHours }} 小时
      </p>
      <h1>{{ topic.title }}</h1>
      <p>{{ topic.summary }}</p>
      <p v-if="topic.officialDocsUrl">
        <a :href="topic.officialDocsUrl" target="_blank" rel="noreferrer">官方文档</a>
      </p>

      <section class="card study">
        <template v-if="auth.isLoggedIn">
          <span>学习状态</span>
          <el-select v-model="state" size="small" class="state-select" @change="saveState">
            <el-option label="未开始" value="not_started" />
            <el-option label="学习中" value="learning" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </template>
        <template v-else>
          <span class="muted">
            登录后可勾选学习进度、写私人笔记。
            <RouterLink :to="{ path: '/login', query: { redirect: route.fullPath } }">去登录</RouterLink>
          </span>
        </template>
      </section>

      <section>
        <h2>概述</h2>
        <div class="md" v-html="html" />
      </section>

      <section v-if="topic.repoPointers.length">
        <h2>在本项目中的位置</h2>
        <ul>
          <li v-for="p in topic.repoPointers" :key="p.path">
            {{ p.label }}：<code>{{ p.path }}</code>
          </li>
        </ul>
      </section>
      <section v-else>
        <h2>在本项目中的位置</h2>
        <p class="muted">规划中，当前以文档或代码片段形式提供。</p>
      </section>

      <section>
        <h2>核心概念</h2>
        <dl>
          <template v-for="c in topic.concepts" :key="c.term">
            <dt>{{ c.term }}</dt>
            <dd>{{ c.explanation }}</dd>
          </template>
        </dl>
      </section>

      <section>
        <h2>学习路线</h2>
        <ol>
          <li v-for="step in topic.pathSteps" :key="step.title">
            <strong>{{ step.title }}</strong>
            <span v-if="step.hours">（{{ step.hours }}h）</span>
            <p>{{ step.goal }}</p>
          </li>
        </ol>
      </section>

      <section>
        <h2>示例代码</h2>
        <div v-for="sample in topic.samples" :key="sample.title" class="card">
          <div class="sample-head">
            <strong>{{ sample.title }}</strong>
            <el-button size="small" @click="copy(sample.code)">复制</el-button>
          </div>
          <p class="muted">{{ sample.description }}</p>
          <pre class="code"><code>{{ sample.code }}</code></pre>
        </div>
      </section>

      <section>
        <h2>常见坑</h2>
        <ul>
          <li v-for="pit in topic.pitfalls" :key="pit.title">
            <strong>{{ pit.title }}：</strong>{{ pit.detail }}
          </li>
        </ul>
      </section>

      <section class="related">
        <div>
          <h2>前置</h2>
          <p v-if="!topic.prerequisites.length" class="muted">无</p>
          <RouterLink v-for="r in topic.prerequisites" :key="r.slug" :to="`/topics/${r.slug}`">
            {{ r.title }}
          </RouterLink>
        </div>
        <div>
          <h2>后续</h2>
          <p v-if="!topic.nextTopics.length" class="muted">无</p>
          <RouterLink v-for="r in topic.nextTopics" :key="r.slug" :to="`/topics/${r.slug}`">
            {{ r.title }}
          </RouterLink>
        </div>
        <div>
          <h2>相关</h2>
          <p v-if="!topic.relatedTopics.length" class="muted">无</p>
          <RouterLink v-for="r in topic.relatedTopics" :key="r.slug" :to="`/topics/${r.slug}`">
            {{ r.title }}
          </RouterLink>
        </div>
      </section>

      <section v-if="auth.isLoggedIn" class="card note-box">
        <h2>我的笔记 <span class="muted">仅自己可见</span></h2>
        <el-input
          v-model="note"
          type="textarea"
          :rows="8"
          placeholder="记录你的理解、命令、踩坑……支持 Markdown。"
        />
        <div class="note-actions">
          <el-button type="primary" :loading="savingNote" @click="saveNoteNow">保存笔记</el-button>
          <span class="muted">{{ noteStatus }}</span>
        </div>
      </section>
    </article>
  </main>
</template>

<style scoped>
section {
  margin-top: 28px;
}
dt {
  font-weight: 600;
  margin-top: 10px;
}
dd {
  margin: 4px 0 0;
  color: var(--muted);
}
.sample-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.related {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}
.related a {
  display: block;
  margin-bottom: 6px;
}
.study {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
}
.state-select {
  width: 120px;
}
.note-box {
  margin-top: 28px;
}
.note-box h2 {
  display: flex;
  gap: 8px;
  align-items: baseline;
}
.note-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}
</style>
