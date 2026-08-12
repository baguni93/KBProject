package org.scoula.insurancerecommendation.service;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.insurancerecommendation.domain.InsuranceRecommendationCandidateVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class InsuranceRecommendationNarrativeServiceImpl
        implements InsuranceRecommendationNarrativeService {

    private static final String MODEL_NAME = "gpt-5-nano";
    private static final int MAX_SUMMARY_LENGTH = 500;

    private final OpenAIClient openAIClient;

    /*
     * 보험추천 한 번에 AI 호출도 한 번만 수행한다.
     * 각 상품 카드의 "추천 이유"는 이 Service를 사용하지 않고
     * 실제 결제내역으로 규칙 기반 생성한다.
     */
    @Override
    public String createInsuranceSummary(
            List<InsuranceRecommendationCandidateVO> candidates
    ) {
        String fallback = createFallbackSummary(candidates);

        if (candidates == null || candidates.isEmpty()) {
            return fallback;
        }

        try {
            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(MODEL_NAME)
                    .input(createPrompt(candidates))
                    .build();

            Response response = openAIClient.responses().create(params);
            String output = extractOutputText(response);

            if (output == null || output.isBlank()) {
                return fallback;
            }

            return limitLength(
                    removeMarkdownCodeFence(output).trim(),
                    MAX_SUMMARY_LENGTH
            );

        } catch (Exception e) {
            /*
             * AI 요약 실패가 보험추천 자체 실패로 이어지지 않게
             * 확인된 거래/추천 결과만으로 만든 fallback을 사용한다.
             */
            log.error(
                    "[INSURANCE-RECOMMENDATION-AI] 전체 추천 요약 생성 실패 recommendationCount={}",
                    candidates.size(),
                    e
            );
            return fallback;
        }
    }

    private String createPrompt(
            List<InsuranceRecommendationCandidateVO> candidates
    ) {
        String categorySummary = candidates.stream()
                .map(InsuranceRecommendationCandidateVO::getCategoryName)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .map(value -> "- " + value)
                .collect(Collectors.joining("\n"));

        String insuranceCategorySummary = candidates.stream()
                .map(InsuranceRecommendationCandidateVO::getInsuranceCategory)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .map(value -> "- " + value)
                .collect(Collectors.joining("\n"));

        if (categorySummary.isBlank()) {
            categorySummary = "- 관련 소비";
        }

        if (insuranceCategorySummary.isBlank()) {
            insuranceCategorySummary = "- 관련 보험";
        }

        return """
                당신은 KB 국민지갑의 보험 추천 결과를 설명하는 금융 서비스 AI입니다.

                아래 정보는 사용자의 실제 소비내역을 기반으로
                서버의 규칙 기반 로직이 이미 분석한 결과입니다.

                보험상품의 추천 여부는 이미 서버에서 결정되었으며,
                당신은 추천 결과를 새로 판단하거나 변경하지 않습니다.

                [분석 정보]
                분석 기간: 최근 12개월

                [보험 추천과 연결된 소비 카테고리]
                %s

                [추천된 보험 카테고리]
                %s

                위 정보를 바탕으로 사용자가 보험 추천 결과를 쉽게 이해할 수 있도록
                자연스러운 한국어 요약을 작성하세요.

                반드시 다음 규칙을 지키세요.

                1. 2~3문장으로 작성하세요.
                2. 실제로 제공된 소비 카테고리만 언급하세요.
                3. 사용자의 질병, 건강 상태, 치료 필요성, 사고 가능성,
                   생활 습관 또는 위험도를 추측하지 마세요.
                4. 보험 가입이 필요하다거나 특정 보험에 가입해야 한다고
                   단정하지 마세요.
                5. 소비내역과 관련된 보장 영역을
                   "함께 살펴볼 수 있다",
                   "참고할 수 있다",
                   "관련 상품을 확인해볼 수 있다"
                   정도로 부드럽게 표현하세요.
                6. 보험료, 예상 보험료, 가격은 언급하지 마세요.
                7. 보험상품의 우열이나 최적 상품이라고 평가하지 마세요.
                8. 서버가 제공하지 않은 사실을 새로 만들어내지 마세요.
                9. "AI가 추천했습니다", "알고리즘이 판단했습니다"처럼
                   내부 구현 방식을 사용자에게 설명하지 마세요.
                10. 금융 앱에서 사용자에게 설명하는
                    친근하고 차분한 문체를 사용하세요.

                좋은 표현 예시:
                - "최근 12개월 동안 병원과 여행 관련 소비가 확인되었어요."
                - "이러한 소비 영역과 연결되는 건강·실비와 여행자 보장을
                   함께 살펴볼 수 있도록 관련 상품을 안내해 드려요."

                피해야 할 표현 예시:
                - "병원을 자주 이용하므로 건강보험이 필요합니다."
                - "사고 위험이 높아 운전자보험 가입을 추천합니다."
                - "반려동물 질병에 대비해야 합니다."
                - "이 보험이 가장 적합합니다."

                최종 출력에는 요약문만 작성하세요.
                """.formatted(
                categorySummary,
                insuranceCategorySummary
        );
    }

    private String createFallbackSummary(
            List<InsuranceRecommendationCandidateVO> candidates
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return "최근 12개월 소비분석에서 확인된 결제내역을 바탕으로 관련 보험을 살펴볼 수 있도록 안내해 드려요. 상품별 보장 내용과 가입 조건은 상세 화면에서 확인해 주세요.";
        }

        String categoryText = candidates.stream()
                .map(InsuranceRecommendationCandidateVO::getCategoryName)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .limit(3)
                .collect(Collectors.joining(", "));

        if (categoryText.isBlank()) {
            categoryText = "관련 소비";
        }

        return limitLength(
                "최근 12개월 결제내역에서 "
                        + categoryText
                        + " 관련 소비가 확인되어, 관련 보장을 함께 살펴보실 수 있도록 보험 상품을 안내해 드려요. "
                        + "총 "
                        + candidates.size()
                        + "개 상품의 보장 내용과 가입 조건을 상세 화면에서 비교해 보세요.",
                MAX_SUMMARY_LENGTH
        );
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
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
