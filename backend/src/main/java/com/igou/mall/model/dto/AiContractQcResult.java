package com.igou.mall.model.dto;

import java.util.List;

/**
 * AI 合同智能质检结果
 */
public class AiContractQcResult {
    private String overall;         // 总体评价：PASS/WARNING/REJECT
    private int score;              // 合同质量评分 0-100
    private List<QcItem> items;    // 检查明细
    private List<QcItem> riskItems; // 风险条款
    private String summary;         // 质检总结

    public String getOverall() { return overall; }
    public void setOverall(String overall) { this.overall = overall; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public List<QcItem> getItems() { return items; }
    public void setItems(List<QcItem> items) { this.items = items; }
    public List<QcItem> getRiskItems() { return riskItems; }
    public void setRiskItems(List<QcItem> riskItems) { this.riskItems = riskItems; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public static class QcItem {
        private String dimension;   // 检查维度
        private boolean passed;     // 是否通过
        private String detail;      // 详情
        private String suggestion;  // 建议

        public QcItem() {}
        public QcItem(String dimension, boolean passed, String detail, String suggestion) {
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
