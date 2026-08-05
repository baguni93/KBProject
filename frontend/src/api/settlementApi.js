import api from '@/api';

const BASE_URL = '/api/settlements';

export default {
  async get(params) {
    const { data } = await api.get(`${BASE_URL}/${params.settlementId}`, {
      params,
    });

    console.log('GET My Settlements:', data);

    return data;
  },

  async getMyList(params) {
    console.log(`${BASE_URL}/me`, { params });

    const { data } = await api.get(`${BASE_URL}/me`, {
      params,
    });

    console.log('GET My Settlements LIST:', data);

    return data;
  },

  async payment(params) {
    const { data } = await api.patch(
      `${BASE_URL}/${params.settlementId}/payment`,
      null,
      {
        params: {
          userId: params.userId,
        },
      },
    );

    console.log('PAYMENT RESPONSE:', data);

    return data;
  },

  async cancel(params) {
    const { data } = await api.patch(
      `${BASE_URL}/${params.settlementId}/cancel`,
      null,
      {
        params: {
          userId: params.userId,
        },
      },
    );

    console.log('CANCEL RESPONSE:', data);

    return data;
  },

  async remine(params) {
    const { data } = await api.patch(
      `${BASE_URL}/${params.settlementId}/remine`,
      null,
      {
        params: {
          userId: params.userId,
        },
      },
    );

    console.log('REMINE RESPONSE:', data);

    return data;
  },
};
