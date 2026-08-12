package org.scoula.customcard.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomCardAgreementVO {

    private int agreementId;
    private String agreementName;
    private String agreementContent;
}
