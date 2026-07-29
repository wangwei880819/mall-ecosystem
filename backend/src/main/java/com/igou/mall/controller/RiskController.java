package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.*;
import com.igou.mall.model.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 风控稽核管理平台Controller
 * 提供风控看板、事件管理、规则管理、名单库、处置管理、数据分析等API
 */
@RestController
@RequestMapping("/api/risk")
public class RiskController {

    @Autowired
    private RiskEventMapper riskEventMapper;

    @Autowired
    private RiskRuleMapper riskRuleMapper;

    @Autowired
    private BlacklistMapper blacklistMapper;

    @Autowired
    private DisposalMapper disposalMapper;

    /** 风控看板数据 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        List<RiskEvent> events = riskEventMapper.findAll();
        List<RiskRule> rules = riskRuleMapper.findAll();

        long pendingCount = events.stream().filter(e -> "PENDING".equals(e.getStatus())).count();
        long highRiskCount = events.stream().filter(e -> "HIGH".equals(e.getRiskLevel())).count();

        int totalHits = rules.stream().mapToInt(r -> r.getHitCount() != null ? r.getHitCount() : 0).sum();
        long activeRules = rules.stream().filter(r -> r.getActive() != null && r.getActive()).count();

        // 最近告警
        List<Map<String, Object>> recentAlerts = new ArrayList<>();
        int limit = Math.min(events.size(), 8);
        for (int i = 0; i < limit; i++) {
            RiskEvent e = events.get(i);
            Map<String, Object> alert = new HashMap<>();
            alert.put("eventType", e.getEventType());
            alert.put("target", e.getTarget());
            alert.put("riskLevel", getRiskLabel(e.getRiskLevel()));
            alert.put("ruleName", e.getHitRule());
            alert.put("time", e.getCreateTime() != null ? e.getCreateTime().toString().replace("T", " ") : "");
            recentAlerts.add(alert);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("todayBlocks", 127);
        data.put("blockTrend", 12.5);
        data.put("pendingAlerts", pendingCount);
        data.put("highRiskCount", highRiskCount);
        data.put("ruleHitRate", 87.3);
        data.put("totalHits", totalHits);
        data.put("auditAccuracy", 96.8);
        data.put("recentAlerts", recentAlerts);
        return Result.success(data);
    }

    /** 风控事件列表 */
    @GetMapping("/events")
    public Result<List<Map<String, Object>>> events(
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String status) {
        List<RiskEvent> all = riskEventMapper.findAll();
        List<Map<String, Object>> list = new ArrayList<>();
        for (RiskEvent e : all) {
            // 简单过滤
            if (eventType != null && !eventType.isEmpty() && !eventType.equals(e.getEventType())) continue;
            if (riskLevel != null && !riskLevel.isEmpty() && !riskLevel.equals(e.getRiskLevel())) continue;
            if (status != null && !status.isEmpty() && !status.equals(e.getStatus())) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("id", "EVT" + e.getId());
            item.put("eventType", e.getEventType());
            item.put("target", e.getTarget());
            item.put("riskLevel", e.getRiskLevel());
            item.put("score", e.getScore());
            item.put("hitRule", e.getHitRule());
            item.put("status", e.getStatus());
            item.put("source", e.getSource());
            item.put("createTime", e.getCreateTime() != null ? e.getCreateTime().toString().replace("T", " ") : "");
            item.put("detail", e.getDetail());
            list.add(item);
        }
        return Result.success(list);
    }

    /** 事件状态更新 (拦截/放行/人工审核) */
    @PutMapping("/events/{id}/status")
    public Result<String> updateEventStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        riskEventMapper.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    /** 规则列表 */
    @GetMapping("/rules")
    public Result<List<Map<String, Object>>> rules() {
        List<RiskRule> all = riskRuleMapper.findAll();
        List<Map<String, Object>> list = new ArrayList<>();
        for (RiskRule r : all) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("name", r.getName());
            item.put("type", r.getType());
            item.put("scene", r.getScene());
            item.put("priority", r.getPriority());
            item.put("condition", r.getRuleCondition());
            item.put("action", r.getAction());
            item.put("hitCount", r.getHitCount());
            item.put("active", r.getActive());
            item.put("description", r.getDescription());
            list.add(item);
        }
        return Result.success(list);
    }

    /** 规则启用/停用 */
    @PutMapping("/rules/{id}/toggle")
    public Result<String> toggleRule(@PathVariable Long id) {
        riskRuleMapper.toggleStatus(id);
        return Result.success("规则状态切换成功");
    }

    /** 更新规则 */
    @PutMapping("/rules/{id}")
    public Result<String> updateRule(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        RiskRule rule = riskRuleMapper.findById(id);
        if (rule == null) return Result.error("规则不存在");
        if (data.containsKey("name")) rule.setName((String) data.get("name"));
        if (data.containsKey("type")) rule.setType((String) data.get("type"));
        if (data.containsKey("scene")) rule.setScene((String) data.get("scene"));
        if (data.containsKey("priority")) rule.setPriority((Integer) data.get("priority"));
        if (data.containsKey("condition")) rule.setRuleCondition((String) data.get("condition"));
        if (data.containsKey("action")) rule.setAction((String) data.get("action"));
        if (data.containsKey("description")) rule.setDescription((String) data.get("description"));
        riskRuleMapper.update(rule);
        return Result.success("规则更新成功");
    }

    /** 新增规则 */
    @PostMapping("/rules")
    public Result<String> addRule(@RequestBody Map<String, Object> data) {
        RiskRule rule = new RiskRule();
        rule.setName((String) data.get("name"));
        rule.setType((String) data.getOrDefault("type", "CONDITION"));
        rule.setScene((String) data.getOrDefault("scene", ""));
        rule.setPriority((Integer) data.getOrDefault("priority", 5));
        rule.setRuleCondition((String) data.getOrDefault("condition", ""));
        rule.setAction((String) data.getOrDefault("action", "MANUAL"));
        rule.setHitCount(0);
        rule.setActive(true);
        rule.setDescription((String) data.getOrDefault("description", ""));
        riskRuleMapper.insert(rule);
        return Result.success("规则添加成功");
    }

    /** 删除规则 */
    @DeleteMapping("/rules/{id}")
    public Result<String> deleteRule(@PathVariable Long id) {
        riskRuleMapper.delete(id);
        return Result.success("规则删除成功");
    }

    /** 名单库列表 */
    @GetMapping("/blacklist")
    public Result<List<Map<String, Object>>> blacklist(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String listType) {
        List<BlacklistItem> all = blacklistMapper.findAll();
        List<Map<String, Object>> list = new ArrayList<>();
        for (BlacklistItem b : all) {
            if (keyword != null && !keyword.isEmpty() && !b.getValue().contains(keyword)) continue;
            if (type != null && !type.isEmpty() && !type.equals(b.getListType())) continue;
            if (listType != null && !listType.isEmpty() && !listType.equals(b.getType())) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("id", b.getId());
            item.put("value", b.getValue());
            item.put("type", b.getType());
            item.put("listType", b.getListType());
            item.put("reason", b.getReason());
            item.put("source", b.getSource());
            item.put("operator", b.getOperator());
            item.put("createTime", b.getCreateTime() != null ? b.getCreateTime().toString().replace("T", " ") : "");
            item.put("expireTime", b.getExpireTime() != null ? b.getExpireTime().toString().replace("T", " ") : null);
            list.add(item);
        }
        return Result.success(list);
    }

    /** 添加名单 */
    @PostMapping("/blacklist")
    public Result<String> addBlacklist(@RequestBody Map<String, Object> data) {
        BlacklistItem item = new BlacklistItem();
        item.setValue((String) data.get("value"));
        item.setType((String) data.getOrDefault("type", "PHONE"));
        item.setListType((String) data.getOrDefault("listType", "BLACK"));
        item.setReason((String) data.getOrDefault("reason", ""));
        item.setSource((String) data.getOrDefault("source", "手动添加"));
        item.setOperator((String) data.getOrDefault("operator", "管理员"));
        // expireTime can be null (permanent)
        blacklistMapper.insert(item);
        return Result.success("添加成功");
    }

    /** 移除名单 */
    @DeleteMapping("/blacklist/{id}")
    public Result<String> removeBlacklist(@PathVariable Long id) {
        blacklistMapper.delete(id);
        return Result.success("移除成功");
    }

    /** 处置方案列表 */
    @GetMapping("/disposals")
    public Result<List<Map<String, Object>>> disposals() {
        List<DisposalConfig> all = disposalMapper.findAll();
        List<Map<String, Object>> list = new ArrayList<>();
        for (DisposalConfig d : all) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", d.getId());
            item.put("name", d.getName());
            item.put("type", d.getType());
            item.put("triggerRule", d.getTriggerRule());
            item.put("riskLevel", d.getRiskLevel());
            item.put("duration", d.getDuration());
            item.put("status", d.getStatus());
            item.put("execCount", d.getExecCount());
            item.put("description", d.getDescription());
            item.put("updateTime", d.getUpdateTime() != null ? d.getUpdateTime().toString().replace("T", " ") : "");
            list.add(item);
        }
        return Result.success(list);
    }

    /** 处置方案新增/更新 */
    @PostMapping("/disposals")
    public Result<String> addDisposal(@RequestBody Map<String, Object> data) {
        DisposalConfig config = new DisposalConfig();
        config.setName((String) data.get("name"));
        config.setType((String) data.getOrDefault("type", "BLOCK"));
        config.setTriggerRule((String) data.getOrDefault("triggerRule", ""));
        config.setRiskLevel((String) data.getOrDefault("riskLevel", "HIGH"));
        config.setDuration((String) data.getOrDefault("duration", "24小时"));
        config.setStatus("ACTIVE");
        config.setExecCount(0);
        config.setDescription((String) data.getOrDefault("description", ""));
        disposalMapper.insert(config);
        return Result.success("方案添加成功");
    }

    @PutMapping("/disposals/{id}")
    public Result<String> updateDisposal(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        DisposalConfig config = disposalMapper.findById(id);
        if (config == null) return Result.error("方案不存在");
        if (data.containsKey("name")) config.setName((String) data.get("name"));
        if (data.containsKey("type")) config.setType((String) data.get("type"));
        if (data.containsKey("triggerRule")) config.setTriggerRule((String) data.get("triggerRule"));
        if (data.containsKey("riskLevel")) config.setRiskLevel((String) data.get("riskLevel"));
        if (data.containsKey("duration")) config.setDuration((String) data.get("duration"));
        if (data.containsKey("status")) config.setStatus((String) data.get("status"));
        if (data.containsKey("description")) config.setDescription((String) data.get("description"));
        disposalMapper.update(config);
        return Result.success("方案更新成功");
    }

    @PutMapping("/disposals/{id}/toggle")
    public Result<String> toggleDisposal(@PathVariable Long id) {
        DisposalConfig config = disposalMapper.findById(id);
        if (config == null) return Result.error("方案不存在");
        config.setStatus("ACTIVE".equals(config.getStatus()) ? "INACTIVE" : "ACTIVE");
        disposalMapper.update(config);
        return Result.success("状态切换成功");
    }

    /** 数据分析 */
    @GetMapping("/analysis")
    public Result<Map<String, Object>> analysis() {
        List<RiskEvent> events = riskEventMapper.findAll();
        List<RiskRule> rules = riskRuleMapper.findAll();

        Map<String, Object> data = new HashMap<>();
        data.put("totalAudits", 12580);
        data.put("monthAudits", 2847);
        data.put("blockRate", 92.5);
        data.put("falsePositiveRate", 1.2);
        data.put("avgResponse", 85);
        data.put("p99Response", 210);
        data.put("sceneCount", 7);
        data.put("ruleCount", rules.size());
        return Result.success(data);
    }

    /** 稽核日志 */
    @GetMapping("/audit-logs")
    public Result<List<Map<String, Object>>> auditLogs() {
        List<RiskEvent> events = riskEventMapper.findAll();
        List<Map<String, Object>> list = new ArrayList<>();
        for (RiskEvent e : events) {
            Map<String, Object> log = new HashMap<>();
            log.put("id", "AUD" + e.getId());
            log.put("eventType", e.getEventType());
            log.put("target", e.getTarget());
            log.put("ruleName", e.getHitRule());
            log.put("riskScore", e.getScore());
            log.put("result", "PENDING".equals(e.getStatus()) ? "MANUAL" : e.getStatus());
            log.put("auditor", "风控系统");
            log.put("auditTime", e.getCreateTime() != null ? e.getCreateTime().toString().replace("T", " ") : "");
            log.put("remark", e.getDetail());
            list.add(log);
        }
        return Result.success(list);
    }

    // ===== Helper =====
    private String getRiskLabel(String level) {
        if ("HIGH".equals(level)) return "高风险";
        if ("MEDIUM".equals(level)) return "中风险";
        if ("LOW".equals(level)) return "低风险";
        return level;
    }
}
