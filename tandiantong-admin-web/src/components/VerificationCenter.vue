<template>
  <section class="verification-panel">
    <el-button type="primary" size="large" class="scan-button">扫描顾客二维码</el-button>
    <el-input v-model="keyword" placeholder="取餐号或手机号后四位">
      <template #append>
        <el-button>查询</el-button>
      </template>
    </el-input>
    <article class="verification-card">
      <strong>A023 桂花拿铁×1、三明治×1</strong>
      <span>预计取餐 15:30｜二维码凭证已校验</span>
      <el-button type="primary">查看</el-button>
    </article>
    <article class="verification-card">
      <strong>预约：咖啡体验课</strong>
      <span>今天 16:00 张女士｜预约凭证已校验</span>
      <el-button>查看</el-button>
    </article>
    <article class="verification-card">
      <strong>A018 已核销</strong>
      <span>重复扫码时展示原核销时间和操作人</span>
      <el-tag type="info">已于 15:42 核销</el-tag>
    </article>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const keyword = ref('')
</script>
