package org.scoula.event.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.event.domain.*;
import org.scoula.event.dto.EventResponseDTO;
import org.scoula.event.dto.UserChallengeDTO;

import java.util.List;

public interface EventMapper {
    // 1. 현재 보유 포인트
    Integer getUserPoint(@Param("userId") Integer userId);

    // 2. 현재 챌린지 참여현황 조회
    UserChallengeDTO getUserChallengeStatus(@Param("userId") Integer userId);

    // 3. 현재 참여 가능 이벤트 목록 조회
    List<EventResponseDTO> getActiveEventProgressList(@Param("userId") Integer userId);

    // 4. 참여 완료 이벤트 목록 조회
    List<EventResponseDTO> getJoinedEventProgressList(
            @Param("userId") Integer userId,
            @Param("yearMonth") String yearMonth
    );

    // 5. 이벤트 참여 처리
    int createParticipation(EventParticipationVO vo);

    // 6. 이벤트 리워드 수령 내역 생성
    int createEventRewardReceive(EventRewardReceiveVO rewardReceive);

    // 7. 챌린지 리워드 수령 내역 생성
    int createChallengeRewardReceive(@Param("userId") Integer userId, @Param("challengeId") Integer challengeId, @Param("rewardPoint") Integer rewardPoint);

    // 포인트(리워드) 수령 처리
    int updateUserPoint(@Param("userId") Integer userId, @Param("point") Integer point);
    //Integer getUserCurrentLevel(Integer userId);

    int updateUserChallengeTarget(@Param("userId") Integer userId);

    EventResponseDTO getEventRewardInfoByEventId(Integer eventId);


    int getParticipationCount(@Param("eventId") Integer eventId, @Param("userId") Integer userId);

    // 48번째 줄 수정
    int getTodayParticipationCount(@Param("eventId") Integer eventId, @Param("userId") Integer userId, @Param("eventType") String eventType);

    // 50번째 줄 수정
    int createAttendanceParticipation(@Param("eventId") Integer eventId, @Param("userId") Integer userId);

    // 52번째 줄 수정
    boolean checkRewardAlreadyReceived(@Param("eventId") Integer eventId, @Param("userId") Integer userId, @Param("rewardId") Integer rewardId);

    List<EventNormalVO> getEvent(int userId);

    void joinEvent(@Param("userId") int userId , @Param("eventId") int eventId);

    List<EventAttendanceVO> getAttendanceEvent(int userId);

    void joinAttendanceEvent(@Param("userId")int userId, @Param("eventId") int eventId);
}
