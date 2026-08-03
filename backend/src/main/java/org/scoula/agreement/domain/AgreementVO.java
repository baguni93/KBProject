package org.scoula.agreement.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgreementVO {

    private Long agreementId;

    private String agreementType;

    private String agreementName;

    private String agreementContent;

    private String requiredYn;

    private String useYn;
}