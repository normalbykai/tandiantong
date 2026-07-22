import { request } from '../http'
import type { CreatePlatformDictionaryItemCommand, PlatformDictionaryItem, PlatformDictionaryType, PlatformSystemConfig, UpdatePlatformDictionaryItemCommand, UpdatePlatformSystemConfigCommand } from '../../types/platform-system'

const basePath = '/api/platform/v1/system'
const logBasePath = '/api/platform/v1/logs'

export const getPlatformSystemConfig = () => request<PlatformSystemConfig>(`${basePath}/config`)
export const updatePlatformSystemConfig = (command: UpdatePlatformSystemConfigCommand) => request<PlatformSystemConfig>(`${basePath}/config`, { method: 'PUT', body: JSON.stringify(command) })
export const listPlatformDictionaryItems = (dictionaryType?: string) => request<PlatformDictionaryItem[]>(`${basePath}/dictionaries${dictionaryType ? `?dictionaryType=${encodeURIComponent(dictionaryType)}` : ''}`)
export const listPlatformDictionaryTypes = () => request<PlatformDictionaryType[]>(`${basePath}/dictionary-types`)
export const createPlatformDictionaryItem = (command: CreatePlatformDictionaryItemCommand) => request<PlatformDictionaryItem>(`${basePath}/dictionaries`, { method: 'POST', body: JSON.stringify(command) })
export const updatePlatformDictionaryItem = (id: number, command: UpdatePlatformDictionaryItemCommand) => request<void>(`${basePath}/dictionaries/${id}`, { method: 'PUT', body: JSON.stringify(command) })
export const updatePlatformDictionaryItemStatus = (id: number, enabled: boolean) => request<void>(`${basePath}/dictionaries/${id}/status`, { method: 'POST', body: JSON.stringify({ enabled }) })

export interface PlatformOperationLogItem {
  id: number
  operatorId: number
  operatorName?: string
  operatorMobile?: string
  operationType: string
  targetType: string
  targetId?: string
  targetName?: string
  sensitive: boolean
  detail?: string
  traceId?: string
  userIp?: string
  requestMethod?: string
  requestUrl?: string
  userAgent?: string
  createdAt: string
}

export interface PlatformOperationLogPage {
  total: number
  current: number
  pageSize: number
  records: PlatformOperationLogItem[]
}

export interface ListPlatformOperationLogQuery {
  keyword?: string
  operationType?: string
  targetType?: string
  traceId?: string
  startDate?: string
  endDate?: string
  page?: number
  pageSize?: number
}

export const listPlatformOperationLogs = (query: ListPlatformOperationLogQuery = {}) => {
  const params = new URLSearchParams()
  if (query.keyword) params.set('keyword', query.keyword)
  if (query.operationType) params.set('operationType', query.operationType)
  if (query.targetType) params.set('targetType', query.targetType)
  if (query.traceId) params.set('traceId', query.traceId)
  if (query.startDate) params.set('startDate', query.startDate)
  if (query.endDate) params.set('endDate', query.endDate)
  params.set('page', String(query.page ?? 1))
  params.set('pageSize', String(query.pageSize ?? 20))
  return request<PlatformOperationLogPage>(`${logBasePath}?${params.toString()}`)
}
