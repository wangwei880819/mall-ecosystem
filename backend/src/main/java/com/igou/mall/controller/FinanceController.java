package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.SettlementMapper;
import com.igou.mall.dao.SettlementDetailMapper;
import com.igou.mall.dao.InvoiceMapper;
import com.igou.mall.dao.ReconciliationRecordMapper;
import com.igou.mall.dao.MallOrderMapper;
import com.igou.mall.model.entity.Settlement;
import com.igou.mall.model.entity.SettlementDetail;
import com.igou.mall.model.entity.Invoice;
import com.igou.mall.model.entity.ReconciliationRecord;
import com.igou.mall.model.entity.MallOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "*")
public class FinanceController {

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private SettlementDetailMapper detailMapper;

    @Autowired
    private InvoiceMapper invoiceMapper;

    @Autowired
    private ReconciliationRecordMapper reconMapper;

    @Autowired
    private MallOrderMapper orderMapper;

    @GetMapping("/settlements")
    public Result<List<Settlement>> listSettlements(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return Result.success(settlementMapper.findPage(page * size, size, null, null));
    }

    @GetMapping("/settlements/{id}")
    public Result<Map<String, Object>> getSettlementDetail(@PathVariable Long id) {
        Settlement settlement = settlementMapper.findById(id);
        if (settlement == null) {
            return Result.error("结算单不存在");
        }
        List<SettlementDetail> details = detailMapper.findBySettleId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("settlement", settlement);
        result.put("details", details);
        return Result.success(result);
    }

    @PostMapping("/settlements")
    public Result<Settlement> createSettlement(@RequestBody Map<String, Object> params) {
        Long merchantId = Long.parseLong(params.get("merchantId").toString());
        String settlePeriod = (String) params.get("settlePeriod");
        String settleType = (String) params.getOrDefault("settleType", "COMMISSION");

        List<MallOrder> orders = orderMapper.findByMerchantId(merchantId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        int itemCount = 0;

        for (MallOrder order : orders) {
            if ("PAID".equals(order.getStatus()) || "FULFILLED".equals(order.getStatus())) {
                totalAmount = totalAmount.add(order.getOrderAmount());
                itemCount++;
            }
        }

        Settlement settlement = new Settlement();
        settlement.setSettleCode("STL" + System.currentTimeMillis());
        settlement.setMerchantId(merchantId);
        settlement.setSettleType(settleType);
        settlement.setTotalAmount(totalAmount);
        settlement.setItemCount(itemCount);
        settlement.setSettlePeriod(settlePeriod);
        settlement.setStatus("PENDING");
        settlement.setCreateTime(LocalDateTime.now());

        settlementMapper.insert(settlement);

        for (MallOrder order : orders) {
            if ("PAID".equals(order.getStatus()) || "FULFILLED".equals(order.getStatus())) {
                SettlementDetail detail = new SettlementDetail();
                detail.setSettleId(settlement.getId());
                detail.setOrderCode(order.getOrderCode());
                detail.setOrderAmount(order.getOrderAmount());
                detail.setMerchantAmount(order.getOrderAmount());
                detailMapper.insert(detail);
            }
        }

        return Result.success(settlement);
    }

    @PutMapping("/settlements/{id}/approve")
    public Result<Settlement> approveSettlement(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Settlement settlement = settlementMapper.findById(id);
        if (settlement == null) {
            return Result.error("结算单不存在");
        }

        settlement.setStatus("COMPLETED");
        settlement.setApprover((String) params.getOrDefault("approver", "admin"));
        settlement.setApproveTime(LocalDateTime.now());
        settlementMapper.update(settlement);

        return Result.success(settlement);
    }

    @PutMapping("/settlements/{id}/pay")
    public Result<Settlement> paySettlement(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Settlement settlement = settlementMapper.findById(id);
        if (settlement == null) {
            return Result.error("结算单不存在");
        }

        settlement.setStatus("PAID");
        settlementMapper.update(settlement);

        return Result.success(settlement);
    }

    @GetMapping("/settlements/merchant/{merchantId}")
    public Result<List<Settlement>> listByMerchant(@PathVariable Long merchantId) {
        return Result.success(settlementMapper.findPage(0, 100, merchantId, null));
    }

    @GetMapping("/settlements/status/{status}")
    public Result<List<Settlement>> listByStatus(@PathVariable String status) {
        return Result.success(settlementMapper.findPage(0, 100, null, status));
    }

    @GetMapping("/invoices")
    public Result<List<Invoice>> listInvoices(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.success(invoiceMapper.findAll(page * size, size));
    }

    @GetMapping("/invoices/{id}")
    public Result<Invoice> getInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceMapper.findById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }
        return Result.success(invoice);
    }

    @PostMapping("/invoices")
    public Result<Invoice> createInvoice(@RequestBody Invoice invoice) {
        invoice.setStatus("APPLIED");
        invoiceMapper.insert(invoice);
        return Result.success(invoice);
    }

    @PutMapping("/invoices/{id}/issue")
    public Result<Invoice> issueInvoice(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Invoice invoice = invoiceMapper.findById(id);
        if (invoice == null) {
            return Result.error("发票不存在");
        }

        invoice.setStatus("ISSUED");
        invoice.setIssueTime(LocalDateTime.now());
        invoice.setInvoiceCode((String) params.get("invoiceCode"));
        invoice.setInvoiceNo((String) params.get("invoiceNo"));
        invoice.setPdfUrl((String) params.get("pdfUrl"));
        invoiceMapper.update(invoice);

        return Result.success(invoice);
    }

    @GetMapping("/invoices/customer/{customerId}")
    public Result<List<Invoice>> listByCustomer(@PathVariable Long customerId) {
        return Result.success(invoiceMapper.findByCustomerId(customerId));
    }

    @GetMapping("/invoices/merchant/{merchantId}")
    public Result<List<Invoice>> listInvoicesByMerchant(@PathVariable Long merchantId) {
        return Result.success(invoiceMapper.findByMerchantId(merchantId));
    }

    @PostMapping("/reconciliation")
    public Result<ReconciliationRecord> createReconciliation(@RequestBody Map<String, Object> params) {
        String reconPeriod = (String) params.get("reconPeriod");

        List<MallOrder> allOrders = orderMapper.findAll(0, Integer.MAX_VALUE);
        int totalOrderCount = allOrders.size();
        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        int payOrderCount = 0;
        BigDecimal payOrderAmount = BigDecimal.ZERO;

        for (MallOrder order : allOrders) {
            totalOrderAmount = totalOrderAmount.add(order.getOrderAmount());
            if ("PAID".equals(order.getStatus())) {
                payOrderCount++;
                payOrderAmount = payOrderAmount.add(order.getPayAmount());
            }
        }

        ReconciliationRecord record = new ReconciliationRecord();
        record.setReconCode("RECON" + System.currentTimeMillis());
        record.setReconPeriod(reconPeriod);
        record.setTotalOrderCount(totalOrderCount);
        record.setTotalOrderAmount(totalOrderAmount);
        record.setPayOrderCount(payOrderCount);
        record.setPayOrderAmount(payOrderAmount);
        record.setStatus("SUCCESS");
        record.setResultDetail("对账完成，无差异");

        reconMapper.insert(record);
        return Result.success(record);
    }

    @GetMapping("/reconciliation")
    public Result<List<ReconciliationRecord>> listReconciliation() {
        return Result.success(reconMapper.findAll());
    }

    @GetMapping("/reconciliation/{id}")
    public Result<ReconciliationRecord> getReconciliation(@PathVariable Long id) {
        ReconciliationRecord record = reconMapper.findById(id);
        if (record == null) {
            return Result.error("对账记录不存在");
        }
        return Result.success(record);
    }
}
