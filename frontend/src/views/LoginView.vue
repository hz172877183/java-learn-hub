<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.doLogin(form.username.trim(), form.password)
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.push(redirect)
  } catch (e) {
    error.value = (e as Error).message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="page narrow">
    <h1>登录</h1>
    <p class="muted">登录后可记录学习进度和私人笔记。</p>
    <div class="card form">
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="current-password"
            @keyup.enter="submit"
          />
        </el-form-item>
        <p v-if="error" class="error">{{ error }}</p>
        <el-button type="primary" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <p class="muted">
        还没有账号？
        <RouterLink :to="{ path: '/register', query: route.query }">注册</RouterLink>
      </p>
    </div>
  </main>
</template>

<style scoped>
.narrow {
  max-width: 460px;
}
.form {
  margin-top: 16px;
}
.error {
  color: #c0392b;
  margin: 0 0 12px;
}
</style>
