<template>
  <div class="page-card">
    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="calcBatch" label="批次" min-width="160" />
      <el-table-column prop="entCode" label="编码" width="100" />
      <el-table-column prop="entName" label="企业" />
      <el-table-column prop="totalLoan" label="贷款总额" />
      <el-table-column prop="totalBalance" label="余额" />
      <el-table-column prop="maxOverdue" label="最大逾期" width="100" />
      <el-table-column prop="overdueCount" label="逾期次数" width="100" />
      <el-table-column prop="riskScore" label="风险分" width="90" />
      <el-table-column prop="riskLevel" label="等级" width="110">
        <template #default="{ row }"><el-tag :type="levelType(row.riskLevel)">{{ row.riskLevel }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="calcTime" label="计算时间" width="170" />
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next"
      :total="total" v-model:current-page="page" :page-size="size" @current-change="load" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import http from '@/utils/http'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

function levelType(level) {
  return { CRITICAL: 'danger', HIGH: 'warning', MEDIUM: '', LOW: 'success' }[level] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await http.get('/dashboard/risk/page', { params: { page: page.value, size: size.value } })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
