package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.SettlementMapper;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.model.entity.Settlement;
import com.igou.mall.model.entity.Merchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/settlement")
@CrossOrigin(origins = "*")
public class SettlementAdminController {

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    private static final Map<Long, Map<String, Object>> SETTLE_RULES = new HashMap<>();

    static {
        Map<String, Object> rule1 = new HashMap<>();
        rule1.put("merchantId", 1L);
        rule1.put("merchantName", "瑞幸咖啡（中国）有限公司");
        rule1.put("commissionRate", new BigDecimal("0.05"));
        rule1.put("settlePeriod", "MONTHLY");
        rule1.put("minSettleAmount", new BigDecimal("100"));
        SETTLE_RULES.put(1L, rule1);

        Map<String, Object> rule2 = new HashMap<>();
        rule2.put("merchantId", 2L);
        rule2.put("merchantName", "腾讯音乐");
        rule2.put("commissionRate", new BigDecimal("0.08"));
        rule2.put("settlePeriod", "WEEKLY");
        rule2.put("minSettleAmount", new BigDecimal("50"));
        SETTLE_RULES.put(2L, rule2);
    }

    @GetMapping("/rules")
    public Result<List<Map<String, Object>>> getSettlementRules() {
        List<Map<String, Object>> rules = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Object>> entry : SETTLE_RULES.entrySet()) {
            Map<String, Object> rule = new HashMap<>(entry.getValue());
            // 尝试从数据库获取商户名称
            Merchant merchant = merchantMapper.findById(entry.getKey());
            if (merchant != null) {
                rule.put("merchantName", merchant.getMerchantName());
            }
            rules.add(rule);
        }
        return Result.success(rules);
    }

    @PostMapping("/rules")
    public Result<String> saveSettlementRule(@RequestBody Map<String, Object> rule) {
        Long merchantId = Long.valueOf(rule.get("merchantId").toString());
        // 保存商户名称
        Merchant merchant = merchantMapper.findById(merchantId);
        if (merchant != null) {
            rule.put("merchantName", merchant.getMerchantName());
        }
        SETTLE_RULES.put(merchantId, rule);
        return Result.success("规则保存成功");
    }

    @GetMapping("/records")
    public Result<List<Map<String, Object>>> getSettlementRecords() {
        List<Settlement> settlements = settlementMapper.findPage(0, 100, null, null);
        List<Map<String, Object>> records = settlements.stream().map(s -> {
            Map<String, Object> record = new HashMap<>();
            record.put("id", s.getId());
            record.put("settleCode", s.getSettleCode());
            record.put("merchantId", s.getMerchantId());
            record.put("settleType", s.getSettleType());
            record.put("settlePeriod", s.getSettlePeriod());
            record.put("totalAmount", s.getTotalAmount());
            record.put("itemCount", s.getItemCount());
            record.put("status", s.getStatus());
            record.put("approver", s.getApprover());
            record.put("approveTime", s.getApproveTime());
            record.put("createTime", s.getCreateTime());
            // 查询商户名称
            Merchant merchant = merchantMapper.findById(s.getMerchantId());
            record.put("merchant", merchant != null ? merchant.getMerchantName() : ("商户" + s.getMerchantId()));
            return record;
        }).collect(Collectors.toList());
        return Result.success(records);
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> getSettlementOverview() {
        Map<String, Object> overview = new HashMap<>();
        BigDecimal totalSettled = settlementMapper.sumCompletedAmount();
        Integer pendingCount = settlementMapper.countPending();
        Integer completedCount = settlementMapper.countCompleted();

        overview.put("totalAmount", totalSettled != null ? totalSettled : BigDecimal.ZERO);
        overview.put("completedCount", completedCount != null ? completedCount : 0);
        overview.put("pendingCount", pendingCount != null ? pendingCount : 0);
        overview.put("totalCount", settlementMapper.countAll());
        overview.put("merchantCount", settlementMapper.countDistinctMerchants());

        return Result.success(overview);
    }
}
