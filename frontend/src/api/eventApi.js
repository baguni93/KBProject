import api from '@/api';

const BASE_URL = '/api/event';

export default {
  // 이벤트 메인화면 조회
  async getEventMain(userId) {
    const { data } = await api.get(`${BASE_URL}`, { params: { userId } });
    return data;
  },

  // 이벤트 리스트 조회
  async getEventList(userId) {
    //const { data } = await api.get(`${BASE_URL}/list`, { params: { userId } });
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

  // 참여완료 이벤트 리스트 조회
  async getJoinedEventList(userId, yearMonth) {
    const { data } = await api.get(`${BASE_URL}/list/joined`, {
      params: { userId, yearMonth },
    });
    return data;
  },

  // 이벤트 참여 처리
  async joinEvent(userId, eventId) {
    const { data } = await api.post(`${BASE_URL}/joinEvent/${eventId}`, null, {
      params: { userId },
    });
    return data;
  },

  //  출석체크 참여
  async joinAttendanceEvent(userId, eventId) {
    const { data } = await api.post(
      `${BASE_URL}/joinAttendanceEvent/${eventId}`,
      null,
      { params: { userId } },
    );
    return data;
  },

  // 이벤트 참여이력 생성
  async createParticipation(userId, eventId) {
    const { data } = await api.post(
      `${BASE_URL}/participateEvent/${eventId}`,
      null,
      {
        params: { userId },
      },
    );
    return data;
  },

  // 이벤트 리워드 수령 처리
  async receiveEventReward(userId, eventId, rewardId) {
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

  // 출석체크 리워드 수령 처리
  async receiveAttendanceEventReward(userId, eventId, rewardId) {
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

  // 챌린지 리워드 수령 처리
  async receiveChallengeReward(userId, challengeId) {
    const { data } = await api.post(
      `${BASE_URL}/challenge/claim/${challengeId}`,
      null,
      {
        params: {
          userId,
        },
      },
    );
    return data;
  },
};
