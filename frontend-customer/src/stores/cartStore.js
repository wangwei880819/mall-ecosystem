// 封装购物车模块
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useUserStore } from './userStore'
import { insertCartAPI, findNewCartListAPI, delCartAPI, mergeCartAPI } from '@/apis/cart'

export const useCartStore = defineStore('cart', () => {
    const userStore = useUserStore()
    const isLogin = computed(() => !!userStore.userInfo?.token)

    // 获取最新购物车列表action
    const updateNewList = async () => {
        if (isLogin.value && userStore.userInfo?.id) {
            try {
                const res = await findNewCartListAPI({ customerId: userStore.userInfo.id })
                const serverCart = res.result
                if (serverCart && serverCart.length > 0) {
                    // 服务端有数据，用服务端数据并保留本地特殊字段
                    const merged = serverCart.map(item => {
                        const local = cartList.value.find(c => c.skuId === item.skuId)
                        return {
                            ...item,
                            attrsText: local?.attrsText || '默认',
                            rechargePhone: local?.rechargePhone || '',
                            isDigital: local?.isDigital || false,
                            selected: local?.selected !== undefined ? local.selected : (item.selected !== undefined ? item.selected : true)
                        }
                    })
                    cartList.value = merged
                } else {
                    // 服务端购物车为空，直接清空本地
                    cartList.value = []
                }
            } catch (e) {
                // 服务端不可用时保留本地数据
            }
        }
    }
    // 1、定义state - cartlist
    const cartList = ref([])
    // 2、定义action - addCart
    const addCart = async (goods) => {
        const { skuId, count } = goods
        const qty = count || 1
        if (isLogin.value && userStore.userInfo?.id) {
            // 登陆之后的加入购物车逻辑
            await insertCartAPI({ skuId, count: qty, customerId: userStore.userInfo.id })
            await updateNewList()
        }
        else {
            // 未登录：本地添加购物车操作
            const item = cartList.value.find((item) => goods.skuId === item.skuId)
            if (item) {
                item.count += qty
            }
            else {
                cartList.value.push({ ...goods, selected: true })
            }
        }
    }
    // 删除购物车
    const delCart = async (skuId) => {
        if (isLogin.value && userStore.userInfo?.id) {
            try {
                const item = cartList.value.find(i => i.skuId === skuId)
                const cartItemId = item?.id
                await delCartAPI({ ids: cartItemId ? [cartItemId] : [], customerId: userStore.userInfo.id, productId: skuId })
                await updateNewList()
            } catch (e) {
                console.error('删除购物车失败:', e)
                throw e
            }
        }
        else {
            cartList.value = cartList.value.filter(item => item.skuId !== skuId)
        }
    }
    // 清除本地购物车
    const clearCart = async () => {
        cartList.value = []
    }
    //单选功能
    const singleCheck = (skuId, selected) => {
        const item = cartList.value.find(item => item.skuId === skuId)
        if (item) item.selected = selected
    }
    //全选功能
    const allCheck = (selected) => {
        cartList.value.forEach(item => item.selected = selected)
    }
    // 计算价格、数量
    const allCount = computed(() => cartList.value.reduce((a, c) => a + c.count, 0))
    const allPrice = computed(() => cartList.value.reduce((a, c) => a + c.count * c.price, 0))
    const selectedCount = computed(() => cartList.value.filter(item => item.selected).reduce((a, c) => a + c.count, 0))
    const selectedPrice = computed(() => cartList.value.filter(item => item.selected).reduce((a, c) => a + c.count * c.price, 0))
    // 是否全选
    const isAll = computed(() => cartList.value.length > 0 && cartList.value.every(item => item.selected))
    return {
        cartList,
        addCart,
        delCart,
        allCount,
        allPrice,
        selectedCount,
        selectedPrice,
        singleCheck,
        isAll,
        allCheck,
        clearCart,
        updateNewList
    }
}, {
    persist: true
})