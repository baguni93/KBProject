import api from '@/api';

const BASE_URL = '/api/event';

export default {
  // EVT-001 : 이벤트 메인화면 조회
  async getEventMainPage(userId) {
    const { data } = await api.get(`${BASE_URL}/main`, { params: { userId } });
    return data;
  },

  // EVT-002 : 진행 중 이벤트 리스트 조회
  async getActiveEventList(userId) {
    const { data } = await api.get(`${BASE_URL}/list`, { params: { userId } });
    return data;
  },

  // EVT-003 : 참여완료 이벤트 리스트 조회
  async getJoinedEventList(userId, yearMonth) {
    const { data } = await api.get(`${BASE_URL}/list/joined`, {
      params: { userId, yearMonth },
    });
    return data;
  },

  // EVT-004 : 이벤트 참여 처리
  async joinEvent(eventId, userId) {
    const { data } = await api.post(`${BASE_URL}/join/${eventId}`, {
      userId: userId,
    });
    return data;
  },

  // EVT-005 : 이벤트 리워드 수령 처리
  async receiveEventReward(eventId, userId) {
    const { data } = await api.post(`${BASE_URL}/${eventId}/reward`, {
      userId: userId,
    });
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
