package org.scoula.pointwallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.common.PointReasonType;
import org.scoula.pointwallet.common.PointTransactionType;
import org.scoula.pointwallet.domain.PointConversionVO;
import org.scoula.pointwallet.domain.PointWalletVO;
import org.scoula.pointwallet.domain.WalletVO;
import org.scoula.pointwallet.dto.PointConversionResultDTO;
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

        if (pointWallet == null) {
            throw new IllegalArgumentException(
                    "포인트 지갑이 존재하지 않습니다."
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

        if (wallet == null) {
            throw new IllegalArgumentException(
                    "전자지갑이 존재하지 않습니다."
            );
        }

        if (!"ACTIVE".equals(wallet.getWalletStatus())) {
            throw new IllegalStateException(
                    "전자지갑이 사용 가능한 상태가 아닙니다."
            );
        }

        if (pointWallet.getPointBalance() < pointAmount) {
            throw new IllegalStateException(
                    "보유 포인트가 부족합니다."
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

        if (pointUpdatedCount != 1) {
            throw new IllegalStateException(
                    "포인트 차감에 실패했습니다."
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

        if (transactionInsertedCount != 1) {
            throw new IllegalStateException(
                    "포인트 전환 거래내역 저장에 실패했습니다."
            );
        }

        int walletUpdatedCount =
                walletMapper.increaseWalletBalance(
                        wallet.getWalletId(),
                        pointAmount
                );

        if (walletUpdatedCount != 1) {
            throw new IllegalStateException(
                    "전자지갑 잔액 반영에 실패했습니다."
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
            throw new IllegalStateException(
                    "포인트 전환 이력 저장에 실패했습니다."
            );
        }

        if (pointConversion.getPointConversionId() == null) {
            throw new IllegalStateException(
                    "포인트 전환 이력 ID를 확인하지 못했습니다."
            );
        }

        PointWalletVO updatedPointWallet =
                pointWalletMapper.selectWalletByUserId(
                        userId
                );

        if (updatedPointWallet == null) {
            throw new IllegalStateException(
                    "변경된 포인트 지갑을 조회하지 못했습니다."
            );
        }

        WalletVO updatedWallet =
                walletMapper.selectWalletByUserId(
                        userId
                );

        if (updatedWallet == null) {
            throw new IllegalStateException(
                    "변경된 전자지갑을 조회하지 못했습니다."
            );
        }

        PointConversionVO savedConversion =
                pointConversionMapper.selectPointConversionById(
                        pointConversion.getPointConversionId()
                );

        if (savedConversion == null) {
            throw new IllegalStateException(
                    "저장된 포인트 전환 이력을 조회하지 못했습니다."
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
            throw new IllegalArgumentException(
                    "유효한 사용자 ID가 필요합니다."
            );
        }

        if (pointAmount == null || pointAmount <= 0) {
            throw new IllegalArgumentException(
                    "전환 포인트는 0보다 커야 합니다."
            );
        }
    }
}