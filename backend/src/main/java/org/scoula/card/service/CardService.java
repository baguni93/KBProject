package org.scoula.card.service;

import org.scoula.card.dto.CardDTO;

import java.util.List;

public interface CardService {

    // 연결 카드 목록 조회
    List<CardDTO> getCards(Long userId);

    // 대표카드 설정
    boolean setRepresentCard(Long userId, Long linkedCardId);

    // 카드 연결 해제
    boolean disconnectCard(Long userId, Long linkedCardId);
}
