package org.scoula.analysis.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.dto.AnalysisCategoryResultDTO;
import org.scoula.analysis.dto.AnalysisNarrativeDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AnalysisNarrativeServiceImpl
        implements AnalysisNarrativeService {

    private static final String MODEL_NAME = "gpt-5-nano";

    // spending_analysis_tbl.ai_title의 최대 길이
    private static final int MAX_TITLE_LENGTH = 100;

    // 화면에 지나치게 긴 분석 문장이 노출되지 않도록 제한
    private static final int MAX_SUMMARY_LENGTH = 500;

    private final OpenAIClient openAIClient;

    // AI가 예상하지 못한 JSON 필드를 추가하더라도 필요한 값만 읽는다.
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false
                    );

    @Override
    public AnalysisNarrativeDTO createNarrative(
            Integer period,
            AnalysisCategoryResultDTO representativeCategory,
            List<AnalysisCategoryResultDTO> categories
    ) {
        /*
         * 대표 카테고리나 카테고리 목록이 비정상인 경우
         * AI를 호출하지 않고 기본 문구를 반환한다.
         */
        if (representativeCategory == null
                || categories == null
                || categories.isEmpty()) {

            return createFallbackNarrative(
                    period,
                    representativeCategory
            );
        }

        try {
            String prompt =
                    createPrompt(
                            period,
                            representativeCategory,
                            categories
                    );

            ResponseCreateParams params =
                    ResponseCreateParams.builder()
                            .model(MODEL_NAME)
                            .input(prompt)
                            .build();

            // 소비 패턴을 기반으로 칭호와 분석 요약 생성
            Response response =
                    openAIClient.responses().create(params);

            String responseText =
                    extractOutputText(response);

            if (responseText == null
                    || responseText.isBlank()) {

                log.warn(
                        "[ANALYSIS-AI] 빈 응답 반환 "
                                + "period={}, representativeCategory={}",
                        period,
                        representativeCategory.getCategoryName()
                );

                return createFallbackNarrative(
                        period,
                        representativeCategory
                );
            }

            // ```json 코드 블록으로 감싸진 경우 제거
            String jsonText =
                    removeMarkdownCodeFence(responseText);

            AnalysisNarrativeDTO narrative =
                    objectMapper.readValue(
                            jsonText,
                            AnalysisNarrativeDTO.class
                    );

            if (!isValidNarrative(narrative)) {
                log.warn(
                        "[ANALYSIS-AI] 응답값 검증 실패 "
                                + "period={}, response={}",
                        period,
                        responseText
                );

                return createFallbackNarrative(
                        period,
                        representativeCategory
                );
            }

            /*
             * DB 컬럼 길이와 화면 표시 길이를 넘지 않도록
             * 서버에서 마지막으로 문자열 길이를 제한한다.
             */
            narrative.setTitle(
                    limitLength(
                            narrative.getTitle().trim(),
                            MAX_TITLE_LENGTH
                    )
            );

            narrative.setSummary(
                    limitLength(
                            narrative.getSummary().trim(),
                            MAX_SUMMARY_LENGTH
                    )
            );

            log.info(
                    "[ANALYSIS-AI] 소비분석 문구 생성 완료 "
                            + "period={}, representativeCategory={}, "
                            + "title={}",
                    period,
                    representativeCategory.getCategoryName(),
                    narrative.getTitle()
            );

            return narrative;

        } catch (Exception e) {
            /*
             * OpenAI 호출 또는 JSON 파싱이 실패해도
             * 소비분석 자체는 실패시키지 않고 기본 문구를 사용한다.
             */
            log.error(
                    "[ANALYSIS-AI] 소비분석 문구 생성 실패 "
                            + "period={}, representativeCategory={}",
                    period,
                    representativeCategory.getCategoryName(),
                    e
            );

            return createFallbackNarrative(
                    period,
                    representativeCategory
            );
        }
    }

    private String createPrompt(
            Integer period,
            AnalysisCategoryResultDTO representativeCategory,
            List<AnalysisCategoryResultDTO> categories
    ) {
        /*
         * 사용자 ID, 가맹점명, 실제 소비 금액은 전달하지 않는다.
         * 계산이 끝난 카테고리 비율과 거래 건수만 AI에 전달한다.
         */
        String categorySummary =
                categories.stream()
                        .map(category ->
                                "- "
                                        + category.getCategoryName()
                                        + ": "
                                        + category.getSpendingRatio()
                                        .toPlainString()
                                        + "%, "
                                        + category.getTransactionCount()
                                        + "건"
                        )
                        .collect(Collectors.joining("\n"));

        return """
        당신은 사용자의 소비 패턴을 재미있고 이해하기 쉽게 설명하는
        금융 소비분석 시스템입니다.

        아래 데이터는 서버에서 집계가 완료된 결과입니다.
        금액, 비율, 거래 건수를 새로 계산하지 말고 제공된 값만 사용하세요.

        [분석 데이터]

        분석 기간:
        최근 %d개월

        대표 소비 카테고리:
        %s

        카테고리별 소비 비율과 거래 건수:
        %s

        [작성 목표]

        사용자가 그래프에 표시된 수치를 다시 읽는 데 그치지 않고,
        자신의 생활 습관과 소비 성향을 발견했다고 느낄 수 있도록 작성하세요.

        [작성 규칙]

        1. title은 대표 소비 성향을 나타내는 재치 있는 한국어 칭호로 작성하세요.
        2. title은 30자 이내로 작성하세요.
        3. title은 사용자를 공격하거나 조롱하지 않는 밝은 표현으로 작성하세요.
        4. summary는 정확히 3문장으로 작성하세요.
        5. 첫 번째 문장은 대표 카테고리를 생활 습관이나 소비 성향으로 해석하세요.
        6. 두 번째 문장은 다른 주요 카테고리와 비교해 눈에 띄는 특징을 설명하세요.
        7. 세 번째 문장은 사용자가 부담 없이 확인할 수 있는 소비 습관을 제안하세요.
        8. "비중이 높습니다", "소비가 많습니다"만 반복하지 마세요.
        9. 제공된 데이터에 없는 소비 빈도, 이용 시간, 가맹점, 상품 종류를 단정하지 마세요.
        10. 실제 소비 금액을 추측하거나 만들어 내지 마세요.
        11. 직업, 나이, 소득, 가족관계, 건강 상태를 추측하지 마세요.
        12. 낭비벽, 과소비, 중독 같은 부정적인 표현을 사용하지 마세요.
        13. 투자, 대출, 보험 가입 등의 금융 조언을 추가하지 마세요.
        14. 절약을 강요하지 말고 관찰하거나 비교해 볼 수 있는 가벼운 행동만 제안하세요.
        15. 예시는 문체와 구성만 참고하고, 칭호나 문장을 그대로 복사하지 마세요.
        16. 제공된 실제 데이터에 맞는 새로운 표현을 매번 작성하세요.
        17. 마크다운이나 추가 설명 없이 JSON 객체 하나만 반환하세요.
        18. 반환되는 title에 띄어쓰기는 지켜 주세요.
        
        [좋은 작성 예시]

        예시 1 — 카페 중심 소비
        {
          "title": "카페인으로 하루를 여는 자",
          "summary": "카페 소비가 가장 두드러져 일상 속 작은 휴식을 자주 챙기는 소비 성향이 보여요. 식비와 교통보다 카페 이용이 눈에 띄어 커피 한 잔이 생활의 익숙한 선택으로 자리 잡은 모습입니다. 기간별 카페 비중을 비교해 보면 이 소비가 꾸준한 습관인지 최근에 생긴 변화인지 확인할 수 있어요."
        }

        예시 2 — 온라인쇼핑 중심 소비
        {
          "title": "장바구니를 미래의 나에게 맡기는 자",
          "summary": "온라인쇼핑이 가장 두드러져 필요한 물건을 빠르고 편리하게 해결하는 소비 성향이 보여요. 생활과 뷰티·미용보다 온라인 결제 비중이 높아 비대면 구매를 적극적으로 활용하는 모습입니다. 기간별 온라인쇼핑 비중을 비교해 보면 반복적인 구매와 특정 시기의 집중 소비를 구분해 볼 수 있어요."
        }

        예시 3 — 여행 중심 소비
        {
          "title": "여행에 미친 지갑의 순례자",
          "summary": "여행 소비가 가장 크게 나타나 물건보다 경험과 새로운 장소에 가치를 두는 성향이 보여요. 주거·통신과 교육도 함께 높은 편이지만 여행이 가장 앞서 있어 떠날 때 확실히 사용하는 패턴이 눈에 띕니다. 다른 기간의 여행 비중과 비교해 보면 여행이 일상적인 관심인지 특정 시기의 이벤트였는지 확인할 수 있어요."
        }

        예시 4 — 교육 중심 소비
        {
          "title": "미래의 나를 선결제한 자",
          "summary": "교육 소비가 가장 두드러져 새로운 지식과 성장에 꾸준히 가치를 두는 모습이 보여요. 쇼핑과 카페보다 교육 비중이 높아 현재의 즐거움뿐 아니라 앞으로의 변화에도 관심을 두는 편입니다. 분석 기간을 바꾸어 비교해 보면 학습 관련 소비가 꾸준히 이어졌는지 살펴볼 수 있어요."
        }

        예시 5 — 소비가 고르게 분산된 경우
        {
          "title": "한쪽에 치우치지 않는 소비 밸런서",
          "summary": "여러 카테고리가 비슷한 비중을 보여 일상의 다양한 영역에 소비가 고르게 분산된 모습입니다. 대표 카테고리가 가장 앞서 있지만 다른 주요 항목과의 차이가 크지 않아 특정 분야에 집중되지 않은 패턴이 눈에 띕니다. 기간별 순위 변화를 살펴보면 평소에도 균형이 유지되는지 확인할 수 있어요."
        }

        [반환 형식]

        {
          "title": "대표 소비 성향을 표현한 새로운 칭호",
          "summary": "소비 성향 해석 문장. 주요 카테고리 비교 문장. 부담 없는 소비 습관 확인 제안 문장."
        }
        """.formatted(
                period,
                representativeCategory.getCategoryName(),
                categorySummary
        );
    }

    private String extractOutputText(Response response) {
        /*
         * Response
         * → output
         * → message
         * → content
         * → outputText
         * 순서로 답변 문자열을 추출한다.
         */
        return response.output()
                .stream()
                .flatMap(item ->
                        item.message().stream()
                )
                .flatMap(message ->
                        message.content().stream()
                )
                .flatMap(content ->
                        content.outputText().stream()
                )
                .map(outputText ->
                        outputText.text()
                )
                .collect(Collectors.joining())
                .trim();
    }

    private String removeMarkdownCodeFence(
            String responseText
    ) {
        return responseText
                .trim()
                .replaceFirst(
                        "^```(?:json)?\\s*",
                        ""
                )
                .replaceFirst(
                        "\\s*```$",
                        ""
                )
                .trim();
    }

    private boolean isValidNarrative(
            AnalysisNarrativeDTO narrative
    ) {
        return narrative != null
                && narrative.getTitle() != null
                && !narrative.getTitle().isBlank()
                && narrative.getSummary() != null
                && !narrative.getSummary().isBlank();
    }

    private String limitLength(
            String value,
            int maximumLength
    ) {
        if (value.length() <= maximumLength) {
            return value;
        }

        return value.substring(
                0,
                maximumLength
        );
    }

    private AnalysisNarrativeDTO createFallbackNarrative(
            Integer period,
            AnalysisCategoryResultDTO representativeCategory
    ) {
        String categoryName =
                representativeCategory == null
                        || representativeCategory
                        .getCategoryName() == null
                        ? "소비"
                        : representativeCategory
                        .getCategoryName();

        String spendingRatio =
                representativeCategory == null
                        || representativeCategory
                        .getSpendingRatio() == null
                        ? "0.00"
                        : representativeCategory
                        .getSpendingRatio()
                        .toPlainString();

        String title =
                categoryName + "에 진심인 소비자";

        String summary =
                "최근 "
                        + period
                        + "개월 동안 "
                        + categoryName
                        + " 소비 비중이 가장 높았으며, "
                        + "전체 소비의 "
                        + spendingRatio
                        + "%를 차지했습니다.";

        log.info(
                "[ANALYSIS-AI] 기본 소비분석 문구 사용 "
                        + "period={}, category={}, ratio={}",
                period,
                categoryName,
                spendingRatio
        );

        return AnalysisNarrativeDTO.builder()
                .title(
                        limitLength(
                                title,
                                MAX_TITLE_LENGTH
                        )
                )
                .summary(
                        limitLength(
                                summary,
                                MAX_SUMMARY_LENGTH
                        )
                )
                .build();
    }
}