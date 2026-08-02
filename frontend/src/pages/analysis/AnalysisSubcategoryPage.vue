<template>
  <div class="container py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
      <div>
        <div class="small text-muted mb-1">화면 ID: analysis-005</div>
        <h2 class="mb-1">세부 카테고리 선택</h2>
        <p class="text-muted mb-0">
          {{ parentCategory?.categoryName ?? '선택한 카테고리' }}의 세부 항목을
          선택합니다.
        </p>
      </div>
      <button type="button" class="btn btn-outline-secondary" @click="goBack">
        이전 화면
      </button>
    </div>

    <div v-if="message" class="alert alert-danger">{{ message }}</div>

    <section class="card mb-4">
      <div class="card-header fw-bold">분류 대상 거래</div>
      <div v-if="loading" class="card-body text-muted">
        거래 정보와 세부 카테고리를 불러오고 있습니다.
      </div>
      <div v-else-if="transaction" class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <div class="small text-muted">가맹점</div>
            <div class="h5 mb-0">{{ transaction.merchantName || '가맹점 정보 없음' }}</div>
          </div>
          <div class="col-md-4">
            <div class="small text-muted">결제 금액</div>
            <div class="h5 mb-0">{{ formatAnalysisNumber(transaction.amount) }}원</div>
          </div>
          <div class="col-md-4">
            <div class="small text-muted">결제 일시</div>
            <div class="h5 mb-0">
              {{ formatAnalysisDateTime(transaction.createdAt) }}
            </div>
          </div>
        </div>
      </div>
      <div v-else class="card-body text-muted">
        현재 분류할 수 있는 거래를 찾지 못했습니다.
      </div>
    </section>

    <section v-if="transaction" class="card">
      <div class="card-header fw-bold">
        {{ parentCategory?.categoryName ?? '세부' }} 카테고리
      </div>
      <div class="card-body">
        <div v-if="!subcategories.length" class="text-muted">
          선택할 수 있는 세부 카테고리가 없습니다.
        </div>
        <div v-else class="row g-2">
          <div
            v-for="category in subcategories"
            :key="category.spendingCategoryId"
            class="col-6 col-md-3"
          >
            <button
              type="button"
              class="btn btn-outline-dark w-100 py-3"
              :disabled="classifying"
              @click="classify(category)"
            >
              {{ category.categoryName }}
            </button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import analysisApi from '@/api/analysisApi';
import {
  formatAnalysisDateTime,
  formatAnalysisNumber,
  getAnalysisErrorMessage,
  normalizeAnalysisPeriod,
} from '@/util/analysis';

const route = useRoute();
const router = useRouter();

const transactionId = Number(route.params.transactionId);
const period = normalizeAnalysisPeriod(route.query.period);
const requestedParentCategoryId = Number(route.query.parentCategoryId);

const categories = ref([]);
const unclassifiedTransactions = ref([]);
const loading = ref(false);
const classifying = ref(false);
const message = ref('');

const transaction = computed(() =>
  unclassifiedTransactions.value.find(
    (item) => item.transactionId === transactionId,
  ),
);

const parentCategory = computed(() => {
  const requestedParent = categories.value.find(
    (category) => category.spendingCategoryId === requestedParentCategoryId,
  );

  if (requestedParent) return requestedParent;

  return categories.value.find((category) =>
    categories.value.some(
      (child) => child.parentCategoryId === category.spendingCategoryId,
    ),
  );
});

const subcategories = computed(() => {
  if (!parentCategory.value) return [];

  return categories.value.filter(
    (category) =>
      category.parentCategoryId === parentCategory.value.spendingCategoryId,
  );
});

const loadData = async () => {
  loading.value = true;
  message.value = '';

  try {
    const [categoryData, transactionData] = await Promise.all([
      analysisApi.getCategories(),
      analysisApi.getUnclassifiedTransactions(period),
    ]);

    categories.value = categoryData.categories ?? [];
    unclassifiedTransactions.value = transactionData.transactions ?? [];
  } catch (error) {
    message.value = getAnalysisErrorMessage(
      error,
      '세부 카테고리 정보를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const classify = async (category) => {
  if (!transaction.value) return;

  classifying.value = true;
  message.value = '';

  try {
    await analysisApi.classifyTransaction(
      transaction.value.transactionId,
      category.spendingCategoryId,
    );

    await router.replace({
      name: 'analysis-classification',
      query: {
        period,
        classified: category.categoryName,
      },
    });
  } catch (error) {
    message.value = getAnalysisErrorMessage(
      error,
      '소비 카테고리 분류에 실패했습니다.',
    );
  } finally {
    classifying.value = false;
  }
};

const goBack = () => {
  router.push({
    name: 'analysis-classification',
    query: { period },
  });
};

onMounted(loadData);
</script>
