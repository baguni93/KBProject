package org.scoula.cardrecommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardRecommendationTaskStatusDTO {
    // 서버의 비동기 작업 상태를 프론트에 JSON으로 보내는 객체이다.
    private Integer spendingAnalysisId; // 어떤 소비분석에 대한 카드추천인지?
    private String status; //현재 비동기 작업의 상태
    private Integer recommendationCount; //생성되거나 조회된 카드 추천 결과의 총개수
    private Boolean created; // 이번 요청으로 새로 추천을 생성했는지
    private String message; // 프론트에 보여줄 사용자 안내 문구
}
