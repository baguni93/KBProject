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
public class AgreementDTO {

    private Long agreementId;

    private String agreementType;

    private String agreementName;

    private String requiredYn;

    public static AgreementDTO of(
            AgreementVO agreement
    ) {
        return AgreementDTO.builder()
                .agreementId(
                        agreement.getAgreementId()
                )
                .agreementType(
                        agreement.getAgreementType()
                )
                .agreementName(
                        agreement.getAgreementName()
                )
                .requiredYn(
                        agreement.getRequiredYn()
                )
                .build();
    }
}