import api from '@/api';

const BASE_URL = '/api/event';

export default {
  // EVT-001 : 이벤트 메인화면 조회
  async getEventMain(userId) {
    const { data } = await api.get(`${BASE_URL}/main`, { params: { userId } });
    return data;
  },

  // EVT-002 : 이벤트 리스트 조회
  async getEventList(userId) {
    // const { data } = await api.get(`${BASE_URL}/list`, { params: { userId } });
    const { data } = await api.get(`${BASE_URL}/eventList`, {
      params: { userId },
    });
    return data;
  },

  // 출석 이벤트 조회
  async getAttendanceEventList(userId) {
    const { data } = await api.get(`${BASE_URL}/attendanceEventList`, {
      params: { userId },
    });
    return data;
  },

  // EVT-003 : 참여완료 이벤트 리스트 조회
  async getJoinedEventList(userId, yearMonth) {
    const { data } = await api.get(`${BASE_URL}/list/joined`, {
      params: { userId, yearMonth },
    });
    return data;
  },

  // 이벤트 참여 처리
  async joinEvent(eventId, userId) {
    const { data } = await api.post(`${BASE_URL}/joinEvent/${eventId}`, null, {
      params: { userId },
    });
    return data;
  },

  //  출석체크 참여
  async joinAttendanceEvent(eventId, userId) {
    const { data } = await api.post(
      `${BASE_URL}/joinAttendanceEvent/${eventId}`,
      null,
      { params: { userId } },
    );
    return data;
  },
  // 이벤트 참여 + 리워드 수령 처리
  async receiveEventReward(eventId, userId, rewardId) {
    const { data } = await api.post(
      `${BASE_URL}/receiveEventReward/${eventId}/rewards/${rewardId}`,
      null,
      {
        params: {
          userId,
        },
      },
    );
    return data;
  },

  // 출석체크 이벤트 참여 +  리워드 수령 처리
  async receiveAttendanceEventReward(eventId, userId, rewardId) {
    const { data } = await api.post(
      `${BASE_URL}/receiveAttendanceEventReward/${eventId}/rewards/${rewardId}`,
      null,
      {
        params: {
          userId,
        },
      },
    );
    return data;
  },

  // EVT-006 : 챌린지 리워드 수령 처리
  async receiveChallengeReward(challengeId, userId) {
    const { data } = await api.post(
      `${BASE_URL}/challenges/${challengeId}/reward`,
      {
        userId: userId,
      },
    );
    return data;
  },
};
