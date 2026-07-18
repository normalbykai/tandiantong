import { computed, reactive } from 'vue'
import { clearAccessToken, getAccessToken, isRememberedLogin, setAccessToken } from '../api/http'
import type { AccessDomain, SessionUser } from '../types/auth'

const userKey = 'tandiantong_session_user'
const storedUser = (isRememberedLogin() ? localStorage : sessionStorage).getItem(userKey)
const initialUser = storedUser ? JSON.parse(storedUser) as SessionUser : null
const state = reactive<{ user: SessionUser | null }>({ user: initialUser })

export function useSession() {
  const isLoggedIn = computed(() => Boolean(state.user && getAccessToken()))

  function signIn(token: string, domain: AccessDomain, displayName: string, rememberMe: boolean) {
    setAccessToken(token, rememberMe)
    state.user = { domain, displayName }
    const storage = rememberMe ? localStorage : sessionStorage
    storage.setItem(userKey, JSON.stringify(state.user))
  }

  function signOut() {
    clearAccessToken()
    state.user = null
    sessionStorage.removeItem(userKey)
    localStorage.removeItem(userKey)
  }

  return { state, isLoggedIn, signIn, signOut }
}
