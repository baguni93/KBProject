package org.scoula.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for batch creation of card master records.
 * Optional fields (cardImgFileName, cardName) will be applied to every generated card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardBatchCreateDTO {

    private Integer accountId;


    private Integer count;

    /**
     * 카드 이미지 파일명 (선택). 지정하지 않으면 기본 이미지가 사용됩니다.
     */
    private String cardImgFileName;

    /**
     * 카드 이름 (선택). 지정하지 않으면 기본 이름이 사용됩니다.
     */
    private String cardName;
}
