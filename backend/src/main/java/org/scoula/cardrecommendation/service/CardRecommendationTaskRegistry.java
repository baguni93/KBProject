package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.CardRecommendationTaskState;
import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

// 현재 어떤 카드의 추천작업이 진행중인지 메모리에 저장하는 곳
// 이 Registry는 Tomcat 메모리에 저장된다.
@Service
public class CardRecommendationTaskRegistry {

    // 이 해시맵은 비동기처리가 가능하다.
    // 여러 쓰레드가 동시에 이 Map을 사용한다.
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
