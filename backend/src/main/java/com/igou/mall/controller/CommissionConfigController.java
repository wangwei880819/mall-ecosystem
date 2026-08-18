package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.CommissionConfigMapper;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.model.entity.CommissionConfig;
import com.igou.mall.model.entity.Merchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/commission")
@CrossOrigin(origins = "*")
public class CommissionConfigController {

    @Autowired
    private CommissionConfigMapper configMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getList() {
        List<CommissionConfig> configs = configMapper.findAllActive();
        List<Map<String, Object>> result = new ArrayList<>();
        for (CommissionConfig c : configs) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", c.getId());
            record.put("merchantId", c.getMerchantId());
            record.put("categoryId", c.getCategoryId());
            record.put("rateType", c.getRateType());
            record.put("commissionRate", c.getCommissionRate());
            record.put("ladderConfig", c.getLadderConfig());
            record.put("effectiveDate", c.getEffectiveDate());
            record.put("expireDate", c.getExpireDate());
            record.put("status", c.getStatus());
            Merchant merchant = merchantMapper.findById(c.getMerchantId());
            record.put("merchantName", merchant != null ? merchant.getMerchantName() : "商户" + c.getMerchantId());
            result.add(record);
        }
        return Result.success(result);
    }

    @GetMapping("/merchant/{merchantId}")
    public Result<List<CommissionConfig>> getByMerchant(@PathVariable Long merchantId) {
        return Result.success(configMapper.findByMerchantId(merchantId));
    }

    @GetMapping("/merchant/{merchantId}/rate")
    public Result<Map<String, Object>> getEffectiveRate(@PathVariable Long merchantId,
                                                         @RequestParam(required = false) Long categoryId) {
        CommissionConfig config;
        if (categoryId != null) {
            config = configMapper.findByMerchantAndCategory(merchantId, categoryId);
        } else {
            config = configMapper.findDefaultByMerchantId(merchantId);
        }
        Map<String, Object> result = new HashMap<>();
        if (config != null) {
            result.put("commissionRate", config.getCommissionRate());
            result.put("rateType", config.getRateType());
            result.put("ladderConfig", config.getLadderConfig());
        } else {
            result.put("commissionRate", new BigDecimal("0.05"));
            result.put("rateType", "FIXED");
        }
        return Result.success(result);
    }

    @PostMapping
    public Result<CommissionConfig> create(@RequestBody CommissionConfig config) {
        config.setStatus("ACTIVE");
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        configMapper.insert(config);
        return Result.success(config);
    }

    @PutMapping("/{id}")
    public Result<CommissionConfig> update(@PathVariable Long id, @RequestBody CommissionConfig config) {
        CommissionConfig existing = configMapper.findById(id);
        if (existing == null) return Result.error("配置不存在");
        existing.setRateType(config.getRateType());
        existing.setCommissionRate(config.getCommissionRate());
        existing.setLadderConfig(config.getLadderConfig());
        existing.setEffectiveDate(config.getEffectiveDate());
        existing.setExpireDate(config.getExpireDate());
        existing.setUpdateTime(LocalDateTime.now());
        configMapper.update(existing);
        return Result.success(existing);
    }

    @PutMapping("/{id}/toggle")
    public Result<CommissionConfig> toggle(@PathVariable Long id, @RequestBody Map<String, String> params) {
        CommissionConfig config = configMapper.findById(id);
        if (config == null) return Result.error("配置不存在");
        config.setStatus(params.get("status"));
        config.setUpdateTime(LocalDateTime.now());
        configMapper.update(config);
        return Result.success(config);
    }

    @PostMapping("/merchant/{merchantId}/replace")
    public Result<Map<String, Object>> replaceConfig(@PathVariable Long merchantId, @RequestBody CommissionConfig config) {
        configMapper.deactivateByMerchantId(merchantId);
        config.setMerchantId(merchantId);
        config.setStatus("ACTIVE");
        config.setCreateTime(LocalDateTime.now());
        config.setUpdateTime(LocalDateTime.now());
        configMapper.insert(config);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "配置已更新");
        result.put("config", config);
        return Result.success(result);
    }
}