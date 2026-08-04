package org.scoula.analysis.service;

import org.scoula.analysis.dto.AiCategoryClassificationResultDTO;

public interface AiCategoryClassificationService {

    /**
     * 가맹점명을 GPT에 전달하여 소비 카테고리를 분류한다.
     * merchantName 앞뒤 공백이 제거된 가맹점명
     * AI 카테고리 분류 결과 리턴( 분류 또는 API 호출에 실패하면 null)
     */
    AiCategoryClassificationResultDTO classify(
            String merchantName
    );
}