<template>
  <div>
    <div class="stat-grid">
      <div class="stat-item ok"><div class="label">企业数量</div><div class="value">{{ overview.enterpriseCount || 0 }}</div></div>
      <div class="stat-item"><div class="label">交易笔数</div><div class="value">{{ overview.transactionCount || 0 }}</div></div>
      <div class="stat-item warn"><div class="label">待处理预警</div><div class="value">{{ overview.openWarningCount || 0 }}</div></div>
      <div class="stat-item critical"><div class="label">危急预警</div><div class="value">{{ overview.criticalCount || 0 }}</div></div>
    </div>

    <el-row :gutter="14">
      <el-col :span="8"><div class="page-card"><div ref="levelRef" class="chart"></div></div></el-col>
      <el-col :span="8"><div class="page-card"><div ref="industryRef" class="chart"></div></div></el-col>
      <el-col :span="8"><div class="page-card"><div ref="trendRef" class="chart"></div></div></el-col>
    </el-row>

    <div class="page-card" style="margin-top:14px">
      <h3 style="margin:0 0 12px">高风险企业 TOP</h3>
      <el-table :data="overview.topRisk || []" stripe>
        <el-table-column prop="entCode" label="企业编码" width="120" />
        <el-table-column prop="entName" label="企业名称" />
        <el-table-column prop="riskScore" label="风险分" width="100" />
        <el-table-column prop="riskLevel" label="等级" width="120">
          <template #default="{ row }">
            <el-tag :type="levelType(row.riskLevel)">{{ row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxOverdue" label="最大逾期(天)" width="120" />
        <el-table-column prop="totalBalance" label="剩余本金" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import http from '@/utils/http'

const overview = reactive({})
const levelRef = ref()
const industryRef = ref()
const trendRef = ref()
let charts = []

function levelType(level) {
  return { CRITICAL: 'danger', HIGH: 'warning', MEDIUM: '', LOW: 'success' }[level] || 'info'
}

function render() {
  const levelChart = echarts.init(levelRef.value)
  const industryChart = echarts.init(industryRef.value)
  const trendChart = echarts.init(trendRef.value)
  charts = [levelChart, industryChart, trendChart]

  levelChart.setOption({
    title: { text: '预警等级分布', textStyle: { color: '#f0f4f8', fontSize: 14 } },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['40%', '68%'],
      data: (overview.byLevel || []).map(i => ({ name: i.name, value: i.value })),
      label: { color: '#9fb3c8' }
    }]
  })

  industryChart.setOption({
    title: { text: '行业贷款规模', textStyle: { color: '#f0f4f8', fontSize: 14 } },
    tooltip: {},
    grid: { left: 50, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: (overview.loanByIndustry || []).map(i => i.name), axisLabel: { color: '#9fb3c8', rotate: 30 } },
    yAxis: { type: 'value', axisLabel: { color: '#9fb3c8' }, splitLine: { lineStyle: { color: '#1f3b57' } } },
    series: [{ type: 'bar', data: (overview.loanByIndustry || []).map(i => i.value), itemStyle: { color: '#1f9d8a' } }]
  })

  trendChart.setOption({
    title: { text: '近30日预警趋势', textStyle: { color: '#f0f4f8', fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: (overview.trend || []).map(i => i.name), axisLabel: { color: '#9fb3c8' } },
    yAxis: { type: 'value', axisLabel: { color: '#9fb3c8' }, splitLine: { lineStyle: { color: '#1f3b57' } } },
    series: [{ type: 'line', smooth: true, data: (overview.trend || []).map(i => i.value), areaStyle: { color: 'rgba(240,180,41,0.2)' }, lineStyle: { color: '#f0b429' } }]
  })
}

onMounted(async () => {
  const res = await http.get('/dashboard/overview')
  Object.assign(overview, res.data)
  render()
  window.addEventListener('resize', resize)
})

function resize() { charts.forEach(c => c.resize()) }
onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.chart { height: 280px; }
h3 { color: #f0f4f8; }
</style>
