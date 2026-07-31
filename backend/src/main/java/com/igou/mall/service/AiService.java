package com.igou.mall.service;

import com.igou.mall.model.dto.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 统一AI能力服务层
 * 模拟实现OCR识别、文案校对、合同质检、价格摸排、卖点提炼五大AI能力
 */
@Service
public class AiService {

    private static final Random RANDOM = new Random();

    // ==================== 1. OCR 智能识别 ====================

    public AiOcrResult ocrRecognize(String mockImageName) {
        AiOcrResult result = new AiOcrResult();
        int baseScore = 85 + RANDOM.nextInt(14); // 85-98

        // 模拟识别出企业信息
        result.setScore(baseScore);
        result.setRawText("统一社会信用代码：91110108XXXXXXXXXX\n" +
                "名称：北京数字科技有限公司\n" +
                "类型：有限责任公司（法人独资）\n" +
                "法定代表人：张明轩\n" +
                "注册资本：1000万元\n" +
                "成立日期：2018年06月15日\n" +
                "营业期限：2018年06月15日至长期\n" +
                "经营范围：技术开发、技术转让、技术咨询、技术服务；销售电子产品、通讯设备；软件开发；\n" +
                "计算机系统服务；数据处理；互联网信息服务；经营电信业务。\n" +
                "登记机关：北京市海淀区市场监督管理局");

        result.setCompanyName(new AiOcrResult.OcrField("北京数字科技有限公司", baseScore));
        result.setCreditCode(new AiOcrResult.OcrField("91110108MA01XXXXXX", baseScore - 2));
        result.setLegalPerson(new AiOcrResult.OcrField("张明轩", baseScore + 1));
        result.setRegisteredCapital(new AiOcrResult.OcrField("1000万元", baseScore - 3));
        result.setBusinessScope(new AiOcrResult.OcrField(
                "技术开发、技术转让、技术咨询、技术服务；销售电子产品、通讯设备；软件开发；" +
                        "计算机系统服务；数据处理；互联网信息服务；经营电信业务", baseScore - 5));
        result.setAddress(new AiOcrResult.OcrField("北京市海淀区中关村科技园", baseScore));
        result.setEstablishDate(new AiOcrResult.OcrField("2018-06-15", baseScore + 2));

        return result;
    }

    // ==================== 2. 文案智能校对 ====================

    public AiProofResult proofText(String text, String context) {
        AiProofResult result = new AiProofResult();
        List<AiProofResult.ProofItem> items = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            result.setScore(0);
            result.setSummary("文本框为空，无法进行校对分析");
            result.setCorrectedText("");
            result.setItems(items);
            return result;
        }

        int deductions = 0;
        String corrected = text;

        // 错别字检测
        Map<String, String> typos = new HashMap<>();
        typos.put("帐号", "账号"); typos.put("连接", "链接"); typos.put("登陆", "登录");
        typos.put("帐单", "账单"); typos.put("在也不", "再也不"); typos.put("一幅", "一副");
        typos.put("以经", "已经"); typos.put("在来", "再来");

        for (Map.Entry<String, String> e : typos.entrySet()) {
            if (corrected.contains(e.getKey())) {
                corrected = corrected.replace(e.getKey(), e.getValue());
                items.add(new AiProofResult.ProofItem("TYPO", "全文",
                        e.getKey(), "应为「" + e.getValue() + "」", "MEDIUM"));
                deductions += 8;
            }
        }

        // 敏感词检测
        List<String> sensitivePatterns = Arrays.asList("最", "第一", "顶级", "全网最低", "国家级",
                "唯一", "绝对", "100%有效", "永久免费", "第一品牌");
        for (String sp : sensitivePatterns) {
            if (corrected.contains(sp)) {
                items.add(new AiProofResult.ProofItem("COMPLIANCE", "全文",
                        sp, "建议使用更客观的表述，避免绝对化用语", "HIGH"));
                deductions += 12;
            }
        }

        // 长度检测
        if (corrected.length() < 5) {
            items.add(new AiProofResult.ProofItem("LENGTH", "全文",
                    "文案长度" + corrected.length() + "字", "建议补充更多描述信息，至少5字", "MEDIUM"));
            deductions += 15;
        } else if (corrected.length() > 200) {
            items.add(new AiProofResult.ProofItem("LENGTH", "全文",
                    "文案长度" + corrected.length() + "字", "建议精简至200字以内，突出核心卖点", "LOW"));
            deductions += 5;
        }

        // 格式检测 - 中文标点符号
        if (corrected.contains(",,") || corrected.contains("。。") || corrected.contains("，，")) {
            items.add(new AiProofResult.ProofItem("FORMAT", "全文",
                    "存在重复标点符号", "建议修正标点符号使用", "LOW"));
            deductions += 3;
        }

        // 空话检测
        for (String filler : Arrays.asList("非常好", "很不错", "挺好的", "还行")) {
            if (corrected.contains(filler)) {
                items.add(new AiProofResult.ProofItem("FORMAT", "全文",
                        filler, "建议使用更具体、有说服力的词汇替换", "LOW"));
                deductions += 5;
                break;
            }
        }

        // 如果没有发现任何问题，添加积极反馈
        if (items.isEmpty()) {
            items.add(new AiProofResult.ProofItem("FORMAT", "全文",
                    "全文表述清晰", "文案质量良好，无明显问题", "LOW"));
        }

        int score = Math.max(100 - deductions, 10);
        result.setScore(score);
        result.setCorrectedText(corrected);
        result.setItems(items);
        result.setSummary(score >= 85 ? "文案质量优秀，可直接使用" :
                score >= 70 ? "文案整体良好，建议优化" + items.size() + "处问题后使用" :
                        "文案存在" + items.size() + "处需改进的地方，建议修改后重新提交");

        return result;
    }

    // ==================== 3. 合同智能质检 ====================

    public AiContractQcResult qcContract(String contractContent) {
        AiContractQcResult result = new AiContractQcResult();
        List<AiContractQcResult.QcItem> items = new ArrayList<>();
        List<AiContractQcResult.QcItem> riskItems = new ArrayList<>();

        int passCount = 0;
        int totalChecks = 8;

        // 结算条款
        boolean hasSettlement = contractContent != null && contractContent.contains("结算");
        items.add(new AiContractQcResult.QcItem("结算条款完整性", hasSettlement,
                hasSettlement ? "已包含结算条款，结算周期和方式明确" : "缺少结算条款说明",
                hasSettlement ? "" : "建议明确结算周期（T+N）、结算比例和方式"));
        if (hasSettlement) passCount++;

        // 违约条款
        boolean hasBreach = contractContent != null && contractContent.contains("违约");
        items.add(new AiContractQcResult.QcItem("违约责任条款", hasBreach,
                hasBreach ? "已包含违约责任条款" : "缺少违约责任约定",
                hasBreach ? "" : "建议明确双方违约责任和处理方式"));
        if (hasBreach) passCount++;

        // 争议解决
        boolean hasDispute = contractContent != null &&
                (contractContent.contains("仲裁") || contractContent.contains("诉讼") || contractContent.contains("争议"));
        items.add(new AiContractQcResult.QcItem("争议解决条款", hasDispute,
                hasDispute ? "已包含争议解决条款" : "缺少争议解决方式约定",
                hasDispute ? "" : "建议约定争议解决方式（仲裁/诉讼）和管辖地"));
        if (hasDispute) passCount++;

        // 知识产权
        boolean hasIp = contractContent != null &&
                (contractContent.contains("知识产权") || contractContent.contains("商标") || contractContent.contains("版权"));
        items.add(new AiContractQcResult.QcItem("知识产权保护", hasIp,
                hasIp ? "已包含知识产权保护条款" : "缺少知识产权条款",
                hasIp ? "" : "建议补充知识产权归属和使用权限约定"));
        if (hasIp) passCount++;

        // 保密条款
        boolean hasConf = contractContent != null &&
                (contractContent.contains("保密") || contractContent.contains("商业秘密"));
        items.add(new AiContractQcResult.QcItem("保密条款", hasConf,
                hasConf ? "已包含保密条款" : "缺少保密条款",
                hasConf ? "" : "建议添加保密条款保护商业信息"));
        if (hasConf) passCount++;

        // 合同期限
        boolean hasTerm = contractContent != null &&
                (contractContent.contains("期限") || contractContent.contains("有效期"));
        items.add(new AiContractQcResult.QcItem("合同期限条款", hasTerm,
                hasTerm ? "已包含合同期限条款" : "缺少合同期限约定",
                hasTerm ? "" : "建议明确合同有效期限和续约条件"));
        if (hasTerm) passCount++;

        // 退款条款
        boolean hasRefund = contractContent != null && contractContent.contains("退款");
        items.add(new AiContractQcResult.QcItem("退款政策条款", hasRefund,
                hasRefund ? "已包含退款政策条款" : "缺少退款政策说明",
                hasRefund ? "" : "建议明确退款条件和流程"));
        if (hasRefund) passCount++;

        // 不可抗力
        boolean hasForce = contractContent != null && contractContent.contains("不可抗力");
        items.add(new AiContractQcResult.QcItem("不可抗力条款", hasForce,
                hasForce ? "已包含不可抗力条款" : "缺少不可抗力条款",
                hasForce ? "" : "建议添加不可抗力条款以降低经营风险"));
        if (hasForce) passCount++;

        // 风险条款检测（模拟）
        riskItems.add(new AiContractQcResult.QcItem("免责条款范围",
                RANDOM.nextBoolean(),
                "检测到免责条款覆盖范围较广",
                "建议限制免责条款的适用范围，确保公平合理"));
        riskItems.add(new AiContractQcResult.QcItem("单方权利条款",
                RANDOM.nextBoolean(),
                "存在赋予单方变更权的条款",
                "建议将重大变更需要双方协商确认写入合同"));
        riskItems.add(new AiContractQcResult.QcItem("赔偿上限",
                RANDOM.nextBoolean(),
                "赔偿上限条款设置较低",
                "建议根据实际业务风险合理设定赔偿上限"));

        int score = (int) ((passCount / (double) totalChecks) * 90 + RANDOM.nextInt(10));
        result.setScore(score);
        result.setItems(items);
        result.setRiskItems(riskItems);
        result.setOverall(score >= 80 ? "PASS" : score >= 60 ? "WARNING" : "REJECT");
        result.setSummary("合同质检完成，共检查" + totalChecks + "项条款，通过" + passCount + "项，" +
                "发现" + riskItems.size() + "项风险提示。建议重点关注风险条款并及时修订。");

        return result;
    }

    // ==================== 4. 价格智能摸排 ====================

    public AiPriceResearchResult researchPrice(BigDecimal price, String productName, String category) {
        AiPriceResearchResult result = new AiPriceResearchResult();
        List<AiPriceResearchResult.PriceItem> items = new ArrayList<>();
        List<AiPriceResearchResult.Competitor> competitors = new ArrayList<>();

        BigDecimal p = price != null ? price : BigDecimal.ZERO;

        // 竞品价格对比（模拟）
        double variation = (RANDOM.nextDouble() - 0.5) * 0.4; // ±20%
        BigDecimal comp1Price = p.multiply(BigDecimal.valueOf(1.0 + variation))
                .setScale(2, RoundingMode.HALF_UP);
        variation = (RANDOM.nextDouble() - 0.5) * 0.4;
        BigDecimal comp2Price = p.multiply(BigDecimal.valueOf(1.0 + variation))
                .setScale(2, RoundingMode.HALF_UP);
        variation = (RANDOM.nextDouble() - 0.5) * 0.3;
        BigDecimal comp3Price = p.multiply(BigDecimal.valueOf(1.0 + variation))
                .setScale(2, RoundingMode.HALF_UP);

        competitors.add(new AiPriceResearchResult.Competitor("淘宝", productName != null ? productName : "同类商品", comp1Price));
        competitors.add(new AiPriceResearchResult.Competitor("京东", productName != null ? productName : "同类商品", comp2Price));
        competitors.add(new AiPriceResearchResult.Competitor("拼多多", productName != null ? productName : "同类商品", comp3Price));

        // 价格合理性分析
        BigDecimal avgComp = comp1Price.add(comp2Price).add(comp3Price).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
        BigDecimal diff = BigDecimal.ZERO;
        if (avgComp.compareTo(BigDecimal.ZERO) > 0) {
            diff = p.subtract(avgComp).divide(avgComp, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }

        // 各维度分析
        items.add(new AiPriceResearchResult.PriceItem("市场价格定位",
                p.compareTo(BigDecimal.ZERO) > 0 && Math.abs(diff.doubleValue()) <= 30,
                "当前售价¥" + p + "，较市场均价¥" + avgComp + "，偏差" +
                        String.format("%+.1f%%", diff.doubleValue()),
                Math.abs(diff.doubleValue()) > 30 ? "建议调整至市场均价±30%范围内" : ""));

        items.add(new AiPriceResearchResult.PriceItem("毛利率分析",
                p.compareTo(BigDecimal.ZERO) > 0,
                "基于品类「" + (category != null ? category : "通用") + "」的平均毛利率模型，" +
                        "当前定价毛利率约" + (25 + RANDOM.nextInt(30)) + "%",
                p.compareTo(BigDecimal.ZERO) <= 0 ? "必须设置有效售价" : ""));

        boolean lowPrice = p.compareTo(new BigDecimal("10")) < 0;
        items.add(new AiPriceResearchResult.PriceItem("价格带竞争分析",
                !lowPrice,
                lowPrice ? "当前定价低于¥10，处于低价带，利润空间有限" :
                        "当前定价处于中等价格带，竞争环境适中",
                lowPrice ? "建议考虑提升产品附加值或优化成本结构" : ""));

        items.add(new AiPriceResearchResult.PriceItem("历史价格趋势",
                true,
                "该品类近30天价格波动幅度约" + (1 + RANDOM.nextInt(8)) + "%，走势平稳",
                ""));

        int score = Math.min(95, 50 + RANDOM.nextInt(40));
        BigDecimal suggestedPrice = avgComp.multiply(BigDecimal.valueOf(0.9 + RANDOM.nextDouble() * 0.2))
                .setScale(2, RoundingMode.HALF_UP);

        result.setOverall(Math.abs(diff.doubleValue()) <= 15 ? "REASONABLE" :
                diff.doubleValue() > 0 ? "HIGH" : "LOW");
        result.setScore(score);
        result.setSuggestedPrice(suggestedPrice);
        result.setPriceLower(suggestedPrice.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP));
        result.setPriceUpper(suggestedPrice.multiply(BigDecimal.valueOf(1.15)).setScale(2, RoundingMode.HALF_UP));
        result.setCompetitors(competitors);
        result.setItems(items);
        result.setSummary("价格智能摸排完成。建议售价区间 ¥" + result.getPriceLower() + " ~ ¥" +
                result.getPriceUpper() + "，推荐售价 ¥" + suggestedPrice + "。" +
                "共对比" + competitors.size() + "个平台竞品价格，当前定价" +
                (Math.abs(diff.doubleValue()) <= 15 ? "合理" : diff.doubleValue() > 0 ? "偏高" : "偏低") + "。");

        return result;
    }

    // ==================== 5. 商品卖点提炼 ====================

    public AiSellingPointResult extractSellingPoints(String productName, String description, String detail) {
        AiSellingPointResult result = new AiSellingPointResult();
        String fullText = (productName != null ? productName : "") + " " +
                (description != null ? description : "");

        // 基于文本分析生成卖点
        String sellingPoint;
        List<String> tags = new ArrayList<>();
        List<AiSellingPointResult.TagItem> tagAnalysis = new ArrayList<>();

        if (fullText.trim().length() < 3) {
            sellingPoint = "商品信息不足，无法自动生成卖点描述";
            tags.add("待完善");
            tagAnalysis.add(new AiSellingPointResult.TagItem("待完善", 0));
            result.setScore(10);
        } else {
            // 关键词提取（模拟AI分析）
            Map<String, Integer> keywordWeights = new HashMap<>();
            String[] words = fullText.replaceAll("[，。！？、；：\"\"''（）\\[\\]【】{}]", " ").split("\\s+");
            for (String w : words) {
                if (w.length() >= 2 && !Pattern.matches("[0-9]+|[¥￥]?.+|元|的|了|和|是|在|有", w)) {
                    keywordWeights.merge(w, 40 + RANDOM.nextInt(60), Integer::sum);
                }
            }

            // 生成标签
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(keywordWeights.entrySet());
            sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));
            int tagCount = Math.min(5, sorted.size());
            for (int i = 0; i < tagCount; i++) {
                String tag = sorted.get(i).getKey();
                int weight = Math.min(98, sorted.get(i).getValue());
                tags.add(tag);
                tagAnalysis.add(new AiSellingPointResult.TagItem(tag, weight));
            }
            if (tags.isEmpty()) {
                tags = Arrays.asList("新品", "热销", "优选");
                tagAnalysis.add(new AiSellingPointResult.TagItem("新品", 75));
                tagAnalysis.add(new AiSellingPointResult.TagItem("热销", 72));
                tagAnalysis.add(new AiSellingPointResult.TagItem("优选", 70));
            }

            // 生成卖点描述
            StringBuilder sb = new StringBuilder();
            if (productName != null && !productName.isEmpty()) {
                sb.append("「").append(productName).append("」");
            }
            sb.append("品质有保障，");
            if (tags.size() >= 2) {
                sb.append("主打").append(tags.get(0)).append("和").append(tags.get(1)).append("，");
            }
            sb.append("性价比出众，是您不容错过的品质之选。");
            sellingPoint = sb.toString();
            result.setScore(60 + RANDOM.nextInt(35));
        }

        // 营销文案生成
        StringBuilder marketing = new StringBuilder();
        if (productName != null && !productName.isEmpty()) {
            marketing.append("【爆款推荐】").append(productName).append("\n\n");
        }
        marketing.append("精选好物，品质保证！\n");
        if (!tags.isEmpty()) {
            marketing.append("核心亮点：").append(String.join(" · ", tags)).append("\n");
        }
        marketing.append("\n限时优惠中，立即抢购！");
        String marketingCopy = marketing.toString();

        // 短标题
        String shortTitle = productName != null && !productName.isEmpty() ?
                productName : (tags.isEmpty() ? "新品" : tags.get(0) + "类商品");

        result.setSellingPoint(sellingPoint);
        result.setTags(tags);
        result.setTagAnalysis(tagAnalysis);
        result.setMarketingCopy(marketingCopy);
        result.setShortTitle(shortTitle);
        result.setSummary("AI卖点提炼完成，基于商品文本自动生成" + tags.size() + "个标签和营销文案。得分：" +
                result.getScore() + "分");

        return result;
    }
}
