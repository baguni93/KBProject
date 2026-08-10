<template>
  <div class="kb-mobile-page category-summary-page">
    <PageHeader title="카테고리별 소비" />

    <div class="category-summary-content">

      <div v-if="loading" class="kb-card kb-loading text-13">
        <div class="spinner-border kb-spinner" role="status"></div>
        <div>카테고리별 소비를 불러오는 중이에요.</div>
      </div>

      <template v-else-if="analysis">
        <section class="summary-head kb-card">
          <span class="text-13">{{ analysis.periodLabel }} 분석 결과</span>
          <strong class="text-20-bold">카테고리별 소비 비중</strong>
          <p class="text-13">
            총 {{ formatAnalysisNumber(analysis.totalSpendingAmount) }}원의 소비를 카테고리별로 확인해 보세요.
          </p>
        </section>

        <section class="kb-section category-section">
          <div class="kb-section-title-row">
            <h2 class="kb-section-title text-20-bold">전체 카테고리</h2>
            <span class="category-count text-13">{{ totalPaymentTransactionCount }}건</span>
          </div>

          <div class="category-list kb-card">
            <div
              v-for="(category, index) in sortedCategories"
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
                <span class="text-13">{{ getCategoryTransactionCount(category.spendingCategoryId) }}건</span>
              </div>
            </div>
          </div>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import analysisApi from '@/api/analysisApi';
import {
  formatAnalysisNumber,
  getAnalysisCategoryColor,
  getAnalysisErrorMessage,
  getCategoryIcon,
} from '@/util/analysis';

const route = useRoute();
const analysis = ref(null);
const loading = ref(false);
const message = ref('');
const periodTransactions = ref([]);

const totalPaymentTransactionCount = computed(
  () => periodTransactions.value.length,
);

const sortedCategories = computed(() =>
  [...(analysis.value?.categories ?? [])].sort(
    (left, right) => Number(right.spendingAmount ?? 0) - Number(left.spendingAmount ?? 0),
  ),
);

const categoryColor = (category, index) =>
  getAnalysisCategoryColor(category.categoryName, index);

const formatRatio = (value) => {
  const ratio = Number(value ?? 0);
  return Number.isInteger(ratio) ? ratio : ratio.toFixed(1);
};

const categoryTransactionCountMap = computed(() => {
  const countMap = new Map();

  for (const transaction of periodTransactions.value) {
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

const getCategoryTransactionCount = (spendingCategoryId) =>
  categoryTransactionCountMap.value.get(Number(spendingCategoryId)) ?? 0;

const loadAnalysis = async () => {
  const id = Number(route.params.spendingAnalysisId);
  if (!Number.isInteger(id) || id <= 0) {
    analysis.value = null;
    message.value = '올바른 소비 분석 ID가 필요합니다.';
    return;
  }

  loading.value = true;
  message.value = '';
  try {
    analysis.value = await analysisApi.getAnalysisDetail(id);
    const transactionData = await analysisApi.getTransactions(analysis.value.period);
    periodTransactions.value = transactionData.transactions ?? [];
  } catch (error) {
    analysis.value = null;
    periodTransactions.value = [];
    message.value = getAnalysisErrorMessage(error, '카테고리별 소비를 불러오지 못했습니다.');
  } finally {
    loading.value = false;
  }
};

watch(() => route.params.spendingAnalysisId, loadAnalysis);
onMounted(loadAnalysis);
</script>

<style scoped>
.category-summary-page {
  min-height: 100%;
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.category-summary-content {
  //margin-top: 14px;
}

.summary-head {
  padding: 18px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.summary-head > span {
  color: var(--color-text-sub);
}

.summary-head > strong {
  display: block;
  margin-top: 5px;
}

.summary-head > p {
  margin: 7px 0 0;
  color: var(--color-text-sub);
  line-height: 1.55;
}

.category-section {
  margin-top: 24px;
}

.category-count {
  color: var(--color-text-muted);
}

.category-list {
  padding: 3px 15px;
  overflow: hidden;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.category-row {
  min-height: 70px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--color-divider);
}

.category-row:last-child {
  border-bottom: 0;
}

.category-icon {
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  display: flex;
  align-items: center;
  justify-content: center;
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

.category-head span,
.category-amount span {
  color: var(--color-text-muted);
}

.ratio-track {
  height: 5px;
  margin-top: 7px;
  overflow: hidden;
  border-radius: 8px;
  background: var(--color-bg-disabled);
}

.ratio-track > span {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.category-amount {
  min-width: 92px;
  text-align: right;
}

.category-amount strong,
.category-amount span {
  display: block;
}

.category-amount span {
  margin-top: 3px;
}
</style>
