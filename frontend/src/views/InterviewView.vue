<!-- 面试真题专区：按等级与分类筛选，支持单选判分和自答对照。 -->
<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink } from 'vue-router'
import {
  clearStudyPosition,
  fetchInterviewMeta,
  fetchInterviewQuestions,
  fetchMyInterviewQuestions,
  fetchStudyPosition,
  resetMyInterviewAnswers,
  saveStudyPosition,
  submitAnswer,
} from '@/api/client'
import type { InterviewMeta, InterviewQuestion, StudyPosition, StudyPositionPayload } from '@/api/types'
import { interviewCategoryLabel, interviewLevelLabel } from '@/labels'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

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
const assessed = ref<Record<number, boolean | undefined>>({})
const savingId = ref<number | null>(null)

type Mode = 'unanswered' | 'all' | 'wrong'
const mode = ref<Mode>(auth.isLoggedIn ? 'unanswered' : 'all')
const myAnswered = ref<Record<number, boolean>>({})
const myResult = ref<Record<number, boolean | undefined>>({})
const resetDialog = ref(false)
const resetting = ref(false)
const resetScope = ref<'all' | 'level' | 'category' | 'current'>('all')

const resume = ref<StudyPosition | null>(null)
const restoring = ref(false)
const lastQuestionId = ref<number | null>(null)
const highlightId = ref<number | null>(null)
let saveTimer: ReturnType<typeof setTimeout> | undefined
let scrollTimer: ReturnType<typeof setTimeout> | undefined

const levels = computed(() => meta.value?.levels ?? [])
const categories = computed(() => meta.value?.categories ?? [])

function resetAnswers() {
  const a: Record<number, number | null> = {}
  const t: Record<number, string> = {}
  const r: Record<number, boolean> = {}
  const s: Record<number, boolean | undefined> = {}
  items.value.forEach((item) => {
    a[item.id] = null
    t[item.id] = ''
    r[item.id] = false
    s[item.id] = undefined
  })
  answers.value = a
  texts.value = t
  revealed.value = r
  assessed.value = s
  myAnswered.value = {}
  myResult.value = {}
}

/** 已答题回显：选中上次选项、回填自答文本、记录对错状态。 */
function applyEcho() {
  const a = { ...answers.value }
  const t = { ...texts.value }
  const s = { ...assessed.value }
  const answered: Record<number, boolean> = {}
  const result: Record<number, boolean | undefined> = {}
  for (const item of items.value) {
    const saved = item.myAnswer
    if (!saved) continue
    answered[item.id] = true
    if (item.type === 'choice' && saved.choiceIndex !== undefined && saved.choiceIndex !== null) {
      a[item.id] = saved.choiceIndex
    }
    if (saved.textAnswer) {
      t[item.id] = saved.textAnswer
    }
    if (saved.correct !== undefined && saved.correct !== null) {
      s[item.id] = saved.correct
      result[item.id] = saved.correct
    }
  }
  answers.value = a
  texts.value = t
  assessed.value = s
  myAnswered.value = answered
  myResult.value = result
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const params = {
      level: level.value,
      category: category.value,
      q: keyword.value.trim(),
      page: page.value - 1,
      size: size.value,
    }
    const data = auth.isLoggedIn
      ? await fetchMyInterviewQuestions({
          ...params,
          includeAnswered: mode.value !== 'unanswered',
          wrongOnly: mode.value === 'wrong',
        })
      : await fetchInterviewQuestions(params)
    items.value = data.items
    total.value = data.total
    resetAnswers()
    if (auth.isLoggedIn) applyEcho()
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

function search() {
  resume.value = null
  lastQuestionId.value = null
  page.value = 1
  void load()
  scheduleSave()
}

function changePage(next: number) {
  resume.value = null
  lastQuestionId.value = null
  page.value = next
  void load()
  scheduleSave()
}

function changeSize(next: number) {
  resume.value = null
  lastQuestionId.value = null
  size.value = next
  page.value = 1
  void load()
  scheduleSave()
}

const resumeSummary = computed(() => {
  const value = resume.value
  if (!value) return ''
  const parts: string[] = []
  if (value.level) parts.push(interviewLevelLabel[value.level] ?? value.level)
  if (value.category) parts.push(interviewCategoryLabel[value.category] ?? value.category)
  parts.push(`第 ${value.page} 页`)
  if (value.questionId) parts.push(`题 #${value.questionId}`)
  parts.push(`更新于 ${value.updatedAt.replace('T', ' ').slice(0, 16)}`)
  return parts.join(' · ')
})

/** 读取上次位置；没有记录时静默跳过。 */
async function loadPosition() {
  try {
    const value = await fetchStudyPosition('interview')
    if (value && (value.page > 1 || value.level || value.category || value.q || value.questionId)) {
      resume.value = value
    }
  } catch {
    /* 位置加载失败不影响答题 */
  }
}

function buildPosition(): StudyPositionPayload | null {
  if (!auth.isLoggedIn) return null
  return {
    level: level.value || undefined,
    category: category.value || undefined,
    q: keyword.value.trim() || undefined,
    page: page.value,
    size: size.value,
    questionId: lastQuestionId.value ?? undefined,
    scrollTop: Math.max(0, Math.round(window.scrollY)),
  }
}

/** 防抖保存位置，避免滚动/筛选时频繁写库。 */
function scheduleSave(delay = 800) {
  if (!auth.isLoggedIn) return
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => {
    const payload = buildPosition()
    if (!payload) return
    void saveStudyPosition('interview', payload).catch(() => {})
  }, delay)
}

function onScroll() {
  if (restoring.value) return
  if (scrollTimer) clearTimeout(scrollTimer)
  scrollTimer = setTimeout(() => scheduleSave(300), 700)
}

/** 继续上次位置：恢复筛选与页码，滚动到题号并短暂高亮。 */
async function applyPosition() {
  const value = resume.value
  if (!value) return
  restoring.value = true
  if (auth.isLoggedIn) {
    mode.value = 'all'
  }
  level.value = value.level ?? ''
  category.value = value.category ?? ''
  keyword.value = value.q ?? ''
  page.value = Math.max(1, value.page)
  size.value = value.size || 20
  await nextTick()
  await load()
  restoring.value = false
  resume.value = null
  lastQuestionId.value = value.questionId ?? null
  await nextTick()

  const target = value.questionId ? document.getElementById(`q-${value.questionId}`) : null
  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'center' })
    highlightId.value = value.questionId ?? null
    setTimeout(() => {
      highlightId.value = null
    }, 2200)
  } else {
    window.scrollTo({ top: value.scrollTop, behavior: 'smooth' })
  }
}

async function clearResume() {
  try {
    await clearStudyPosition('interview')
  } catch {
    /* 清除失败也让横幅消失 */
  }
  resume.value = null
}

function openReset() {
  resetScope.value =
    level.value && category.value
      ? 'current'
      : level.value
        ? 'level'
        : category.value
          ? 'category'
          : 'all'
  resetDialog.value = true
}

/** 按选定范围删除面试题答题记录，题目会重新回到「未答」。 */
async function confirmReset() {
  resetting.value = true
  try {
    const params =
      resetScope.value === 'all'
        ? {}
        : resetScope.value === 'level'
          ? { level: level.value }
          : resetScope.value === 'category'
            ? { category: category.value }
            : { level: level.value || undefined, category: category.value || undefined }
    const result = await resetMyInterviewAnswers(params)
    ElMessage.success(`已重置 ${result.deleted} 条记录`)
    resetDialog.value = false
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message)
  } finally {
    resetting.value = false
  }
}

function reveal(id: number) {
  revealed.value = { ...revealed.value, [id]: true }
}

/** 选择题：先写答题记录，再展开解析；记录失败不阻塞学习。 */
async function submitChoice(item: InterviewQuestion) {
  const index = answers.value[item.id]
  if (index === null || index === undefined || savingId.value === item.id) {
    return
  }
  if (!auth.isLoggedIn) {
    reveal(item.id)
    return
  }
  savingId.value = item.id
  try {
    const saved = await submitAnswer({ source: 'interview', questionId: item.id, choiceIndex: index })
    myAnswered.value = { ...myAnswered.value, [item.id]: true }
    myResult.value = { ...myResult.value, [item.id]: saved.correct }
  } catch (e) {
    ElMessage.warning('答题记录保存失败：' + (e as Error).message)
  } finally {
    savingId.value = null
    reveal(item.id)
    lastQuestionId.value = item.id
    scheduleSave(300)
  }
}

/** 自答题：展开参考答案时顺手保存作答（不判定对错）。 */
async function revealText(item: InterviewQuestion) {
  const text = texts.value[item.id]?.trim()
  reveal(item.id)
  lastQuestionId.value = item.id
  scheduleSave(300)
  if (!auth.isLoggedIn || !text) {
    return
  }
  try {
    await submitAnswer({ source: 'interview', questionId: item.id, textAnswer: text })
    myAnswered.value = { ...myAnswered.value, [item.id]: true }
  } catch {
    /* 记录失败不影响看答案 */
  }
}

/** 自答自评：已掌握 / 加入错题本。 */
async function assess(item: InterviewQuestion, correct: boolean) {
  if (!auth.isLoggedIn) {
    ElMessage.warning('登录后才能记录错题')
    return
  }
  const text = texts.value[item.id]?.trim()
  if (!text) {
    ElMessage.warning('先写下你的答案，再自评')
    return
  }
  try {
    await submitAnswer({
      source: 'interview',
      questionId: item.id,
      textAnswer: text,
      selfCorrect: correct,
    })
    assessed.value = { ...assessed.value, [item.id]: correct }
    myAnswered.value = { ...myAnswered.value, [item.id]: true }
    myResult.value = { ...myResult.value, [item.id]: correct }
    lastQuestionId.value = item.id
    scheduleSave(300)
    ElMessage.success(correct ? '已标记掌握' : '已加入错题本')
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
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
  assessed.value = { ...assessed.value, [id]: undefined }
}

watch([level, category], () => {
  if (restoring.value) {
    return
  }
  resume.value = null
  lastQuestionId.value = null
  page.value = 1
  void load()
  scheduleSave()
})

watch(mode, () => {
  if (restoring.value) {
    return
  }
  resume.value = null
  lastQuestionId.value = null
  page.value = 1
  void load()
  scheduleSave()
})

onMounted(async () => {
  window.addEventListener('scroll', onScroll, { passive: true })
  try {
    meta.value = await fetchInterviewMeta()
  } catch {
    ElMessage.warning('分类统计加载失败，筛选仍可使用')
  }
  if (auth.isLoggedIn) {
    await loadPosition()
  }
  await load()
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', onScroll)
  if (saveTimer) clearTimeout(saveTimer)
  if (scrollTimer) clearTimeout(scrollTimer)
})
</script>

<template>
  <main class="page">
    <h1>面试真题专区</h1>
    <p class="muted">
      按等级和方向刷题：选择题提交即判分，自答题先自己说，再对照参考答案与知识点。
      共 {{ meta?.total ?? total }} 道。
    </p>

    <div v-if="resume" class="resume card">
      <div class="resume-text">
        <strong>上次学习位置</strong>
        <span class="muted">{{ resumeSummary }}</span>
      </div>
      <div class="resume-actions">
        <el-button type="primary" size="small" @click="applyPosition">继续</el-button>
        <el-button text size="small" @click="clearResume">清除记录</el-button>
      </div>
    </div>

    <div v-if="auth.isLoggedIn" class="mode-row">
      <el-radio-group v-model="mode" size="small">
        <el-radio-button value="unanswered">未答</el-radio-button>
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="wrong">错题</el-radio-button>
      </el-radio-group>
      <el-button text size="small" @click="openReset">重置已答</el-button>
    </div>

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
    <div v-else-if="!items.length" class="empty">
      <p class="muted">
        {{
          mode === 'unanswered' && auth.isLoggedIn
            ? '当前筛选下已经全部答过，可以切到「全部」复习，或重置已答。'
            : '没有符合条件的题目。'
        }}
      </p>
      <el-button
        v-if="mode === 'unanswered' && auth.isLoggedIn"
        size="small"
        @click="openReset"
      >
        重置已答
      </el-button>
    </div>

    <div v-else class="list">
      <article
        v-for="item in items"
        :id="`q-${item.id}`"
        :key="item.id"
        class="card question"
        :class="{ highlight: highlightId === item.id }"
      >
        <div class="badges">
          <span class="badge level">{{ interviewLevelLabel[item.level] ?? item.level }}</span>
          <span class="badge">{{ interviewCategoryLabel[item.category] ?? item.category }}</span>
          <span class="badge type">{{ item.type === 'choice' ? '单选题' : '自答题' }}</span>
          <span
            v-if="auth.isLoggedIn && myAnswered[item.id]"
            class="badge answered"
            :class="{ good: myResult[item.id] === true, bad: myResult[item.id] === false }"
          >
            已答{{ myResult[item.id] === true ? ' · 答对' : myResult[item.id] === false ? ' · 答错' : '' }}
          </span>
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
              :loading="savingId === item.id"
              @click="submitChoice(item)"
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
            <el-button size="small" @click="revealText(item)">查看参考答案</el-button>
          </div>
          <div v-else class="answer">
            <p class="label">参考答案</p>
            <p>{{ item.referenceAnswer }}</p>
            <p class="label">知识点说明</p>
            <p class="muted">{{ item.explanation }}</p>
            <div v-if="auth.isLoggedIn" class="assess">
              <span class="muted">自评：</span>
              <el-button
                size="small"
                :type="assessed[item.id] === true ? 'success' : ''"
                @click="assess(item, true)"
              >
                已掌握
              </el-button>
              <el-button
                size="small"
                :type="assessed[item.id] === false ? 'danger' : ''"
                plain
                @click="assess(item, false)"
              >
                加入错题本
              </el-button>
            </div>
            <p v-else class="muted login-hint">
              <RouterLink :to="{ path: '/login', query: { redirect: '/interview' } }">登录</RouterLink>
              后可记录答题与错题。
            </p>
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

    <el-dialog v-model="resetDialog" title="重置答题记录" width="440px">
      <p class="muted">将删除对应面试题的答题记录，题目会重新出现在「未答」中。</p>
      <el-radio-group v-model="resetScope" class="reset-scope">
        <el-radio value="all">全部面试题</el-radio>
        <el-radio value="level" :disabled="!level">
          当前等级：{{ level ? interviewLevelLabel[level] ?? level : '未选择' }}
        </el-radio>
        <el-radio value="category" :disabled="!category">
          当前分类：{{ category ? interviewCategoryLabel[category] ?? category : '未选择' }}
        </el-radio>
        <el-radio value="current" :disabled="!level && !category">当前筛选</el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="resetDialog = false">取消</el-button>
        <el-button type="danger" :loading="resetting" @click="confirmReset">确认重置</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.filters {
  display: grid;
  gap: 12px;
  margin: 16px 0 20px;
}
.mode-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 14px;
}
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 36px 0;
}
.reset-scope {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  margin-top: 8px;
}
.resume {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 14px;
  border-color: var(--accent);
}
.resume-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}
.resume-actions {
  display: flex;
  gap: 8px;
}
.question.highlight {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-soft);
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
.badge.answered {
  background: #eef1f4;
  color: var(--muted);
}
.badge.answered.good {
  background: #d9f0e3;
  color: #1e7a4f;
}
.badge.answered.bad {
  background: #f7dedb;
  color: #a5342a;
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
  .resume {
    align-items: flex-start;
  }
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
