<template>
  <div class="kb-mobile-page analysis-page">
    <header class="kb-app-header">
      <span></span>
      <h1 class="kb-app-header__title">내 소비 분석</h1>
      <span></span>
    </header>

    <div
      v-if="message"
      :class="[
        'kb-toast',
        messageType === 'success'
          ? 'kb-toast--success'
          : messageType === 'info'
            ? 'kb-toast--info'
            : 'kb-toast--error',
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
        :disabled="availabilityLoading || analysisLoading"
        @click="changePeriod(periodOption.value)"
      >
        {{ periodOption.label }}
      </button>
    </nav>

    <div v-if="pageLoading" class="kb-card kb-loading content-loading">
      <div class="spinner-border kb-spinner" role="status"></div>
      <div>소비 분석 정보를 불러오는 중이에요.</div>
    </div>

    <template v-else-if="displayedAnalysis">
      <section class="title-card kb-card">
        <div class="title-copy">
          <span class="ai-label">AI 칭호</span>
          <h2>{{ displayedAnalysis.aiTitle }}</h2>
          <p>{{ displayedAnalysis.aiAnalysisSummary }}</p>
        </div>
        <div class="title-illustration" aria-hidden="true">
          <i :class="getCategoryIcon(displayedAnalysis.representativeCategoryName)"></i>
          <span>AI</span>
        </div>
      </section>

      <section class="summary-card kb-card">
        <div class="donut-column">
          <AnalysisDonutChart
            :categories="sortedAnalysisCategories"
            :total-amount="displayedAnalysis.totalSpendingAmount"
          />
          <small>차트 조각을 누르면 상세 금액을 볼 수 있어요.</small>
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
              <i :class="getCategoryIcon(displayedAnalysis.representativeCategoryName)"></i>
            </span>
            <strong>{{ displayedAnalysis.representativeCategoryName }}</strong>
          </div>
          <div v-if="representativeCategory" class="representative-amount">
            <strong>{{ formatAnalysisNumber(representativeCategory.spendingAmount) }}원</strong>
            <span>{{ formatRatio(representativeCategory.spendingRatio) }}%</span>
          </div>
          <p>{{ displayedAnalysis.periodLabel }} 기준</p>
        </div>

        <div class="summary-stats">
          <div>
            <span>총 소비 금액</span>
            <strong>{{ formatAnalysisNumber(displayedAnalysis.totalSpendingAmount) }}원</strong>
          </div>
          <div>
            <span>일 평균 소비</span>
            <strong>{{ formatAnalysisNumber(dailyAverageAmount) }}원</strong>
          </div>
          <div>
            <span>분석 거래</span>
            <strong>{{ formatAnalysisNumber(displayedAnalysis.classifiedTransactionCount) }}건</strong>
          </div>
        </div>
      </section>

      <section class="kb-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title">카테고리별 소비</h2>
          <button
            v-if="sortedAnalysisCategories.length > 5"
            class="category-toggle"
            type="button"
            @click="showAllCategories = !showAllCategories"
          >
            {{ showAllCategories ? '접기' : '전체보기' }}
            <i :class="showAllCategories ? 'fa-solid fa-chevron-up' : 'fa-solid fa-chevron-right'"></i>
          </button>
        </div>

        <div class="category-breakdown kb-card">
          <div
            v-for="(category, index) in visibleAnalysisCategories"
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
          <p v-if="!showAllCategories && sortedAnalysisCategories.length > 5" class="category-guide">
            소비 금액이 큰 상위 5개 항목을 보여드려요.
          </p>
        </div>
      </section>

      <section class="ai-insight kb-card">
        <div class="ai-insight__label"><i class="fa-solid fa-wand-magic-sparkles"></i> AI 분석</div>
        <p>{{ displayedAnalysis.aiAnalysisSummary }}</p>
        <div class="ai-insight__illustration" aria-hidden="true">
          <i class="fa-solid fa-chart-pie"></i>
        </div>
      </section>

      <section class="kb-section recommendation-section">
        <div class="recommendation-grid">
          <button
            type="button"
            class="recommendation-card kb-card"
            @click="showPlannedFeature('카드 추천')"
          >
            <div class="recommend-icon card-icon"><i class="fa-regular fa-credit-card"></i></div>
            <div><strong>카드 추천</strong><span>나에게 맞는 카드 찾기</span></div>
            <i class="fa-solid fa-chevron-right"></i>
          </button>
          <button
            type="button"
            class="recommendation-card kb-card"
            @click="showPlannedFeature('보험 추천')"
          >
            <div class="recommend-icon insurance-icon"><i class="fa-solid fa-shield-heart"></i></div>
            <div><strong>보험 추천</strong><span>나에게 맞는 보험 찾기</span></div>
            <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>
      </section>
    </template>

    <section v-else-if="availability" class="analysis-state kb-card">
      <div class="state-visual" :class="stateVisualClass">
        <i :class="stateIcon"></i>
      </div>

      <template v-if="!availability.available">
        <h2>소비 분석을 위해<br />카테고리 분류가 필요해요</h2>
        <p>
          선택한 기간의 결제 거래를 {{ availability.remainingCount }}건 더 분류하면<br />소비 분석을 시작할 수 있어요.
        </p>
      </template>

      <template v-else-if="availability.unclassifiedPaymentCount > 0">
        <h2>아직 분류하지 않은<br />거래가 있어요</h2>
        <p>
          미분류 거래 {{ availability.unclassifiedPaymentCount }}건이 남아 있어요.<br />지금 분류하거나 현재 내역으로 바로 분석할 수 있어요.
        </p>
      </template>

      <template v-else>
        <h2>소비 분석 준비가<br />완료됐어요</h2>
        <p>선택한 기간의 소비내역으로<br />나만의 소비 패턴을 확인해 보세요.</p>
      </template>

      <div class="classification-progress">
        <div>
          <strong>{{ availability.classifiedPaymentCount }}건 분류 완료</strong>
          <span>{{ availability.requiredCount }}건 필요</span>
        </div>
        <div class="analysis-progress">
          <span :style="{ width: `${progressPercent}%` }"></span>
        </div>
        <small>{{ availability.classifiedPaymentCount }} / {{ availability.requiredCount }}</small>
      </div>

      <div class="analysis-actions" :class="{ single: !availability.available || availability.unclassifiedPaymentCount === 0 }">
        <button
          v-if="!availability.available || availability.unclassifiedPaymentCount > 0"
          type="button"
          class="kb-outline-button"
          @click="goToClassification"
        >
          {{ availability.available ? '미분류 거래 분류하기' : '카테고리 분류하기' }}
        </button>
        <button
          v-if="availability.available"
          type="button"
          class="kb-primary-button"
          :disabled="analysisLoading"
          @click="executeAnalysis"
        >
          {{ analysisLoading ? '분석 중...' : '현재 내역으로 분석하기' }}
        </button>
      </div>
    </section>

    <section class="kb-section recent-section">
      <div class="kb-section-title-row">
        <h2 class="kb-section-title">최근 소비내역</h2>
        <span class="transaction-count">{{ analysisTransactions.length }}건</span>
      </div>
      <div class="recent-spending kb-card">
        <div v-if="transactionsLoading" class="kb-loading py-4">최근 거래를 불러오는 중이에요.</div>
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
                {{ transaction.categoryName || '미분류' }} <i class="fa-solid fa-pen"></i>
              </button>
            </div>
          </div>
        </div>
        <div v-else class="kb-empty-state py-4"><strong>표시할 소비내역이 없어요.</strong></div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import AnalysisDonutChart from '@/components/analysis/AnalysisDonutChart.vue';
import analysisApi from '@/api/analysisApi';
import {
  ANALYSIS_PERIODS,
  formatAnalysisNumber,
  getAnalysisCategoryColor,
  getAnalysisErrorMessage,
  getCategoryIcon,
  isAnalysisResultNotFound,
} from '@/util/analysis';

const router = useRouter();
const selectedPeriod = ref(1);
const availability = ref(null);
const latestAnalysis = ref(null);
const analysisTransactions = ref([]);
const availabilityLoading = ref(false);
const latestLoading = ref(false);
const transactionsLoading = ref(false);
const analysisLoading = ref(false);
const showAllCategories = ref(false);
const message = ref('');
const messageType = ref('success');

const pageLoading = computed(
  () => availabilityLoading.value || latestLoading.value,
);

const displayedAnalysis = computed(() => {
  if (!latestAnalysis.value) return null;

  const samePeriod =
    Number(latestAnalysis.value.period) === Number(selectedPeriod.value);

  return samePeriod ? latestAnalysis.value : null;
});

const sortedAnalysisCategories = computed(() =>
  [...(displayedAnalysis.value?.categories ?? [])].sort(
    (left, right) =>
      Number(right.spendingAmount ?? 0) - Number(left.spendingAmount ?? 0),
  ),
);

const visibleAnalysisCategories = computed(() =>
  showAllCategories.value
    ? sortedAnalysisCategories.value
    : sortedAnalysisCategories.value.slice(0, 5),
);

const representativeCategory = computed(() =>
  sortedAnalysisCategories.value.find(
    (category) =>
      Number(category.spendingCategoryId) ===
      Number(displayedAnalysis.value?.representativeCategoryId),
  ),
);

const representativeColor = computed(() =>
  getAnalysisCategoryColor(
    displayedAnalysis.value?.representativeCategoryName,
    0,
  ),
);

const recentTransactions = computed(() => analysisTransactions.value.slice(0, 5));

const progressPercent = computed(() => {
  if (!availability.value) return 0;
  const required = Number(availability.value.requiredCount || 10);
  return Math.min(
    (Number(availability.value.classifiedPaymentCount || 0) / required) * 100,
    100,
  );
});

const analysisDayCount = computed(() => {
  const start = displayedAnalysis.value?.analysisStartDate;
  const end = displayedAnalysis.value?.analysisEndDate;
  if (!start || !end) return 1;

  const startDate = new Date(`${start}T00:00:00`);
  const endDate = new Date(`${end}T00:00:00`);
  const difference = endDate.getTime() - startDate.getTime();
  return Math.max(Math.floor(difference / 86400000) + 1, 1);
});

const dailyAverageAmount = computed(() =>
  Math.round(
    Number(displayedAnalysis.value?.totalSpendingAmount ?? 0) /
      analysisDayCount.value,
  ),
);

const stateIcon = computed(() => {
  if (!availability.value?.available) return 'fa-solid fa-clipboard-list';
  if (availability.value.unclassifiedPaymentCount > 0)
    return 'fa-solid fa-file-circle-question';
  return 'fa-solid fa-chart-pie';
});

const stateVisualClass = computed(() => ({
  'needs-classification': !availability.value?.available,
  'has-unclassified':
    availability.value?.available &&
    availability.value?.unclassifiedPaymentCount > 0,
  ready:
    availability.value?.available &&
    availability.value?.unclassifiedPaymentCount === 0,
}));

const formatRatio = (value) => {
  const ratio = Number(value ?? 0);
  return Number.isInteger(ratio) ? ratio : ratio.toFixed(1);
};

const categoryColor = (category, index) =>
  getAnalysisCategoryColor(category.categoryName, index);

const formatShortDate = (value) =>
  value ? String(value).replace('T', ' ').slice(5, 16) : '-';

const setMessage = (type, text) => {
  messageType.value = type;
  message.value = text;
};

const loadAvailability = async () => {
  availabilityLoading.value = true;
  try {
    availability.value = await analysisApi.getAvailability(selectedPeriod.value);
  } catch (error) {
    availability.value = null;
    setMessage(
      'error',
      getAnalysisErrorMessage(
        error,
        '소비 분석 가능 여부를 불러오지 못했습니다.',
      ),
    );
  } finally {
    availabilityLoading.value = false;
  }
};

const loadLatestAnalysis = async () => {
  latestLoading.value = true;
  try {
    latestAnalysis.value = await analysisApi.getLatestAnalysisDetail();
  } catch (error) {
    if (isAnalysisResultNotFound(error)) {
      latestAnalysis.value = null;
    } else {
      setMessage(
        'error',
        getAnalysisErrorMessage(
          error,
          '최근 소비 분석 결과를 불러오지 못했습니다.',
        ),
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
    analysisTransactions.value = result.transactions ?? [];
  } catch (error) {
    analysisTransactions.value = [];
    setMessage(
      'error',
      getAnalysisErrorMessage(error, '최근 소비내역을 불러오지 못했습니다.'),
    );
  } finally {
    transactionsLoading.value = false;
  }
};

const loadPage = async () => {
  message.value = '';
  await Promise.all([
    loadAvailability(),
    loadLatestAnalysis(),
    loadTransactions(),
  ]);
};

const changePeriod = async (period) => {
  selectedPeriod.value = period;
  showAllCategories.value = false;
  message.value = '';
  await Promise.all([loadAvailability(), loadTransactions()]);
};

const goToClassification = () =>
  router.push({
    name: 'analysis-classification',
    query: { period: selectedPeriod.value },
  });

const goToCategoryEdit = (transaction) =>
  router.push({
    name: 'analysis-category-edit',
    params: { transactionId: transaction.transactionId },
    query: { period: selectedPeriod.value },
  });

const executeAnalysis = async () => {
  if (!availability.value?.available) {
    setMessage('error', '분류 완료된 결제 거래가 10건 이상 필요합니다.');
    return;
  }

  analysisLoading.value = true;
  setMessage(
    'info',
    'AI가 소비 패턴을 분석하고 있어요. 잠시만 기다려 주세요.',
  );

  try {
    const result = await analysisApi.executeAnalysis(selectedPeriod.value);
    if (!result?.spendingAnalysisId) {
      throw new Error('분석 결과 ID를 응답에서 확인할 수 없습니다.');
    }
    await router.push({
      name: 'analysis-result',
      params: { spendingAnalysisId: result.spendingAnalysisId },
    });
  } catch (error) {
    const fallback =
      error?.code === 'ECONNABORTED'
        ? '분석 요청 시간이 초과되었습니다. 최근 분석 결과를 다시 확인해 주세요.'
        : '소비 분석 실행에 실패했습니다.';
    setMessage('error', getAnalysisErrorMessage(error, fallback));
  } finally {
    analysisLoading.value = false;
  }
};

const showPlannedFeature = (featureName) =>
  setMessage('info', `${featureName} 기능은 다음 단계에서 연결할 예정입니다.`);

onMounted(loadPage);
</script>

<style scoped>
.analysis-page {
  padding-bottom: 34px;
}

.period-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
}

.period-tabs button {
  position: relative;
  height: 50px;
  border: 0;
  background: transparent;
  color: #777;
  font-size: 14px;
  font-weight: 800;
}

.period-tabs button::after {
  content: '';
  position: absolute;
  right: 22%;
  bottom: -1px;
  left: 22%;
  height: 3px;
  border-radius: 3px 3px 0 0;
  background: transparent;
}

.period-tabs button.active {
  color: #d99500;
}

.period-tabs button.active::after {
  background: var(--kb-yellow);
}

.content-loading {
  margin-top: 14px;
}

.title-card {
  margin-top: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  overflow: hidden;
  border: 1px solid #ffe19a;
  background: linear-gradient(135deg, #fffaf0 0%, #fff4d2 100%);
  box-shadow: none;
}

.title-copy {
  min-width: 0;
  flex: 1;
}

.ai-label {
  display: inline-flex;
  padding: 4px 9px;
  border-radius: 999px;
  background: #ffeab0;
  color: #9b7000;
  font-size: 10px;
  font-weight: 900;
}

.title-copy h2 {
  margin: 9px 0 7px;
  font-size: 20px;
  font-weight: 900;
  letter-spacing: -0.7px;
}

.title-copy p {
  margin: 0;
  display: -webkit-box;
  overflow: hidden;
  color: #746d5d;
  font-size: 11px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.title-illustration {
  position: relative;
  width: 76px;
  height: 76px;
  margin-left: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 76px;
  border-radius: 26px;
  background: rgba(255, 255, 255, 0.85);
  color: #e7a300;
  font-size: 32px;
  box-shadow: 0 7px 18px rgba(153, 117, 0, 0.12);
}

.title-illustration span {
  position: absolute;
  top: -4px;
  right: -3px;
  padding: 3px 7px;
  border-radius: 8px;
  background: #222;
  color: #fff;
  font-size: 8px;
  font-weight: 900;
}

.summary-card {
  margin-top: 13px;
  padding: 18px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(130px, 0.9fr);
  align-items: center;
  gap: 12px 18px;
  border: 1px solid #ededed;
  box-shadow: none;
}

.donut-column {
  min-width: 0;
  text-align: center;
}

.donut-column small {
  display: block;
  margin-top: -2px;
  color: #aaa;
  font-size: 8px;
}

.representative-column > span {
  color: #858585;
  font-size: 10px;
}

.representative-name {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 9px;
}

.representative-icon {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 38px;
  border-radius: 13px;
  font-size: 16px;
}

.representative-name strong {
  font-size: 20px;
  font-weight: 900;
}

.representative-amount {
  margin-top: 11px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.representative-amount strong {
  font-size: 17px;
  font-weight: 900;
}

.representative-amount span {
  color: #777;
  font-size: 11px;
}

.representative-column p {
  margin: 6px 0 0;
  color: #aaa;
  font-size: 9px;
}

.summary-stats {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  overflow: hidden;
  border: 1px solid #ececec;
  border-radius: 13px;
}

.summary-stats div {
  position: relative;
  min-width: 0;
  padding: 12px 5px;
  text-align: center;
}

.summary-stats div + div::before {
  content: '';
  position: absolute;
  top: 12px;
  bottom: 12px;
  left: 0;
  width: 1px;
  background: #ececec;
}

.summary-stats span,
.summary-stats strong {
  display: block;
}

.summary-stats span {
  color: #8f8f8f;
  font-size: 9px;
}

.summary-stats strong {
  margin-top: 4px;
  overflow: hidden;
  font-size: 11px;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-toggle {
  padding: 0;
  border: 0;
  background: transparent;
  color: #4084ce;
  font-size: 11px;
  font-weight: 800;
}

.category-toggle i {
  margin-left: 4px;
  font-size: 8px;
}

.category-breakdown {
  padding: 3px 15px 12px;
  border: 1px solid #ededed;
  box-shadow: none;
}

.category-row {
  min-height: 68px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f1f1f1;
}

.category-icon {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 38px;
  border-radius: 13px;
  font-size: 14px;
}

.category-info {
  min-width: 0;
  flex: 1;
}

.category-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.category-head strong {
  font-size: 11px;
}

.category-head span {
  color: #858585;
  font-size: 9px;
}

.ratio-track {
  height: 6px;
  margin-top: 8px;
  overflow: hidden;
  border-radius: 8px;
  background: #eff0f2;
}

.ratio-track span {
  display: block;
  height: 100%;
  border-radius: 8px;
}

.category-amount {
  min-width: 76px;
  text-align: right;
}

.category-amount strong,
.category-amount span {
  display: block;
}

.category-amount strong {
  font-size: 10px;
  font-weight: 900;
}

.category-amount span {
  margin-top: 3px;
  color: #a0a0a0;
  font-size: 8px;
}

.category-guide {
  margin: 11px 2px 0;
  color: #999;
  font-size: 9px;
}

.ai-insight {
  position: relative;
  margin-top: 18px;
  padding: 16px 80px 16px 16px;
  overflow: hidden;
  border: 1px solid #bcdcff;
  background: linear-gradient(135deg, #f4f9ff, #eaf5ff);
  box-shadow: none;
}

.ai-insight__label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 999px;
  background: #4f9ded;
  color: #fff;
  font-size: 9px;
  font-weight: 900;
}

.ai-insight p {
  margin: 10px 0 0;
  color: #3e5367;
  font-size: 11px;
  line-height: 1.65;
}

.ai-insight__illustration {
  position: absolute;
  right: 18px;
  bottom: 10px;
  color: #72afe8;
  font-size: 45px;
  opacity: 0.65;
}

.recommendation-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.recommendation-card {
  min-width: 0;
  padding: 14px;
  display: grid;
  grid-template-columns: 40px 1fr 12px;
  align-items: center;
  gap: 9px;
  border: 1px solid #ededed;
  text-align: left;
  box-shadow: none;
}

.recommendation-card > i {
  color: #8d8d8d;
  font-size: 10px;
}

.recommend-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 13px;
  font-size: 17px;
}

.card-icon {
  background: #fff2bf;
  color: #e4a300;
}

.insurance-icon {
  background: #efe8ff;
  color: #8060dd;
}

.recommendation-card strong,
.recommendation-card span {
  display: block;
}

.recommendation-card strong {
  font-size: 12px;
}

.recommendation-card span {
  margin-top: 3px;
  overflow: hidden;
  color: #888;
  font-size: 8px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.analysis-state {
  margin-top: 14px;
  padding: 26px 20px 20px;
  text-align: center;
  border: 1px solid #ededed;
  box-shadow: none;
}

.state-visual {
  width: 84px;
  height: 84px;
  margin: 0 auto 17px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 28px;
  background: #fff5d4;
  color: #e0a000;
  font-size: 38px;
}

.state-visual.has-unclassified {
  background: #fff7e9;
  color: #ed9d3d;
}

.state-visual.ready {
  background: #eaf8f1;
  color: #269866;
}

.analysis-state h2 {
  margin: 0;
  font-size: 19px;
  font-weight: 900;
  line-height: 1.4;
  letter-spacing: -0.6px;
}

.analysis-state > p {
  margin: 10px 0 0;
  color: #777;
  font-size: 10px;
  line-height: 1.7;
}

.classification-progress {
  margin-top: 21px;
  padding: 14px;
  border-radius: 14px;
  background: #fafafa;
  text-align: left;
}

.classification-progress > div:first-child {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.classification-progress strong {
  font-size: 11px;
}

.classification-progress span,
.classification-progress small {
  color: #8d8d8d;
  font-size: 9px;
}

.classification-progress small {
  display: block;
  text-align: right;
}

.analysis-progress {
  height: 7px;
  margin: 9px 0 5px;
  overflow: hidden;
  border-radius: 10px;
  background: #e8e8e8;
}

.analysis-progress span {
  display: block;
  height: 100%;
  border-radius: 10px;
  background: var(--kb-yellow);
}

.analysis-actions {
  margin-top: 14px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.analysis-actions.single {
  grid-template-columns: 1fr;
}

.analysis-actions button {
  padding: 0 10px;
  font-size: 11px;
}

.transaction-count {
  color: #888;
  font-size: 10px;
}

.recent-spending {
  overflow: hidden;
  border: 1px solid #ededed;
  box-shadow: none;
}

.spending-row {
  min-height: 66px;
  padding: 11px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f2f2f2;
}

.spending-row:last-child {
  border-bottom: 0;
}

.spending-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 36px;
  border-radius: 12px;
  background: var(--kb-yellow-soft);
  color: #d99600;
  font-size: 13px;
}

.spending-info {
  min-width: 0;
  flex: 1;
}

.spending-info strong,
.spending-info span {
  display: block;
}

.spending-info strong {
  overflow: hidden;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.spending-info span {
  margin-top: 3px;
  color: #999;
  font-size: 9px;
}

.spending-right {
  text-align: right;
}

.spending-right strong {
  display: block;
  font-size: 11px;
  font-weight: 900;
}

.spending-right button {
  margin-top: 3px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #a27800;
  font-size: 9px;
  font-weight: 700;
}

.spending-right button i {
  margin-left: 2px;
  font-size: 7px;
}

@media (max-width: 420px) {
  .summary-card {
    grid-template-columns: 1fr;
  }

  .representative-column {
    text-align: center;
  }

  .representative-name,
  .representative-amount {
    justify-content: center;
  }

  .summary-stats {
    grid-column: 1;
  }

  .recommendation-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 360px) {
  .analysis-actions {
    grid-template-columns: 1fr;
  }

  .category-amount {
    min-width: 66px;
  }
}
</style>
