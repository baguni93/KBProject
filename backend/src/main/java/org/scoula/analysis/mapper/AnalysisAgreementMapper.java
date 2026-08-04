package org.scoula.analysis.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.analysis.domain.AnalysisAgreementVO;

import java.util.List;

public interface AnalysisAgreementMapper {

    List<AnalysisAgreementVO> selectAnalysisAgreements(
            @Param("userId") Integer userId
    );

    Integer selectLatestUserAgreementId(
            @Param("userId") Integer userId,
            @Param("agreementId") Integer agreementId
    );

    int insertUserAgreement(
            @Param("userId") Integer userId,
            @Param("agreementId") Integer agreementId,
            @Param("agreedYn") String agreedYn
    );

    int updateUserAgreement(
            @Param("userAgreementId") Integer userAgreementId,
            @Param("agreedYn") String agreedYn
    );
}
