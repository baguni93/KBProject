package org.scoula.analysis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.dto.MerchantCategoryClassificationResultDTO;
import org.scoula.pointwallet.service.RandomBoxService;
import org.scoula.wallet.dto.PaymentTokenDTO;
import org.scoula.wallet.mapper.WalletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


//
@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentTransactionRecordServiceImpl
        implements PaymentTransactionRecordService {

    private static final String DEFAULT_MERCHANT_NAME =
            "KB Pay 현장 가맹점";

    // 기존 매핑 조회와 AI 분류를 담당
    private final MerchantCategoryService merchantCategoryService;

    // financial_transaction_tbl 결제 거래 저장 담당
    private final WalletMapper walletMapper;

    // 랜덤박스 공통 발급 서비스
    private final RandomBoxService randomBoxService;

    @Override
    @Transactional
    public void recordSuccessfulPayment(
            PaymentTokenDTO tokenDTO,
            Integer amount,
            String merchantName
    ) {
        if (tokenDTO == null) {
            throw new IllegalArgumentException(
                    "결제 토큰 정보가 필요합니다."
            );
        }

        // 가맹점명이 없으면 기존 Controller와 동일한 기본 문구 사용
        String savedMerchantName =
                normalizeMerchantName(merchantName);

        Integer spendingCategoryId = null;
        String classificationSource = "UNCLASSIFIED";

        /*
         * 기본 가맹점명은 실제 업종을 알 수 없으므로
         * AI에 전달하지 않고 미분류 상태로 저장한다.
         */
        if (!DEFAULT_MERCHANT_NAME.equals(savedMerchantName)) {
            MerchantCategoryClassificationResultDTO result =
                    merchantCategoryService.classify(
                            savedMerchantName
                    );

            if (result != null) {
                spendingCategoryId =
                        result.getSpendingCategoryId();

                classificationSource =
                        result.getClassificationSource();
            }
        }

        // 성공한 결제 내역을 통합 거래 테이블에 저장
        int insertedRows =
                walletMapper.insertPaymentTransaction(
                        tokenDTO.getUserId(),
                        amount,
                        savedMerchantName,
                        spendingCategoryId
                );

        /*
         * Controller의 기존 @Transactional과 같은 트랜잭션에 참여한다.
         * INSERT가 실패하면 앞에서 처리한 잔액 차감도 롤백된다.
         */
        if (insertedRows != 1) {
            throw new RuntimeException(
                    "결제 거래 내역 저장에 실패했습니다."
            );
        }

        log.info(
                "결제 거래 기록 완료 - userId={}, merchantName={}, amount={}, categoryId={}, source={}",
                tokenDTO.getUserId(),
                savedMerchantName,
                amount,
                spendingCategoryId,
                classificationSource
        );

        // 카드 / QR / 바코드 결제 성공 보상: 1,000원 이상 시 랜덤박스 발급
//        try {
//            int txId = Math.abs((int)(System.currentTimeMillis() % 100000000)) + 1;
//            randomBoxService.issueForPayment(tokenDTO.getUserId(), txId, amount);
//            log.info("결제 성공 보상 랜덤박스 발급 성공 - userId={}, amount={}", tokenDTO.getUserId(), amount);
//        } catch (Exception rBoxErr) {
//            log.warn("결제 성공 후 랜덤박스 발급 예외 처리 (결제는 승인 완료): {}", rBoxErr.getMessage());
//        }
    }

    private String normalizeMerchantName(
            String merchantName
    ) {
        // 가맹점명이 없으면 기존 Controller의 기본 이름 사용
        if (merchantName == null
                || merchantName.isBlank()) {

            return DEFAULT_MERCHANT_NAME;
        }

        // 별도 변환 없이 앞뒤 공백만 제거
        return merchantName.trim();
    }
}