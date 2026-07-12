export type Domain = 'PLATFORM' | 'TENANT'

export interface Merchant {
  id: number
  name: string
  contact: string
  mobile: string
  status: '待启用' | '需修改' | '待复核' | '已启用' | '已停用'
  paymentStatus: '未配置' | '待验证' | '已验证' | '验证失败'
  adminStatus: '待激活' | '已激活'
  products: number
  freeServices: number
  sceneKey: string
}

export interface MerchantDraft {
  subjectName: string
  contact: string
  mobile: string
  storeName: string
  address: string
  adminName: string
  brandColor: string
}
