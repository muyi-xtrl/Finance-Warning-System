<template>
  <div>
    <div class="page-card" style="margin-bottom:14px">
      <h3>集群信息</h3>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="HDFS">{{ cluster.fsDefaultFs }}</el-descriptions-item>
        <el-descriptions-item label="YARN RM">{{ cluster.yarnRm }}</el-descriptions-item>
        <el-descriptions-item label="Mock模式">{{ cluster.mockEnabled ? '开启(本地演示)' : '关闭(真实集群)' }}</el-descriptions-item>
        <el-descriptions-item label="原始目录">{{ cluster.rawPath }}</el-descriptions-item>
        <el-descriptions-item label="结果目录">{{ cluster.resultPath }}</el-descriptions-item>
        <el-descriptions-item label="节点">{{ (cluster.nodes || []).join(', ') }}</el-descriptions-item>
      </el-descriptions>
      <div class="actions">
        <el-upload :http-request="doUpload" :show-file-list="false">
          <el-button type="primary">上传CSV到HDFS</el-button>
        </el-upload>
        <el-button type="warning" :loading="submitting" @click="submitJob">提交风险预警MR作业</el-button>
        <el-button @click="initDirs">初始化HDFS目录</el-button>
        <el-button @click="refresh">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="14">
      <el-col :span="12">
        <div class="page-card">
          <h3>HDFS 文件登记</h3>
          <el-table :data="files" size="small" stripe>
            <el-table-column prop="fileName" label="文件名" />
            <el-table-column prop="hdfsPath" label="路径" min-width="180" />
            <el-table-column prop="fileSize" label="大小" width="90" />
            <el-table-column prop="uploadTime" label="时间" width="160" />
          </el-table>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card">
          <h3>作业日志</h3>
          <el-table :data="logs" size="small" stripe>
            <el-table-column prop="batchNo" label="批次" min-width="140" />
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.status==='SUCCEEDED'?'success':(row.status==='FAILED'?'danger':'warning')">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="message" label="信息" show-overflow-tooltip />
            <el-table-column prop="startTime" label="开始" width="160" />
          </el-table>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '@/utils/http'
import { ElMessage } from 'element-plus'

const cluster = reactive({})
const files = ref([])
const logs = ref([])
const submitting = ref(false)

async function refresh() {
  const [c, f, l] = await Promise.all([
    http.get('/hadoop/cluster'),
    http.get('/hadoop/hdfs/files', { params: { page: 1, size: 20 } }),
    http.get('/hadoop/mr/logs', { params: { page: 1, size: 20 } })
  ])
  Object.assign(cluster, c.data)
  files.value = f.data.records
  logs.value = l.data.records
}

async function doUpload(option) {
  const form = new FormData()
  form.append('file', option.file)
  await http.post('/hadoop/hdfs/upload', form)
  ElMessage.success('上传成功（Mock下仅登记元数据）')
  refresh()
}

async function submitJob() {
  submitting.value = true
  try {
    const res = await http.post('/hadoop/mr/submit', {})
    ElMessage.success(`作业状态: ${res.data.status}`)
    await refresh()
  } finally {
    submitting.value = false
  }
}

async function initDirs() {
  await http.post('/hadoop/hdfs/init')
  ElMessage.success('目录已初始化')
}

onMounted(refresh)
</script>

<style scoped>
h3 { margin: 0 0 12px; font-size: 15px; }
.actions { display: flex; gap: 10px; margin-top: 14px; flex-wrap: wrap; align-items: center; }
</style>
