export interface TenantOverview {
  tenantId: number
  merchantName: string
  adminName: string
  adminMobileMasked: string
  status: 'ENABLED' | 'DISABLED' | 'PENDING_ENABLE' | 'DRAFT' | 'NEEDS_REVISION' | 'PENDING_REVIEW'
  paymentConfigStatus: string
  adminStatus: string
  sceneKey: string
}

export interface CreateTenantCommand {
  merchantName: string
  storeAddress: string
  adminName: string
  adminMobile: string
}

export interface MerchantProvisioning {
  tenantId: number
  storeId: number
  merchantName: string
  storeName: string
  invitationCode: string
  invitationExpiresAt: string
  sceneKey: string
  paymentConfigStatus: string
}
