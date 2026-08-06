<template>
  <div class="kb-mobile-page result-page">
    <header class="kb-app-header">
      <button class="kb-icon-button" type="button" aria-label="뒤로가기" @click="goToMain">
        <i class="fa-solid fa-chevron-left"></i>
      </button>
      <h1 class="kb-app-header__title">소비 분석 결과</h1>
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

    <div v-if="loading" class="kb-card kb-loading">
      <div class="spinner-border kb-spinner"></div>
      <div>분석 결과를 불러오는 중이에요.</div>
    </div>

    <template v-else-if="analysis">
      <section class="execution-date-card" aria-label="분석 실행 정보">
        <span>분석 실행 일시</span>
        <strong>{{ formatAnalysisExecutionDate(analysis.createdAt) }}</strong>
        <p>{{ analysis.periodLabel }} 소비내역으로 생성된 분석 결과입니다.</p>
      </section>

      <section class="result-hero kb-card">
        <div>
          <span class="ai-label">AI 칭호</span>
          <h2>{{ analysis.aiTitle }}</h2>
          <p>{{ analysis.aiAnalysisSummary }}</p>
        </div>
        <div class="hero-icon" aria-hidden="true">
          <i :class="getCategoryIcon(analysis.representativeCategoryName)"></i>
        </div>
      </section>

      <section class="result-summary kb-card">
        <AnalysisDonutChart
            :categories="sortedCategories"
            :total-amount="analysis.totalSpendingAmount"
        />

        <div class="summary-meta">
          <div>
            <span>대표 카테고리</span>
            <strong>{{ analysis.representativeCategoryName }}</strong>
          </div>
          <div>
            <span>분석 기간</span>
            <strong>{{
                formatAnalysisPeriodRange(analysis.createdAt, analysis.analysisPeriod || analysis.period)
              }}</strong>
          </div>
        </div>

        <div class="summary-stats">
          <div>
            <span>총 소비 금액</span>
            <strong>{{ formatAnalysisNumber(analysis.totalSpendingAmount) }}원</strong>
          </div>
          <div>
            <span>분석 거래</span>
            <strong>{{ formatAnalysisNumber(analysis.classifiedTransactionCount) }}건</strong>
          </div>
          <div>
            <span>분석 대상</span>
            <strong>{{ analysis.periodLabel }}</strong>
          </div>
        </div>
      </section>

      <section class="kb-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title">카테고리별 소비</h2>
          <span class="category-count">{{ sortedCategories.length }}개 카테고리</span>
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
                <strong>{{ category.categoryName }}</strong>
                <span>{{ formatRatio(category.spendingRatio) }}%</span>
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
              <strong>{{ formatAnalysisNumber(category.spendingAmount) }}원</strong>
              <span>{{ category.transactionCount }}건</span>
            </div>
          </div>
        </div>
      </section>

      <section class="ai-insight kb-card">
        <div class="ai-insight__label">
          <i class="fa-solid fa-wand-magic-sparkles"></i> AI 분석
        </div>
        <p>{{ analysis.aiAnalysisSummary }}</p>
        <i class="fa-solid fa-chart-pie ai-insight__icon" aria-hidden="true"></i>
      </section>

      <section class="recommendation-grid">
        <button type="button" class="recommendation-card kb-card" @click="openCardRecommendation">
          <span class="recommendation-icon card"><i class="fa-regular fa-credit-card"></i></span>
          <span><strong>카드 추천</strong><small>나에게 맞는 카드 찾기</small></span>
          <i class="fa-solid fa-chevron-right"></i>
        </button>
        <button type="button" class="recommendation-card kb-card" @click="openInsuranceRecommendation">
          <span class="recommendation-icon insurance"><i class="fa-solid fa-shield-heart"></i></span>
          <span><strong>보험 추천</strong><small>나에게 맞는 보험 찾기</small></span>
          <i class="fa-solid fa-chevron-right"></i>
        </button>
      </section>

      <section id="all-transactions" class="kb-section transactions-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title">전체 소비내역</h2>
          <span class="transaction-count">{{ filteredTransactions.length }}건</span>
        </div>

        <div class="transaction-filter kb-card">
          <label>
            <span>카테고리</span>
            <select v-model="selectedCategoryId">
              <option value="ALL">전체 카테고리</option>
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
            <span>분류 상태</span>
            <select v-model="classificationFilter">
              <option value="ALL">전체</option>
              <option value="CLASSIFIED">분류 완료</option>
              <option value="UNCLASSIFIED">미분류</option>
            </select>
          </label>
          <label>
            <span>정렬</span>
            <select v-model="sortOption">
              <option value="LATEST">최신순</option>
              <option value="OLDEST">과거순</option>
              <option value="AMOUNT_DESC">금액 높은순</option>
              <option value="AMOUNT_ASC">금액 낮은순</option>
            </select>
          </label>
        </div>

        <div class="transaction-list kb-card">
          <div v-if="transactionsLoading" class="kb-loading py-4">
            전체 소비내역을 불러오는 중이에요.
          </div>
          <template v-else-if="pagedTransactions.length">
            <div
                v-for="transaction in pagedTransactions"
                :key="transaction.transactionId"
                class="transaction-row"
            >
              <div class="transaction-icon">
                <i :class="getCategoryIcon(transaction.parentCategoryName || transaction.categoryName)"></i>
              </div>
              <div class="transaction-info">
                <strong>{{ transaction.merchantName || '가맹점 정보 없음' }}</strong>
                <span>{{ formatAnalysisDateTime(transaction.createdAt) }}</span>
              </div>
              <div class="transaction-right">
                <strong>-{{ formatAnalysisNumber(transaction.amount) }}원</strong>
                <button type="button" @click="goToCategoryEdit(transaction)">
                  {{ transaction.categoryName || '미분류' }}
                  <i class="fa-solid fa-pen"></i>
                </button>
              </div>
            </div>
          </template>
          <div v-else class="kb-empty-state py-4">
            <strong>선택한 조건에 맞는 소비내역이 없어요.</strong>
          </div>
        </div>

        <nav v-if="totalPages > 1" class="pagination" aria-label="소비내역 페이지">
          <button type="button" :disabled="currentPage === 1" @click="currentPage -= 1">
            <i class="fa-solid fa-chevron-left"></i>
          </button>
          <button
              v-for="page in pageNumbers"
              :key="page"
              type="button"
              :class="{ active: currentPage === page }"
              @click="currentPage = page"
          >
            {{ page }}
          </button>
          <button type="button" :disabled="currentPage === totalPages" @click="currentPage += 1">
            <i class="fa-solid fa-chevron-right"></i>
          </button>
        </nav>
      </section>

      <div class="result-actions">
        <button type="button" class="kb-outline-button" @click="goToMain">
          분석 메인으로
        </button>
        <button type="button" class="kb-primary-button" @click="shareResult">
          분석 결과 공유하기
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, ref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import AnalysisDonutChart from '@/components/analysis/AnalysisDonutChart.vue';
import analysisApi from '@/api/analysisApi';
import {
  formatAnalysisDateTime,
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
const currentPage = ref(1);

const sortedCategories = computed(() =>
    [...(analysis.value?.categories ?? [])].sort(
        (left, right) =>
            Number(right.spendingAmount ?? 0) - Number(left.spendingAmount ?? 0),
    ),
);

const topCategories = computed(() =>
    categories.value.filter((category) => category.parentCategoryId == null),
);

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

    return categoryMatches && classificationMatches;
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

const totalPages = computed(() =>
    Math.max(Math.ceil(filteredTransactions.value.length / PAGE_SIZE), 1),
);

const pagedTransactions = computed(() => {
  const start = (currentPage.value - 1) * PAGE_SIZE;
  return filteredTransactions.value.slice(start, start + PAGE_SIZE);
});

const pageNumbers = computed(() =>
    Array.from({length: totalPages.value}, (_, index) => index + 1),
);

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
    openRecommendationGuide('insurance');
    return;
  }

  messageType.value = 'info';
  message.value = '보험 추천 기능은 아직 준비 중입니다.';
};

watch(
    [selectedCategoryId, classificationFilter, sortOption],
    () => {
      currentPage.value = 1;
    },
);
watch(totalPages, (pages) => {
  if (currentPage.value > pages) currentPage.value = pages;
});
watch(() => route.params.spendingAnalysisId, loadAnalysisDetail);
onMounted(loadAnalysisDetail);
</script>

<style scoped>
.result-page {
  margin-top: -16px;
  padding-bottom: 34px
}

.execution-date-card {
  margin: 14px 0 12px;
  padding: 0 2px
}

.execution-date-card span {
  display: block;
  color: #b37b00;
  font-size: 10px;
  font-weight: 900
}

.execution-date-card strong {
  display: block;
  margin-top: 5px;
  color: #222;
  font-size: 18px;
  font-weight: 900;
  letter-spacing: -.45px
}

.execution-date-card p {
  margin: 5px 0 0;
  color: #777;
  font-size: 10px;
  line-height: 1.5
}

.result-hero {
  padding: 20px;
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
  font-size: 10px;
  font-weight: 900
}

.result-hero h2 {
  margin: 9px 0 7px;
  font-size: 20px;
  font-weight: 900;
  letter-spacing: -.7px
}

.result-hero p {
  margin: 0;
  display: -webkit-box;
  overflow: hidden;
  color: #746d5c;
  font-size: 11px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 4
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
  border: 1px solid #ededed;
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
  font-size: 9px
}

.summary-meta strong {
  margin-top: 4px;
  font-size: 11px;
  font-weight: 900;
  line-height: 1.4;
  overflow-wrap: anywhere;
  word-break: keep-all
}

.summary-stats {
  margin-top: 9px;
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
  font-size: 8px
}

.summary-stats strong {
  margin-top: 4px;
  min-width: 0;
  font-size: 10px;
  font-weight: 900;
  line-height: 1.35;
  overflow-wrap: anywhere;
  word-break: keep-all;
  white-space: normal
}

.category-count, .transaction-count {
  color: #8d8d8d;
  font-size: 10px
}

.result-list {
  padding: 3px 15px;
  border: 1px solid #ededed;
  box-shadow: none
}

.result-row {
  min-height: 68px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f1f1f1
}

.result-row:last-child {
  border-bottom: 0
}

.result-category-icon {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 38px;
  border-radius: 13px;
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
  font-size: 13px
}

.result-category-info span {
  color: #858585;
  font-size: 11px
}

.result-track {
  height: 6px;
  margin-top: 8px;
  overflow: hidden;
  border-radius: 8px;
  background: #eff0f2
}

.result-track span {
  display: block;
  height: 100%;
  border-radius: 8px
}

.result-category-amount {
  min-width: 76px;
  text-align: right
}

.result-category-amount strong, .result-category-amount span {
  display: block
}

.result-category-amount strong {
  font-size: 10px;
  font-weight: 900
}

.result-category-amount span {
  margin-top: 3px;
  color: #9d9d9d;
  font-size: 8px
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
  border: 1px solid #ededed;
  background: #fff;
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
  font-size: 11px
}

.recommendation-card small {
  margin-top: 3px;
  color: #999;
  font-size: 8px
}

.recommendation-card > i {
  color: #999;
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
  padding: 12px;
  display: grid;
  grid-template-columns:repeat(3, 1fr);
  gap: 8px;
  border: 1px solid #ededed;
  box-shadow: none
}

.transaction-filter label span {
  display: block;
  margin-bottom: 5px;
  color: #888;
  font-size: 8px
}

.transaction-filter select {
  width: 100%;
  height: 34px;
  padding: 0 8px;
  border: 1px solid #dedede;
  border-radius: 9px;
  background: #fff;
  color: #444;
  font-size: 9px
}

.transaction-list {
  margin-top: 8px;
  padding: 3px 15px;
  border: 1px solid #ededed;
  box-shadow: none
}

.transaction-row {
  min-height: 66px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f2f2f2
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

.transaction-info strong, .transaction-info span {
  display: block
}

.transaction-info strong {
  overflow: hidden;
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap
}

.transaction-info span {
  margin-top: 3px;
  color: #aaa;
  font-size: 8px
}

.transaction-right {
  text-align: right
}

.transaction-right > strong {
  display: block;
  font-size: 10px
}

.transaction-right button {
  margin-top: 3px;
  border: 0;
  background: transparent;
  color: #a27800;
  font-size: 8px
}

.pagination {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px
}

.pagination button {
  width: 30px;
  height: 30px;
  border: 1px solid #e1e1e1;
  border-radius: 9px;
  background: #fff;
  color: #666;
  font-size: 9px
}

.pagination button.active {
  border-color: var(--kb-yellow);
  background: var(--kb-yellow);
  color: #222;
  font-weight: 900
}

.pagination button:disabled {
  opacity: .35
}

.result-actions {
  margin-top: 17px;
  display: grid;
  grid-template-columns:1fr 1fr;
  gap: 9px
}

.result-actions button {
  font-size: 11px
}

@media (max-width: 420px) {
  .transaction-filter {
    grid-template-columns:1fr
  }

  .recommendation-grid {
    grid-template-columns:1fr
  }
}

@media (max-width: 360px) {
  .result-actions {
    grid-template-columns:1fr
  }
}
</style>
