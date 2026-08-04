package org.scoula.analysis.service;

import org.scoula.wallet.dto.PaymentTokenDTO;

public interface PaymentTransactionRecordService {

    // 성공한 결제를 자동분류하고 거래 내역으로 저장
    void recordSuccessfulPayment(
            PaymentTokenDTO tokenDTO,
            Integer amount,
            String merchantName
    );
}