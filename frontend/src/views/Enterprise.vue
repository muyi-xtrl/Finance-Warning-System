<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="企业名称/编码" clearable style="width:220px" @keyup.enter="load" />
      <el-input v-model="query.industry" placeholder="行业" clearable style="width:160px" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="openEdit()">新增企业</el-button>
    </div>
    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="entCode" label="编码" width="110" />
      <el-table-column prop="entName" label="名称" />
      <el-table-column prop="industry" label="行业" width="120" />
      <el-table-column prop="region" label="地区" width="100" />
      <el-table-column prop="creditLevel" label="信用" width="80" />
      <el-table-column prop="registeredCap" label="注册资本(万)" width="130" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination class="pager" background layout="total, prev, pager, next"
      :total="total" v-model:current-page="query.page" :page-size="query.size" @current-change="load" />

    <el-dialog v-model="visible" :title="form.id ? '编辑企业' : '新增企业'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="企业编码"><el-input v-model="form.entCode" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="企业名称"><el-input v-model="form.entName" /></el-form-item>
        <el-form-item label="行业"><el-input v-model="form.industry" /></el-form-item>
        <el-form-item label="地区"><el-input v-model="form.region" /></el-form-item>
        <el-form-item label="信用等级">
          <el-select v-model="form.creditLevel"><el-option v-for="l in ['A','B','C','D']" :key="l" :label="l" :value="l" /></el-select>
        </el-form-item>
        <el-form-item label="注册资本"><el-input-number v-model="form.registeredCap" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '@/utils/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const visible = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '', industry: '' })
const form = reactive({})

async function load() {
  loading.value = true
  try {
    const res = await http.get('/enterprise/page', { params: query })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, row ? { ...row } : { creditLevel: 'B', status: 1, registeredCap: 0 })
  visible.value = true
}

async function save() {
  if (form.id) await http.put('/enterprise', form)
  else await http.post('/enterprise', form)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除 ${row.entName}?`, '提示')
  await http.delete(`/enterprise/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; gap: 10px; margin-bottom: 12px; flex-wrap: wrap; }
.pager { margin-top: 14px; justify-content: flex-end; }
</style>
