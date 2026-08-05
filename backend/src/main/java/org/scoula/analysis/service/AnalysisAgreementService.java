package org.scoula.analysis.service;

import org.scoula.analysis.dto.AnalysisAgreementConsentRequestDTO;
import org.scoula.analysis.dto.AnalysisAgreementListDTO;
import org.scoula.analysis.dto.AnalysisAgreementStatusDTO;

public interface AnalysisAgreementService {
    AnalysisAgreementStatusDTO getStatus(Integer userId);
    AnalysisAgreementListDTO getAgreements(Integer userId);
    AnalysisAgreementStatusDTO saveAgreements(
            Integer userId,
            AnalysisAgreementConsentRequestDTO request
    );
}
