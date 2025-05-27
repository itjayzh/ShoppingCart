import { defineStore } from 'pinia'
import { userApi } from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token'),
    userInfo: null
  }),
  
  actions: {
    async login(username, password) {
      try {
        const response = await userApi.login(username, password)
        if (response && response.token) {
          this.token = response.token
          localStorage.setItem('token', response.token)
          // 获取用户信息
          await this.fetchUserInfo()
          return true
        }
        return false
      } catch (error) {
        console.error('Login failed:', error)
        throw error
      }
    },
    
    async register(username, password) {
      try {
        const response = await userApi.register(username, password)
        return true
      } catch (error) {
        console.error('Registration failed:', error)
        throw error
      }
    },
    
    logout() {
      this.token = null
      this.userInfo = null
      localStorage.removeItem('token')
    },
    
    async fetchUserInfo() {
      try {
        if (this.token) {
          const userInfo = await userApi.getCurrentUser()
          this.userInfo = userInfo
          return userInfo
        }
        return null
      } catch (error) {
        console.error('Failed to fetch user info:', error)
        this.token = null
        this.userInfo = null
        localStorage.removeItem('token')
        return null
      }
    },
    
    async init() {
      if (this.token && !this.userInfo) {
        await this.fetchUserInfo()
      }
    }
  },
  
  getters: {
    isLoggedIn: (state) => !!state.token && !!state.userInfo,
    currentUser: (state) => state.userInfo
  }
}) 