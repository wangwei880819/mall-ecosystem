import httpInstance from "@/utils/http";

export function getBannerAPI(params = {}) {
    return httpInstance({
        url: '/products/categories',
        params
    });
}
