package org.scoula.agreement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.agreement.domain.AgreementVO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgreementDetailDTO {

    private Long agreementId;

    private String agreementType;

    private String agreementName;

    private String agreementContent;

    private String requiredYn;

    public static AgreementDetailDTO of(
            AgreementVO agreement
    ) {
        return AgreementDetailDTO.builder()
                .agreementId(
                        agreement.getAgreementId()
                )
                .agreementType(
                        agreement.getAgreementType()
                )
                .agreementName(
                        agreement.getAgreementName()
                )
                .agreementContent(
                        agreement.getAgreementContent()
                )
                .requiredYn(
                        agreement.getRequiredYn()
                )
                .build();
    }
}