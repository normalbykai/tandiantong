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
  itemValue: string
  itemLabel: string
  sortOrder: number
  status: 'ENABLED' | 'DISABLED'
}

export interface CreatePlatformDictionaryItemCommand {
  dictionaryType: string
  itemCode: string
  itemValue: string
  itemLabel: string
  sortOrder: number
}

export interface UpdatePlatformDictionaryItemCommand {
  itemLabel: string
  sortOrder: number
}
