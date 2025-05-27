<template>
  <div class="home">
    <el-container>
      <el-header class="fixed-header">
        <div class="header-left">
          <h1 class="logo">家电商城</h1>
        </div>
        <div class="header-right">
          <div class="nav-links">
            <el-button type="link" class="nav-link" @click="$router.push('/home')">首页</el-button>
            <el-button type="link" class="nav-link" @click="$router.push('/cart')">
              购物车
              <el-badge v-if="cartCount > 0" :value="cartCount" class="cart-badge" type="danger"></el-badge>
            </el-button>
          </div>
          <el-button v-if="!isLoggedIn" @click="$router.push('/login')">登录</el-button>
          <el-button v-if="!isLoggedIn" @click="$router.push('/register')">注册</el-button>
          <template v-else>
            <el-dropdown @command="handleCommand">
              <span class="user-dropdown">
                {{ userStore.userInfo?.username || '用户' }}
                <el-icon><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </div>
      </el-header>
      <div class="header-placeholder"></div>
      <el-main>
        <div class="products-container">
          <h2 class="section-title">热门商品</h2>
          <el-row :gutter="24">
            <el-col :sm="12" :md="8" :lg="6" v-for="product in products" :key="product.id">
              <el-card :body-style="{ padding: '0px' }" class="product-card">
                <div class="product-image-container">
                  <img 
                    :src="getImageUrl(product.imageUrl) || 'https://via.placeholder.com/300'" 
                    class="image" 
                    @click="$router.push(`/product/${product.id}`)" 
                    style="cursor: pointer"
                    @error="handleImageError" 
                  />
                </div>
                <div class="product-info">
                  <h3 class="product-name" @click="$router.push(`/product/${product.id}`)" style="cursor: pointer">{{ product.name }}</h3>
                  <div class="bottom">
                    <span class="price">¥ {{ product.price }}</span>
                    <el-button type="primary" size="small" @click="addToCart(product)">加入购物车</el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../stores/user'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { productApi, cartApi } from '../api'

export default {
  name: 'Home',
  components: {
    ArrowDown
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const { isLoggedIn } = storeToRefs(userStore)
    const products = ref([])
    const cartItems = ref([])
    const cartCount = computed(() => {
      return cartItems.value.reduce((total, item) => total + item.quantity, 0)
    })

    // 获取商品列表
    const fetchProducts = async () => {
      try {
        const data = await productApi.getProductList()
        products.value = data
      } catch (error) {
        console.error('获取商品列表失败:', error)
        ElMessage.error('获取商品列表失败')
      }
    }

    // 获取购物车数量
    const fetchCartItems = async () => {
      if (isLoggedIn.value && userStore.userInfo) {
        try {
          cartItems.value = await cartApi.getCartItems(userStore.userInfo.id)
        } catch (error) {
          console.error('获取购物车失败:', error)
        }
      }
    }

    // 添加到购物车
    const addToCart = async (product) => {
      if (!isLoggedIn.value) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
      }
      try {
        // 设置默认规格：白色和小号
        const defaultAttributes = JSON.stringify({
          颜色: '白色',
          规格: '小'
        })

        // 获取购物车列表
        const cartItemsList = await cartApi.getCartItems(userStore.userInfo.id)

        // 查找购物车中是否已有相同商品（相同商品ID和属性）
        let existingItem = null;

        // 遍历购物车项，检查是否有匹配的商品
        for (const item of cartItemsList) {
          if (item.productId !== product.id) continue;

          try {
            // 解析属性JSON，进行深度比较
            const itemAttrs = JSON.parse(item.selectedAttributes);
            const defaultAttrs = JSON.parse(defaultAttributes);

            // 检查属性值是否相同
            if (itemAttrs.颜色 === defaultAttrs.颜色 &&
                itemAttrs.规格 === defaultAttrs.规格) {
              existingItem = item;
              break;
            }
          } catch (e) {
            console.error('解析属性失败:', e);
            // 如果解析失败，尝试直接比较字符串
            if (item.selectedAttributes === defaultAttributes) {
              existingItem = item;
              break;
            }
          }
        }
        
        if (existingItem) {
          // 如果商品已存在，更新数量
          await cartApi.updateQuantity(existingItem.id, existingItem.quantity + 1, userStore.userInfo.id)
          ElMessage.success('商品数量已更新')
        } else {
          // 如果商品不存在，新增商品
          await cartApi.addToCart({
            userId: userStore.userInfo.id,
            productId: product.id,
            quantity: 1,
            selectedAttributes: defaultAttributes,
            price: product.price,
            productName: product.name,
            imageUrl: product.imageUrl
          })
          ElMessage.success('已添加到购物车')
        }
        // 更新购物车数量
        fetchCartItems()
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

    const handleCommand = (command) => {
      if (command === 'logout') {
        handleLogout()
      } else if (command === 'profile') {
        router.push('/profile')
      } else if (command === 'orders') {
        router.push('/orders')
      }
    }

    const handleLogout = () => {
      userStore.logout()
      router.push('/login')
    }

    // 处理图片加载错误
    const handleImageError = (e) => {
      e.target.src = 'https://via.placeholder.com/200?text=图片加载失败'
    }

    // 处理图片路径
    const getImageUrl = (path) => {
      if (!path) return 'https://via.placeholder.com/200'
      if (path.startsWith('http')) return path
      // 假设后端图片路径以/images开头，需要添加基础URL
      if (path.startsWith('/images')) {
        return `http://localhost:8080${path}`
      }
      return path
    }

    onMounted(() => {
      fetchProducts()
      fetchCartItems()
    })

    return {
      products,
      isLoggedIn,
      userStore,
      cartItems,
      cartCount,
      handleLogout,
      handleCommand,
      addToCart,
      handleImageError,
      getImageUrl
    }
  }
}
</script>

<style scoped>
.home {
  width: 100%;
  min-height: 100vh;
}

.fixed-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
}

.header-placeholder {
  height: 100px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  margin: 0;
  color: #409EFF;
  font-size: 1.5rem;
}

.header-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

.nav-links {
  display: flex;
  margin-right: 20px;
}

.nav-link {
  color: #333;
  font-size: 1rem;
  font-weight: 500;
}

.user-dropdown {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 16px;
  color: #333;
}

.cart-badge {
  margin-left: -5px;
}

.product-card {
  margin-bottom: 25px;
  transition: all 0.3s;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  height: 320px;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.product-card:focus,
.product-card:focus-within {
  outline: none;
  box-shadow: 0 0 0 2px #409EFF, 0 6px 16px rgba(0, 0, 0, 0.12);
  border-radius: 12px;
}

.product-image-container {
  height: 180px;
  overflow: hidden;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.4s;
}

.image:hover {
  transform: scale(1.05);
}

.product-info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 10px 0;
  color: #333;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  height: 36px;
}

.product-name:hover {
  color: #409EFF;
}

.bottom {
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 16px;
  color: #f56c6c;
  font-weight: bold;
}

@media (max-width: 768px) {
  .product-card {
    height: 300px;
  }
  
  .product-image-container {
    height: 160px;
  }
}

.products-container {
  max-width: 1400px;
  margin: 450px auto 20px auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 24px;
  color: #333;
  margin-bottom: 30px;
  position: relative;
  padding-left: 15px;
  border-left: 4px solid #409EFF;
}
</style> 