package org.scoula.analysis.service;

import org.scoula.analysis.domain.AnalysisTaskState;
import org.scoula.analysis.dto.AnalysisAvailabilityDTO;
import org.scoula.analysis.dto.AnalysisTaskStatus;
import org.scoula.analysis.dto.AnalysisTaskStatusDTO;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class AnalysisAsyncServiceImpl implements AnalysisAsyncService {

    private final AnalysisService analysisService;
    private final AnalysisTaskRegistry taskRegistry;
    private final AnalysisAsyncWorker asyncWorker;

    public AnalysisAsyncServiceImpl(
            AnalysisService analysisService,
            AnalysisTaskRegistry taskRegistry,
            AnalysisAsyncWorker asyncWorker
    ) {
        this.analysisService = analysisService;
        this.taskRegistry = taskRegistry;
        this.asyncWorker = asyncWorker;
    }

    @Override
    public AnalysisTaskStatusDTO startAnalysis(
            Integer userId,
            Integer period
    ) {
        AnalysisTaskState current = taskRegistry.get(userId, period);
        if (current != null
                && current.getStatus() == AnalysisTaskStatus.PROCESSING) {
            return toDTO(period, current);
        }

        AnalysisAvailabilityDTO availability =
                analysisService.getAnalysisAvailability(userId, period);

        if (!Boolean.TRUE.equals(availability.getAvailable())) {
            throw new CustomException(
                    ErrorCode.ANALYSIS_TRANSACTION_COUNT_NOT_ENOUGH
            );
        }

        boolean started = taskRegistry.begin(userId, period);
        AnalysisTaskState processing = taskRegistry.get(userId, period);

        if (started) {
            try {
                asyncWorker.execute(userId, period);
            } catch (RuntimeException e) {
                taskRegistry.fail(
                        userId,
                        period,
                        "소비 분석 작업을 시작하지 못했습니다."
                );
                throw e;
            }
        }

        return toDTO(period, processing);
    }

    @Override
    public AnalysisTaskStatusDTO getStatus(
            Integer userId,
            Integer period
    ) {
        validatePeriod(period);

        AnalysisTaskState state = taskRegistry.get(userId, period);
        if (state == null) {
            return new AnalysisTaskStatusDTO(
                    period,
                    AnalysisTaskStatus.IDLE.name(),
                    null,
                    "진행 중인 소비 분석이 없습니다."
            );
        }

        return toDTO(period, state);
    }

    private void validatePeriod(Integer period) {
        if (period == null
                || (period != 1 && period != 3 && period != 12)) {
            throw new CustomException(ErrorCode.INVALID_ANALYSIS_PERIOD);
        }
    }

    private AnalysisTaskStatusDTO toDTO(
            Integer period,
            AnalysisTaskState state
    ) {
        return new AnalysisTaskStatusDTO(
                period,
                state.getStatus().name(),
                state.getSpendingAnalysisId(),
                state.getMessage()
        );
    }
}
