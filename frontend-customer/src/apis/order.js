import httpInstance from "@/utils/http";

export const createOrderAPI = (data) => {
  return httpInstance({
    url: '/orders',
    method: 'POST',
    data
  })
}

export const getOrderListAPI = (params) => {
  return httpInstance({
    url: '/orders',
    params
  })
}

export const cancelOrderAPI = (id) => {
  return httpInstance({
    url: `/orders/${id}/cancel`,
    method: 'PUT'
  })
}