package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.dao.ProductMapper;
import com.igou.mall.dao.MallOrderMapper;
import com.igou.mall.model.entity.Merchant;
import com.igou.mall.model.entity.MallOrder;
import com.igou.mall.model.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/merchant-portal")
@CrossOrigin(origins = "*")
public class MerchantPortalController {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MallOrderMapper orderMapper;

    /**
     * 商户入驻注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Merchant merchant) {
        // 检查手机号是否已注册
        Merchant existing = merchantMapper.findByContactPhone(merchant.getContactPhone());
        if (existing != null) {
            return Result.error("该手机号已注册，请直接登录");
        }

        merchant.setMerchantCode("M" + System.currentTimeMillis());
        merchant.setOnboardingStatus("PENDING");
        merchant.setOnboardingStep(1);
        merchant.setAuditNode("QUALIFICATION");
        merchant.setStatus("ACTIVE");
        merchantMapper.insert(merchant);

        Map<String, Object> result = new HashMap<>();
        result.put("id", merchant.getId());
        result.put("merchantCode", merchant.getMerchantCode());
        result.put("merchantName", merchant.getMerchantName());
        result.put("contactPhone", merchant.getContactPhone());
        result.put("onboardingStatus", merchant.getOnboardingStatus());
        return Result.success(result);
    }

    /**
     * 商户登录（手机号+密码）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String contactPhone = params.get("contactPhone");
        String password = params.get("password");

        if (contactPhone == null || password == null) {
            return Result.error("手机号和密码不能为空");
        }

        Merchant merchant = merchantMapper.findByContactPhoneAndPassword(contactPhone, password);
        if (merchant == null) {
            // 检查账号是否存在
            Merchant exist = merchantMapper.findByContactPhone(contactPhone);
            if (exist == null) {
                return Result.error("该账号未注册，请先申请入驻");
            } else {
                // 密码不匹配
                return Result.error("密码错误");
            }
        }

        if (!"APPROVED".equals(merchant.getOnboardingStatus())) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("onboardingStatus", merchant.getOnboardingStatus());
            errorResult.put("message", "您的入驻申请正在审核中，请耐心等待");
            return new Result<>(403, "您的入驻申请正在审核中，请耐心等待", errorResult);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", String.valueOf(merchant.getId()));
        result.put("merchant", buildMerchantInfo(merchant));
        return Result.success(result);
    }

    /**
     * 商户仪表盘
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(@RequestParam Long merchantId) {
        Merchant merchant = merchantMapper.findById(merchantId);
        if (merchant == null) {
            return Result.error("商户不存在");
        }

        Map<String, Object> result = new HashMap<>();

        // 商户信息
        result.put("merchantInfo", buildMerchantInfo(merchant));

        // 商品统计
        List<Product> products = productMapper.findByMerchantId(merchantId);
        long totalProducts = products.size();
        long pendingProducts = products.stream().filter(p -> "PENDING".equals(p.getStatus())).count();
        long approvedProducts = products.stream().filter(p -> "ON_SHELF".equals(p.getStatus())).count();
        long rejectedProducts = products.stream().filter(p -> "REJECTED".equals(p.getStatus())).count();
        Map<String, Long> productStats = new HashMap<>();
        productStats.put("total", totalProducts);
        productStats.put("pending", pendingProducts);
        productStats.put("approved", approvedProducts);
        productStats.put("rejected", rejectedProducts);
        result.put("productStats", productStats);

        // 订单统计
        long orderTotal = orderMapper.countByMerchantId(merchantId);
        java.math.BigDecimal totalAmount = orderMapper.sumAmountByMerchantId(merchantId);
        Map<String, Object> orderStats = new HashMap<>();
        orderStats.put("total", orderTotal);
        orderStats.put("totalAmount", totalAmount != null ? totalAmount : java.math.BigDecimal.ZERO);
        result.put("orderStats", orderStats);

        // 最近5条订单
        List<MallOrder> allOrders = orderMapper.findByMerchantId(merchantId);
        List<MallOrder> recentOrders = allOrders.stream().limit(5).collect(Collectors.toList());
        result.put("recentOrders", recentOrders);

        return Result.success(result);
    }

    /**
     * 获取商户商品列表
     */
    @GetMapping("/products")
    public Result<List<Product>> listProducts(@RequestParam Long merchantId) {
        return Result.success(productMapper.findByMerchantId(merchantId));
    }

    /**
     * 商户新增商品
     */
    @PostMapping("/products")
    public Result<Product> addProduct(@RequestBody Product product) {
        product.setProductCode("PRD" + System.currentTimeMillis());
        product.setStatus("PENDING");
        productMapper.insert(product);
        return Result.success(product);
    }

    /**
     * 获取商户订单列表（支持按状态筛选）
     */
    @GetMapping("/orders")
    public Result<List<MallOrder>> listOrders(@RequestParam Long merchantId,
                                               @RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            return Result.success(orderMapper.findByMerchantIdAndStatus(merchantId, status));
        }
        return Result.success(orderMapper.findByMerchantId(merchantId));
    }

    /**
     * 订单发货
     */
    @PutMapping("/orders/{id}/ship")
    public Result<MallOrder> shipOrder(@PathVariable Long id) {
        MallOrder order = orderMapper.findById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        order.setStatus("SHIPPED");
        orderMapper.update(order);
        return Result.success(order);
    }

    private Map<String, Object> buildMerchantInfo(Merchant merchant) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", merchant.getId());
        info.put("merchantCode", merchant.getMerchantCode());
        info.put("merchantName", merchant.getMerchantName());
        info.put("contactName", merchant.getContactName());
        info.put("contactPhone", merchant.getContactPhone());
        info.put("onboardingStatus", merchant.getOnboardingStatus());
        info.put("status", merchant.getStatus());
        return info;
    }
}
