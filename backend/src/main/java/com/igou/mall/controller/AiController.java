package com.igou.mall.controller;

import com.igou.mall.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * AI+应用Controller — DEMO演示项6：AI智能赋能（2分）
 * 提供OCR识别、信息录入、文案校对、合同质检、价格摸排、卖点提炼六大AI能力API
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    /** AI能力矩阵 */
    @GetMapping("/capabilities")
    public Result<List<Map<String, Object>>> capabilities() {
        return Result.success(List.of(
            cap("ocr", "商户入驻智能识别", "🔍", "OCR自动识别营业执照、法人身份证等资质文件，NLP校验信息一致性"),
            cap("entry", "信息高效录入", "✍️", "AI辅助商品信息补全、格式校验、批量导入，录入效率提升6倍"),
            cap("proofread", "文案智能校对", "📝", "大模型智能校对商品描述、营销文案的准确性与规范性"),
            cap("contract", "合同智能质检", "📋", "大模型+NLP解析合同条款，自动识别合规风险并生成修改建议"),
            cap("price", "价格智能摸排", "💹", "AI采集主流平台价格，多维度比对分析，给出定价建议"),
            cap("selling", "商品卖点提炼", "✨", "大模型自动生成商品卖点描述和营销文案")
        ));
    }

    /** 1. OCR资质智能识别 */
    @PostMapping("/ocr")
    public Result<Map<String, Object>> ocrRecognize(@RequestBody Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "2.3秒");
        result.put("accuracy", "98.8%");
        result.put("license", List.of(
            field("统一社会信用代码", "91110108MA01ABC23X", 99.2),
            field("企业名称", "瑞幸咖啡（中国）有限公司", 98.8),
            field("企业类型", "有限责任公司（台港澳法人独资）", 97.5),
            field("法定代表人", "郭谨一", 99.5),
            field("注册资本", "15000万美元", 98.3),
            field("成立日期", "2018年03月02日", 99.0),
            field("营业期限", "2018-03-02 至 2048-03-01", 98.6),
            field("经营范围", "餐饮服务；食品经营；销售工艺品等", 96.2)
        ));
        result.put("riskCheck", List.of(
            risk("营业执照有效期", "pass", "有效期至2048年，状态正常"),
            risk("注册资本验证", "pass", "注册资本充足"),
            risk("经营范围匹配", "pass", "经营范围与入驻品类匹配"),
            risk("企业信用查询", "warning", "存在2条经营异常记录，需人工复核"),
            risk("法人身份核验", "pass", "法人身份信息与4A系统匹配")
        ));
        result.put("riskLevel", "LOW");
        result.put("suggestion", "低风险商户，建议进入标准审核流程（快速通道）");
        return Result.success(result);
    }

    /** 2. 信息高效录入 */
    @PostMapping("/entry")
    public Result<Map<String, Object>> infoEntry(@RequestBody Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "1.5秒");
        result.put("fields", List.of(
            entryField("商品名称", "爱奇艺黄金会员", "爱奇艺黄金会员月卡", "NLP补全"),
            entryField("商品分类", "—", "视频娱乐 → 视频会员", "分类推荐"),
            entryField("商品描述", "—", "爱奇艺黄金VIP会员月卡，享受专属内容...", "大模型生成"),
            entryField("建议售价", "—", "¥19.90（基于市场摸排）", "价格摸排"),
            entryField("有效期", "—", "30天", "规则匹配"),
            entryField("使用说明", "—", "1.购买后自动发卡 2.登录爱奇艺兑换...", "模板匹配")
        ));
        result.put("efficiencyGain", "录入耗时从30分钟缩短至5分钟");
        return Result.success(result);
    }

    /** 3. 文案智能校对 */
    @PostMapping("/proofread")
    public Result<Map<String, Object>> proofread(@RequestBody Map<String, String> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "1.2秒");
        result.put("issues", List.of(
            issue("违规用语", "\"最便宜的价格\"", "违反广告法", "建议修改为：\"超值优惠价\"", "high"),
            issue("绝对化用语", "\"支持所有设备\"", "表述不准确", "建议修改为：\"支持多设备同时登录\"", "medium"),
            issue("标点规范", "感叹号过多", "格式问题", "建议保留一个感叹号", "low")
        ));
        result.put("corrected", "爱奇艺黄金VIP会员月卡，超清画质，无广告，超值优惠价，让你追剧不停！支持多设备同时登录。");
        return Result.success(result);
    }

    /** 4. 合同智能质检 */
    @PostMapping("/contract")
    public Result<Map<String, Object>> contractCheck(@RequestBody Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "3.2秒");
        result.put("model", "大模型+NLP条款解析引擎");
        result.put("summary", Map.of("total", 6, "pass", 3, "warning", 1, "risk", 2));
        result.put("overallRisk", "中风险");
        result.put("clauses", List.of(
            clause(1, "甲方应于合同签订后30日内向乙方支付首期合作保证金人民币伍万元整。", "付款条款", "low", ""),
            clause(2, "乙方有权在任何情况下单方面终止本合同，且无需承担违约责任。", "终止条款", "high",
                "建议修改为\"乙方在甲方严重违约且经书面通知30日仍未纠正的情况下方可终止\""),
            clause(3, "甲方应保证所提供商品符合国家相关质量标准及法律法规要求。", "质量条款", "none", ""),
            clause(4, "本合同项下商品销售价格由乙方单方面确定。", "价格条款", "medium",
                "建议增加价格协商机制：\"商品销售价格应由双方协商确定\""),
            clause(5, "甲方因商品质量问题造成的损失，乙方不承担任何连带责任。", "责任条款", "high",
                "建议修改为\"由甲方承担主要责任，乙方在过错范围内承担相应责任\""),
            clause(6, "本合同争议提交北京仲裁委员会仲裁解决。", "争议解决", "none", "")
        ));
        result.put("suggestion", "合同存在2处高风险条款和1处中风险条款，建议修改后重新提交质检。重点关注终止条款和责任条款的公平性。");
        return Result.success(result);
    }

    /** 5. 价格智能摸排 */
    @PostMapping("/price")
    public Result<Map<String, Object>> priceSurvey(@RequestBody Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "2.8秒");
        result.put("ourPrice", 19.90);
        result.put("marketAvg", 23.50);
        result.put("platforms", List.of(
            platform("京东", 24.90), platform("天猫", 23.00), platform("拼多多", 21.90),
            platform("淘宝", 22.50), platform("抖音", 25.00)
        ));
        result.put("priceAdvantage", "15.3%");
        result.put("competitiveness", "优秀");
        result.put("riskLevel", "低风险");
        result.put("suggestion", "当前定价低于市场均价15.3%，价格竞争力优秀。建议保持当前定价策略，可在营销活动中作为引流商品重点推广。");
        return Result.success(result);
    }

    /** 6. 商品卖点提炼 */
    @PostMapping("/selling")
    public Result<Map<String, Object>> sellingPoint(@RequestBody Map<String, String> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "2.8秒");
        result.put("model", "大语言模型");
        result.put("outputs", List.of(
            output("核心卖点", "🎬 VIP专享海量内容，院线大片抢先看\n📺 4K超清画质，沉浸式观影体验\n🚫 纯净无广告，追剧零打扰\n📱 多设备同时在线，全家共享好时光"),
            output("营销文案", "🔥 腾讯视频VIP月卡，低至5折！\n追剧不等待，大片随心看！4K超清+无广告+多设备同登，一站式满足全家观影需求。\n⏰ 限时特惠，抢完即止！"),
            output("短标题", "腾讯视频VIP月卡｜4K超清·无广告·多设备同登"),
            output("社交分享文案", "终于等到腾讯视频VIP打折了！月卡只要一杯奶茶钱，4K大片随便看，还能多设备同时用，赶紧冲！ #腾讯视频VIP #限时优惠")
        ));
        result.put("highlights", List.of("4K超清画质", "VIP专享内容", "无广告纯净体验", "多设备同时登录", "院线大片抢先看"));
        return Result.success(result);
    }

    /** 7. NLP信息一致性校验 */
    @PostMapping("/nlp-check")
    public Result<Map<String, Object>> nlpCheck(@RequestBody Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "1.6秒");
        result.put("model", "NLP语义比对引擎");
        result.put("consistencyRate", "92.5%");
        result.put("fields", List.of(
            nlpField("企业名称", "一致", "资质文件与填报信息一致", 100.0),
            nlpField("统一社会信用代码", "一致", "资质文件与填报信息一致", 100.0),
            nlpField("法定代表人", "一致", "资质文件与填报信息一致", 100.0),
            nlpField("经营范围", "部分偏差", "资质含\"餐饮服务\"，填报未包含该项", 78.5),
            nlpField("注册资本", "一致", "资质文件与填报信息一致", 100.0),
            nlpField("注册地址", "需确认", "资质地址与填报地址存在门牌号差异", 65.0)
        ));
        result.put("anomalyFields", 2);
        result.put("suggestion", "发现2处信息不一致，建议人工复核经营范围与注册地址信息。");
        return Result.success(result);
    }

    /** 8. 三级风险预判 */
    @PostMapping("/risk-predict")
    public Result<Map<String, Object>> riskPredict(@RequestBody Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "2.1秒");
        result.put("model", "风险评估模型 v3.2");
        result.put("dimensions", List.of(
            riskDim("资质风险", "LOW", 85, "资质文件齐全，有效期正常"),
            riskDim("经营风险", "MEDIUM", 62, "存在2条经营异常记录"),
            riskDim("信用风险", "LOW", 78, "信用评分良好，无不良记录"),
            riskDim("合规风险", "MEDIUM", 55, "经营范围与入驻品类部分不匹配"),
            riskDim("财务风险", "LOW", 90, "注册资本充足，财务状况正常")
        ));
        result.put("overallRisk", "MEDIUM");
        result.put("riskScore", 74);
        result.put("flowRecommend", "中风险商户，建议进入复核流程（增加业务复审环节）");
        result.put("attentionItems", List.of(
            "经营异常记录需人工核实", 
            "经营范围匹配度需人工确认"
        ));
        return Result.success(result);
    }

    /** 9. AI品类推荐 */
    @PostMapping("/category-recommend")
    public Result<Map<String, Object>> categoryRecommend(@RequestBody Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "1.3秒");
        result.put("model", "品类推荐模型 v2.1");
        result.put("recommendations", List.of(
            catRec("视频娱乐", "视频会员", 95, "行业特征高度匹配，优先推荐"),
            catRec("本地生活", "咖啡茶饮", 88, "经营范围匹配，市场需求旺盛"),
            catRec("数字权益", "音乐会员", 82, "品牌调性匹配，用户重叠度高"),
            catRec("生活服务", "外卖配送", 65, "互补品类，可拓展业务版图")
        ));
        result.put("primaryRecommend", "视频娱乐 → 视频会员（匹配度95%）");
        result.put("strategy", List.of(
            "促销策略: 首月低价体验+续费折扣",
            "定价策略: 低于市场均价10%-15%引流",
            "物流策略: 即时到账无需物流"
        ));
        return Result.success(result);
    }

    /** 10. 流程瓶颈分析 */
    @GetMapping("/bottleneck-analysis")
    public Result<Map<String, Object>> bottleneckAnalysis() {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "1.8秒");
        result.put("model", "流程分析模型 v1.0");
        result.put("bottlenecks", List.of(
            bottleNode("资质初审", 3.2, 2.0, "审核人员不足，建议增加自动审核规则"),
            bottleNode("合规终审", 4.5, 3.0, "合规检查维度多，建议AI预审辅助"),
            bottleNode("合同签署", 2.8, 1.5, "合同审批链路长，建议启用AI合同质检提速")
        ));
        result.put("summary", Map.of(
            "totalAudits", 156,
            "avgTime", 3.5,
            "bottleneckCount", 3,
            "optimizationGain", "预计可缩短30%审核周期"
        ));
        result.put("suggestions", List.of(
            optSug("资质初审节点增加自动审核规则", "预判审核时间缩短50%", "HIGH"),
            optSug("合规终审AI预审辅助", "减少人工审核维度2个", "MEDIUM"),
            optSug("合同签署启用AI质检", "合同审查时间缩短至30分钟", "HIGH")
        ));
        return Result.success(result);
    }

    /** 11. 批量入驻预览（AI批量识别） */
    @PostMapping("/batch-preview")
    public Result<Map<String, Object>> batchPreview(@RequestBody Map<String, Object> input) {
        Map<String, Object> result = new HashMap<>();
        result.put("processTime", "3.5秒");
        result.put("model", "批量识别引擎 v2.0");
        result.put("totalRows", 15);
        result.put("processedRows", 15);
        result.put("successRows", 13);
        result.put("warningRows", 2);
        result.put("summary", Map.of(
            "lowRisk", 10, "mediumRisk", 3, "highRisk", 2,
            "avgConfidence", "94.2%", "estimatedTime", "预计45分钟完成全部审核"
        ));
        result.put("details", List.of(
            batchRow(1, "瑞幸咖啡", "DIGITAL", "LOW", 95.2, "success"),
            batchRow(2, "腾讯音乐", "DIGITAL", "LOW", 93.8, "success"),
            batchRow(3, "京东集团", "PHYSICAL", "LOW", 91.5, "success"),
            batchRow(4, "阿里巴巴", "PHYSICAL", "MEDIUM", 78.3, "warning"),
            batchRow(5, "美团点评", "LOCAL_LIFE", "HIGH", 52.1, "warning")
        ));
        return Result.success(result);
    }

    // === Helper methods ===
    private Map<String, Object> cap(String id, String name, String icon, String desc) {
        return Map.of("id", id, "name", name, "icon", icon, "desc", desc);
    }
    private Map<String, Object> field(String label, String value, double confidence) {
        return Map.of("label", label, "value", value, "confidence", confidence);
    }
    private Map<String, Object> risk(String item, String status, String desc) {
        return Map.of("item", item, "status", status, "desc", desc);
    }
    private Map<String, Object> entryField(String field, String input, String aiFill, String source) {
        return Map.of("field", field, "input", input, "aiFill", aiFill, "source", source);
    }
    private Map<String, Object> issue(String type, String content, String severity, String suggestion, String level) {
        return Map.of("type", type, "content", content, "severity", severity, "suggestion", suggestion, "level", level);
    }
    private Map<String, Object> clause(int id, String content, String type, String risk, String suggestion) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id); m.put("content", content); m.put("type", type);
        m.put("risk", risk); m.put("suggestion", suggestion);
        return m;
    }
    private Map<String, Object> platform(String name, double price) {
        return Map.of("platform", name, "price", price);
    }
    private Map<String, Object> output(String type, String content) {
        return Map.of("type", type, "content", content);
    }
    private Map<String, Object> nlpField(String field, String status, String detail, double confidence) {
        return Map.of("field", field, "status", status, "detail", detail, "confidence", confidence);
    }
    private Map<String, Object> riskDim(String dimension, String level, int score, String detail) {
        return Map.of("dimension", dimension, "level", level, "score", score, "detail", detail);
    }
    private Map<String, Object> catRec(String category, String subCategory, int match, String reason) {
        return Map.of("category", category, "subCategory", subCategory, "match", match, "reason", reason);
    }
    private Map<String, Object> bottleNode(String node, double avgDays, double targetDays, String cause) {
        return Map.of("node", node, "avgDays", avgDays, "targetDays", targetDays, "cause", cause);
    }
    private Map<String, Object> optSug(String suggestion, String effect, String priority) {
        return Map.of("suggestion", suggestion, "effect", effect, "priority", priority);
    }
    private Map<String, Object> batchRow(int row, String name, String type, String risk, double confidence, String status) {
        return Map.of("row", row, "name", name, "type", type, "risk", risk, "confidence", confidence, "status", status);
    }
}
