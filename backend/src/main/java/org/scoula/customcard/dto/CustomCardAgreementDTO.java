package org.scoula.customcard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.customcard.domain.CustomCardAgreementVO;
import org.scoula.customcard.domain.CustomCardVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomCardAgreementDTO {

    private int agreementId;
    private String agreementName;
    private String agreementContent;

    public static CustomCardAgreementDTO of(CustomCardAgreementVO customCardAgreementVO){
        return customCardAgreementVO == null ? null : CustomCardAgreementDTO
                .builder()
                .agreementId(customCardAgreementVO.getAgreementId())
                .agreementName(customCardAgreementVO.getAgreementName())
                .agreementContent(customCardAgreementVO.getAgreementContent())
                .build();
    }
}
