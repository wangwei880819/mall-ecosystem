import httpInstance from "@/utils/http";

export function getCategoryAPI(id) {
  return httpInstance({
    url: '/products/categories',
    params: {
        id
    }
  });
}

export const getSubCategoryAPI = (data) => {
  return httpInstance({
    url:'/products/category/' + data.categoryId,
    method:'GET',
    params: {
        page: data.page || 0,
        size: data.pageSize || 20
    }
  })
}