package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.CrmLeadMapper;
import com.igou.mall.dao.CrmFollowUpMapper;
import com.igou.mall.model.entity.CrmLead;
import com.igou.mall.model.entity.CrmFollowUp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/crm")
@CrossOrigin(origins = "*")
public class CrmController {

    @Autowired
    private CrmLeadMapper leadMapper;

    @Autowired
    private CrmFollowUpMapper followUpMapper;

    // ========== 线索管理 ==========

    @GetMapping("/leads")
    public Result<Map<String, Object>> getLeads(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String assignedTo,
                                                @RequestParam(required = false) String keyword) {
        List<CrmLead> list = leadMapper.findPage(page * size, size, status, assignedTo, keyword);
        int total = leadMapper.count(status, assignedTo, keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return Result.success(result);
    }

    @GetMapping("/leads/{id}")
    public Result<CrmLead> getLead(@PathVariable Long id) {
        CrmLead lead = leadMapper.findById(id);
        if (lead == null) return Result.error("线索不存在");
        return Result.success(lead);
    }

    @PostMapping("/leads")
    public Result<CrmLead> createLead(@RequestBody CrmLead lead) {
        lead.setLeadCode("LD" + System.currentTimeMillis());
        lead.setStatus("NEW");
        lead.setCreateTime(LocalDateTime.now());
        lead.setUpdateTime(LocalDateTime.now());
        leadMapper.insert(lead);
        return Result.success(lead);
    }

    @PutMapping("/leads/{id}")
    public Result<CrmLead> updateLead(@PathVariable Long id, @RequestBody CrmLead lead) {
        CrmLead existing = leadMapper.findById(id);
        if (existing == null) return Result.error("线索不存在");

        if (lead.getCompanyName() != null) existing.setCompanyName(lead.getCompanyName());
        if (lead.getBrandName() != null) existing.setBrandName(lead.getBrandName());
        if (lead.getIndustry() != null) existing.setIndustry(lead.getIndustry());
        if (lead.getContactName() != null) existing.setContactName(lead.getContactName());
        if (lead.getContactPhone() != null) existing.setContactPhone(lead.getContactPhone());
        if (lead.getContactEmail() != null) existing.setContactEmail(lead.getContactEmail());
        if (lead.getSource() != null) existing.setSource(lead.getSource());
        if (lead.getIntentionLevel() != null) existing.setIntentionLevel(lead.getIntentionLevel());
        if (lead.getEstimatedGmv() != null) existing.setEstimatedGmv(lead.getEstimatedGmv());
        if (lead.getAssignedTo() != null) existing.setAssignedTo(lead.getAssignedTo());
        if (lead.getRemark() != null) existing.setRemark(lead.getRemark());
        existing.setUpdateTime(LocalDateTime.now());
        leadMapper.update(existing);
        return Result.success(existing);
    }

    @PutMapping("/leads/{id}/status")
    public Result<CrmLead> updateLeadStatus(@PathVariable Long id, @RequestBody Map<String, String> params) {
        CrmLead lead = leadMapper.findById(id);
        if (lead == null) return Result.error("线索不存在");

        String newStatus = params.get("status");
        lead.setStatus(newStatus);
        if ("LOST".equals(newStatus)) {
            lead.setLostReason(params.get("lostReason"));
        }
        lead.setUpdateTime(LocalDateTime.now());
        leadMapper.update(lead);
        return Result.success(lead);
    }

    // ========== 跟进记录 ==========

    @GetMapping("/leads/{leadId}/follow-ups")
    public Result<List<CrmFollowUp>> getFollowUps(@PathVariable Long leadId) {
        return Result.success(followUpMapper.findByLeadId(leadId));
    }

    @PostMapping("/leads/{leadId}/follow-ups")
    public Result<CrmFollowUp> addFollowUp(@PathVariable Long leadId, @RequestBody CrmFollowUp followUp) {
        followUp.setLeadId(leadId);
        followUp.setCreateTime(LocalDateTime.now());
        followUpMapper.insert(followUp);

        // 自动更新线索状态为洽谈中
        CrmLead lead = leadMapper.findById(leadId);
        if (lead != null && "NEW".equals(lead.getStatus())) {
            leadMapper.updateStatus(leadId, "CONTACTING");
        }
        return Result.success(followUp);
    }

    // ========== 转化漏斗 ==========

    @GetMapping("/funnel")
    public Result<Map<String, Object>> getFunnel() {
        Map<String, Object> funnel = new HashMap<>();
        funnel.put("newCount", leadMapper.count("NEW", null, null));
        funnel.put("contactingCount", leadMapper.count("CONTACTING", null, null));
        funnel.put("negotiatingCount", leadMapper.count("NEGOTIATING", null, null));
        funnel.put("intentConfirmedCount", leadMapper.count("INTENT_CONFIRMED", null, null));
        funnel.put("convertedCount", leadMapper.count("CONVERTED", null, null));
        funnel.put("lostCount", leadMapper.count("LOST", null, null));
        funnel.put("totalCount", leadMapper.count(null, null, null));
        return Result.success(funnel);
    }
}