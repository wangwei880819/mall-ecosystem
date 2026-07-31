package com.igou.mall.controller;

import com.igou.mall.dao.BenefitMapper;
import com.igou.mall.dao.ProductMapper;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.model.entity.Benefit;
import com.igou.mall.model.entity.Merchant;
import com.igou.mall.model.entity.Product;
import com.igou.mall.common.Result;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/benefit")
public class BenefitController {

    @Resource
    private BenefitMapper benefitMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private MerchantMapper merchantMapper;

    /**
     * 获取全部权益列表
     */
    @GetMapping
    public Result<List<Benefit>> list() {
        return Result.success(benefitMapper.findAll());
    }

    /**
     * 获取单个权益详情
     */
    @GetMapping("/{id}")
    public Result<Benefit> detail(@PathVariable Long id) {
        Benefit benefit = benefitMapper.findById(id);
        if (benefit == null) return Result.error("权益不存在");
        return Result.success(benefit);
    }

    /**
     * 按商户查询权益
     */
    @GetMapping("/merchant/{merchantId}")
    public Result<List<Benefit>> listByMerchant(@PathVariable Long merchantId) {
        return Result.success(benefitMapper.findByMerchantId(merchantId));
    }

    /**
     * 按状态查询权益（用于审核列表）
     */
    @GetMapping("/status/{status}")
    public Result<List<Benefit>> listByStatus(@PathVariable String status) {
        return Result.success(benefitMapper.findByStatus(status));
    }

    /**
     * 按权益名称查询
     */
    @GetMapping("/by-name/{name}")
    public Result<Benefit> getByName(@PathVariable String name) {
        Benefit benefit = benefitMapper.findByName(name);
        if (benefit == null) return Result.error("权益不存在");
        return Result.success(benefit);
    }

    /**
     * 新增权益（商户入驻端调用）
     */
    @PostMapping
    public Result<Benefit> create(@RequestBody Benefit benefit) {
        benefit.setBenefitCode("BFT" + System.currentTimeMillis());
        if (benefit.getStatus() == null) benefit.setStatus("PENDING");
        if (benefit.getStockUsed() == null) benefit.setStockUsed(0);
        benefitMapper.insert(benefit);

        // 同步创建 product 记录，使权益进入统一商品审核流程
        Product product = new Product();
        product.setProductCode("PRD" + System.currentTimeMillis());
        product.setProductName(benefit.getBenefitName());
        product.setProductType("BENEFIT");
        product.setCategory("权益商品");
        product.setCategoryId(17L); // 权益商品分类
        if (benefit.getMerchantId() != null) {
            product.setMerchantId(benefit.getMerchantId());
            Merchant merchant = merchantMapper.findById(benefit.getMerchantId());
            if (merchant != null) {
                product.setMerchantName(merchant.getMerchantName());
            }
        }
        product.setPrice(benefit.getPrice() != null ? benefit.getPrice() : BigDecimal.ZERO);
        product.setDescription(benefit.getBenefitType());
        product.setDetail(benefit.getUsageRules());
        product.setImageUrls(benefit.getImageUrl());
        if (benefit.getStockTotal() != null) product.setStock(0); // benefit has stockTotal but product uses stock
        product.setStatus("PENDING");
        productMapper.insert(product);

        return Result.success(benefit);
    }

    /**
     * 更新权益
     */
    @PutMapping("/{id}")
    public Result<Benefit> update(@PathVariable Long id, @RequestBody Benefit benefit) {
        Benefit exist = benefitMapper.findById(id);
        if (exist == null) return Result.error("权益不存在");
        benefit.setId(id);
        benefitMapper.update(benefit);
        return Result.success(benefit);
    }

    /**
     * 更新权益状态（上架/下架）
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        benefitMapper.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 审核权益
     */
    @PutMapping("/{id}/audit")
    public Result<?> audit(@PathVariable Long id, @RequestParam String action,
                           @RequestParam(required = false) String reason) {
        String newStatus = "APPROVED".equals(action) ? "ON_SHELF" : "REJECTED";
        benefitMapper.updateStatus(id, newStatus);
        // 同步更新 product 表状态
        Benefit benefit = benefitMapper.findById(id);
        if (benefit != null) {
            List<Product> products = productMapper.findByMerchantId(benefit.getMerchantId());
            for (Product p : products) {
                if ("BENEFIT".equals(p.getProductType()) && benefit.getBenefitName().equals(p.getProductName())) {
                    productMapper.updateStatus(p.getId(), newStatus);
                    break;
                }
            }
        }
        return Result.success("审核完成");
    }

    /**
     * 删除权益（物理删除）
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        benefitMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
