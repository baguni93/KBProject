package org.scoula.analysis.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scoula.analysis.dto.AnalysisExecutionResponseDTO;
import org.scoula.exception.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AnalysisAsyncWorker {

    private static final Logger log =
            LogManager.getLogger(AnalysisAsyncWorker.class);

    private final AnalysisService analysisService;
    private final AnalysisTaskRegistry taskRegistry;

    public AnalysisAsyncWorker(
            AnalysisService analysisService,
            AnalysisTaskRegistry taskRegistry
    ) {
        this.analysisService = analysisService;
        this.taskRegistry = taskRegistry;
    }

    @Async
    public void execute(Integer userId, Integer period) {
        try {
            AnalysisExecutionResponseDTO result =
                    analysisService.executeAnalysis(userId, period);

            taskRegistry.complete(
                    userId,
                    period,
                    result.getSpendingAnalysisId()
            );
        } catch (CustomException e) {
            log.warn(
                    "비동기 소비분석 실패 userId={}, period={}, code={}",
                    userId,
                    period,
                    e.getErrorCode().getCode(),
                    e
            );
            taskRegistry.fail(userId, period, e.getMessage());
        } catch (Exception e) {
            log.error(
                    "비동기 소비분석 처리 중 예외 userId={}, period={}",
                    userId,
                    period,
                    e
            );
            taskRegistry.fail(
                    userId,
                    period,
                    "소비 분석 처리 중 오류가 발생했습니다."
            );
        }
    }
}
