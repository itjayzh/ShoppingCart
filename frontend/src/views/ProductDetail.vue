<template>
  <div class="product-detail">
    <el-card class="detail-card">
      <template #header>
        <div class="header">
          <el-button @click="$router.back()">返回</el-button>
          <h2>商品详情</h2>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <img 
            :src="getImageUrl(product.imageUrl)" 
            class="product-image"
            @error="handleImageError"
          />
        </el-col>
        <el-col :span="12">
          <div class="product-info">
            <h1>{{ product.name }}</h1>
            <div class="price">¥{{ product.price }}</div>
            
            <!-- 属性选择 -->
            <div class="attributes-selection" v-if="productAttributes && Object.keys(productAttributes).length > 0">
              <div v-for="(values, attrName) in productAttributes" :key="attrName" class="attribute-group">
                <div class="attribute-name">{{ attrName }}:</div>
                <div class="attribute-values">
                  <el-radio-group v-model="selectedAttributes[attrName]">
                    <el-radio 
                      v-for="value in values" 
                      :key="value" 
                      :label="value"
                      border
                    >
                      {{ value }}
                    </el-radio>
                  </el-radio-group>
                </div>
              </div>
            </div>
            
            <div class="actions">
              <el-input-number v-model="quantity" :min="1" :max="99" />
              <el-button type="primary" @click="addToCart">加入购物车</el-button>
              <el-button type="danger" @click="buyNow">立即购买</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 选项卡 -->
      <div class="product-tabs">
        <el-tabs v-model="activeTab" class="demo-tabs">
          <el-tab-pane label="商品详情" name="details">
            <div class="product-description">
              <h3>商品介绍</h3>
              <p>{{ product.description || '暂无详细介绍' }}</p>
              
              <div class="detail-images" v-if="detailImages.length > 0">
                <img 
                  v-for="(image, index) in detailImages" 
                  :key="index" 
                  :src="image" 
                  class="detail-image"
                  @error="handleImageError"
                />
              </div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="规格参数" name="specs">
            <el-table :data="specificationsList" style="width: 100%" v-if="specificationsList.length > 0">
              <el-table-column prop="name" label="参数名称" width="180" />
              <el-table-column prop="value" label="参数值" />
            </el-table>
            <el-empty description="暂无规格参数" v-else />
          </el-tab-pane>
          <el-tab-pane label="用户评价" name="reviews">
            <el-empty description="暂无用户评价" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
    
    <!-- 添加成功弹窗 -->
    <el-dialog
      v-model="addToCartDialogVisible"
      title="已加入购物车"
      width="30%"
      :show-close="false"
    >
      <div class="add-success">
        <el-icon class="success-icon"><CircleCheckFilled /></el-icon>
        <p>商品已成功加入购物车！</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addToCartDialogVisible = false">继续购物</el-button>
          <el-button type="primary" @click="goToCart">
            去购物车结算
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 立即购买确认弹窗 -->
    <el-dialog
      v-model="buyNowDialogVisible"
      title="确认购买"
      width="30%"
      :show-close="false"
    >
      <div class="buy-confirm">
        <p>确认立即购买该商品吗？</p>
        <p class="buy-info">{{ product.name }} x {{ quantity }}</p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="buyNowDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmBuyNow">
            确认购买
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { productApi, cartApi, orderApi } from '../api'

export default {
  name: 'ProductDetail',
  components: {
    CircleCheckFilled
  },
  setup() {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()
    const { isLoggedIn } = storeToRefs(userStore)
    const quantity = ref(1)
    const product = ref({})
    const addToCartDialogVisible = ref(false)
    const activeTab = ref('details')
    const selectedAttributes = reactive({})
    const buyNowDialogVisible = ref(false)
    
    // 模拟的详情图片
    const detailImages = ref([])
    // 模拟的规格参数
    const specificationsList = ref([])
    
    // 处理图片加载错误
    const handleImageError = (e) => {
      e.target.src = 'https://via.placeholder.com/400?text=图片加载失败'
    }
    
    // 处理图片路径
    const getImageUrl = (path) => {
      if (!path) return 'https://via.placeholder.com/400'
      if (path.startsWith('http')) return path
      // 假设后端图片路径以/images开头，需要添加基础URL
      if (path.startsWith('/images')) {
        return `http://localhost:8080${path}`
      }
      return path
    }
    
    // 解析商品属性
    const productAttributes = computed(() => {
      if (!product.value || !product.value.attributes) {
        return {}
      }
      
      try {
        return JSON.parse(product.value.attributes)
      } catch (e) {
        return {}
      }
    })
    
    // 获取所选属性的JSON字符串
    const selectedAttributesJSON = computed(() => {
      return JSON.stringify(selectedAttributes)
    })

    const fetchProductDetail = async () => {
      try {
        const data = await productApi.getProductById(route.params.id)
        product.value = data
        
        // 如果有属性，默认选择第一个值
        if (product.value.attributes) {
          try {
            const attrs = JSON.parse(product.value.attributes)
            Object.keys(attrs).forEach(key => {
              if (attrs[key].length > 0) {
                selectedAttributes[key] = attrs[key][0]
              }
            })
          } catch (e) {
            console.error('解析商品属性失败:', e)
          }
        }
        
        // 模拟加载详情图片和规格参数
        loadMockData()
      } catch (error) {
        console.error('获取商品详情失败:', error)
        ElMessage.error('获取商品详情失败')
      }
    }
    
    // 加载模拟数据
    const loadMockData = () => {
      // 模拟详情图片
      detailImages.value = [
        'https://via.placeholder.com/800x400?text=Detail+Image+1',
        'https://via.placeholder.com/800x400?text=Detail+Image+2'
      ]
      
      // 模拟规格参数
      specificationsList.value = [
        { name: '品牌', value: '示例品牌' },
        { name: '型号', value: product.value.name },
        { name: '材质', value: '优质材料' },
        { name: '重量', value: '500克' }
      ]
    }

    const addToCart = async () => {
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }

      try {
        // 获取购物车列表
        const cartItems = await cartApi.getCartItems(userStore.userInfo.id)

        // 查找购物车中是否已有相同商品（相同商品ID和属性）
        let existingItem = null;

        // 遍历购物车项，检查是否有匹配的商品
        for (const item of cartItems) {
          if (item.productId !== product.value.id) continue;

          try {
            // 解析属性JSON，进行深度比较
            const itemAttrs = JSON.parse(item.selectedAttributes);
            const currentAttrs = JSON.parse(selectedAttributesJSON.value);

            // 检查属性数量是否相同
            const itemKeys = Object.keys(itemAttrs);
            const currentKeys = Object.keys(currentAttrs);

            if (itemKeys.length !== currentKeys.length) continue;

            // 检查每个属性值是否相同
            let allMatch = true;
            for (const key of currentKeys) {
              if (itemAttrs[key] !== currentAttrs[key]) {
                allMatch = false;
                break;
              }
            }

            if (allMatch) {
              existingItem = item;
              break;
            }
          } catch (e) {
            console.error('解析属性失败:', e);
            // 如果解析失败，尝试直接比较字符串
            if (item.selectedAttributes === selectedAttributesJSON.value) {
              existingItem = item;
              break;
            }
          }
        }
        if (existingItem) {
          // 如果商品已存在，更新数量
          await cartApi.updateQuantity(
              existingItem.id,
              existingItem.quantity + quantity.value,
              userStore.userInfo.id
          )
          ElMessage.success('购物车商品数量已更新')
        } else {
          // 如果商品不存在，新增商品
          await cartApi.addToCart({
            userId: userStore.userInfo.id,
            productId: product.value.id,
            quantity: quantity.value,
            selectedAttributes: selectedAttributesJSON.value,
            price: product.value.price,
            productName: product.value.name,
            imageUrl: product.value.imageUrl
          })
          ElMessage.success('已添加到购物车')
        }
        // 显示添加成功弹窗
        addToCartDialogVisible.value = true
      } catch (error) {
        console.error('添加到购物车失败:', error)
        if (!userStore.userInfo) {
          ElMessage.error('请先登录')
          router.push('/login')
        } else {
          ElMessage.error('添加到购物车失败: ' + (error.response?.data || error.message))
        }
      }
    }
    
    const buyNow = () => {
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      
      // 显示确认购买弹窗
      buyNowDialogVisible.value = true
    }
    
    const confirmBuyNow = async () => {
      try {
        // 关闭确认弹窗
        buyNowDialogVisible.value = false
        
        // 弹出结算确认
        await ElMessageBox.confirm(
          `确认支付 ${product.value.name} x ${quantity.value}，总计 ¥${(product.value.price * quantity.value).toFixed(2)}？`, 
          '确认支付', 
          {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'info'
          }
        )
        
        // 使用API直接创建订单
        const orderData = await orderApi.createDirectOrder({
          userId: userStore.userInfo.id,
          productId: product.value.id,
          quantity: quantity.value,
          selectedAttributes: selectedAttributesJSON.value,
          price: product.value.price,
          productName: product.value.name,
          imageUrl: product.value.imageUrl
        });
        
        ElMessage.success('订单已创建，感谢您的购买！')
        
        // 跳转到订单页面
        router.push('/orders')
        
      } catch (error) {
        if (error !== 'cancel') {
          console.error('立即购买失败:', error)
          ElMessage.error('立即购买失败: ' + (error.response?.data || error.message))
        }
      }
    }
    
    const goToCart = () => {
      addToCartDialogVisible.value = false
      router.push('/cart')
    }

    onMounted(() => {
      fetchProductDetail()
    })

    return {
      product,
      quantity,
      addToCart,
      buyNow,
      activeTab,
      productAttributes,
      selectedAttributes,
      selectedAttributesJSON,
      detailImages,
      specificationsList,
      addToCartDialogVisible,
      buyNowDialogVisible,
      confirmBuyNow,
      goToCart,
      handleImageError,
      getImageUrl
    }
  }
}
</script>

<style scoped>
.product-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 1200px;
  height: 1100px;
}

.header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.product-image {
  width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-info {
  padding: 20px;
}

.price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
  margin: 20px 0;
}

.description {
  color: #666;
  line-height: 1.6;
  margin-bottom: 30px;
}

.actions {
  display: flex;
  gap: 20px;
  align-items: center;
  margin-top: 30px;
}

.product-tabs {
  margin-top: 40px;
}

.product-description {
  padding: 20px 0;
}

.detail-images {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-image {
  width: 100%;
  max-width: 800px;
  height: auto;
  border-radius: 8px;
}

.attributes-selection {
  margin: 20px 0;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  background-color: #f9f9f9;
}

.attribute-group {
  margin-bottom: 15px;
}

.attribute-name {
  margin-bottom: 8px;
  font-weight: bold;
}

.attribute-values {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.add-success {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 48px;
  color: #67c23a;
  margin-bottom: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.buy-confirm {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.buy-info {
  margin-top: 10px;
  font-weight: bold;
  color: #409EFF;
}
</style> 