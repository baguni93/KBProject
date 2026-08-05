package org.scoula.analysis.service;

import org.scoula.analysis.domain.SpendingAnalysisCategoryVO;
import org.scoula.analysis.domain.SpendingAnalysisVO;

import java.util.List;

public interface AnalysisSaveService {

    Integer saveAnalysis(
            SpendingAnalysisVO spendingAnalysis,
            List<SpendingAnalysisCategoryVO> categories
    );
}