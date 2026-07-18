export interface ApiResponse<T> {
  success: boolean
  code: string
  message: string
  traceId: string
  data: T
}
