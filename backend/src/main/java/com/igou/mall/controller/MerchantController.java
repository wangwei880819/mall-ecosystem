package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.dao.MerchantAuditLogMapper;
import com.igou.mall.dao.MerchantQualificationMapper;
import com.igou.mall.dao.ProductMapper;
import com.igou.mall.model.entity.Merchant;
import com.igou.mall.model.entity.MerchantAuditLog;
import com.igou.mall.model.entity.MerchantQualification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.time.Duration;

@RestController
@RequestMapping("/api/merchant")
@CrossOrigin(origins = "*")
public class MerchantController {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private MerchantQualificationMapper qualificationMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private MerchantAuditLogMapper auditLogMapper;

    @GetMapping
    public Result<List<Merchant>> list(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return Result.success(merchantMapper.findAll(page * size, size));
    }

    @GetMapping("/{id}")
    public Result<Merchant> getById(@PathVariable Long id) {
        Merchant merchant = merchantMapper.findById(id);
        if (merchant == null) {
            return Result.error("商户不存在");
        }
        return Result.success(merchant);
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Merchant merchant = merchantMapper.findById(id);
        if (merchant == null) {
            return Result.error("商户不存在");
        }
        List<MerchantQualification> qualifications = qualificationMapper.findByMerchantId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("merchant", merchant);
        result.put("qualifications", qualifications);
        return Result.success(result);
    }

    @PostMapping
    public Result<Merchant> create(@RequestBody Merchant merchant) {
        merchant.setMerchantCode("M" + System.currentTimeMillis());
        merchant.setOnboardingStatus("PENDING");
        merchant.setOnboardingStep(1);
        merchant.setStatus("ACTIVE");
        merchantMapper.insert(merchant);
        return Result.success(merchant);
    }

    @PutMapping("/{id}")
    public Result<Merchant> update(@PathVariable Long id, @RequestBody Merchant merchant) {
        Merchant existing = merchantMapper.findById(id);
        if (existing == null) {
            return Result.error("商户不存在");
        }
        if (merchant.getMerchantName() != null) existing.setMerchantName(merchant.getMerchantName());
        if (merchant.getMerchantType() != null) existing.setMerchantType(merchant.getMerchantType());
        if (merchant.getCreditCode() != null) existing.setCreditCode(merchant.getCreditCode());
        if (merchant.getLegalPerson() != null) existing.setLegalPerson(merchant.getLegalPerson());
        if (merchant.getRegisteredCapital() != null) existing.setRegisteredCapital(merchant.getRegisteredCapital());
        if (merchant.getBusinessScope() != null) existing.setBusinessScope(merchant.getBusinessScope());
        if (merchant.getContactName() != null) existing.setContactName(merchant.getContactName());
        if (merchant.getContactPhone() != null) existing.setContactPhone(merchant.getContactPhone());
        if (merchant.getProvince() != null) existing.setProvince(merchant.getProvince());
        if (merchant.getCity() != null) existing.setCity(merchant.getCity());
        if (merchant.getDistrict() != null) existing.setDistrict(merchant.getDistrict());
        if (merchant.getAddress() != null) existing.setAddress(merchant.getAddress());
        if (merchant.getBankName() != null) existing.setBankName(merchant.getBankName());
        if (merchant.getBankAccount() != null) existing.setBankAccount(merchant.getBankAccount());
        if (merchant.getTaxNumber() != null) existing.setTaxNumber(merchant.getTaxNumber());
        if (merchant.getOnboardingStep() != null) existing.setOnboardingStep(merchant.getOnboardingStep());
        // 先保存旧状态，再设置新状态
        String oldStatus = existing.getOnboardingStatus();
        if (merchant.getOnboardingStatus() != null) existing.setOnboardingStatus(merchant.getOnboardingStatus());
        if (merchant.getRejectReason() != null) existing.setRejectReason(merchant.getRejectReason());
        merchantMapper.update(existing);
        // 检测重新提交：从驳回状态变更为非驳回状态时，记录RESUBMIT日志
        if ("REJECTED".equals(oldStatus) && existing.getOnboardingStatus() != null
                && !"REJECTED".equals(existing.getOnboardingStatus())) {
            MerchantAuditLog resubmitLog = new MerchantAuditLog();
            resubmitLog.setMerchantId(id);
            resubmitLog.setAuditNode("RESUBMIT");
            resubmitLog.setAction("RESUBMIT");
            resubmitLog.setOperator(merchant.getContactName() != null ? merchant.getContactName() : "商户");
            resubmitLog.setComment("商户重新提交申请");
            auditLogMapper.insert(resubmitLog);
        }
        return Result.success(existing);
    }

    @PutMapping("/{id}/status")
    public Result<Merchant> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        Merchant merchant = merchantMapper.findById(id);
        if (merchant == null) {
            return Result.error("商户不存在");
        }
        String status = params.get("status");
        merchant.setStatus(status);
        merchantMapper.update(merchant);
        return Result.success(merchant);
    }

    @PutMapping("/{id}/audit")
    public Result<Merchant> audit(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Merchant merchant = merchantMapper.findById(id);
        if (merchant == null) {
            return Result.error("商户不存在");
        }
        String auditStatus = (String) params.get("auditStatus");
        String rejectReason = (String) params.get("rejectReason");
        String auditNode = (String) params.get("auditNode");
        String onboardingStatus = (String) params.get("onboardingStatus");

        // 保存原始审核节点，用于日志记录（在修改 merchant 之前）
        String originalNode = merchant.getAuditNode();

        if ("APPROVED".equals(auditStatus)) {
            // 多节点审核：按节点推进
            if (auditNode != null) {
                merchant.setAuditNode(auditNode);
            }
            String currentNode = merchant.getAuditNode();
            if (currentNode == null || "QUALIFICATION".equals(currentNode)) {
                merchant.setAuditNode("BUSINESS");
                merchant.setOnboardingStep(2);
                merchant.setOnboardingStatus("REVIEWING");
            } else if ("BUSINESS".equals(currentNode)) {
                merchant.setAuditNode("COMPLIANCE");
                merchant.setOnboardingStep(3);
            } else if ("COMPLIANCE".equals(currentNode)) {
                merchant.setAuditNode("CONTRACT");
                merchant.setOnboardingStep(4);
            } else if ("CONTRACT".equals(currentNode)) {
                merchant.setAuditNode("PAYMENT");
                merchant.setOnboardingStep(5);
            } else if ("PAYMENT".equals(currentNode)) {
                // 支付进件通过 → 已入驻
                merchant.setAuditNode("COMPLETED");
                merchant.setOnboardingStep(8);
                merchant.setOnboardingStatus(onboardingStatus != null ? onboardingStatus : "APPROVED");
                merchant.setAuditNodeDeadline(null);
            } else {
                merchant.setOnboardingStatus("APPROVED");
                merchant.setOnboardingStep(8);
                merchant.setAuditNode("COMPLETED");
            }
            // 设置下一节点截止时间（3个工作日），已完成节点不再设置
            if (!"COMPLETED".equals(merchant.getAuditNode())) {
                merchant.setAuditNodeDeadline(java.time.LocalDateTime.now().plusDays(3));
            }
        } else if ("REJECTED".equals(auditStatus)) {
            merchant.setOnboardingStatus("REJECTED");
            merchant.setRejectReason(rejectReason);
            merchant.setAuditNode(null);
            merchant.setAuditNodeDeadline(null);
        }

        // 保存审核日志（使用原始节点名，确保日志记录的是本次审核的节点）
        MerchantAuditLog log = new MerchantAuditLog();
        log.setMerchantId(id);
        log.setAuditNode(auditNode != null ? auditNode : (originalNode != null ? originalNode : "QUALIFICATION"));
        log.setAction(auditStatus);
        log.setOperator((String) params.getOrDefault("operator", "系统管理员"));
        log.setComment((String) params.getOrDefault("comment", ""));
        log.setRejectReason(rejectReason);
        auditLogMapper.insert(log);

        merchantMapper.update(merchant);
        return Result.success(merchant);
    }

    @GetMapping("/{merchantId}/audit-logs")
    public Result<List<MerchantAuditLog>> getAuditLogs(@PathVariable Long merchantId) {
        return Result.success(auditLogMapper.findByMerchantId(merchantId));
    }

    @PostMapping("/{id}/qualification")
    public Result<MerchantQualification> addQualification(@PathVariable Long id,
                                                          @RequestBody MerchantQualification qualification) {
        qualification.setMerchantId(id);
        qualification.setAuditStatus("PENDING");
        qualificationMapper.insert(qualification);
        return Result.success(qualification);
    }

    @PutMapping("/qualification/{qid}/audit")
    public Result<MerchantQualification> auditQualification(@PathVariable Long qid,
                                                            @RequestBody Map<String, Object> params) {
        MerchantQualification qualification = qualificationMapper.findById(qid);
        if (qualification == null) {
            return Result.error("资质不存在");
        }
        qualification.setAuditStatus((String) params.get("auditStatus"));
        qualification.setAuditor((String) params.get("auditor"));
        qualification.setAuditComment((String) params.get("auditComment"));
        qualificationMapper.update(qualification);
        return Result.success(qualification);
    }

    @GetMapping("/status/{status}")
    public Result<List<Merchant>> listByStatus(@PathVariable String status) {
        return Result.success(merchantMapper.findByStatus(status));
    }

    @GetMapping("/onboarding/{status}")
    public Result<List<Merchant>> listByOnboardingStatus(@PathVariable String status) {
        return Result.success(merchantMapper.findByOnboardingStatus(status));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        merchantMapper.delete(id);
        return Result.success("删除成功");
    }

    /** 下架商户：商户本身+所有在售商品设为下架 */
    @PutMapping("/{id}/offline")
    public Result<Map<String, Object>> offShelf(@PathVariable Long id) {
        Merchant merchant = merchantMapper.findById(id);
        if (merchant == null) return Result.error("商户不存在");
        merchantMapper.offShelf(id);
        int productCount = productMapper.offShelfByMerchantId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("merchantId", id);
        result.put("merchantStatus", "OFF_SHELF");
        result.put("productsOffShelf", productCount);
        return Result.success(result);
    }

    /** 物理删除商户及关联商品 */
    @DeleteMapping("/{id}/force")
    public Result<Map<String, Object>> forceDelete(@PathVariable Long id) {
        Merchant merchant = merchantMapper.findById(id);
        if (merchant == null) return Result.error("商户不存在");
        int productCount = productMapper.deleteByMerchantId(id);
        merchantMapper.forceDelete(id);
        Map<String, Object> result = new HashMap<>();
        result.put("merchantId", id);
        result.put("merchantName", merchant.getMerchantName());
        result.put("productsDeleted", productCount);
        return Result.success(result);
    }

    /** 业务复审列表 */
    @GetMapping("/business-audit")
    public Result<List<Merchant>> businessAuditList() {
        return Result.success(merchantMapper.findByAuditNode("BUSINESS"));
    }

    /** 按审核节点获取商户列表 */
    @GetMapping("/node/{node}")
    public Result<List<Merchant>> byNode(@PathVariable String node) {
        return Result.success(merchantMapper.findByAuditNode(node));
    }

    /** 批量导入商户 */
    @PostMapping("/batch")
    public Result<Map<String, Object>> batchImport(@RequestBody List<Map<String, Object>> batch) {
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> details = new ArrayList<>();
        
        for (Map<String, Object> row : batch) {
            try {
                Merchant m = new Merchant();
                m.setMerchantCode("M" + System.currentTimeMillis() + "_" + successCount);
                m.setMerchantName((String) row.getOrDefault("merchantName", "未知商户"));
                m.setMerchantType((String) row.getOrDefault("merchantType", "DIGITAL"));
                m.setCreditCode((String) row.getOrDefault("creditCode", ""));
                m.setLegalPerson((String) row.getOrDefault("legalPerson", ""));
                m.setContactName((String) row.getOrDefault("contactName", ""));
                m.setContactPhone((String) row.getOrDefault("contactPhone", ""));
                m.setIndustry((String) row.getOrDefault("industry", ""));
                m.setCategoryMatch((String) row.getOrDefault("categoryMatch", ""));
                m.setCreditScore((Integer) row.getOrDefault("creditScore", 80));
                m.setOnboardingStep(1);
                m.setOnboardingStatus("PENDING");
                m.setAuditNode("QUALIFICATION");
                m.setAuditNodeDeadline(java.time.LocalDateTime.now().plusDays(3));
                m.setStatus("ACTIVE");
                merchantMapper.insert(m);
                successCount++;
                details.add(Map.of("name", m.getMerchantName(), "status", "success", "code", m.getMerchantCode()));
            } catch (Exception e) {
                failCount++;
                details.add(Map.of("name", row.getOrDefault("merchantName", "未知"), "status", "fail", "reason", e.getMessage()));
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", batch.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("details", details);
        return Result.success(result);
    }

    /** 超时预警：查询超过审核截止时间的商户 */
    @GetMapping("/timeout-warnings")
    public Result<List<Map<String, Object>>> timeoutWarnings() {
        List<Merchant> pending = merchantMapper.findByOnboardingStatus("REVIEWING");
        List<Map<String, Object>> warnings = new ArrayList<>();
        for (Merchant m : pending) {
            if (m.getAuditNodeDeadline() != null && m.getAuditNodeDeadline().isBefore(java.time.LocalDateTime.now())) {
                Map<String, Object> warn = new HashMap<>();
                warn.put("id", m.getId());
                warn.put("merchantName", m.getMerchantName());
                warn.put("auditNode", m.getAuditNode());
                warn.put("deadline", m.getAuditNodeDeadline().toString());
                warn.put("overdueDays", java.time.Duration.between(m.getAuditNodeDeadline(), java.time.LocalDateTime.now()).toDays());
                warn.put("riskLevel", m.getRiskLevel());
                warnings.add(warn);
            }
        }
        return Result.success(warnings);
    }
}