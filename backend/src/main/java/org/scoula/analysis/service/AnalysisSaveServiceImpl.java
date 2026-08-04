package org.scoula.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.domain.SpendingAnalysisCategoryVO;
import org.scoula.analysis.domain.SpendingAnalysisVO;
import org.scoula.analysis.mapper.AnalysisMapper;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class AnalysisSaveServiceImpl implements AnalysisSaveService {

    //분석 결과 매퍼
    private final AnalysisMapper analysisMapper;

    @Override
    @Transactional
    public Integer saveAnalysis(
            SpendingAnalysisVO spendingAnalysis, // 대표 분석 결과
            List<SpendingAnalysisCategoryVO> categories // 분석 카테고리
    ) {
        // 분석 결과 있으면 1
        int analysisInsertCount =
                analysisMapper.insertSpendingAnalysis(
                        spendingAnalysis
                );

        // 분석결과 없으면 예외처리(처리가 안되었거나, auto increment로 생성된 PK가 없는 경우 )
        if (analysisInsertCount != 1
                || spendingAnalysis.getSpendingAnalysisId() == null) {

            throw new CustomException(
                    ErrorCode.ANALYSIS_RESULT_SAVE_FAILED
            );
        }

        // 분석 본문의 PK값을 가져옴
        Integer spendingAnalysisId =
                spendingAnalysis.getSpendingAnalysisId();

        // 이를 카테고리별 분석결과의 FK로 쓰기 위해서,모든 VO에 PK값을 FK로 넣음
        categories.forEach(category ->
                category.setSpendingAnalysisId(
                        spendingAnalysisId
                )
        );

        int categoryInsertCount =
                analysisMapper.insertSpendingAnalysisCategories(
                        categories
                );

        if (categoryInsertCount != categories.size()) {
            throw new CustomException(
                    ErrorCode.ANALYSIS_RESULT_SAVE_FAILED
            );
        }

        log.info(
                "소비분석 저장 완료 spendingAnalysisId={}, categoryCount={}",
                spendingAnalysisId,
                categories.size()
        );

        return spendingAnalysisId;
    }
}