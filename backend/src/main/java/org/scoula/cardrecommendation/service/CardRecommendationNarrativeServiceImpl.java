package org.scoula.cardrecommendation.service;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.cardrecommendation.domain.CardProductCalculationResult;
import org.scoula.cardrecommendation.domain.CardRecommendationCategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CardRecommendationNarrativeServiceImpl
        implements CardRecommendationNarrativeService {

    private static final String MODEL_NAME = "gpt-5-nano";
    private static final int MAX_SUMMARY_LENGTH = 500;

    private final OpenAIClient openAIClient;

    @Override
    public String createSummary(
            List<CardRecommendationCategoryVO> categories,
            CardProductCalculationResult topCreditCard,
            CardProductCalculationResult topCheckCard
    ) {
        String fallback = createFallbackSummary(
                categories,
                topCreditCard,
                topCheckCard
        );

        if ((topCreditCard == null && topCheckCard == null)
                || categories == null
                || categories.isEmpty()) {
            return fallback;
        }

        try {
            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(MODEL_NAME)
                    .input(createPrompt(
                            categories,
                            topCreditCard,
                            topCheckCard
                    ))
                    .build();

            Response response = openAIClient.responses().create(params);
            String output = extractOutputText(response);

            if (output == null || output.isBlank()) {
                return fallback;
            }

            String summary = removeMarkdownCodeFence(output).trim();
            return limitLength(summary, MAX_SUMMARY_LENGTH);

        } catch (Exception e) {
            log.error("[CARD-RECOMMENDATION-AI] 추천 요약 생성 실패", e);
            return fallback;
        }
    }

    private String createPrompt(
            List<CardRecommendationCategoryVO> categories,
            CardProductCalculationResult topCreditCard,
            CardProductCalculationResult topCheckCard
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

        String creditText = createCardText(topCreditCard);
        String checkText = createCardText(topCheckCard);

        return """
                당신은 KB 카드 추천 결과를 설명하는 금융 서비스입니다.

                아래 소비 비율과 카드 추천 결과는 서버에서 계산이 끝난 값입니다.
                금액이나 순위를 다시 계산하지 말고 제공된 값만 사용하세요.

                [주요 소비 카테고리]
                %s

                [신용카드 1위]
                %s

                [체크카드 1위]
                %s

                [작성 규칙]
                1. 한국어로 자연스럽게 작성하세요.
                2. 정확히 2문장으로 작성하세요.
                3. 첫 문장은 주요 소비 성향을 설명하세요.
                4. 둘째 문장은 추천 카드가 왜 어울리는지 설명하세요.
                5. 가입을 강요하거나 혜택을 확정적으로 보장하지 마세요.
                6. 사용자의 직업, 소득, 나이, 가족관계를 추측하지 마세요.
                7. 마크다운, 제목, 목록, JSON 없이 문장만 반환하세요.
                """.formatted(
                categoryText,
                creditText,
                checkText
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

    private String createCardText(CardProductCalculationResult result) {
        if (result == null || result.getProduct() == null) {
            return "해당 유형의 추천 카드 없음";
        }

        return result.getProduct().getCardName()
                + " / 연간 예상 할인액 "
                + result.getExpectedBenefitAmount()
                + "원";
    }

    private String createFallbackSummary(
            List<CardRecommendationCategoryVO> categories,
            CardProductCalculationResult topCreditCard,
            CardProductCalculationResult topCheckCard
    ) {
        String categoryName = "주요 카테고리";
        if (categories != null && !categories.isEmpty()
                && categories.get(0) != null
                && categories.get(0).getCategoryName() != null) {
            categoryName = categories.get(0).getCategoryName();
        }

        String cardName;
        if (topCreditCard != null && topCreditCard.getProduct() != null) {
            cardName = topCreditCard.getProduct().getCardName();
        } else if (topCheckCard != null && topCheckCard.getProduct() != null) {
            cardName = topCheckCard.getProduct().getCardName();
        } else {
            cardName = "현재 소비 패턴에 맞는 KB 카드";
        }

        return limitLength(
                "최근 12개월 동안 "
                        + categoryName
                        + " 소비가 두드러졌습니다. 실제 거래에 카드 혜택 조건을 적용한 결과 "
                        + cardName
                        + "의 예상 할인 혜택이 상대적으로 크게 나타났습니다.",
                MAX_SUMMARY_LENGTH
        );
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
