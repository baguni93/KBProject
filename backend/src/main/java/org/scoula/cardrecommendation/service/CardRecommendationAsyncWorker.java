package org.scoula.cardrecommendation.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scoula.cardrecommendation.dto.CardRecommendationCreateResponseDTO;
import org.scoula.exception.CustomException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

// 카드 추천계산 메서드를 별도의 쓰레드에서 실행하고, 결과에 따라 작업상태를 완료, 실패로 변경(put)한다.
@Service
public class CardRecommendationAsyncWorker {

    // 비동기 작업은 HTTP요청이 끝난뒤에도 실행되기에, 오류를 처리하는게 어려워서 로그를 출력한다.
    private static final Logger log =
            LogManager.getLogger(CardRecommendationAsyncWorker.class);

    // 실제 추천 계산과 DB 저장 역할의 service
    private final CardRecommendationService cardRecommendationService;

    // 계산 결과에 따라 상태를 변경해야한다.
    private final CardRecommendationTaskRegistry taskRegistry;

    // 생성자 주입 -> spring이 worker를 생성할 때 필요한 두가지 필수 매개변수
    //
    public CardRecommendationAsyncWorker(
            CardRecommendationService cardRecommendationService,
            CardRecommendationTaskRegistry taskRegistry
    ) {
        this.cardRecommendationService = cardRecommendationService;
        this.taskRegistry = taskRegistry;
    }

    // 비동기 실행을 하고 결과를 Registry(톰켓 메모리)에 저장하는 방식
    @Async
    public void execute(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        try {
            // 실제 추천 계산 호출 -> 이게 성공하면 추천계산과 저장이 완료된것임
            CardRecommendationCreateResponseDTO result =
                    cardRecommendationService.createOrReuse(
                            userId,
                            spendingAnalysisId
                    );
            // 정상 완료 처리 -> 결과가 담긴 result를 complate를 호출해 마무리한다.
            taskRegistry.complete(
                    userId,
                    spendingAnalysisId,
                    result.getRecommendationCount(),
                    result.isCreated(),
                    result.getMessage()
            );
        } catch (CustomException e) {
            log.warn(
                    "비동기 카드 추천 실패 userId={}, analysisId={}, code={}",
                    userId,
                    spendingAnalysisId,
                    e.getErrorCode().getCode(),
                    e
            );
            taskRegistry.fail(
                    userId,
                    spendingAnalysisId,
                    e.getMessage()
            );
        } catch (Exception e) {
            log.error(
                    "비동기 카드 추천 처리 중 예외 userId={}, analysisId={}",
                    userId,
                    spendingAnalysisId,
                    e
            );
            taskRegistry.fail(
                    userId,
                    spendingAnalysisId,
                    "카드 추천 처리 중 오류가 발생했습니다."
            );
        }
    }
}
