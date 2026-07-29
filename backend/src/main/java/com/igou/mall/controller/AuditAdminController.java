package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.AuditRecordMapper;
import com.igou.mall.model.entity.AuditRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin/audit")
@CrossOrigin(origins = "*")
public class AuditAdminController {

    @Autowired
    private AuditRecordMapper auditRecordMapper;

    private static final List<Map<String, Object>> AUDIT_RULES = new ArrayList<>();

    static {
        AUDIT_RULES.add(Map.of(
                "id", 1, "name", "刷单识别", "description", "同一IP/设备短时间内下单超过阈值",
                "threshold", 10, "timeWindow", 600, "enabled", true, "riskLevel", "HIGH"
        ));
        AUDIT_RULES.add(Map.of(
                "id", 2, "name", "虚假交易识别", "description", "收货地址与下单地址不一致且金额超阈值",
                "threshold", 500, "enabled", true, "riskLevel", "MEDIUM"
        ));
        AUDIT_RULES.add(Map.of(
                "id", 3, "name", "异常退款监控", "description", "下单后24小时内申请退款率超30%",
                "threshold", 30, "enabled", true, "riskLevel", "MEDIUM"
        ));
        AUDIT_RULES.add(Map.of(
                "id", 4, "name", "结算金额波动", "description", "商户结算金额环比增长超200%",
                "threshold", 200, "enabled", true, "riskLevel", "HIGH"
        ));
        AUDIT_RULES.add(Map.of(
                "id", 5, "name", "重复结算检测", "description", "同一订单触发多次结算",
                "enabled", true, "riskLevel", "HIGH"
        ));
        AUDIT_RULES.add(Map.of(
                "id", 6, "name", "AI异常交易模型", "description", "基于深度学习的异常交易识别",
                "enabled", true, "riskLevel", "MEDIUM"
        ));
    }

    @GetMapping("/rules")
    public Result<List<Map<String, Object>>> getAuditRules() {
        return Result.success(AUDIT_RULES);
    }

    @PutMapping("/rules/{id}")
    public Result<String> updateAuditRule(@PathVariable Integer id, @RequestBody Map<String, Object> updates) {
        for (Map<String, Object> rule : AUDIT_RULES) {
            if (rule.get("id").equals(id)) {
                rule.putAll(updates);
                break;
            }
        }
        return Result.success("规则更新成功");
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getAuditStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChecked", 156320);
        stats.put("passRate", "99.98%");
        stats.put("highRiskCount", 12);
        stats.put("mediumRiskCount", 28);
        stats.put("lowRiskCount", 45);
        stats.put("resolvedCount", 78);
        stats.put("recoveredAmount", "86500.00");
        return Result.success(stats);
    }

    @GetMapping("/records")
    public Result<List<AuditRecord>> getAuditRecords(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     @RequestParam(defaultValue = "") String riskLevel,
                                                     @RequestParam(defaultValue = "") String status) {
        return Result.success(auditRecordMapper.findAll(page * size, size));
    }

    @PostMapping("/records")
    public Result<String> createAuditRecord(@RequestBody AuditRecord record) {
        String auditCode = "AUD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        record.setAuditCode(auditCode);
        record.setStatus("PENDING");
        auditRecordMapper.insert(record);
        return Result.success("稽核记录创建成功");
    }
}
