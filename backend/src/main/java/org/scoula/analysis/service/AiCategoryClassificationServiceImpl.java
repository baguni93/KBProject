package org.scoula.analysis.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.dto.AiCategoryClassificationResultDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AiCategoryClassificationServiceImpl
        implements AiCategoryClassificationService {

    private static final String MODEL_NAME = "gpt-5-nano";

    // 카테고리 ID와 이름을 검증하기 위한 기준 목록
    private static final Map<Integer, String> CATEGORY_MAP =
            createCategoryMap();

    private final OpenAIClient openAIClient;

    /*
     * AI가 예상하지 못한 필드를 추가해도
     * 필요한 필드만 읽을 수 있도록 설정한다.
     */
    private final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            false
                    );

    @Override
    public AiCategoryClassificationResultDTO classify(
            String merchantName
    ) {
        // 가맹점명이 없으면 AI를 호출하지 않는다.
        if (merchantName == null || merchantName.isBlank()) {
            return null;
        }

        String trimmedMerchantName = merchantName.trim();

        try {
            String prompt = createPrompt(trimmedMerchantName);

            // Responses API 요청 객체 생성
            ResponseCreateParams params =
                    ResponseCreateParams.builder()
                            .model(MODEL_NAME)
                            .input(prompt)
                            .build();

            // OpenAI API 호출
            Response response =
                    openAIClient.responses().create(params);

            // SDK 4.6.1의 응답 구조에서 텍스트만 추출
            String responseText =
                    extractOutputText(response);

            if (responseText == null || responseText.isBlank()) {
                log.warn(
                        "[CATEGORY-AI] 빈 응답 반환 - merchantName={}",
                        trimmedMerchantName
                );

                return null;
            }

            // AI가 ```json 형태로 감싸서 반환하는 경우 제거
            String jsonText =
                    removeMarkdownCodeFence(responseText);

            AiCategoryClassificationResultDTO aiResult =
                    objectMapper.readValue(
                            jsonText,
                            AiCategoryClassificationResultDTO.class
                    );

            Integer categoryId =
                    aiResult.getSpendingCategoryId();

            // 0은 AI가 판단하기 어려운 미분류 상태를 의미한다.
            if (categoryId == null || categoryId == 0) {
                log.info(
                        "[CATEGORY-AI] 분류 불가 - merchantName={}",
                        trimmedMerchantName
                );

                return null;
            }

            // 실제 프로젝트에서 허용한 카테고리 ID인지 검증한다.
            String categoryName =
                    CATEGORY_MAP.get(categoryId);

            if (categoryName == null) {
                log.warn(
                        "[CATEGORY-AI] 존재하지 않는 카테고리 반환"
                                + " - merchantName={}, categoryId={}",
                        trimmedMerchantName,
                        categoryId
                );

                return null;
            }

            /*
             * AI가 반환한 categoryName은 그대로 신뢰하지 않고,
             * 서버에 등록된 정확한 카테고리명으로 교체한다.
             */
            aiResult.setCategoryName(categoryName);

            log.info(
                    "[CATEGORY-AI] 분류 완료"
                            + " - merchantName={}, categoryId={}, categoryName={}",
                    trimmedMerchantName,
                    categoryId,
                    categoryName
            );

            return aiResult;

        } catch (Exception e) {
            /*
             * AI 호출 실패 때문에 결제 거래 전체를 실패시키지 않는다.
             * null을 반환하면 상위 서비스에서 미분류로 처리한다.
             */
            log.error(
                    "[CATEGORY-AI] 분류 처리 중 오류"
                            + " - merchantName={}",
                    trimmedMerchantName,
                    e
            );

            return null;
        }
    }

    private String createPrompt(String merchantName) {
        String categoryList =
                CATEGORY_MAP.entrySet()
                        .stream()
                        .map(entry ->
                                entry.getKey()
                                        + ": "
                                        + entry.getValue()
                        )
                        .collect(Collectors.joining("\n"));

        return """
                당신은 금융 거래의 가맹점명을 보고
                소비 카테고리를 분류하는 시스템입니다.

                다음 카테고리 중 반드시 하나를 선택하세요.

                %s

                분류 규칙:
                1. 가맹점명만을 기준으로 가장 적절한 카테고리를 선택하세요.
                2. 병원 진료과가 명확하면 산부인과, 안과, 내과,
                   정형외과, 한의원, 치과, 소아과 중 하나를 선택하세요.
                3. 병원은 확인되지만 진료과를 알 수 없으면
                   병원 카테고리를 선택하세요.
                4. 가맹점명만으로 전혀 판단할 수 없으면
                   spendingCategoryId를 0으로 반환하세요.
                5. 설명이나 마크다운 없이 JSON 객체 하나만 반환하세요.
                6. JSON 필드명은 아래 형식을 정확히 지키세요.

                반환 형식:
                {"spendingCategoryId": 2, "categoryName": "카페"}

                분류할 가맹점명:
                %s
                """.formatted(
                categoryList,
                merchantName
        );
    }

    private String extractOutputText(Response response) {
        /*
         * Response
         * → output
         * → message
         * → content
         * → outputText
         * 순서로 실제 답변 문자열을 추출한다.
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

    private String removeMarkdownCodeFence(String responseText) {
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

    // 이부분은 DB에서 가져오면, 성능나빠지는 대신, 관리가 편해짐
    // 자바 코드로 남기면 성능이 좋아지고, 관리가 나빠진다.
    private static Map<Integer, String> createCategoryMap() {
        /*
         * LinkedHashMap을 사용해 프롬프트에
         * 카테고리가 ID 순서대로 전달되도록 한다.
         */
        Map<Integer, String> categories =
                new LinkedHashMap<>();

        categories.put(1, "식비");
        categories.put(2, "카페");
        categories.put(3, "생활");
        categories.put(4, "온라인쇼핑");
        categories.put(5, "뷰티/미용");
        categories.put(6, "교통");
        categories.put(7, "자동차");
        categories.put(8, "주거/통신");
        categories.put(9, "금융");
        categories.put(10, "여행");
        categories.put(11, "교육");
        categories.put(12, "반려동물");
        categories.put(13, "병원");
        categories.put(14, "산부인과");
        categories.put(15, "안과");
        categories.put(16, "내과");
        categories.put(17, "정형외과");
        categories.put(18, "한의원");
        categories.put(19, "치과");
        categories.put(20, "소아과");

        return Collections.unmodifiableMap(categories);
    }
}