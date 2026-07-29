import request from '../utils/request'

const handleResponse = async (promise) => {
  try {
    const response = await promise
    if (response && typeof response === 'object') {
      if (response.code === 200) {
        return { success: true, data: response.data, message: response.message }
      } else {
        console.error('API Error:', response.message)
        return { success: false, error: response.message, code: response.code }
      }
    }
    console.error('Invalid response format:', response)
    return { success: false, error: 'Invalid response format' }
  } catch (error) {
    console.error('Request failed:', error)
    let errorMessage = 'Network error'
    if (error.response) {
      errorMessage = error.response.data?.message || 'Server error'
    } else if (error.message) {
      errorMessage = error.message
    }
    return { success: false, error: errorMessage }
  }
}

export const mallApi = {
  getProducts: async (params = {}) => {
    const { category = '', page = 0, size = 20 } = params
    return handleResponse(request.get('/c-mall/products', {
      params: { category, page, size }
    }))
  },

  getProductDetail: async (id) => {
    if (!id || isNaN(id)) {
      return { success: false, error: 'Invalid product ID' }
    }
    return handleResponse(request.get(`/c-mall/products/${id}`))
  },

  getProductEvaluations: async (productId) => {
    if (!productId || isNaN(productId)) {
      return { success: false, error: 'Invalid product ID' }
    }
    return handleResponse(request.get(`/c-mall/products/${productId}/evaluations`))
  },

  createOrder: async (orderData) => {
    const { productId, userPhone, quantity, aiDouDeduct } = orderData
    if (!productId || !userPhone || !quantity) {
      return { success: false, error: 'Missing required fields' }
    }
    if (!/^1[3-9]\d{9}$/.test(userPhone)) {
      return { success: false, error: 'Invalid phone number' }
    }
    if (quantity < 1) {
      return { success: false, error: 'Quantity must be at least 1' }
    }
    return handleResponse(request.post('/c-mall/orders', {
      productId,
      userPhone,
      quantity,
      aiDouDeduct: aiDouDeduct || 0
    }))
  },

  payOrder: async (orderId) => {
    if (!orderId || isNaN(orderId)) {
      return { success: false, error: 'Invalid order ID' }
    }
    return handleResponse(request.post(`/c-mall/orders/${orderId}/pay`))
  },

  getUserOrders: async (userPhone) => {
    if (!userPhone || !/^1[3-9]\d{9}$/.test(userPhone)) {
      return { success: false, error: 'Invalid phone number' }
    }
    return handleResponse(request.get('/c-mall/orders', {
      params: { userPhone }
    }))
  },

  getOrderDetail: async (orderId) => {
    if (!orderId || isNaN(orderId)) {
      return { success: false, error: 'Invalid order ID' }
    }
    return handleResponse(request.get(`/c-mall/orders/${orderId}`))
  },

  createEvaluation: async (evalData) => {
    const { orderId, scoreQuality, scoreDelivery, scoreService, content } = evalData
    if (!orderId || !content) {
      return { success: false, error: 'Order ID and content are required' }
    }
    const scores = [scoreQuality, scoreDelivery, scoreService]
    for (const score of scores) {
      if (score < 1 || score > 5) {
        return { success: false, error: 'Scores must be between 1 and 5' }
      }
    }
    if (content.length > 500) {
      return { success: false, error: 'Content cannot exceed 500 characters' }
    }
    return handleResponse(request.post('/c-mall/evaluations', evalData))
  }
}

export const parseProduct = (product) => {
  if (!product) return null
  return {
    ...product,
    price: parseFloat(product.price) || 0,
    marketPrice: parseFloat(product.marketPrice) || 0,
    stock: parseInt(product.stock) || 0,
    salesCount: parseInt(product.salesCount) || 0,
    avgScore: parseFloat(product.avgScore) || 0,
    discount: product.marketPrice && product.price
      ? Math.round((1 - product.price / product.marketPrice) * 10)
      : 0
  }
}

export const parseOrder = (order) => {
  if (!order) return null
  return {
    ...order,
    orderAmount: parseFloat(order.orderAmount) || 0,
    aiDouDeduct: parseFloat(order.aiDouDeduct) || 0,
    payAmount: parseFloat(order.payAmount) || 0,
    quantity: parseInt(order.quantity) || 0
  }
}

export const parseEvaluation = (evaluation) => {
  if (!evaluation) return null
  return {
    ...evaluation,
    scoreQuality: parseInt(evaluation.scoreQuality) || 0,
    scoreDelivery: parseInt(evaluation.scoreDelivery) || 0,
    scoreService: parseInt(evaluation.scoreService) || 0,
    scoreAftersale: parseInt(evaluation.scoreAftersale) || 0,
    scoreValue: parseInt(evaluation.scoreValue) || 0
  }
}