package org.scoula.insurancerecommendation.dto;

/** KB손해보험 상품 안내 페이지에서 수집한 주요 보장 항목. */
public class CrawledInsuranceCoverageDTO {

    private Integer insuranceProductId;
    private String coverageName;
    private Integer coverageAmount;
    private String coverageDescription;
    private String coverageLimit;

    public Integer getInsuranceProductId() {
        return insuranceProductId;
    }

    public void setInsuranceProductId(Integer insuranceProductId) {
        this.insuranceProductId = insuranceProductId;
    }

    public String getCoverageName() {
        return coverageName;
    }

    public void setCoverageName(String coverageName) {
        this.coverageName = coverageName;
    }

    public Integer getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(Integer coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public String getCoverageDescription() {
        return coverageDescription;
    }

    public void setCoverageDescription(String coverageDescription) {
        this.coverageDescription = coverageDescription;
    }

    public String getCoverageLimit() {
        return coverageLimit;
    }

    public void setCoverageLimit(String coverageLimit) {
        this.coverageLimit = coverageLimit;
    }
}
