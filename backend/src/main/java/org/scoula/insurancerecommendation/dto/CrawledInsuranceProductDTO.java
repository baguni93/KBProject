package org.scoula.insurancerecommendation.dto;

import java.util.ArrayList;
import java.util.List;

/** KB손해보험 상품 메뉴와 상품 안내 페이지에서 수집한 상품 정보. */
public class CrawledInsuranceProductDTO {

    private Integer insuranceProductId;

    /* DB에는 저장하지 않고 중복 제거와 추천 매핑 규칙에 사용하는 공식 페이지 코드. */
    private String sourceCode;

    private String insuranceName;
    private String insuranceCategory;
    private String insuranceDescription;
    private Integer monthlyPremium;
    private String insuranceImage;
    private String applicationUrl;

    private List<CrawledInsuranceCoverageDTO> coverages = new ArrayList<>();

    public Integer getInsuranceProductId() {
        return insuranceProductId;
    }

    public void setInsuranceProductId(Integer insuranceProductId) {
        this.insuranceProductId = insuranceProductId;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getInsuranceName() {
        return insuranceName;
    }

    public void setInsuranceName(String insuranceName) {
        this.insuranceName = insuranceName;
    }

    public String getInsuranceCategory() {
        return insuranceCategory;
    }

    public void setInsuranceCategory(String insuranceCategory) {
        this.insuranceCategory = insuranceCategory;
    }

    public String getInsuranceDescription() {
        return insuranceDescription;
    }

    public void setInsuranceDescription(String insuranceDescription) {
        this.insuranceDescription = insuranceDescription;
    }

    public Integer getMonthlyPremium() {
        return monthlyPremium;
    }

    public void setMonthlyPremium(Integer monthlyPremium) {
        this.monthlyPremium = monthlyPremium;
    }

    public String getInsuranceImage() {
        return insuranceImage;
    }

    public void setInsuranceImage(String insuranceImage) {
        this.insuranceImage = insuranceImage;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public List<CrawledInsuranceCoverageDTO> getCoverages() {
        return coverages;
    }

    public void setCoverages(List<CrawledInsuranceCoverageDTO> coverages) {
        this.coverages = coverages == null ? new ArrayList<>() : coverages;
    }
}
