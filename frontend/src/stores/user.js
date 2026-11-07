import { defineStore } from 'pinia'
import { ref } from 'vue'
import http from '@/utils/http'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const realName = ref(localStorage.getItem('realName') || '')
  const role = ref(localStorage.getItem('role') || '')

  async function login(form) {
    const res = await http.post('/auth/login', form)
    token.value = res.data.token
    username.value = res.data.username
    realName.value = res.data.realName
    role.value = res.data.role
    localStorage.setItem('token', token.value)
    localStorage.setItem('username', username.value)
    localStorage.setItem('realName', realName.value)
    localStorage.setItem('role', role.value)
  }

  function logout() {
    token.value = ''
    username.value = ''
    realName.value = ''
    role.value = ''
    localStorage.clear()
  }

  return { token, username, realName, role, login, logout }
})
