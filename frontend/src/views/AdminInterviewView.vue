<!-- 面试题维护：管理端列表与新建/编辑弹窗。 -->
<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminCreateInterviewQuestion,
  adminDeleteInterviewQuestion,
  adminListInterviewQuestions,
  adminUpdateInterviewQuestion,
  fetchInterviewMeta,
  getAdminToken,
} from '@/api/client'
import type { InterviewMeta, InterviewQuestion, InterviewQuestionPayload } from '@/api/types'
import { interviewCategoryLabel, interviewLevelLabel } from '@/labels'

const router = useRouter()
const items = ref<InterviewQuestion[]>([])
const meta = ref<InterviewMeta | null>(null)
const loading = ref(false)
const error = ref('')

const filter = reactive({ level: '', category: '', q: '' })

const dialogVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const formError = ref('')

const form = reactive<InterviewQuestionPayload>({
  category: 'java-basics',
  level: 'junior',
  type: 'choice',
  stem: '',
  options: ['', '', '', ''],
  correctIndex: 0,
  referenceAnswer: '',
  explanation: '',
})

const categoryOptions = [
  'java-basics',
  'collections',
  'concurrency',
  'jvm',
  'spring',
  'mysql',
  'redis',
  'mq',
  'distributed',
  'architecture',
  'engineering',
]

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await adminListInterviewQuestions({
      level: filter.level,
      category: filter.category,
      q: filter.q.trim(),
      page: 0,
      size: 100,
    })
    items.value = data.items
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  formError.value = ''
  Object.assign(form, {
    category: filter.category || 'java-basics',
    level: filter.level || 'junior',
    type: 'choice',
    stem: '',
    options: ['', '', '', ''],
    correctIndex: 0,
    referenceAnswer: '',
    explanation: '',
  })
  dialogVisible.value = true
}

function openEdit(row: InterviewQuestion) {
  editingId.value = row.id
  formError.value = ''
  Object.assign(form, {
    category: row.category,
    level: row.level,
    type: row.type,
    stem: row.stem,
    options: row.options.length ? [...row.options] : ['', '', '', ''],
    correctIndex: row.correctIndex ?? 0,
    referenceAnswer: row.referenceAnswer ?? '',
    explanation: row.explanation,
  })
  dialogVisible.value = true
}

function addOption() {
  if (form.options.length < 6) form.options.push('')
}

function removeOption(index: number) {
  if (form.options.length <= 2) return
  form.options.splice(index, 1)
  if ((form.correctIndex ?? 0) >= form.options.length) form.correctIndex = 0
}

function onTypeChange() {
  if (form.type === 'choice') {
    if (form.options.length < 2) form.options = ['', '', '', '']
    if (form.correctIndex === undefined || form.correctIndex >= form.options.length) {
      form.correctIndex = 0
    }
  }
}

async function save() {
  formError.value = ''
  saving.value = true
  const payload: InterviewQuestionPayload = {
    ...form,
    options: form.type === 'choice' ? form.options : [],
    correctIndex: form.type === 'choice' ? form.correctIndex : undefined,
    referenceAnswer: form.type === 'text' ? form.referenceAnswer : undefined,
    stem: form.stem.trim(),
    explanation: form.explanation.trim(),
    category: form.category.trim(),
  }
  try {
    if (editingId.value === null) {
      await adminCreateInterviewQuestion(payload)
    } else {
      await adminUpdateInterviewQuestion(editingId.value, payload)
    }
    ElMessage.success('已保存')
    dialogVisible.value = false
    await load()
    await loadMeta()
  } catch (e) {
    formError.value = (e as Error).message
  } finally {
    saving.value = false
  }
}

async function remove(row: InterviewQuestion) {
  try {
    await ElMessageBox.confirm(`确定删除题目 #${row.id}？`, '删除面试题', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await adminDeleteInterviewQuestion(row.id)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

async function loadMeta() {
  try {
    meta.value = await fetchInterviewMeta()
  } catch {
    meta.value = null
  }
}

onMounted(async () => {
  if (!getAdminToken()) {
    ElMessage.warning('请先在 /admin 输入管理令牌')
    await router.replace('/admin')
    return
  }
  await load()
  await loadMeta()
})
</script>

<template>
  <main class="page">
    <div class="head">
      <div>
        <h1>面试题维护</h1>
        <p class="muted">共 {{ meta?.total ?? items.length }} 道；按等级与分类组织，不绑定学习项。</p>
      </div>
      <div class="head-actions">
        <el-button @click="router.push('/interview')">查看前台</el-button>
        <el-button type="primary" @click="openCreate">新建题目</el-button>
      </div>
    </div>

    <div class="filters card">
      <el-select v-model="filter.level" placeholder="全部等级" clearable>
        <el-option
          v-for="level in ['junior', 'mid', 'senior', 'architect']"
          :key="level"
          :label="interviewLevelLabel[level]"
          :value="level"
        />
      </el-select>
      <el-select v-model="filter.category" placeholder="全部分类" clearable filterable>
        <el-option
          v-for="item in categoryOptions"
          :key="item"
          :label="interviewCategoryLabel[item] ?? item"
          :value="item"
        />
      </el-select>
      <el-input v-model="filter.q" placeholder="搜索题干 / 解析" clearable @keyup.enter="load" />
      <el-button @click="load">筛选</el-button>
    </div>

    <p v-if="error" class="error">{{ error }}</p>

    <el-table v-loading="loading" :data="items" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="等级" width="90">
        <template #default="{ row }">{{ interviewLevelLabel[row.level] ?? row.level }}</template>
      </el-table-column>
      <el-table-column label="分类" width="120">
        <template #default="{ row }">
          {{ interviewCategoryLabel[row.category] ?? row.category }}
        </template>
      </el-table-column>
      <el-table-column label="题型" width="90">
        <template #default="{ row }">{{ row.type === 'choice' ? '单选题' : '自答题' }}</template>
      </el-table-column>
      <el-table-column prop="stem" label="题干" min-width="320" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId === null ? '新建面试题' : `编辑题目 #${editingId}`"
      width="680px"
      top="6vh"
    >
      <el-form label-position="top">
        <div class="row-3">
          <el-form-item label="等级">
            <el-select v-model="form.level">
              <el-option
                v-for="level in ['junior', 'mid', 'senior', 'architect']"
                :key="level"
                :label="interviewLevelLabel[level]"
                :value="level"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="form.category" filterable allow-create default-first-option>
              <el-option
                v-for="item in categoryOptions"
                :key="item"
                :label="interviewCategoryLabel[item] ?? item"
                :value="item"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="题型">
            <el-select v-model="form.type" @change="onTypeChange">
              <el-option label="单选题" value="choice" />
              <el-option label="自答题" value="text" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="题干">
          <el-input v-model="form.stem" type="textarea" :rows="3" />
        </el-form-item>

        <template v-if="form.type === 'choice'">
          <el-form-item label="选项（单选，点左侧圆点标记正确答案）">
            <div v-for="(_, index) in form.options" :key="index" class="option-row">
              <el-radio
                :model-value="form.correctIndex ?? 0"
                :value="index"
                @change="form.correctIndex = index"
              >
                {{ String.fromCharCode(65 + index) }}
              </el-radio>
              <el-input v-model="form.options[index]" :placeholder="`选项 ${String.fromCharCode(65 + index)}`" />
              <el-button text type="danger" :disabled="form.options.length <= 2" @click="removeOption(index)">
                删
              </el-button>
            </div>
            <el-button size="small" :disabled="form.options.length >= 6" @click="addOption">+ 选项</el-button>
          </el-form-item>
        </template>

        <el-form-item v-else label="参考答案">
          <el-input v-model="form.referenceAnswer" type="textarea" :rows="4" />
        </el-form-item>

        <el-form-item label="知识点说明（必填）">
          <el-input v-model="form.explanation" type="textarea" :rows="3" />
        </el-form-item>

        <p v-if="formError" class="error">{{ formError }}</p>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.head-actions {
  display: flex;
  gap: 8px;
}
.filters {
  display: grid;
  grid-template-columns: 140px 180px 1fr auto;
  gap: 10px;
  margin: 16px 0;
}
.row-3 {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 0 12px;
}
.option-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 8px;
  align-items: center;
  width: 100%;
  margin-bottom: 6px;
}
.error {
  color: #c0392b;
}
@media (max-width: 800px) {
  .filters,
  .row-3 {
    grid-template-columns: 1fr;
  }
  .head {
    flex-direction: column;
  }
}
</style>
