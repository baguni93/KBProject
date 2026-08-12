package org.scoula.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BIN 매핑 기반 카드 마스터 등록 요청 DTO
 * - cardName 으로 BIN_MAPPING_MAP 에서 BIN 6자리 + 이미지파일명을 자동 조회
 * - 카드번호(16자리), CVV, 유효기간, 비밀번호는 서버에서 자동 생성
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardMasterCreateDTO {

    /** 카드 이름 (BIN_MAPPING_MAP 에 등록된 카드명과 정확히 일치해야 함) */
    private String cardName;
}
