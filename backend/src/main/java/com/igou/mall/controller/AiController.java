package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.SystemConfigMapper;
import com.igou.mall.model.dto.AiPriceResearchResult;
import com.igou.mall.model.entity.SystemConfig;
import com.igou.mall.service.AiService;
import com.igou.mall.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AiService aiService;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private SystemConfigMapper configMapper;

    // ========== 模型配置 ==========

    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> result = new HashMap<>();
        SystemConfig enabled = configMapper.findByKey("deepseek.enabled");
        result.put("enabled", enabled != null && "true".equals(enabled.getConfigValue()));
        SystemConfig apiKey = configMapper.findByKey("deepseek.api_key");
        result.put("apiKey", apiKey != null ? apiKey.getConfigValue() : "");
        return Result.success(result);
    }

    @PostMapping("/config")
    public Result<?> saveConfig(@RequestBody Map<String, Object> params) {
        boolean enabled = Boolean.TRUE.equals(params.get("enabled"));

        SystemConfig enabledConfig = new SystemConfig();
        enabledConfig.setConfigKey("deepseek.enabled");
        enabledConfig.setConfigValue(String.valueOf(enabled));
        enabledConfig.setDescription("DeepSeek模型启用开关");
        configMapper.upsert(enabledConfig);

        if (enabled) {
            String apiKey = (String) params.get("apiKey");
            SystemConfig keyConfig = new SystemConfig();
            keyConfig.setConfigKey("deepseek.api_key");
            keyConfig.setConfigValue(apiKey != null ? apiKey : "");
            keyConfig.setDescription("DeepSeek API密钥");
            configMapper.upsert(keyConfig);
        }

        return Result.success("配置已保存");
    }

    // ========== AI校对（Task 3） ==========

    @PostMapping("/proofread")
    public Result<Map<String, Object>> proofread(@RequestBody Map<String, Object> params) {
        if (!deepSeekService.isEnabled()) {
            return Result.error("AI服务未启用");
        }

        String content = (String) params.get("content");
        if (content == null || content.trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        String systemPrompt = "你是一个专业的内容审核校对助手。请对用户提交的商品描述内容进行多维度校对，并严格按照以下JSON格式返回结果（不要包含markdown代码块标记）：\n" +
                "{\n" +
                "  \"issues\": [{\"type\":\"错别字/违规用语/绝对化用语/标点规范\", \"original\":\"原文片段\", \"suggestion\":\"修改建议\", \"position\":\"大致位置说明\"}],\n" +
                "  \"optimizedContent\": \"优化后的完整内容\",\n" +
                "  \"summary\": \"整体评价和建议\"\n" +
                "}\n" +
                "校对维度包括：错别字检测、合规性审查（违规用语识别）、绝对化用语识别（如\"最好\"、\"第一\"、\"国家级\"等）、标点符号规范化。";

        String response = deepSeekService.chat(systemPrompt, "请对以下内容进行校对：\n" + content);

        if (response == null) {
            return Result.error("AI服务调用失败，请检查API密钥配置");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("raw", response);
        result.put("success", true);
        return Result.success(result);
    }

    // ========== AI辅助补全（Task 4） ==========

    @PostMapping("/autofill")
    public Result<Map<String, Object>> autofill(@RequestBody Map<String, Object> params) {
        if (!deepSeekService.isEnabled()) {
            return Result.error("AI服务未启用");
        }

        String productName = (String) params.getOrDefault("productName", "");
        String productType = (String) params.getOrDefault("productType", "PHYSICAL");
        String existingDesc = (String) params.getOrDefault("description", "");
        String existingDetail = (String) params.getOrDefault("detail", "");

        String systemPrompt = "你是一个专业的电商商品运营助手。请根据用户提供的商品基本信息，智能补全以下字段，并严格按照JSON格式返回（不要包含markdown代码块标记）：\n" +
                "{\n" +
                "  \"productName\": \"优化后的商品名称\",\n" +
                "  \"categorySuggestion\": \"建议的商品分类名称\",\n" +
                "  \"description\": \"商品描述/介绍\",\n" +
                "  \"suggestedPrice\": 99.00,\n" +
                "  \"validityPeriod\": \"建议的有效期（如：30天/长期有效）\",\n" +
                "  \"detail\": \"商品详情描述\",\n" +
                "  \"tags\": [\"标签1\", \"标签2\", \"标签3\"]\n" +
                "}\n" +
                "请确保建议合理、专业，符合电商行业规范。价格单位为元。";

        StringBuilder userMsg = new StringBuilder("请为以下商品提供补全建议：\n");
        if (!productName.isEmpty()) userMsg.append("商品名称：").append(productName).append("\n");
        userMsg.append("商品类型：").append(productType).append("\n");
        if (!existingDesc.isEmpty()) userMsg.append("已有描述：").append(existingDesc).append("\n");
        if (!existingDetail.isEmpty()) userMsg.append("已有详情：").append(existingDetail).append("\n");

        String response = deepSeekService.chat(systemPrompt, userMsg.toString());

        if (response == null) {
            return Result.error("AI服务调用失败，请检查API密钥配置");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("raw", response);
        result.put("success", true);
        return Result.success(result);
    }

    // ========== 价格智能摸排 ==========

    @PostMapping("/price-research")
    public Result<AiPriceResearchResult> priceResearch(@RequestBody Map<String, Object> params) {
        BigDecimal price = params.get("price") != null ?
                new BigDecimal(params.get("price").toString()) : BigDecimal.ZERO;
        String productName = (String) params.getOrDefault("productName", "");
        String category = (String) params.getOrDefault("category", "");

        AiPriceResearchResult result = aiService.researchPrice(price, productName, category);
        return Result.success(result);
    }

    // ========== 商品卖点提炼 ==========

    @PostMapping("/selling-points")
    public Result<Map<String, Object>> sellingPoints(@RequestBody Map<String, Object> params) {
        if (!deepSeekService.isEnabled()) {
            return Result.error("AI服务未启用");
        }

        String productName = (String) params.getOrDefault("productName", "");
        String productType = (String) params.getOrDefault("productType", "PHYSICAL");
        String description = (String) params.getOrDefault("description", "");
        String price = params.get("price") != null ? params.get("price").toString() : "";
        String category = (String) params.getOrDefault("category", "");

        String systemPrompt = "你是一个专业的电商文案专家，请根据用户提供的商品基本信息，生成以下四种类型的卖点文案，并严格按照JSON格式返回（不要包含markdown代码块标记）：\n" +
                "{\n" +
                "  \"corePoints\": \"核心卖点描述（每行一个卖点，用换行符分隔，每个卖点用emoji开头）\",\n" +
                "  \"marketingCopy\": \"营销文案（包含促销用语和紧迫感的营销文案，2-3句话）\",\n" +
                "  \"shortTitle\": \"短标题（15-30字的精炼标题，突出核心卖点）\",\n" +
                "  \"socialCopy\": \"社交分享文案（适合朋友圈/小红书分享的文案，口语化且有感染力，可包含emoji和话题标签）\",\n" +
                "  \"highlights\": [\"亮点1\", \"亮点2\", \"亮点3\", \"亮点4\", \"亮点5\"]\n" +
                "}\n" +
                "要求：文案要有感染力，突出商品优势和差异化，适合电商场景使用。emoji要贴切。";

        StringBuilder userMsg = new StringBuilder("请为以下商品生成卖点文案：\n");
        if (!productName.isEmpty()) userMsg.append("商品名称：").append(productName).append("\n");
        if (!category.isEmpty()) userMsg.append("商品分类：").append(category).append("\n");
        if (!description.isEmpty()) userMsg.append("商品描述：").append(description).append("\n");
        if (!price.isEmpty()) userMsg.append("售价：").append(price).append("\n");
        userMsg.append("商品类型：").append(productType);

        String response = deepSeekService.chat(systemPrompt, userMsg.toString());

        if (response == null) {
            return Result.error("AI服务调用失败，请检查API密钥配置");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("raw", response);
        result.put("success", true);
        return Result.success(result);
    }
}
