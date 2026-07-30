import api from '@/api';

const POINT_WALLET_URL = '/api/point-wallet';
const POINT_TRANSACTION_URL = '/api/point-transactions';
const ATTENDANCE_URL = '/api/attendance';
const POINT_CONVERSION_URL = '/api/point-conversions';
const RANDOM_BOX_URL = '/api/random-boxes';

export default {
    async getWallet() {
        const { data } = await api.get(POINT_WALLET_URL);
        return data;
    },

    async getTransactions(transactionType = '') {
        const params = transactionType ? { type: transactionType } : {};
        const { data } = await api.get(POINT_TRANSACTION_URL, { params });
        return data;
    },

    async getRecentTransactions() {
        const { data } = await api.get(`${POINT_TRANSACTION_URL}/recent`);
        return data;
    },

    async getTodayAttendanceStatus() {
        const { data } = await api.get(`${ATTENDANCE_URL}/today`);
        return data;
    },

    async attend() {
        const { data } = await api.post(ATTENDANCE_URL);
        return data;
    },

    async convertPoints(pointAmount) {
        const { data } = await api.post(POINT_CONVERSION_URL, { pointAmount });
        return data;
    },

    async getUnopenedRandomBoxes() {
        const { data } = await api.get(`${RANDOM_BOX_URL}/unopened`);
        return data;
    },

    async getUnopenedRandomBoxCount() {
        const { data } = await api.get(`${RANDOM_BOX_URL}/unopened/count`);
        return data;
    },

    async openRandomBox(userRandomBoxId) {
        const { data } = await api.post(
            `${RANDOM_BOX_URL}/${userRandomBoxId}/open`,
        );
        return data;
    },

    async openAllRandomBoxes() {
        const { data } = await api.post(`${RANDOM_BOX_URL}/open-all`);
        return data;
    },
};
