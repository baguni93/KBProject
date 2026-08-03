package org.scoula.card.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.card.domain.LinkedCardVO;
import org.scoula.card.dto.CardDTO;
import org.scoula.card.mapper.CardMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardMapper cardMapper;

    // 연결 카드 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<CardDTO> getCards(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("회원번호가 필요합니다.");
        }

        return cardMapper.findCardsByUserId(userId).stream()
                .map(CardDTO::of)
                .collect(Collectors.toList());
    }

    // 대표카드 설정
    @Override
    @Transactional
    public boolean setRepresentCard(Long userId, Long linkedCardId) {
        LinkedCardVO card = cardMapper.findCardById(userId, linkedCardId);

        if (card == null) {
            throw new IllegalArgumentException("연결된 카드를 찾을 수 없습니다.");
        }

        if ("Y".equals(card.getRepresentYn())) return true;

        cardMapper.clearRepresentCard(userId);

        int result = cardMapper.setRepresentCard(userId, linkedCardId);

        if (result != 1) {
            throw new IllegalStateException("대표카드 설정에 실패했습니다.");
        }

        log.info("대표카드 변경 완료: userId={}, linkedCardId={}", userId, linkedCardId);

        return true;
    }

    // 카드 연결 해제
    @Override
    @Transactional
    public boolean disconnectCard(Long userId, Long linkedCardId) {
        LinkedCardVO card = cardMapper.findCardById(userId, linkedCardId);

        if (card == null) {
            throw new IllegalArgumentException("연결된 카드를 찾을 수 없습니다.");
        }

        int linkedCardCount = cardMapper.countLinkedCards(userId);

        if (linkedCardCount <= 1) {
            throw new IllegalArgumentException("마지막 카드는 연결 해제할 수 없습니다.");
        }

        LinkedCardVO nextRepresentCard = null;

        if ("Y".equals(card.getRepresentYn())) {
            nextRepresentCard = cardMapper.findAnotherLinkedCard(userId, linkedCardId);

            if (nextRepresentCard == null) {
                throw new IllegalStateException("대표카드로 설정할 다른 카드를 찾을 수 없습니다.");
            }
        }

        int result = cardMapper.deleteLinkedCard(userId, linkedCardId);

        if (result != 1) {
            throw new IllegalStateException("카드 연결 해제에 실패했습니다.");
        }

        if (nextRepresentCard != null) {
            int representResult = cardMapper.setRepresentCard(userId, nextRepresentCard.getLinkedCardId());

            if (representResult != 1) {
                throw new IllegalStateException("대표카드 재설정에 실패했습니다.");
            }
        }

        log.info("카드 연결 해제 완료: userId={}, linkedCardId={}", userId, linkedCardId);

        return true;
    }
}