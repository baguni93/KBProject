package org.scoula.card.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * card_tbl 실물 카드 원장 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardVO {

    private Integer cardCode;          // card_code (PK, AUTO_INCREMENT)
    private String  cardNum;           // card_num  (16자리)
    private String  expiryDate;        // expiry_date (MM/YY)
    private String  cvv;               // cvv
    private String  cardPassword;      // card_password (4자리)
    private String  cardImgFileName;   // card_img_file_name
    private String  cardName;          // card_name
}
