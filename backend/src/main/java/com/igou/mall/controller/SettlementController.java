package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.model.entity.Settlement;
import com.igou.mall.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settlement")
@CrossOrigin(origins = "*")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @GetMapping("/list")
    public Result<Map<String, Object>> getList(@RequestParam(defaultValue = "0") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) Long merchantId,
                                                @RequestParam(required = false) String status) {
        Map<String, Object> result = settlementService.getSettlementList(page, size, merchantId, status);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Settlement> getById(@PathVariable Long id) {
        Settlement settlement = settlementService.getSettlementById(id);
        if (settlement == null) {
            return Result.error("结算单不存在");
        }
        return Result.success(settlement);
    }

    @PostMapping
    public Result<Settlement> create(@RequestBody Settlement settlement) {
        Settlement result = settlementService.createSimpleSettlement(settlement);
        return Result.success(result);
    }

    @PutMapping("/{id}/confirm")
    public Result<Settlement> confirm(@PathVariable Long id) {
        Settlement settlement = settlementService.confirmSettlement(id);
        if (settlement == null) {
            return Result.error("结算单不存在");
        }
        return Result.success(settlement);
    }

    @PutMapping("/{id}/pay")
    public Result<Settlement> pay(@PathVariable Long id) {
        Settlement settlement = settlementService.paySettlement(id);
        if (settlement == null) {
            return Result.error("结算单不存在");
        }
        return Result.success(settlement);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics(@RequestParam Long merchantId) {
        return Result.success(settlementService.getStatistics(merchantId));
    }
}
