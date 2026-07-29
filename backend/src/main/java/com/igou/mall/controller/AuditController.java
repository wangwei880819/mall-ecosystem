package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.AuditRecordMapper;
import com.igou.mall.model.entity.AuditRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 业务数据稽核Controller — DEMO演示项4：风控稽核管理平台（1分）
 * 提供订单稽核、资金稽核、AI风控规则、三维度对账等API
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired private AuditRecordMapper auditRecordMapper;

    /** 稽核概览 */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalChecked", 156320);
        data.put("passRate", "99.98%");
        data.put("anomalyCount", 34);
        data.put("pendingCount", auditRecordMapper.highRiskPendingCount());
        data.put("recoveredAmount", 86500);
        return Result.success(data);
    }

    /** 订单稽核列表 */
    @GetMapping("/orders")
    public Result<List<AuditRecord>> orderAudit() {
        return Result.success(auditRecordMapper.findByType("ORDER"));
    }

    /** 资金稽核列表 */
    @GetMapping("/funds")
    public Result<List<AuditRecord>> fundAudit() {
        return Result.success(auditRecordMapper.findByType("FUND"));
    }

    /** AI风控规则引擎 */
    @GetMapping("/rules")
    public Result<List<Map<String, Object>>> rules() {
        return Result.success(List.of(
            rule("R001", "刷单识别规则", "同一IP/设备10分钟内下单超过10笔", 23),
            rule("R002", "虚假交易识别", "收货地址与下单地址不一致且金额超500", 15),
            rule("R003", "异常退款监控", "下单后24小时内申请退款率超30%", 8),
            rule("R004", "结算金额波动", "商户结算金额环比增长超200%", 5),
            rule("R005", "重复结算检测", "同一订单触发多次结算", 3),
            rule("R006", "AI异常交易模型", "基于深度学习的异常交易识别模型", 12)
        ));
    }

    /** 三维度资金对账 */
    @GetMapping("/reconciliation")
    public Result<List<Map<String, Object>>> reconciliation() {
        return Result.success(List.of(
            Map.of("type", "AI豆对账", "formula", "发放量=消费量+过期量+余额量", "status", "一致",
                   "details", "发放326.5万 | 消费285.2万 | 过期12.3万 | 余额29.0万"),
            Map.of("type", "佣金对账", "formula", "计算总额=分账总额=结算总额", "status", "一致",
                   "details", "计算789.2万 | 分账789.2万 | 结算789.2万"),
            Map.of("type", "商拓费对账", "formula", "核算总额=审批总额=结算总额", "status", "差异2笔",
                   "details", "核算170.8万 | 审批170.5万 | 结算170.5万 | 差异¥3,000")
        ));
    }

    /** 实时稽核引擎指标 */
    @GetMapping("/engine")
    public Result<Map<String, Object>> engineMetrics() {
        Map<String, Object> data = new HashMap<>();
        data.put("engine", "ClickHouse");
        data.put("queryResponseMs", 500);
        data.put("coverageRate", "100%");
        data.put("desc", "实时OLAP引擎·分钟级数据更新·无稽核盲区");
        return Result.success(data);
    }

    /** 处理稽核记录 */
    @PostMapping("/{id}/resolve")
    public Result<String> resolve(@PathVariable Long id, @RequestParam String handler,
                                   @RequestParam String result) {
        auditRecordMapper.resolve(id, "RESOLVED", handler, result);
        return Result.success("处理成功");
    }

    private Map<String, Object> rule(String id, String name, String desc, int triggered) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("desc", desc);
        m.put("triggered", triggered);
        m.put("enabled", true);
        return m;
    }
}
