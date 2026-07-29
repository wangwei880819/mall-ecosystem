package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.EvaluationMapper;
import com.igou.mall.model.entity.Evaluation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 服务能力Controller — DEMO演示项3：多维度订单评价支撑体系（1分）
 * 提供五维度评价体系、评价列表、商户评价考核、AI内容审核等API
 */
@RestController
@RequestMapping("/api/service")
public class ServiceController {

    @Autowired private EvaluationMapper evaluationMapper;

    /** 五维度评价体系 */
    @GetMapping("/evaluation/dimensions")
    public Result<List<Map<String, Object>>> dimensions() {
        return Result.success(List.of(
            dim("商品质量", 4.65, 89230),
            dim("配送速度", 4.52, 85120),
            dim("客服服务", 4.71, 78930),
            dim("售后体验", 4.43, 65230),
            dim("性价比", 4.58, 88950)
        ));
    }

    /** 评价列表 */
    @GetMapping("/evaluations")
    public Result<List<Evaluation>> evaluations(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return Result.success(evaluationMapper.findAll(page * size, size));
    }

    /** 商户评价考核排名 */
    @GetMapping("/evaluation/merchant-scores")
    public Result<List<Map<String, Object>>> merchantScores() {
        return Result.success(List.of(
            score("腾讯", 4.82, 15632, "98.5%", "+0.3%", "A级"),
            score("美团", 4.71, 23456, "97.2%", "+0.1%", "B级"),
            score("网易云音乐", 4.65, 8923, "96.8%", "-0.2%", "B级"),
            score("哔哩哔哩", 4.89, 11234, "99.1%", "+0.5%", "A级"),
            score("爱奇艺", 4.23, 6789, "92.3%", "-0.8%", "C级")
        ));
    }

    /** AI内容审核统计 */
    @GetMapping("/evaluation/ai-audit")
    public Result<Map<String, Object>> aiAuditStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("autoPassRate", "87.3%");
        data.put("manualReviewRate", "10.2%");
        data.put("blockedRate", "2.5%");
        data.put("desc", "AI+人工双轨审核：AI可信通过自动放行、AI可疑推送人工复核、AI明确违规自动屏蔽");
        return Result.success(data);
    }

    /** 评价考核闭环 */
    @GetMapping("/evaluation/cycle")
    public Result<List<Map<String, String>>> evaluationCycle() {
        return Result.success(List.of(
            Map.of("step", "评价采集", "desc", "多触点推送邀请"),
            Map.of("step", "评价互动", "desc", "回复+追问+晒单"),
            Map.of("step", "内容审核", "desc", "AI+人工双轨"),
            Map.of("step", "考核互通", "desc", "指标映射商户分级"),
            Map.of("step", "运营调整", "desc", "佣金/权重联动"),
            Map.of("step", "服务提升", "desc", "正向驱动闭环")
        ));
    }

    private Map<String, Object> dim(String name, double score, int total) {
        return Map.of("name", name, "score", score, "total", total);
    }

    private Map<String, Object> score(String merchant, double avg, int total, String respRate, String trend, String grade) {
        Map<String, Object> m = new HashMap<>();
        m.put("merchant", merchant);
        m.put("avgScore", avg);
        m.put("totalReviews", total);
        m.put("responseRate", respRate);
        m.put("trend", trend);
        m.put("grade", grade);
        return m;
    }
}
