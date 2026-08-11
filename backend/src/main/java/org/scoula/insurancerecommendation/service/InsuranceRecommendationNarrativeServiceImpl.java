package org.scoula.insurancerecommendation.service;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.insurancerecommendation.domain.InsuranceRecommendationCandidateVO;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class InsuranceRecommendationNarrativeServiceImpl
        implements InsuranceRecommendationNarrativeService {

    private static final String MODEL_NAME = "gpt-5-nano";
    private static final int MAX_SUMMARY_LENGTH = 500;

    private final OpenAIClient openAIClient;

    @Override
    public String createInsuranceSummary(
            InsuranceRecommendationCandidateVO candidate
    ) {
        String fallback = createFallbackSummary(candidate);

        if (candidate == null
                || candidate.getInsuranceProductId() == null) {
            return fallback;
        }

        try {
            ResponseCreateParams params = ResponseCreateParams.builder()
                    .model(MODEL_NAME)
                    .input(createPrompt(candidate))
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
             * AI 호출 실패가 보험추천 전체 실패로 이어지지 않도록
             * 규칙 기반 데이터로 만든 안전한 기본 문구를 저장한다.
             */
            log.error(
                    "[INSURANCE-RECOMMENDATION-AI] 상품별 추천 요약 생성 실패 insuranceProductId={}",
                    candidate.getInsuranceProductId(),
                    e
            );
            return fallback;
        }
    }

    private String createPrompt(
            InsuranceRecommendationCandidateVO candidate
    ) {
        return """
                당신은 KB 보험 추천 결과를 설명하는 금융 서비스입니다.

                아래 내용은 서버의 소비 카테고리 매칭 규칙으로 이미 추천 대상으로 확정된 보험 상품입니다.
                새로운 건강 상태, 질병, 사고 가능성 또는 사용자의 개인정보를 추측하지 마세요.

                [추천 근거 소비 카테고리]
                %s

                [이번에 설명할 보험 상품]
                상품명: %s
                보험 카테고리: %s
                상품 설명: %s
                월 보험료: %s원

                [규칙 기반 추천 이유]
                %s

                [작성 규칙]
                1. 이 보험 상품 한 건에 대한 추천 설명만 한국어로 작성하세요.
                2. 정확히 2문장으로 작성하세요.
                3. 첫 문장은 확인된 소비 카테고리와 상품의 연결 이유를 자연스럽게 설명하세요.
                4. 둘째 문장은 상품 설명 또는 규칙 기반 추천 이유를 바탕으로 어떤 대비에 참고할 수 있는지 설명하세요.
                5. 건강 상태, 질병 유무, 사고 위험, 나이, 직업, 소득, 가족관계를 추측하지 마세요.
                6. 보험 가입이 반드시 필요하다거나 보장이 확정된다고 표현하지 마세요.
                7. 제공되지 않은 보장금액, 가입조건, 면책사항, 할인 내용을 만들어내지 마세요.
                8. 다른 보험 상품과 비교하거나 순위를 새로 만들지 마세요.
                9. 마크다운, 제목, 목록, JSON 없이 문장만 반환하세요.
                """.formatted(
                safe(candidate.getCategoryName(), "관련 소비 카테고리"),
                safe(candidate.getInsuranceName(), "KB 보험 상품"),
                safe(candidate.getInsuranceCategory(), "보험"),
                safe(candidate.getInsuranceDescription(), "등록된 상품 설명 없음"),
                nullSafe(candidate.getMonthlyPremium()),
                safe(candidate.getRecommendationReason(), "관련 소비 카테고리 매칭으로 추천")
        );
    }

    private String createFallbackSummary(
            InsuranceRecommendationCandidateVO candidate
    ) {
        if (candidate == null) {
            return "최근 12개월 소비분석에서 확인된 소비 카테고리를 바탕으로 추천된 보험 상품입니다. 상품의 보장 내용과 가입 조건을 상세 화면에서 확인해 보세요.";
        }

        String categoryName = safe(
                candidate.getCategoryName(),
                "관련 카테고리"
        );
        String insuranceName = safe(
                candidate.getInsuranceName(),
                "이 보험 상품"
        );

        return limitLength(
                "최근 12개월 동안 "
                        + categoryName
                        + " 관련 소비가 확인되어 "
                        + insuranceName
                        + "을 추천했습니다. "
                        + safe(
                                candidate.getRecommendationReason(),
                                "상품의 보장 내용과 가입 조건을 상세 화면에서 확인해 보세요."
                        ),
                MAX_SUMMARY_LENGTH
        );
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
