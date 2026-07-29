package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.MallOrderMapper;
import com.igou.mall.dao.ProductMapper;
import com.igou.mall.dao.RefundApplyMapper;
import com.igou.mall.model.entity.MallOrder;
import com.igou.mall.model.entity.Product;
import com.igou.mall.model.entity.RefundApply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private MallOrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RefundApplyMapper refundApplyMapper;

    @GetMapping
    public Result<List<MallOrder>> list(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return Result.success(orderMapper.findAll(page * size, size));
    }

    @GetMapping("/{id}")
    public Result<MallOrder> getById(@PathVariable Long id) {
        MallOrder order = orderMapper.findById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @GetMapping("/detail/{orderCode}")
    public Result<MallOrder> getByCode(@PathVariable String orderCode) {
        MallOrder order = orderMapper.findByCode(orderCode);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @PostMapping
    public Result<MallOrder> create(@RequestBody Map<String, Object> params) {
        Long productId = Long.parseLong(params.get("productId").toString());
        Integer quantity = Integer.parseInt(params.get("quantity").toString());
        String customerPhone = (String) params.get("customerPhone");
        Long customerId = params.containsKey("customerId") ? Long.parseLong(params.get("customerId").toString()) : null;

        Product product = productMapper.findById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }
        if (product.getStock() < quantity) {
            return Result.error("库存不足");
        }

        MallOrder order = new MallOrder();
        order.setOrderCode("ORD" + System.currentTimeMillis());
        order.setProductId(productId);
        order.setProductName(product.getProductName());
        order.setProductImage(product.getImageUrls());
        order.setMerchantId(product.getMerchantId());
        order.setPrice(product.getPrice());
        order.setQuantity(quantity);
        
        BigDecimal orderAmount = product.getPrice().multiply(new BigDecimal(quantity));
        order.setOrderAmount(orderAmount);
        
        BigDecimal aiDouDeduct = params.containsKey("aiDouDeduct") ? new BigDecimal(params.get("aiDouDeduct").toString()) : BigDecimal.ZERO;
        order.setAiDouDeduct(aiDouDeduct);
        
        BigDecimal payAmount = orderAmount.subtract(aiDouDeduct);
        order.setPayAmount(payAmount);
        
        order.setCustomerPhone(customerPhone);
        order.setCustomerId(customerId);
        order.setStatus("CREATED");

        orderMapper.insert(order);
        productMapper.updateStock(productId, quantity);

        return Result.success(order);
    }

    @PutMapping("/{id}/pay")
    public Result<MallOrder> pay(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        MallOrder order = orderMapper.findById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!"CREATED".equals(order.getStatus())) {
            return Result.error("订单状态不允许支付");
        }

        order.setStatus("PAID");
        order.setPayMethod((String) params.get("payMethod"));
        order.setPayNo((String) params.get("payNo"));
        order.setPayTime(LocalDateTime.now());
        orderMapper.update(order);

        return Result.success(order);
    }

    @PutMapping("/{id}/fulfill")
    public Result<MallOrder> fulfill(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        MallOrder order = orderMapper.findById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            return Result.error("订单状态不允许发货");
        }

        order.setStatus("FULFILLED");
        order.setLogisticsNo((String) params.get("logisticsNo"));
        order.setLogisticsCompany((String) params.get("logisticsCompany"));
        order.setFulfillTime(LocalDateTime.now());
        orderMapper.update(order);

        return Result.success(order);
    }

    @PutMapping("/{id}/cancel")
    public Result<MallOrder> cancel(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        MallOrder order = orderMapper.findById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!"CREATED".equals(order.getStatus())) {
            return Result.error("订单状态不允许取消");
        }

        order.setStatus("CANCELLED");
        order.setCancelReason((String) params.get("cancelReason"));
        order.setCancelTime(LocalDateTime.now());
        orderMapper.update(order);

        return Result.success(order);
    }

    @PostMapping("/{id}/refund")
    public Result<RefundApply> applyRefund(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        MallOrder order = orderMapper.findById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!"PAID".equals(order.getStatus()) && !"FULFILLED".equals(order.getStatus())) {
            return Result.error("订单状态不允许退款");
        }

        RefundApply refundApply = new RefundApply();
        refundApply.setOrderCode(order.getOrderCode());
        refundApply.setCustomerId(order.getCustomerId());
        refundApply.setMerchantId(order.getMerchantId());
        refundApply.setProductId(order.getProductId());
        refundApply.setRefundType((String) params.get("refundType"));
        refundApply.setRefundAmount(new BigDecimal(params.get("refundAmount").toString()));
        refundApply.setReason((String) params.get("reason"));
        refundApply.setStatus("APPLIED");

        refundApplyMapper.insert(refundApply);
        return Result.success(refundApply);
    }

    @PutMapping("/refund/{rid}/audit")
    public Result<RefundApply> auditRefund(@PathVariable Long rid, @RequestBody Map<String, Object> params) {
        RefundApply refundApply = refundApplyMapper.findById(rid);
        if (refundApply == null) {
            return Result.error("退款申请不存在");
        }

        String auditStatus = (String) params.get("auditStatus");
        refundApply.setStatus(auditStatus);
        refundApply.setAuditor((String) params.get("auditor"));
        refundApply.setAuditComment((String) params.get("auditComment"));
        refundApply.setAuditTime(LocalDateTime.now());

        if ("APPROVED".equals(auditStatus)) {
            MallOrder order = orderMapper.findByCode(refundApply.getOrderCode());
            if (order != null) {
                order.setStatus("REFUNDED");
                order.setRefundAmount(refundApply.getRefundAmount());
                order.setRefundTime(LocalDateTime.now());
                orderMapper.update(order);
            }
            refundApply.setRefundTime(LocalDateTime.now());
            refundApply.setRefundNo("RFD" + System.currentTimeMillis());
        }

        refundApplyMapper.update(refundApply);
        return Result.success(refundApply);
    }

    @GetMapping("/merchant/{merchantId}")
    public Result<List<MallOrder>> listByMerchant(@PathVariable Long merchantId) {
        return Result.success(orderMapper.findByMerchantId(merchantId));
    }

    @GetMapping("/customer/{customerId}")
    public Result<List<MallOrder>> listByCustomer(@PathVariable Long customerId) {
        return Result.success(orderMapper.findByCustomerId(customerId));
    }

    @GetMapping("/customer-phone/{phone}")
    public Result<List<MallOrder>> listByCustomerPhone(@PathVariable String phone) {
        return Result.success(orderMapper.findByCustomerPhone(phone));
    }

    @GetMapping("/status/{status}")
    public Result<List<MallOrder>> listByStatus(@PathVariable String status) {
        return Result.success(orderMapper.findByStatus(status));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", orderMapper.count());
        stats.put("created", orderMapper.countByStatus("CREATED"));
        stats.put("paid", orderMapper.countByStatus("PAID"));
        stats.put("fulfilled", orderMapper.countByStatus("FULFILLED"));
        stats.put("evaluated", orderMapper.countByStatus("EVALUATED"));
        stats.put("refunded", orderMapper.countByStatus("REFUNDED"));
        stats.put("cancelled", orderMapper.countByStatus("CANCELLED"));
        return Result.success(stats);
    }
}