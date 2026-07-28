package org.scoula.pointwallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.common.PointReasonType;
import org.scoula.pointwallet.common.PointTransactionType;
import org.scoula.pointwallet.domain.PointConversionVO;
import org.scoula.pointwallet.domain.PointWalletVO;
import org.scoula.pointwallet.domain.WalletVO;
import org.scoula.pointwallet.dto.PointConversionResultDTO;
import org.scoula.pointwallet.exception.PointWalletErrorCode;
import org.scoula.pointwallet.exception.PointWalletException;
import org.scoula.pointwallet.mapper.PointConversionMapper;
import org.scoula.pointwallet.mapper.PointWalletMapper;
import org.scoula.pointwallet.mapper.WalletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class PointConversionServiceImpl implements PointConversionService {

    private final PointWalletMapper pointWalletMapper;
    private final WalletMapper walletMapper;
    private final PointConversionMapper pointConversionMapper;
    private static final int MINIMUM_CONVERSION_POINT = 100;

    @Override
    @Transactional
    public PointConversionResultDTO convertPoints(
            Integer userId,
            Integer pointAmount
    ) {
        validateConversionRequest(
                userId,
                pointAmount
        );

        /*
         * 포인트 차감 과정에서 동일 사용자의 전환 요청이
         * 동시에 처리되지 않도록 포인트 지갑 행을 잠근다.
         */
        PointWalletVO pointWallet =
                pointWalletMapper.selectPointWalletForUpdate(
                        userId
                );

        // 포인트 지갑이 존재하지 않는 경우
        if (pointWallet == null) {
            throw new PointWalletException(
                    PointWalletErrorCode.WALLET_NOT_FOUND
            );
        }

        /*
         * 포인트 지갑 잠금 이후 전자지갑도 동일한 순서로 잠근다.
         * 다른 서비스에서도 두 지갑을 함께 수정한다면
         * 동일한 잠금 순서를 사용해야 한다.
         */
        WalletVO wallet =
                walletMapper.selectWalletForUpdate(
                        userId
                );

        // 전자 지갑이 존재하지 않는 경우
        if (wallet == null) {
            throw new PointWalletException(
                    PointWalletErrorCode.WALLET_NOT_FOUND
            );
        }

        // 전자 지갑이 사용 가능한 상태가 아닌 경우
        if (!"ACTIVE".equals(wallet.getWalletStatus())) {
            throw new PointWalletException(
                    PointWalletErrorCode.WALLET_NOT_ACTIVE
            );
        }

        // 보유 포인트가 부족한 경우
        if (pointWallet.getPointBalance() < pointAmount) {
            throw new PointWalletException(
                    PointWalletErrorCode.INSUFFICIENT_POINT
            );
        }

        /*
         * Service에서 잔액을 확인했지만,
         * UPDATE SQL에서도 잔액 조건을 다시 확인한다.
         */
        int pointUpdatedCount =
                pointWalletMapper.decreasePointBalanceIfSufficient(
                        pointWallet.getPointWalletId(),
                        pointAmount
                );

        // 포인트 차감에 실패한 경우.
        if (pointUpdatedCount != 1) {
            log.error(
                    "포인트 차감 실패 userId={}, pointWalletId={}, amount={}",
                    userId,
                    pointWallet.getPointWalletId(),
                    pointAmount
            );

            throw new PointWalletException(
                    PointWalletErrorCode.INTERNAL_PROCESS_ERROR
            );
        }

        /*
         * point_amount는 양수로 저장하고,
         * 실제 차감 여부는 USE로 구분한다.
         */
        int transactionInsertedCount =
                pointWalletMapper.insertPointTransaction(
                        pointWallet.getPointWalletId(),
                        PointTransactionType.USE.name(),
                        pointAmount,
                        PointReasonType.CONVERSION.name()
                );

        // 포인트 전환 거래내역 저장에 실패
        if (transactionInsertedCount != 1) {
            log.error(
                    "포인트 전환 거래내역 저장 실패 userId={}, pointWalletId={}, amount={}",
                    userId,
                    pointWallet.getPointWalletId(),
                    pointAmount
            );

            throw new PointWalletException(
                    PointWalletErrorCode.INTERNAL_PROCESS_ERROR
            );
        }

        int walletUpdatedCount =
                walletMapper.increaseWalletBalance(
                        wallet.getWalletId(),
                        pointAmount
                );

        if (walletUpdatedCount != 1) {
            log.error(
                    "전자지갑 잔액 반영 실패 userId={}, walletId={}, amount={}",
                    userId,
                    wallet.getWalletId(),
                    pointAmount
            );

            throw new PointWalletException(
                    PointWalletErrorCode.INTERNAL_PROCESS_ERROR
            );
        }

        PointConversionVO pointConversion =
                PointConversionVO.builder()
                        .userId(userId)
                        .pointWalletId(
                                pointWallet.getPointWalletId()
                        )
                        .walletId(
                                wallet.getWalletId()
                        )
                        .convertedPoint(pointAmount)
                        .build();

        int conversionInsertedCount =
                pointConversionMapper.insertPointConversion(
                        pointConversion
                );

        if (conversionInsertedCount != 1) {
            log.error(
                    "포인트 전환 이력 저장 실패 userId={}, walletId={}, amount={}",
                    userId,
                    wallet.getWalletId(),
                    pointAmount
            );

            throw new PointWalletException(
                    PointWalletErrorCode.INTERNAL_PROCESS_ERROR
            );
        }

        if (pointConversion.getPointConversionId() == null) {
            log.error(
                    "포인트 전환 이력 ID 생성 실패 userId={}, walletId={}, amount={}",
                    userId,
                    wallet.getWalletId(),
                    pointAmount
            );

            throw new PointWalletException(
                    PointWalletErrorCode.INTERNAL_PROCESS_ERROR
            );
        }

        PointWalletVO updatedPointWallet =
                pointWalletMapper.selectWalletByUserId(
                        userId
                );

        if (updatedPointWallet == null) {
            log.error(
                    "변경된 포인트 지갑 조회 실패 userId={}",
                    userId
            );

            throw new PointWalletException(
                    PointWalletErrorCode.INTERNAL_PROCESS_ERROR
            );
        }

        WalletVO updatedWallet =
                walletMapper.selectWalletByUserId(
                        userId
                );

        if (updatedWallet == null) {
            log.error(
                    "변경된 전자지갑 조회 실패 userId={}",
                    userId
            );

            throw new PointWalletException(
                    PointWalletErrorCode.INTERNAL_PROCESS_ERROR
            );
        }

        PointConversionVO savedConversion =
                pointConversionMapper.selectPointConversionById(
                        pointConversion.getPointConversionId()
                );

        if (savedConversion == null) {
            log.error(
                    "저장된 포인트 전환 이력 조회 실패 pointConversionId={}",
                    pointConversion.getPointConversionId()
            );

            throw new PointWalletException(
                    PointWalletErrorCode.INTERNAL_PROCESS_ERROR
            );
        }

        log.info(
                "포인트 전환 완료 userId={}, pointConversionId={}, amount={}, pointBalance={}, walletBalance={}",
                userId,
                savedConversion.getPointConversionId(),
                pointAmount,
                updatedPointWallet.getPointBalance(),
                updatedWallet.getBalance()
        );

        return savedConversion.toResultDTO(
                updatedPointWallet.getPointBalance(),
                updatedWallet.getBalance()
        );
    }

    // 포인트 전환 요청 검증
    private void validateConversionRequest(
            Integer userId,
            Integer pointAmount
    ) {
        if (userId == null || userId <= 0) {
            throw new PointWalletException(
                    PointWalletErrorCode.INVALID_REQUEST,
                    "유효한 사용자 ID가 필요합니다."
            );
        }

        if (pointAmount == null
                || pointAmount < MINIMUM_CONVERSION_POINT) {

            throw new PointWalletException(
                    PointWalletErrorCode.INVALID_POINT_AMOUNT,
                    "포인트 전환은 최소 "
                            + MINIMUM_CONVERSION_POINT
                            + "포인트부터 가능합니다."
            );
        }
    }

}