package org.scoula.insurancerecommendation.domain;

import org.scoula.insurancerecommendation.dto.InsuranceRecommendationTaskStatus;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationTaskStatusDTO;

/*
 * 비동기 보험추천 작업의 현재 상태를 Tomcat 메모리에 보관하는 객체.
 * 카드추천과 동일하게 상태가 바뀔 때 새 객체를 만들어 Registry에 저장한다.
 */
public class InsuranceRecommendationTaskState {

    private final InsuranceRecommendationTaskStatus status;
    private final Integer recommendationCount;
    private final Boolean created;
    private final String message;

    public InsuranceRecommendationTaskState(
            InsuranceRecommendationTaskStatus status,
            Integer recommendationCount,
            Boolean created,
            String message
    ) {
        this.status = status;
        this.recommendationCount = recommendationCount;
        this.created = created;
        this.message = message;
    }

    public InsuranceRecommendationTaskStatus getStatus() {
        return status;
    }

    public Integer getRecommendationCount() {
        return recommendationCount;
    }

    public Boolean getCreated() {
        return created;
    }

    public String getMessage() {
        return message;
    }

    // 상태 VO -> API 응답 DTO 변환도 VO 내부에서 수행한다.
    public InsuranceRecommendationTaskStatusDTO toDTO(
            Integer spendingAnalysisId
    ) {
        return new InsuranceRecommendationTaskStatusDTO(
                spendingAnalysisId,
                status.name(),
                recommendationCount,
                created,
                message
        );
    }
}
