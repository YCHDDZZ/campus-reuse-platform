import { defineStore } from 'pinia'
import http from '../api/http'

const normalizeUser = (data) => ({
  id: data.id,
  username: data.username,
  realName: data.realName,
  email: data.email,
  phone: data.phone,
  avatarUrl: data.avatarUrl,
  role: data.role,
  auditStatus: data.auditStatus,
})

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    user: null,
  }),
  getters: {
    isAdmin: (state) => state.user?.role === 'ADMIN',
    isLogin: (state) => !!state.token,
  },
  actions: {
    restore() {
      this.token = localStorage.getItem('campus-token') || ''
      const user = localStorage.getItem('campus-user')
      this.user = user ? JSON.parse(user) : null
    },
    saveSession(data) {
      this.token = data.token || ''
      this.user = normalizeUser(data)
      localStorage.setItem('campus-token', this.token)
      localStorage.setItem('campus-user', JSON.stringify(this.user))
    },
    setUser(data) {
      this.user = normalizeUser(data)
      localStorage.setItem('campus-user', JSON.stringify(this.user))
    },
    async login(payload) {
      const data = await http.post('/auth/login', payload)
      this.saveSession(data)
    },
    async fetchMe() {
      const data = await http.get('/auth/me')
      this.setUser(data)
    },
    async updateProfile(payload) {
      const data = await http.put('/auth/profile', payload)
      this.setUser(data)
      return data
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('campus-token')
      localStorage.removeItem('campus-user')
    },
  },
})
