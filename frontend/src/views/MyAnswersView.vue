<!-- 答题记录与错题本：查看最近作答、正确率和错题。 -->
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { deleteAnswer, fetchAnswerStats, fetchAnswers } from '@/api/client'
import type { AnswerStats, AnswerView } from '@/api/types'
import { interviewCategoryLabel, interviewLevelLabel } from '@/labels'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const wrongOnly = ref(false)
const source = ref('')
const level = ref('')
const category = ref('')
const page = ref(1)
const size = 20

const items = ref<AnswerView[]>([])
const total = ref(0)
const stats = ref<AnswerStats>({ total: 0, correct: 0, wrong: 0, ungraded: 0 })
const loading = ref(false)
const error = ref('')

const accuracy = computed(() => {
  const graded = stats.value.correct + stats.value.wrong
  return graded ? Math.round((stats.value.correct / graded) * 100) : 0
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchAnswers({
      source: source.value,
      wrong: wrongOnly.value || undefined,
      level: level.value,
      category: category.value,
      page: page.value - 1,
      size,
    })
    items.value = data.items
    total.value = data.total
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  try {
    stats.value = await fetchAnswerStats()
  } catch {
    /* 统计失败不影响列表 */
  }
}

function resetPage() {
  page.value = 1
  void load()
}

function changePage(next: number) {
  page.value = next
  void load()
}

async function remove(item: AnswerView) {
  try {
    await deleteAnswer(item.source, item.questionId)
    ElMessage.success('已移出记录')
    await Promise.all([load(), loadStats()])
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

function optionLabel(index?: number) {
  return index === undefined || index === null ? '-' : String.fromCharCode(65 + index)
}

function resultLabel(item: AnswerView) {
  if (item.correct === true) return '答对'
  if (item.correct === false) return '答错'
  return '未判定'
}

watch([wrongOnly, source, level, category], resetPage)

onMounted(async () => {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录')
    await router.replace({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  await Promise.all([load(), loadStats()])
})
</script>

<template>
  <main class="page">
    <div class="head">
      <div>
        <h1>答题记录</h1>
        <p class="muted">最近一次作答会覆盖同题记录；答对后会自动离开错题本。</p>
      </div>
      <RouterLink to="/me">返回我的学习</RouterLink>
    </div>

    <section class="stats">
      <div class="stat card">
        <strong>{{ stats.total }}</strong>
        <span class="muted">已答</span>
      </div>
      <div class="stat card">
        <strong>{{ stats.correct }}</strong>
        <span class="muted">答对</span>
      </div>
      <div class="stat card">
        <strong>{{ stats.wrong }}</strong>
        <span class="muted">答错</span>
      </div>
      <div class="stat card">
        <strong>{{ accuracy }}%</strong>
        <span class="muted">正确率</span>
      </div>
    </section>

    <div class="toolbar card">
      <el-radio-group v-model="wrongOnly" size="small">
        <el-radio-button :value="false">全部记录</el-radio-button>
        <el-radio-button :value="true">错题本</el-radio-button>
      </el-radio-group>
      <el-select v-model="source" placeholder="全部来源" clearable>
        <el-option label="面试题库" value="interview" />
        <el-option label="随堂题" value="topic_quiz" />
      </el-select>
      <el-select v-if="source !== 'topic_quiz'" v-model="level" placeholder="全部等级" clearable>
        <el-option
          v-for="(label, id) in interviewLevelLabel"
          :key="id"
          :label="label"
          :value="id"
        />
      </el-select>
      <el-select v-if="source !== 'topic_quiz'" v-model="category" placeholder="全部分类" clearable>
        <el-option
          v-for="(label, id) in interviewCategoryLabel"
          :key="id"
          :label="label"
          :value="id"
        />
      </el-select>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading" class="muted">加载中…</p>
    <p v-else-if="!items.length" class="muted">
      {{ wrongOnly ? '错题本是空的，继续保持。' : '还没有答题记录，去面试专区或学习项里做几题吧。' }}
    </p>

    <div v-else class="list">
      <article v-for="item in items" :key="`${item.source}-${item.questionId}`" class="card record">
        <div class="badges">
          <span class="badge source">{{ item.source === 'interview' ? '面试题' : '随堂题' }}</span>
          <span v-if="item.level" class="badge">{{ interviewLevelLabel[item.level] ?? item.level }}</span>
          <span v-if="item.category" class="badge">
            {{ interviewCategoryLabel[item.category] ?? item.category }}
          </span>
          <span v-if="item.topicTitle" class="badge">{{ item.topicTitle }}</span>
          <span class="badge result" :class="{ good: item.correct === true, bad: item.correct === false }">
            {{ resultLabel(item) }}
          </span>
        </div>

        <p class="stem">{{ item.stem }}</p>

        <div class="answer-row">
          <span class="label">我的答案</span>
          <span v-if="item.questionType === 'choice'">
            {{ optionLabel(item.choiceIndex) }}. {{ item.options[item.choiceIndex ?? 0] }}
          </span>
          <span v-else class="answer-text">{{ item.textAnswer }}</span>
        </div>
        <div v-if="item.questionType === 'choice'" class="answer-row">
          <span class="label">正确答案</span>
          <span>{{ optionLabel(item.correctIndex) }}. {{ item.options[item.correctIndex ?? 0] }}</span>
        </div>

        <details class="explain">
          <summary>查看解析</summary>
          <p v-if="item.questionType === 'text'" class="muted">参考答案：{{ item.referenceAnswer }}</p>
          <p class="muted">{{ item.explanation }}</p>
        </details>

        <div class="foot">
          <span class="muted">
            作答 {{ item.attempts }} 次 · {{ item.answeredAt.replace('T', ' ').slice(0, 16) }}
          </span>
          <span class="links">
            <RouterLink v-if="item.topicSlug" :to="`/topics/${item.topicSlug}`">去复习</RouterLink>
            <el-button text type="danger" size="small" @click="remove(item)">
              {{ wrongOnly ? '移出错题本' : '删除记录' }}
            </el-button>
          </span>
        </div>
      </article>
    </div>

    <div v-if="total > size" class="pager">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="changePage"
      />
    </div>
  </main>
</template>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin: 16px 0;
}
.stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 14px;
}
.stat strong {
  font-size: 22px;
}
.toolbar {
  display: grid;
  grid-template-columns: 1fr 160px 140px 160px;
  gap: 10px;
  align-items: center;
  margin-bottom: 16px;
}
.list {
  display: grid;
  gap: 12px;
}
.badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.badge {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent);
}
.badge.source {
  background: var(--ink);
  color: #fff;
}
.badge.result {
  margin-left: auto;
  background: #eee;
  color: var(--muted);
}
.badge.result.good {
  background: #d9f0e3;
  color: #1e7a4f;
}
.badge.result.bad {
  background: #f7dedb;
  color: #a5342a;
}
.stem {
  font-weight: 600;
  line-height: 1.65;
  margin: 10px 0;
}
.answer-row {
  display: flex;
  gap: 10px;
  margin: 4px 0;
  line-height: 1.6;
}
.label {
  font-size: 12px;
  color: var(--accent);
  font-weight: 600;
  white-space: nowrap;
  padding-top: 2px;
}
.answer-text {
  white-space: pre-wrap;
}
.explain {
  margin-top: 8px;
}
.explain summary {
  cursor: pointer;
  color: var(--muted);
  font-size: 13px;
}
.foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed var(--line);
}
.links {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}
.error {
  color: #c0392b;
}
@media (max-width: 768px) {
  .head {
    flex-direction: column;
  }
  .stats {
    grid-template-columns: repeat(2, 1fr);
  }
  .toolbar {
    grid-template-columns: 1fr;
  }
  .foot {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
