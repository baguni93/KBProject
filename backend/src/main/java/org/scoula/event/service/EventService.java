package org.scoula.event.service;

import org.scoula.event.dto.EventGetAttendanceResponseDTO;
import org.scoula.event.dto.EventGetResponseDTO;
import org.scoula.event.dto.EventMainDTO;
import org.scoula.event.dto.EventResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EventService {

    // 1. 이벤트 메인 페이지
    EventMainDTO getEventMainPageData(int userId);

    // 2. 참여가능 이벤트 리스트 전체 조회(진행중 탭)
    List<EventResponseDTO> getActiveEventsProgress(Integer userId);

    // 3. 참여확인 탭 리스트 조회(참여완료 탭)
    List<EventResponseDTO> getJoinedEventsProgress(Integer userId, String yearMonth);

    // 4. 일반 이벤트 참여 처리
//    boolean participateEvent(Integer userId,  Integer eventId);
//
//    // 5. 이벤트 리워드 수령 처리
//    boolean receiveEventReward(Integer userId,  Integer eventId);

    // 6. 챌린지 참여 처리 및 리워드 수령 처리
    boolean claimChallengeReward(Integer userId,  Integer eventId);

    List<EventGetAttendanceResponseDTO> joinAttendanceEvent(int userId, int eventId);
    List<EventGetResponseDTO> joinEvent(int userId, int eventId);
    List<EventGetAttendanceResponseDTO> getAttendanceEventList(int userId);
    List<EventGetResponseDTO> getEventList(int userId);

    List<EventGetResponseDTO> receiveEventReward(int userId, int eventId, int rewardId);

    List<EventGetAttendanceResponseDTO> receiveAttendanceEventReward(int userId, int eventId, int rewardId);

    List<EventGetResponseDTO> createParticipation(int userId, int eventId);
}
