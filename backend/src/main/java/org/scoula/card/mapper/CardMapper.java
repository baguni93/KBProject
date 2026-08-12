package org.scoula.card.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.card.domain.CardVO;
import org.scoula.card.domain.LinkedCardVO;

import java.util.List;

@Mapper
public interface CardMapper {

    // 연결 카드 목록 조회
    List<LinkedCardVO> findCardsByUserId(Long userId);

    // 연결 카드 단건 조회
    LinkedCardVO findCardById(@Param("userId") Long userId, @Param("linkedCardId") Long linkedCardId);

    // 연결 카드 개수 조회
    int countLinkedCards(Long userId);

    // 기존 대표카드 해제
    int clearRepresentCard(Long userId);

    // 대표카드 설정
    int setRepresentCard(@Param("userId") Long userId, @Param("linkedCardId") Long linkedCardId);

    // 연결 카드 삭제
    int deleteLinkedCard(@Param("userId") Long userId, @Param("linkedCardId") Long linkedCardId);

    // 다른 연결 카드 한 건 조회
    LinkedCardVO findAnotherLinkedCard(@Param("userId") Long userId, @Param("linkedCardId") Long linkedCardId);

    // card_tbl 에 카드 마스터 데이터 삽입 (시연/테스트 사전 등록용)
    int insertCard(CardVO cardVO);

    // BIN prefix(6자리)로 card_tbl에서 카드명·이미지 조회 (커스텀 카드용)
    java.util.Map<String, String> findByBinPrefix(@Param("bin") String bin);
}
