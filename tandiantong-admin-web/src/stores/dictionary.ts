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

    /** 按字典类型和字典项编码构建索引。 */
    itemMap: (state) => {
      const map: Record<string, Record<string, PlatformDictionaryItem>> = {}
      for (const item of state.items) {
        (map[item.dictionaryType] ??= {})[item.itemCode] = item
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
    dictLabel(dictionaryType: string, itemCode: string): string {
      const found = this.itemMap[dictionaryType]?.[itemCode]
      return found?.status === 'ENABLED' ? found.itemLabel : itemCode
    },

    /**
     * 根据字典类型和业务存储值获取 Element Plus 标签颜色类型。
     */
    dictTagType(dictionaryType: string, itemCode: string): '' | 'success' | 'warning' | 'info' | 'danger' {
      const item = this.itemMap[dictionaryType]?.[itemCode]
      return item?.status === 'ENABLED' ? item.tagType : ''
    }
  }
})
