package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.ContractMapper;
import com.igou.mall.dao.ContractTemplateMapper;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.model.entity.Contract;
import com.igou.mall.model.entity.ContractTemplate;
import com.igou.mall.model.entity.Merchant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/contract")
@CrossOrigin(origins = "*")
public class ContractController {

    @Autowired
    private ContractMapper contractMapper;

    @Autowired
    private ContractTemplateMapper templateMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    // ========== 合同管理 ==========

    @GetMapping("/list")
    public Result<Map<String, Object>> getList(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) Long merchantId,
                                               @RequestParam(required = false) String status) {
        List<Contract> list = contractMapper.findPage(page * size, size, status, merchantId);
        int total = contractMapper.count(status, merchantId);
        List<Map<String, Object>> records = new ArrayList<>();
        for (Contract c : list) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", c.getId());
            record.put("contractCode", c.getContractCode());
            record.put("merchantId", c.getMerchantId());
            record.put("contractType", c.getContractType());
            record.put("contractTitle", c.getContractTitle());
            record.put("commissionRate", c.getCommissionRate());
            record.put("depositAmount", c.getDepositAmount());
            record.put("platformSigned", c.getPlatformSigned());
            record.put("merchantSigned", c.getMerchantSigned());
            record.put("status", c.getStatus());
            record.put("effectiveDate", c.getEffectiveDate());
            record.put("expireDate", c.getExpireDate());
            record.put("createTime", c.getCreateTime());
            Merchant merchant = merchantMapper.findById(c.getMerchantId());
            record.put("merchantName", merchant != null ? merchant.getMerchantName() : "商户" + c.getMerchantId());
            records.add(record);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", records);
        result.put("total", total);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Contract> getById(@PathVariable Long id) {
        Contract contract = contractMapper.findById(id);
        if (contract == null) return Result.error("合同不存在");
        return Result.success(contract);
    }

    @GetMapping("/merchant/{merchantId}")
    public Result<List<Contract>> getByMerchant(@PathVariable Long merchantId) {
        return Result.success(contractMapper.findByMerchantId(merchantId));
    }

    @PostMapping
    public Result<Contract> create(@RequestBody Map<String, Object> params) {
        Contract contract = new Contract();
        contract.setContractCode("CT" + System.currentTimeMillis());
        contract.setMerchantId(Long.valueOf(params.get("merchantId").toString()));
        contract.setTemplateId(params.get("templateId") != null ? Long.valueOf(params.get("templateId").toString()) : null);
        contract.setContractType((String) params.getOrDefault("contractType", "SETTLEMENT"));
        contract.setContractTitle((String) params.getOrDefault("contractTitle", "商户入驻合作协议"));
        contract.setContractContent((String) params.getOrDefault("contractContent", ""));
        contract.setFileUrl((String) params.getOrDefault("fileUrl", ""));
        contract.setCommissionRate(params.get("commissionRate") != null ? new java.math.BigDecimal(params.get("commissionRate").toString()) : null);
        contract.setDepositAmount(params.get("depositAmount") != null ? new java.math.BigDecimal(params.get("depositAmount").toString()) : null);
        contract.setStatus("DRAFT");
        contract.setRemark((String) params.getOrDefault("remark", ""));
        contract.setCreateTime(LocalDateTime.now());
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.insert(contract);
        return Result.success(contract);
    }

    @PutMapping("/{id}")
    public Result<Contract> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Contract contract = contractMapper.findById(id);
        if (contract == null) return Result.error("合同不存在");

        if (params.containsKey("contractTitle")) contract.setContractTitle((String) params.get("contractTitle"));
        if (params.containsKey("contractContent")) contract.setContractContent((String) params.get("contractContent"));
        if (params.containsKey("fileUrl")) contract.setFileUrl((String) params.get("fileUrl"));
        if (params.containsKey("signUrl")) contract.setSignUrl((String) params.get("signUrl"));
        if (params.containsKey("commissionRate"))
            contract.setCommissionRate(new java.math.BigDecimal(params.get("commissionRate").toString()));
        if (params.containsKey("depositAmount"))
            contract.setDepositAmount(new java.math.BigDecimal(params.get("depositAmount").toString()));
        if (params.containsKey("status")) contract.setStatus((String) params.get("status"));
        if (params.containsKey("effectiveDate"))
            contract.setEffectiveDate(java.time.LocalDate.parse((String) params.get("effectiveDate")));
        if (params.containsKey("expireDate"))
            contract.setExpireDate(java.time.LocalDate.parse((String) params.get("expireDate")));
        if (params.containsKey("remark")) contract.setRemark((String) params.get("remark"));
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.update(contract);
        return Result.success(contract);
    }

    @PutMapping("/{id}/platform-sign")
    public Result<Contract> platformSign(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Contract contract = contractMapper.findById(id);
        if (contract == null) return Result.error("合同不存在");
        String signer = (String) params.getOrDefault("signer", "admin");
        contractMapper.platformSign(id, signer);
        return Result.success(contractMapper.findById(id));
    }

    @PutMapping("/{id}/merchant-sign")
    public Result<Contract> merchantSign(@PathVariable Long id) {
        Contract contract = contractMapper.findById(id);
        if (contract == null) return Result.error("合同不存在");
        contractMapper.merchantSign(id);
        return Result.success(contractMapper.findById(id));
    }

    @PutMapping("/{id}/submit")
    public Result<Contract> submitForSign(@PathVariable Long id) {
        Contract contract = contractMapper.findById(id);
        if (contract == null) return Result.error("合同不存在");
        contract.setStatus("PENDING_SIGN");
        contract.setUpdateTime(LocalDateTime.now());
        contractMapper.update(contract);
        return Result.success(contract);
    }

    // ========== 合同模板管理 ==========

    @GetMapping("/templates")
    public Result<List<ContractTemplate>> getTemplates(@RequestParam(required = false) String type) {
        if (type != null) {
            return Result.success(templateMapper.findPage(0, 100, type));
        }
        return Result.success(templateMapper.findAllActive());
    }

    @GetMapping("/templates/{id}")
    public Result<ContractTemplate> getTemplate(@PathVariable Long id) {
        ContractTemplate template = templateMapper.findById(id);
        if (template == null) return Result.error("模板不存在");
        return Result.success(template);
    }

    @PostMapping("/templates")
    public Result<ContractTemplate> createTemplate(@RequestBody ContractTemplate template) {
        template.setTemplateCode("TPL" + System.currentTimeMillis());
        template.setStatus("ACTIVE");
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        templateMapper.insert(template);
        return Result.success(template);
    }

    @PutMapping("/templates/{id}")
    public Result<ContractTemplate> updateTemplate(@PathVariable Long id, @RequestBody ContractTemplate template) {
        ContractTemplate existing = templateMapper.findById(id);
        if (existing == null) return Result.error("模板不存在");
        existing.setTemplateName(template.getTemplateName());
        existing.setTemplateType(template.getTemplateType());
        existing.setContent(template.getContent());
        existing.setVariables(template.getVariables());
        existing.setUpdateTime(LocalDateTime.now());
        templateMapper.update(existing);
        return Result.success(existing);
    }

    @PutMapping("/templates/{id}/toggle")
    public Result<ContractTemplate> toggleTemplate(@PathVariable Long id, @RequestBody Map<String, String> params) {
        ContractTemplate template = templateMapper.findById(id);
        if (template == null) return Result.error("模板不存在");
        template.setStatus(params.get("status"));
        template.setUpdateTime(LocalDateTime.now());
        templateMapper.update(template);
        return Result.success(template);
    }

    // ========== 概览统计 ==========

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> result = new HashMap<>();
        result.put("draftCount", contractMapper.count("DRAFT", null));
        result.put("pendingSignCount", contractMapper.count("PENDING_SIGN", null));
        result.put("signedCount", contractMapper.count("SIGNED", null));
        result.put("totalCount", contractMapper.count(null, null));
        result.put("templateCount", templateMapper.findAllActive().size());
        return Result.success(result);
    }
}