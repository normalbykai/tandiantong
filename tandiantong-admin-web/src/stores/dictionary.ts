import { defineStore } from 'pinia'
import { listPlatformDictionaryItems, listPlatformDictionaryTypes } from '../api/platform/system'
import type { PlatformDictionaryItem, PlatformDictionaryType } from '../types/platform-system'

/** 平台字典缓存，按需加载全量字典数据和类型元数据。 */
export const useDictionary = defineStore('dictionary', {
  state: () => ({
    items: [] as PlatformDictionaryItem[],
    types: [] as PlatformDictionaryType[],
    loaded: false,
    loading: false
  }),
  getters: {
    /** 字典类型编码 → 类型元数据的索引。 */
    typeMap: (state) => {
      const map: Record<string, PlatformDictionaryType> = {}
      for (const t of state.types) map[t.dictionaryType] = t
      return map
    },

    /** 按字典类型获取全部字典项。 */
    itemsByType: (state) => {
      const map: Record<string, PlatformDictionaryItem[]> = {}
      for (const item of state.items) {
        (map[item.dictionaryType] ??= []).push(item)
      }
      return map
    }
  },
  actions: {
    /** 确保字典数据已加载（幂等，已加载则跳过）。 */
    async ensureLoaded() {
      if (this.loaded || this.loading) return
      await this._fetch()
    },

    /** 强制重新加载字典数据和类型元数据。 */
    async refresh() {
      await this._fetch()
    },

    async _fetch() {
      this.loading = true
      try {
        const [items, types] = await Promise.all([
          listPlatformDictionaryItems(),
          listPlatformDictionaryTypes()
        ])
        this.items = items
        this.types = types
        this.loaded = true
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取字典类型的中文名称。
     * @param dictionaryType 字典类型编码
     * @returns 中文名称，未配置时返回编码本身
     */
    typeLabel(dictionaryType: string): string {
      return this.typeMap[dictionaryType]?.typeLabel ?? dictionaryType
    },

    /**
     * 获取字典类型的描述说明。
     * @param dictionaryType 字典类型编码
     * @returns 描述文字，未配置时返回空字符串
     */
    typeDescription(dictionaryType: string): string {
      return this.typeMap[dictionaryType]?.description ?? ''
    },

    /**
     * 根据字典类型和业务存储值获取显示标签。
     */
    dictLabel(dictionaryType: string, itemValue: string): string {
      const found = this.items.find(
        item => item.dictionaryType === dictionaryType && item.itemValue === itemValue && item.status === 'ENABLED'
      )
      return found?.itemLabel ?? itemValue
    },

    /**
     * 根据字典类型和业务存储值获取 Element Plus 标签颜色类型。
     */
    dictTagType(dictionaryType: string, itemValue: string): '' | 'success' | 'warning' | 'info' | 'danger' {
      const item = this.items.find(
        i => i.dictionaryType === dictionaryType && i.itemValue === itemValue && i.status === 'ENABLED'
      )
      if (!item) return ''
      const code = item.itemCode
      if (code === 'ENABLED' || code === 'SUCCESS' || code === 'VERIFIED' || code === 'COMPLETED' || code === 'FULFILLED' || code === 'ON_SHELF') return 'success'
      if (code === 'DISABLED') return 'info'
      if (code === 'FAILED' || code === 'VERIFY_FAILED' || code === 'CANCELED') return 'danger'
      if (code === 'PROCESSING' || code === 'REFUNDING' || code === 'PENDING_PAYMENT' || code === 'PENDING_VERIFY' || code === 'PENDING_REVIEW' || code === 'PENDING_ENABLE' || code === 'PENDING') return 'warning'
      return ''
    }
  }
})
