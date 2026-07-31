package org.scoula.analysis.service;

import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.dto.AnalysisCategoryResultDTO;
import org.scoula.analysis.dto.AnalysisNarrativeDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
// 임시 칭호 발급 서비스
// AI 분석 요약을 만드는 서비스 -> 현재 AI연결 X 임시 텍스트만 출력
public class AnalysisNarrativeServiceImpl
        implements AnalysisNarrativeService {

    @Override
    public AnalysisNarrativeDTO createNarrative(
            Integer period,
            Integer totalSpendingAmount,
            AnalysisCategoryResultDTO representativeCategory,
            List<AnalysisCategoryResultDTO> categories
    ) {
        // 카테고리 이름
        String categoryName =
                representativeCategory.getCategoryName();

        String title =
                categoryName + "에 진심인 소비자";

        String summary =
                "최근 "
                        + period
                        + "개월 동안 "
                        + categoryName
                        + " 지출 비중이 가장 높았으며, 전체 소비의 "
                        + representativeCategory
                        .getSpendingRatio()
                        .toPlainString()
                        + "%를 차지했습니다.";

        log.info(
                "소비분석 기본 칭호 생성 category={}, ratio={}",
                categoryName,
                representativeCategory.getSpendingRatio()
        );

        return AnalysisNarrativeDTO.builder()
                .title(title)
                .summary(summary)
                .build();
    }
}