import { computed, reactive } from 'vue'
import { clearAccessToken, getAccessToken, setAccessToken } from '../api/http'
import type { AccessDomain, SessionUser } from '../types/auth'

const userKey = 'tandiantong_session_user'
const storedUser = sessionStorage.getItem(userKey)
const initialUser = storedUser ? JSON.parse(storedUser) as SessionUser : null
const state = reactive<{ user: SessionUser | null }>({ user: initialUser })

export function useSession() {
  const isLoggedIn = computed(() => Boolean(state.user && getAccessToken()))

  function signIn(token: string, domain: AccessDomain, displayName: string) {
    setAccessToken(token)
    state.user = { domain, displayName }
    sessionStorage.setItem(userKey, JSON.stringify(state.user))
  }

  function signOut() {
    clearAccessToken()
    state.user = null
    sessionStorage.removeItem(userKey)
  }

  return { state, isLoggedIn, signIn, signOut }
}
