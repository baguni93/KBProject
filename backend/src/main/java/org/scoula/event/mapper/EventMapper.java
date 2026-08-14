package org.scoula.event.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.event.domain.*;
import org.scoula.event.dto.EventChallengeResponseDTO;
import org.scoula.event.dto.EventResponseDTO;

import java.util.List;

public interface EventMapper {
    // ===== 레벨 챌린지 =====

    // 현재 진행 중인 챌린지와 사용자의 레벨·누적 EXP 조회
    List<EventChallengeResponseDTO> getEventChallengeUser(@Param("userId") int userId);

    // 레벨 보상 중복 수령을 막기 위해 챌린지 참여 행을 잠금 조회
    EventChallengeUserVO getChallengeClaimForUpdate(
            @Param("userId") int userId,
            @Param("challengeId") int challengeId
    );

    // 활성 챌린지에 사용자 참여 데이터 최초 생성
    int createEventChallengeParticipation(@Param("userId") int userId);

    // 미션 보상으로 받은 EXP를 현재 챌린지에 누적
    int updateUserChallenge(@Param("userId") int userId, @Param("rewardId") int rewardId);

    // 현재 레벨 보상 수령 후 다음 레벨로 이동(EXP는 차감하지 않음)
    int updateUserLevel(@Param("userId") int userId, @Param("challengeId") int challengeId);

    // 최고 레벨(5레벨) 보상 수령 후 챌린지를 종료 상태로 변경
    int completeChallenge(@Param("userId") int userId, @Param("challengeId") int challengeId);

    // ===== 이벤트 목록 =====

    // 현재 참여 가능한 전체 이벤트와 진행 횟수 조회
    List<EventResponseDTO> getActiveEventProgressList(@Param("userId") Integer userId);

    // 선택한 월에 보상을 수령한 모든 이벤트 조회(출석 포함)
    List<EventResponseDTO> getJoinedEventProgressList(
            @Param("userId") Integer userId,
            @Param("yearMonth") String yearMonth
    );

    // 출석을 제외한 일반 미션 이벤트 상태 조회
    List<EventNormalVO> getEvent(@Param("userId") int userId);

    // 일반 미션 이벤트 참여 신청(중복 신청은 DB UNIQUE KEY로 방지)
    int joinEvent(@Param("userId") int userId , @Param("eventId") int eventId);

    // 이벤트 출석 전용 상태 조회(포인트 지갑 출석과 독립)
    List<EventAttendanceVO> getAttendanceEvent(@Param("userId") int userId);

    // 이벤트 출석 참여 신청
    int joinAttendanceEvent(@Param("userId")int userId, @Param("eventId") int eventId);

    // 동일 날짜 출석의 동시 요청을 직렬화하기 위한 참여 행 잠금
    Integer lockEventUser(
            @Param("userId") int userId,
            @Param("eventId") int eventId
    );

    // 오늘 이벤트 출석 이력 생성(하루 1회, 목표 횟수 이내)
    int createAttendanceParticipation(@Param("userId") int userId, @Param("eventId") int eventId);

    // ===== 미션 진척도·보상 =====

    // 실제 기능 성공 시 카테고리가 같은, 참여 중인 미션의 진행도를 1회 증가
    int recordMissionProgress(
            @Param("userId") int userId,
            @Param("eventCategory") String eventCategory
    );

    // 보상 수령 전 이벤트·보상 일치, 참여, 달성, 중복 수령 여부 검증용 조회
    EventRewardVO getRewardClaimInfo(
            @Param("userId") int userId,
            @Param("eventId") int eventId,
            @Param("rewardId") int rewardId
    );

    // 검증이 완료된 이벤트 보상 수령 내역 생성
    int createEventRewardReceive(
            @Param("userId") int userId,
            @Param("eventId") int eventId,
            @Param("rewardId") int rewardId
    );
}
