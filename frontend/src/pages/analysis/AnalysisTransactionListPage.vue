<template>
  <div class="kb-mobile-page transaction-list-page">
    <PageHeader title="전체 소비내역" />

    <div class="transaction-content">
      <div v-if="loading" class="kb-card kb-loading text-13">
        <div class="spinner-border kb-spinner" role="status"></div>
        <div>전체 소비내역을 불러오는 중이에요.</div>
      </div>

      <template v-else>
        <section class="transaction-filter kb-card">
          <div class="filter-section period-filter-section">
            <span class="filter-section-title text-13-bold">조회 기간</span>

            <div class="period-options" role="group" aria-label="조회 기간 선택">
              <button
                v-for="option in periodOptions"
                :key="option.value"
                type="button"
                class="period-option text-13"
                :class="{ active: selectedPeriod === option.value }"
                @click="selectedPeriod = option.value"
              >
                {{ option.label }}
              </button>
            </div>

            <div v-if="selectedPeriod === 'CUSTOM'" class="custom-period-row">
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
        </section>

        <section class="kb-section">
          <div class="kb-section-title-row">
            <h2 class="kb-section-title text-20-bold">소비내역</h2>
            <span class="transaction-count text-13">{{ filteredTransactions.length }}건</span>
          </div>

          <div class="transaction-list kb-card">
            <template v-if="visibleTransactions.length">
              <div
                v-for="transaction in visibleTransactions"
                :key="transaction.transactionId"
                class="transaction-row"
              >
                <div class="transaction-icon">
                  <i :class="getCategoryIcon(transaction.parentCategoryName || transaction.categoryName)"></i>
                </div>

                <!--
                  거래명/거래일시 영역도 카테고리 수정 진입점으로 사용한다.
                  우측의 카테고리명/연필 버튼과 동일한 수정 화면으로 이동한다.
                -->
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

          <!--
            현재 단계에서는 이미 조회한 데이터를 20건씩 추가 노출한다.
            추후 DB/서버 페이징 적용 시 이 지점을 cursor 기반 다음 조회 호출로 교체하면 된다.
          -->
          <div ref="loadMoreSentinel" class="load-more-sentinel" aria-hidden="true"></div>
          <div v-if="hasMore" class="load-more-message text-13">아래로 스크롤하면 더 불러와요.</div>
        </section>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import analysisApi from '@/api/analysisApi';
import {
  formatAnalysisDateTimeMinute,
  formatAnalysisNumber,
  getAnalysisErrorMessage,
  getCategoryIcon,
} from '@/util/analysis';

const PAGE_SIZE = 20;
const route = useRoute();
const router = useRouter();
const categories = ref([]);
const transactions = ref([]);
const loading = ref(false);
const message = ref('');

const selectedPeriod = ref('12');
const customStartDate = ref('');
const customEndDate = ref('');
const selectedCategoryId = ref('ALL');
const classificationFilter = ref('ALL');
const sortOption = ref('LATEST');
const visibleCount = ref(PAGE_SIZE);
const loadMoreSentinel = ref(null);
let loadMoreObserver = null;

const periodOptions = [
  { value: '1', label: '1개월' },
  { value: '3', label: '3개월' },
  { value: '6', label: '6개월' },
  { value: '12', label: '12개월' },
  { value: 'CUSTOM', label: '직접 선택' },
];

const topCategories = computed(() =>
  categories.value.filter((category) => category.parentCategoryId == null),
);

const parseTransactionDate = (value) => {
  if (!value) return null;
  const parsed = new Date(String(value).replace(' ', 'T'));
  return Number.isNaN(parsed.getTime()) ? null : parsed;
};

const matchesSelectedPeriod = (transaction) => {
  const transactionDate = parseTransactionDate(transaction.createdAt);
  if (!transactionDate) return false;

  if (selectedPeriod.value === 'CUSTOM') {
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

  const months = Number(selectedPeriod.value);
  const end = new Date();
  end.setHours(23, 59, 59, 999);
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
    { rootMargin: '160px 0px' },
  );
  loadMoreObserver.observe(loadMoreSentinel.value);
};

const resetVisibleTransactions = async () => {
  visibleCount.value = PAGE_SIZE;
  await nextTick();
  setupLoadMoreObserver();
};

const loadPage = async () => {
  loading.value = true;
  message.value = '';
  try {
    const [transactionData, categoryData] = await Promise.all([
      analysisApi.getAllTransactions(),
      analysisApi.getCategories(),
    ]);
    transactions.value = transactionData.transactions ?? [];
    categories.value = categoryData.categories ?? [];
  } catch (error) {
    transactions.value = [];
    categories.value = [];
    message.value = getAnalysisErrorMessage(error, '전체 소비내역을 불러오지 못했습니다.');
  } finally {
    loading.value = false;
    await nextTick();
    setupLoadMoreObserver();
  }
};

const goToCategoryEdit = (transaction) =>
  router.push({
    name: 'analysis-category-edit',
    params: { transactionId: transaction.transactionId },
    query: {
      period: 12,
      returnTo: route.fullPath,
    },
  });

watch(
  [selectedPeriod, customStartDate, customEndDate, selectedCategoryId, classificationFilter, sortOption],
  resetVisibleTransactions,
);

onMounted(loadPage);
onBeforeUnmount(() => loadMoreObserver?.disconnect());
</script>

<style scoped>
.transaction-list-page {
  min-height: 100%;
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.transaction-content {
  //margin-top: 14px;
}

.transaction-filter {
  padding: 14px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
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
  gap: 5px;
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
  cursor: pointer;
}

.period-option.active {
  border-color: var(--color-primary-border);
  background: var(--color-primary);
  color: var(--color-text-main);
}

.custom-period-row {
  margin-top: 9px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 7px;
}

.custom-period-row input {
  min-width: 0;
  width: 100%;
  height: 38px;
  padding: 0 7px;
  border: 1px solid var(--color-border-main);
  border-radius: 9px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
}

.custom-period-row span {
  color: var(--color-text-sub);
}

.filter-divider {
  height: 1px;
  margin: 13px 0;
  background: var(--color-divider);
}

.filter-select-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.filter-select-grid label {
  min-width: 0;
}

.filter-select-grid select {
  width: 100%;
  height: 40px;
  padding: 0 8px;
  border: 1px solid var(--color-border-main);
  border-radius: 9px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
}

.transaction-count {
  color: var(--color-text-muted);
}

.transaction-list {
  padding: 3px 15px;
  overflow: hidden;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
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
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #fff3cf;
  color: #d99b00;
  font-size: 14px;
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
  cursor: pointer;
}

.transaction-info strong,
.transaction-info span,
.transaction-right strong {
  display: block;
}

.transaction-info strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.transaction-info span {
  margin-top: 3px;
  color: var(--color-text-muted);
}

.transaction-right {
  text-align: right;
}

.transaction-right button {
  margin-top: 3px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #a27800;
}

.load-more-sentinel {
  height: 1px;
}

.load-more-message {
  padding: 10px 0 0;
  color: var(--color-text-muted);
  text-align: center;
}

@media (max-width: 380px) {
  .filter-select-grid {
    grid-template-columns: 1fr;
  }

  .period-options {
    gap: 4px;
  }

  .period-option {
    padding: 0 2px;
  }
}
</style>
