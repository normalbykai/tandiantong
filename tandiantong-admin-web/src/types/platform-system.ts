export interface PlatformSystemConfig {
  logoUrl: string
  description: string
  resetPasswordMode: 'RANDOM' | 'FIXED'
  fixedResetPasswordConfigured: boolean
}

export interface UpdatePlatformSystemConfigCommand {
  logoUrl: string
  description: string
  resetPasswordMode: 'RANDOM' | 'FIXED'
  fixedResetPassword?: string
}

export interface PlatformDictionaryItem {
  id: number
  dictionaryType: string
  itemCode: string
  tagType: '' | 'success' | 'warning' | 'info' | 'danger'
  itemLabel: string
  sortOrder: number
  status: 'ENABLED' | 'DISABLED'
}

export interface PlatformDictionaryType {
  dictionaryType: string
  typeLabel: string
  description?: string
  sortOrder: number
}

export interface CreatePlatformDictionaryItemCommand {
  dictionaryType: string
  itemCode: string
  tagType?: '' | 'success' | 'warning' | 'info' | 'danger'
  itemLabel: string
  sortOrder: number
}

export interface UpdatePlatformDictionaryItemCommand {
  tagType?: '' | 'success' | 'warning' | 'info' | 'danger'
  itemLabel: string
  sortOrder: number
}
