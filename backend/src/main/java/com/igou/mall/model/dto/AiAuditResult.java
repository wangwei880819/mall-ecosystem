package com.igou.mall.model.dto;

import java.util.List;

public class AiAuditResult {
    private String overall;
    private int score;
    private List<AiAuditItem> items;
    private String summary;

    public String getOverall() { return overall; }
    public void setOverall(String overall) { this.overall = overall; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public List<AiAuditItem> getItems() { return items; }
    public void setItems(List<AiAuditItem> items) { this.items = items; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}
