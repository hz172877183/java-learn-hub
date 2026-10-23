<!-- 注册页。 -->
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ username: '', password: '', confirm: '', displayName: '' })
const loading = ref(false)
const error = ref('')

async function submit() {
  error.value = ''
  if (form.password !== form.confirm) {
    error.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  try {
    await auth.doRegister(form.username.trim(), form.password, form.displayName.trim() || undefined)
    ElMessage.success('注册成功，已自动登录')
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
    <h1>注册</h1>
    <p class="muted">当前为本地学习项目，注册信息仅保存在你自己的数据库里。</p>
    <div class="card form">
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="3-32 位字母、数字或下划线" autocomplete="username" />
        </el-form-item>
        <el-form-item label="昵称（可选）">
          <el-input v-model="form.displayName" placeholder="默认与用户名相同" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirm" type="password" show-password autocomplete="new-password" @keyup.enter="submit" />
        </el-form-item>
        <p v-if="error" class="error">{{ error }}</p>
        <el-button type="primary" :loading="loading" @click="submit">注册并登录</el-button>
      </el-form>
      <p class="muted">
        已有账号？
        <RouterLink :to="{ path: '/login', query: route.query }">去登录</RouterLink>
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
