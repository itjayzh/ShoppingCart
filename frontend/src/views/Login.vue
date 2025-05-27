<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-banner">
        <h1 class="site-title">家电商城</h1>
        <p class="site-slogan">质量保证，价格实惠</p>
      </div>
      <el-card class="login-card" shadow="never">
        <template #header>
          <h2 class="login-title">欢迎登录</h2>
        </template>
        <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名" 
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              size="large"
              :prefix-icon="Lock" 
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleLogin" class="login-button">登录</el-button>
          </el-form-item>
          <div class="register-link">
            还没有账号？<el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const loginFormRef = ref(null)

    const loginForm = reactive({
      username: '',
      password: ''
    })

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
      ]
    }

    const handleLogin = async () => {
      if (!loginFormRef.value) return
      
      await loginFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const success = await userStore.login(loginForm.username, loginForm.password)
            if (success) {
              ElMessage.success('登录成功')
              router.push('/home')
            } else {
              ElMessage.error('登录失败')
            }
          } catch (error) {
            ElMessage.error(error.response?.data?.error || '登录失败')
          }
        }
      })
    }

    return {
      loginForm,
      loginFormRef,
      rules,
      handleLogin,
      User,
      Lock
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  min-width: 100vw;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #e0f2fe, #1e40af);
  margin: 0;
  padding: 0;
}

.login-box {
  display: flex;
  flex-direction: column;
  width: 420px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.login-banner {
  background-color: #1e40af;
  color: white;
  padding: 30px;
  text-align: center;
}

.site-title {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.site-slogan {
  margin: 10px 0 0;
  font-size: 16px;
  opacity: 0.8;
}

.login-card {
  border: none;
  border-radius: 0;
}

.login-title {
  margin: 0;
  color: #333;
  font-size: 20px;
  text-align: center;
}

.login-form {
  padding: 10px 0;
}

.login-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
  background-color: #1e40af;
  border-color: #1e40af;
  border-radius: 8px;
  margin-top: 15px;
}

.login-button:hover,
.login-button:focus {
  background-color: #2563eb;
  border-color: #2563eb;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
}

:deep(.el-input__wrapper) {
  padding: 8px 12px;
  border-radius: 8px;
}

:deep(.el-form-item__error) {
  margin-top: 3px;
}
</style> 