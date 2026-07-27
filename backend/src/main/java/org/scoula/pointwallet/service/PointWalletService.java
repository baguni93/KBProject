package org.scoula.pointwallet.service;

import org.scoula.pointwallet.common.PointReasonType;
import org.scoula.pointwallet.dto.PointTransactionDTO;
import org.scoula.pointwallet.dto.PointWalletDTO;

import java.util.List;

public interface PointWalletService {

    // 포인트 지갑을 생성한다.
    PointWalletDTO createWallet(Integer userId);

    // 포인트 지갑 조회
    PointWalletDTO getWallet(Integer userId);

    // 거래내역 조회(전체+ 카테고리 기능까지)
    List<PointTransactionDTO> getTransactions(
            Integer userId,
            String transactionType
    );

    // 출석, 랜덤박스, 이벤트에 의해 포인트를 적립한다.
    PointWalletDTO earnPoints(
            Integer userId,
            Integer pointAmount,
            PointReasonType reasonType
    );

    // 최근 포인트 거래내역 5건 조회
    List<PointTransactionDTO> getRecentTransactions(
            Integer userId
    );

    // 취소 포인트
    PointWalletDTO cancelPoints(
            Integer userId,
            Integer pointAmount,
            PointReasonType reasonType
    );


}