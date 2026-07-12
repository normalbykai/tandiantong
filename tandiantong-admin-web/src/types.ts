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

export interface ProductSku {
  id: number
  specification: string
  priceCent: number
  availableStock: number
  lockedStock: number
  warningStock: number
  code: string
}

export interface InventoryRecord {
  id: number
  time: string
  type: '初始库存' | '手工入库' | '手工出库' | '订单锁定' | '订单释放' | '支付扣减' | '退款回补'
  productName: string
  sku: string
  quantity: number
  businessNo: string
  reason: string
}

export interface ProductItem {
  id: number
  name: string
  category: string
  status: '草稿' | '已上架' | '已下架'
  image: string
  basePriceCent: number
  skus: ProductSku[]
  addonSummary: string
  updatedAt: string
}
