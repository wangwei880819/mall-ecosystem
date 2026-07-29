package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 统一门户Controller — DEMO演示项1：多平台统一支撑能力展示（1分）
 * 提供SSO认证、多平台整合、四个统一、运营看板、待办/预警等API
 */
@RestController
@RequestMapping("/api/portal")
public class PortalController {

    @Autowired private MerchantMapper merchantMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private SettlementMapper settlementMapper;
    @Autowired private AuditRecordMapper auditRecordMapper;
    @Autowired private EvaluationMapper evaluationMapper;

    /** 运营数据看板 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("merchantTotal", merchantMapper.count());
        data.put("pendingSettlements", settlementMapper.count(null, "PENDING"));
        data.put("highRiskAudits", 0);
        data.put("negativeReviews", evaluationMapper.negativeCount());
        return Result.success(data);
    }

    /** 多平台整合状态 */
    @GetMapping("/platforms")
    public Result<List<Map<String, String>>> platforms() {
        return Result.success(List.of(
            Map.of("name", "生态合作平台", "url", "qy.10086.cn", "status", "已整合", "desc", "16000+品牌"),
            Map.of("name", "积分商城后台", "url", "jifen.10086.cn", "status", "已整合", "desc", "积分兑换管理"),
            Map.of("name", "权益超市后台", "url", "quan.10086.cn", "status", "已整合", "desc", "数字权益运营"),
            Map.of("name", "泛全联盟平台", "url", "fanquan.10086.cn", "status", "已整合", "desc", "联盟合作管理"),
            Map.of("name", "风控稽核管理平台", "url", "risk.10086.cn", "status", "已整合", "desc", "电商风控稽核审计"),
            Map.of("name", "工单管理系统", "url", "gongdan.10086.cn", "status", "已整合", "desc", "客诉工单处理"),
            Map.of("name", "评价管理系统", "url", "pingjia.10086.cn", "status", "已整合", "desc", "商户评价考核")
        ));
    }

    /** 四个统一能力 */
    @GetMapping("/unified")
    public Result<List<Map<String, String>>> unifiedCapabilities() {
        return Result.success(List.of(
            Map.of("dimension", "账号统一", "desc", "一次登录·全系统通行", "tech", "SSO认证中心 OAuth2.0+4A"),
            Map.of("dimension", "入口统一", "desc", "统一门户·千人千面", "tech", "角色动态生成导航菜单"),
            Map.of("dimension", "权限统一", "desc", "一处配置·全系统生效", "tech", "统一RBAC模型 5级角色体系"),
            Map.of("dimension", "入驻统一", "desc", "一套标准·一次审核", "tech", "八节点标准化流程")
        ));
    }

    /** 统一能力层 */
    @GetMapping("/capabilities")
    public Result<List<Map<String, String>>> capabilities() {
        return Result.success(List.of(
            Map.of("name", "统一数据底座", "desc", "汇聚6大系统核心数据，形成商户/商品/订单/结算/评价五大统一数据模型"),
            Map.of("name", "统一能力对接", "desc", "API网关统一管理所有外围系统交互，统一鉴权/限流/监控"),
            Map.of("name", "统一订购服务", "desc", "一站式订购体验，支持AI豆抵扣，订购数据统一归集")
        ));
    }
}
