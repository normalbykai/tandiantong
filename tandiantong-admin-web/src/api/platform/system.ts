import { request } from '../http'
import type { CreatePlatformDictionaryItemCommand, PlatformDictionaryItem, PlatformSystemConfig, UpdatePlatformDictionaryItemCommand, UpdatePlatformSystemConfigCommand } from '../../types/platform-system'

const basePath = '/api/platform/v1/system'

export const getPlatformSystemConfig = () => request<PlatformSystemConfig>(`${basePath}/config`)
export const updatePlatformSystemConfig = (command: UpdatePlatformSystemConfigCommand) => request<PlatformSystemConfig>(`${basePath}/config`, { method: 'PUT', body: JSON.stringify(command) })
export const listPlatformDictionaryItems = (dictionaryType?: string) => request<PlatformDictionaryItem[]>(`${basePath}/dictionaries${dictionaryType ? `?dictionaryType=${encodeURIComponent(dictionaryType)}` : ''}`)
export const createPlatformDictionaryItem = (command: CreatePlatformDictionaryItemCommand) => request<PlatformDictionaryItem>(`${basePath}/dictionaries`, { method: 'POST', body: JSON.stringify(command) })
export const updatePlatformDictionaryItem = (id: number, command: UpdatePlatformDictionaryItemCommand) => request<void>(`${basePath}/dictionaries/${id}`, { method: 'PUT', body: JSON.stringify(command) })
export const updatePlatformDictionaryItemStatus = (id: number, enabled: boolean) => request<void>(`${basePath}/dictionaries/${id}/status`, { method: 'POST', body: JSON.stringify({ enabled }) })
