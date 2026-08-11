package org.scoula.cardrecommendation.service;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.cardrecommendation.domain.CardBenefitCalculationResult;
import org.scoula.cardrecommendation.domain.CardBenefitVO;
import org.scoula.cardrecommendation.domain.CardProductCalculationResult;
import org.scoula.cardrecommendation.domain.CardRecommendationCategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
 * OpenAI API를 호출해 카드 한 장의 추천 이유를 자연어로 생성한다.
 *
 * 입력:
 * - 사용자의 소비 카테고리 분석 결과
 * - 현재 설명할 카드의 예상 할인액/순위
 * - 해당 카드에서 실제 계산된 혜택 결과
 *
 * 출력:
 * - card_recommendation_tbl.ai_recommendation_summary에 저장할 카드별 AI 문구
 *
 * AI 호출이 실패해도 추천 전체가 실패하지 않도록 fallback 문구를 반환한다.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class CardRecommendationNarrativeServiceImpl
        implements CardRecommendationNarrativeService {

    private static final String MODEL_NAME = "gpt-5-nano";
    private static final int MAX_SUMMARY_LENGTH = 500;

    private final OpenAIClient openAIClient;

    /*
     * 카드 한 장 단위로 GPT를 호출한다.
     * ServiceImpl에서 신용/체크 각각 TOP 3만 이 메서드를 호출하므로 최대 6회 호출된다.
     */
    @Override
    public String createCardSummary(
            List<CardRecommendationCategoryVO> categories,
            CardProductCalculationResult cardResult,
            List<CardBenefitVO> benefits
    ) {
        String fallback = createFallbackSummary(categories, cardResult);

        if (cardResult == null
                || cardResult.getProduct() == null
                || categories == null
                || categories.isEmpty()) {
            return fallback;
        }

        try {
            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(MODEL_NAME)
                    .input(createPrompt(categories, cardResult, benefits))
                    .build();

            Response response = openAIClient.responses().create(params);
            String output = extractOutputText(response);

            if (output == null || output.isBlank()) {
                return fallback;
            }

            String summary = removeMarkdownCodeFence(output).trim();
            return limitLength(summary, MAX_SUMMARY_LENGTH);

        } catch (Exception e) {
            log.error(
                    "[CARD-RECOMMENDATION-AI] 카드별 추천 요약 생성 실패 cardProductId={}",
                    cardResult.getProduct().getCardProductId(),
                    e
            );
            return fallback;
        }
    }

    /*
     * GPT에게 전달할 프롬프트를 만든다.
     * 계산된 숫자와 혜택 정보만 제공하여 없는 혜택을 만들어내지 않도록 제한한다.
     */
    private String createPrompt(
            List<CardRecommendationCategoryVO> categories,
            CardProductCalculationResult cardResult,
            List<CardBenefitVO> benefits
    ) {
        String categoryText = categories.stream()
                .filter(Objects::nonNull)
                .limit(5)
                .map(category -> "- "
                        + safeCategoryName(category)
                        + ": "
                        + safeRatio(category)
                        + "%")
                .collect(Collectors.joining("\n"));

        String benefitText = createBenefitText(cardResult, benefits);

        return """
                당신은 KB 카드 추천 결과를 설명하는 금융 서비스입니다.

                아래 소비 비율과 카드 혜택 계산 결과는 서버에서 이미 계산이 끝난 값입니다.
                금액이나 순위를 다시 계산하지 말고 제공된 값만 사용하세요.

                [주요 소비 카테고리]
                %s

                [이번에 설명할 추천 카드]
                카드명: %s
                카드 유형: %s
                카드 설명: %s
                연간 예상 할인액: %s원
                연회비: %s원

                [실제 계산에 반영된 주요 혜택]
                %s

                [작성 규칙]
                1. 이 카드 한 장에 대한 추천 이유만 한국어로 작성하세요.
                2. 정확히 2문장으로 작성하세요.
                3. 첫 문장은 사용자의 주요 소비패턴과 이 카드가 잘 맞는 이유를 설명하세요.
                4. 둘째 문장은 실제 계산된 혜택 또는 예상 할인액을 근거로 설명하세요.
                5. 다른 카드나 다른 카드 유형을 언급하지 마세요.
                6. 순위나 금액을 다시 계산하거나 새로운 혜택을 만들어내지 마세요.
                7. 가입을 강요하거나 혜택을 확정적으로 보장하지 마세요.
                8. 사용자의 직업, 소득, 나이, 가족관계를 추측하지 마세요.
                9. 마크다운, 제목, 목록, JSON 없이 문장만 반환하세요.
                """.formatted(
                categoryText,
                safe(cardResult.getProduct().getCardName(), "KB 카드"),
                safe(cardResult.getProduct().getCardType(), "CARD"),
                safe(cardResult.getProduct().getCardDescription(), "등록된 설명 없음"),
                nullSafe(cardResult.getExpectedBenefitAmount()),
                nullSafe(cardResult.getProduct().getAnnualFee()),
                benefitText
        );
    }

    private String createBenefitText(
            CardProductCalculationResult cardResult,
            List<CardBenefitVO> benefits
    ) {
        if (cardResult == null
                || cardResult.getBenefitResults() == null
                || cardResult.getBenefitResults().isEmpty()
                || benefits == null
                || benefits.isEmpty()) {
            return "- 계산된 주요 혜택 정보 없음";
        }

        Map<Integer, CardBenefitVO> benefitMap = benefits.stream()
                .filter(Objects::nonNull)
                .filter(benefit -> benefit.getCardBenefitId() != null)
                .collect(Collectors.toMap(
                        CardBenefitVO::getCardBenefitId,
                        Function.identity(),
                        (left, right) -> left
                ));

        String text = cardResult.getBenefitResults().stream()
                .filter(Objects::nonNull)
                .filter(result -> nullSafe(result.getExpectedBenefitAmount()) > 0)
                .sorted((left, right) -> Integer.compare(
                        nullSafe(right.getExpectedBenefitAmount()),
                        nullSafe(left.getExpectedBenefitAmount())
                ))
                .limit(3)
                .map(result -> createBenefitLine(result, benefitMap))
                .filter(line -> line != null && !line.isBlank())
                .collect(Collectors.joining("\n"));

        return text.isBlank()
                ? "- 계산된 주요 혜택 정보 없음"
                : text;
    }

    private String createBenefitLine(
            CardBenefitCalculationResult result,
            Map<Integer, CardBenefitVO> benefitMap
    ) {
        CardBenefitVO benefit = benefitMap.get(result.getCardBenefitId());
        if (benefit == null) {
            return "- 혜택 ID " + result.getCardBenefitId()
                    + ": 예상 할인액 "
                    + nullSafe(result.getExpectedBenefitAmount())
                    + "원";
        }

        String description = safe(
                benefit.getBenefitDescription(),
                "상세 설명 없음"
        );

        return "- "
                + safe(benefit.getBenefitName(), "카드 혜택")
                + ": 예상 할인액 "
                + nullSafe(result.getExpectedBenefitAmount())
                + "원 / "
                + description;
    }

    /*
     * GPT 호출 실패/빈 응답 시 사용할 기본 추천문구.
     * 이미 계산된 카드 정보만 사용하므로 추천 기능 자체는 계속 동작한다.
     */
    private String createFallbackSummary(
            List<CardRecommendationCategoryVO> categories,
            CardProductCalculationResult cardResult
    ) {
        String categoryName = "주요 카테고리";
        if (categories != null && !categories.isEmpty()
                && categories.get(0) != null
                && categories.get(0).getCategoryName() != null) {
            categoryName = categories.get(0).getCategoryName();
        }

        String cardName = "현재 소비 패턴에 맞는 KB 카드";
        int expectedBenefit = 0;

        if (cardResult != null) {
            expectedBenefit = nullSafe(cardResult.getExpectedBenefitAmount());
            if (cardResult.getProduct() != null
                    && cardResult.getProduct().getCardName() != null) {
                cardName = cardResult.getProduct().getCardName();
            }
        }

        return limitLength(
                "최근 12개월 동안 "
                        + categoryName
                        + " 소비가 두드러져 "
                        + cardName
                        + "의 혜택과 잘 맞습니다. 실제 거래에 카드 혜택 조건을 적용한 결과 연간 예상 할인액은 "
                        + expectedBenefit
                        + "원으로 계산되었습니다.",
                MAX_SUMMARY_LENGTH
        );
    }

    private String safeCategoryName(
            CardRecommendationCategoryVO category
    ) {
        if (category == null
                || category.getCategoryName() == null
                || category.getCategoryName().isBlank()) {
            return "기타";
        }
        return category.getCategoryName();
    }

    private String safeRatio(
            CardRecommendationCategoryVO category
    ) {
        if (category == null || category.getSpendingRatio() == null) {
            return "0.00";
        }
        return category.getSpendingRatio().toPlainString();
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int nullSafe(Integer value) {
        return value == null ? 0 : value;
    }

    private String extractOutputText(Response response) {
        return response.output()
                .stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(outputText -> outputText.text())
                .collect(Collectors.joining())
                .trim();
    }

    private String removeMarkdownCodeFence(String value) {
        return value.trim()
                .replaceFirst("^```(?:text)?\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();
    }

    private String limitLength(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
