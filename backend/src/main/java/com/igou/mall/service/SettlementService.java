package com.igou.mall.service;

import com.igou.mall.dao.SettlementMapper;
import com.igou.mall.dao.SettlementDetailMapper;
import com.igou.mall.dao.MallOrderMapper;
import com.igou.mall.model.entity.Settlement;
import com.igou.mall.model.entity.SettlementDetail;
import com.igou.mall.model.entity.MallOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettlementService {

    @Autowired
    private SettlementMapper settlementMapper;

    @Autowired
    private SettlementDetailMapper detailMapper;

    @Autowired
    private MallOrderMapper orderMapper;

    @Transactional
    public Map<String, Object> getSettlementList(Integer page, Integer size, Long merchantId, String status) {
        int offset = page * size;
        List<Settlement> settlements = settlementMapper.findPage(offset, size, merchantId, status);
        Integer total = settlementMapper.count(merchantId, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", settlements);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @Transactional
    public Settlement getSettlementById(Long id) {
        return settlementMapper.findById(id);
    }

    @Transactional
    public Map<String, Object> getSettlementDetail(Long id) {
        Settlement settlement = settlementMapper.findById(id);
        if (settlement == null) {
            return null;
        }
        List<SettlementDetail> details = detailMapper.findBySettleId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("settlement", settlement);
        result.put("details", details);
        return result;
    }

    @Transactional
    public Settlement createSettlement(Map<String, Object> params) {
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

        return settlement;
    }

    @Transactional
    public Settlement createSimpleSettlement(Settlement settlement) {
        settlement.setSettleCode("SETTLE" + System.currentTimeMillis());
        settlement.setStatus("PENDING");
        settlement.setCreateTime(LocalDateTime.now());
        if (settlement.getItemCount() == null) settlement.setItemCount(0);
        settlementMapper.insert(settlement);
        return settlement;
    }

    @Transactional
    public Settlement confirmSettlement(Long id) {
        Settlement settlement = settlementMapper.findById(id);
        if (settlement == null) {
            return null;
        }
        settlement.setStatus("COMPLETED");
        settlement.setApproveTime(LocalDateTime.now());
        settlementMapper.update(settlement);
        return settlement;
    }

    @Transactional
    public Settlement approveSettlement(Long id, String approver) {
        Settlement settlement = settlementMapper.findById(id);
        if (settlement == null) {
            return null;
        }
        settlement.setStatus("COMPLETED");
        settlement.setApprover(approver);
        settlement.setApproveTime(LocalDateTime.now());
        settlementMapper.update(settlement);
        return settlement;
    }

    @Transactional
    public Settlement paySettlement(Long id) {
        Settlement settlement = settlementMapper.findById(id);
        if (settlement == null) {
            return null;
        }
        settlement.setStatus("PAID");
        settlementMapper.update(settlement);
        return settlement;
    }

    @Transactional
    public List<Settlement> listByMerchant(Long merchantId) {
        return settlementMapper.findPage(0, 100, merchantId, null);
    }

    @Transactional
    public List<Settlement> listByStatus(String status) {
        return settlementMapper.findPage(0, 100, null, status);
    }

    @Transactional
    public Map<String, Object> getStatistics(Long merchantId) {
        Map<String, Object> stats = new HashMap<>();
        BigDecimal totalAmount = settlementMapper.sumAmount(merchantId, "COMPLETED");
        BigDecimal pendingAmount = settlementMapper.sumAmount(merchantId, "PENDING");
        Integer paidCount = settlementMapper.count(merchantId, "COMPLETED");
        Integer pendingCount = settlementMapper.count(merchantId, "PENDING");

        stats.put("totalAmount", totalAmount != null ? totalAmount : BigDecimal.ZERO);
        stats.put("pendingAmount", pendingAmount != null ? pendingAmount : BigDecimal.ZERO);
        stats.put("paidCount", paidCount != null ? paidCount : 0);
        stats.put("pendingCount", pendingCount != null ? pendingCount : 0);
        return stats;
    }
}
