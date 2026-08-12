package org.scoula.cardrecommendation.service;

import org.scoula.cardrecommendation.domain.CardRecommendationTaskState;
import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatus;
import org.scoula.cardrecommendation.dto.CardRecommendationTaskStatusDTO;
import org.scoula.cardrecommendation.mapper.CardRecommendationMapper;
import org.springframework.stereotype.Service;

/*
 * 비동기 카드추천의 진입점.
 *
 * 역할:
 * - 12개월 분석인지 먼저 검증
 * - 같은 추천 작업의 중복 실행 방지
 * - 이미 DB에 저장된 추천이 있으면 기존 결과 재사용
 * - 새 작업이면 TaskRegistry를 PROCESSING으로 바꾸고 AsyncWorker 실행
 */
@Service
public class CardRecommendationAsyncServiceImpl
        implements CardRecommendationAsyncService {

    private final CardRecommendationService cardRecommendationService;
    private final CardRecommendationMapper cardRecommendationMapper;
    private final CardRecommendationTaskRegistry taskRegistry;
    private final CardRecommendationAsyncWorker asyncWorker;

    public CardRecommendationAsyncServiceImpl(
            CardRecommendationService cardRecommendationService,
            CardRecommendationMapper cardRecommendationMapper,
            CardRecommendationTaskRegistry taskRegistry,
            CardRecommendationAsyncWorker asyncWorker
    ) {
        this.cardRecommendationService = cardRecommendationService;
        this.cardRecommendationMapper = cardRecommendationMapper;
        this.taskRegistry = taskRegistry;
        this.asyncWorker = asyncWorker;
    }

    /*
     * 추천 작업 시작 API에서 호출된다.
     * HTTP 요청은 빠르게 반환하고 무거운 카드 계산/GPT 호출은 Worker에 넘긴다.
     */
    @Override
    public CardRecommendationTaskStatusDTO start(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        cardRecommendationService.validateRecommendationAnalysis(
                userId,
                spendingAnalysisId
        );

        // 같은 사용자 + 같은 소비분석의 작업이 이미 PROCESSING이면 중복 실행하지 않는다.
        CardRecommendationTaskState current =
                taskRegistry.get(userId, spendingAnalysisId);

        if (current != null
                && current.getStatus()
                == CardRecommendationTaskStatus.PROCESSING) {
            return toDTO(spendingAnalysisId, current);
        }

        // 서버 재시작으로 메모리 상태가 사라졌어도 DB에 추천 결과가 있으면 완료 상태로 복원한다.
        int existingCount = cardRecommendationMapper
                .countRecommendations(spendingAnalysisId);

        if (existingCount > 0) {
            taskRegistry.complete(
                    userId,
                    spendingAnalysisId,
                    existingCount,
                    false,
                    "기존 카드 추천 결과를 불러왔습니다."
            );
            return toDTO(
                    spendingAnalysisId,
                    taskRegistry.get(userId, spendingAnalysisId)
            );
        }

        // 실제 신규 작업이면 Registry를 PROCESSING 상태로 선점한다.
        boolean started = taskRegistry.begin(userId, spendingAnalysisId);
        CardRecommendationTaskState processing =
                taskRegistry.get(userId, spendingAnalysisId);

        if (started) {
            try {
                asyncWorker.execute(userId, spendingAnalysisId);
            } catch (RuntimeException e) {
                taskRegistry.fail(
                        userId,
                        spendingAnalysisId,
                        "카드 추천 작업을 시작하지 못했습니다."
                );
                throw e;
            }
        }

        return toDTO(spendingAnalysisId, processing);
    }

    /*
     * 폴링용 상태 조회.
     * 메모리에 상태가 없을 경우 DB의 기존 추천 결과를 확인해 COMPLETED 상태를 복원한다.
     */
    @Override
    public CardRecommendationTaskStatusDTO getStatus(
            Integer userId,
            Integer spendingAnalysisId
    ) {
        cardRecommendationService.validateRecommendationAnalysis(
                userId,
                spendingAnalysisId
        );

        CardRecommendationTaskState state =
                taskRegistry.get(userId, spendingAnalysisId);

        if (state != null) {
            return toDTO(spendingAnalysisId, state);
        }

        int existingCount = cardRecommendationMapper
                .countRecommendations(spendingAnalysisId);

        if (existingCount > 0) {
            taskRegistry.complete(
                    userId,
                    spendingAnalysisId,
                    existingCount,
                    false,
                    "저장된 카드 추천 결과가 있습니다."
            );
            return toDTO(
                    spendingAnalysisId,
                    taskRegistry.get(userId, spendingAnalysisId)
            );
        }

        return new CardRecommendationTaskStatusDTO(
                spendingAnalysisId,
                CardRecommendationTaskStatus.IDLE.name(),
                0,
                null,
                "진행 중인 카드 추천 작업이 없습니다."
        );
    }

    private CardRecommendationTaskStatusDTO toDTO(
            Integer spendingAnalysisId,
            CardRecommendationTaskState state
    ) {
        return new CardRecommendationTaskStatusDTO(
                spendingAnalysisId,
                state.getStatus().name(),
                state.getRecommendationCount(),
                state.getCreated(),
                state.getMessage()
        );
    }
}
