package org.scoula.event.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.event.domain.*;
import org.scoula.event.dto.EventChallengeDTO;
import org.scoula.event.dto.EventChallengeResponseDTO;
import org.scoula.event.dto.EventResponseDTO;

import java.util.List;

public interface EventMapper {
    // 현재 보유 포인트
    Integer getUserPoint(@Param("userId") int userId);

    // 이벤트 챌린지 조회
    List<EventChallengeDTO> getEventChallengeList();
    // 사용자 이벤트 챌린지 현황 조회
    List<EventChallengeResponseDTO> getEventChallengeUser(@Param("userId") int userId);

    // 3. 현재 참여 가능 이벤트 목록 조회
    List<EventResponseDTO> getActiveEventProgressList(@Param("userId") Integer userId);

    // 4. 참여 완료 이벤트 목록 조회
    List<EventResponseDTO> getJoinedEventProgressList(
            @Param("userId") Integer userId,
            @Param("yearMonth") String yearMonth
    );

    int getParticipationCount(@Param("eventId") Integer eventId, @Param("userId") Integer userId);

    int getTodayParticipationCount(@Param("eventId") Integer eventId, @Param("userId") Integer userId, @Param("eventType") String eventType);

    List<EventNormalVO> getEvent(int userId);

    void joinEvent(@Param("userId") int userId , @Param("eventId") int eventId);

    List<EventAttendanceVO> getAttendanceEvent(int userId);

    void joinAttendanceEvent(@Param("userId")int userId, @Param("eventId") int eventId);

    // 이벤트 참여 처리
    void createParticipation(@Param("userId") int userId, @Param("eventId") int eventId);
    // 출석체크 이벤트 참여 처리
    int createAttendanceParticipation(@Param("userId") int userId, @Param("eventId") int eventId);

    // 이벤트 리워드 수령 내역 생성
    int createEventRewardReceive(@Param("userId") int userId, @Param("eventId") int eventId, @Param("rewardId") int rewardId);

    // 포인트 수령 반영 처리
    int updateUserPoint(@Param("userId") int userId, @Param("rewardId") int rewardId);
    // 포인트(리워드) 수령 내역 생성
    int createUserPointTransaction(@Param("userId") int userId, @Param("rewardId") int rewardId);

    // 이벤트 챌린지 사용자 데이터 생성
    int createEventChallengeParticipation(@Param("userId") int userId);
    // 이벤트 챌린지 경험치 반영
    int updateUserChallenge(@Param("userId") int userId, @Param("rewardId") int rewardId);
    // 챌린지 리워드 수령 처리
    int receiveChallengeReward(@Param("userId") int userId, @Param("challengeId") int challengeId);

    int updateUserLevel(@Param("userId") int userId, @Param("challengeId") int challengeId);

    int updateEventChallengeUserPoint(@Param("userId") int userId, @Param("challengeId") int challengeId);

    void createEventChallengeUserPointTransaction(@Param("userId") int userId, @Param("challengeId") int challengeId);

    int checkFeedExists(@Param("userId") int userId, @Param("eventId") int eventId);

    int checkCustomCardExists(@Param("userId") int userId);

    int insertFeed(@Param("userId") int userId, @Param("eventId") int eventId, @Param("content") String content, @Param("visibility") String visibility);
}
