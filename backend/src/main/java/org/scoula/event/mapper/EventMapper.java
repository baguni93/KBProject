package org.scoula.event.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.event.domain.*;
import org.scoula.event.dto.EventGetResponseDTO;
import org.scoula.event.dto.EventResponseDTO;
import org.scoula.event.dto.UserChallengeDTO;

import java.util.List;

public interface EventMapper {
    // 1. 현재 보유 포인트
    Integer getUserPoint(@Param("userId") int userId);

    // 2. 현재 챌린지 참여현황 조회
    UserChallengeDTO getEventChallengeUser(@Param("userId") int userId);

    // 3. 현재 참여 가능 이벤트 목록 조회
    List<EventResponseDTO> getActiveEventProgressList(@Param("userId") Integer userId);

    // 4. 참여 완료 이벤트 목록 조회
    List<EventResponseDTO> getJoinedEventProgressList(
            @Param("userId") Integer userId,
            @Param("yearMonth") String yearMonth
    );

    // 7. 챌린지 리워드 수령 내역 생성
    int createChallengeRewardReceive(@Param("userId") Integer userId, @Param("challengeId") Integer challengeId, @Param("rewardPoint") Integer rewardPoint);

    // 포인트 수령 반영 처리
    int updateUserPoint(@Param("userId") Integer userId);
    // 포인트(리워드) 수령 내역 생성
    int createUserPointTransaction(@Param("userId") Integer userId, @Param("point") Integer point);

    int updateUserChallengeTarget(@Param("userId") Integer userId);

    int getParticipationCount(@Param("eventId") Integer eventId, @Param("userId") Integer userId);

    int getTodayParticipationCount(@Param("eventId") Integer eventId, @Param("userId") Integer userId, @Param("eventType") String eventType);

    //boolean checkRewardAlreadyReceived(@Param("eventId") Integer eventId, @Param("userId") Integer userId, @Param("rewardId") Integer rewardId);

    List<EventNormalVO> getEvent(int userId);

    void joinEvent(@Param("userId") int userId , @Param("eventId") int eventId);

    List<EventAttendanceVO> getAttendanceEvent(int userId);

    void joinAttendanceEvent(@Param("userId")int userId, @Param("eventId") int eventId);

    void createParticipation(@Param("userId") int userId, @Param("eventId") int eventId);

    // 출석체크 이벤트 참여 처리
    int createAttendanceParticipation(@Param("userId") int userId, @Param("eventId") int eventId);

    // 이벤트 리워드 수령 내역 생성
    int createEventRewardReceive(@Param("userId") int userId, @Param("eventId") int eventId, @Param("rewardId") int rewardId);


}
