import api from './index';

// 은행 목록 조회
export const getBanks = async () => {
    const { data } = await api.get('/api/banks');
    return data;
};