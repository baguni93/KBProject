package org.scoula.agreement.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.agreement.domain.AgreementVO;
import org.scoula.agreement.dto.AgreementConsentDTO;
import org.scoula.agreement.dto.AgreementDTO;
import org.scoula.agreement.dto.AgreementDetailDTO;
import org.scoula.agreement.mapper.AgreementMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AgreementServiceImpl
        implements AgreementService {

    private final AgreementMapper agreementMapper;


    // 약관 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<AgreementDTO> getAgreements() {

        List<AgreementVO> agreements =
                agreementMapper.findAllActive();

        return agreements.stream()
                .map(AgreementDTO::of)
                .collect(Collectors.toList());
    }


    // 약관 상세 조회
    @Override
    @Transactional(readOnly = true)
    public AgreementDetailDTO getAgreementDetail(
            String agreementType
    ) {
        if (agreementType == null
                || agreementType.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "약관 유형이 필요합니다."
            );
        }

        AgreementVO agreement =
                agreementMapper.findByAgreementType(
                        agreementType.trim()
                                .toUpperCase()
                );

        if (agreement == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 약관입니다."
            );
        }

        return AgreementDetailDTO.of(
                agreement
        );
    }


    // 회원 약관 동의 저장
    @Override
    @Transactional
    public void saveConsent(
            AgreementConsentDTO consentDTO
    ) {
        if (consentDTO == null) {
            throw new IllegalArgumentException(
                    "약관 동의 정보가 필요합니다."
            );
        }

        if (consentDTO.getUserId() == null) {
            throw new IllegalArgumentException(
                    "회원번호가 필요합니다."
            );
        }

        if (consentDTO.getAgreements() == null
                || consentDTO.getAgreements().isEmpty()) {
            throw new IllegalArgumentException(
                    "약관 동의 항목이 필요합니다."
            );
        }

        for (
                AgreementConsentDTO.ConsentItem item
                : consentDTO.getAgreements()
        ) {
            validateConsentItem(item);

            AgreementVO agreement =
                    agreementMapper.findById(
                            item.getAgreementId()
                    );

            if (agreement == null) {
                throw new IllegalArgumentException(
                        "존재하지 않는 약관입니다."
                );
            }

            boolean agreed =
                    Boolean.TRUE.equals(
                            item.getAgreed()
                    );

            if ("Y".equals(
                    agreement.getRequiredYn()
            ) && !agreed) {
                throw new IllegalArgumentException(
                        agreement.getAgreementName()
                                + "은(는) 필수 약관입니다."
                );
            }

            String agreedYn =
                    agreed ? "Y" : "N";

            int count =
                    agreementMapper.countUserAgreement(
                            consentDTO.getUserId(),
                            item.getAgreementId()
                    );

            int result;

            if (count == 0) {
                result =
                        agreementMapper.insertConsent(
                                consentDTO.getUserId(),
                                item.getAgreementId(),
                                agreedYn
                        );
            } else {
                result =
                        agreementMapper.updateConsent(
                                consentDTO.getUserId(),
                                item.getAgreementId(),
                                agreedYn
                        );
            }

            if (result != 1) {
                throw new IllegalStateException(
                        "약관 동의 저장에 실패했습니다."
                );
            }
        }

        log.info(
                "회원 약관 동의 저장 완료: userId={}",
                consentDTO.getUserId()
        );
    }


    private void validateConsentItem(
            AgreementConsentDTO.ConsentItem item
    ) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "약관 동의 항목이 올바르지 않습니다."
            );
        }

        if (item.getAgreementId() == null) {
            throw new IllegalArgumentException(
                    "약관번호가 필요합니다."
            );
        }

        if (item.getAgreed() == null) {
            throw new IllegalArgumentException(
                    "약관 동의 여부가 필요합니다."
            );
        }
    }
}