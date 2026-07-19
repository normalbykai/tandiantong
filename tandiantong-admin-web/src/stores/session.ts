import { computed } from 'vue'
import { defineStore } from 'pinia'
import { clearAccessToken, getAccessToken, setAccessToken } from '../api/http'
import type { AccessDomain, SessionUser } from '../types/auth'

const userKey = 'tandiantong_session_user'
const tokenKey = 'tandiantong_access_token'

/** 根据当前令牌所在的存储位置恢复登录用户，兼容临时会话和七天登录。 */
const sessionAwareStorage: Storage = {
  getItem(key) {
    const sessionValue = sessionStorage.getItem(key)
    if (sessionValue && sessionStorage.getItem(tokenKey)) return sessionValue
    return localStorage.getItem(key) ?? sessionValue
  },
  setItem(key, value) {
    if (sessionStorage.getItem(tokenKey)) {
      sessionStorage.setItem(key, value)
      localStorage.removeItem(key)
      return
    }
    localStorage.setItem(key, value)
    sessionStorage.removeItem(key)
  },
  removeItem(key) {
    sessionStorage.removeItem(key)
    localStorage.removeItem(key)
  },
  clear() {
    sessionStorage.removeItem(userKey)
    localStorage.removeItem(userKey)
  },
  key(index) {
    return localStorage.key(index) ?? sessionStorage.key(index)
  },
  get length() {
    return localStorage.length + sessionStorage.length
  }
}

export const useSession = defineStore('session', {
  state: () => ({
    user: null as SessionUser | null,
    rememberMe: false
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.user && getAccessToken()),
    primaryRoleName: (state) => state.user?.roleNames?.[0] ?? state.user?.roleName ?? ''
    ,hasPermission: (state) => (permissionCode: string) => Boolean(state.user?.permissionCodes?.includes(permissionCode))
  },
  actions: {
    signIn(token: string, domain: AccessDomain, displayName: string, roleName: string, roleNames: string[], permissionCodes: string[], rememberMe: boolean) {
      setAccessToken(token, rememberMe)
      this.rememberMe = rememberMe
      this.user = { domain, displayName, roleName, roleNames: roleNames.length ? roleNames : [roleName], permissionCodes }
    },
    signOut() {
      clearAccessToken()
      this.user = null
      this.rememberMe = false
      sessionStorage.removeItem(userKey)
      localStorage.removeItem(userKey)
    }
  },
  persist: {
    key: userKey,
    storage: sessionAwareStorage,
    pick: ['user', 'rememberMe']
  }
})
