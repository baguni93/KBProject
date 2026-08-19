package org.scoula.card.service;

import org.scoula.card.domain.CardVO;
import org.scoula.card.dto.CardCustomCreateDTO;
import org.scoula.card.dto.CardDTO;
import org.scoula.card.dto.CardMasterCreateDTO;

import java.util.List;

public interface CardService {

    // 연결 카드 목록 조회
    List<CardDTO> getCards(Long userId);

    // 대표카드 설정
    boolean setRepresentCard(Long userId, Long linkedCardId);

    // 카드 연결 해제
    boolean disconnectCard(Long userId, Long linkedCardId);

    /**
     * BIN 매핑 기반 카드 마스터 등록
     * - cardName 으로 BIN_MAPPING_MAP 조회 → BIN + 이미지파일명 자동
     * - 16자리 카드번호, CVV, 유효기간, 비밀번호 자동 생성
     */
    CardVO createCardMaster(CardMasterCreateDTO dto);

    /**
     * 커스텀 카드 마스터 등록
     * - 디자인팀이 만든 카드: cardName + cardImgFileName + cardPassword 입력
     * - BIN 은 커스텀 전용 풀(421029, 463654, 484404, 463652) 에서 랜덤 선택
     * - 16자리 카드번호, CVV, 유효기간 자동 생성
     */
    CardVO createCardMasterCustom(CardCustomCreateDTO dto);

    /**
     * 53개 전체 카드 마스터 일괄 자동 등록 (Admin/개발용)
     */
    List<CardVO> createAllCardMasters();
}
