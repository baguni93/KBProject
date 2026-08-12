import api from '@/api';

const SPENDING_ANALYSIS_URL = '/api/spending-analyses';

const mergeTransactionMetadata = (transaction, metadata) => {
  if (!transaction || !metadata) return transaction;

  return {
    ...transaction,
    transactionType:
      transaction.transactionType ?? metadata.transactionType ?? null,
    receiverName: transaction.receiverName ?? metadata.receiverName ?? null,
    memo: transaction.memo ?? metadata.memo ?? null,
    settlementTitle:
      transaction.settlementTitle ?? metadata.settlementTitle ?? null,
  };
};

const enrichTransactionList = async (payload) => {
  const transactions = payload?.transactions ?? [];
  if (!transactions.length) return payload;

  try {
    const { data: metadataList } = await api.get('/api/transactions');
    const metadataMap = new Map(
      (metadataList ?? []).map((item) => [Number(item.transactionId), item]),
    );

    return {
      ...payload,
      transactions: transactions.map((transaction) =>
        mergeTransactionMetadata(
          transaction,
          metadataMap.get(Number(transaction.transactionId)),
        ),
      ),
    };
  } catch (error) {
    // 거래 표시용 부가정보 조회 실패가 소비분석 자체 실패로 이어지지 않게 한다.
    return payload;
  }
};

const enrichTransaction = async (transaction) => {
  if (!transaction?.transactionId) return transaction;

  try {
    const { data: metadata } = await api.get(
      `/api/transactions/${transaction.transactionId}`,
    );
    return mergeTransactionMetadata(transaction, metadata);
  } catch (error) {
    return transaction;
  }
};

export default {
  async getAvailability(period = 1) {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/availability`, {
      params: { period },
    });
    return data;
  },

  async getTransactions(period = 1) {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/transactions`, {
      params: { period },
    });
    return enrichTransactionList(data);
  },

  async getAllTransactions() {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/transactions/all`);
    return enrichTransactionList(data);
  },

  async getTransaction(transactionId) {
    const { data } = await api.get(
      `${SPENDING_ANALYSIS_URL}/transactions/${transactionId}`,
    );
    return enrichTransaction(data);
  },

  async getAnalysisResultTransactions(spendingAnalysisId) {
    const { data } = await api.get(
      `${SPENDING_ANALYSIS_URL}/${spendingAnalysisId}/transactions`,
    );
    return enrichTransactionList(data);
  },

  async getUnclassifiedTransactions(period = 1) {
    const { data } = await api.get(
      `${SPENDING_ANALYSIS_URL}/unclassified-transactions`,
      { params: { period } },
    );
    return enrichTransactionList(data);
  },

  async getCategories() {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/categories`);
    return data;
  },

  async classifyTransaction(transactionId, spendingCategoryId) {
    const { data } = await api.patch(
      `${SPENDING_ANALYSIS_URL}/transactions/${transactionId}/category`,
      { spendingCategoryId },
    );
    return data;
  },

  async executeAnalysis(period) {
    // 소비 분석은 OpenAI 문구 생성까지 포함하므로
    // 공통 Axios 제한(10초)보다 오래 걸릴 수 있습니다.
    const { data } = await api.post(
      SPENDING_ANALYSIS_URL,
      { period },
      { timeout: 120000 },
    );
    return data;
  },

  async startAsyncAnalysis(period) {
    const { data } = await api.post(
      `${SPENDING_ANALYSIS_URL}/async`,
      { period },
    );
    return data;
  },

  async getAnalysisStatus(period = 1) {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/status`, {
      params: { period },
    });
    return data;
  },

  async getAnalysisDetail(spendingAnalysisId) {
    const { data } = await api.get(
      `${SPENDING_ANALYSIS_URL}/${spendingAnalysisId}`,
    );
    return data;
  },

  async getLatestAnalysisDetail(period = 1) {
    const { data } = await api.get(`${SPENDING_ANALYSIS_URL}/latest`, {
      params: { period },
    });
    return data;
  },
};
