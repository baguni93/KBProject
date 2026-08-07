<template>
  <div class="kb-mobile-page analysis-page">
    <header class="kb-app-header">
      <span></span>
      <h1 class="kb-app-header__title">최근 소비 분석 결과</h1>
      <span></span>
    </header>

    <div
        v-if="message"
        :class="[
        'kb-toast',
        messageType === 'info' ? 'kb-toast--info' : 'kb-toast--error',
      ]"
    >
      {{ message }}
    </div>

    <nav class="period-tabs" aria-label="소비 분석 기간">
      <button
          v-for="periodOption in ANALYSIS_PERIODS"
          :key="periodOption.value"
          type="button"
          :class="{ active: selectedPeriod === periodOption.value }"
          :disabled="pageLoading"
          @click="changePeriod(periodOption.value)"
      >
        {{ periodOption.label }}
      </button>
    </nav>

    <div v-if="pageLoading" class="kb-card kb-loading content-loading">
      <div class="spinner-border kb-spinner" role="status"></div>
      <div>{{ selectedPeriod }}개월 최근 분석 결과를 불러오는 중이에요.</div>
    </div>

    <template v-else-if="latestAnalysis">
      <section class="title-card kb-card">
        <div class="title-copy">
          <span class="ai-label">AI 칭호</span>
          <h2>{{ latestAnalysis.aiTitle }}</h2>
          <p>{{ latestAnalysis.aiAnalysisSummary }}</p>
        </div>
        <div class="title-illustration" aria-hidden="true">
          <i :class="getCategoryIcon(latestAnalysis.representativeCategoryName)"></i>
        </div>
      </section>

      <section class="summary-card kb-card">
        <div class="donut-column">
          <AnalysisDonutChart
              :categories="sortedCategories"
              :total-amount="latestAnalysis.totalSpendingAmount"
          />
        </div>

        <div class="representative-column">
          <span>대표 소비 카테고리</span>
          <div class="representative-name">
            <span
                class="representative-icon"
                :style="{
                backgroundColor: `${representativeColor}20`,
                color: representativeColor,
              }"
            >
              <i :class="getCategoryIcon(latestAnalysis.representativeCategoryName)"></i>
            </span>
            <strong>{{ latestAnalysis.representativeCategoryName }}</strong>
          </div>
          <div v-if="representativeCategory" class="representative-amount">
            <strong>{{ formatAnalysisNumber(representativeCategory.spendingAmount) }}원</strong>
            <span>{{ formatRatio(representativeCategory.spendingRatio) }}%</span>
          </div>
          <p>{{ formatAnalysisExecutionDate(latestAnalysis.createdAt) }} 실행</p>
        </div>

        <div class="summary-stats">
          <div>
            <span>총 소비 금액</span>
            <strong>{{ formatAnalysisNumber(latestAnalysis.totalSpendingAmount) }}원</strong>
          </div>
          <div>
            <span>분석 거래</span>
            <strong>{{ formatAnalysisNumber(latestAnalysis.classifiedTransactionCount) }}건</strong>
          </div>
          <div>
            <span>분석 기간</span>
            <strong class="summary-period-range">
              <span>{{ formatAnalysisExecutionDate(latestAnalysis.analysisStartDate, false) }}</span>
              <span>~ {{ formatAnalysisExecutionDate(latestAnalysis.analysisEndDate, false) }}</span>
            </strong>
          </div>
        </div>

        <div class="summary-actions">
          <button type="button" class="kb-outline-button" @click="goToResult">
            상세 분석 보기
          </button>
          <button
              type="button"
              class="kb-primary-button analysis-action-button"
              :disabled="analysisRunning"
              @click="goToCheck"
          >
            <span>{{ analysisRunning ? '분석 중' : '다시 분석하기' }}</span>
            <span v-if="analysisRunning" class="button-spinner" aria-hidden="true"></span>
          </button>
        </div>
      </section>

      <section class="kb-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title">카테고리별 소비</h2>
          <button type="button" class="text-link" @click="goToResult">
            전체보기 <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>

        <div class="category-breakdown kb-card">
          <div
              v-for="(category, index) in sortedCategories.slice(0, 5)"
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
                <strong>{{ category.categoryName }}</strong>
                <span>{{ formatRatio(category.spendingRatio) }}%</span>
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
              <strong>{{ formatAnalysisNumber(category.spendingAmount) }}원</strong>
              <span>{{ category.transactionCount }}건</span>
            </div>
          </div>
        </div>
      </section>
    </template>

    <section v-else class="empty-analysis kb-card">
      <div class="empty-analysis__icon"><i class="fa-solid fa-chart-pie"></i></div>
      <span>{{ selectedPeriod }}개월</span>
      <h2>아직 저장된 소비 분석 결과가 없어요</h2>
      <p>현재 소비내역이 분석 가능한 상태인지 확인한 뒤<br/>새로운 분석을 시작해 보세요.</p>
      <button
          type="button"
          class="kb-primary-button analysis-action-button"
          :disabled="analysisRunning"
          @click="goToCheck"
      >
        <span>{{ analysisRunning ? '분석 중' : `${selectedPeriod}개월 소비 분석하기` }}</span>
        <span v-if="analysisRunning" class="button-spinner" aria-hidden="true"></span>
      </button>
    </section>

    <section class="kb-section recent-section">
      <div class="kb-section-title-row">
        <h2 class="kb-section-title">최근 소비내역</h2>
        <button type="button" class="text-link" @click="goToAllTransactions">
          전체보기 <i class="fa-solid fa-chevron-right"></i>
        </button>
      </div>

      <div class="recent-spending kb-card">
        <div v-if="transactionsLoading" class="kb-loading py-4">
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
            <div class="spending-info">
              <strong>{{ transaction.merchantName || '가맹점 정보 없음' }}</strong>
              <span>{{ formatShortDate(transaction.createdAt) }}</span>
            </div>
            <div class="spending-right">
              <strong>-{{ formatAnalysisNumber(transaction.amount) }}원</strong>
              <button type="button" @click="goToCategoryEdit(transaction)">
                {{ transaction.categoryName || '미분류' }}
                <i class="fa-solid fa-pen"></i>
              </button>
            </div>
          </div>
        </div>
        <div v-else class="kb-empty-state py-4">
          <strong>표시할 소비내역이 없어요.</strong>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import AnalysisDonutChart from '@/components/analysis/AnalysisDonutChart.vue';
import analysisApi from '@/api/analysisApi';
import {
  ANALYSIS_PERIODS,
  formatAnalysisExecutionDate,
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
const recentTransactions = computed(() => transactions.value.slice(0, 5));
const representativeCategory = computed(() =>
    sortedCategories.value.find(
        (category) =>
            Number(category.spendingCategoryId) ===
            Number(latestAnalysis.value?.representativeCategoryId),
    ),
);
const representativeColor = computed(() =>
    getAnalysisCategoryColor(latestAnalysis.value?.representativeCategoryName, 0),
);

const formatRatio = (value) => {
  const ratio = Number(value ?? 0);
  return Number.isInteger(ratio) ? ratio : ratio.toFixed(1);
};
const formatShortDate = (value) =>
    value ? String(value).replace('T', ' ').slice(5, 16) : '-';
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
    const result = await analysisApi.getTransactions(selectedPeriod.value);
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

const goToAllTransactions = () => {
  if (!latestAnalysis.value?.spendingAnalysisId) {
    goToCheck();
    return;
  }
  router.push({
    name: 'analysis-result',
    params: {spendingAnalysisId: latestAnalysis.value.spendingAnalysisId},
    query: {section: 'transactions'},
  });
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
  margin-top: -16px;
  padding-bottom: 34px
}

.analysis-page .kb-app-header__title {
  font-size: 16px
}

.period-tabs {
  display: grid;
  grid-template-columns:repeat(3, 1fr);
  border-bottom: 1px solid #e8e8e8;
  background: #fff
}

.period-tabs button {
  position: relative;
  height: 50px;
  border: 0;
  background: transparent;
  color: #777;
  font-size: 13px;
  font-weight: 800
}

.period-tabs button::after {
  content: '';
  position: absolute;
  right: 22%;
  bottom: -1px;
  left: 22%;
  height: 3px;
  border-radius: 3px 3px 0 0;
  background: transparent
}

.period-tabs button.active {
  color: #d99500
}

.period-tabs button.active::after {
  background: var(--kb-yellow)
}

.content-loading {
  margin-top: 14px
}

.title-card {
  margin-top: 14px;
  padding: 16px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
  border: 1px solid #ffe19a;
  background: linear-gradient(135deg, #fffaf0 0%, #fff4d2 100%);
  box-shadow: none
}

.title-copy {
  min-width: 0;
  flex: 1
}

.ai-label {
  display: inline-flex;
  padding: 4px 9px;
  border-radius: 999px;
  background: #ffeab0;
  color: #9b7000;
  font-size: 11px;
  font-weight: 900
}

.title-copy h2 {
  margin: 9px 0 7px;
  font-size: 20px;
  font-weight: 900;
  letter-spacing: -.7px
}

.title-copy p {
  margin: 0;
  display: -webkit-box;
  overflow: hidden;
  color: #746d5d;
  font-size: 12px;
  line-height: 1.6;

}

.title-illustration {
  position: relative;
  width: 54px;
  height: 54px;
  margin-left: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 54px;
  border-radius: 18px;
  background: rgba(255, 255, 255, .85);
  color: #e7a300;
  font-size: 22px;
  box-shadow: 0 7px 18px rgba(153, 117, 0, .12)
}


.summary-card {
  margin-top: 13px;
  padding: 18px;
  display: grid;
  grid-template-columns:minmax(0, 1fr) minmax(130px, .9fr);
  align-items: center;
  gap: 12px 18px;
  border: 1px solid #ededed;
  box-shadow: none
}

.donut-column {
  min-width: 0;
  text-align: center
}

.representative-column > span {
  color: #858585;
  font-size: 11px
}

.representative-name {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 9px
}

.representative-icon {
  width: 30px;
  height: 35px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 38px;
  border-radius: 13px;
  font-size: 10px
}

.representative-name strong {
  font-size: 15px;
  font-weight: 900
}

.representative-amount {
  margin-top: 11px;
  display: flex;
  align-items: baseline;
  gap: 8px
}

.representative-amount strong {
  font-size: 17px;
  font-weight: 900
}

.representative-amount span {
  color: #777;
  font-size: 11px
}

.representative-column p {
  margin: 6px 0 0;
  color: #aaa;
  font-size: 9px
}

.summary-stats {
  grid-column: 1/-1;
  display: grid;
  grid-template-columns:repeat(3, 1fr);
  overflow: hidden;
  border: 1px solid #ececec;
  border-radius: 13px
}

.summary-stats div {
  position: relative;
  min-width: 0;
  padding: 12px 5px;
  text-align: center
}

.summary-stats div + div::before {
  content: '';
  position: absolute;
  top: 12px;
  bottom: 12px;
  left: 0;
  width: 1px;
  background: #ececec
}

.summary-stats span, .summary-stats strong {
  display: block
}

.summary-stats span {
  color: #8f8f8f;
  font-size: 10px
}

.summary-stats strong {
  margin-top: 4px;
  min-width: 0;
  font-size: 11px;
  font-weight: 900;
  line-height: 1.35;
  overflow-wrap: anywhere;
  word-break: keep-all;
  white-space: normal;
  color: inherit;
}

.summary-actions {
  grid-column: 1/-1;
  display: grid;
  grid-template-columns:1fr 1fr;
  gap: 8px
}

.summary-actions button {
  font-size: 13px
}

.text-link {
  border: 0;
  background: transparent;
  color: #2676c9;
  font-size: 12px;
  font-weight: 800
}

.category-breakdown {
  padding: 3px 15px;
  border: 1px solid #ededed;
  box-shadow: none
}

.category-row {
  min-height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f1f1f1
}

.category-row:last-child {
  border-bottom: 0
}

.category-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 36px;
  border-radius: 12px;
  font-size: 14px
}

.category-info {
  min-width: 0;
  flex: 1
}

.category-head {
  display: flex;
  justify-content: space-between;
  gap: 8px
}

.category-head strong {
  font-size: 13px
}

.category-head span {
  color: #858585;
  font-size: 11px
}

.ratio-track {
  height: 5px;
  margin-top: 7px;
  overflow: hidden;
  border-radius: 8px;
  background: #eff0f2
}

.ratio-track span {
  display: block;
  height: 100%;
  border-radius: 8px
}

.category-amount {
  min-width: 72px;
  text-align: right
}

.category-amount strong, .category-amount span {
  display: block
}

.category-amount strong {
  font-size: 10px
}

.category-amount span {
  margin-top: 2px;
  color: #aaa;
  font-size: 8px
}

.empty-analysis {
  margin-top: 16px;
  padding: 34px 20px;
  text-align: center;
  border: 1px solid #ededed;
  box-shadow: none
}

.empty-analysis__icon {
  width: 74px;
  height: 74px;
  margin: 0 auto 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 26px;
  background: #fff3cf;
  color: #d99b00;
  font-size: 30px
}

.empty-analysis > span {
  display: inline-flex;
  padding: 4px 9px;
  border-radius: 999px;
  background: #f4f4f4;
  color: #777;
  font-size: 9px;
  font-weight: 800
}

.empty-analysis h2 {
  margin: 10px 0 0;
  font-size: 17px;
  font-weight: 900
}

.empty-analysis p {
  margin: 8px 0 18px;
  color: #777;
  font-size: 10px;
  line-height: 1.65
}

.empty-analysis button {
  width: 100%
}

.recent-spending {
  padding: 3px 15px;
  border: 1px solid #ededed;
  box-shadow: none
}

.spending-row {
  min-height: 65px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f2f2f2
}

.spending-row:last-child {
  border-bottom: 0
}

.spending-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 36px;
  border-radius: 12px;
  background: #fff3cf;
  color: #d99b00;
  font-size: 13px
}

.spending-info {
  min-width: 0;
  flex: 1
}

.spending-info strong, .spending-info span {
  display: block
}

.spending-info strong {
  overflow: hidden;
  font-size: 13px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spending-info span {
  margin-top: 3px;
  color: #aaa;
  font-size: 8px
}

.spending-right {
  text-align: right
}

.spending-right > strong {
  display: block;
  font-size: 10px
}

.spending-right button {
  margin-top: 4px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #a27800;
  font-size: 10px;
  font-weight: 700;
}

@media (max-width: 380px) {
  .summary-card {
    grid-template-columns:1fr
  }

  .representative-column {
    text-align: center
  }

  .representative-name, .representative-amount {
    justify-content: center
  }

  .summary-stats, .summary-actions {
    grid-column: 1
  }

  .summary-actions {
    grid-template-columns:1fr
  }
}

.analysis-action-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px
}

.analysis-action-button:disabled {
  cursor: not-allowed;
  opacity: .75
}

.button-spinner {
  width: 13px;
  height: 13px;
  flex: 0 0 13px;
  border: 2px solid rgba(34, 34, 34, .25);
  border-top-color: #222;
  border-radius: 50%;
  animation: analysis-button-spin .75s linear infinite
}

.summary-period-range span {
  display: block;
  white-space: nowrap;
  color: inherit;
}

@keyframes analysis-button-spin {
  to {
    transform: rotate(360deg)
  }
}
</style>
