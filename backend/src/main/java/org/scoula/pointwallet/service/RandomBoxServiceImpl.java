package org.scoula.pointwallet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.common.PointReasonType;
import org.scoula.pointwallet.common.RandomBoxIssueReason;
import org.scoula.pointwallet.common.RandomBoxRevokeReason;
import org.scoula.pointwallet.common.RandomBoxStatus;
import org.scoula.pointwallet.domain.UserRandomBoxVO;
import org.scoula.pointwallet.dto.*;
import org.scoula.pointwallet.mapper.RandomBoxMapper;
import org.scoula.pointwallet.policy.RandomBoxIssuePolicy;
import org.scoula.pointwallet.policy.RandomBoxRewardPolicy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class RandomBoxServiceImpl implements RandomBoxService {

    private final RandomBoxMapper randomBoxMapper;
    private final PointWalletService pointWalletService;

    @Override
    @Transactional
    public RandomBoxIssueResultDTO issueForAttendance(
            Integer userId,
            Integer attendanceId
    ) {
        return issueRandomBox(
                userId,
                RandomBoxIssueReason.ATTENDANCE,
                attendanceId
        );
    }

    @Override
    @Transactional
    public RandomBoxIssueResultDTO issueForPayment(
            Integer userId,
            Integer transactionId,
            Integer paymentAmount
    ) {
        validatePositiveId(
                userId,
                "유효한 사용자 ID가 필요합니다."
        );

        validatePositiveId(
                transactionId,
                "유효한 결제 거래 ID가 필요합니다."
        );

        if (paymentAmount == null || paymentAmount <= 0) {
            throw new IllegalArgumentException(
                    "유효한 결제 금액이 필요합니다."
            );
        }

        /*
         * 1,000원 미만 결제는 잘못된 결제가 아니다.
         * 정상 결제로 처리하되 랜덤박스만 지급하지 않는다.
         */
        if (paymentAmount
                < RandomBoxIssuePolicy.PAYMENT_MINIMUM_AMOUNT) {

            log.info(
                    "결제 랜덤박스 미지급 userId={}, transactionId={}, amount={}",
                    userId,
                    transactionId,
                    paymentAmount
            );

            return RandomBoxIssueResultDTO.builder()
                    .issued(false)
                    .message("1,000원 미만 결제는 랜덤박스 지급 대상이 아닙니다.")
                    .userRandomBoxId(null)
                    .issueReason(RandomBoxIssueReason.PAYMENT.name())
                    .boxStatus(null)
                    .issuedAt(null)
                    .build();
        }

        // TODO-INTEGRATION: 결제 거래가 완료 상태이고 현재 사용자의 거래인지 검증
        return issueRandomBox(
                userId,
                RandomBoxIssueReason.PAYMENT,
                transactionId
        );
    }

    @Override
    @Transactional
    public RandomBoxIssueResultDTO issueForFeedShare(
            Integer userId,
            Integer feedId
    ) {
        // TODO-INTEGRATION: 현재 사용자가 생성한 피드인지 검증
        // TODO-INTEGRATION: FRIEND 또는 PUBLIC 공유가 실제로 완료됐는지 검증
        return issueRandomBox(
                userId,
                RandomBoxIssueReason.FEED_SHARE,
                feedId
        );
    }

    @Override
    @Transactional
    public RandomBoxIssueResultDTO claimFromReceivedFeed(
            Integer userId,
            Integer feedId
    ) {
        // TODO-INTEGRATION: 현재 사용자가 해당 피드를 실제로 수신했는지 검증
        // TODO-INTEGRATION: 비공개 피드 또는 본인 피드는 수신 보상에서 제외
        return issueRandomBox(
                userId,
                RandomBoxIssueReason.FEED_RECEIVE_CLAIM,
                feedId
        );
    }

    @Override
    @Transactional
    public RandomBoxIssueResultDTO issueForEvent(
            Integer userId,
            Integer participationId
    ) {
        // TODO-INTEGRATION: 이벤트 참여가 완료 상태인지 검증
        return issueRandomBox(
                userId,
                RandomBoxIssueReason.EVENT,
                participationId
        );
    }

    @Override
    @Transactional
    public void revokePaymentRandomBox(
            Integer userId,
            Integer transactionId
    ) {
        validatePositiveId(
                userId,
                "유효한 사용자 ID가 필요합니다."
        );

        validatePositiveId(
                transactionId,
                "유효한 결제 거래 ID가 필요합니다."
        );

        UserRandomBoxVO randomBox =
                randomBoxMapper.selectRandomBoxBySource(
                        userId,
                        RandomBoxIssueReason.PAYMENT.name(),
                        transactionId
                );

        // 1,000원 미만 결제 등으로 랜덤박스가 지급되지 않은 경우
        if (randomBox == null) {
            return;
        }

        String currentStatus = randomBox.getBoxStatus();

        // 이미 회수된 경우 중복 처리하지 않음
        if (RandomBoxStatus.REVOKED.name()
                .equals(currentStatus)) {
            return;
        }

        if (!RandomBoxStatus.UNOPENED.name()
                .equals(currentStatus)
                && !RandomBoxStatus.OPENED.name()
                .equals(currentStatus)) {

            throw new IllegalStateException(
                    "회수할 수 없는 랜덤박스 상태입니다."
            );
        }

        /*
         * 먼저 상태를 REVOKED로 변경한다.
         *
         * WHERE box_status = expectedStatus 조건 때문에
         * 동일 취소 요청이 동시에 들어와도 한 요청만 성공한다.
         *
         * 이후 포인트 취소가 실패하면 @Transactional에 의해
         * 랜덤박스 상태 변경도 함께 롤백된다.
         */
        int updatedCount =
                randomBoxMapper.revokeRandomBox(
                        randomBox.getUserRandomBoxId(),
                        userId,
                        currentStatus,
                        RandomBoxRevokeReason.PAYMENT_CANCEL.name()
                );

        if (updatedCount != 1) {
            throw new IllegalStateException(
                    "랜덤박스 회수에 실패했거나 이미 회수되었습니다."
            );
        }

        // 미개봉 랜덤박스는 지급된 포인트가 없으므로 상태만 회수
        if (RandomBoxStatus.UNOPENED.name()
                .equals(currentStatus)) {
            return;
        }

        Integer rewardPoint = randomBox.getRewardPoint();

        if (rewardPoint == null || rewardPoint < 0) {
            throw new IllegalStateException(
                    "개봉된 랜덤박스의 보상 포인트가 올바르지 않습니다."
            );
        }

        // 보상이 0포인트이면 포인트 거래내역을 만들 수 없으므로 상태만 회수
        if (rewardPoint == 0) {
            return;
        }

        pointWalletService.cancelPoints(
                userId,
                rewardPoint,
                PointReasonType.RANDOM_BOX
        );
    }

    private RandomBoxIssueResultDTO issueRandomBox(
            Integer userId,
            RandomBoxIssueReason issueReason,
            Integer sourceId
    ) {
        validateIssueRequest(
                userId,
                issueReason,
                sourceId
        );

        int duplicateCount =
                randomBoxMapper.countIssuedRandomBox(
                        userId,
                        issueReason.name(),
                        sourceId
                );

        if (duplicateCount > 0) {
            throw new IllegalStateException(
                    "이미 해당 조건으로 랜덤박스가 지급되었습니다."
            );
        }

        validateDailyLimit(
                userId,
                issueReason
        );

        UserRandomBoxVO randomBoxVO =
                UserRandomBoxVO.builder()
                        .userId(userId)
                        .issueReason(issueReason.name())
                        .sourceId(sourceId)
                        .boxStatus(RandomBoxStatus.UNOPENED.name())
                        .rewardPoint(null)
                        .openedAt(null)
                        .revokedAt(null)
                        .revokeReason(null)
                        .build();

        try {
            int insertedCount =
                    randomBoxMapper.insertRandomBox(randomBoxVO);

            if (insertedCount != 1) {
                throw new IllegalStateException(
                        "랜덤박스 지급에 실패했습니다."
                );
            }
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException(
                    "이미 해당 조건으로 랜덤박스가 지급되었습니다.",
                    e
            );
        }

        if (randomBoxVO.getUserRandomBoxId() == null) {
            throw new IllegalStateException(
                    "생성된 랜덤박스 ID를 확인하지 못했습니다."
            );
        }

        UserRandomBoxVO createdRandomBox =
                randomBoxMapper.selectRandomBoxById(
                        randomBoxVO.getUserRandomBoxId(),
                        userId
                );

        if (createdRandomBox == null) {
            throw new IllegalStateException(
                    "지급된 랜덤박스를 조회하지 못했습니다."
            );
        }

        return createdRandomBox.toIssueResultDTO();
    }

    private void validateDailyLimit(
            Integer userId,
            RandomBoxIssueReason issueReason
    ) {
        int dailyLimit;

        if (issueReason
                == RandomBoxIssueReason.FEED_SHARE) {

            dailyLimit =
                    RandomBoxIssuePolicy.FEED_SHARE_DAILY_LIMIT;

        } else if (issueReason
                == RandomBoxIssueReason.FEED_RECEIVE_CLAIM) {

            dailyLimit =
                    RandomBoxIssuePolicy.FEED_RECEIVE_DAILY_LIMIT;

        } else {
            return;
        }

        int todayIssuedCount =
                randomBoxMapper.countTodayIssuedByReason(
                        userId,
                        issueReason.name()
                );

        if (todayIssuedCount >= dailyLimit) {
            throw new IllegalStateException(
                    "오늘 받을 수 있는 랜덤박스의 최대 개수를 초과했습니다."
            );
        }
    }

    private void validateIssueRequest(
            Integer userId,
            RandomBoxIssueReason issueReason,
            Integer sourceId
    ) {
        validatePositiveId(
                userId,
                "유효한 사용자 ID가 필요합니다."
        );

        if (issueReason == null) {
            throw new IllegalArgumentException(
                    "랜덤박스 지급 사유가 필요합니다."
            );
        }

        validatePositiveId(
                sourceId,
                "유효한 지급 원본 ID가 필요합니다."
        );
    }

    private void validatePositiveId(
            Integer value,
            String message
    ) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    @Transactional
    public RandomBoxOpenResultDTO openRandomBox(
            Integer userId,
            Integer userRandomBoxId
    ) {
        validatePositiveId(
                userId,
                "유효한 사용자 ID가 필요합니다."
        );

        validatePositiveId(
                userRandomBoxId,
                "유효한 랜덤박스 ID가 필요합니다."
        );

        /*
         * 동일한 랜덤박스가 동시에 두 번 개봉되지 않도록
         * 해당 랜덤박스 행을 잠근 상태로 조회한다.
         */
        UserRandomBoxVO randomBox =
                randomBoxMapper.selectRandomBoxForUpdate(
                        userRandomBoxId,
                        userId
                );

        if (randomBox == null) {
            throw new IllegalArgumentException(
                    "랜덤박스가 존재하지 않거나 현재 사용자의 랜덤박스가 아닙니다."
            );
        }

        String currentStatus =
                randomBox.getBoxStatus();

        if (RandomBoxStatus.OPENED.name()
                .equals(currentStatus)) {

            throw new IllegalStateException(
                    "이미 개봉된 랜덤박스입니다."
            );
        }

        if (RandomBoxStatus.REVOKED.name()
                .equals(currentStatus)) {

            throw new IllegalStateException(
                    "회수된 랜덤박스는 개봉할 수 없습니다."
            );
        }

        if (!RandomBoxStatus.UNOPENED.name()
                .equals(currentStatus)) {

            throw new IllegalStateException(
                    "개봉할 수 없는 랜덤박스 상태입니다."
            );
        }

        int rewardPoint =
                RandomBoxRewardPolicy.drawRewardPoint();

        /*
         * UPDATE의 box_status = 'UNOPENED' 조건으로
         * 이미 처리된 랜덤박스의 중복 개봉을 방지한다.
         */
        int updatedCount =
                randomBoxMapper.openRandomBox(
                        userRandomBoxId,
                        userId,
                        rewardPoint
                );

        if (updatedCount != 1) {
            throw new IllegalStateException(
                    "랜덤박스 개봉에 실패했거나 이미 처리된 랜덤박스입니다."
            );
        }

        /*
         * 랜덤박스 상태 변경과 포인트 적립은 하나의 트랜잭션이다.
         * 포인트 적립 실패 시 랜덤박스 OPENED 처리도 롤백된다.
         */
        PointWalletDTO updatedWallet =
                pointWalletService.earnPoints(
                        userId,
                        rewardPoint,
                        PointReasonType.RANDOM_BOX
                );

        UserRandomBoxVO openedRandomBox =
                randomBoxMapper.selectRandomBoxById(
                        userRandomBoxId,
                        userId
                );

        if (openedRandomBox == null) {
            throw new IllegalStateException(
                    "개봉된 랜덤박스를 조회하지 못했습니다."
            );
        }

        log.info(
                "랜덤박스 개봉 완료 userId={}, userRandomBoxId={}, rewardPoint={}, pointBalance={}",
                userId,
                userRandomBoxId,
                rewardPoint,
                updatedWallet.getPointBalance()
        );

        return openedRandomBox.toOpenResultDTO(
                updatedWallet.getPointBalance()
        );
    }

    @Override
    public int getUnopenedRandomBoxCount(
            Integer userId
    ) {
        validatePositiveId(
                userId,
                "유효한 사용자 ID가 필요합니다."
        );

        int unopenedCount =
                randomBoxMapper.countUnopenedRandomBoxes(
                        userId
                );

        log.info(
                "미개봉 랜덤박스 개수 조회 userId={}, count={}",
                userId,
                unopenedCount
        );

        return unopenedCount;
    }

    // 사용자의 미개봉 랜덤박스 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<UserRandomBoxDTO> getUnopenedRandomBoxes(
            Integer userId
    ) {
        validatePositiveId(
                userId,
                "유효한 사용자 ID가 필요합니다."
        );

        List<UserRandomBoxVO> unopenedRandomBoxes =
                randomBoxMapper.selectUnopenedRandomBoxes(
                        userId
                );

        List<UserRandomBoxDTO> result =
                unopenedRandomBoxes.stream()
                        .map(UserRandomBoxVO::toDTO)
                        .toList();

        log.info(
                "미개봉 랜덤박스 목록 조회 userId={}, count={}",
                userId,
                result.size()
        );

        return result;
    }

    // 사용자의 미개봉 랜덤박스 모두 개봉
    @Override
    @Transactional
    public RandomBoxOpenAllResultDTO openAllRandomBoxes(
            Integer userId
    ) {
        validatePositiveId(
                userId,
                "유효한 사용자 ID가 필요합니다."
        );

        /*
         * 모두 열기 처리 중 다른 요청이 같은 랜덤박스를
         * 개봉하거나 회수하지 못하도록 미개봉 행을 잠근다.
         */
        List<UserRandomBoxVO> unopenedRandomBoxes =
                randomBoxMapper.selectUnopenedRandomBoxesForUpdate(
                        userId
                );

        /*
         * 열 수 있는 박스가 없는 것은 잘못된 요청이 아니므로
         * 예외 대신 개봉 수 0인 정상 결과를 반환한다.
         */
        if (unopenedRandomBoxes.isEmpty()) {

            PointWalletDTO currentWallet =
                    pointWalletService.getWallet(userId);

            return RandomBoxOpenAllResultDTO.builder()
                    .openedCount(0)
                    .totalRewardPoint(0)
                    .pointBalance(currentWallet.getPointBalance())
                    .openedBoxes(List.of())
                    .build();
        }

        List<RandomBoxOpenResultDTO> openedResults =
                new ArrayList<>();

        int totalRewardPoint = 0;

        PointWalletDTO updatedWallet = null;

        for (UserRandomBoxVO unopenedRandomBox
                : unopenedRandomBoxes) {

            Integer userRandomBoxId =
                    unopenedRandomBox.getUserRandomBoxId();

            int rewardPoint =
                    RandomBoxRewardPolicy.drawRewardPoint();

            /*
             * 조회 시 행 잠금을 적용했지만 UPDATE에도
             * UNOPENED 조건을 적용해 중복 개봉을 한 번 더 방지한다.
             */
            int updatedCount =
                    randomBoxMapper.openRandomBox(
                            userRandomBoxId,
                            userId,
                            rewardPoint
                    );

            if (updatedCount != 1) {
                throw new IllegalStateException(
                        "랜덤박스 모두 열기 중 개봉 처리에 실패했습니다."
                );
            }

            /*
             * 박스별 포인트 적립 내역이 남도록
             * 랜덤박스마다 적립 거래를 생성한다.
             */
            updatedWallet =
                    pointWalletService.earnPoints(
                            userId,
                            rewardPoint,
                            PointReasonType.RANDOM_BOX
                    );

            UserRandomBoxVO openedRandomBox =
                    randomBoxMapper.selectRandomBoxById(
                            userRandomBoxId,
                            userId
                    );

            if (openedRandomBox == null) {
                throw new IllegalStateException(
                        "개봉된 랜덤박스를 조회하지 못했습니다."
                );
            }

            openedResults.add(
                    openedRandomBox.toOpenResultDTO(
                            updatedWallet.getPointBalance()
                    )
            );

            totalRewardPoint += rewardPoint;
        }

        log.info(
                "랜덤박스 모두 개봉 완료 userId={}, openedCount={}, totalRewardPoint={}, pointBalance={}",
                userId,
                openedResults.size(),
                totalRewardPoint,
                updatedWallet.getPointBalance()
        );

        return RandomBoxOpenAllResultDTO.builder()
                .openedCount(openedResults.size())
                .totalRewardPoint(totalRewardPoint)
                .pointBalance(updatedWallet.getPointBalance())
                .openedBoxes(openedResults)
                .build();
    }

}