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
        </section>

        <section class="kb-section">
          <div class="kb-section-title-row">
            <h2 class="kb-section-title text-20-bold">소비내역</h2>
            <span class="transaction-count text-13">{{ filteredTransactions.length }}건</span>
          </div>

          <div class="transaction-list kb-card">
            <template v-if="pagedTransactions.length">
              <div
                v-for="transaction in pagedTransactions"
                :key="transaction.transactionId"
                class="transaction-row"
              >
                <div class="transaction-icon">
                  <i :class="getCategoryIcon(transaction.parentCategoryName || transaction.categoryName)"></i>
                </div>
                <div class="transaction-info">
                  <strong class="text-15-bold">{{ transaction.transactionLabel || transaction.merchantName || '거래 정보 없음' }}</strong>
                  <span class="text-13">{{ formatAnalysisDateTime(transaction.createdAt) }}</span>
                </div>
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
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import analysisApi from '@/api/analysisApi';
import {
  formatAnalysisDateTime,
  formatAnalysisNumber,
  getAnalysisErrorMessage,
  getCategoryIcon,
} from '@/util/analysis';

const PAGE_SIZE = 10;
const route = useRoute();
const router = useRouter();
const categories = ref([]);
const transactions = ref([]);
const loading = ref(false);
const message = ref('');
const selectedCategoryId = ref('ALL');
const classificationFilter = ref('ALL');
const sortOption = ref('LATEST');
const currentPage = ref(1);

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
  Array.from({ length: totalPages.value }, (_, index) => index + 1),
);

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

watch([selectedCategoryId, classificationFilter, sortOption], () => {
  currentPage.value = 1;
});

watch(totalPages, (pages) => {
  if (currentPage.value > pages) currentPage.value = pages;
});

onMounted(loadPage);
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
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.transaction-filter label {
  min-width: 0;
}

.transaction-filter label > span {
  display: block;
  margin-bottom: 6px;
  color: var(--color-text-sub);
}

.transaction-filter select {
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

.pagination {
  margin-top: 14px;
  display: flex;
  justify-content: center;
  gap: 6px;
}

.pagination button {
  min-width: 34px;
  height: 34px;
  padding: 0 8px;
  border: 1px solid var(--color-border-main);
  border-radius: 8px;
  background: var(--color-bg-page);
  color: var(--color-text-sub);
}

.pagination button.active {
  border-color: var(--color-primary-border);
  background: var(--color-primary);
  color: var(--color-text-main);
}

.pagination button:disabled {
  background: var(--color-bg-disabled);
  color: var(--color-text-disabled);
}

@media (max-width: 380px) {
  .transaction-filter {
    grid-template-columns: 1fr;
  }
}
</style>
