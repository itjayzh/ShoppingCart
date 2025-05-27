import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // 未授权，清除token并跳转到登录页
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// 用户相关接口
export const userApi = {
  login: (username, password) => {
    return api.post('/user/login', { username, password })
  },
  register: (username, password) => {
    return api.post('/user/register', { username, password })
  },
  getCurrentUser: () => {
    return api.get('/user/current')
  }
}

// 商品相关接口
export const productApi = {
  getProductList: () => {
    return api.get('/product/list')
  },
  getProductById: (id) => {
    return api.get(`/product/${id}`)
  }
}

// 购物车相关接口
export const cartApi = {
  getCartItems: (userId) => {
    return api.get(`/cart/listDetail?userId=${userId}`)
  },
  addToCart: (cartItem) => {
    return api.post('/cart/add', cartItem)
  },
  updateQuantity: (cartItemId, quantity, userId) => {
    return api.put(`/cart/updateQuantity?cartItemId=${cartItemId}&quantity=${quantity}&userId=${userId}`)
  },
  deleteCartItem: (cartItemId, userId) => {
    return api.delete(`/cart/delete?cartItemId=${cartItemId}&userId=${userId}`)
  },
  deleteByProductId: (productId, userId) => {
    return api.delete(`/cart/deleteBatch?productId=${productId}&userId=${userId}`)
  },
  clearCart: (userId) => {
    return api.delete(`/cart/clear?userId=${userId}`)
  },
  checkout: (data) => {
    return api.post('/cart/checkout', data)
  },
  calculateTotal: (data) => {
    return api.post('/cart/calculateTotal', data)
  }
}

// 订单相关接口
export const orderApi = {
  createOrder: (data) => {
    return api.post('/order/create', data)
  },
  createDirectOrder: (data) => {
    return api.post('/order/createDirect', data)
  },
  getOrderList: (userId) => {
    return api.get(`/order/list?userId=${userId}`)
  },
  getOrderDetail: (orderId, userId) => {
    return api.get(`/order/detail?orderId=${orderId}&userId=${userId}`)
  }
}

export default api 