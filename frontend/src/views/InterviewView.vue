<!-- 面试真题专区：按等级与分类筛选，支持单选判分和自答对照。 -->
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchInterviewMeta, fetchInterviewQuestions } from '@/api/client'
import type { InterviewMeta, InterviewQuestion } from '@/api/types'
import { interviewCategoryLabel, interviewLevelLabel } from '@/labels'

const level = ref('')
const category = ref('')
const keyword = ref('')
const page = ref(1)
const size = ref(20)

const items = ref<InterviewQuestion[]>([])
const total = ref(0)
const meta = ref<InterviewMeta | null>(null)
const loading = ref(false)
const error = ref('')

const answers = ref<Record<number, number | null>>({})
const texts = ref<Record<number, string>>({})
const revealed = ref<Record<number, boolean>>({})

const levels = computed(() => meta.value?.levels ?? [])
const categories = computed(() => meta.value?.categories ?? [])

function resetAnswers() {
  const a: Record<number, number | null> = {}
  const t: Record<number, string> = {}
  const r: Record<number, boolean> = {}
  items.value.forEach((item) => {
    a[item.id] = null
    t[item.id] = ''
    r[item.id] = false
  })
  answers.value = a
  texts.value = t
  revealed.value = r
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchInterviewQuestions({
      level: level.value,
      category: category.value,
      q: keyword.value.trim(),
      page: page.value - 1,
      size: size.value,
    })
    items.value = data.items
    total.value = data.total
    resetAnswers()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  void load()
}

function changePage(next: number) {
  page.value = next
  void load()
}

function changeSize(next: number) {
  size.value = next
  page.value = 1
  void load()
}

function reveal(id: number) {
  revealed.value = { ...revealed.value, [id]: true }
}

function isCorrect(item: InterviewQuestion) {
  return item.type === 'choice' && answers.value[item.id] === item.correctIndex
}

function optionLabel(index: number) {
  return String.fromCharCode(65 + index)
}

function redo(id: number) {
  answers.value = { ...answers.value, [id]: null }
  texts.value = { ...texts.value, [id]: '' }
  revealed.value = { ...revealed.value, [id]: false }
}

watch([level, category], () => {
  page.value = 1
  void load()
})

onMounted(async () => {
  void load()
  try {
    meta.value = await fetchInterviewMeta()
  } catch {
    ElMessage.warning('分类统计加载失败，筛选仍可使用')
  }
})
</script>

<template>
  <main class="page">
    <h1>面试真题专区</h1>
    <p class="muted">
      按等级和方向刷题：选择题提交即判分，自答题先自己说，再对照参考答案与知识点。
      共 {{ meta?.total ?? total }} 道。
    </p>

    <div class="filters card">
      <div class="level-row">
        <el-radio-group v-model="level" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button v-for="item in levels" :key="item.id" :value="item.id">
            {{ interviewLevelLabel[item.id] ?? item.id }}（{{ item.count }}）
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-row">
        <el-select v-model="category" placeholder="全部分类" clearable>
          <el-option
            v-for="item in categories"
            :key="item.id"
            :label="`${interviewCategoryLabel[item.id] ?? item.id}（${item.count}）`"
            :value="item.id"
          />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索题干 / 解析" clearable @keyup.enter="search" />
        <el-button type="primary" @click="search">搜索</el-button>
      </div>
    </div>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-else-if="loading" class="muted">加载中…</p>
    <p v-else-if="!items.length" class="muted">没有符合条件的题目。</p>

    <div v-else class="list">
      <article v-for="item in items" :key="item.id" class="card question">
        <div class="badges">
          <span class="badge level">{{ interviewLevelLabel[item.level] ?? item.level }}</span>
          <span class="badge">{{ interviewCategoryLabel[item.category] ?? item.category }}</span>
          <span class="badge type">{{ item.type === 'choice' ? '单选题' : '自答题' }}</span>
          <span class="badge id">#{{ item.id }}</span>
        </div>

        <p class="stem">{{ item.stem }}</p>

        <template v-if="item.type === 'choice'">
          <el-radio-group
            v-model="answers[item.id]"
            :disabled="revealed[item.id]"
            class="options"
          >
            <el-radio v-for="(option, index) in item.options" :key="index" :value="index">
              {{ optionLabel(index) }}. {{ option }}
            </el-radio>
          </el-radio-group>

          <div v-if="!revealed[item.id]" class="actions">
            <el-button
              type="primary"
              size="small"
              :disabled="answers[item.id] === null || answers[item.id] === undefined"
              @click="reveal(item.id)"
            >
              提交答案
            </el-button>
          </div>
          <div v-else class="answer">
            <p :class="isCorrect(item) ? 'ok' : 'bad'">
              {{ isCorrect(item) ? '回答正确' : '回答错误' }}，正确答案是
              {{ optionLabel(item.correctIndex ?? 0) }}
            </p>
            <p class="label">知识点说明</p>
            <p class="muted">{{ item.explanation }}</p>
            <el-button text size="small" @click="redo(item.id)">重做</el-button>
          </div>
        </template>

        <template v-else>
          <el-input
            v-model="texts[item.id]"
            type="textarea"
            :rows="3"
            :disabled="revealed[item.id]"
            placeholder="先自己组织答案，再展开参考"
          />
          <div v-if="!revealed[item.id]" class="actions">
            <el-button size="small" @click="reveal(item.id)">查看参考答案</el-button>
          </div>
          <div v-else class="answer">
            <p class="label">参考答案</p>
            <p>{{ item.referenceAnswer }}</p>
            <p class="label">知识点说明</p>
            <p class="muted">{{ item.explanation }}</p>
            <el-button text size="small" @click="redo(item.id)">重做</el-button>
          </div>
        </template>
      </article>
    </div>

    <div v-if="total > 10" class="pager">
      <el-pagination
        :current-page="page"
        :page-size="size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="changePage"
        @size-change="changeSize"
      />
    </div>
  </main>
</template>

<style scoped>
.filters {
  display: grid;
  gap: 12px;
  margin: 16px 0 20px;
}
.level-row {
  overflow-x: auto;
}
.filter-row {
  display: grid;
  grid-template-columns: 220px 1fr auto;
  gap: 10px;
}
.list {
  display: grid;
  gap: 14px;
}
.question {
  padding: 18px 20px;
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
.badge.level {
  background: var(--ink);
  color: #fff;
}
.badge.id {
  margin-left: auto;
  background: transparent;
  color: var(--muted);
  padding-right: 0;
}
.stem {
  font-weight: 600;
  line-height: 1.7;
  margin: 12px 0;
}
.options {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}
.actions {
  margin-top: 12px;
}
.answer {
  margin-top: 12px;
  border-top: 1px dashed var(--line);
  padding-top: 10px;
}
.answer p {
  margin: 4px 0;
  line-height: 1.7;
}
.label {
  font-size: 12px;
  color: var(--accent);
  font-weight: 600;
}
.ok {
  color: #1e7a4f;
  font-weight: 600;
}
.bad {
  color: #c0392b;
  font-weight: 600;
}
.error {
  color: #c0392b;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
@media (max-width: 800px) {
  .filter-row {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 768px) {
  .pager :deep(.el-pagination__sizes),
  .pager :deep(.el-pagination__jump) {
    display: none;
  }
  .question {
    padding: 14px;
  }
  .badge.id {
    display: none;
  }
  .options :deep(.el-radio) {
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
