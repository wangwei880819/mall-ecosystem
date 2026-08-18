package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.DepositMapper;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.model.entity.Deposit;
import com.igou.mall.model.entity.Merchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/deposit")
@CrossOrigin(origins = "*")
public class DepositController {

    @Autowired
    private DepositMapper depositMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @GetMapping("/list")
    public Result<Map<String, Object>> getList(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) Long merchantId,
                                               @RequestParam(required = false) String status) {
        List<Deposit> list = depositMapper.findPage(page * size, size, status, merchantId);
        int total = depositMapper.count(status, merchantId);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Deposit d : list) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", d.getId());
            record.put("depositCode", d.getDepositCode());
            record.put("merchantId", d.getMerchantId());
            record.put("depositType", d.getDepositType());
            record.put("amount", d.getAmount());
            record.put("balance", d.getBalance());
            record.put("payMethod", d.getPayMethod());
            record.put("payNo", d.getPayNo());
            record.put("status", d.getStatus());
            record.put("reason", d.getReason());
            record.put("approver", d.getApprover());
            record.put("approveTime", d.getApproveTime());
            record.put("createTime", d.getCreateTime());
            Merchant merchant = merchantMapper.findById(d.getMerchantId());
            record.put("merchantName", merchant != null ? merchant.getMerchantName() : "商户" + d.getMerchantId());
            records.add(record);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", total);
        return Result.success(result);
    }

    @GetMapping("/merchant/{merchantId}")
    public Result<Map<String, Object>> getByMerchant(@PathVariable Long merchantId) {
        List<Deposit> list = depositMapper.findByMerchantId(merchantId);
        BigDecimal paid = depositMapper.sumPaidByMerchantId(merchantId);
        BigDecimal refunded = depositMapper.sumRefundedByMerchantId(merchantId);
        BigDecimal deducted = depositMapper.sumDeductedByMerchantId(merchantId);
        BigDecimal balance = paid.subtract(refunded).subtract(deducted);

        Map<String, Object> result = new HashMap<>();
        result.put("records", list);
        result.put("totalPaid", paid);
        result.put("totalRefunded", refunded);
        result.put("totalDeducted", deducted);
        result.put("currentBalance", balance);
        return Result.success(result);
    }

    @PostMapping("/pay")
    public Result<Deposit> payDeposit(@RequestBody Map<String, Object> params) {
        Long merchantId = Long.valueOf(params.get("merchantId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());

        Deposit deposit = new Deposit();
        deposit.setDepositCode("DEP" + System.currentTimeMillis());
        deposit.setMerchantId(merchantId);
        deposit.setDepositType("PAY");
        deposit.setAmount(amount);
        deposit.setBalance(amount);
        deposit.setPayMethod((String) params.getOrDefault("payMethod", "BANK_TRANSFER"));
        deposit.setPayNo((String) params.getOrDefault("payNo", ""));
        deposit.setStatus("PENDING");
        deposit.setReason((String) params.getOrDefault("reason", "商户入驻保证金"));
        deposit.setCreateTime(LocalDateTime.now());
        depositMapper.insert(deposit);
        return Result.success(deposit);
    }

    @PutMapping("/{id}/approve")
    public Result<Deposit> approve(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Deposit deposit = depositMapper.findById(id);
        if (deposit == null) return Result.error("保证金记录不存在");
        deposit.setStatus("COMPLETED");
        deposit.setApprover((String) params.getOrDefault("approver", "admin"));
        deposit.setApproveTime(LocalDateTime.now());
        depositMapper.updateStatus(deposit);
        return Result.success(deposit);
    }

    @PutMapping("/{id}/reject")
    public Result<Deposit> reject(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Deposit deposit = depositMapper.findById(id);
        if (deposit == null) return Result.error("保证金记录不存在");
        deposit.setStatus("REJECTED");
        deposit.setReason((String) params.getOrDefault("reason", ""));
        depositMapper.updateStatus(deposit);
        return Result.success(deposit);
    }

    @PostMapping("/refund")
    public Result<Deposit> refundDeposit(@RequestBody Map<String, Object> params) {
        Long merchantId = Long.valueOf(params.get("merchantId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());

        BigDecimal paid = depositMapper.sumPaidByMerchantId(merchantId);
        BigDecimal refunded = depositMapper.sumRefundedByMerchantId(merchantId);
        BigDecimal deducted = depositMapper.sumDeductedByMerchantId(merchantId);
        BigDecimal currentBalance = paid.subtract(refunded).subtract(deducted);

        if (amount.compareTo(currentBalance) > 0) {
            return Result.error("退还金额超过当前保证金余额");
        }

        Deposit deposit = new Deposit();
        deposit.setDepositCode("REF" + System.currentTimeMillis());
        deposit.setMerchantId(merchantId);
        deposit.setDepositType("REFUND");
        deposit.setAmount(amount);
        deposit.setBalance(currentBalance.subtract(amount));
        deposit.setStatus("PENDING");
        deposit.setReason((String) params.getOrDefault("reason", "保证金退还"));
        deposit.setCreateTime(LocalDateTime.now());
        depositMapper.insert(deposit);
        return Result.success(deposit);
    }

    @PostMapping("/deduct")
    public Result<Deposit> deductDeposit(@RequestBody Map<String, Object> params) {
        Long merchantId = Long.valueOf(params.get("merchantId").toString());
        BigDecimal amount = new BigDecimal(params.get("amount").toString());

        BigDecimal paid = depositMapper.sumPaidByMerchantId(merchantId);
        BigDecimal refunded = depositMapper.sumRefundedByMerchantId(merchantId);
        BigDecimal deducted = depositMapper.sumDeductedByMerchantId(merchantId);
        BigDecimal currentBalance = paid.subtract(refunded).subtract(deducted);

        if (amount.compareTo(currentBalance) > 0) {
            return Result.error("扣除金额超过当前保证金余额");
        }

        Deposit deposit = new Deposit();
        deposit.setDepositCode("DED" + System.currentTimeMillis());
        deposit.setMerchantId(merchantId);
        deposit.setDepositType("DEDUCT");
        deposit.setAmount(amount);
        deposit.setBalance(currentBalance.subtract(amount));
        deposit.setStatus("PENDING");
        deposit.setReason((String) params.getOrDefault("reason", "保证金扣除"));
        deposit.setCreateTime(LocalDateTime.now());
        depositMapper.insert(deposit);
        return Result.success(deposit);
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalPendingCount", depositMapper.count("PENDING", null));
        result.put("totalCompletedCount", depositMapper.count("COMPLETED", null));
        result.put("totalCount", depositMapper.count(null, null));
        return Result.success(result);
    }
}