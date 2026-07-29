import httpInstance from "@/utils/http";

export const payOrderAPI = (id) => {
  return httpInstance({
    url: `/orders/${id}/pay`,
    method: 'POST'
  })
}

export const getOrderAPI = (id) => {
  return httpInstance({
    url: `/orders/${id}`
  })
}