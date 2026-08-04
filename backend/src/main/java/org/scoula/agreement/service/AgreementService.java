package org.scoula.agreement.service;

import org.scoula.agreement.dto.AgreementConsentDTO;
import org.scoula.agreement.dto.AgreementDTO;
import org.scoula.agreement.dto.AgreementDetailDTO;

import java.util.List;

public interface AgreementService {

    // 약관 목록 조회
    List<AgreementDTO> getAgreements();

    // 약관 상세 조회
    AgreementDetailDTO getAgreementDetail(String agreementType);

    // 회원 약관 동의 저장
    void saveConsent(AgreementConsentDTO consentDTO);
}