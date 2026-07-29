package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.MerchantMapper;
import com.igou.mall.dao.ProductMapper;
import com.igou.mall.model.entity.Merchant;
import com.igou.mall.model.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 生态入驻与商品引入Controller — DEMO演示项2（1分）
 * 提供商户入驻全流程、商品引入全流程、两级选品委员会等API
 */
@RestController
@RequestMapping("/api/ecosystem")
public class EcosystemController {

    @Autowired private MerchantMapper merchantMapper;
    @Autowired private ProductMapper productMapper;

    /** 商户入驻八节点流程 */
    @GetMapping("/onboarding/steps")
    public Result<List<Map<String, Object>>> onboardingSteps() {
        return Result.success(List.of(
            step(1, "商户申请", "填写品牌信息与合作意向"),
            step(2, "资质初审", "OCR识别 + NLP校验 + AI风险预判"),
            step(3, "业务复审", "品类适配度、合作模式审核"),
            step(4, "合同签署", "电子合同模板 + AI智能质检"),
            step(5, "支付进件", "结算账户配置 + 支付渠道开通"),
            step(6, "选品审批", "两级选品委员会审批"),
            step(7, "商品录入", "AI辅助信息补全与校验"),
            step(8, "上架发布", "入驻完成，权限自动开通")
        ));
    }

    /** 商品引入九节点流程 */
    @GetMapping("/product/steps")
    public Result<List<Map<String, Object>>> productSteps() {
        return Result.success(List.of(
            step(1, "选品策划", "基于热销监测AI分析推荐引入方向"),
            step(2, "品牌对接", "运营人员与品牌方沟通合作意向"),
            step(3, "商品信息录入", "AI辅助信息补全、格式校验、批量导入"),
            step(4, "合同签署", "商品合作合同在线签署 + AI质检"),
            step(5, "价格智能摸排", "AI采集主流平台价格多维度比对"),
            step(6, "价格合理性判定", "AI价格竞争力评分辅助决策"),
            step(7, "卖点AI提炼", "大模型自动生成卖点描述和营销文案"),
            step(8, "上架审核", "两级选品委员会审核"),
            step(9, "发布上线", "商品上架发布，订购服务自动开通")
        ));
    }

    /** 商户列表 */
    @GetMapping("/merchants")
    public Result<List<Merchant>> merchantList(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return Result.success(merchantMapper.findAll(page * size, size));
    }

    /** 商品列表 */
    @GetMapping("/products")
    public Result<List<Product>> productList(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return Result.success(productMapper.findAll(page * size, size));
    }

    /** 两级选品委员会 */
    @GetMapping("/selection/committee")
    public Result<Map<String, Object>> selectionCommittee() {
        Map<String, Object> data = new HashMap<>();
        data.put("provincial", Map.of(
            "name", "省级选品初审",
            "dimensions", List.of("品牌资质", "商品合规性", "市场契合度"),
            "desc", "审核品牌资质（营业执照/商标注册/行业认证）、商品合规性（资质文件/质检报告/授权链路）、市场契合度（用户需求匹配/品类缺口分析）"
        ));
        data.put("headquarters", Map.of(
            "name", "总部选品终审",
            "dimensions", List.of("战略契合度", "资源分配合理性", "风险综合评估"),
            "desc", "审核整体战略契合度、资源分配合理性、风险综合评估。合规风险维度设置一票否决权"
        ));
        return Result.success(data);
    }

    private Map<String, Object> step(int num, String name, String desc) {
        return Map.of("num", num, "name", name, "desc", desc);
    }
}
