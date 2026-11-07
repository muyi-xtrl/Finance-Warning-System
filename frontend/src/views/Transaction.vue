<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.entCode" placeholder="企业编码" clearable style="width:160px" />
      <el-select v-model="query.txnType" clearable placeholder="交易类型" style="width:140px">
        <el-option v-for="t in ['LOAN','REPAY','OVERDUE','GUARANTEE']" :key="t" :label="t" :value="t" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="txnId" label="流水号" min-width="160" />
      <el-table-column prop="entCode" label="企业" width="100" />
      <el-table-column prop="txnDate" label="日期" width="120" />
      <el-table-column prop="txnType" label="类型" width="110" />
      <el-table-column prop="amount" label="金额" />
      <el-table-column prop="balance" label="余额" />
      <el-table-column prop="overdueDays" label="逾期天数" width="100" />
      <el-table-column prop="riskFlag" label="风险标记" width="100">
        <template #default="{ row }">
          <el-tag :type="row.riskFlag ? 'danger' : 'success'">{{ row.riskFlag ? '可疑' : '正常' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next"
      :total="total" v-model:current-page="query.page" :page-size="query.size" @current-change="load" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '@/utils/http'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, entCode: '', txnType: '' })

async function load() {
  loading.value = true
  try {
    const res = await http.get('/transaction/page', { params: query })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 12px; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
