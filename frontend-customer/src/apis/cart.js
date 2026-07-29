import httpInstance from "@/utils/http";

export const findNewCartListAPI = (params) => {
  return httpInstance({
    url: '/cart',
    params
  })
}

export const insertCartAPI = (data) => {
  return httpInstance({
    url: '/cart',
    method: 'POST',
    data
  })
}

export const updateCartAPI = (id, data) => {
  return httpInstance({
    url: `/cart/${id}`,
    method: 'PUT',
    data
  })
}

export const delCartAPI = (data) => {
  return httpInstance({
    url: '/cart',
    method: 'DELETE',
    data
  })
}

export const mergeCartAPI = (data) => {
  return httpInstance({
    url: '/cart/merge',
    method: 'POST',
    data
  })
}