package org.scoula.analysis.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.analysis.domain.MerchantCategoryMappingVO;

public interface MerchantCategoryMappingMapper {

    /**
     * 가맹점명으로 기존 카테고리 매핑 조회
     *
     * @param merchantName 앞뒤 공백이 제거된 가맹점명
     * @return 매핑 정보, 존재하지 않으면 null
     */
    MerchantCategoryMappingVO selectByMerchantName(
            @Param("merchantName") String merchantName
    );

    /**
     * AI 분류 결과를 기반으로 신규 가맹점 매핑 생성
     *
     * @param mapping 생성할 매핑 정보
     * @return INSERT된 행 개수
     */
    int insertMapping(
            MerchantCategoryMappingVO mapping
    );

    /**
     * 사용자의 카테고리 수정 요청 횟수 1 증가
     *
     * @param merchantName 수정 대상 가맹점명
     * @return UPDATE된 행 개수
     */
    int increaseCorrectionCount(
            @Param("merchantName") String merchantName
    );

    /**
     * 오류 의심률이 5% 이상인 가맹점 매핑 삭제
     *
     * @param merchantName 삭제 대상 가맹점명
     * @return DELETE된 행 개수
     */
    int deleteByMerchantName(
            @Param("merchantName") String merchantName
    );


}