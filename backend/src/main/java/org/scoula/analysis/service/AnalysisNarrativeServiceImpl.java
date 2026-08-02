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
                당신은 사용자의 소비 패턴을 재미있고 이해하기 쉽게
                설명하는 금융 소비분석 시스템입니다.

                아래 데이터는 서버에서 계산이 완료된 결과입니다.
                금액이나 비율을 새로 계산하지 말고 제공된 값만 사용하세요.

                분석 기간:
                최근 %d개월

                대표 소비 카테고리:
                %s

                카테고리별 소비 비율과 거래 건수:
                %s

                작성 규칙:
                1. title은 사용자의 대표 소비 성향을 나타내는
                   재미있는 한국어 칭호로 작성하세요.
                2. title은 30자 이내로 작성하세요.
                3. summary는 소비 비율을 중심으로 2문장 이내로 작성하세요.
                4. 사용자를 비난하거나 과도하게 부정적으로 표현하지 마세요.
                5. 실제 소비 금액을 추측하거나 만들어 내지 마세요.
                6. 투자, 대출, 보험 등의 금융 조언을 추가하지 마세요.
                7. 설명이나 마크다운 없이 JSON 객체 하나만 반환하세요.
                8. JSON 필드명은 아래 형식을 정확히 지키세요.

                반환 형식:
                {
                  "title": "망설임 없이 카페로 향하는 자",
                  "summary": "최근 소비에서 카페 비중이 가장 높았습니다. 다른 카테고리와 비교해 카페 이용이 두드러지는 소비 패턴을 보였습니다."
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