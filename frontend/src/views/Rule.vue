<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="openEdit()">新增规则</el-button>
    </div>
    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="ruleCode" label="编码" width="100" />
      <el-table-column prop="ruleName" label="名称" />
      <el-table-column prop="ruleType" label="类型" width="130" />
      <el-table-column prop="thresholdValue" label="阈值" width="120" />
      <el-table-column prop="level" label="等级" width="110" />
      <el-table-column prop="enabled" label="启用" width="90">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="200" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑规则' : '新增规则'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="编码"><el-input v-model="form.ruleCode" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.ruleName" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.ruleType">
            <el-option v-for="t in ['OVERDUE','AMOUNT','CONCENTRATION','SCORE']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值"><el-input-number v-model="form.thresholdValue" :min="0" :step="1" /></el-form-item>
        <el-form-item label="等级">
          <el-select v-model="form.level">
            <el-option v-for="l in ['LOW','MEDIUM','HIGH','CRITICAL']" :key="l" :label="l" :value="l" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="说明"><el-input v-model="form.description" type="textarea" /></el-form-item>
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
const visible = ref(false)
const form = reactive({})

async function load() {
  loading.value = true
  try {
    const res = await http.get('/rule/list')
    list.value = res.data
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, row ? { ...row } : { enabled: 1, level: 'MEDIUM', ruleType: 'OVERDUE', thresholdValue: 30 })
  visible.value = true
}

async function save() {
  if (form.id) await http.put('/rule', form)
  else await http.post('/rule', form)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row) {
  await ElMessageBox.confirm(`删除规则 ${row.ruleName}?`)
  await http.delete(`/rule/${row.id}`)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 12px; }
</style>
