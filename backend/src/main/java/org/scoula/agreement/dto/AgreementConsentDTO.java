package org.scoula.agreement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgreementConsentDTO {

    private Long userId;

    private List<ConsentItem> agreements;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentItem {

        private Long agreementId;

        private Boolean agreed;
    }
}