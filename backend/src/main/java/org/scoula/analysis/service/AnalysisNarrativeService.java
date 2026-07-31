package org.scoula.analysis.service;

import org.scoula.analysis.dto.AnalysisCategoryResultDTO;
import org.scoula.analysis.dto.AnalysisNarrativeDTO;

import java.util.List;

// 칭호 생성 서비스
public interface AnalysisNarrativeService {



    AnalysisNarrativeDTO createNarrative(
            Integer period,
            Integer totalSpendingAmount,
            AnalysisCategoryResultDTO representativeCategory,
            List<AnalysisCategoryResultDTO> categories
    );
}