/** 登录态 Store：token 与用户信息持久化到 localStorage。 */
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { UserInfo } from '@/api/types'
import { getUserToken, login, register, setUserToken } from '@/api/client'

const USER_KEY = 'learnhub-user'

/** 读取本地用户信息；数据损坏时回退为未登录。 */
function readUser(): UserInfo | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? (JSON.parse(raw) as UserInfo) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getUserToken())
  const user = ref<UserInfo | null>(readUser())
  const isLoggedIn = computed(() => Boolean(token.value))

  /** 登录/注册成功后，同时更新内存状态与 localStorage。 */
  function apply(newToken: string, newUser: UserInfo) {
    token.value = newToken
    user.value = newUser
    setUserToken(newToken)
    localStorage.setItem(USER_KEY, JSON.stringify(newUser))
  }

  /** 退出登录：清空 token 与用户缓存。 */
  function logout() {
    token.value = ''
    user.value = null
    setUserToken('')
    localStorage.removeItem(USER_KEY)
  }

  async function doLogin(username: string, password: string) {
    const result = await login({ username, password })
    apply(result.token, result.user)
  }

  async function doRegister(username: string, password: string, displayName?: string) {
    const result = await register({ username, password, displayName })
    apply(result.token, result.user)
  }

  return { token, user, isLoggedIn, logout, doLogin, doRegister }
})
