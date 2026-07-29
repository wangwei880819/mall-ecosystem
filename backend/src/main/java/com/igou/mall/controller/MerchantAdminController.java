package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.dao.ProductMapper;
import com.igou.mall.model.entity.Merchant;
import com.igou.mall.model.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class MerchantAdminController {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ProductMapper productMapper;

    @PostMapping("/merchants")
    public Result<String> createMerchant(@RequestBody Merchant merchant) {
        String merchantCode = "MCH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        merchant.setMerchantCode(merchantCode);
        merchant.setOnboardingStep(1);
        merchant.setOnboardingStatus("PENDING");
        merchant.setRiskLevel("LOW");
        merchantMapper.insert(merchant);
        return Result.success("商户创建成功");
    }

    @PutMapping("/merchants/{id}/step")
    public Result<String> updateOnboardingStep(@PathVariable Long id,
                                               @RequestParam int step,
                                               @RequestParam String status) {
        merchantMapper.updateOnboarding(id, step, status);
        return Result.success("流程状态更新成功");
    }

    @PostMapping("/products")
    public Result<String> createProduct(@RequestBody Product product) {
        String productCode = "PRD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        product.setProductCode(productCode);
        product.setStatus("PENDING");
        product.setSalesCount(0);
        product.setAvgScore(java.math.BigDecimal.ZERO);
        productMapper.insert(product);
        return Result.success("商品创建成功");
    }

    @GetMapping("/merchants/{id}")
    public Result<Merchant> getMerchant(@PathVariable Long id) {
        return Result.success(merchantMapper.findById(id));
    }

    @GetMapping("/merchants")
    public Result<Map<String, Object>> getMerchants(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size,
                                                    @RequestParam(defaultValue = "") String status) {
        List<Merchant> merchants;
        if (status.isEmpty()) {
            merchants = merchantMapper.findAll(page * size, size);
        } else {
            merchants = merchantMapper.findByStatus(status);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", merchants);
        result.put("total", merchantMapper.count());
        return Result.success(result);
    }
}
