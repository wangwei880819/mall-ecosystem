// 管理用户数据相关
import { defineStore } from 'pinia'
import { loginAPI } from '@/apis/user'
import { ref } from 'vue'
import { useCartStore } from './cartStore'
import { mergeCartAPI } from '@/apis/cart'
export const useUserStore = defineStore('user', () => {
    // 定义管理用户数据的state
    const userInfo = ref({})
    const cartStore = useCartStore()
    // 定义获取接口数据的action函数
    const getUserInfo = async ({ account, password }) => {
        const res = await loginAPI({ account, password })
        // 检查登录返回的code，非200/0表示失败
        if (res.code && res.code !== 200 && res.code !== 0) {
            throw new Error(res.message || '登录失败')
        }
        if (!res.result || !res.result.token) {
            throw new Error('登录失败，请重试')
        }
        userInfo.value = res.result
        // 合并购物车操作
        if (cartStore.cartList.length > 0) {
            await mergeCartAPI(cartStore.cartList.map(item => ({
                skuId: item.skuId,
                selected: item.selected,
                count: item.count,
                customerId: userInfo.value.id
            })))
        }
        cartStore.updateNewList()
    }
    // 退出登录时清除用户信息
    const clearUserInfo = () => {
        userInfo.value = {}
        // 执行清除购物车的action
        cartStore.clearCart()
    }
    // 以对象格式返回state和action函数
    return {
        userInfo,
        getUserInfo,
        clearUserInfo
    }
}, {
    persist: true // 持久化存储
})