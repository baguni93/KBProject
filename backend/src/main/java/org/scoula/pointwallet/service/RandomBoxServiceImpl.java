package org.scoula.pointwallet.service;

import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.event.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.common.PointReasonType;
import org.scoula.pointwallet.common.RandomBoxIssueReason;
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
    private final EventService eventService;

    @Override
    @Transactional
    public RandomBoxIssueResultDTO issueForAttendance(
            Integer userId,
            Integer attendanceId
    ) {
        return issueRandomBox(
                userId,
                RandomBoxIssueReason.ATTENDANCE,
                attendanceId,
                null
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
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        /*
         * 최소 금액 미만 결제도 정상 결제이므로 오류로 처리하지 않는다.
         * 랜덤박스만 지급하지 않고 issued=false를 반환한다.
         */
        if (paymentAmount < RandomBoxIssuePolicy.PAYMENT_MINIMUM_AMOUNT) {
            log.info(
                    "결제 랜덤박스 미지급 userId={}, transactionId={}, amount={}, minimumAmount={}",
                    userId,
                    transactionId,
                    paymentAmount,
                    RandomBoxIssuePolicy.PAYMENT_MINIMUM_AMOUNT
            );

            return RandomBoxIssueResultDTO.builder()
                    .issued(false)
                    .message(
                            RandomBoxIssuePolicy.PAYMENT_MINIMUM_AMOUNT
                                    + "원 미만 결제는 랜덤박스 지급 대상이 아닙니다."
                    )
                    .userRandomBoxId(null)
                    .issueReason(RandomBoxIssueReason.PAYMENT.name())
                    .boxStatus(null)
                    .issuedAt(null)
                    .build();
        }

        // 결제 거래 ID를 source_id로 사용해 같은 결제의 중복 지급을 방지한다.
        return issueRandomBox(
                userId,
                RandomBoxIssueReason.PAYMENT,
                transactionId,
                null
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
                feedId,
                null
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
                participationId,
                null
        );
    }


    private RandomBoxIssueResultDTO issueRandomBox(
            Integer userId,
            RandomBoxIssueReason issueReason,
            Integer sourceId,
            Integer targetAccountId
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

        // 이미 해당 조건으로, 랜덤박스가 지급되었습니다.
        if (duplicateCount > 0) {
            throw new CustomException(
                    ErrorCode.RANDOM_BOX_ALREADY_ISSUED
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
                        .targetAccountId(targetAccountId)
                        .boxStatus(RandomBoxStatus.UNOPENED.name())
                        .rewardPoint(null)
                        .openedAt(null)
                        .build();

        try {
            int insertedCount =
                    randomBoxMapper.insertRandomBox(randomBoxVO);

            // 랜덤박스 삽입 실패.
            if (insertedCount != 1) {
                log.error(
                        "랜덤박스 발급 실패 userId={}, issueReason={}, sourceId={}, insertedCount={}",
                        userId,
                        issueReason,
                        sourceId,
                        insertedCount
                );

                throw new CustomException(
                        ErrorCode.POINT_WALLET_PROCESS_ERROR
                );
            }
            // 유니크 키 에러가 일어나면,
        } catch (DuplicateKeyException exception) {

            if (issueReason == RandomBoxIssueReason.TRANSFER) {
                return RandomBoxIssueResultDTO.builder()
                        .issued(false)
                        .message("이미 랜덤박스를 지급받은 수취 계좌입니다.")
                        .userRandomBoxId(null)
                        .issueReason(RandomBoxIssueReason.TRANSFER.name())
                        .boxStatus(null)
                        .issuedAt(null)
                        .build();
            }

            throw new CustomException(
                    ErrorCode.RANDOM_BOX_ALREADY_ISSUED
            );
        }

        // 생성된 랜덤박스 ID 확인에 실패한 경우
        if (randomBoxVO.getUserRandomBoxId() == null) {
            log.error(
                    "생성된 랜덤박스 ID 확인 실패 userId={}, issueReason={}, sourceId={}",
                    userId,
                    issueReason,
                    sourceId
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
            );
        }

        UserRandomBoxVO createdRandomBox =
                randomBoxMapper.selectRandomBoxById(
                        randomBoxVO.getUserRandomBoxId(),
                        userId
                );

        if (createdRandomBox == null) {
            log.error(
                    "발급된 랜덤박스 조회 실패 userId={}, userRandomBoxId={}",
                    userId,
                    randomBoxVO.getUserRandomBoxId()
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
            );
        }

        return createdRandomBox.toIssueResultDTO();
    }

    private void validateDailyLimit(
            Integer userId,
            RandomBoxIssueReason issueReason
    ) {
        if (issueReason != RandomBoxIssueReason.FEED_SHARE) {
            return;
        }

        int todayIssuedCount =
                randomBoxMapper.countTodayIssuedByReason(
                        userId,
                        RandomBoxIssueReason.FEED_SHARE.name()
                );

        // 피드 송신이 10개이상 인 경우...
        if (todayIssuedCount
                >= RandomBoxIssuePolicy.FEED_SHARE_DAILY_LIMIT) {

            throw new CustomException(
                    ErrorCode.RANDOM_BOX_DAILY_LIMIT_EXCEEDED
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

        // 지급 사유를 읽어오지 못했을 때
        if (issueReason == null) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
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
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
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

        // 랜덤박스가 존재하지 않거나, 현재 사용자의 랜덤박스가 아니다.
        if (randomBox == null) {
            throw new CustomException(
                    ErrorCode.RANDOM_BOX_NOT_FOUND
            );
        }

        String currentStatus =
                randomBox.getBoxStatus();

        // 이미 랜덤박스가 개봉된 상태인 경우
        if (RandomBoxStatus.OPENED.name()
                .equals(currentStatus)) {

            throw new CustomException(
                    ErrorCode.RANDOM_BOX_ALREADY_OPENED
            );
        }


        // 랜덤박스가 개봉 불가인 상태인 경우
        if (!RandomBoxStatus.UNOPENED.name()
                .equals(currentStatus)) {

            log.error(
                    "개봉할 수 없는 랜덤박스 상태 userId={}, userRandomBoxId={}, status={}",
                    userId,
                    userRandomBoxId,
                    currentStatus
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
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

        // 랜덤박스 개봉에 실패했거나, 이미 다른 요청에서 처리된 랜덤박스인 경우
        if (updatedCount != 1) {
            throw new CustomException(
                    ErrorCode.RANDOM_BOX_ALREADY_PROCESSED
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
            log.error(
                    "개봉된 랜덤박스 조회 실패 userId={}, userRandomBoxId={}",
                    userId,
                    userRandomBoxId
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
            );
        }

        eventService.recordMissionProgress(userId, "RANDOMBOX");

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
    @Transactional(readOnly = true)
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
         * 동시에 개봉하지 못하도록 row lock을 설정다.
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
                throw new CustomException(
                        ErrorCode.RANDOM_BOX_ALREADY_PROCESSED
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
                log.error(
                        "모두 열기 중 개봉 결과 조회 실패 userId={}, userRandomBoxId={}",
                        userId,
                        userRandomBoxId
                );

                throw new CustomException(
                        ErrorCode.POINT_WALLET_PROCESS_ERROR
                );
            }

            openedResults.add(
                    openedRandomBox.toOpenResultDTO(
                            updatedWallet.getPointBalance()
                    )
            );

            totalRewardPoint += rewardPoint;
        }
        // 랜덤박스 열기 성공 후 이벤트 진행 기록
        if (!openedResults.isEmpty()) {
            eventService.recordMissionProgress(userId, "RANDOMBOX");
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

    // 송금시 랜덤박스 지급
    @Override
    @Transactional
    public RandomBoxIssueResultDTO issueForTransfer(
            Integer userId,
            Integer transactionId,
            Integer targetAccountId
    ) {
        try {
            validatePositiveId(
                    userId,
                    "유효한 사용자 ID가 필요합니다."
            );

            validatePositiveId(
                    transactionId,
                    "유효한 송금 거래 ID가 필요합니다."
            );

            validatePositiveId(
                    targetAccountId,
                    "유효한 수취 계좌 ID가 필요합니다."
            );

            int issuedCount =
                    randomBoxMapper.countTransferRandomBoxByAccount(
                            userId,
                            targetAccountId
                    );

            if (issuedCount > 0) {
                return RandomBoxIssueResultDTO.builder()
                        .issued(false)
                        .message("이미 랜덤박스를 지급받은 수취 계좌입니다.")
                        .userRandomBoxId(null)
                        .issueReason(RandomBoxIssueReason.TRANSFER.name())
                        .boxStatus(null)
                        .issuedAt(null)
                        .build();
            }

            return issueRandomBox(
                    userId,
                    RandomBoxIssueReason.TRANSFER,
                    transactionId,
                    targetAccountId
            );
        } catch (Exception e) {
            log.warn("송금 성공 후 랜덤박스 발급 처리 중 예외 (송금 본래 거래는 유지됨): {}", e.getMessage());
            return RandomBoxIssueResultDTO.builder()
                    .issued(false)
                    .message("랜덤박스 발급 패스: " + e.getMessage())
                    .issueReason(RandomBoxIssueReason.TRANSFER.name())
                    .build();
        }
    }
}