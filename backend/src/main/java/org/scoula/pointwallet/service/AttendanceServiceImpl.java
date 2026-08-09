package org.scoula.pointwallet.service;

import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.pointwallet.common.PointReasonType;
import org.scoula.pointwallet.domain.AttendanceVO;
import org.scoula.pointwallet.dto.*;
import org.scoula.pointwallet.mapper.AttendanceMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.scoula.pointwallet.dto.AttendanceStatusDTO;

import java.time.LocalDate;

@Log4j2
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    /* 1. 포인트 지급 액
     * TODO-POLICY:
     * 매일 50P 말고, 이벤트 적립코드는 또 따로 구현해줘야한다.
     */
    private static final int ATTENDANCE_REWARD_POINT = 50;

    /*
     * TODO-POLICY:
     * 출석 시 지급되는 랜덤박스 수 설정
     */
    private static final int ATTENDANCE_RANDOM_BOX_COUNT = 1;

    // 2. 출석 mapper
    private final AttendanceMapper attendanceMapper;
    // 3. 포인트 지갑 service
    private final PointWalletService pointWalletService;
    // 4. 랜덤박스 service
    private final RandomBoxService randomBoxService;

    @Override
    @Transactional
    public AttendanceResultDTO attend(Integer userId) {

        validateAttendanceRequest(userId);

        // 사용자의 포인트 지갑 존재 여부 확인
        pointWalletService.getWallet(userId);

        // 오늘 출석 여부 확인
        int todayAttendanceCount =
                attendanceMapper.countTodayAttendance(
                        userId
                );

        // 이미 오늘 출석을 완료한 경우.
        if (todayAttendanceCount > 0) {
            throw new CustomException(
                    ErrorCode.ALREADY_ATTENDED
            );
        }

        /*
         * DB 저장 객체이므로 AttendanceVO를 사용한다.
         */
        AttendanceVO attendanceVO =
                AttendanceVO.builder()
                        .userId(userId)
                        .build();

        int insertedCount;

        try {
            insertedCount =
                    attendanceMapper.insertTodayAttendance(
                            attendanceVO
                    );
        } catch (DuplicateKeyException exception) {
            // 이미 출석을 완료한 경우
            throw new CustomException(
                    ErrorCode.ALREADY_ATTENDED
            );
        }

        if (insertedCount != 1) {
            log.error(
                    "출석 내역 저장 실패 userId={}, insertedCount={}",
                    userId,
                    insertedCount
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
            );
        }

        if (attendanceVO.getAttendanceId() == null) {
            log.error(
                    "출석 ID 가져오기 실패 userId={}",
                    userId
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
            );
        }

        /*
         * 기존 포인트 지갑 기능은 아직 DTO 구조이므로
         * 현재 구현을 그대로 사용한다.
         *
         * 모든 기능 완성 후 VO와 DTO로 분리할 예정이다.
         */
        PointWalletDTO updatedWallet =
                pointWalletService.earnPoints(
                        userId,
                        ATTENDANCE_REWARD_POINT,
                        PointReasonType.ATTENDANCE
                );

        if (updatedWallet == null) {
            log.error(
                    "출석 포인트 지급 결과 조회 실패 userId={}, attendanceId={}",
                    userId,
                    attendanceVO.getAttendanceId()
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
            );
        }

        /*
         * 랜덤박스 Service는 VO로 DB 저장 후
         * 발급 결과 DTO를 반환한다.
         */
        RandomBoxIssueResultDTO randomBoxResult =
                randomBoxService.issueForAttendance(
                        userId,
                        attendanceVO.getAttendanceId()
                );


        /*
         * 출석 보상에서는 반드시 랜덤박스가 1개 지급되어야 한다.
         */
        if (randomBoxResult == null
                || !randomBoxResult.isIssued()
                || randomBoxResult.getUserRandomBoxId() == null) {

            log.error(
                    "출석 랜덤박스 지급 결과 오류 userId={}, attendanceId={}, result={}",
                    userId,
                    attendanceVO.getAttendanceId(),
                    randomBoxResult
            );

            throw new CustomException(
                    ErrorCode.POINT_WALLET_PROCESS_ERROR
            );
        }

        log.info(
                "출석 완료 userId={}, attendanceId={}, rewardPoint={}, randomBoxId={}",
                userId,
                attendanceVO.getAttendanceId(),
                ATTENDANCE_REWARD_POINT,
                randomBoxResult.getUserRandomBoxId()
        );

        /*
         * 프론트 응답이므로 AttendanceResultDTO를 반환한다.
         */
        return AttendanceResultDTO.builder()
                .userId(userId)
                .attendanceDate(
                        LocalDate.now().toString()
                )
                .rewardPoint(
                        ATTENDANCE_REWARD_POINT
                )
                .pointBalance(
                        updatedWallet.getPointBalance()
                )
                .randomBoxIssued(true)
                .userRandomBoxId(
                        randomBoxResult.getUserRandomBoxId()
                )
                .message(
                        "출석이 완료되어 50포인트와 랜덤박스 1개가 지급되었습니다."
                )
                .build();
    }


    private void validateAttendanceRequest(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new CustomException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceStatusDTO getTodayAttendanceStatus(
            Integer userId
    ) {
        validateAttendanceRequest(userId);

        boolean attendedToday =
                attendanceMapper.countTodayAttendance(userId) > 0;

        return AttendanceStatusDTO.builder()
                .attendedToday(attendedToday)
                .attendanceDate(LocalDate.now().toString())
                .rewardPoint(ATTENDANCE_REWARD_POINT)
                .randomBoxCount(ATTENDANCE_RANDOM_BOX_COUNT)
                .message(
                        attendedToday
                                ? "오늘 출석체크를 완료했습니다."
                                : "오늘 출석체크가 가능합니다."
                )
                .build();
    }
}