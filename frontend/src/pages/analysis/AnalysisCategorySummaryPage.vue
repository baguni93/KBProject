<template>
  <div class="kb-mobile-page category-summary-page">
    <PageHeader title="카테고리별 소비" :showBack="true" />

    <main class="category-summary-content">
      <div v-if="loading" class="kb-card kb-loading text-13">
        <div class="spinner-border kb-spinner" role="status"></div>
        <div>카테고리별 소비를 불러오는 중이에요.</div>
      </div>

      <template v-else-if="analysis">
        <section class="summary-card kb-card">
          <div class="summary-top">
            <div>
              <span class="summary-label">{{ analysis.periodLabel }} 소비 요약</span>
              <strong class="summary-amount">{{ formatAnalysisNumber(analysis.totalSpendingAmount) }}원</strong>
            </div>

            <div class="summary-count">
              <i class="fa-regular fa-calendar"></i>
              <span>총 {{ totalPaymentTransactionCount }}건</span>
            </div>
          </div>

          <div v-if="sortedCategories.length" class="top-category">
            <div
                class="top-category-icon"
                :style="{
                backgroundColor: `${categoryColor(sortedCategories[0], 0)}20`,
                color: categoryColor(sortedCategories[0], 0),
              }"
            >
              <i :class="getCategoryIcon(sortedCategories[0].categoryName)"></i>
            </div>

            <div class="top-category-info">
              <span>가장 많이 쓴 카테고리</span>
              <strong>{{ sortedCategories[0].categoryName }}</strong>
            </div>

            <div class="top-category-amount">
              <strong>{{ formatAnalysisNumber(sortedCategories[0].spendingAmount) }}원</strong>
              <span>{{ formatRatio(sortedCategories[0].spendingRatio) }}%</span>
            </div>
          </div>
        </section>

        <section class="category-section">
          <div class="section-header">
            <div>
              <h2>카테고리별 소비</h2>
              <p>소비 금액이 큰 순서로 보여드려요</p>
            </div>

            <span class="category-total">{{ sortedCategories.length }}개</span>
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
                <span>{{ getCategoryTransactionCount(category.spendingCategoryId) }}건</span>
              </div>
            </div>
          </div>
        </section>
      </template>

      <div v-else-if="message" class="kb-card empty-state">
        <p>{{ message }}</p>
      </div>
    </main>
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

const totalPaymentTransactionCount = computed(() => periodTransactions.value.filter((transaction) => transaction.spendingCategoryId != null).length);

const sortedCategories = computed(() =>
    [...(analysis.value?.categories ?? [])].sort(
        (left, right) => Number(right.spendingAmount ?? 0) - Number(left.spendingAmount ?? 0),
    ),
);

const categoryColor = (category, index) => getAnalysisCategoryColor(category.categoryName, index);

const formatRatio = (value) => {
  const ratio = Number(value ?? 0);
  return Number.isInteger(ratio) ? ratio : ratio.toFixed(1);
};

const categoryTransactionCountMap = computed(() => {
  const countMap = new Map();

  for (const transaction of periodTransactions.value) {
    const categoryId = transaction.parentCategoryId ?? transaction.spendingCategoryId;
    if (categoryId == null) continue;

    const normalizedCategoryId = Number(categoryId);
    countMap.set(normalizedCategoryId, (countMap.get(normalizedCategoryId) ?? 0) + 1);
  }

  return countMap;
});

const getCategoryTransactionCount = (spendingCategoryId) => categoryTransactionCountMap.value.get(Number(spendingCategoryId)) ?? 0;

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

.category-summary-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

.category-summary-content {
  padding: 16px 24px 0;
}

.summary-card {
  padding: 18px 16px;
  border: 1px solid var(--color-divider);
  border-radius: 20px;
  background: var(--color-bg-page);
  box-shadow: none;
}

.summary-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.summary-label {
  display: block;
  margin-bottom: 7px;
  color: var(--color-text-sub);
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
}

.summary-amount {
  display: block;
  color: var(--color-text-main);
  font-size: 24px;
  font-weight: 600;
  line-height: 1.25;
  letter-spacing: -0.6px;
}

.summary-count {
  padding-top: 3px;
  display: flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.3;
  white-space: nowrap;
}

.summary-count i {
  font-size: 14px;
}

.top-category {
  margin-top: 18px;
  padding-top: 18px;
  display: flex;
  align-items: center;
  border-top: 1px solid var(--color-divider);
}

.top-category-icon {
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 13px;
  font-size: 15px;
  line-height: 1;
}

.top-category-icon i {
  line-height: 1;
}

.top-category-info {
  min-width: 0;
  flex: 1;
  margin-left: 11px;
}

.top-category-info span {
  display: block;
  margin-bottom: 3px;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.35;
}

.top-category-info strong {
  display: block;
  color: var(--color-text-main);
  font-size: 16px;
  font-weight: 600;
  line-height: 1.3;
}

.top-category-amount {
  margin-left: 12px;
  flex-shrink: 0;
  text-align: right;
}

.top-category-amount strong {
  display: block;
  color: var(--color-text-main);
  font-size: 16px;
  font-weight: 600;
  line-height: 1.3;
}

.top-category-amount span {
  display: block;
  margin-top: 3px;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.3;
}

.category-section {
  margin-top: 28px;
}

.section-header {
  margin-bottom: 12px;
  padding: 0;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.section-header h2 {
  margin: 0;
  color: var(--color-text-main);
  font-size: 18px;
  font-weight: 600;
  line-height: 1.35;
}

.section-header p {
  margin: 5px 0 0;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
}

.category-total {
  padding-bottom: 2px;
  flex-shrink: 0;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 500;
}

.category-list {
  padding: 4px 16px;
  overflow: hidden;
  border: 1px solid var(--color-divider);
  border-radius: 20px;
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

.category-row:last-child {
  border-bottom: 0;
}

.category-icon {
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1;
}

.category-icon i {
  line-height: 1;
}

.category-info {
  min-width: 0;
  flex: 1;
}

.category-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.category-head strong {
  min-width: 0;
  overflow: hidden;
  color: var(--color-text-main);
  font-size: 15px;
  font-weight: 600;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-head span {
  flex-shrink: 0;
  color: var(--color-text-muted);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.3;
}

.ratio-track {
  height: 5px;
  margin-top: 7px;
  overflow: hidden;
  border-radius: 999px;
  background: var(--color-bg-disabled);
}

.ratio-track > span {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.category-amount {
  min-width: 74px;
  flex-shrink: 0;
  text-align: right;
}

.category-amount strong {
  display: block;
  color: var(--color-text-main);
  font-size: 15px;
  font-weight: 600;
  line-height: 1.3;
  white-space: nowrap;
}

.category-amount span {
  display: block;
  margin-top: 1px;
  color: var(--color-text-disabled);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.3;
}

.empty-state {
  padding: 40px 20px;
  color: var(--color-text-sub);
  text-align: center;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
}

@media (max-width: 380px) {
  .category-summary-content {
    padding-right: 20px;
    padding-left: 20px;
  }
}

</style>