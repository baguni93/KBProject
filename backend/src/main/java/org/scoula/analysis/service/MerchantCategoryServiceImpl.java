package org.scoula.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.domain.MerchantCategoryMappingVO;
import org.scoula.analysis.dto.AiCategoryClassificationResultDTO;
import org.scoula.analysis.dto.MerchantCategoryClassificationResultDTO;
import org.scoula.analysis.mapper.MerchantCategoryMappingMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class MerchantCategoryServiceImpl
        implements MerchantCategoryService {

    private static final String SOURCE_MAPPING = "MAPPING";
    private static final String SOURCE_AI = "AI";
    private static final String SOURCE_UNCLASSIFIED = "UNCLASSIFIED";

    // 구현체가 아니라 인터페이스 타입으로 주입한다.
    private final MerchantCategoryMappingMapper
            merchantCategoryMappingMapper;

    private final AiCategoryClassificationService
            aiCategoryClassificationService;

    @Override
    public MerchantCategoryClassificationResultDTO classify(
            String merchantName
    ) {
        // 가맹점명이 없으면 AI를 호출하지 않고 미분류로 반환한다.
        if (merchantName == null || merchantName.isBlank()) {
            return createUnclassifiedResult(null);
        }

        // 별도의 정규화는 하지 않고 앞뒤 공백만 제거한다.
        String trimmedMerchantName = merchantName.trim();

        // 먼저 기존 가맹점 매핑을 조회한다.
        MerchantCategoryMappingVO existingMapping =
                merchantCategoryMappingMapper
                        .selectByMerchantName(trimmedMerchantName);

        // 기존 매핑이 있으면 AI를 호출하지 않고 바로 반환한다.
        if (existingMapping != null) {
            log.info(
                    "[CATEGORY-MAPPING] 기존 매핑 사용 - merchantName={}, categoryName={}, AI 호출 생략",
                    existingMapping.getMerchantName(),
                    existingMapping.getCategoryName()
            );

            return createMappingResult(existingMapping);
        }

        log.info(
                "[CATEGORY-AI] 기존 매핑 없음 - merchantName={}",
                trimmedMerchantName
        );

        // 매핑이 없는 가맹점만 AI에 분류를 요청한다.
        AiCategoryClassificationResultDTO aiResult =
                aiCategoryClassificationService
                        .classify(trimmedMerchantName);

        // AI 호출 또는 응답 파싱에 실패하면 미분류로 반환한다.
        if (aiResult == null
                || aiResult.getSpendingCategoryId() == null) {

            log.warn(
                    "[CATEGORY-AI] 분류 실패 - merchantName={}",
                    trimmedMerchantName
            );

            return createUnclassifiedResult(trimmedMerchantName);
        }

        // AI가 선택한 카테고리를 새로운 가맹점 매핑으로 저장한다.
        MerchantCategoryMappingVO newMapping =
                MerchantCategoryMappingVO.builder()
                        .merchantName(trimmedMerchantName)
                        .spendingCategoryId(
                                aiResult.getSpendingCategoryId()
                        )
                        .categoryName(aiResult.getCategoryName())
                        .correctionCount(0)
                        .build();

        try {
            merchantCategoryMappingMapper.insertMapping(newMapping);

            log.info(
                    "[CATEGORY-MAPPING] 신규 매핑 생성 - merchantName={}, categoryName={}",
                    trimmedMerchantName,
                    aiResult.getCategoryName()
            );

            return createAiResult(
                    trimmedMerchantName,
                    aiResult
            );

        } catch (DuplicateKeyException e) {
            /*
             * 동시에 같은 가맹점이 분류되면
             * 다른 요청이 먼저 UNIQUE 데이터를 넣었을 수 있다.
             * 이 경우 먼저 저장된 매핑을 다시 조회해서 사용한다.
             */
            MerchantCategoryMappingVO createdMapping =
                    merchantCategoryMappingMapper
                            .selectByMerchantName(trimmedMerchantName);

            if (createdMapping == null) {
                // 중복 외의 예상하지 못한 문제가 있을 수 있으므로 다시 예외를 발생시킨다.
                throw e;
            }

            log.info(
                    "[CATEGORY-MAPPING] 이미 생성된 매핑 재사용 - merchantName={}, categoryName={}",
                    createdMapping.getMerchantName(),
                    createdMapping.getCategoryName()
            );

            return createMappingResult(createdMapping);
        }
    }

    private MerchantCategoryClassificationResultDTO createMappingResult(
            MerchantCategoryMappingVO mapping
    ) {
        return MerchantCategoryClassificationResultDTO.builder()
                .merchantName(mapping.getMerchantName())
                .spendingCategoryId(
                        mapping.getSpendingCategoryId()
                )
                .categoryName(mapping.getCategoryName())
                .classificationSource(SOURCE_MAPPING)
                .mappingCreated(false)
                .build();
    }

    private MerchantCategoryClassificationResultDTO createAiResult(
            String merchantName,
            AiCategoryClassificationResultDTO aiResult
    ) {
        return MerchantCategoryClassificationResultDTO.builder()
                .merchantName(merchantName)
                .spendingCategoryId(
                        aiResult.getSpendingCategoryId()
                )
                .categoryName(aiResult.getCategoryName())
                .classificationSource(SOURCE_AI)
                .mappingCreated(true)
                .build();
    }

    private MerchantCategoryClassificationResultDTO createUnclassifiedResult(
            String merchantName
    ) {
        return MerchantCategoryClassificationResultDTO.builder()
                .merchantName(merchantName)
                .spendingCategoryId(null)
                .categoryName(null)
                .classificationSource(SOURCE_UNCLASSIFIED)
                .mappingCreated(false)
                .build();
    }
}