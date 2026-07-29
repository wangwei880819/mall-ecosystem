package com.igou.mall.service;

import com.igou.mall.dao.ProductMapper;
import com.igou.mall.dao.MallOrderMapper;
import com.igou.mall.dao.StockChangeMapper;
import com.igou.mall.dao.ShoppingCartMapper;
import com.igou.mall.model.entity.Product;
import com.igou.mall.model.entity.MallOrder;
import com.igou.mall.model.entity.StockChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class DataConsistencyService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MallOrderMapper orderMapper;

    @Autowired
    private StockChangeMapper stockChangeMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    private final ConcurrentLinkedQueue<Map<String, Object>> inconsistencyQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Map<String, Object>> repairQueue = new ConcurrentLinkedQueue<>();

    public Map<String, Object> validateAllData() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> inconsistencies = new ArrayList<>();

        inconsistencies.addAll(validateProducts());
        inconsistencies.addAll(validateOrders());
        inconsistencies.addAll(validateStock());
        inconsistencies.addAll(validateShoppingCart());

        result.put("validatedAt", LocalDateTime.now().toString());
        result.put("totalInconsistencies", inconsistencies.size());
        result.put("inconsistencies", inconsistencies);
        result.put("isConsistent", inconsistencies.isEmpty());

        for (Map<String, Object> inconsistency : inconsistencies) {
            inconsistencyQueue.offer(inconsistency);
        }

        return result;
    }

    public List<Map<String, Object>> validateProducts() {
        List<Map<String, Object>> inconsistencies = new ArrayList<>();
        List<Product> products = productMapper.findAll(0, Integer.MAX_VALUE);

        for (Product product : products) {
            if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                inconsistencies.add(createInconsistency("PRODUCT", product.getId(),
                    "价格为负数", product.getProductName()));
            }

            if (product.getStock() != null && product.getStock() < 0) {
                inconsistencies.add(createInconsistency("PRODUCT", product.getId(),
                    "库存为负数", product.getProductName()));
            }

            if (product.getMarketPrice() != null && product.getPrice() != null
                && product.getMarketPrice().compareTo(product.getPrice()) < 0) {
                inconsistencies.add(createInconsistency("PRODUCT", product.getId(),
                    "市场价低于售价", product.getProductName()));
            }
        }

        return inconsistencies;
    }

    public List<Map<String, Object>> validateOrders() {
        List<Map<String, Object>> inconsistencies = new ArrayList<>();
        List<MallOrder> orders = orderMapper.findAll(0, Integer.MAX_VALUE);

        for (MallOrder order : orders) {
            if (order.getOrderAmount() != null && order.getOrderAmount().compareTo(BigDecimal.ZERO) <= 0) {
                inconsistencies.add(createInconsistency("ORDER", order.getId(),
                    "订单金额小于等于0", order.getOrderCode()));
            }

            if (order.getPayAmount() != null && order.getPayAmount().compareTo(BigDecimal.ZERO) < 0) {
                inconsistencies.add(createInconsistency("ORDER", order.getId(),
                    "实付金额为负数", order.getOrderCode()));
            }

            if (order.getQuantity() != null && order.getQuantity() < 1) {
                inconsistencies.add(createInconsistency("ORDER", order.getId(),
                    "订单数量小于1", order.getOrderCode()));
            }

            if ("PAID".equals(order.getStatus()) && order.getPayTime() == null) {
                inconsistencies.add(createInconsistency("ORDER", order.getId(),
                    "已支付订单缺少支付时间", order.getOrderCode()));
            }

            if ("FULFILLED".equals(order.getStatus()) && order.getFulfillTime() == null) {
                inconsistencies.add(createInconsistency("ORDER", order.getId(),
                    "已发货订单缺少发货时间", order.getOrderCode()));
            }
        }

        return inconsistencies;
    }

    public List<Map<String, Object>> validateStock() {
        List<Map<String, Object>> inconsistencies = new ArrayList<>();
        List<Product> products = productMapper.findAll(0, Integer.MAX_VALUE);

        for (Product product : products) {
            List<StockChange> stockChanges = stockChangeMapper.findByProductId(product.getId());
            int calculatedStock = 0;

            for (StockChange change : stockChanges) {
                calculatedStock += change.getChangeAmount();
            }

            if (product.getStock() != null && calculatedStock != product.getStock()) {
                inconsistencies.add(createInconsistency("STOCK", product.getId(),
                    "库存计算不一致: 数据库=" + product.getStock() + ", 计算值=" + calculatedStock,
                    product.getProductName()));
            }
        }

        return inconsistencies;
    }

    public List<Map<String, Object>> validateShoppingCart() {
        List<Map<String, Object>> inconsistencies = new ArrayList<>();

        return inconsistencies;
    }

    private Map<String, Object> createInconsistency(String type, Long id, String description, String name) {
        Map<String, Object> inconsistency = new HashMap<>();
        inconsistency.put("type", type);
        inconsistency.put("id", id);
        inconsistency.put("description", description);
        inconsistency.put("name", name);
        inconsistency.put("detectedAt", LocalDateTime.now().toString());
        inconsistency.put("status", "PENDING");
        return inconsistency;
    }

    public Map<String, Object> repairInconsistency(String type, Long id) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        try {
            switch (type) {
                case "PRODUCT" -> {
                    Product product = productMapper.findById(id);
                    if (product != null) {
                        if (product.getStock() != null && product.getStock() < 0) {
                            product.setStock(0);
                        }
                        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                            product.setPrice(BigDecimal.ZERO);
                        }
                        productMapper.update(product);
                        result.put("success", true);
                        result.put("message", "商品数据修复成功");
                    }
                }
                case "ORDER" -> {
                    MallOrder order = orderMapper.findById(id);
                    if (order != null) {
                        if (order.getQuantity() != null && order.getQuantity() < 1) {
                            order.setQuantity(1);
                        }
                        orderMapper.update(order);
                        result.put("success", true);
                        result.put("message", "订单数据修复成功");
                    }
                }
                case "STOCK" -> {
                    Product product = productMapper.findById(id);
                    if (product != null) {
                        List<StockChange> stockChanges = stockChangeMapper.findByProductId(id);
                        int calculatedStock = 0;
                        for (StockChange change : stockChanges) {
                            calculatedStock += change.getChangeAmount();
                        }
                        product.setStock(Math.max(calculatedStock, 0));
                        productMapper.update(product);
                        result.put("success", true);
                        result.put("message", "库存数据修复成功");
                    }
                }
                default -> result.put("message", "不支持的修复类型");
            }
        } catch (Exception e) {
            result.put("message", "修复失败: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> repairAllInconsistencies() {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        while (!inconsistencyQueue.isEmpty()) {
            Map<String, Object> inconsistency = inconsistencyQueue.poll();
            String type = (String) inconsistency.get("type");
            Long id = (Long) inconsistency.get("id");

            Map<String, Object> repairResult = repairInconsistency(type, id);
            if ((Boolean) repairResult.get("success")) {
                successCount++;
            } else {
                failCount++;
                repairQueue.offer(inconsistency);
            }
        }

        result.put("success", failCount == 0);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("repairedAt", LocalDateTime.now().toString());

        return result;
    }

    public Map<String, Object> getInconsistencyQueue() {
        Map<String, Object> result = new HashMap<>();
        result.put("pendingCount", inconsistencyQueue.size());
        result.put("repairQueueCount", repairQueue.size());
        result.put("pendingInconsistencies", new ArrayList<>(inconsistencyQueue));
        return result;
    }

    @Scheduled(fixedRate = 3600000)
    public void scheduledValidation() {
        validateAllData();
    }
}