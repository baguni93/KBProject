package org.scoula.pointwallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.common.PointReasonType;
import org.scoula.pointwallet.common.PointTransactionType;
import org.scoula.pointwallet.domain.PointWalletVO;
import org.scoula.pointwallet.dto.PointTransactionDTO;
import org.scoula.pointwallet.dto.PointWalletDTO;
import org.scoula.pointwallet.mapper.PointWalletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class PointWalletServiceImpl implements PointWalletService {

    // 포인트 지갑 매퍼
    private final PointWalletMapper pointWalletMapper;


    // 사용자의 포인트 지갑 가져오기
    @Override
    public PointWalletDTO getWallet(Integer userId) {

        validateUserId(userId);

        PointWalletVO wallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "포인트 지갑이 존재하지 않습니다."
            );
        }

        return wallet.toDTO();
    }


    // 사용자의 거래 내역 상세 조회
    @Override
    public List<PointTransactionDTO> getTransactions(
            Integer userId,
            String transactionType
    ) {
        validateUserId(userId);

        PointWalletVO wallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "포인트 지갑이 존재하지 않습니다."
            );
        }

        String normalizedType =
                normalizeTransactionType(transactionType);

        List<PointTransactionDTO> transactions =
                pointWalletMapper.selectTransactionsByUserId(
                        userId,
                        normalizedType
                );

        log.info(
                "포인트 거래내역 조회 userId={}, type={}, count={}",
                userId,
                normalizedType,
                transactions.size()
        );

        return transactions;
    }


    // 포인트 지갑 메인 화면에 표시할 최근 거래내역 5건 조회
    @Override
    public List<PointTransactionDTO> getRecentTransactions(
            Integer userId
    ) {
        validateUserId(userId);

        PointWalletVO wallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "포인트 지갑이 존재하지 않습니다."
            );
        }

        List<PointTransactionDTO> transactions =
                pointWalletMapper.selectRecentTransactionsByUserId(
                        userId
                );

        log.info(
                "최근 포인트 거래내역 조회 userId={}, count={}",
                userId,
                transactions.size()
        );

        return transactions;
    }


    // 사용자 거래 유형 검증
    // 거래 유형이 없거나 ALL이면 전체 조회를 위해 null 반환
    private String normalizeTransactionType(
            String transactionType
    ) {
        if (transactionType == null
                || transactionType.trim().isEmpty()
                || "ALL".equalsIgnoreCase(transactionType.trim())) {

            return null;
        }

        String normalized =
                transactionType.trim().toUpperCase();

        try {
            PointTransactionType.valueOf(normalized);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "거래 유형은 ALL, EARN, USE, EXPIRE, CANCEL 중 하나여야 합니다."
            );
        }

        return normalized;
    }


    // 출석, 랜덤박스, 이벤트 등에 의해 포인트를 적립
    @Override
    @Transactional
    public PointWalletDTO earnPoints(
            Integer userId,
            Integer pointAmount,
            PointReasonType reasonType
    ) {
        validateEarnRequest(
                userId,
                pointAmount,
                reasonType
        );

        PointWalletVO wallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "포인트 지갑이 존재하지 않습니다."
            );
        }

        // 포인트 지갑 잔액 증가
        int updatedCount =
                pointWalletMapper.increasePointBalance(
                        wallet.getPointWalletId(),
                        pointAmount
                );

        if (updatedCount != 1) {
            throw new IllegalStateException(
                    "포인트 잔액 반영에 실패했습니다."
            );
        }

        // 포인트 적립 거래내역 저장
        int insertedCount =
                pointWalletMapper.insertPointTransaction(
                        wallet.getPointWalletId(),
                        PointTransactionType.EARN.name(),
                        pointAmount,
                        reasonType.name()
                );

        if (insertedCount != 1) {
            throw new IllegalStateException(
                    "포인트 거래내역 저장에 실패했습니다."
            );
        }

        PointWalletVO updatedWallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (updatedWallet == null) {
            throw new IllegalStateException(
                    "변경된 포인트 지갑을 조회하지 못했습니다."
            );
        }

        log.info(
                "포인트 적립 완료 userId={}, amount={}, reasonType={}",
                userId,
                pointAmount,
                reasonType
        );

        return updatedWallet.toDTO();
    }


    // 포인트 적립 요청 검증
    private void validateEarnRequest(
            Integer userId,
            Integer pointAmount,
            PointReasonType reasonType
    ) {
        validateUserId(userId);

        if (pointAmount == null || pointAmount <= 0) {
            throw new IllegalArgumentException(
                    "적립 포인트는 0보다 커야 합니다."
            );
        }

        if (reasonType == null) {
            throw new IllegalArgumentException(
                    "포인트 적립 사유는 필수입니다."
            );
        }

        // 포인트 전환은 적립이 아니라 사용 처리
        if (reasonType == PointReasonType.CONVERSION) {
            throw new IllegalArgumentException(
                    "CONVERSION은 포인트 적립 사유로 사용할 수 없습니다."
            );
        }
    }


    // 포인트 지갑 생성
    @Override
    @Transactional
    public PointWalletDTO createWallet(Integer userId) {

        validateUserId(userId);

        /*
         * 회원가입 처리 재시도 등의 이유로 같은 메서드가 여러 번 호출되더라도
         * 지갑이 중복 생성되지 않도록 기존 지갑을 먼저 확인한다.
         */
        PointWalletVO existingWallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (existingWallet != null) {
            log.info(
                    "포인트 지갑이 이미 존재합니다. userId={}, pointWalletId={}",
                    userId,
                    existingWallet.getPointWalletId()
            );

            return existingWallet.toDTO();
        }

        int insertedCount =
                pointWalletMapper.insertPointWallet(userId);

        if (insertedCount != 1) {
            throw new IllegalStateException(
                    "포인트 지갑 생성에 실패했습니다."
            );
        }

        PointWalletVO createdWallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (createdWallet == null) {
            throw new IllegalStateException(
                    "생성된 포인트 지갑을 조회하지 못했습니다."
            );
        }

        log.info(
                "포인트 지갑 생성 완료 userId={}, pointWalletId={}",
                userId,
                createdWallet.getPointWalletId()
        );

        return createdWallet.toDTO();
    }


    // 지급했던 포인트 취소
    @Override
    @Transactional
    public PointWalletDTO cancelPoints(
            Integer userId,
            Integer pointAmount,
            PointReasonType reasonType
    ) {
        validateCancelRequest(
                userId,
                pointAmount,
                reasonType
        );

        /*
         * 취소 처리 중 같은 사용자의 포인트가 동시에 변경되는 것을 막기 위해
         * 포인트 지갑 행을 잠근 상태로 조회한다.
         */
        PointWalletVO wallet =
                pointWalletMapper.selectPointWalletForUpdate(userId);

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "포인트 지갑이 존재하지 않습니다."
            );
        }

        /*
         * 취소 포인트는 잔액이 부족하더라도 차감한다.
         * point_balance는 음수가 될 수 있다.
         */
        int updatedCount =
                pointWalletMapper.decreasePointBalance(
                        userId,
                        pointAmount
                );

        if (updatedCount != 1) {
            throw new IllegalStateException(
                    "포인트 취소 반영에 실패했습니다."
            );
        }

        /*
         * point_amount는 양수로 저장하고,
         * 차감 여부는 transaction_type의 CANCEL로 구분한다.
         */
        int insertedCount =
                pointWalletMapper.insertPointTransaction(
                        wallet.getPointWalletId(),
                        PointTransactionType.CANCEL.name(),
                        pointAmount,
                        reasonType.name()
                );

        if (insertedCount != 1) {
            throw new IllegalStateException(
                    "포인트 취소 거래내역 저장에 실패했습니다."
            );
        }

        PointWalletVO updatedWallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (updatedWallet == null) {
            throw new IllegalStateException(
                    "변경된 포인트 지갑을 조회하지 못했습니다."
            );
        }

        log.info(
                "포인트 취소 완료 userId={}, amount={}, reasonType={}, balance={}",
                userId,
                pointAmount,
                reasonType,
                updatedWallet.getPointBalance()
        );

        return updatedWallet.toDTO();
    }


    // 포인트 취소 요청 검증
    private void validateCancelRequest(
            Integer userId,
            Integer pointAmount,
            PointReasonType reasonType
    ) {
        validateUserId(userId);

        if (pointAmount == null || pointAmount <= 0) {
            throw new IllegalArgumentException(
                    "취소 포인트는 0보다 커야 합니다."
            );
        }

        if (reasonType == null) {
            throw new IllegalArgumentException(
                    "포인트 취소 사유는 필수입니다."
            );
        }
    }


    // 사용자 ID 공통 검증
    private void validateUserId(Integer userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "유효한 사용자 ID가 필요합니다."
            );
        }
    }
}