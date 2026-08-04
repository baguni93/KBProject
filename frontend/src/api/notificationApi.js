import api from '@/api';
const BASE_URL = '/api/notifications';

export default {
  async getList(params) {
    console.log(`${BASE_URL}`, { params });

    const { data } = await api.get(`${BASE_URL}`, {
      params: {
        userId: params.userId,
      },
    });

    console.log('Get Notification List :', data);

    return data;
  },

  async read(params) {
    const { data } = await api.patch(
      `${BASE_URL}/${params.notificationId}/read`,
      null,
    );

    console.log('Read Notification:', data);

    return data;
  },

  async readAll(params) {
    const { data } = await api.patch(`${BASE_URL}/read-all`, null, {
      params: {
        userId: params.userId,
      },
    });

    console.log('Read All Notification:', data);

    return data;
  },
};
