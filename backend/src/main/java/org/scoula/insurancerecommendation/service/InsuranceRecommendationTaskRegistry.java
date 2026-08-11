package org.scoula.insurancerecommendation.service;

import org.scoula.insurancerecommendation.domain.InsuranceRecommendationTaskState;
import org.scoula.insurancerecommendation.dto.InsuranceRecommendationTaskStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * 현재 실행 중인 보험추천 비동기 작업 상태를 Tomcat 메모리에 저장한다.
 * 카드추천과 동일하게 userId + spendingAnalysisId 조합을 작업 키로 사용한다.
 */
@Service
public class InsuranceRecommendationTaskRegistry {

    private final ConcurrentMap<String, InsuranceRecommendationTaskState> taskStates =
            new ConcurrentHashMap<>();

    public InsuranceRecommendationTaskState get(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        return taskStates.get(createKey(userId, spendingAnalysisId));
    }

    /*
     * 같은 사용자/소비분석 조합의 작업이 이미 PROCESSING이면
     * 새 작업을 시작하지 않는다.
     */
    public boolean begin(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        AtomicBoolean started = new AtomicBoolean(false);
        String key = createKey(userId, spendingAnalysisId);

        taskStates.compute(key, (ignored, current) -> {
            if (current != null
                    && current.getStatus()
                    == InsuranceRecommendationTaskStatus.PROCESSING) {
                return current;
            }

            started.set(true);
            return new InsuranceRecommendationTaskState(
                    InsuranceRecommendationTaskStatus.PROCESSING,
                    null,
                    null,
                    "12개월 실제 거래를 분석해 보험을 추천하고 있습니다."
            );
        });

        return started.get();
    }

    public void complete(
            Integer userId,
            Integer spendingAnalysisId,
            Integer recommendationCount,
            Boolean created,
            String message
    ) {
        taskStates.put(
                createKey(userId, spendingAnalysisId),
                new InsuranceRecommendationTaskState(
                        InsuranceRecommendationTaskStatus.COMPLETED,
                        recommendationCount,
                        created,
                        message == null || message.isBlank()
                                ? "보험 추천이 완료되었습니다."
                                : message
                )
        );
    }

    public void fail(
            Integer userId,
            Integer spendingAnalysisId,
            String message
    ) {
        taskStates.put(
                createKey(userId, spendingAnalysisId),
                new InsuranceRecommendationTaskState(
                        InsuranceRecommendationTaskStatus.FAILED,
                        null,
                        null,
                        message == null || message.isBlank()
                                ? "보험 추천 처리 중 오류가 발생했습니다."
                                : message
                )
        );
    }

    private String createKey(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        return userId + ":" + spendingAnalysisId;
    }
}
