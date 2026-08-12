package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.CardRecommendationTaskState;
import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * 카드 추천 비동기 작업 상태를 Tomcat 메모리에 보관하는 Registry.
 *
 * Key   : userId + spendingAnalysisId
 * Value : PROCESSING / COMPLETED / FAILED 상태와 결과 개수, 메시지
 *
 * DB가 아니라 메모리에 저장되므로 서버를 재시작하면 상태는 사라진다.
 * 다만 AsyncService가 DB의 기존 추천 결과를 다시 확인해 COMPLETED 상태를 복원한다.
 */
@Service
public class CardRecommendationTaskRegistry {

    // 비동기 Worker와 상태 조회 요청이 동시에 접근하므로 thread-safe한 ConcurrentHashMap을 사용한다.
    private final ConcurrentMap<String, CardRecommendationTaskState> taskStates =
            new ConcurrentHashMap<>();
    // 현재의 작업 상태를 가져오는 메서드
    //registry.get(1.7)을 호출하면, 사용자 1번이 소비분석 7번으로 카드추천 실행
    public CardRecommendationTaskState get(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        return taskStates.get(createKey(userId, spendingAnalysisId));
    }
    // 작업 시작을 등록하는 메서드.
    // 상태를 실행중으로 바꾸고, 같은작업이 중복실행되는 것을 막는다.
    public boolean begin(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        AtomicBoolean started = new AtomicBoolean(false);
        String key = createKey(userId, spendingAnalysisId);

        // 해당 키의 현재값을 확인하면서, 새값(카드추천 번호)을 결정한다.
        taskStates.compute(key, (ignored, current) -> {
            if (current != null
                    && current.getStatus()
                    == CardRecommendationTaskStatus.PROCESSING) {
                return current;
            }

            started.set(true);
            return new CardRecommendationTaskState(
                    CardRecommendationTaskStatus.PROCESSING,
                    null,
                    null,
                    "카드 혜택을 계산하고 있습니다."
            );
        });

        return started.get();
    }

    // 비동기 작업이 완료되면 호출된다.
    public void complete(
            Integer userId,
            Integer spendingAnalysisId,
            Integer recommendationCount,
            Boolean created,
            String message
    ) {
        taskStates.put(
                createKey(userId, spendingAnalysisId),
                new CardRecommendationTaskState(
                        CardRecommendationTaskStatus.COMPLETED,
                        recommendationCount,
                        created,
                        message == null || message.isBlank()
                                ? "카드 추천이 완료되었습니다."
                                : message
                )
        );
    }

    //비동기 작업이 실패하면 호출된다.
    public void fail(
            Integer userId,
            Integer spendingAnalysisId,
            String message
    ) {
        taskStates.put(
                createKey(userId, spendingAnalysisId),
                new CardRecommendationTaskState(
                        CardRecommendationTaskStatus.FAILED,
                        null,
                        null,
                        message == null || message.isBlank()
                                ? "카드 추천 처리 중 오류가 발생했습니다."
                                : message
                )
        );
    }

    //사용자(키)와 소비분석(값)의 조합을 만든다.
    private String createKey(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        return userId + ":" + spendingAnalysisId;
    }
}
