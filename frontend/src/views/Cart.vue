<template>
  <div class="cart-container">
    <el-card class="cart-card">
      <template #header>
        <div class="cart-header">
          <h2>购物车</h2>
          <el-button @click="$router.push('/')">继续购物</el-button>
        </div>
      </template>
      
      <div v-if="cartItems.length === 0" class="empty-cart">
        <el-empty description="购物车是空的" />
        <el-button type="primary" @click="$router.push('/')" style="margin-top: 20px;">去购物</el-button>
      </div>
      
      <template v-else>
        <div class="cart-toolbar">
          <el-checkbox v-model="allSelected" @change="handleSelectAllChange">全选</el-checkbox>
          <el-button type="danger" size="small" :disabled="selectedItems.length === 0" @click="batchDelete">批量删除</el-button>
        </div>
        
        <el-table
          ref="tableRef"
          :data="cartItems"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column label="商品信息">
            <template #default="scope">
              <div style="display: flex; align-items: center;">
                <img 
                  :src="getImageUrl(scope.row.imageUrl)" 
                  style="width: 100px; height: 100px; object-fit: cover; margin-right: 10px;" 
                  @click="$router.push(`/product/${scope.row.productId}`)"
                  class="product-image"
                  @error="handleImageError"
                />
                <div>
                  <div 
                    class="product-name" 
                    @click="$router.push(`/product/${scope.row.productId}`)"
                  >
                    {{ scope.row.productName }}
                  </div>
                  <div class="attributes" v-if="scope.row.selectedAttributes && scope.row.selectedAttributes !== '{}'">
                    {{ formatAttributesWithLabels(scope.row.selectedAttributes) }}
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="单价" width="120">
            <template #default="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column label="数量" width="150">
            <template #default="scope">
              <el-input-number 
                v-model="scope.row.quantity" 
                :min="1" 
                @change="updateQuantity(scope.row)"
              />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120">
            <template #default="scope">
              ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button 
                type="danger" 
                @click="removeItem(scope.row)"
                :icon="Delete"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="cart-footer">
          <div class="cart-actions">
            <el-checkbox v-model="allSelected" @change="handleSelectAllChange">全选</el-checkbox>
            <el-button type="danger" :disabled="selectedItems.length === 0" @click="batchDelete">批量删除</el-button>
            <el-button @click="clearCart">清空购物车</el-button>
          </div>
          <div class="cart-summary">
            <div class="summary-item">
              已选择 <span class="highlight">{{ selectedItems.length }}</span> 件商品
            </div>
            <div class="summary-item">
              总计: <span class="price">¥{{ selectedTotal.toFixed(2) }}</span>
            </div>
            <el-button 
              type="primary" 
              @click="checkout" 
              :disabled="selectedItems.length === 0"
            >
              结算
            </el-button>
          </div>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { cartApi, orderApi } from '../api'
import { Delete } from '@element-plus/icons-vue'

export default {
  name: 'Cart',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const cartItems = ref([])
    const selectedItems = ref([])
    const tableRef = ref(null)
    const allSelected = ref(false)

    // 获取购物车列表
    const fetchCartItems = async () => {
      if (!userStore.userInfo) {
        router.push('/login')
        return
      }
      try {
        const data = await cartApi.getCartItems(userStore.userInfo.id)
        cartItems.value = data
      } catch (error) {
        console.error('获取购物车列表失败:', error)
        ElMessage.error('获取购物车列表失败')
      }
    }

    const total = computed(() => {
      return cartItems.value.reduce((sum, item) => {
        return sum + Number(item.price) * item.quantity
      }, 0)
    })
    
    const selectedTotal = computed(() => {
      return selectedItems.value.reduce((sum, item) => {
        return sum + Number(item.price) * item.quantity
      }, 0)
    })

    const updateQuantity = async (item) => {
      if (!userStore.userInfo) {
        router.push('/login')
        return
      }
      
      try {
        await cartApi.updateQuantity(item.id, item.quantity, userStore.userInfo.id)
        ElMessage.success('数量已更新')
        await fetchCartItems()
      } catch (error) {
        console.error('更新数量失败:', error)
        ElMessage.error('更新数量失败')
      }
    }

    const removeItem = async (item) => {
      if (!userStore.userInfo) {
        router.push('/login')
        return
      }
      
      try {
        await cartApi.deleteByProductId(item.productId, userStore.userInfo.id)
        ElMessage.success('商品已移除')
        await fetchCartItems()
      } catch (error) {
        console.error('删除商品失败:', error)
        ElMessage.error('删除商品失败')
      }
    }

    // 批量删除
    const batchDelete = async () => {
      if (selectedItems.value.length === 0) {
        ElMessage.warning('请至少选择一件商品')
        return
      }
      
      try {
        await ElMessageBox.confirm(`确定要删除选中的${selectedItems.value.length}件商品吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        const deletePromises = selectedItems.value.map(item => {
          return cartApi.deleteCartItem(item.id, userStore.userInfo.id)
        })
        
        await Promise.all(deletePromises)
        ElMessage.success('商品已删除')
        await fetchCartItems()
        selectedItems.value = []
        allSelected.value = false
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除商品失败:', error)
          ElMessage.error('删除商品失败')
        }
      }
    }
    
    const clearCart = async () => {
      if (!userStore.userInfo) {
        router.push('/login')
        return
      }
      
      try {
        await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await cartApi.clearCart(userStore.userInfo.id)
        ElMessage.success('购物车已清空')
      cartItems.value = []
        selectedItems.value = []
        allSelected.value = false
      } catch (error) {
        if (error !== 'cancel') {
          console.error('清空购物车失败:', error)
          ElMessage.error('清空购物车失败')
        }
      }
    }

    const checkout = async () => {
      if (!userStore.userInfo) {
        router.push('/login')
        return
      }
      
      if (selectedItems.value.length === 0) {
        ElMessage.warning('请至少选择一件商品')
        return
      }
      
      try {
        // 确保itemIds是数字格式
        const itemIds = selectedItems.value.map(item => Number(item.id))
        
        await ElMessageBox.confirm(
          `确认结算 ${selectedItems.value.length} 件商品，总计 ¥${selectedTotal.value.toFixed(2)}？`, 
          '确认结算', 
          {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'info'
          }
        )
        
        // 调用创建订单API
        const response = await fetch('/api/order/create', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify({
            itemIds: itemIds,
            userId: userStore.userInfo.id
          })
        })
        
        if (!response.ok) {
          const errorData = await response.text()
          throw new Error(errorData || '结算失败')
        }
        
        const orderData = await response.json()
        
        ElMessage.success('订单已创建，感谢您的购买！')

      } catch (error) {
        if (error !== 'cancel') {
          console.error('结算失败:', error)
          ElMessage.error('结算失败: ' + (error.response?.data || error.message))
        }
      } finally {
        // 刷新购物车列表
        await fetchCartItems()
        selectedItems.value = []
        allSelected.value = false
      }
    }
    
    // 全选/取消全选
    const handleSelectAllChange = (val) => {
      if (tableRef.value) {
        cartItems.value.forEach(row => {
          tableRef.value.toggleRowSelection(row, val)
        })
      }
    }
    
    const handleSelectionChange = (items) => {
      selectedItems.value = items
      allSelected.value = items.length === cartItems.value.length && items.length > 0
    }
    
    const selectAll = () => {
      if (tableRef.value) {
        cartItems.value.forEach(row => {
          tableRef.value.toggleRowSelection(row, true)
        })
      }
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
            return `${key}: ${value}`
          })
          .join(', ')
      } catch (e) {
        return ''
      }
    }

    const handleImageError = () => {
      // 处理图片加载失败后的逻辑
      console.error('图片加载失败')
    }

    const getImageUrl = (path) => {
      if (!path) return 'https://via.placeholder.com/100'
      if (path.startsWith('http')) return path
      // 假设后端图片路径以/images开头，需要添加基础URL
      if (path.startsWith('/images')) {
        return `http://localhost:8080${path}`
      }
      return path
    }

    onMounted(() => {
      if (!userStore.userInfo) {
        router.push('/login')
        return
      }
      fetchCartItems()
    })

    return {
      cartItems,
      selectedItems,
      allSelected,
      total,
      selectedTotal,
      updateQuantity,
      removeItem,
      batchDelete,
      checkout,
      handleSelectionChange,
      handleSelectAllChange,
      selectAll,
      clearCart,
      formatAttributes,
      formatAttributesWithLabels,
      tableRef,
      Delete,
      handleImageError,
      getImageUrl
    }
  }
}
</script>

<style scoped>
.cart-container {
  /*长和宽固定*/
  width: 1200px;
  height: 1100px;
  padding: 20px;
  max-width: 1400px;
  margin: 100px auto 20px auto;
  background-color: #f7f8fa;

  position: relative;

}

.cart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cart-toolbar {
  padding: 10px 0;
  display: flex;
  align-items: center;
  gap: 15px;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-cart {
  padding: 60px 0;
  text-align: center;
}

.cart-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.cart-summary {
  display: flex;
  align-items: center;
  gap: 20px;
}

.summary-item {
  font-size: 16px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 20px;
}

.highlight {
  color: #409EFF;
  font-weight: bold;
}

.product-image {
  cursor: pointer;
  transition: transform 0.3s;
}

.product-image:hover {
  transform: scale(1.05);
}

.product-name {
  font-size: 16px;
  margin-bottom: 8px;
  cursor: pointer;
  color: #333;
}

.product-name:hover {
  color: #409EFF;
}

.attributes {
  font-size: 14px;
  color: #666;
}
</style> 