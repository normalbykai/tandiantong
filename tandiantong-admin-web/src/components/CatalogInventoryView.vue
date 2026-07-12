<template>
  <section class="catalog-page">
    <div class="toolbar">
      <el-segmented v-model="activeStatus" :options="statusOptions" />
      <div class="toolbar-actions">
        <el-select v-model="activeCategory" class="category-select" aria-label="分类筛选">
          <el-option label="全部分类" value="全部分类" />
          <el-option v-for="category in categories" :key="category" :label="category" :value="category" />
        </el-select>
        <el-button type="primary" :icon="Plus">新建商品</el-button>
      </div>
    </div>

    <div class="catalog-grid">
      <section class="panel catalog-list">
        <div class="panel-header">
          <div>
            <h3>商品列表</h3>
            <p>按分类、状态和库存预警维护商品</p>
          </div>
          <el-tag type="warning">库存预警 {{ warningCount }}</el-tag>
        </div>
        <el-table :data="filteredProducts" class="desktop-table">
          <el-table-column label="商品" min-width="220">
            <template #default="{ row }: { row: ProductItem }">
              <div class="product-cell">
                <img :src="row.image" :alt="row.name">
                <div>
                  <strong>{{ row.name }}</strong>
                  <span>{{ row.category }}｜{{ row.addonSummary }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="最低价" width="100">
            <template #default="{ row }: { row: ProductItem }">{{ money(row.basePriceCent) }}</template>
          </el-table-column>
          <el-table-column label="SKU" width="80">
            <template #default="{ row }: { row: ProductItem }">{{ row.skus.length }}</template>
          </el-table-column>
          <el-table-column label="可售库存" width="110">
            <template #default="{ row }: { row: ProductItem }">{{ totalAvailable(row) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }: { row: ProductItem }">
              <el-tag :type="row.status === '已上架' ? 'success' : row.status === '草稿' ? 'info' : 'warning'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="更新" prop="updatedAt" width="130" />
        </el-table>

        <div class="mobile-list">
          <article v-for="product in filteredProducts" :key="product.id" class="mobile-card">
            <div class="product-cell">
              <img :src="product.image" :alt="product.name">
              <div>
                <strong>{{ product.name }}</strong>
                <span>{{ product.category }}｜库存 {{ totalAvailable(product) }}</span>
              </div>
            </div>
            <el-tag>{{ product.status }}</el-tag>
          </article>
        </div>
      </section>

      <section class="panel editor-panel">
        <div class="panel-header">
          <div>
            <h3>商品分步编辑</h3>
            <p>基础信息、规格与 SKU、加料、库存、预览保存</p>
          </div>
        </div>
        <el-steps :active="2" finish-status="success" direction="vertical">
          <el-step title="基础信息" description="桂花拿铁｜咖啡｜商品图已上传" />
          <el-step title="规格与 SKU" description="杯型、温度生成 2 个 SKU，价格均为整数分" />
          <el-step title="加料配置" description="风味分组必选 1 项，最多选择 2 项" />
          <el-step title="库存与上架" description="支付待验证时付费商品仅可保存草稿" />
          <el-step title="预览保存" description="检查 C 端卡片、规格弹层和价格汇总" />
        </el-steps>
      </section>

      <section class="panel wide">
        <div class="panel-header">
          <div>
            <h3>库存流水</h3>
            <p>所有库存变化必须保留业务来源和中文原因</p>
          </div>
          <el-button :icon="SlidersHorizontal">库存调整</el-button>
        </div>
        <el-table :data="records" class="desktop-table">
          <el-table-column prop="time" label="时间" width="160" />
          <el-table-column prop="type" label="类型" width="110" />
          <el-table-column prop="productName" label="商品" width="120" />
          <el-table-column prop="sku" label="SKU" />
          <el-table-column prop="quantity" label="数量" width="90" />
          <el-table-column prop="businessNo" label="业务单号" width="140" />
          <el-table-column prop="reason" label="原因" />
        </el-table>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Plus, SlidersHorizontal } from 'lucide-vue-next'
import type { InventoryRecord, ProductItem } from '../types'

const activeStatus = ref('全部')
const activeCategory = ref('全部分类')
const statusOptions = ['全部', '草稿', '已上架', '已下架']

const products = ref<ProductItem[]>([
  {
    id: 1,
    name: '桂花拿铁',
    category: '咖啡',
    status: '草稿',
    image: 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=240&q=80',
    basePriceCent: 1800,
    addonSummary: '风味加料 3 项',
    updatedAt: '07-12 18:20',
    skus: [
      { id: 11, specification: '中杯 / 热', priceCent: 1800, availableStock: 20, lockedStock: 0, warningStock: 5, code: 'GL-M-HOT' },
      { id: 12, specification: '大杯 / 冰', priceCent: 2200, availableStock: 15, lockedStock: 0, warningStock: 5, code: 'GL-L-ICE' }
    ]
  },
  {
    id: 2,
    name: '手作三明治',
    category: '轻食',
    status: '已上架',
    image: 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?auto=format&fit=crop&w=240&q=80',
    basePriceCent: 2600,
    addonSummary: '无加料',
    updatedAt: '07-12 16:10',
    skus: [
      { id: 21, specification: '默认规格', priceCent: 2600, availableStock: 8, lockedStock: 2, warningStock: 10, code: 'SW-DEFAULT' }
    ]
  }
])

const records = ref<InventoryRecord[]>([
  { id: 1, time: '2026-07-12 18:20', type: '初始库存', productName: '桂花拿铁', sku: '中杯 / 热', quantity: 20, businessNo: 'PRODUCT-1001', reason: '商品初始库存' },
  { id: 2, time: '2026-07-12 18:25', type: '手工入库', productName: '手作三明治', sku: '默认规格', quantity: 6, businessNo: 'MANUAL', reason: '上午补货入库' },
  { id: 3, time: '2026-07-12 18:31', type: '订单锁定', productName: '手作三明治', sku: '默认规格', quantity: 2, businessNo: 'ORDER-0008', reason: '订单锁定库存' }
])

const categories = computed(() => Array.from(new Set(products.value.map(product => product.category))))
const filteredProducts = computed(() => products.value.filter(product => {
  const statusMatched = activeStatus.value === '全部' || product.status === activeStatus.value
  const categoryMatched = activeCategory.value === '全部分类' || product.category === activeCategory.value
  return statusMatched && categoryMatched
}))
const warningCount = computed(() => products.value.filter(product =>
  product.skus.some(sku => sku.availableStock <= sku.warningStock)
).length)

function totalAvailable(product: ProductItem) {
  return product.skus.reduce((sum, sku) => sum + sku.availableStock, 0)
}

function money(value: number) {
  return `¥${(value / 100).toFixed(2)}`
}
</script>
