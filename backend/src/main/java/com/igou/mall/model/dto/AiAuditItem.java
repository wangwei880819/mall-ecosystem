package com.igou.mall.model.dto;

public class AiAuditItem {
    private String dimension;
    private boolean passed;
    private String detail;
    private String suggestion;

    public AiAuditItem() {}

    public AiAuditItem(String dimension, boolean passed, String detail, String suggestion) {
        this.dimension = dimension;
        this.passed = passed;
        this.detail = detail;
        this.suggestion = suggestion;
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
