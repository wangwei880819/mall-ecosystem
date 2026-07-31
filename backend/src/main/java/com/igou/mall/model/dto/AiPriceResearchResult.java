package com.igou.mall.model.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 价格智能摸排结果
 */
public class AiPriceResearchResult {
    private String overall;              // 价格合理性评估：REASONABLE/HIGH/LOW
    private int score;                   // 价格评分 0-100
    private BigDecimal suggestedPrice;   // 建议售价
    private BigDecimal priceLower;       // 建议价格区间下限
    private BigDecimal priceUpper;       // 建议价格区间上限
    private List<Competitor> competitors; // 竞品价格对比
    private List<PriceItem> items;       // 分析明细
    private String summary;              // 总结建议

    public String getOverall() { return overall; }
    public void setOverall(String overall) { this.overall = overall; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public BigDecimal getSuggestedPrice() { return suggestedPrice; }
    public void setSuggestedPrice(BigDecimal suggestedPrice) { this.suggestedPrice = suggestedPrice; }
    public BigDecimal getPriceLower() { return priceLower; }
    public void setPriceLower(BigDecimal priceLower) { this.priceLower = priceLower; }
    public BigDecimal getPriceUpper() { return priceUpper; }
    public void setPriceUpper(BigDecimal priceUpper) { this.priceUpper = priceUpper; }
    public List<Competitor> getCompetitors() { return competitors; }
    public void setCompetitors(List<Competitor> competitors) { this.competitors = competitors; }
    public List<PriceItem> getItems() { return items; }
    public void setItems(List<PriceItem> items) { this.items = items; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public static class Competitor {
        private String platform;     // 平台名称
        private String productName;  // 竞品名称
        private BigDecimal price;    // 竞品价格

        public Competitor() {}
        public Competitor(String platform, String productName, BigDecimal price) {
            this.platform = platform; this.productName = productName; this.price = price;
        }
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    public static class PriceItem {
        private String dimension;
        private boolean passed;
        private String detail;
        private String suggestion;

        public PriceItem() {}
        public PriceItem(String dimension, boolean passed, String detail, String suggestion) {
            this.dimension = dimension; this.passed = passed;
            this.detail = detail; this.suggestion = suggestion;
        }
        public String getDimension() { return dimension; }
        public void setDimension(String dimension) { this.dimension = dimension; }
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }
}
