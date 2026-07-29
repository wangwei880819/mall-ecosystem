package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.service.DataSyncService;
import com.igou.mall.service.DataConsistencyService;
import com.igou.mall.service.SyncMonitorService;
import com.igou.mall.service.JwtService;
import com.igou.mall.service.RateLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    @Autowired
    private DataSyncService dataSyncService;

    @Autowired
    private DataConsistencyService consistencyService;

    @Autowired
    private SyncMonitorService monitorService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RateLimitService rateLimitService;

    private boolean checkAuth(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return false;
        }
        String token = authorization.substring(7);
        return jwtService.isTokenValid(token);
    }

    @PostMapping("/product/{id}")
    public Result<Map<String, Object>> syncProduct(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        if (!rateLimitService.checkRateLimit("sync:product")) {
            return Result.error("请求过于频繁，请稍后重试");
        }

        Map<String, Object> result = dataSyncService.syncProduct(id);
        monitorService.recordSyncEvent("PRODUCT", id, (Boolean) result.get("success"),
                (String) result.get("message"));

        if ((Boolean) result.get("success")) {
            return Result.success(result);
        }
        return Result.error((String) result.get("message"));
    }

    @PostMapping("/product/batch")
    public Result<Map<String, Object>> syncProductBatch(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        if (!rateLimitService.checkRateLimit("sync:product:batch", 10, 60000)) {
            return Result.error("请求过于频繁，请稍后重试");
        }

        @SuppressWarnings("unchecked")
        List<Long> productIds = (List<Long>) params.get("productIds");
        Map<String, Object> result = dataSyncService.syncProductBatch(productIds);

        for (Long productId : productIds) {
            monitorService.recordSyncEvent("PRODUCT", productId, true, "批量同步");
        }

        return Result.success(result);
    }

    @PostMapping("/order/{id}")
    public Result<Map<String, Object>> syncOrder(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        if (!rateLimitService.checkRateLimit("sync:order")) {
            return Result.error("请求过于频繁，请稍后重试");
        }

        Map<String, Object> result = dataSyncService.syncOrder(id);
        monitorService.recordSyncEvent("ORDER", id, (Boolean) result.get("success"),
                (String) result.get("message"));

        if ((Boolean) result.get("success")) {
            return Result.success(result);
        }
        return Result.error((String) result.get("message"));
    }

    @PostMapping("/order/batch")
    public Result<Map<String, Object>> syncOrderBatch(
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        @SuppressWarnings("unchecked")
        List<Long> orderIds = (List<Long>) params.get("orderIds");
        Map<String, Object> result = dataSyncService.syncOrderBatch(orderIds);

        for (Long orderId : orderIds) {
            monitorService.recordSyncEvent("ORDER", orderId, true, "批量同步");
        }

        return Result.success(result);
    }

    @PostMapping("/order/status/{id}")
    public Result<Map<String, Object>> syncOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        String newStatus = (String) params.get("status");
        Map<String, Object> result = dataSyncService.syncOrderStatus(id, newStatus);
        monitorService.recordSyncEvent("ORDER_STATUS", id, (Boolean) result.get("success"),
                (String) result.get("message"));

        if ((Boolean) result.get("success")) {
            return Result.success(result);
        }
        return Result.error((String) result.get("message"));
    }

    @PostMapping("/stock/{productId}")
    public Result<Map<String, Object>> syncStock(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> params,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        Integer changeAmount = (Integer) params.get("changeAmount");
        String changeType = (String) params.get("changeType");
        String reason = (String) params.get("reason");

        Map<String, Object> result = dataSyncService.syncStock(productId, changeAmount, changeType, reason);
        monitorService.recordSyncEvent("STOCK", productId, (Boolean) result.get("success"),
                (String) result.get("message"));

        if ((Boolean) result.get("success")) {
            return Result.success(result);
        }
        return Result.error((String) result.get("message"));
    }

    @GetMapping("/status")
    public Result<Map<String, Object>> getSyncStatus() {
        return Result.success(dataSyncService.getSyncStatus());
    }

    @GetMapping("/consistency/validate")
    public Result<Map<String, Object>> validateDataConsistency(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        return Result.success(consistencyService.validateAllData());
    }

    @PostMapping("/consistency/repair/{type}/{id}")
    public Result<Map<String, Object>> repairInconsistency(
            @PathVariable String type,
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        Map<String, Object> result = consistencyService.repairInconsistency(type, id);
        if ((Boolean) result.get("success")) {
            return Result.success(result);
        }
        return Result.error((String) result.get("message"));
    }

    @PostMapping("/consistency/repair-all")
    public Result<Map<String, Object>> repairAllInconsistencies(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        return Result.success(consistencyService.repairAllInconsistencies());
    }

    @GetMapping("/consistency/queue")
    public Result<Map<String, Object>> getInconsistencyQueue(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        return Result.success(consistencyService.getInconsistencyQueue());
    }

    @GetMapping("/monitor/stats")
    public Result<Map<String, Object>> getMonitorStats() {
        return Result.success(monitorService.getMonitorStats());
    }

    @GetMapping("/monitor/events")
    public Result<List<Map<String, Object>>> getRecentEvents(
            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(monitorService.getRecentSyncEvents(limit));
    }

    @GetMapping("/monitor/alerts")
    public Result<List<Map<String, Object>>> getAlerts() {
        return Result.success(monitorService.getAlertEvents());
    }

    @PostMapping("/monitor/alerts/{index}/acknowledge")
    public Result<String> acknowledgeAlert(
            @PathVariable int index,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        if (!checkAuth(authorization)) {
            return Result.error("未授权访问");
        }

        monitorService.acknowledgeAlert(index);
        return Result.success("告警已确认");
    }
}