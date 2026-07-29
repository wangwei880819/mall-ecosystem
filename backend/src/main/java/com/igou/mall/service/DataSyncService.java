package com.igou.mall.service;

import com.igou.mall.dao.ProductMapper;
import com.igou.mall.dao.MallOrderMapper;
import com.igou.mall.dao.StockChangeMapper;
import com.igou.mall.model.entity.Product;
import com.igou.mall.model.entity.MallOrder;
import com.igou.mall.model.entity.StockChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DataSyncService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MallOrderMapper orderMapper;

    @Autowired
    private StockChangeMapper stockChangeMapper;

    private final ConcurrentHashMap<String, Long> syncTimestamps = new ConcurrentHashMap<>();
    private final AtomicLong syncCounter = new AtomicLong(0);

    @Transactional
    public Map<String, Object> syncProduct(Long productId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            Product product = productMapper.findById(productId);
            if (product == null) {
                result.put("message", "商品不存在");
                return result;
            }

            if (!"ACTIVE".equals(product.getStatus())) {
                result.put("message", "商品未上架");
                return result;
            }

            result.put("productId", productId);
            result.put("productCode", product.getProductCode());
            result.put("syncTime", LocalDateTime.now().toString());
            result.put("syncId", syncCounter.incrementAndGet());
            result.put("success", true);
            result.put("message", "商品数据同步成功");

            syncTimestamps.put("product:" + productId, System.currentTimeMillis());
        } catch (Exception e) {
            result.put("message", "商品数据同步失败: " + e.getMessage());
        }

        return result;
    }

    @Transactional
    public Map<String, Object> syncProductBatch(List<Long> productIds) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (Long productId : productIds) {
            Map<String, Object> syncResult = syncProduct(productId);
            if ((Boolean) syncResult.get("success")) {
                successCount++;
            } else {
                failCount++;
            }
        }

        result.put("success", failCount == 0);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("syncTime", LocalDateTime.now().toString());
        result.put("totalCount", productIds.size());

        return result;
    }

    @Transactional
    public Map<String, Object> syncOrder(Long orderId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            MallOrder order = orderMapper.findById(orderId);
            if (order == null) {
                result.put("message", "订单不存在");
                return result;
            }

            result.put("orderId", orderId);
            result.put("orderCode", order.getOrderCode());
            result.put("status", order.getStatus());
            result.put("syncTime", LocalDateTime.now().toString());
            result.put("syncId", syncCounter.incrementAndGet());
            result.put("success", true);
            result.put("message", "订单数据同步成功");

            syncTimestamps.put("order:" + orderId, System.currentTimeMillis());
        } catch (Exception e) {
            result.put("message", "订单数据同步失败: " + e.getMessage());
        }

        return result;
    }

    @Transactional
    public Map<String, Object> syncOrderBatch(List<Long> orderIds) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (Long orderId : orderIds) {
            Map<String, Object> syncResult = syncOrder(orderId);
            if ((Boolean) syncResult.get("success")) {
                successCount++;
            } else {
                failCount++;
            }
        }

        result.put("success", failCount == 0);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("syncTime", LocalDateTime.now().toString());
        result.put("totalCount", orderIds.size());

        return result;
    }

    @Transactional
    public Map<String, Object> syncStock(Long productId, Integer changeAmount, String changeType, String reason) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            Product product = productMapper.findById(productId);
            if (product == null) {
                result.put("message", "商品不存在");
                return result;
            }

            int beforeStock = product.getStock();
            int afterStock = beforeStock + changeAmount;

            if (afterStock < 0) {
                result.put("message", "库存不足");
                return result;
            }

            product.setStock(afterStock);
            productMapper.update(product);

            StockChange stockChange = new StockChange();
            stockChange.setProductId(productId);
            stockChange.setChangeType(changeType);
            stockChange.setChangeAmount(changeAmount);
            stockChange.setBeforeStock(beforeStock);
            stockChange.setAfterStock(afterStock);
            stockChange.setReason(reason);
            stockChangeMapper.insert(stockChange);

            result.put("productId", productId);
            result.put("beforeStock", beforeStock);
            result.put("afterStock", afterStock);
            result.put("changeAmount", changeAmount);
            result.put("changeType", changeType);
            result.put("syncTime", LocalDateTime.now().toString());
            result.put("syncId", syncCounter.incrementAndGet());
            result.put("success", true);
            result.put("message", "库存数据同步成功");

            syncTimestamps.put("stock:" + productId, System.currentTimeMillis());
        } catch (Exception e) {
            result.put("message", "库存数据同步失败: " + e.getMessage());
        }

        return result;
    }

    @Transactional
    public Map<String, Object> syncOrderStatus(Long orderId, String newStatus) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            MallOrder order = orderMapper.findById(orderId);
            if (order == null) {
                result.put("message", "订单不存在");
                return result;
            }

            String oldStatus = order.getStatus();
            if (!isValidStatusTransition(oldStatus, newStatus)) {
                result.put("message", "无效的订单状态转换: " + oldStatus + " -> " + newStatus);
                return result;
            }

            order.setStatus(newStatus);
            order.setUpdateTime(LocalDateTime.now());

            if ("PAID".equals(newStatus)) {
                order.setPayTime(LocalDateTime.now());
            } else if ("FULFILLED".equals(newStatus)) {
                order.setFulfillTime(LocalDateTime.now());
            } else if ("REFUNDED".equals(newStatus)) {
                order.setRefundTime(LocalDateTime.now());
            } else if ("CANCELLED".equals(newStatus)) {
                order.setCancelTime(LocalDateTime.now());
            }

            orderMapper.update(order);

            result.put("orderId", orderId);
            result.put("orderCode", order.getOrderCode());
            result.put("oldStatus", oldStatus);
            result.put("newStatus", newStatus);
            result.put("syncTime", LocalDateTime.now().toString());
            result.put("syncId", syncCounter.incrementAndGet());
            result.put("success", true);
            result.put("message", "订单状态同步成功");

            syncTimestamps.put("order:" + orderId, System.currentTimeMillis());
        } catch (Exception e) {
            result.put("message", "订单状态同步失败: " + e.getMessage());
        }

        return result;
    }

    private boolean isValidStatusTransition(String oldStatus, String newStatus) {
        return switch (oldStatus) {
            case "CREATED" -> List.of("PAID", "CANCELLED").contains(newStatus);
            case "PAID" -> List.of("FULFILLED", "REFUNDED").contains(newStatus);
            case "FULFILLED" -> List.of("EVALUATED", "REFUNDED").contains(newStatus);
            case "EVALUATED" -> false;
            case "REFUNDED" -> false;
            case "CANCELLED" -> false;
            default -> false;
        };
    }

    public Map<String, Object> getSyncStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("syncCounter", syncCounter.get());
        result.put("latestSyncTime", LocalDateTime.now().toString());
        result.put("syncTimestamps", syncTimestamps);
        return result;
    }

    public void clearSyncTimestamps() {
        syncTimestamps.clear();
    }
}