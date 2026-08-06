package org.scoula.analysis.service;

import org.scoula.analysis.domain.AnalysisTaskState;
import org.scoula.analysis.dto.AnalysisTaskStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AnalysisTaskRegistry {

    private final ConcurrentMap<String, AnalysisTaskState> taskStates =
            new ConcurrentHashMap<>();

    public AnalysisTaskState get(Integer userId, Integer period) {
        return taskStates.get(createKey(userId, period));
    }

    public boolean begin(Integer userId, Integer period) {
        AtomicBoolean started = new AtomicBoolean(false);
        String key = createKey(userId, period);

        taskStates.compute(key, (ignored, current) -> {
            if (current != null
                    && current.getStatus() == AnalysisTaskStatus.PROCESSING) {
                return current;
            }

            started.set(true);
            return new AnalysisTaskState(
                    AnalysisTaskStatus.PROCESSING,
                    null,
                    "소비 분석이 진행 중입니다."
            );
        });

        return started.get();
    }

    public void complete(
            Integer userId,
            Integer period,
            Integer spendingAnalysisId
    ) {
        taskStates.put(
                createKey(userId, period),
                new AnalysisTaskState(
                        AnalysisTaskStatus.COMPLETED,
                        spendingAnalysisId,
                        "소비 분석이 완료되었습니다."
                )
        );
    }

    public void fail(
            Integer userId,
            Integer period,
            String message
    ) {
        taskStates.put(
                createKey(userId, period),
                new AnalysisTaskState(
                        AnalysisTaskStatus.FAILED,
                        null,
                        message == null || message.isBlank()
                                ? "소비 분석 처리 중 오류가 발생했습니다."
                                : message
                )
        );
    }

    private String createKey(Integer userId, Integer period) {
        return userId + ":" + period;
    }
}
