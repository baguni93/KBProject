package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatusDTO;

/*
 * 프론트에서 카드 추천 작업을 "시작"하고 "진행상태를 조회"할 때 사용하는 서비스.
 * 실제 추천 계산은 AsyncWorker가 별도 스레드에서 수행한다.
 */
public interface CardRecommendationAsyncService {

    // 추천 작업 시작 요청. 이미 실행 중이거나 DB에 결과가 있으면 새 작업을 만들지 않는다.
    CardRecommendationTaskStatusDTO start(
            Integer userId,
            Integer spendingAnalysisId
    );

    // 현재 작업 상태(PROCESSING/COMPLETED/FAILED/IDLE)를 조회한다.
    CardRecommendationTaskStatusDTO getStatus(
            Integer userId,
            Integer spendingAnalysisId
    );
}
