<template>
  <div class="kb-mobile-page result-page">
    <PageHeader
        title="소비 분석 결과"
        :custom-back="true"
        @back="goToMain"
    />

    <div class="result-content-start">

    <div v-if="loading" class="kb-card kb-loading">
      <div class="spinner-border kb-spinner"></div>
      <div class="text-13">분석 결과를 불러오는 중이에요.</div>
    </div>

    <template v-else-if="analysis">
      <section class="execution-date-card" aria-label="분석 실행 정보">
        <span class="text-13-bold">분석 실행 일시</span>
        <strong class="text-20-bold">{{ formatAnalysisExecutionDate(analysis.createdAt) }}</strong>
        <p class="text-13">{{ analysis.periodLabel }} 소비내역으로 생성된 분석 결과입니다.</p>
      </section>

      <section class="result-hero kb-card">
        <div>
          <span class="ai-label text-13-bold">AI 칭호</span>
          <h2 class="text-20-bold">{{ analysis.aiTitle }}</h2>
          <p class="text-13">{{ analysis.aiAnalysisSummary }}</p>
          <button type="button" class="hero-share-button content-btn secondary" @click="shareResult">
            <i class="fa-solid fa-share-nodes"></i>
            분석 결과 공유하기
          </button>
        </div>
        <div class="hero-icon" aria-hidden="true">
          <i :class="getCategoryIcon(analysis.representativeCategoryName)"></i>
        </div>
      </section>

      <section class="result-summary kb-card">
        <AnalysisDonutChart
            :categories="sortedCategories"
            :total-amount="analysis.totalSpendingAmount"
            variant="detail"
        />

        <div class="summary-meta">
          <div>
            <span class="text-13">대표 카테고리</span>
            <strong class="text-13-bold">{{ analysis.representativeCategoryName }}</strong>
          </div>
          <div>
            <span class="text-13">분석 기간</span>
            <strong class="text-13-bold">{{
                formatAnalysisPeriodRange(analysis.createdAt, analysis.analysisPeriod || analysis.period)
              }}</strong>
          </div>
        </div>

        <div class="summary-stats">
          <div>
            <span class="text-13">총 소비 금액</span>
            <strong class="text-13-bold">{{ formatAnalysisNumber(analysis.totalSpendingAmount) }}원</strong>
          </div>
          <div>
            <span class="text-13">분석 거래</span>
            <strong class="text-13-bold">{{ formatAnalysisNumber(analysis.classifiedTransactionCount) }}건</strong>
          </div>
          <div>
            <span class="text-13">분석 대상</span>
            <strong class="text-13-bold">{{ analysis.periodLabel }}</strong>
          </div>
        </div>
      </section>

      <section class="kb-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">카테고리별 소비</h2>
          <span class="category-count text-13">{{ totalCategoryTransactionCount }}건</span>
        </div>
        <div class="result-list kb-card">
          <div
              v-for="(category, index) in sortedCategories"
              :key="category.spendingCategoryId"
              class="result-row"
          >
            <div
                class="result-category-icon"
                :style="{
                backgroundColor: `${categoryColor(category, index)}20`,
                color: categoryColor(category, index),
              }"
            >
              <i :class="getCategoryIcon(category.categoryName)"></i>
            </div>
            <div class="result-category-info">
              <div>
                <strong class="text-15-bold">{{ category.categoryName }}</strong>
                <span class="text-13">{{ formatRatio(category.spendingRatio) }}%</span>
              </div>
              <div class="result-track">
                <span
                    :style="{
                    width: `${Math.min(Number(category.spendingRatio), 100)}%`,
                    backgroundColor: categoryColor(category, index),
                  }"
                ></span>
              </div>
            </div>
            <div class="result-category-amount">
              <strong class="text-15-bold">{{ formatAnalysisNumber(category.spendingAmount) }}원</strong>
              <span class="text-13">{{ getCategoryTransactionCount(category.spendingCategoryId) }}건</span>
            </div>
          </div>
        </div>
      </section>
<!--      <section class="ai-insight kb-card">-->
<!--        <div class="ai-insight__label">-->
<!--          <i class="fa-solid fa-wand-magic-sparkles"></i> AI 분석-->
<!--        </div>-->
<!--        <p class="text-13">{{ analysis.aiAnalysisSummary }}</p>-->
<!--        <i class="fa-solid fa-chart-pie ai-insight__icon" aria-hidden="true"></i>-->
<!--      </section>-->

      <section class="recommendation-grid">
        <button type="button" class="recommendation-card kb-card" @click="openCardRecommendation">
          <span class="recommendation-icon card"><i class="fa-regular fa-credit-card"></i></span>
          <span><strong class="text-15-bold">카드 추천</strong><small class="text-13">나에게 맞는<br/> 카드 찾기</small></span>
          <i class="fa-solid fa-chevron-right"></i>
        </button>
        <button type="button" class="recommendation-card kb-card" @click="openInsuranceRecommendation">
          <span class="recommendation-icon insurance"><i class="fa-solid fa-shield-heart"></i></span>
          <span><strong class="text-15-bold">보험 추천</strong><small class="text-13">나에게 맞는<br/> 보험 찾기</small></span>
          <i class="fa-solid fa-chevron-right"></i>
        </button>
      </section>

      <section id="all-transactions" class="kb-section transactions-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">전체 소비내역</h2>
          <span class="transaction-count text-13">{{ filteredTransactions.length }}건</span>
        </div>

        <div class="transaction-filter kb-card">
          <div class="filter-section period-filter-section">
            <span class="filter-section-title text-13-bold">조회 기간</span>

            <div class="period-options" role="group" aria-label="조회 기간 선택">
              <button
                  v-for="option in periodOptions"
                  :key="option.value"
                  type="button"
                  class="period-option text-13"
                  :class="{ active: selectedPeriodFilter === option.value }"
                  @click="selectedPeriodFilter = option.value"
              >
                {{ option.label }}
              </button>
            </div>

            <div v-if="selectedPeriodFilter === 'CUSTOM'" class="custom-period-row">
              <input
                  v-model="customStartDate"
                  class="text-13"
                  type="date"
                  aria-label="조회 시작일"
              />
              <span class="text-13">~</span>
              <input
                  v-model="customEndDate"
                  class="text-13"
                  type="date"
                  aria-label="조회 종료일"
              />
            </div>
          </div>

          <div class="filter-divider" aria-hidden="true"></div>

          <div class="filter-select-grid">
            <label>
              <span class="text-13-bold">카테고리</span>
              <select v-model="selectedCategoryId" class="text-13">
                <option value="ALL">전체</option>
                <option
                    v-for="category in topCategories"
                    :key="category.spendingCategoryId"
                    :value="String(category.spendingCategoryId)"
                >
                  {{ category.categoryName }}
                </option>
              </select>
            </label>
            <label>
              <span class="text-13-bold">분류 상태</span>
              <select v-model="classificationFilter" class="text-13">
                <option value="ALL">전체</option>
                <option value="CLASSIFIED">분류 완료</option>
                <option value="UNCLASSIFIED">미분류</option>
              </select>
            </label>
            <label>
              <span class="text-13-bold">정렬</span>
              <select v-model="sortOption" class="text-13">
                <option value="LATEST">최신순</option>
                <option value="OLDEST">과거순</option>
                <option value="AMOUNT_DESC">금액 높은순</option>
                <option value="AMOUNT_ASC">금액 낮은순</option>
              </select>
            </label>
          </div>
        </div>

        <div class="transaction-list kb-card">
          <div v-if="transactionsLoading" class="kb-loading py-4 text-13">
            전체 소비내역을 불러오는 중이에요.
          </div>
          <template v-else-if="visibleTransactions.length">
            <div
                v-for="transaction in visibleTransactions"
                :key="transaction.transactionId"
                class="transaction-row"
            >
              <div class="transaction-icon">
                <i :class="getCategoryIcon(transaction.parentCategoryName || transaction.categoryName)"></i>
              </div>
              <!-- 거래명/거래일시 영역도 우측 연필과 동일하게 카테고리 수정 화면으로 이동한다. -->
              <button
                  type="button"
                  class="transaction-info transaction-info-button"
                  @click="goToCategoryEdit(transaction)"
              >
                <strong class="text-15-bold">{{ transaction.transactionLabel || transaction.merchantName || '거래 정보 없음' }}</strong>
                <span class="text-13">{{ formatAnalysisDateTimeMinute(transaction.createdAt) }}</span>
              </button>
              <div class="transaction-right">
                <strong class="text-15-bold">-{{ formatAnalysisNumber(transaction.amount) }}원</strong>
                <button type="button" class="text-13-bold" @click="goToCategoryEdit(transaction)">
                  {{ transaction.categoryName || '미분류' }}
                  <i class="fa-solid fa-pen"></i>
                </button>
              </div>
            </div>
          </template>
          <div v-else class="kb-empty-state py-4">
            <strong class="text-15-bold">선택한 조건에 맞는 소비내역이 없어요.</strong>
          </div>
        </div>

        <!-- 현재는 조회된 데이터를 추가 노출하고, 추후 cursor 기반 서버 조회로 교체할 수 있는 지점이다. -->
        <div ref="loadMoreSentinel" class="load-more-sentinel" aria-hidden="true"></div>
        <div v-if="hasMore" class="load-more-message text-13">아래로 스크롤하면 더 불러와요.</div>
      </section>

    </template>
    </div>
  </div>
</template>

<script setup>
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import AnalysisDonutChart from '@/components/analysis/AnalysisDonutChart.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import analysisApi from '@/api/analysisApi';
import {
  formatAnalysisDateTimeMinute,
  formatAnalysisExecutionDate,
  formatAnalysisNumber,
  formatAnalysisPeriodRange,
  getAnalysisCategoryColor,
  getAnalysisErrorMessage,
  getCategoryIcon,
} from '@/util/analysis';

const PAGE_SIZE = 10;
const route = useRoute();
const router = useRouter();
const analysis = ref(null);
const categories = ref([]);
const transactions = ref([]);
const loading = ref(false);
const transactionsLoading = ref(false);
const message = ref('');
const messageType = ref('error');
const selectedCategoryId = ref('ALL');
const classificationFilter = ref('ALL');
const sortOption = ref('LATEST');
const selectedPeriodFilter = ref('12');
const customStartDate = ref('');
const customEndDate = ref('');
const visibleCount = ref(PAGE_SIZE);
const loadMoreSentinel = ref(null);
let loadMoreObserver = null;

const periodOptions = [
  {value: '1', label: '1개월'},
  {value: '3', label: '3개월'},
  {value: '6', label: '6개월'},
  {value: '12', label: '12개월'},
  {value: 'CUSTOM', label: '직접 선택'},
];

const sortedCategories = computed(() =>
    [...(analysis.value?.categories ?? [])].sort(
        (left, right) =>
            Number(right.spendingAmount ?? 0) - Number(left.spendingAmount ?? 0),
    ),
);

const topCategories = computed(() =>
    categories.value.filter((category) => category.parentCategoryId == null),
);


const categoryTransactionCountMap = computed(() => {
  const countMap = new Map();

  for (const transaction of transactions.value) {
    const categoryId =
        transaction.parentCategoryId ?? transaction.spendingCategoryId;

    if (categoryId == null) continue;

    const normalizedCategoryId = Number(categoryId);
    countMap.set(
        normalizedCategoryId,
        (countMap.get(normalizedCategoryId) ?? 0) + 1,
    );
  }

  return countMap;
});

const totalCategoryTransactionCount = computed(
    () =>
        transactions.value.filter(
            (transaction) => transaction.spendingCategoryId != null,
        ).length,
);

const getCategoryTransactionCount = (spendingCategoryId) =>
    categoryTransactionCountMap.value.get(Number(spendingCategoryId)) ?? 0;

const parseTransactionDate = (value) => {
  if (!value) return null;
  const parsed = new Date(String(value).replace(' ', 'T'));
  return Number.isNaN(parsed.getTime()) ? null : parsed;
};

const matchesSelectedPeriod = (transaction) => {
  const transactionDate = parseTransactionDate(transaction.createdAt);
  if (!transactionDate) return false;

  if (selectedPeriodFilter.value === 'CUSTOM') {
    if (customStartDate.value) {
      const start = new Date(`${customStartDate.value}T00:00:00`);
      if (transactionDate < start) return false;
    }
    if (customEndDate.value) {
      const end = new Date(`${customEndDate.value}T23:59:59.999`);
      if (transactionDate > end) return false;
    }
    return true;
  }

  const months = Number(selectedPeriodFilter.value);
  const referenceValue = analysis.value?.analysisEndDate || analysis.value?.createdAt;
  const end = referenceValue
      ? new Date(`${String(referenceValue).slice(0, 10)}T23:59:59.999`)
      : new Date();
  const start = new Date(end);
  start.setMonth(start.getMonth() - months);
  start.setHours(0, 0, 0, 0);

  return transactionDate >= start && transactionDate <= end;
};

const filteredTransactions = computed(() => {
  const filtered = transactions.value.filter((transaction) => {
    const normalizedCategoryId =
        transaction.parentCategoryId ?? transaction.spendingCategoryId;
    const categoryMatches =
        selectedCategoryId.value === 'ALL' ||
        Number(selectedCategoryId.value) === Number(normalizedCategoryId);

    const classified = transaction.spendingCategoryId != null;
    const classificationMatches =
        classificationFilter.value === 'ALL' ||
        (classificationFilter.value === 'CLASSIFIED' && classified) ||
        (classificationFilter.value === 'UNCLASSIFIED' && !classified);

    return matchesSelectedPeriod(transaction) && categoryMatches && classificationMatches;
  });

  return filtered.sort((left, right) => {
    if (sortOption.value === 'OLDEST') {
      return new Date(left.createdAt) - new Date(right.createdAt);
    }
    if (sortOption.value === 'AMOUNT_DESC') {
      return Number(right.amount) - Number(left.amount);
    }
    if (sortOption.value === 'AMOUNT_ASC') {
      return Number(left.amount) - Number(right.amount);
    }
    return new Date(right.createdAt) - new Date(left.createdAt);
  });
});

const visibleTransactions = computed(() =>
    filteredTransactions.value.slice(0, visibleCount.value),
);

const hasMore = computed(() =>
    visibleCount.value < filteredTransactions.value.length,
);

const loadMore = () => {
  if (!hasMore.value) return;
  visibleCount.value += PAGE_SIZE;
};

const setupLoadMoreObserver = () => {
  loadMoreObserver?.disconnect();
  loadMoreObserver = null;

  if (!loadMoreSentinel.value || typeof IntersectionObserver === 'undefined') return;

  loadMoreObserver = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) loadMore();
      },
      {rootMargin: '160px 0px'},
  );
  loadMoreObserver.observe(loadMoreSentinel.value);
};

const resetVisibleTransactions = async () => {
  visibleCount.value = PAGE_SIZE;
  await nextTick();
  setupLoadMoreObserver();
};

const categoryColor = (category, index) =>
    getAnalysisCategoryColor(category.categoryName, index);

const formatRatio = (value) => {
  const ratio = Number(value ?? 0);
  return Number.isInteger(ratio) ? ratio : ratio.toFixed(1);
};

const loadTransactions = async () => {
  if (!analysis.value?.period) return;
  transactionsLoading.value = true;
  try {
    const [transactionData, categoryData] = await Promise.all([
      analysisApi.getAnalysisResultTransactions(analysis.value.spendingAnalysisId),
      analysisApi.getCategories(),
    ]);
    transactions.value = transactionData.transactions ?? [];
    categories.value = categoryData.categories ?? [];
  } catch (error) {
    transactions.value = [];
    messageType.value = 'error';
    message.value = getAnalysisErrorMessage(
        error,
        '전체 소비내역을 불러오지 못했습니다.',
    );
  } finally {
    transactionsLoading.value = false;
  }
};

const loadAnalysisDetail = async () => {
  const id = Number(route.params.spendingAnalysisId);
  if (!Number.isInteger(id) || id <= 0) {
    analysis.value = null;
    message.value = '올바른 소비 분석 ID가 필요합니다.';
    return;
  }

  loading.value = true;
  message.value = '';
  messageType.value = 'error';

  try {
    analysis.value = await analysisApi.getAnalysisDetail(id);
    const detailPeriod = String(analysis.value?.period ?? 12);
    selectedPeriodFilter.value = ['1', '3', '6', '12'].includes(detailPeriod)
        ? detailPeriod
        : '12';
    await loadTransactions();
    if (route.query.section === 'transactions') {
      await nextTick();
      document.getElementById('all-transactions')?.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      });
    }
  } catch (error) {
    analysis.value = null;
    message.value = getAnalysisErrorMessage(
        error,
        '소비 분석 상세 결과를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
    await nextTick();
    setupLoadMoreObserver();
  }
};

const goToCategoryEdit = (transaction) =>
    router.push({
      name: 'analysis-category-edit',
      params: {transactionId: transaction.transactionId},
      query: {
        period: analysis.value.period,
        returnTo: route.fullPath,
      },
    });

const goToMain = () =>
    router.push({
      name: 'analysis-main',
      query: {period: analysis.value?.period ?? 1},
    });

const shareResult = () => {
  messageType.value = 'info';
  message.value = '공유 기능은 피드 기능 연결 단계에서 추가할 예정입니다.';
};

const openRecommendationGuide = (type) =>
    router.push({
      name: 'analysis-recommendation-guide',
      query: {
        type,
        period: analysis.value?.period ?? 1,
      },
    });

const openCardRecommendation = () => {
  if (Number(analysis.value?.period) !== 12) {
    openRecommendationGuide('card');
    return;
  }

  router.push({
    name: 'card-recommendation',
    params: {
      spendingAnalysisId: analysis.value.spendingAnalysisId,
    },
  });
};

const openInsuranceRecommendation = () => {
  if (Number(analysis.value?.period) !== 12) {
    // 12개월 소비분석이 아닌 경우 기존 추천 안내 화면을 그대로 재사용한다.
    openRecommendationGuide('insurance');
    return;
  }

  // 12개월 소비분석이면 보험 추천 화면으로 이동한다.
  // 보험 추천 화면 진입 후 비동기 추천 상태 확인 및 추천 생성이 시작된다.
  // 카드 추천과 동일하게 추천 작업 중에는 대기 화면이 표시되고,
  // 완료되면 저장된 보험 추천 결과를 조회해 보여준다.
  router.push({
    name: 'insurance-recommendation',
    params: {
      spendingAnalysisId: analysis.value.spendingAnalysisId,
    },
  });
};

watch(
    [
      selectedPeriodFilter,
      customStartDate,
      customEndDate,
      selectedCategoryId,
      classificationFilter,
      sortOption,
    ],
    resetVisibleTransactions,
);
watch(() => route.params.spendingAnalysisId, loadAnalysisDetail);
onMounted(loadAnalysisDetail);
onBeforeUnmount(() => loadMoreObserver?.disconnect());
</script>

<style scoped>
.result-page {
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.result-content-start {
  /*
   * 팀 협의 후 PageHeader와 첫 콘텐츠 사이 간격을 적용할 경우
   * 아래 주석을 해제합니다.
   * margin-top: 14px;
   */
}

.execution-date-card {
  margin: 14px 0 12px;
  padding: 0 2px
}

.execution-date-card span {
  display: block;
  color: #b37b00;
}

.execution-date-card strong {
  display: block;
  margin-top: 5px;
  color: #222;
  letter-spacing: -.45px
}

.execution-date-card p {
  margin: 5px 0 0;
  color: var(--color-text-sub);
  line-height: 1.5
}

.result-hero {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid #ffe19a;
  background: linear-gradient(135deg, #fffaf0, #fff3cc);
  box-shadow: none
}

.result-hero > div:first-child {
  min-width: 0;
  flex: 1
}

.ai-label {
  display: inline-flex;
  padding: 4px 9px;
  border-radius: 999px;
  background: #ffeab0;
  color: #9b7000;
}

.result-hero h2 {
  margin: 9px 0 7px;
  letter-spacing: -.7px
}

.result-hero p {
  margin: 0;
  display: -webkit-box;
  overflow: hidden;
  color: #746d5c;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 4
}

.hero-share-button {
  width: auto;
  height: 36px;
  margin-top: 11px;
  padding: 0 12px;
  color: var(--color-text-main);
}

.hero-icon {
  width: 54px;
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 54px;
  border-radius: 18px;
  background: rgba(255, 255, 255, .85);
  color: #e5a200;
  font-size: 22px;
  box-shadow: 0 7px 18px rgba(153, 117, 0, .12)
}

.result-summary {
  margin-top: 13px;
  padding: 18px;
  border: 1px solid var(--color-divider);
  box-shadow: none
}

.summary-meta {
  margin-top: 4px;
  display: grid;
  grid-template-columns:1fr 1fr;
  gap: 8px
}

.summary-meta div {
  padding: 11px 12px;
  border-radius: 12px;
  background: #f8f8f8
}

.summary-meta span, .summary-meta strong {
  display: block
}

.summary-meta span {
  color: #8d8d8d;
}

.summary-meta strong {
  margin-top: 4px;
  line-height: 1.4;
  overflow-wrap: anywhere;
  word-break: keep-all
}

.summary-stats {
  margin-top: 9px;
  display: grid;
  grid-template-columns:repeat(3, 1fr);
  overflow: hidden;
  border: 1px solid var(--color-divider);
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
}

.summary-stats strong {
  margin-top: 4px;
  min-width: 0;
  line-height: 1.35;
  overflow-wrap: anywhere;
  word-break: keep-all;
  white-space: normal
}

.category-count, .transaction-count {
  color: #8d8d8d;
}

.result-list {
  padding: 3px 15px;
  border: 1px solid var(--color-divider);
  box-shadow: none
}

.result-row {
  min-height: 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--color-divider)
}

.result-row:last-child {
  border-bottom: 0
}

.result-category-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 36px;
  border-radius: 12px;
  font-size: 14px
}

.result-category-info {
  min-width: 0;
  flex: 1
}

.result-category-info > div:first-child {
  display: flex;
  justify-content: space-between;
  gap: 8px
}

.result-category-info strong {
}

.result-category-info span {
  color: #858585;
}

.result-track {
  height: 5px;
  margin-top: 7px;
  overflow: hidden;
  border-radius: 8px;
  background: var(--color-bg-disabled)
}

.result-track span {
  display: block;
  height: 100%;
  border-radius: 8px
}

.result-category-amount {
  min-width: 72px;
  text-align: right
}

.result-category-amount strong, .result-category-amount span {
  display: block
}

.result-category-amount strong {
}

.result-category-amount span {
  margin-top: 3px;
  color: #9d9d9d;
}

.ai-insight {
  position: relative;
  margin-top: 18px;
  padding: 16px 74px 16px 16px;
  overflow: hidden;
  border: 1px solid #bcdcff;
  background: linear-gradient(135deg, #f4f9ff, #eaf5ff);
  box-shadow: none
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
  font-weight: 900
}

.ai-insight p {
  margin: 10px 0 0;
  color: #3e5367;
  font-size: 11px;
  line-height: 1.65
}

.ai-insight__icon {
  position: absolute;
  right: 18px;
  bottom: 12px;
  color: #72afe8;
  font-size: 42px;
  opacity: .65
}

.recommendation-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns:1fr 1fr;
  gap: 8px
}

.recommendation-card {
  min-height: 68px;
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 9px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  text-align: left;
  box-shadow: none
}

.recommendation-card > span:nth-child(2) {
  min-width: 0;
  flex: 1
}

.recommendation-card strong, .recommendation-card small {
  display: block
}

.recommendation-card strong {
}

.recommendation-card small {
  margin-top: 3px;
  color: var(--color-text-muted);
}

.recommendation-card > i {
  color: var(--color-text-muted);
  font-size: 9px
}

.recommendation-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 36px;
  border-radius: 12px;
  font-size: 15px
}

.recommendation-icon.card {
  background: #fff3cf;
  color: #cf9500
}

.recommendation-icon.insurance {
  background: #f0eaff;
  color: #7c5ed5
}

.transactions-section {
  scroll-margin-top: 10px
}

.transaction-filter {
  padding: 14px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none
}

.filter-section-title,
.filter-select-grid label > span {
  display: block;
  margin-bottom: 7px;
  color: var(--color-text-sub);
}

.period-options {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 5px
}

.period-option {
  min-width: 0;
  height: 36px;
  padding: 0 4px;
  border: 1px solid var(--color-border-main);
  border-radius: 9px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  white-space: nowrap;
  cursor: pointer
}

.period-option.active {
  border-color: var(--color-primary-border);
  background: var(--color-primary);
  color: var(--color-text-main)
}

.custom-period-row {
  margin-top: 9px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 7px
}

.custom-period-row input {
  min-width: 0;
  width: 100%;
  height: 38px;
  padding: 0 7px;
  border: 1px solid var(--color-border-main);
  border-radius: 9px;
  background: var(--color-bg-page);
  color: var(--color-text-main)
}

.custom-period-row span {
  color: var(--color-text-sub)
}

.filter-divider {
  height: 1px;
  margin: 13px 0;
  background: var(--color-divider)
}

.filter-select-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px
}

.filter-select-grid label {
  min-width: 0
}

.filter-select-grid select {
  width: 100%;
  height: 40px;
  padding: 0 8px;
  border: 1px solid var(--color-border-main);
  border-radius: 9px;
  background: var(--color-bg-page);
  color: var(--color-text-main)
}

.transaction-list {
  margin-top: 8px;
  padding: 3px 15px;
  border: 1px solid var(--color-divider);
  box-shadow: none
}

.transaction-row {
  min-height: 65px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--color-divider)
}

.transaction-row:last-child {
  border-bottom: 0
}

.transaction-icon {
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

.transaction-info {
  min-width: 0;
  flex: 1
}

.transaction-info-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer
}

.transaction-info strong, .transaction-info span {
  display: block
}

.transaction-info strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap
}

.transaction-info span {
  margin-top: 3px;
  color: var(--color-text-disabled);
}

.transaction-right {
  text-align: right
}

.transaction-right > strong {
  display: block;
}

.transaction-right button {
  margin-top: 4px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #a27800;
}

.load-more-sentinel {
  height: 1px
}

.load-more-message {
  padding: 10px 0 0;
  color: var(--color-text-muted);
  text-align: center
}

@media (max-width: 420px) {
  .recommendation-grid {
    grid-template-columns:1fr
  }
}

@media (max-width: 380px) {
  .filter-select-grid {
    grid-template-columns:1fr
  }

  .period-options {
    gap: 4px
  }

  .period-option {
    padding: 0 2px
  }
}
</style>
