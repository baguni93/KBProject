package org.scoula.pointwallet.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.pointwallet.dto.PointTransactionDTO;
import org.scoula.pointwallet.dto.PointWalletDTO;
import org.scoula.pointwallet.domain.PointWalletVO;

import java.util.List;


public interface PointWalletMapper {

    // 유저 선택
    PointWalletVO selectWalletByUserId(
            @Param("userId") Integer userId
    );

    // 유저 거래내역 조회
    List<PointTransactionDTO> selectTransactionsByUserId(
            @Param("userId") Integer userId,
            @Param("transactionType") String transactionType
    );

    // 사용자 포인트 지갑 자동생성(회원 가입시 이거 자동 호출해야 댐 ㅇㅅㅇ)
    int insertPointWallet(
            @Param("userId") Integer userId
    );


    // 포인트 잔액 증가
    int increasePointBalance(
            @Param("pointWalletId") Integer pointWalletId,
            @Param("pointAmount") Integer pointAmount
    );

    // 포인트 지갑 메인에 표시할 최근 거래내역 5건 조회
    List<PointTransactionDTO> selectRecentTransactionsByUserId(
            @Param("userId") Integer userId
    );

    // 포인트 거래내역 저장
    int insertPointTransaction(
            @Param("pointWalletId") Integer pointWalletId,
            @Param("transactionType") String transactionType,
            @Param("pointAmount") Integer pointAmount,
            @Param("reasonType") String reasonType
    );


    PointWalletVO selectPointWalletForUpdate(
            @Param("userId") Integer userId
    );

    // 포인트 단순 차감- > 취소 용도
    int decreasePointBalance(
            @Param("userId") Integer userId,
            @Param("pointAmount") Integer pointAmount
    );

    // 포인트 전환 시 잔액이 충분한 경우에만 차감 -> 전환 용도
    int decreasePointBalanceIfSufficient(
            @Param("pointWalletId") Integer pointWalletId,
            @Param("pointAmount") Integer pointAmount
    );

}