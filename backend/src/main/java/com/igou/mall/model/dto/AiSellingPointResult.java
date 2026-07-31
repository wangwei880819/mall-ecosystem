package com.igou.mall.model.dto;

import java.util.List;

/**
 * AI 商品卖点提炼结果
 */
public class AiSellingPointResult {
    private String sellingPoint;       // 核心卖点（1-2句话）
    private List<String> tags;         // 标签列表（3-5个）
    private String marketingCopy;      // 营销文案
    private String shortTitle;         // 短标题（适合列表展示）
    private List<TagItem> tagAnalysis; // 标签分析明细
    private int score;                 // 卖点质量评分 0-100
    private String summary;            // 分析总结

    public String getSellingPoint() { return sellingPoint; }
    public void setSellingPoint(String sellingPoint) { this.sellingPoint = sellingPoint; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getMarketingCopy() { return marketingCopy; }
    public void setMarketingCopy(String marketingCopy) { this.marketingCopy = marketingCopy; }
    public String getShortTitle() { return shortTitle; }
    public void setShortTitle(String shortTitle) { this.shortTitle = shortTitle; }
    public List<TagItem> getTagAnalysis() { return tagAnalysis; }
    public void setTagAnalysis(List<TagItem> tagAnalysis) { this.tagAnalysis = tagAnalysis; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public static class TagItem {
        private String tag;
        private int relevance; // 相关度 0-100

        public TagItem() {}
        public TagItem(String tag, int relevance) { this.tag = tag; this.relevance = relevance; }
        public String getTag() { return tag; }
        public void setTag(String tag) { this.tag = tag; }
        public int getRelevance() { return relevance; }
        public void setRelevance(int relevance) { this.relevance = relevance; }
    }
}
