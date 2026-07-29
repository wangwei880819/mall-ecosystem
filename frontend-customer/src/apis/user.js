import httpInstance from "@/utils/http";

export const loginAPI = (data) => {
  return httpInstance({
    url: '/auth/login',
    method: 'POST',
    data
  })
}

export const registerAPI = (data) => {
  return httpInstance({
    url: '/auth/register',
    method: 'POST',
    data
  })
}

