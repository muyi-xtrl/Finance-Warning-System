<template>
  <div class="layout">
    <aside class="sider">
      <div class="brand">
        <div class="logo">FW</div>
        <div>
          <div class="title">金融预警平台</div>
          <div class="sub">BigData · Hadoop</div>
        </div>
      </div>
      <el-menu :default-active="route.path" router background-color="transparent" text-color="#9fb3c8" active-text-color="#1f9d8a">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.title }}</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <section class="main">
      <header class="header">
        <div class="crumb">{{ route.meta.title }}</div>
        <div class="user">
          <span>{{ user.realName || user.username }}（{{ user.role }}）</span>
          <el-button link type="primary" @click="onLogout">退出</el-button>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const menus = [
  { path: '/dashboard', title: '风险看板', icon: 'DataAnalysis' },
  { path: '/enterprise', title: '企业管理', icon: 'OfficeBuilding' },
  { path: '/transaction', title: '交易明细', icon: 'List' },
  { path: '/warning', title: '预警中心', icon: 'Bell' },
  { path: '/rule', title: '预警规则', icon: 'Setting' },
  { path: '/risk', title: '风险评分', icon: 'TrendCharts' },
  { path: '/hadoop', title: 'Hadoop作业', icon: 'Cpu' }
]

function onLogout() {
  user.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.layout {
  display: flex;
  height: 100%;
  background:
    radial-gradient(ellipse at top right, rgba(31, 157, 138, 0.18), transparent 45%),
    radial-gradient(ellipse at bottom left, rgba(240, 180, 41, 0.08), transparent 40%),
    #0b1f33;
}
.sider {
  width: 220px;
  padding: 16px 10px;
  border-right: 1px solid rgba(159, 179, 200, 0.15);
  background: rgba(11, 31, 51, 0.85);
}
.brand {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 8px 12px 20px;
}
.logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-weight: 800;
  background: linear-gradient(135deg, #1f9d8a, #0e7c6b);
}
.title { font-weight: 700; }
.sub { font-size: 12px; color: #9fb3c8; }
.main { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid rgba(159, 179, 200, 0.12);
}
.crumb { font-size: 16px; font-weight: 600; }
.user { display: flex; gap: 12px; align-items: center; color: #9fb3c8; }
.content { padding: 16px; overflow: auto; flex: 1; }
:deep(.el-menu) { border-right: none; }
:deep(.el-menu-item.is-active) {
  background: rgba(31, 157, 138, 0.15) !important;
  border-radius: 8px;
}
</style>
