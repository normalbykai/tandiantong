interface UniRequestSuccess<T> { statusCode: number; data: T }
interface UniRequestFail { errMsg: string }
declare const uni: {
  request<T>(options: { url: string; method: 'GET' | 'POST'; data?: unknown; success: (response: UniRequestSuccess<T>) => void; fail: (error: UniRequestFail) => void }): void
  getLaunchOptionsSync(): { query?: Record<string, string> }
  getStorageSync(key: string): unknown
}
