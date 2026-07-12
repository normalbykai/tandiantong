interface ApiEnvelope<T> { success: boolean; message: string; traceId: string; data: T }
const baseUrl = 'http://localhost:8080'

export async function miniRequest<T>(path: string, method: 'GET' | 'POST' = 'GET', data?: unknown): Promise<T> {
  return await new Promise<T>((resolve, reject) => {
    uni.request<ApiEnvelope<T>>({
      url: `${baseUrl}${path}`, method, data,
      success(response) {
        const envelope = response.data
        if (response.statusCode >= 200 && response.statusCode < 300 && envelope.success) resolve(envelope.data)
        else reject(new Error(`${envelope.message}（追踪号：${envelope.traceId}）`))
      },
      fail(error) { reject(new Error(error.errMsg || '网络请求失败')) }
    })
  })
}
