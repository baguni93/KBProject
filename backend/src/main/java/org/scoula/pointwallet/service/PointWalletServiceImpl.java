package org.scoula.pointwallet.service;

import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
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

        // 포인트 지갑이 존재하지 않는 경우
        if (wallet == null) {
            throw new CustomException(
                    ErrorCode.POINT_WALLET_NOT_FOUND
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

        // 포인트 지갑이 존재하지 않는 경우
        if (wallet == null) {
            throw new CustomException(
                    ErrorCode.POINT_WALLET_NOT_FOUND
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

        // 포인트 지갑이 존재하지 않는 경우
        if (wallet == null) {
            throw new CustomException(
                    ErrorCode.POINT_WALLET_NOT_FOUND
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

        } catch (IllegalArgumentException exception) {
            throw new CustomException(
                    ErrorCode.INVALID_TRANSACTION_TYPE
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

        // 포인트 지갑이 존재하지 않는 경우
        if (wallet == null) {
            throw new CustomException(
                    ErrorCode.POINT_WALLET_NOT_FOUND
            );
        }

        // 포인트 지갑 잔액 증가
        int updatedCount =
                pointWalletMapper.increasePointBalance(
                        wallet.getPointWalletId(),
                        pointAmount
                );

        // 포인트 잔액 반영 실패
        if (updatedCount != 1) {
            log.error(
                    "포인트 잔액 반영 실패 userId={}, pointWalletId={}, amount={}",
                    userId,
                    wallet.getPointWalletId(),
                    pointAmount
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
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
            log.error(
                    "포인트 적립 거래내역 저장 실패 userId={}, pointWalletId={}, amount={}, reasonType={}",
                    userId,
                    wallet.getPointWalletId(),
                    pointAmount,
                    reasonType
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
            );
        }

        PointWalletVO updatedWallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (updatedWallet == null) {
            log.error(
                    "포인트 적립 후 지갑 조회 실패 userId={}",
                    userId
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
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
            throw new CustomException(
                    ErrorCode.INVALID_POINT_AMOUNT
            );
        }

        if (reasonType == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        // 포인트 전환은 적립이 아니라 사용 처리
        if (reasonType == PointReasonType.CONVERSION) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
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
            log.error(
                    "포인트 지갑 생성 실패 userId={}, insertedCount={}",
                    userId,
                    insertedCount
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
            );
        }

        PointWalletVO createdWallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (createdWallet == null) {
            log.error(
                    "생성된 포인트 지갑 조회 실패 userId={}",
                    userId
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
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

        // 포인트 지갑이 없는 경우
        if (wallet == null) {
            throw new CustomException(
                    ErrorCode.POINT_WALLET_NOT_FOUND
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
            log.error(
                    "포인트 취소 반영 실패 userId={}, pointWalletId={}, amount={}",
                    userId,
                    wallet.getPointWalletId(),
                    pointAmount
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
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
            log.error(
                    "포인트 취소 거래내역 저장 실패 userId={}, pointWalletId={}, amount={}, reasonType={}",
                    userId,
                    wallet.getPointWalletId(),
                    pointAmount,
                    reasonType
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
            );
        }

        PointWalletVO updatedWallet =
                pointWalletMapper.selectWalletByUserId(userId);

        if (updatedWallet == null) {
            log.error(
                    "포인트 취소 후 변경된 지갑 조회 실패 userId={}",
                    userId
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
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

        // 취소하는 포인트 금액이 없거나 0이하일떄.
        if (pointAmount == null || pointAmount <= 0) {
            throw new CustomException(
                    ErrorCode.INVALID_POINT_AMOUNT
            );
        }

        // 포인트 취소사유가 들어오지 않았을 때
        if (reasonType == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }


    // 사용자 ID 공통 검증
    private void validateUserId(Integer userId) {

        if (userId == null || userId <= 0) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}