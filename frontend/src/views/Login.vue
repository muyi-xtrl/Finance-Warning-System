<template>
  <div class="login-page">
    <div class="panel">
      <h1>金融预警大数据管理平台</h1>
      <p>Spring Boot · Vue3 · Hadoop(YARN/HDFS/MR)</p>
      <el-form :model="form" @keyup.enter="onSubmit">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width:100%" :loading="loading" @click="onSubmit">登录</el-button>
      </el-form>
      <div class="tip">默认账号 admin / 123456</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const user = useUserStore()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })

async function onSubmit() {
  loading.value = true
  try {
    await user.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100%;
  display: grid;
  place-items: center;
  background:
    linear-gradient(160deg, rgba(11,31,51,0.92), rgba(16,42,67,0.88)),
    url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><path d="M0 40h80M40 0v80" stroke="%231f9d8a22" stroke-width="1"/></svg>');
}
.panel {
  width: 420px;
  padding: 36px 32px;
  border-radius: 14px;
  background: rgba(16, 42, 67, 0.95);
  border: 1px solid rgba(31, 157, 138, 0.35);
  box-shadow: 0 20px 50px rgba(0,0,0,0.35);
}
h1 { margin: 0 0 8px; font-size: 22px; }
p { margin: 0 0 24px; color: #9fb3c8; font-size: 13px; }
.tip { margin-top: 16px; color: #9fb3c8; font-size: 12px; text-align: center; }
</style>
