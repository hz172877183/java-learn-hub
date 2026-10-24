<!-- 学习项详情：概述、使用场景、概念、路线、示例、常见坑、面试题与进度。 -->
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import { fetchProgress, fetchQuizzes, fetchTopic, setProgress, submitAnswer } from '@/api/client'
import type { LearningState, QuizQuestion, TopicDetail } from '@/api/types'
import { categoryLabel, difficultyLabel, integrationLabel } from '@/labels'
import { useAuthStore } from '@/stores/auth'
import NotePanel from '@/components/NotePanel.vue'

const route = useRoute()
const auth = useAuthStore()
const topic = ref<TopicDetail | null>(null)
const missing = ref(false)
const error = ref('')
const html = computed(() => (topic.value ? marked.parse(topic.value.overviewMd) : ''))

const state = ref<LearningState>('not_started')

const quizzes = ref<QuizQuestion[]>([])
const quizAnswers = ref<Record<number, number | null>>({})
const quizText = ref<Record<number, string>>({})
const quizRevealed = ref<Record<number, boolean>>({})
const quizAssessed = ref<Record<number, boolean | undefined>>({})
const quizSaving = ref<number | null>(null)

/** 清空所有题目的作答与解析状态，用于“全部重做”。 */
function resetQuiz() {
  const answers: Record<number, number | null> = {}
  const texts: Record<number, string> = {}
  const revealed: Record<number, boolean> = {}
  const assessed: Record<number, boolean | undefined> = {}
  quizzes.value.forEach((_, index) => {
    answers[index] = null
    texts[index] = ''
    revealed[index] = false
    assessed[index] = undefined
  })
  quizAnswers.value = answers
  quizText.value = texts
  quizRevealed.value = revealed
  quizAssessed.value = assessed
}

/** 展开本题的答案与知识点说明。 */
function reveal(index: number) {
  quizRevealed.value = { ...quizRevealed.value, [index]: true }
}

/** 随堂选择题：先记录到答题历史，再展开解析。 */
async function submitQuizChoice(question: QuizQuestion, index: number) {
  const choice = quizAnswers.value[index]
  if (choice === null || choice === undefined || quizSaving.value === index) {
    return
  }
  if (!auth.isLoggedIn) {
    reveal(index)
    return
  }
  quizSaving.value = index
  try {
    if (question.id) {
      await submitAnswer({ source: 'topic_quiz', questionId: question.id, choiceIndex: choice })
    }
  } catch (e) {
    ElMessage.warning('答题记录保存失败：' + (e as Error).message)
  } finally {
    quizSaving.value = null
    reveal(index)
  }
}

/** 随堂自答题：展开答案时保存作答。 */
async function revealQuizText(question: QuizQuestion, index: number) {
  const text = quizText.value[index]?.trim()
  reveal(index)
  if (!auth.isLoggedIn || !question.id || !text) {
    return
  }
  try {
    await submitAnswer({ source: 'topic_quiz', questionId: question.id, textAnswer: text })
  } catch {
    /* 记录失败不影响看答案 */
  }
}

/** 自答自评，用于错题本。 */
async function assessQuiz(question: QuizQuestion, index: number, correct: boolean) {
  if (!auth.isLoggedIn) {
    ElMessage.warning('登录后才能记录错题')
    return
  }
  const text = quizText.value[index]?.trim()
  if (!question.id || !text) {
    ElMessage.warning('先写下你的答案，再自评')
    return
  }
  try {
    await submitAnswer({
      source: 'topic_quiz',
      questionId: question.id,
      textAnswer: text,
      selfCorrect: correct,
    })
    quizAssessed.value = { ...quizAssessed.value, [index]: correct }
    ElMessage.success(correct ? '已标记掌握' : '已加入错题本')
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

/** 单选题判分：只比较选中下标，自答题不参与。 */
function isCorrect(index: number) {
  const question = quizzes.value[index]
  return question?.type === 'choice' && quizAnswers.value[index] === question.correctIndex
}

function optionLabel(index: number) {
  return String.fromCharCode(65 + index)
}

/** 题目加载失败不影响正文阅读，静默降级为空列表。 */
async function loadQuizzes(slug: string) {
  quizzes.value = []
  try {
    quizzes.value = await fetchQuizzes(slug)
  } catch {
    quizzes.value = []
  }
  resetQuiz()
}

async function copy(code: string) {
  await navigator.clipboard.writeText(code)
}

async function loadStudy(slug: string) {
  state.value = 'not_started'
  if (!auth.isLoggedIn) {
    return
  }
  try {
    const progressList = await fetchProgress()
    state.value = progressList.find((item) => item.topicSlug === slug)?.state ?? 'not_started'
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

// 路由参数变化即整页重载；进度与题目并行加载，互不阻塞
watch(
  () => route.params.slug,
  async (slug) => {
    topic.value = null
    missing.value = false
    error.value = ''
    try {
      topic.value = await fetchTopic(String(slug))
      await Promise.all([loadStudy(String(slug)), loadQuizzes(String(slug))])
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

      <section v-if="topic.scenarios.length">
        <h2>典型使用场景</h2>
        <p class="muted">遇到下面这些信号时，可以想到用它。</p>
        <div class="scenario-grid">
          <div v-for="scenario in topic.scenarios" :key="scenario.title" class="card scenario">
            <h3>{{ scenario.title }}</h3>
            <p class="label">什么时候会想到它</p>
            <p>{{ scenario.signal }}</p>
            <p class="label">具体怎么做</p>
            <p>{{ scenario.approach }}</p>
          </div>
        </div>
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

      <section v-if="quizzes.length" class="quiz">
        <div class="quiz-head">
          <h2>面试真题</h2>
          <el-button text @click="resetQuiz">全部重做</el-button>
        </div>
        <p class="muted">选择题提交后判分；自答题先自己写，再看参考答案。</p>

        <div v-for="(question, index) in quizzes" :key="index" class="card quiz-item">
          <div class="quiz-meta">
            <span class="pill">{{ question.type === 'choice' ? '单选题' : '自答题' }}</span>
            <span class="muted">第 {{ index + 1 }} 题</span>
          </div>
          <p class="stem">{{ question.stem }}</p>

          <template v-if="question.type === 'choice'">
            <el-radio-group v-model="quizAnswers[index]" :disabled="quizRevealed[index]" class="options">
              <el-radio
                v-for="(option, optionIndex) in question.options"
                :key="optionIndex"
                :value="optionIndex"
                class="option"
              >
                {{ optionLabel(optionIndex) }}. {{ option }}
              </el-radio>
            </el-radio-group>
            <div v-if="!quizRevealed[index]" class="quiz-actions">
              <el-button
                type="primary"
                size="small"
                :disabled="quizAnswers[index] === null || quizAnswers[index] === undefined"
                :loading="quizSaving === index"
                @click="submitQuizChoice(question, index)"
              >
                提交答案
              </el-button>
            </div>
            <div v-else class="answer">
              <p :class="isCorrect(index) ? 'ok' : 'bad'">
                {{ isCorrect(index) ? '回答正确' : '回答错误' }}，正确答案是
                {{ optionLabel(question.correctIndex ?? 0) }}
              </p>
              <p class="label">知识点说明</p>
              <p class="muted">{{ question.explanation }}</p>
            </div>
          </template>

          <template v-else>
            <el-input
              v-model="quizText[index]"
              type="textarea"
              :rows="4"
              :disabled="quizRevealed[index]"
              placeholder="先自己写答案，再看参考答案（系统不判分）"
            />
            <div v-if="!quizRevealed[index]" class="quiz-actions">
              <el-button size="small" @click="revealQuizText(question, index)">查看参考答案</el-button>
            </div>
            <div v-else class="answer">
              <p class="label">参考答案</p>
              <p>{{ question.referenceAnswer }}</p>
              <p class="label">知识点说明</p>
              <p class="muted">{{ question.explanation }}</p>
              <div v-if="auth.isLoggedIn" class="assess">
                <span class="muted">自评：</span>
                <el-button
                  size="small"
                  :type="quizAssessed[index] === true ? 'success' : ''"
                  @click="assessQuiz(question, index, true)"
                >
                  已掌握
                </el-button>
                <el-button
                  size="small"
                  :type="quizAssessed[index] === false ? 'danger' : ''"
                  plain
                  @click="assessQuiz(question, index, false)"
                >
                  加入错题本
                </el-button>
              </div>
            </div>
          </template>
        </div>
      </section>

    </article>

    <NotePanel v-if="topic && auth.isLoggedIn" :slug="topic.slug" :title="topic.title" />
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
.scenario-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 14px;
  margin-top: 10px;
}
.scenario h3 {
  margin: 0 0 8px;
  font-size: 15px;
}
.label {
  font-size: 12px;
  color: var(--accent);
  font-weight: 600;
  margin: 10px 0 2px;
}
.scenario p {
  margin: 0;
  line-height: 1.6;
}
.quiz-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.quiz-item {
  margin-top: 12px;
}
.quiz-meta {
  display: flex;
  gap: 10px;
  align-items: center;
}
.pill {
  background: var(--accent-soft);
  color: var(--accent);
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 999px;
}
.stem {
  font-weight: 600;
  margin: 10px 0 12px;
  line-height: 1.6;
}
.options {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
}
.option {
  height: auto;
  white-space: normal;
}
.quiz-actions {
  margin-top: 12px;
}
.answer {
  margin-top: 12px;
  border-top: 1px dashed var(--line);
  padding-top: 10px;
}
.answer p {
  margin: 4px 0;
  line-height: 1.6;
}
.ok {
  color: #1e7a4f;
  font-weight: 600;
}
.assess {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 0;
  flex-wrap: wrap;
}
.bad {
  color: #c0392b;
  font-weight: 600;
}
@media (max-width: 768px) {
  .study {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
    flex-wrap: wrap;
  }
  .study a {
    white-space: nowrap;
  }
  .related {
    grid-template-columns: 1fr;
  }
  .sample-head {
    flex-wrap: wrap;
    gap: 8px;
  }
  .options :deep(.el-radio),
  .option {
    height: auto;
    margin-right: 0;
    align-items: flex-start;
    white-space: normal;
    width: 100%;
  }
  .options :deep(.el-radio__label) {
    white-space: normal;
    line-height: 1.6;
  }
}
</style>
