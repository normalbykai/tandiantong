<template>
  <view class="page">
    <view class="merchant-header">
      <view>
        <text class="merchant-name">春风小铺</text>
        <text class="merchant-meta">今日营业 09:00-20:00｜到店自取</text>
      </view>
      <text class="status">营业中</text>
    </view>

    <image class="banner" src="https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=900&q=80" mode="aspectFill" />

    <scroll-view class="tabs" scroll-x>
      <text v-for="category in categories" :key="category" class="tab" :class="{ active: activeCategory === category }" @tap="activeCategory = category">
        {{ category }}
      </text>
    </scroll-view>

    <view v-if="errorMessage" class="order-panel"><text class="sheet-subtitle">{{ errorMessage }}</text></view>
    <view class="product-grid">
      <view v-for="product in filteredProducts" :key="product.id" class="product-card">
        <image class="product-image" :src="product.image" mode="aspectFill" />
        <text class="product-name">{{ product.name }}</text>
        <text class="product-desc">{{ product.description }}</text>
        <view class="product-footer">
          <text class="price">¥{{ (product.priceCent / 100).toFixed(2) }}</text>
          <button class="pick-button" @tap="selectProduct(product)">选规格</button>
        </view>
      </view>
    </view>

    <view class="sku-sheet">
      <text class="sheet-title">{{ selectedProduct?.name ?? '请选择商品' }}</text>
      <text class="sheet-subtitle">杯型和温度必选，加料最多选择 2 项</text>
      <view class="option-row">
        <text class="option active">中杯</text>
        <text class="option">大杯 +¥4.00</text>
      </view>
      <view class="option-row">
        <text class="option active">热</text>
        <text class="option">冰</text>
      </view>
      <view class="option-row">
        <text class="option active">燕麦奶 +¥3.00</text>
        <text class="option">浓缩咖啡 +¥4.00</text>
      </view>
      <button class="cart-button" :disabled="!selectedProduct" @tap="submitOrder">提交订单并模拟支付</button>
    </view>

    <view class="order-panel">
      <text class="sheet-title">确认订单</text>
      <text class="sheet-subtitle">联系电话 138****8000｜预计 18:30 到店取餐</text>
      <view class="order-line">
        <text>桂花拿铁 中杯/热 ×2</text>
        <text>¥36.00</text>
      </view>
      <view class="order-line">
        <text>燕麦奶 ×2</text>
        <text>¥6.00</text>
      </view>
      <view class="order-total">
        <text>实付</text>
        <text>¥42.00</text>
      </view>
      <button class="cart-button">提交订单并支付</button>
    </view>

    <view class="order-panel">
      <text class="sheet-title">支付成功 · 待取餐</text>
      <text class="pickup-no">{{ orderResult?.pickupNo ?? '待支付' }}</text>
      <text class="sheet-subtitle">请到店后出示核销码，请勿提前将核销码提供给他人。退款仅支持核销前整单退款。</text>
      <button class="cart-button">查看订单详情</button>
    </view>

    <view class="order-panel">
      <text class="sheet-title">{{ services[0]?.name ?? '暂无可预约服务' }}</text>
      <text class="sheet-subtitle">{{ services[0]?.durationMinutes ?? 0 }} 分钟｜剩余 {{ services[0]?.remainingCapacity ?? 0 }} 名</text>
      <view class="option-row">
        <text class="option active">7月13日 周一</text>
        <text class="option">7月14日 周二</text>
      </view>
      <view class="option-row">
        <text class="option active">14:00-15:00 剩余2名</text>
        <text class="option disabled">15:00-16:00 已满</text>
      </view>
      <button class="cart-button" :disabled="services.length === 0" @tap="submitReservation">免费确认预约</button>
    </view>

    <view class="order-panel">
      <text class="sheet-title">预约成功 · 待履约</text>
      <text class="pickup-no">{{ reservationResult?.reservationNo ?? '待预约' }}</text>
      <text class="sheet-subtitle">咖啡体验课｜7月13日 周一 14:00-15:00｜春风小铺｜二维码仅用于到店核销</text>
      <button class="cart-button">取消预约</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { miniRequest } from '../api'

interface MiniProduct {
  id: number
  name: string
  category: string
  description: string
  priceCent: number
  image: string
  hasSku: boolean
  skuId?: number
}

interface MiniService { serviceId: number; name: string; paymentMode: string; priceCent: number; durationMinutes: number; slotId: number; remainingCapacity: number }
interface OrderResult { orderNo: string; payAmountCent: number; pickupNo: string | null; verificationToken: string | null }
interface ReservationResult { reservationNo: string; status: string; voucherCode: string | null }

const activeCategory = ref('推荐')
const categories = ['推荐', '咖啡', '轻食']
const products = ref<MiniProduct[]>([])
const services = ref<MiniService[]>([])
const selectedProduct = ref<MiniProduct | null>(null)
const orderResult = ref<OrderResult | null>(null)
const reservationResult = ref<ReservationResult | null>(null)
const errorMessage = ref('')
const sceneKey = ref('')

const filteredProducts = computed(() => {
  if (activeCategory.value === '推荐') {
    return products.value
  }
  return products.value.filter(product => product.category === activeCategory.value)
})

function selectProduct(product: MiniProduct) { selectedProduct.value = product }
async function loadData() {
  try {
    const launch = uni.getLaunchOptionsSync()
    sceneKey.value = launch.query?.scene || String(uni.getStorageSync('tandiantong_scene') || '')
    if (!sceneKey.value) throw new Error('缺少商户入口码，请扫描商户小程序码进入')
    const remoteProducts = await miniRequest<Array<{ productId: number; productName: string; description: string | null; priceCent: number; categoryName: string; skuId: number; availableStock: number }>>(`/api/mini/v1/catalog/products?scene=${encodeURIComponent(sceneKey.value)}`)
    products.value = remoteProducts.map(item => ({ id: item.productId, name: item.productName, category: item.categoryName, description: item.description || `剩余库存 ${item.availableStock}`, priceCent: item.priceCent, image: 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=360&q=80', hasSku: true, skuId: item.skuId }))
    services.value = await miniRequest<MiniService[]>(`/api/mini/v1/reservations/services?scene=${encodeURIComponent(sceneKey.value)}`)
  } catch (error) { errorMessage.value = error instanceof Error ? error.message : '门店数据加载失败' }
}
async function submitOrder() {
  if (!selectedProduct.value?.skuId) { errorMessage.value = '当前商品缺少可购买 SKU，请联系商户'; return }
  try { orderResult.value = await miniRequest<OrderResult>('/api/mini/v1/orders', 'POST', { sceneKey: sceneKey.value, idempotencyKey: `mini-${Date.now()}`, contactMobile: '13800008000', pickupTimeText: '尽快取餐', lines: [{ skuId: selectedProduct.value.skuId, quantity: 1 }] }) } catch (error) { errorMessage.value = error instanceof Error ? error.message : '订单提交失败' }
}
async function submitReservation() {
  const service = services.value[0]; if (!service) return
  try { reservationResult.value = await miniRequest<ReservationResult>('/api/mini/v1/reservations', 'POST', { sceneKey: sceneKey.value, idempotencyKey: `reservation-${Date.now()}`, serviceId: service.serviceId, slotId: service.slotId, contactMobile: '13800008000' }) } catch (error) { errorMessage.value = error instanceof Error ? error.message : '预约提交失败' }
}
onMounted(loadData)
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24rpx;
}

.merchant-header {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
  align-items: center;
}

.merchant-name,
.sheet-title {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
}

.merchant-meta,
.sheet-subtitle,
.product-desc {
  display: block;
  margin-top: 8rpx;
  color: #7c6758;
  font-size: 24rpx;
}

.status {
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  color: #0f766e;
  background: #dff7f3;
  font-size: 24rpx;
}

.banner {
  width: 100%;
  height: 260rpx;
  margin-top: 24rpx;
  border-radius: 16rpx;
}

.tabs {
  white-space: nowrap;
  margin: 24rpx 0;
}

.tab {
  display: inline-block;
  margin-right: 18rpx;
  padding: 12rpx 24rpx;
  border-radius: 999rpx;
  background: #fff;
  color: #7c6758;
}

.tab.active {
  color: #fff;
  background: #c2410c;
}

.product-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.product-card,
.sku-sheet,
.order-panel {
  padding: 18rpx;
  border-radius: 16rpx;
  background: #fff;
  box-shadow: 0 8rpx 24rpx rgba(124, 69, 35, 0.08);
}

.product-image {
  width: 100%;
  height: 180rpx;
  border-radius: 12rpx;
}

.product-name {
  display: block;
  margin-top: 12rpx;
  font-size: 30rpx;
  font-weight: 700;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 14rpx;
}

.price {
  color: #c2410c;
  font-weight: 700;
}

.pick-button,
.cart-button {
  height: 56rpx;
  line-height: 56rpx;
  border-radius: 999rpx;
  color: #fff;
  background: #c2410c;
  font-size: 24rpx;
}

.sku-sheet {
  margin-top: 28rpx;
}

.order-panel {
  margin-top: 24rpx;
}

.order-line,
.order-total {
  display: flex;
  justify-content: space-between;
  margin-top: 16rpx;
  color: #5b4636;
}

.order-total {
  color: #c2410c;
  font-size: 32rpx;
  font-weight: 700;
}

.pickup-no {
  display: block;
  margin: 20rpx 0;
  color: #c2410c;
  font-size: 72rpx;
  font-weight: 800;
  text-align: center;
}

.option-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 18rpx;
}

.option {
  padding: 12rpx 20rpx;
  border: 2rpx solid #f1dfd2;
  border-radius: 999rpx;
  color: #5b4636;
}

.option.active {
  border-color: #c2410c;
  color: #c2410c;
  background: #fff1e8;
}

.option.disabled {
  color: #9ca3af;
  background: #f3f4f6;
}

.cart-button {
  width: 100%;
  margin-top: 24rpx;
}
</style>
