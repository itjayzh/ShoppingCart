<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-banner">
        <h1 class="site-title">家电商城</h1>
        <p class="site-slogan">质量保证，价格实惠</p>
      </div>
      <el-card class="register-card" shadow="never">
        <template #header>
          <h2 class="register-title">欢迎注册</h2>
        </template>
        <el-form :model="registerForm" :rules="rules" ref="registerFormRef" class="register-form">
          <el-form-item prop="username">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请输入用户名" 
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码" 
              size="large"
              :prefix-icon="Lock" 
              show-password
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input 
              v-model="registerForm.confirmPassword" 
              type="password" 
              placeholder="请再次输入密码" 
              size="large"
              :prefix-icon="Lock" 
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleRegister" class="register-button">注册</el-button>
          </el-form-item>
          <div class="login-link">
            已有账号？<el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
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
  name: 'Register',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const registerFormRef = ref(null)

    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: ''
    })

    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else {
        if (registerForm.confirmPassword !== '') {
          registerFormRef.value.validateField('confirmPassword')
        }
        callback()
      }
    }

    const validatePass2 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, validator: validatePass, trigger: 'blur' },
        { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, validator: validatePass2, trigger: 'blur' }
      ]
    }

    const handleRegister = async () => {
      if (!registerFormRef.value) return
      
      await registerFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            const success = await userStore.register(registerForm.username, registerForm.password)
            if (success) {
              ElMessage.success('注册成功')
              router.push('/login')
            }
          } catch (error) {
            ElMessage.error(error.response?.data?.error || '注册失败')
          }
        }
      })
    }

    return {
      registerForm,
      registerFormRef,
      rules,
      handleRegister,
      User,
      Lock
    }
  }
}
</script>

<style scoped>
.register-container {
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

.register-box {
  display: flex;
  flex-direction: column;
  width: 420px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.register-banner {
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

.register-card {
  border: none;
  border-radius: 0;
}

.register-title {
  margin: 0;
  color: #333;
  font-size: 20px;
  text-align: center;
}

.register-form {
  padding: 10px 0;
}

.register-button {
  width: 100%;
  height: 45px;
  font-size: 16px;
  background-color: #1e40af;
  border-color: #1e40af;
  border-radius: 8px;
  margin-top: 15px;
}

.register-button:hover,
.register-button:focus {
  background-color: #2563eb;
  border-color: #2563eb;
}

.login-link {
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