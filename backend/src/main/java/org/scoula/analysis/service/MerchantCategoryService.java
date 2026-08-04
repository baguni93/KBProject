package org.scoula.analysis.service;

import org.scoula.analysis.dto.MerchantCategoryClassificationResultDTO;

public interface MerchantCategoryService {

    /**
     * 가맹점명을 기준으로 소비 카테고리를 결정한다.
     *
     * 처리 순서:
     * 1. 가맹점명 앞뒤 공백 trim 연산
     * 2. 기존 가맹점 카테고리 매핑 조회
     * 3. 매핑이 있으면 기존 카테고리 반환
     * 4. 매핑이 없으면 AI 카테고리 분류
     * 5. AI 분류 성공 시 신규 매핑 생성
     * 6. AI 분류 실패 시 미분류 결과 반환
     *
     * @param merchantName 거래에 기록된 가맹점명
     * @return 가맹점 카테고리 분류 결과
     */
    MerchantCategoryClassificationResultDTO classify(
            String merchantName
    );
}