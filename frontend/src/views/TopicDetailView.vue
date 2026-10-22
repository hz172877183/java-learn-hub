<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { marked } from 'marked'
import { fetchTopic } from '@/api/client'
import type { TopicDetail } from '@/api/types'
import { categoryLabel, difficultyLabel, integrationLabel } from '@/labels'

const route = useRoute()
const topic = ref<TopicDetail | null>(null)
const missing = ref(false)
const error = ref('')
const html = computed(() => (topic.value ? marked.parse(topic.value.overviewMd) : ''))

async function copy(code: string) {
  await navigator.clipboard.writeText(code)
}

watch(
  () => route.params.slug,
  async (slug) => {
    topic.value = null
    missing.value = false
    error.value = ''
    try {
      topic.value = await fetchTopic(String(slug))
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
</style>
