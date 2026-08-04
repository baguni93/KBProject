package org.scoula.agreement.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.agreement.domain.AgreementVO;

import java.util.List;

public interface AgreementMapper {

    List<AgreementVO> findAllActive();

    AgreementVO findByAgreementType(
            @Param("agreementType") String agreementType
    );

    AgreementVO findById(
            @Param("agreementId") Long agreementId
    );

    int countUserAgreement(
            @Param("userId") Long userId,
            @Param("agreementId") Long agreementId
    );

    int insertConsent(
            @Param("userId") Long userId,
            @Param("agreementId") Long agreementId,
            @Param("agreedYn") String agreedYn
    );

    int updateConsent(
            @Param("userId") Long userId,
            @Param("agreementId") Long agreementId,
            @Param("agreedYn") String agreedYn
    );
}