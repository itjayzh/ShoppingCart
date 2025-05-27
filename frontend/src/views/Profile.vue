<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <h2>个人信息</h2>
      </template>
      <div v-if="userInfo" class="profile-content">
        <div class="user-info-item">
          <span class="label">用户名:</span>
          <span class="value">{{ userInfo.username }}</span>
        </div>
        <div class="user-info-item">
          <span class="label">注册时间:</span>
          <span class="value">{{ formatTime(userInfo.createTime) }}</span>
        </div>
      </div>
      <div v-else class="loading-info">
        加载中...
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

export default {
  name: 'Profile',
  setup() {
    const userStore = useUserStore()
    const router = useRouter()
    const userInfo = ref(null)

    onMounted(async () => {
      if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      
      userInfo.value = userStore.userInfo
    })

    const formatTime = (timeStr) => {
      if (!timeStr) return '-'
      try {
        const date = new Date(timeStr)
        return new Intl.DateTimeFormat('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        }).format(date)
      } catch (e) {
        return timeStr
      }
    }

    return {
      userInfo,
      formatTime
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  margin-top: 20px;
}

.profile-card {
  width: 100%;
}

.profile-content {
  padding: 20px 0;
}

.user-info-item {
  margin-bottom: 15px;
  display: flex;
}

.label {
  font-weight: bold;
  width: 100px;
}

.value {
  flex: 1;
  color: #333;
}

.loading-info {
  text-align: center;
  padding: 20px;
  color: #999;
}
</style> 