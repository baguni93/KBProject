<template>
  <div class="container py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
      <div>
        <div class="small text-muted mb-1">화면 ID: analysis-004</div>
        <h2 class="mb-1">소비 카테고리 분류</h2>
        <p class="text-muted mb-0">
          미분류 결제 거래를 하나씩 확인하고 카테고리를 지정합니다.
        </p>
      </div>
      <router-link class="btn btn-outline-secondary" to="/analysis">
        분석 화면으로
      </router-link>
    </div>

    <div
      v-if="message"
      :class="['alert', messageType === 'success' ? 'alert-success' : 'alert-danger']"
    >
      {{ message }}
    </div>

    <section class="card mb-4">
      <div class="card-header d-flex justify-content-between align-items-center">
        <span class="fw-bold">분류 대상 거래</span>
        <span class="small text-muted">{{ periodLabel }}</span>
      </div>
      <div v-if="loading" class="card-body text-muted">
        미분류 거래와 카테고리를 불러오고 있습니다.
      </div>
      <div v-else-if="currentTransaction" class="card-body">
        <div class="row g-3">
          <div class="col-md-4">
            <div class="small text-muted">가맹점</div>
            <div class="h5 mb-0">{{ currentTransaction.merchantName || '가맹점 정보 없음' }}</div>
          </div>
          <div class="col-md-4">
            <div class="small text-muted">결제 금액</div>
            <div class="h5 mb-0">
              {{ formatAnalysisNumber(currentTransaction.amount) }}원
            </div>
          </div>
          <div class="col-md-4">
            <div class="small text-muted">결제 일시</div>
            <div class="h5 mb-0">
              {{ formatAnalysisDateTime(currentTransaction.createdAt) }}
            </div>
          </div>
        </div>
        <hr />
        <div>
          남은 미분류 거래:
          <strong>{{ unclassifiedData?.unclassifiedCount ?? 0 }}건</strong>
        </div>
      </div>
      <div v-else class="card-body">
        <div class="alert alert-success mb-3">
          선택한 기간의 미분류 거래를 모두 분류했습니다.
        </div>
        <router-link class="btn btn-warning" to="/analysis">
          분석 가능 여부 확인하기
        </router-link>
      </div>
    </section>

    <section v-if="currentTransaction" class="card">
      <div class="card-header fw-bold">카테고리 선택</div>
      <div class="card-body">
        <p class="text-muted">
          하위 카테고리가 있는 항목은 세부 카테고리 선택 화면으로 이동합니다.
        </p>

        <div v-if="!topCategories.length" class="text-muted">
          선택할 수 있는 소비 카테고리가 없습니다.
        </div>

        <div v-else class="row g-2">
          <div
            v-for="category in topCategories"
            :key="category.spendingCategoryId"
            class="col-6 col-md-3"
          >
            <button
              type="button"
              class="btn btn-outline-dark w-100 h-100 py-3"
              :disabled="classifying"
              @click="selectCategory(category)"
            >
              <span class="d-block fw-semibold">{{ category.categoryName }}</span>
              <small v-if="hasChildren(category.spendingCategoryId)" class="text-muted">
                세부 항목 선택
              </small>
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

const period = ref(normalizeAnalysisPeriod(route.query.period));
const categories = ref([]);
const unclassifiedData = ref(null);
const loading = ref(false);
const classifying = ref(false);
const message = ref('');
const messageType = ref('success');

const currentTransaction = computed(
  () => unclassifiedData.value?.transactions?.[0] ?? null,
);

const topCategories = computed(() =>
  categories.value.filter((category) => category.parentCategoryId == null),
);

const periodLabel = computed(
  () => unclassifiedData.value?.periodLabel ?? `최근 ${period.value}개월`,
);

const childCategories = (parentCategoryId) =>
  categories.value.filter(
    (category) => category.parentCategoryId === parentCategoryId,
  );

const hasChildren = (parentCategoryId) =>
  childCategories(parentCategoryId).length > 0;

const loadData = async () => {
  loading.value = true;
  message.value = '';

  try {
    const [categoryData, transactionData] = await Promise.all([
      analysisApi.getCategories(),
      analysisApi.getUnclassifiedTransactions(period.value),
    ]);

    categories.value = categoryData.categories ?? [];
    unclassifiedData.value = transactionData;

    if (route.query.classified) {
      messageType.value = 'success';
      message.value = `${route.query.classified} 카테고리로 분류했습니다.`;
      await router.replace({
        name: 'analysis-classification',
        query: { period: period.value },
      });
    }
  } catch (error) {
    messageType.value = 'error';
    message.value = getAnalysisErrorMessage(
      error,
      '미분류 거래 정보를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const classifyCurrentTransaction = async (category) => {
  if (!currentTransaction.value) return;

  classifying.value = true;
  message.value = '';

  try {
    const result = await analysisApi.classifyTransaction(
      currentTransaction.value.transactionId,
      category.spendingCategoryId,
    );

    messageType.value = 'success';
    message.value = result.message;
    unclassifiedData.value = await analysisApi.getUnclassifiedTransactions(
      period.value,
    );
  } catch (error) {
    messageType.value = 'error';
    message.value = getAnalysisErrorMessage(
      error,
      '소비 카테고리 분류에 실패했습니다.',
    );
  } finally {
    classifying.value = false;
  }
};

const selectCategory = async (category) => {
  if (hasChildren(category.spendingCategoryId)) {
    await router.push({
      name: 'analysis-subcategory',
      params: { transactionId: currentTransaction.value.transactionId },
      query: {
        period: period.value,
        parentCategoryId: category.spendingCategoryId,
      },
    });
    return;
  }

  await classifyCurrentTransaction(category);
};

onMounted(loadData);
</script>
