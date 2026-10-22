<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminCreateTopic, adminGetTopic, adminUpdateTopic, getAdminToken } from '@/api/client'
import type {
  Concept,
  PathStep,
  Pitfall,
  RepoPointer,
  Sample,
  TopicDocument,
} from '@/api/types'

type FormDoc = Omit<
  TopicDocument,
  'tags' | 'prerequisites' | 'nextTopics' | 'relatedTopics' | 'concepts' | 'pathSteps' | 'samples' | 'pitfalls' | 'repoPointers'
> & {
  tags: string[]
  prerequisites: string[]
  nextTopics: string[]
  relatedTopics: string[]
  concepts: Concept[]
  pathSteps: PathStep[]
  samples: Sample[]
  pitfalls: Pitfall[]
  repoPointers: RepoPointer[]
}

const route = useRoute()
const router = useRouter()
const slugParam = computed(() => String(route.params.slug ?? ''))
const editing = computed(() => slugParam.value !== '' && slugParam.value !== 'new')

const form = reactive<FormDoc>(blank())
const loading = ref(false)
const saving = ref(false)
const error = ref('')

function blank(): FormDoc {
  return {
    slug: '',
    title: '',
    category: '',
    summary: '',
    difficulty: 'beginner',
    estimatedHours: 4,
    tags: [],
    status: 'draft',
    integrationStatus: 'docs_only',
    officialDocsUrl: '',
    prerequisites: [],
    nextTopics: [],
    relatedTopics: [],
    overviewMd: '',
    concepts: [],
    pathSteps: [],
    samples: [],
    pitfalls: [],
    repoPointers: [],
  }
}

function normalize(doc: TopicDocument): FormDoc {
  return {
    ...blank(),
    ...doc,
    tags: doc.tags ?? [],
    prerequisites: doc.prerequisites ?? [],
    nextTopics: doc.nextTopics ?? [],
    relatedTopics: doc.relatedTopics ?? [],
    concepts: doc.concepts ?? [],
    pathSteps: doc.pathSteps ?? [],
    samples: doc.samples ?? [],
    pitfalls: doc.pitfalls ?? [],
    repoPointers: doc.repoPointers ?? [],
  }
}

onMounted(async () => {
  if (!getAdminToken()) {
    ElMessage.warning('请先在 /admin 输入管理令牌')
    await router.replace('/admin')
    return
  }
  if (!editing.value) {
    return
  }
  loading.value = true
  try {
    Object.assign(form, normalize(await adminGetTopic(slugParam.value)))
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
})

function clean(value?: string) {
  const v = value?.trim()
  return v ? v : undefined
}

function payload(): TopicDocument {
  return {
    slug: form.slug.trim(),
    title: form.title.trim(),
    category: form.category.trim(),
    summary: clean(form.summary),
    difficulty: form.difficulty.trim(),
    estimatedHours: form.estimatedHours ?? 0,
    tags: form.tags.filter(Boolean),
    status: form.status,
    integrationStatus: form.integrationStatus,
    officialDocsUrl: clean(form.officialDocsUrl),
    prerequisites: form.prerequisites.filter(Boolean),
    nextTopics: form.nextTopics.filter(Boolean),
    relatedTopics: form.relatedTopics.filter(Boolean),
    overviewMd: form.overviewMd ?? '',
    concepts: form.concepts.filter((c) => c.term || c.explanation),
    pathSteps: form.pathSteps.filter((s) => s.title || s.goal),
    samples: form.samples.filter((s) => s.title || s.code),
    pitfalls: form.pitfalls.filter((p) => p.title || p.detail),
    repoPointers: form.repoPointers.filter((r) => r.label || r.path),
  }
}

async function save() {
  error.value = ''
  saving.value = true
  try {
    if (editing.value) {
      await adminUpdateTopic(slugParam.value, payload())
    } else {
      await adminCreateTopic(payload())
    }
    ElMessage.success('已保存')
    await router.push('/admin')
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    saving.value = false
  }
}

function addConcept() {
  form.concepts.push({ term: '', explanation: '' })
}
function addStep() {
  form.pathSteps.push({ title: '', goal: '', hours: 1, resources: [] })
}
function addSample() {
  form.samples.push({ title: '', language: 'java', description: '', code: '' })
}
function addPitfall() {
  form.pitfalls.push({ title: '', detail: '' })
}
function addPointer() {
  form.repoPointers.push({ label: '', path: '' })
}
</script>

<template>
  <main class="page">
    <h1>{{ editing ? `编辑：${form.title || form.slug}` : '新建学习项' }}</h1>
    <p class="muted">
      slug 创建后不可修改。状态为「已发布」时，必须填写概述、至少 1 条路线、至少 1 个示例和至少 3 条常见坑。
    </p>

    <el-form v-loading="loading" label-position="top" class="editor">
      <section class="card block">
        <h2>基本信息</h2>
        <div class="two">
          <el-form-item label="slug">
            <el-input v-model="form.slug" :disabled="editing" placeholder="spring-boot-fundamentals" />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="form.title" placeholder="Spring Boot 3 应用结构" />
          </el-form-item>
          <el-form-item label="分类">
            <el-input v-model="form.category" placeholder="spring-core" />
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="form.difficulty" filterable allow-create default-first-option>
              <el-option label="入门" value="beginner" />
              <el-option label="进阶" value="intermediate" />
              <el-option label="实践" value="practical" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status">
              <el-option label="草稿（前台不可见）" value="draft" />
              <el-option label="已发布" value="published" />
            </el-select>
          </el-form-item>
          <el-form-item label="接入状态">
            <el-select v-model="form.integrationStatus">
              <el-option label="仅文档" value="docs_only" />
              <el-option label="有代码片段" value="snippet" />
              <el-option label="可运行时验证" value="runtime" />
            </el-select>
          </el-form-item>
          <el-form-item label="预计小时">
            <el-input-number v-model="form.estimatedHours" :min="0" :step="0.5" />
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="form.tags" multiple filterable allow-create default-first-option />
          </el-form-item>
        </div>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="官方文档">
          <el-input v-model="form.officialDocsUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="概述（Markdown）">
          <el-input v-model="form.overviewMd" type="textarea" :rows="8" class="mono" />
        </el-form-item>
      </section>

      <section class="card block">
        <h2>关联学习项（slug）</h2>
        <el-form-item label="前置">
          <el-select v-model="form.prerequisites" multiple filterable allow-create default-first-option />
        </el-form-item>
        <el-form-item label="下一步">
          <el-select v-model="form.nextTopics" multiple filterable allow-create default-first-option />
        </el-form-item>
        <el-form-item label="相关">
          <el-select v-model="form.relatedTopics" multiple filterable allow-create default-first-option />
        </el-form-item>
      </section>

      <section class="card block">
        <div class="block-head">
          <h2>核心概念</h2>
          <el-button size="small" @click="addConcept">+ 添加</el-button>
        </div>
        <div v-for="(item, i) in form.concepts" :key="`c${i}`" class="row">
          <el-input v-model="item.term" placeholder="术语" class="term" />
          <el-input v-model="item.explanation" type="textarea" :rows="2" placeholder="解释" />
          <el-button text type="danger" @click="form.concepts.splice(i, 1)">删除</el-button>
        </div>
      </section>

      <section class="card block">
        <div class="block-head">
          <h2>学习路线</h2>
          <el-button size="small" @click="addStep">+ 添加</el-button>
        </div>
        <div v-for="(item, i) in form.pathSteps" :key="`s${i}`" class="row">
          <el-input v-model="item.title" placeholder="阶段标题" class="term" />
          <el-input-number v-model="item.hours" :min="0" :step="0.5" />
          <el-input v-model="item.goal" type="textarea" :rows="2" placeholder="目标" />
          <el-select v-model="item.resources" multiple filterable allow-create default-first-option placeholder="资源" />
          <el-button text type="danger" @click="form.pathSteps.splice(i, 1)">删除</el-button>
        </div>
      </section>

      <section class="card block">
        <div class="block-head">
          <h2>示例代码</h2>
          <el-button size="small" @click="addSample">+ 添加</el-button>
        </div>
        <div v-for="(item, i) in form.samples" :key="`m${i}`" class="row">
          <el-input v-model="item.title" placeholder="示例标题" class="term" />
          <el-input v-model="item.language" placeholder="语言" class="lang" />
          <el-input v-model="item.description" type="textarea" :rows="2" placeholder="说明" />
          <el-input v-model="item.code" type="textarea" :rows="6" class="mono" placeholder="代码" />
          <el-button text type="danger" @click="form.samples.splice(i, 1)">删除</el-button>
        </div>
      </section>

      <section class="card block">
        <div class="block-head">
          <h2>常见坑（已发布至少 3 条）</h2>
          <el-button size="small" @click="addPitfall">+ 添加</el-button>
        </div>
        <div v-for="(item, i) in form.pitfalls" :key="`p${i}`" class="row">
          <el-input v-model="item.title" placeholder="坑的标题" class="term" />
          <el-input v-model="item.detail" type="textarea" :rows="2" placeholder="说明" />
          <el-button text type="danger" @click="form.pitfalls.splice(i, 1)">删除</el-button>
        </div>
      </section>

      <section class="card block">
        <div class="block-head">
          <h2>仓库指引</h2>
          <el-button size="small" @click="addPointer">+ 添加</el-button>
        </div>
        <div v-for="(item, i) in form.repoPointers" :key="`r${i}`" class="row">
          <el-input v-model="item.label" placeholder="说明" class="term" />
          <el-input v-model="item.path" placeholder="backend/src/..." />
          <el-button text type="danger" @click="form.repoPointers.splice(i, 1)">删除</el-button>
        </div>
      </section>

      <p v-if="error" class="error">{{ error }}</p>

      <div class="actions">
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button @click="router.push('/admin')">取消</el-button>
      </div>
    </el-form>
  </main>
</template>

<style scoped>
.editor {
  display: grid;
  gap: 16px;
  margin-top: 16px;
}
.block h2 {
  font-size: 16px;
  margin: 0 0 12px;
}
.block-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.block-head h2 {
  margin: 0;
}
.two {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}
.row {
  display: grid;
  gap: 8px;
  padding: 12px 0;
  border-top: 1px dashed var(--line);
}
.term {
  max-width: 320px;
}
.lang {
  max-width: 160px;
}
.mono :deep(textarea) {
  font-family: Consolas, "Courier New", monospace;
  font-size: 13px;
}
.actions {
  display: flex;
  gap: 12px;
  padding-bottom: 40px;
}
.error {
  color: #c0392b;
}
@media (max-width: 800px) {
  .two {
    grid-template-columns: 1fr;
  }
}
</style>
