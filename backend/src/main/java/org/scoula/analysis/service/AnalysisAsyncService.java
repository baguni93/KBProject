package org.scoula.analysis.service;

import org.scoula.analysis.dto.AnalysisTaskStatusDTO;

public interface AnalysisAsyncService {

    AnalysisTaskStatusDTO startAnalysis(
            Integer userId,
            Integer period
    );

    AnalysisTaskStatusDTO getStatus(
            Integer userId,
            Integer period
    );
}
