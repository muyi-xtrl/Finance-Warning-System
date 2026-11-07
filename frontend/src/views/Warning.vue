<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="企业/标题" clearable style="width:200px" />
      <el-select v-model="query.level" clearable placeholder="等级" style="width:140px">
        <el-option v-for="l in ['LOW','MEDIUM','HIGH','CRITICAL']" :key="l" :label="l" :value="l" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态" style="width:140px">
        <el-option v-for="s in ['OPEN','PROCESSING','CLOSED']" :key="s" :label="s" :value="s" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="warningTime" label="时间" width="170" />
      <el-table-column prop="entName" label="企业" min-width="160" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="warningLevel" label="等级" width="110">
        <template #default="{ row }"><el-tag :type="levelType(row.warningLevel)">{{ row.warningLevel }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="riskScore" label="评分" width="90" />
      <el-table-column prop="source" label="来源" width="90" />
      <el-table-column prop="status" label="状态" width="110" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="handle(row, 'PROCESSING')">处理中</el-button>
          <el-button link type="success" @click="handle(row, 'CLOSED')">关闭</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', level: '', status: '' })

function levelType(level) {
  return { CRITICAL: 'danger', HIGH: 'warning', MEDIUM: '', LOW: 'success' }[level] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await http.get('/warning/page', { params: query })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function handle(row, status) {
  const { value } = await ElMessageBox.prompt('处理备注', '处置预警', { inputPlaceholder: '请输入备注' })
  await http.put('/warning/handle', { id: row.id, status, handleRemark: value })
  ElMessage.success('已更新')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
