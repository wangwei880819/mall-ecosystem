import httpInstance from "@/utils/http";

export function addAddressAPI(data) {
    return httpInstance({
        url: '/address',
        method: 'POST',
        data
    })
}

export function createOrderAPI(data) {
    return httpInstance({
        url: '/orders',
        method: 'POST',
        data
    })
}