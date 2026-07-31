package com.igou.mall.model.dto;

import java.util.List;

/**
 * AI 文案智能校对结果
 */
public class AiProofResult {
    private int score;              // 文案质量评分 0-100
    private String correctedText;   // 校对后文本
    private List<ProofItem> items;  // 校对明细
    private String summary;         // 总体评价

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getCorrectedText() { return correctedText; }
    public void setCorrectedText(String correctedText) { this.correctedText = correctedText; }
    public List<ProofItem> getItems() { return items; }
    public void setItems(List<ProofItem> items) { this.items = items; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public static class ProofItem {
        private String type;        // 问题类型：TYPO/SENSITIVE/FORMAT/LENGTH/COMPLIANCE
        private String position;    // 问题位置描述
        private String original;    // 原文
        private String suggestion;  // 修改建议
        private String severity;    // 严重程度：HIGH/MEDIUM/LOW

        public ProofItem() {}
        public ProofItem(String type, String position, String original, String suggestion, String severity) {
            this.type = type; this.position = position; this.original = original;
            this.suggestion = suggestion; this.severity = severity;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public String getOriginal() { return original; }
        public void setOriginal(String original) { this.original = original; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
}
