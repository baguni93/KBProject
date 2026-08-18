<template>
  <div class="kb-mobile-page result-page">
    <PageHeader
        title="소비 분석 결과"
        :showBack="true"
        :customBack="true"
        @back="goToMain"
    />

    <div class="result-content-start">
      <div v-if="loading" class="kb-card kb-loading">
        <div class="spinner-border kb-spinner"></div>
        <div class="text-13">분석 결과를 불러오는 중이에요.</div>
      </div>

      <template v-else-if="analysis">
        <div class="analysis-meta text-13">
          <span>{{ formatAnalysisExecutionDate(analysis.createdAt) }}</span>
          <span class="analysis-meta-divider">·</span>
          <span>{{ analysis.periodLabel }} 분석</span>
        </div>

        <section class="result-hero kb-card">
          <div class="hero-top">
            <span class="ai-label text-13-bold">AI 칭호</span>

            <button
                type="button"
                class="hero-share-button"
                @click="shareResult"
            >
              <i class="fa-solid fa-share-nodes"></i>
              <span>공유</span>
            </button>
          </div>

          <div class="hero-main">
            <div class="hero-copy">
              <h2 class="text-20-bold">{{ analysis.aiTitle }}</h2>
              <p class="text-13">{{ analysis.aiAnalysisSummary }}</p>
            </div>

            <div class="hero-icon" aria-hidden="true">
              <i :class="getCategoryIcon(analysis.representativeCategoryName)"></i>
            </div>
          </div>
        </section>

        <AnalysisSummaryCard
            :categories="sortedCategories"
            :total-amount="analysis.totalSpendingAmount"
            :transaction-count="analysis.classifiedTransactionCount"
            :representative-category-id="analysis.representativeCategoryId"
            :representative-category-name="analysis.representativeCategoryName"
            :period="analysis.analysisPeriod || analysis.period"
            :start-date="analysis.analysisStartDate"
            :end-date="analysis.analysisEndDate"
        />

        <section class="result-section">
          <div class="section-title-row">
            <h2>카테고리별 소비</h2>
            <button type="button" class="section-more" @click="goToCategorySummary">
              전체보기
              <i class="fa-solid fa-chevron-right"></i>
            </button>
          </div>

          <div class="result-list kb-card">
            <div
                v-for="(category, index) in sortedCategories.slice(0, 3)"
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
                <div class="result-category-head">
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

        <section class="result-section">
          <div class="section-title-row">
            <h2>추천 서비스</h2>
          </div>

          <div class="recommendation-list kb-card">
            <button type="button" class="recommendation-row" @click="openCardRecommendation">
              <span class="recommendation-icon card">
                <i class="fa-regular fa-credit-card"></i>
              </span>

              <span class="recommendation-copy">
                <strong class="text-15-bold">카드 추천</strong>
                <small class="text-13">소비 패턴에 맞는 카드를 확인해보세요</small>
              </span>

              <i class="fa-solid fa-chevron-right"></i>
            </button>

            <button type="button" class="recommendation-row" @click="openInsuranceRecommendation">
              <span class="recommendation-icon insurance">
                <i class="fa-solid fa-shield-heart"></i>
              </span>

              <span class="recommendation-copy">
                <strong class="text-15-bold">보험 추천</strong>
                <small class="text-13">내 소비에 맞는 보험을 확인해보세요</small>
              </span>

              <i class="fa-solid fa-chevron-right"></i>
            </button>
          </div>
        </section>

        <section class="result-section">
          <div class="section-title-row">
            <h2>최근 소비내역</h2>
            <button type="button" class="section-more" @click="goToTransactionList">
              전체보기
              <i class="fa-solid fa-chevron-right"></i>
            </button>
          </div>

          <div class="transaction-list kb-card">
            <div v-if="transactionsLoading" class="kb-loading py-4 text-13">
              소비내역을 불러오는 중이에요.
            </div>

            <template v-else-if="recentTransactions.length">
              <div
                  v-for="transaction in recentTransactions"
                  :key="transaction.transactionId"
                  class="transaction-row"
              >
                <div class="transaction-icon">
                  <i :class="getCategoryIcon(transaction.parentCategoryName || transaction.categoryName)"></i>
                </div>

                <button
                    type="button"
                    class="transaction-info transaction-info-button"
                    @click="goToCategoryEdit(transaction)"
                >
                  <strong class="text-15-bold">
                    {{ transaction.transactionLabel || transaction.merchantName || '거래 정보 없음' }}
                  </strong>
                  <span class="text-13">{{ formatAnalysisDateTimeMinute(transaction.createdAt) }}</span>
                </button>

                <div class="transaction-right">
                  <strong class="text-15-bold">-{{ formatAnalysisNumber(transaction.amount) }}원</strong>
                  <button type="button" class="text-13" @click="goToCategoryEdit(transaction)">
                    {{ transaction.categoryName || '미분류' }}
                    <i class="fa-solid fa-pen"></i>
                  </button>
                </div>
              </div>
            </template>

            <div v-else class="kb-empty-state py-4">
              <strong class="text-15-bold">소비내역이 없어요.</strong>
            </div>
          </div>
        </section>
      </template>

      <div v-else-if="message" class="kb-card result-message text-13">
        {{ message }}
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import AnalysisSummaryCard from '@/components/common/AnalysisSummaryCard.vue';
import PageHeader from '@/components/common/PageHeader.vue';
import analysisApi from '@/api/analysisApi';
import {
  formatAnalysisDateTimeMinute,
  formatAnalysisExecutionDate,
  formatAnalysisNumber,
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

const recentTransactions = computed(() =>
    [...transactions.value]
        .sort((left, right) => new Date(right.createdAt) - new Date(left.createdAt))
        .slice(0, 3),
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


const goToCategorySummary = () => {
  const id = analysis.value?.spendingAnalysisId;
  if (!id) return;
  router.push(`/analysis/result/${id}/categories`);
};

const goToTransactionList = () => {
  router.push({
    name: 'analysis-transactions',
    query: {
      period: analysis.value?.period ?? 1,
    },
  });
};

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
  min-height: 100%;
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.result-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

.result-content-start {
  padding: 16px 24px 0;
}

.analysis-meta {
  padding: 0 2px 10px;
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--color-text-muted);
}

.analysis-meta-divider {
  color: var(--color-text-disabled);
}

.result-hero {
  padding: 18px 16px;
  border: 1px solid #ffe19a;
  border-radius: 18px;
  background: linear-gradient(135deg, #fffaf0, #fff3cc);
  box-shadow: none;
}

.hero-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.ai-label {
  display: inline-flex;
  align-items: center;
  padding: 4px 9px;
  border-radius: 999px;
  background: #ffeab0;
  color: #9b7000;
}

.hero-share-button {
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
}

.hero-share-button i {
  font-size: 12px;
}

.hero-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.hero-copy {
  min-width: 0;
  flex: 1;
}

.result-hero h2 {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: -0.5px;
}

.result-hero p {
  margin: 0;
  color: #746d5c;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.5;
  word-break: keep-all;
}

.hero-icon {
  width: 50px;
  height: 50px;
  flex: 0 0 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.88);
  color: #e5a200;
  font-size: 20px;
}

.result-section {
  margin-top: 28px;
}

.section-title-row {
  padding: 0;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-title-row h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.35;
}

.section-more {
  padding: 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 0;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 600;
}

.section-more i {
  font-size: 10px;
}

.result-list,
.transaction-list,
.recommendation-list {
  overflow: hidden;
  border: 1px solid var(--color-divider);
  border-radius: 18px;
  background: var(--color-bg-page);
  box-shadow: none;
}

.result-list {
  padding: 4px 16px;
}

.result-row {
  min-height: 68px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--color-divider);
}

.result-row:last-child {
  border-bottom: 0;
}

.result-category-icon {
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 14px;
}

.result-category-info {
  min-width: 0;
  flex: 1;
}

.result-category-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.result-category-head span {
  color: var(--color-text-muted);
}

.result-track {
  height: 5px;
  margin-top: 7px;
  overflow: hidden;
  border-radius: 999px;
  background: var(--color-bg-disabled);
}

.result-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.result-category-amount {
  min-width: 74px;
  flex-shrink: 0;
  text-align: right;
}

.result-category-amount strong,
.result-category-amount span {
  display: block;
}

.result-category-amount span {
  margin-top: 1px;
  color: var(--color-text-disabled);
}

.recommendation-list {
  padding: 4px 16px;
}

.recommendation-row {
  width: 100%;
  min-height: 68px;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 0;
  border-bottom: 1px solid var(--color-divider);
  background: transparent;
  color: inherit;
  text-align: left;
}

.recommendation-row:last-child {
  border-bottom: 0;
}

.recommendation-icon {
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 15px;
}

.recommendation-icon.card {
  background: #fff3cf;
  color: #cf9500;
}

.recommendation-icon.insurance {
  background: #f0eaff;
  color: #7c5ed5;
}

.recommendation-copy {
  min-width: 0;
  flex: 1;
}

.recommendation-copy strong,
.recommendation-copy small {
  display: block;
}

.recommendation-copy small {
  margin-top: 3px;
  color: var(--color-text-muted);
}

.recommendation-row > i {
  color: var(--color-text-muted);
  font-size: 10px;
}

.transaction-list {
  padding: 4px 16px;
}

.transaction-row {
  min-height: 68px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--color-divider);
}

.transaction-row:last-child {
  border-bottom: 0;
}

.transaction-icon {
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #fff3cf;
  color: #d99b00;
  font-size: 13px;
}

.transaction-info {
  min-width: 0;
  flex: 1;
}

.transaction-info-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  text-align: left;
}

.transaction-info strong,
.transaction-info span {
  display: block;
}

.transaction-info strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.transaction-info span {
  margin-top: 1px;
  color: var(--color-text-disabled);
}

.transaction-right {
  flex-shrink: 0;
  text-align: right;
}

.transaction-right > strong {
  display: block;
}

.transaction-right button {
  margin-top: 1px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #a27800;
}

.result-message {
  margin-top: 14px;
  padding: 20px;
  color: var(--color-text-sub);
  text-align: center;
}

@media (max-width: 380px) {
  .result-page :deep(.page-header) {
    padding: 0 20px;
  }

  .result-content-start {
    padding-right: 20px;
    padding-left: 20px;
  }
}
</style>