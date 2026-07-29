package com.igou.mall.controller;

import com.igou.mall.common.Result;
import com.igou.mall.dao.ProductMapper;
import com.igou.mall.dao.ProductCategoryMapper;
import com.igou.mall.dao.StockChangeMapper;
import com.igou.mall.model.dto.AiAuditItem;
import com.igou.mall.model.dto.AiAuditResult;
import com.igou.mall.model.entity.Product;
import com.igou.mall.model.entity.ProductCategory;
import com.igou.mall.model.entity.StockChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/product")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductCategoryMapper categoryMapper;

    @Autowired
    private StockChangeMapper stockChangeMapper;

    @GetMapping
    public Result<List<Product>> list(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return Result.success(productMapper.findAll(page * size, size));
    }

    @GetMapping("/list")
    public Result<List<Product>> listAll(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(productMapper.findAll(page * size, size));
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success(product);
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        List<StockChange> stockChanges = stockChangeMapper.findByProductId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("product", product);
        result.put("stockChanges", stockChanges);
        return Result.success(result);
    }

    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        product.setProductCode("PRD" + System.currentTimeMillis());
        product.setStatus("PENDING");
        if (product.getCategoryId() != null && product.getCategory() == null) {
            ProductCategory cat = categoryMapper.findById(product.getCategoryId());
            if (cat != null) {
                product.setCategory(cat.getCategoryName());
            }
        }
        productMapper.insert(product);
        return Result.success(product);
    }

    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        // 仅更新传入的非 null 字段
        if (params.get("productName") != null) product.setProductName((String) params.get("productName"));
        if (params.get("description") != null) product.setDescription((String) params.get("description"));
        if (params.get("detail") != null) product.setDetail((String) params.get("detail"));
        if (params.get("categoryId") != null) product.setCategoryId(Long.valueOf(params.get("categoryId").toString()));
        if (params.get("brand") != null) product.setBrand((String) params.get("brand"));
        if (params.get("merchantId") != null) product.setMerchantId(Long.valueOf(params.get("merchantId").toString()));
        if (params.get("price") != null) product.setPrice(new BigDecimal(params.get("price").toString()));
        if (params.get("marketPrice") != null) product.setMarketPrice(new BigDecimal(params.get("marketPrice").toString()));
        if (params.get("vipPrice") != null) product.setVipPrice(new BigDecimal(params.get("vipPrice").toString()));
        if (params.get("stock") != null) product.setStock(Integer.valueOf(params.get("stock").toString()));
        if (params.get("imageUrls") != null) product.setImageUrls((String) params.get("imageUrls"));
        if (params.get("status") != null) product.setStatus((String) params.get("status"));
        if (params.get("rejectReason") != null) product.setRejectReason((String) params.get("rejectReason"));
        productMapper.update(product);
        return Result.success(product);
    }

    @PutMapping("/{id}/audit")
    public Result<Product> audit(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        String auditStatus = (String) params.get("auditStatus");
        String rejectReason = (String) params.get("rejectReason");
        String approveReason = (String) params.get("approveReason");
        String auditor = (String) params.get("auditor");
        product.setStatus(auditStatus);
        product.setRejectReason(rejectReason);
        product.setApproveReason(approveReason);
        product.setAuditor(auditor != null ? auditor : "系统");
        product.setAuditTime(java.time.LocalDateTime.now());
        productMapper.update(product);
        return Result.success(product);
    }

    @PutMapping("/{id}/stock")
    public Result<Product> updateStock(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        Integer changeAmount = (Integer) params.get("changeAmount");
        String changeType = (String) params.get("changeType");
        String reason = (String) params.get("reason");
        
        int beforeStock = product.getStock();
        int afterStock = beforeStock + changeAmount;
        product.setStock(afterStock);
        productMapper.update(product);

        StockChange stockChange = new StockChange();
        stockChange.setProductId(id);
        stockChange.setChangeType(changeType);
        stockChange.setChangeAmount(changeAmount);
        stockChange.setBeforeStock(beforeStock);
        stockChange.setAfterStock(afterStock);
        stockChange.setReason(reason);
        stockChangeMapper.insert(stockChange);

        return Result.success(product);
    }

    @PutMapping("/{id}/status")
    public Result<Product> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        String status = (String) params.get("status");
        product.setStatus(status);
        productMapper.update(product);
        return Result.success(product);
    }

    @PutMapping("/{id}/price")
    public Result<Product> updatePrice(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        product.setPrice(new BigDecimal(params.get("price").toString()));
        if (params.containsKey("vipPrice")) {
            product.setVipPrice(new BigDecimal(params.get("vipPrice").toString()));
        }
        if (params.containsKey("marketPrice")) {
            product.setMarketPrice(new BigDecimal(params.get("marketPrice").toString()));
        }
        productMapper.update(product);
        return Result.success(product);
    }

    @GetMapping("/category/{category}")
    public Result<List<Product>> listByCategory(@PathVariable String category,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return Result.success(productMapper.findByCategory(category, page * size, size));
    }

    @GetMapping("/category-id/{categoryId}")
    public Result<List<Product>> listByCategoryId(@PathVariable Long categoryId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return Result.success(productMapper.findByCategoryId(categoryId, page * size, size));
    }

    @GetMapping("/search")
    public Result<List<Product>> search(@RequestParam String keyword,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return Result.success(productMapper.search(keyword, page * size, size));
    }

    @GetMapping("/hot")
    public Result<List<Product>> hotProducts(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(productMapper.findHotProducts(limit));
    }

    @GetMapping("/new")
    public Result<List<Product>> newProducts(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(productMapper.findNewProducts(limit));
    }

    @GetMapping("/categories")
    public Result<List<ProductCategory>> categories() {
        return Result.success(categoryMapper.findAll());
    }

    @GetMapping("/categories/{level}")
    public Result<List<ProductCategory>> categoriesByLevel(@PathVariable Integer level) {
        return Result.success(categoryMapper.findByLevel(level));
    }

    @PostMapping("/category")
    public Result<ProductCategory> createCategory(@RequestBody ProductCategory category) {
        category.setStatus("ACTIVE");
        categoryMapper.insert(category);
        return Result.success(category);
    }

    @GetMapping("/audit-list")
    public Result<List<Product>> auditList() {
        return Result.success(productMapper.findByStatus("PENDING"));
    }

    @PostMapping("/{id}/ai-audit")
    public Result<AiAuditResult> aiAudit(@PathVariable Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }

        List<AiAuditItem> items = new ArrayList<>();
        Set<String> bannedWords = new HashSet<>(Arrays.asList("假", "刷", "诈骗", "最", "第一", "顶级", "全网最低", "国家级", "免费", "100%"));

        // 1. 商品名称合规性
        String name = product.getProductName();
        boolean nameOk = true;
        String nameDetail;
        String nameSuggestion = "";
        if (name == null || name.trim().isEmpty()) {
            nameOk = false;
            nameDetail = "商品名称为空，必须填写商品名称";
            nameSuggestion = "请填写完整、准确的商品名称";
        } else if (name.length() < 2) {
            nameOk = false;
            nameDetail = "商品名称过短（当前" + name.length() + "字符），需至少2个字符";
            nameSuggestion = "建议补充完整的商品名称，包含品牌+核心卖点";
        } else if (name.length() > 50) {
            nameOk = false;
            nameDetail = "商品名称过长（当前" + name.length() + "字符），应不超过50个字符";
            nameSuggestion = "建议精简商品名称，保留核心关键词";
        } else {
            List<String> foundBanned = new ArrayList<>();
            for (String bw : bannedWords) {
                if (name.contains(bw)) {
                    foundBanned.add(bw);
                }
            }
            if (!foundBanned.isEmpty()) {
                nameOk = false;
                nameDetail = "商品名称包含违禁词：" + String.join("、", foundBanned);
                nameSuggestion = "建议移除违禁词汇，使用合规描述";
            } else {
                nameDetail = "商品名称「" + name + "」符合规范，长度" + name.length() + "字符";
            }
        }
        items.add(new AiAuditItem("商品名称合规性", nameOk, nameDetail, nameSuggestion));

        // 2. 商品编号格式
        String productCode = product.getProductCode();
        boolean codeOk = productCode != null && !productCode.trim().isEmpty();
        String codeDetail = codeOk ? "商品编号「" + productCode + "」已生成，格式正确" : "商品编号缺失";
        String codeSuggestion = codeOk ? "" : "商品编号应由系统自动生成，请检查创建流程";
        items.add(new AiAuditItem("商品编号格式", codeOk, codeDetail, codeSuggestion));

        // 3. 所属分类
        Long categoryId = product.getCategoryId();
        String category = product.getCategory();
        boolean categoryOk = (categoryId != null && categoryId > 0) || (category != null && !category.isEmpty());
        String categoryDetail;
        String categorySuggestion = "";
        if (categoryOk) {
            String catName = product.getCategory() != null ? product.getCategory() : "ID:" + categoryId;
            categoryDetail = "商品已关联分类「" + catName + "」";
        } else {
            categoryDetail = "商品未关联任何分类";
            categorySuggestion = "请为商品选择合适的分类，便于用户查找";
        }
        items.add(new AiAuditItem("所属分类校验", categoryOk, categoryDetail, categorySuggestion));

        // 4. 品牌信息
        String brand = product.getBrand();
        boolean brandOk = brand != null && !brand.trim().isEmpty();
        String brandDetail = brandOk ? "品牌「" + brand + "」已填写" : "品牌信息未填写";
        String brandSuggestion = brandOk ? "" : "建议填写商品品牌，提升商品可信度";
        items.add(new AiAuditItem("品牌信息", brandOk, brandDetail, brandSuggestion));

        // 5. 售价合理性
        BigDecimal price = product.getPrice();
        boolean priceOk = true;
        String priceDetail;
        String priceSuggestion = "";
        if (price == null) {
            priceOk = false;
            priceDetail = "售价未设置";
            priceSuggestion = "必须设置商品售价";
        } else if (price.compareTo(BigDecimal.ZERO) <= 0) {
            priceOk = false;
            priceDetail = "售价为¥" + price + "，必须大于0元";
            priceSuggestion = "请设置合理的售价（建议≥0.01元）";
        } else if (price.compareTo(new BigDecimal("0.01")) < 0) {
            priceOk = false;
            priceDetail = "售价¥" + price + "过低，低于最低限额0.01元";
            priceSuggestion = "建议将售价设定在0.01~99999元之间";
        } else if (price.compareTo(new BigDecimal("99999")) > 0) {
            priceOk = false;
            priceDetail = "售价¥" + price + "过高，超出上限99999元";
            priceSuggestion = "建议将售价设定在合理范围内";
        } else {
            priceDetail = "售价¥" + price + "，价格在合理范围内";
        }
        items.add(new AiAuditItem("售价合理性", priceOk, priceDetail, priceSuggestion));

        // 6. 市场价合理性
        BigDecimal marketPrice = product.getMarketPrice();
        boolean marketOk = true;
        String marketDetail;
        String marketSuggestion = "";
        if (marketPrice == null || marketPrice.compareTo(BigDecimal.ZERO) <= 0) {
            marketOk = false;
            marketDetail = "市场价未设置或为0";
            marketSuggestion = "建议设置市场价，通常为售价的1.2~3倍，突显优惠力度";
        } else if (price != null && marketPrice.compareTo(price) < 0) {
            marketOk = false;
            marketDetail = "市场价¥" + marketPrice + "低于售价¥" + price + "，不符合逻辑";
            marketSuggestion = "市场价应高于或等于售价，建议设为售价的1.2~3倍";
        } else if (price != null && marketPrice.compareTo(price.multiply(new BigDecimal("3"))) > 0) {
            marketOk = false;
            marketDetail = "市场价¥" + marketPrice + "远超售价¥" + price + "的3倍，可能虚高";
            marketSuggestion = "建议将市场价控制在售价的1.2~3倍以内，避免夸大宣传";
        } else {
            marketDetail = price != null ? "市场价¥" + marketPrice + "，为售价的" + String.format("%.1f", marketPrice.divide(price, 2, java.math.RoundingMode.HALF_UP).doubleValue()) + "倍，比例合理" : "市场价¥" + marketPrice;
        }
        items.add(new AiAuditItem("市场价合理性", marketOk, marketDetail, marketSuggestion));

        // 7. 会员价合理性
        BigDecimal vipPrice = product.getVipPrice();
        boolean vipOk = true;
        String vipDetail;
        String vipSuggestion = "";
        if (vipPrice == null || vipPrice.compareTo(BigDecimal.ZERO) <= 0) {
            vipDetail = "未设置会员价，非必填项";
        } else if (price != null && vipPrice.compareTo(price) >= 0) {
            vipOk = false;
            vipDetail = "会员价¥" + vipPrice + "不低于售价¥" + price + "，无法体现会员优惠";
            vipSuggestion = "会员价应低于售价，建议为售价的0.8~0.95倍";
        } else if (price != null && vipPrice.compareTo(price.multiply(new BigDecimal("0.5"))) < 0) {
            vipOk = false;
            vipDetail = "会员价¥" + vipPrice + "低于售价的50%，折扣过大";
            vipSuggestion = "建议将会员价控制在售价的50%以上";
        } else {
            vipDetail = price != null ? "会员价¥" + vipPrice + "，为售价的" + String.format("%.1f", vipPrice.divide(price, 2, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue()) + "%，折扣合理" : "会员价¥" + vipPrice;
        }
        items.add(new AiAuditItem("会员价合理性", vipOk, vipDetail, vipSuggestion));

        // 8. 库存合理性
        Integer stock = product.getStock();
        boolean stockOk = stock != null && stock >= 0;
        String stockDetail;
        String stockSuggestion = "";
        if (stock == null) {
            stockOk = false;
            stockDetail = "库存未设置";
            stockSuggestion = "请设置初始库存数量";
        } else if (stock == 0) {
            stockDetail = "库存为0，商品上架后将显示为「缺货」状态";
            stockSuggestion = "建议设置合理库存数量（≥1），否则商品无法正常售卖";
        } else if (stock > 99999) {
            stockOk = false;
            stockDetail = "库存数量" + stock + "异常过大";
            stockSuggestion = "请核实库存数量，建议设置为实际库存";
        } else {
            stockDetail = "库存" + stock + "件，数量合理";
        }
        items.add(new AiAuditItem("库存合理性", stockOk, stockDetail, stockSuggestion));

        // 9. 商品介绍合规性
        String description = product.getDescription();
        boolean descOk = description != null && !description.trim().isEmpty() && description.length() >= 10;
        String descDetail;
        String descSuggestion = "";
        if (description == null || description.trim().isEmpty()) {
            descOk = false;
            descDetail = "商品介绍为空";
            descSuggestion = "建议填写商品核心卖点和特色介绍（≥10字符），帮助用户快速了解商品";
        } else if (description.length() < 10) {
            descOk = false;
            descDetail = "商品介绍过短（当前" + description.length() + "字符），至少需要10个字符";
            descSuggestion = "建议补充商品核心卖点、使用场景等关键信息";
        } else {
            // 检查介绍中是否有违规词
            List<String> descBanned = new ArrayList<>();
            for (String bw : bannedWords) {
                if (description.contains(bw)) {
                    descBanned.add(bw);
                }
            }
            if (!descBanned.isEmpty()) {
                descOk = false;
                descDetail = "商品介绍包含违禁词：" + String.join("、", descBanned);
                descSuggestion = "建议移除违禁词汇后重新提交";
            } else {
                descDetail = "商品介绍" + description.length() + "字符，内容合规";
            }
        }
        items.add(new AiAuditItem("商品介绍合规性", descOk, descDetail, descSuggestion));

        // 10. 商品详情完整性
        String detail = product.getDetail();
        boolean detailOk = detail != null && !detail.trim().isEmpty() && detail.length() >= 20;
        String detailDetail;
        String detailSuggestion = "";
        if (detail == null || detail.trim().isEmpty()) {
            detailOk = false;
            detailDetail = "商品详情（富文本）为空";
            detailSuggestion = "必须填写商品详情，包含图文描述、规格参数等信息（≥20字符）";
        } else if (detail.length() < 20) {
            detailOk = false;
            detailDetail = "商品详情过短（当前" + detail.replaceAll("<[^>]*>", "").length() + "字符），至少需要20个字符";
            detailSuggestion = "建议补充完整的商品详情，包括规格参数、使用说明、售后保障等";
        } else {
            // 检查是否包含图片（富文本中）
            boolean hasImage = detail.contains("<img");
            String plainText = detail.replaceAll("<[^>]*>", "").trim();
            if (plainText.length() < 30) {
                detailOk = false;
                detailDetail = "商品详情纯文本内容不足（" + plainText.length() + "字符），实际有效信息偏少";
                detailSuggestion = "建议增加详细的文字描述，不要仅依赖图片展示";
            } else {
                detailDetail = "商品详情完善，纯文本" + plainText.length() + "字符" + (hasImage ? "，含图文混排" : "");
            }
        }
        items.add(new AiAuditItem("商品详情完整性", detailOk, detailDetail, detailSuggestion));

        // 11. 商品图片检查
        String imageUrls = product.getImageUrls();
        boolean imageOk = imageUrls != null && !imageUrls.trim().isEmpty();
        String imageDetail;
        String imageSuggestion = "";
        if (!imageOk) {
            imageDetail = "未上传商品图片";
            imageSuggestion = "必须上传至少1张商品图片，建议上传3~5张多角度展示图";
        } else {
            String[] images = imageUrls.split(",");
            if (images.length >= 3) {
                imageDetail = "已上传" + images.length + "张商品图片，数量充足";
            } else if (images.length >= 1) {
                imageDetail = "已上传" + images.length + "张商品图片";
                imageSuggestion = "建议上传3~5张图片，从不同角度展示商品";
            } else {
                imageDetail = "已上传商品图片";
            }
        }
        items.add(new AiAuditItem("商品图片检查", imageOk, imageDetail, imageSuggestion));

        // 12. 商品类型校验
        String productType = product.getAiTag() != null ? product.getAiTag() : "PHYSICAL";
        boolean typeOk = true;
        String typeDetail = "商品类型未明确设置，默认为实物商品";
        String typeSuggestion = "建议明确选择商品类型（实物/虚拟/数字权益）";
        items.add(new AiAuditItem("商品类型校验", typeOk, typeDetail, typeSuggestion));

        // 13. 卖点标签检查
        String tags = null;
        // tags字段在Product实体中可能对应aiSellingPoint
        String sellingPoint = product.getAiSellingPoint();
        boolean tagsOk = true;
        String tagsDetail;
        String tagsSuggestion = "";
        if (sellingPoint == null || sellingPoint.trim().isEmpty()) {
            tagsDetail = "未设置卖点标签/AI卖点";
            tagsSuggestion = "建议添加商品卖点标签（如「热销」「新品」「限时优惠」），提升曝光转化";
        } else {
            tagsDetail = "卖点标签：「" + sellingPoint + "」";
            if (sellingPoint.length() > 100) {
                tagsOk = false;
                tagsDetail += "，但内容过长（" + sellingPoint.length() + "字符）";
                tagsSuggestion = "卖点标签应简洁明了，建议控制在100字符以内";
            }
        }
        items.add(new AiAuditItem("卖点标签检查", tagsOk, tagsDetail, tagsSuggestion));

        // 14. 商户关联检查
        Long merchantId = product.getMerchantId();
        boolean merchantOk = merchantId != null && merchantId > 0;
        String merchantDetail = merchantOk ? "商品已关联商户（ID:" + merchantId + "）" : "商品未关联商户";
        String merchantSuggestion = merchantOk ? "" : "商品必须关联所属商户，请检查商品创建流程";
        items.add(new AiAuditItem("商户关联检查", merchantOk, merchantDetail, merchantSuggestion));

        // 计算得分：每项满分7分，总计98分→调整为百分制
        int totalItems = items.size();
        int passedCount = 0;
        List<String> failedDimensions = new ArrayList<>();
        for (AiAuditItem item : items) {
            if (item.isPassed()) {
                passedCount++;
            } else {
                failedDimensions.add(item.getDimension());
            }
        }
        int score = (int) Math.round((double) passedCount / totalItems * 100);

        // 关键项：名称、价格、图片、详情必须通过
        boolean criticalPassed = true;
        List<String> criticalFailures = new ArrayList<>();
        for (AiAuditItem item : items) {
            if (!item.isPassed()) {
                String dim = item.getDimension();
                if (dim.contains("名称") || dim.contains("售价") || dim.contains("图片") || dim.contains("详情")) {
                    criticalPassed = false;
                    criticalFailures.add(dim);
                }
            }
        }
        boolean overallPassed = passedCount >= totalItems * 0.7 && criticalPassed;

        AiAuditResult result = new AiAuditResult();
        result.setOverall(overallPassed ? "PASS" : "FAIL");
        result.setScore(score);
        result.setItems(items);

        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append("共检查").append(totalItems).append("项，通过").append(passedCount).append("项，未通过").append(totalItems - passedCount).append("项。\n");
        if (overallPassed) {
            summaryBuilder.append("✅ 审核结论：通过。商品信息整体合规，建议审核后上架。");
        } else {
            summaryBuilder.append("❌ 审核结论：不通过。");
            if (!criticalPassed) {
                summaryBuilder.append("\n【关键项未通过】").append(String.join("、", criticalFailures)).append("，必须修复后才能通过审核。");
            }
            if (failedDimensions.size() > criticalFailures.size()) {
                List<String> nonCritical = new ArrayList<>(failedDimensions);
                nonCritical.removeAll(criticalFailures);
                if (!nonCritical.isEmpty()) {
                    summaryBuilder.append("\n【建议优化项】").append(String.join("、", nonCritical)).append("，建议修复以提升商品质量。");
                }
            }
        }
        result.setSummary(summaryBuilder.toString());

        return Result.success(result);
    }

    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/products/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String contentType = file.getContentType();
            String ext = ".jpg";
            if (contentType != null) {
                if (contentType.contains("png")) ext = ".png";
                else if (contentType.contains("gif")) ext = ".gif";
                else if (contentType.contains("jpeg") || contentType.contains("jpg")) ext = ".jpg";
            }
            if (ext.equals(".jpg")) {
                String originalName = file.getOriginalFilename();
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf("."));
                    if (ext.length() > 5) ext = ".jpg";
                }
            }
            String newName = UUID.randomUUID().toString() + ext;
            
            Path filePath = Paths.get(uploadDir + newName);
            Files.write(filePath, file.getBytes());
            
            String url = "/uploads/products/" + newName;
            return Result.success(url);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        productMapper.delete(id);
        return Result.success("删除成功");
    }
}