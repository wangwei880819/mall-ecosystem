package com.igou.mall.model.dto;

/**
 * AI OCR 智能识别结果
 */
public class AiOcrResult {
    private int score;           // 识别置信度 0-100
    private String rawText;      // 原始识别文本
    private OcrField companyName;     // 企业名称
    private OcrField creditCode;      // 统一社会信用代码
    private OcrField legalPerson;     // 法定代表人
    private OcrField registeredCapital; // 注册资本
    private OcrField businessScope;   // 经营范围
    private OcrField address;         // 注册地址
    private OcrField establishDate;   // 成立日期

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public OcrField getCompanyName() { return companyName; }
    public void setCompanyName(OcrField companyName) { this.companyName = companyName; }
    public OcrField getCreditCode() { return creditCode; }
    public void setCreditCode(OcrField creditCode) { this.creditCode = creditCode; }
    public OcrField getLegalPerson() { return legalPerson; }
    public void setLegalPerson(OcrField legalPerson) { this.legalPerson = legalPerson; }
    public OcrField getRegisteredCapital() { return registeredCapital; }
    public void setRegisteredCapital(OcrField registeredCapital) { this.registeredCapital = registeredCapital; }
    public OcrField getBusinessScope() { return businessScope; }
    public void setBusinessScope(OcrField businessScope) { this.businessScope = businessScope; }
    public OcrField getAddress() { return address; }
    public void setAddress(OcrField address) { this.address = address; }
    public OcrField getEstablishDate() { return establishDate; }
    public void setEstablishDate(OcrField establishDate) { this.establishDate = establishDate; }

    public static class OcrField {
        private String value;
        private int confidence; // 0-100
        public OcrField() {}
        public OcrField(String value, int confidence) { this.value = value; this.confidence = confidence; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public int getConfidence() { return confidence; }
        public void setConfidence(int confidence) { this.confidence = confidence; }
    }
}
