<template>
  <div class="kb-mobile-page analysis-page">
    <PageHeader
        title="최근 소비 분석 결과"
        :showBack="true"
        :customBack="true"
        @back="goToFinance"
    />

    <div class="analysis-content-start">

      <div :class="['analysis-tabs', { 'is-loading': pageLoading }]">
        <CommonTabBar
            :tabs="ANALYSIS_PERIODS"
            :model-value="selectedPeriod"
            @update:model-value="changePeriod"
        />
      </div>
    </div>

    <p
        v-if="message"
        :class="['analysis-message', 'text-13', messageType]"
        role="status"
    >
      {{ message }}
    </p>

    <div v-if="pageLoading" class="kb-card kb-loading content-loading">
      <div class="spinner-border kb-spinner" role="status"></div>
      <div class="text-13">{{ selectedPeriod }}개월 최근 분석 결과를 불러오는 중이에요.</div>
    </div>

    <template v-else-if="latestAnalysis">
      <section class="title-card kb-card">
        <div class="title-card-top">
          <span class="ai-label text-13-bold">AI 칭호</span>

          <button
              type="button"
              class="title-share-button"
              aria-label="소비 분석 결과 공유"
              @click="shareResult"
          >
            <i class="fa-solid fa-share-nodes"></i>
            <span>공유</span>
          </button>
        </div>

        <div class="title-card-main">
          <div class="title-copy">
            <h2 class="text-18-bold">{{ latestAnalysis.aiTitle }}</h2>
            <p class="text-13">{{ latestAnalysis.aiAnalysisSummary }}</p>
          </div>

          <div class="title-illustration" aria-hidden="true">
            <i :class="getCategoryIcon(latestAnalysis.representativeCategoryName)"></i>
          </div>
        </div>
      </section>

      <AnalysisSummaryCard
          class="main-summary-card"
          :categories="sortedCategories"
          :total-amount="latestAnalysis.totalSpendingAmount"
          :transaction-count="latestAnalysis.classifiedTransactionCount"
          :representative-category-id="latestAnalysis.representativeCategoryId"
          :representative-category-name="latestAnalysis.representativeCategoryName"
          :period="selectedPeriod"
          :start-date="latestAnalysis.analysisStartDate"
          :end-date="latestAnalysis.analysisEndDate"
      >
        <template #actions>
          <button
              type="button"
              class="content-btn secondary analysis-action-button"
              :disabled="analysisRunning"
              @click="goToCheck"
          >
            <span>{{ analysisRunning ? '분석 중' : '다시 분석하기' }}</span>
            <span v-if="analysisRunning" class="button-spinner" aria-hidden="true"></span>
          </button>
          <button type="button" class="content-btn primary" @click="goToResult">
            상세 분석 보기 <i class="fa-solid fa-chevron-right"></i>
          </button>
        </template>
      </AnalysisSummaryCard>

      <section class="kb-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-18-bold">카테고리별 소비</h2>
          <button type="button" class="text-link text-13-bold" @click="goToCategorySummary">
            전체보기 <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>

        <div class="category-breakdown kb-card">
          <div
              v-for="(category, index) in sortedCategories.slice(0, 3)"
              :key="category.spendingCategoryId"
              class="category-row"
          >
            <div
                class="category-icon"
                :style="{
                backgroundColor: `${categoryColor(category, index)}20`,
                color: categoryColor(category, index),
              }"
            >
              <i :class="getCategoryIcon(category.categoryName)"></i>
            </div>
            <div class="category-info">
              <div class="category-head">
                <strong class="text-15-bold">{{ category.categoryName }}</strong>
                <span class="text-13">{{ formatRatio(category.spendingRatio) }}%</span>
              </div>
              <div class="ratio-track">
                <span
                    :style="{
                    width: `${Math.min(Number(category.spendingRatio), 100)}%`,
                    backgroundColor: categoryColor(category, index),
                  }"
                ></span>
              </div>
            </div>
            <div class="category-amount">
              <strong class="text-15-bold">{{ formatAnalysisNumber(category.spendingAmount) }}원</strong>
              <span class="text-13">{{ category.transactionCount }}건</span>
            </div>
          </div>
        </div>
      </section>
    </template>

    <section v-else class="empty-analysis kb-card">
      <div class="empty-analysis__icon"><i class="fa-solid fa-chart-pie"></i></div>
      <span class="text-13-bold">{{ selectedPeriod }}개월</span>
      <h2 class="text-20-bold">아직 저장된 소비 분석 결과가 없어요</h2>
      <p class="text-13">현재 소비내역이 분석 가능한 상태인지 확인한 뒤<br/>새로운 분석을 시작해 보세요.</p>
      <button
          type="button"
          class="content-btn primary analysis-action-button"
          :disabled="analysisRunning"
          @click="goToCheck"
      >
        <span>{{ analysisRunning ? '분석 중' : `${selectedPeriod}개월 소비 분석하기` }}</span>
        <span v-if="analysisRunning" class="button-spinner" aria-hidden="true"></span>
      </button>
    </section>

    <section class="kb-section recent-section">
      <div class="kb-section-title-row">
        <h2 class="kb-section-title text-18-bold">최근 소비내역</h2>
        <button type="button" class="text-link text-13-bold" @click="goToAllTransactions">
          전체보기 <i class="fa-solid fa-chevron-right"></i>
        </button>
      </div>

      <div class="recent-spending kb-card">
        <div v-if="transactionsLoading" class="kb-loading py-4 text-13">
          최근 거래를 불러오는 중이에요.
        </div>
        <div v-else-if="recentTransactions.length">
          <div
              v-for="transaction in recentTransactions"
              :key="transaction.transactionId"
              class="spending-row"
          >
            <div class="spending-icon">
              <i :class="getCategoryIcon(transaction.parentCategoryName || transaction.categoryName)"></i>
            </div>
            <!-- 거래명/거래일시 영역도 카테고리 수정 화면으로 이동할 수 있게 한다. -->
            <button
                type="button"
                class="spending-info spending-info-button"
                @click="goToCategoryEdit(transaction)"
            >
              <strong class="text-15-bold">{{ transaction.transactionLabel || transaction.merchantName || '거래 정보 없음' }}</strong>
              <span class="text-13">{{ formatShortDate(transaction.createdAt) }}</span>
            </button>
            <div class="spending-right">
              <strong class="text-15-bold">-{{ formatAnalysisNumber(transaction.amount) }}원</strong>
              <button type="button" class="text-13-bold" @click="goToCategoryEdit(transaction)">
                {{ transaction.categoryName || '미분류' }}
                <i class="fa-solid fa-pen"></i>
              </button>
            </div>
          </div>
        </div>
        <div v-else class="kb-empty-state py-4">
          <strong class="text-15-bold">표시할 소비내역이 없어요.</strong>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import AnalysisSummaryCard from '@/components/common/AnalysisSummaryCard.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import CommonTabBar from '@/components/common/CommonTabBar.vue';
import analysisApi from '@/api/analysisApi';
import {
  ANALYSIS_PERIODS,
  formatAnalysisNumber,
  getAnalysisCategoryColor,
  getAnalysisErrorMessage,
  getCategoryIcon,
  isAnalysisResultNotFound,
  normalizeAnalysisPeriod,
} from '@/util/analysis';

const route = useRoute();
const router = useRouter();
const selectedPeriod = ref(normalizeAnalysisPeriod(route.query.period));
const latestAnalysis = ref(null);
const transactions = ref([]);
const latestLoading = ref(false);
const transactionsLoading = ref(false);
const message = ref('');
const messageType = ref('error');
const analysisRunning = ref(false);
let statusTimer = null;

const pageLoading = computed(() => latestLoading.value);
const sortedCategories = computed(() =>
    [...(latestAnalysis.value?.categories ?? [])].sort(
        (left, right) =>
            Number(right.spendingAmount ?? 0) - Number(left.spendingAmount ?? 0),
    ),
);
const recentTransactions = computed(() => transactions.value.slice(0, 3));

const formatRatio = (value) => {
  const ratio = Number(value ?? 0);
  return Number.isInteger(ratio) ? ratio : ratio.toFixed(1);
};
const formatShortDate = (value) =>
    value ? String(value).replace('T', ' ').slice(0, 16) : '-';
const categoryColor = (category, index) =>
    getAnalysisCategoryColor(category.categoryName, index);

const loadLatestAnalysis = async () => {
  latestLoading.value = true;
  try {
    latestAnalysis.value = await analysisApi.getLatestAnalysisDetail(
        selectedPeriod.value,
    );
  } catch (error) {
    if (isAnalysisResultNotFound(error)) {
      latestAnalysis.value = null;
    } else {
      latestAnalysis.value = null;
      messageType.value = 'error';
      message.value = getAnalysisErrorMessage(
          error,
          '최근 소비 분석 결과를 불러오지 못했습니다.',
      );
    }
  } finally {
    latestLoading.value = false;
  }
};

const loadTransactions = async () => {
  transactionsLoading.value = true;
  try {
    const result = await analysisApi.getAllTransactions();
    transactions.value = result.transactions ?? [];
  } catch (error) {
    transactions.value = [];
    messageType.value = 'error';
    message.value = getAnalysisErrorMessage(
        error,
        '최근 소비내역을 불러오지 못했습니다.',
    );
  } finally {
    transactionsLoading.value = false;
  }
};

const stopStatusPolling = () => {
  if (statusTimer) {
    window.clearInterval(statusTimer);
    statusTimer = null;
  }
};

const loadAnalysisStatus = async ({notifyOnComplete = false} = {}) => {
  try {
    const status = await analysisApi.getAnalysisStatus(selectedPeriod.value);
    const wasRunning = analysisRunning.value;
    analysisRunning.value = status?.status === 'PROCESSING';

    if (analysisRunning.value) {
      if (!statusTimer) {
        statusTimer = window.setInterval(
            () => loadAnalysisStatus({notifyOnComplete: true}),
            2000,
        );
      }
      return;
    }

    if (status?.status === 'COMPLETED') {
      const completedAnalysisId = Number(status?.spendingAnalysisId);
      const displayedAnalysisId = Number(latestAnalysis.value?.spendingAnalysisId);

      if (
          wasRunning ||
          (Number.isInteger(completedAnalysisId) &&
              completedAnalysisId > 0 &&
              completedAnalysisId !== displayedAnalysisId)
      ) {
        stopStatusPolling();
        await loadLatestAnalysis();
      }

      if (wasRunning && notifyOnComplete) {
        messageType.value = 'info';
        message.value = '소비 분석이 완료되어 최신 결과를 불러왔습니다.';
      }
      return;
    }

    if (wasRunning && status?.status === 'FAILED') {
      stopStatusPolling();
      messageType.value = 'error';
      message.value = status?.message || '소비 분석 실행에 실패했습니다.';
    }
  } catch (error) {
    if (analysisRunning.value) {
      stopStatusPolling();
      analysisRunning.value = false;
      messageType.value = 'error';
      message.value = getAnalysisErrorMessage(
          error,
          '소비 분석 진행 상태를 확인하지 못했습니다.',
      );
    }
  }
};

const loadPage = async () => {
  message.value = '';
  await Promise.all([
    loadLatestAnalysis(),
    loadTransactions(),
    loadAnalysisStatus(),
  ]);
};

const changePeriod = async (period) => {
  stopStatusPolling();
  analysisRunning.value = false;
  selectedPeriod.value = period;
  await router.replace({name: 'analysis-main', query: {period}});
  await loadPage();
};

const goToFinance = () => {
  router.push('/finance');
};

const goToCheck = () => {
  if (analysisRunning.value) return;
  router.push({name: 'analysis-check', query: {period: selectedPeriod.value}});
};

const goToResult = () => {
  if (!latestAnalysis.value?.spendingAnalysisId) {
    goToCheck();
    return;
  }
  router.push({
    name: 'analysis-result',
    params: {spendingAnalysisId: latestAnalysis.value.spendingAnalysisId},
  });
};

const shareResult = () => {
  // 분석 결과 피드 공유 동작 연결 영역
};

const goToCategorySummary = () => {
  if (!latestAnalysis.value?.spendingAnalysisId) {
    goToCheck();
    return;
  }
  router.push({
    name: 'analysis-category-summary',
    params: {spendingAnalysisId: latestAnalysis.value.spendingAnalysisId},
  });
};

const goToAllTransactions = () => {
  router.push({name: 'analysis-transactions'});
};

const goToCategoryEdit = (transaction) =>
    router.push({
      name: 'analysis-category-edit',
      params: {transactionId: transaction.transactionId},
      query: {
        period: selectedPeriod.value,
        returnTo: router.currentRoute.value.fullPath,
      },
    });

onMounted(loadPage);
onBeforeUnmount(stopStatusPolling);
</script>

<style scoped>
.analysis-page {
  min-height: 100vh;
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.content-btn {
  font-size: 15px;
  font-weight: 600;
}

/* 포인트 전환 화면과 동일하게 헤더 좌우 24px 여백 적용 */
.analysis-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

.analysis-tabs.is-loading {
  pointer-events: none;
  opacity: 0.65;
}

/* 소비 분석 화면에서만 선택선이 각 탭 너비 전체를 차지하도록 보정 */
.analysis-tabs :deep(.common-tab-btn.active::after) {
  right: 0;
  left: 0;
}

.content-loading,
.title-card,
.empty-analysis,
.kb-section {
  margin-right: 24px;
  margin-left: 24px;
}

.content-loading,
.title-card,
.empty-analysis {
  margin-top: 16px;
}

.title-card {
  padding: 18px 16px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  overflow: hidden;
  border: 1px solid #ffe19a;
  background: linear-gradient(135deg, #fffaf0 0%, #fff4d2 100%);
  box-shadow: none;
}

.title-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.title-card-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.title-copy {
  min-width: 0;
  flex: 1;
  gap: 10px;
}

.ai-label {
  display: inline-flex;
  padding: 4px 9px;
  border-radius: 999px;
  background: #ffeab0;
  color: #9b7000;
}

.title-share-button {
  margin: 0;
  padding: 3px 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 0;
  background: transparent;
  color: var(--color-text-sub);
  font-size: 12px;
  font-weight: 500;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
}

.title-share-button i {
  font-size: 12px;
}

.analysis-message {
  margin: 12px 24px 0;
  padding: 11px 13px;
  border-radius: 12px;
  line-height: 1.45;
  word-break: keep-all;
}

.analysis-message.info {
  background: #fff8dc;
  color: #806000;
}

.analysis-message.error {
  background: #fff0f0;
  color: var(--color-error);
}

.title-copy h2 {
  margin: 10px 0 8px;
  letter-spacing: -0.7px;
  line-height: 1.25;
}

/* AI 설명은 내용과 줄 수를 제한하지 않고 그대로 노출 */
.title-copy p {
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.6;
  word-break: keep-all;
}

.title-illustration {
  width: 54px;
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 54px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  color: #e7a300;
  font-size: 22px;
  box-shadow: 0 7px 18px rgba(153, 117, 0, 0.1);
}

.main-summary-card {
  margin-right: 24px;
  margin-left: 24px;
}

.text-link {
  border: 0;
  background: transparent;
  color: var(--color-text-sub);
}

.kb-section {
  margin-top: 28px;
}

.category-breakdown,
.recent-spending {
  padding: 4px 16px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.category-row {
  min-height: 68px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--color-divider);
}

.category-row:last-child,
.spending-row:last-child {
  border-bottom: 0;
}

.category-icon,
.spending-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 36px;
  border-radius: 12px;
  font-size: 14px;
}

.category-info {
  min-width: 0;
  flex: 1;
}

.category-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.category-head span {
  color: var(--color-text-muted);
}

.ratio-track {
  height: 5px;
  margin-top: 7px;
  overflow: hidden;
  border-radius: 8px;
  background: var(--color-bg-disabled);
}

.ratio-track span {
  display: block;
  height: 100%;
  border-radius: 8px;
}

.category-amount {
  min-width: 74px;
  text-align: right;
}

.category-amount strong,
.category-amount span {
  display: block;
  line-height: 1.35;
}

.category-amount span {
  margin-top: 1px;
  color: var(--color-text-disabled);
}

.empty-analysis {
  padding: 32px 24px;
  text-align: center;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.empty-analysis__icon {
  width: 68px;
  height: 68px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  background: #fff3cf;
  color: #d99b00;
  font-size: 28px;
}

.empty-analysis > span {
  display: inline-flex;
  padding: 4px 9px;
  border-radius: 999px;
  background: var(--color-bg-disabled);
  color: var(--color-text-sub);
}

.empty-analysis h2 {
  margin: 10px 0 0;
}

.empty-analysis p {
  margin: 8px 0 18px;
  color: var(--color-text-sub);
  line-height: 1.65;
}

.empty-analysis button {
  width: 100%;
}

.recent-section {
  margin-top: 28px;
}

.spending-row {
  min-height: 68px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--color-divider);
}

.spending-icon {
  background: #fff3cf;
  color: #d99b00;
  font-size: 13px;
}

.spending-info {
  min-width: 0;
  flex: 1;
}

.spending-info-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.spending-info strong,
.spending-info span {
  display: block;
  line-height: 1.35;
}

.spending-info strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spending-info span {
  margin-top: 1px;
  color: var(--color-text-disabled);
}

.spending-right {
  text-align: right;
}

.spending-right > strong {
  display: block;
  line-height: 1.35;
}

.spending-right button {
  margin-top: 1px;
  border: 0;
  background: transparent;
  color: #a27800;
}

.analysis-action-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
}

.analysis-action-button:disabled {
  cursor: not-allowed;
  opacity: 0.75;
}

.button-spinner {
  width: 13px;
  height: 13px;
  flex: 0 0 13px;
  border: 2px solid rgba(34, 34, 34, 0.25);
  border-top-color: #222;
  border-radius: 50%;
  animation: analysis-button-spin 0.75s linear infinite;
}

@media (max-width: 380px) {
  .content-loading,
  .title-card,
  .empty-analysis,
  .kb-section,
  .main-summary-card {
    margin-right: 20px;
    margin-left: 20px;
  }

  .analysis-message {
    margin-right: 20px;
    margin-left: 20px;
  }

  .title-card {
    padding-right: 20px;
    padding-left: 20px;
  }
}

@keyframes analysis-button-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
