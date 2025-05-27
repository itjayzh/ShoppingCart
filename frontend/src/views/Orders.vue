<template>
  <div class="orders-container">
    <el-card class="orders-card">
      <template #header>
        <div class="header-with-actions">
          <h2>我的订单</h2>
          <el-button type="primary" @click="$router.push('/home')" :icon="Back">返回首页</el-button>
        </div>
      </template>
      
      <el-empty v-if="!orders || orders.length === 0" description="暂无订单记录">
        <el-button type="primary" @click="$router.push('/home')">去购物</el-button>
      </el-empty>
      
      <div v-else class="orders-list">
        <el-card v-for="order in orders" :key="order.id" class="order-card" shadow="hover">
          <div class="order-time">
            <span>下单时间: {{ formatTime(order.createTime) }}</span>
            <el-tag type="success" size="small">已支付</el-tag>
          </div>
          
          <div class="order-items">
            <div v-for="item in order.orderItems" :key="item.id" class="order-item">
              <div class="item-image">
                <img :src="getImageUrl(item.imageUrl)" @error="handleImageError">
              </div>
              
              <div class="item-info">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-attrs" v-if="item.selectedAttributes && item.selectedAttributes !== '{}'">
                  {{ formatAttributesWithLabels(item.selectedAttributes) }}
                </div>
              </div>
              
              <div class="item-price">￥{{ item.price }}</div>
              
              <div class="item-quantity">x {{ item.quantity }}</div>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              共 <span>{{ getTotalQuantity(order) }}</span> 件商品，
              总计: <span class="price">￥{{ order.totalAmount }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { orderApi } from '../api'
import { Delete, Back } from '@element-plus/icons-vue'

export default {
  name: 'Orders',
  setup() {
    const userStore = useUserStore()
    const router = useRouter()
    const orders = ref([])
    
    const fetchOrders = async () => {
      if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      
      try {
        // 使用orderApi获取订单列表
        const data = await orderApi.getOrderList(userStore.userInfo.id)
        orders.value = data
        

      } catch (error) {
        console.error('获取订单列表失败:', error)
        ElMessage.error('获取订单列表失败，请稍后再试')
      }
    }
    

    
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
    
    const getStatusType = (status) => {
      const types = {
        'PENDING': 'warning',
        'PROCESSING': 'primary',
        'PAID': 'success',
        'SHIPPED': 'success',
        'DELIVERED': 'success',
        'CANCELLED': 'danger'
      }
      return types[status] || 'info'
    }
    
    const getStatusText = (status) => {
      const texts = {
        'PENDING': '待付款',
        'PROCESSING': '处理中',
        'PAID': '已支付',
        'SHIPPED': '已发货',
        'DELIVERED': '已送达',
        'CANCELLED': '已取消'
      }
      return texts[status] || status
    }
    
    const getTotalQuantity = (order) => {
      return order.orderItems.reduce((total, item) => total + item.quantity, 0)
    }
    
    const formatAttributes = (attributesJson) => {
      try {
        const attributes = JSON.parse(attributesJson)
        return Object.entries(attributes)
          .map(([key, value]) => `${key}: ${value}`)
          .join(', ')
      } catch (e) {
        return ''
      }
    }
    
    const formatAttributesWithLabels = (attributesJson) => {
      try {
        const attributes = JSON.parse(attributesJson)
        return Object.entries(attributes)
          .map(([key, value]) => {
            if (key.toLowerCase() === 'size') return `规格: ${value}`
            if (key.toLowerCase() === 'color') return `颜色: ${value}`
            return `${key}: ${value}`
          })
          .join(', ')
      } catch (e) {
        return ''
      }
    }
    
    const handleImageError = (e) => {
      e.target.src = 'https://via.placeholder.com/100?text=图片加载失败'
    }
    
    const getImageUrl = (path) => {
      if (!path) return 'https://via.placeholder.com/100'
      if (path.startsWith('http')) return path
      if (path.startsWith('/images')) {
        return `http://localhost:8080${path}`
      }
      return path
    }
    
    onMounted(() => {
      fetchOrders()
    })
    
    return {
      orders,
      formatTime,
      getStatusType,
      getStatusText,
      getTotalQuantity,
      formatAttributes,
      formatAttributesWithLabels,
      fetchOrders,
      handleImageError,
      getImageUrl,
      Delete,
      Back
    }
  }
}

window.scrollTo({
  top: 0,
  behavior: 'auto' // or 'smooth' if你希望平滑滚动
})

</script>

<style scoped>
.orders-container {
  padding: 20px;
  max-width: 1000px;
  margin: 120px auto 20px auto;
  width: 1200px;
  height: 1100px;

}

.orders-card {
  width: 100%;
}

.header-with-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-with-actions h2 {
  margin: 0;
}

.orders-list {
  margin-top: 20px;
}

.order-card {
  margin-bottom: 20px;
}

.order-time {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 15px;
  color: #606266;
  font-size: 14px;
}

.order-items {
  margin-bottom: 15px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}

.order-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 80px;
  height: 80px;
  margin-right: 15px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  padding-right: 15px;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  text-align: left;
}

.item-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.item-attrs {
  color: #666;
  font-size: 13px;
}

.item-price {
  color: #f56c6c;
  font-weight: 600;
  margin-right: 30px;
  min-width: 80px;
  text-align: right;
  flex-shrink: 0;
}

.item-quantity {
  color: #606266;
  margin-right: 10px;
  min-width: 60px;
  text-align: center;
  flex-shrink: 0;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #ebeef5;
  padding-top: 15px;
}

.order-total {
  text-align: right;
  font-size: 14px;
}

.order-total span {
  color: #409EFF;
  font-weight: bold;
}

.order-total .price {
  color: #f56c6c;
  font-size: 18px;
}
</style> 