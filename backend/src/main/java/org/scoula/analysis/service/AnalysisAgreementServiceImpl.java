package org.scoula.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.domain.AnalysisAgreementVO;
import org.scoula.analysis.dto.*;
import org.scoula.analysis.mapper.AnalysisAgreementMapper;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AnalysisAgreementServiceImpl
        implements AnalysisAgreementService {

    private final AnalysisAgreementMapper analysisAgreementMapper;

    @Override
    public AnalysisAgreementStatusDTO getStatus(Integer userId) {
        List<AnalysisAgreementVO> agreements = loadConfiguredAgreements(userId);

        int requiredCount = (int) agreements.stream()
                .filter(this::isRequired)
                .count();

        int agreedRequiredCount = (int) agreements.stream()
                .filter(this::isRequired)
                .filter(agreement -> "Y".equals(agreement.getAgreedYn()))
                .count();

        boolean agreed = requiredCount > 0
                && requiredCount == agreedRequiredCount;

        return AnalysisAgreementStatusDTO.builder()
                .agreed(agreed)
                .requiredCount(requiredCount)
                .agreedRequiredCount(agreedRequiredCount)
                .message(agreed
                        ? "소비 분석 필수 약관에 동의했습니다."
                        : "소비 분석을 시작하려면 필수 약관 동의가 필요합니다.")
                .build();
    }

    @Override
    public AnalysisAgreementListDTO getAgreements(Integer userId) {
        List<AnalysisAgreementVO> agreements = loadConfiguredAgreements(userId);

        List<AnalysisAgreementItemDTO> items = agreements.stream()
                .map(agreement -> AnalysisAgreementItemDTO.builder()
                        .agreementId(agreement.getAgreementId())
                        .agreementType(agreement.getAgreementType())
                        .agreementName(agreement.getAgreementName())
                        .agreementContent(agreement.getAgreementContent())
                        .requiredYn(agreement.getRequiredYn())
                        .agreedYn(agreement.getAgreedYn())
                        .build())
                .collect(Collectors.toList());

        boolean completed = agreements.stream()
                .filter(this::isRequired)
                .allMatch(agreement -> "Y".equals(agreement.getAgreedYn()));

        return AnalysisAgreementListDTO.builder()
                .agreementCount(items.size())
                .requiredAgreementCompleted(completed)
                .agreements(items)
                .build();
    }

    @Override
    @Transactional
    public AnalysisAgreementStatusDTO saveAgreements(
            Integer userId,
            AnalysisAgreementConsentRequestDTO request
    ) {
        List<AnalysisAgreementVO> configured = loadConfiguredAgreements(userId);
        List<AnalysisAgreementConsentItemDTO> submitted =
                request == null || request.getAgreements() == null
                        ? Collections.emptyList()
                        : request.getAgreements();

        Map<Integer, String> submittedAgreementMap = new HashMap<>();
        for (AnalysisAgreementConsentItemDTO item : submitted) {
            if (item == null || item.getAgreementId() == null) {
                continue;
            }
            String agreedYn = normalizeAgreedYn(item.getAgreedYn());
            submittedAgreementMap.put(item.getAgreementId(), agreedYn);
        }

        for (AnalysisAgreementVO agreement : configured) {
            String agreedYn = submittedAgreementMap.getOrDefault(
                    agreement.getAgreementId(),
                    "N"
            );

            if (isRequired(agreement) && !"Y".equals(agreedYn)) {
                throw new CustomException(
                        ErrorCode.ANALYSIS_REQUIRED_AGREEMENT_MISSING
                );
            }
        }

        for (AnalysisAgreementVO agreement : configured) {
            String agreedYn = submittedAgreementMap.getOrDefault(
                    agreement.getAgreementId(),
                    "N"
            );

            Integer userAgreementId =
                    analysisAgreementMapper.selectLatestUserAgreementId(
                            userId,
                            agreement.getAgreementId()
                    );

            int affectedRows;
            if (userAgreementId == null) {
                affectedRows = analysisAgreementMapper.insertUserAgreement(
                        userId,
                        agreement.getAgreementId(),
                        agreedYn
                );
            } else {
                affectedRows = analysisAgreementMapper.updateUserAgreement(
                        userAgreementId,
                        agreedYn
                );
            }

            if (affectedRows != 1) {
                throw new CustomException(ErrorCode.DATA_ACCESS_ERROR);
            }
        }

        log.info("소비분석 약관 저장 완료 userId={}, agreementCount={}",
                userId, configured.size());

        return getStatus(userId);
    }

    private List<AnalysisAgreementVO> loadConfiguredAgreements(Integer userId) {
        List<AnalysisAgreementVO> agreements =
                analysisAgreementMapper.selectAnalysisAgreements(userId);

        if (agreements == null || agreements.isEmpty()) {
            throw new CustomException(
                    ErrorCode.ANALYSIS_AGREEMENT_NOT_CONFIGURED
            );
        }
        return agreements;
    }

    private boolean isRequired(AnalysisAgreementVO agreement) {
        return "Y".equals(agreement.getRequiredYn());
    }

    private String normalizeAgreedYn(String value) {
        return "Y".equalsIgnoreCase(value) ? "Y" : "N";
    }
}
