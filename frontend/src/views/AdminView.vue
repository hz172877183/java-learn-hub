<!-- 管理端列表：令牌登录、搜索、删除与跳转编辑。 -->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminDeleteTopic,
  adminListTopics,
  clearAdminToken,
  getAdminToken,
  setAdminToken,
} from '@/api/client'
import type { TopicDocument } from '@/api/types'

const router = useRouter()
const token = ref(getAdminToken())
const authed = ref(false)
const items = ref<TopicDocument[]>([])
const q = ref('')
const loading = ref(false)
const error = ref('')

async function load() {
  loading.value = true
  error.value = ''
  // 先落盘再请求：首次登录时 localStorage 里还没有令牌
  token.value = token.value.trim()
  if (token.value) {
    setAdminToken(token.value)
  }
  try {
    items.value = await adminListTopics(q.value)
    authed.value = true
  } catch (e) {
    // 401 时回到令牌输入卡片，并展示服务端提示
    authed.value = false
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}

async function logout() {
  clearAdminToken()
  authed.value = false
  token.value = ''
  items.value = []
}

async function remove(row: TopicDocument) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」？此操作不可恢复。`, '删除学习项', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await adminDeleteTopic(row.slug)
    ElMessage.success('已删除')
    await load()
  } catch (e) {
    ElMessage.error((e as Error).message)
  }
}

onMounted(() => {
  if (token.value) {
    void load()
  }
})
</script>

<template>
  <main class="page">
    <h1>学习项管理</h1>

    <div v-if="!authed" class="card login">
      <p class="muted">写接口需要管理令牌。本地默认值：<code>dev-admin-token</code></p>
      <el-input
        v-model="token"
        placeholder="Bearer Token"
        show-password
        @keyup.enter="load"
      />
      <el-button type="primary" :loading="loading" @click="load">进入管理</el-button>
      <p v-if="error" class="error">{{ error }}</p>
    </div>

    <template v-else>
      <div class="toolbar">
        <el-input v-model="q" placeholder="搜索 slug / 标题 / 分类" clearable @keyup.enter="load" />
        <el-button @click="load">搜索</el-button>
        <el-button type="primary" @click="router.push('/admin/topics/new')">新建学习项</el-button>
        <el-button @click="router.push('/admin/interview')">面试题维护</el-button>
        <el-button text @click="logout">退出</el-button>
      </div>

      <p class="muted">{{ loading ? '加载中…' : `共 ${items.length} 项（含 draft）` }}</p>
      <p v-if="error" class="error">{{ error }}</p>

      <el-table v-loading="loading" :data="items" border stripe>
        <el-table-column prop="slug" label="slug" width="220" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="category" label="分类" width="140" />
        <el-table-column prop="difficulty" label="难度" width="110" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'published' ? 'success' : 'info'">
              {{ row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="router.push(`/admin/topics/${row.slug}`)">编辑</el-button>
            <el-button
              size="small"
              type="danger"
              plain
              @click="remove(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </main>
</template>

<style scoped>
.login {
  display: grid;
  gap: 12px;
  max-width: 420px;
}
.toolbar {
  display: grid;
  grid-template-columns: 2fr auto auto auto auto;
  gap: 10px;
  margin: 16px 0;
}
.error {
  color: #c0392b;
}
@media (max-width: 800px) {
  .toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
