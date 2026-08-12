package org.scoula.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 커스텀 카드 마스터 등록 요청 DTO
 * - 디자인팀이 만든 커스텀 카드를 card_tbl 에 등록할 때 사용
 * - 카드번호(16자리), CVV, 유효기간은 서버에서 자동 생성
 * - BIN 앞자리는 커스텀 전용 BIN 풀(421029, 463654, 484404, 463652) 에서 랜덤 선택
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardCustomCreateDTO {

    /** 카드 이름 (디자인팀이 지정한 이름) */
    private String cardName;

    /** 카드 이미지 파일명 (예: custom_card_001.png) */
    private String cardImgFileName;

    /** 카드 비밀번호 4자리 */
    private String cardPassword;
}
