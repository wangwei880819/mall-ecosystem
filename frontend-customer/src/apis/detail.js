import httpInstance from "@/utils/http";

export const getDetailAPI = (id) => {
  return httpInstance({
    url: `/products/${id}`
  })
}