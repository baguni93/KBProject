<template>
  <div class="kb-mobile-page subcategory-page">
    <PageHeader
        title="세부 카테고리 선택"
        :custom-back="true"
        @back="goBack"
    />

    <div class="subcategory-content">
      <div v-if="loading" class="kb-card kb-loading loading-card">
        <div class="spinner-border kb-spinner"></div>
        <div class="text-13">세부 카테고리를 불러오는 중이에요.</div>
      </div>

      <template v-else-if="transaction">
        <section class="target-card kb-card">
          <div class="target-card__top">
            <span class="target-label text-13-bold">분류할 거래</span>
            <span class="parent-chip text-13-bold">
              {{ parentCategory?.categoryName || '카테고리' }}
            </span>
          </div>

          <div class="target-card__body">
            <div class="target-icon" aria-hidden="true">
              <i :class="getCategoryIcon(parentCategory?.categoryName)"></i>
            </div>

            <div class="target-info">
              <strong class="text-18-bold">
                {{ transaction.transactionLabel || transaction.merchantName || '거래 정보 없음' }}
              </strong>
              <span class="text-13">
                {{ formatAnalysisDateTime(transaction.createdAt) }}
              </span>
            </div>

            <strong class="target-amount text-18-bold">
              -{{ formatAnalysisNumber(transaction.amount) }}원
            </strong>
          </div>

          <p class="target-help text-13">
            {{ parentCategory?.categoryName || '선택한 카테고리' }} 안에서
            가장 알맞은 세부 항목을 선택해 주세요.
          </p>
        </section>

        <section class="subcategory-section">
          <div class="subcategory-section__head">
            <h2 class="text-20-bold">{{ subcategoryQuestion }}</h2>
            <p class="text-13">하나를 선택한 뒤 아래 버튼을 눌러 분류해 주세요.</p>
          </div>

          <div class="selector-card kb-card">
            <SpendingCategorySelector
                v-model="selectedCategoryId"
                :categories="categories"
                :parent-category-id="requestedParentCategoryId"
                :columns="2"
                compact
            />
          </div>
        </section>

        <button
            type="button"
            class="content-btn primary complete-button"
            :disabled="!selectedCategoryId || classifying"
            @click="classifySelected"
        >
          {{ classifying ? actionLoadingLabel : actionLabel }}
        </button>
      </template>

      <div v-else class="kb-card kb-empty-state empty-card">
        <div class="kb-empty-state__icon">
          <i class="fa-solid fa-circle-exclamation"></i>
        </div>
        <strong class="text-15-bold">분류할 거래를 찾지 못했어요.</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import SpendingCategorySelector from '@/components/common/SpendingCategorySelector.vue';
import analysisApi from '@/api/analysisApi';
import {
  formatAnalysisDateTime,
  formatAnalysisNumber,
  getAnalysisErrorMessage,
  getCategoryIcon,
  normalizeAnalysisPeriod,
} from '@/util/analysis';

const route = useRoute();
const router = useRouter();
const transactionId = Number(route.params.transactionId);
const period = normalizeAnalysisPeriod(route.query.period);
const requestedParentCategoryId = Number(route.query.parentCategoryId);
const isEditMode = computed(() => route.query.mode === 'edit');

const categories = ref([]);
const transactionData = ref(null);
const loading = ref(false);
const classifying = ref(false);
const message = ref('');
const selectedCategoryId = ref(null);

const parentCategory = computed(() =>
    categories.value.find(
        (category) => category.spendingCategoryId === requestedParentCategoryId,
    ),
);

const subcategories = computed(() =>
    parentCategory.value
        ? categories.value.filter(
            (category) =>
                category.parentCategoryId === parentCategory.value.spendingCategoryId,
        )
        : [],
);

const transaction = computed(() => transactionData.value);

const subcategoryQuestion = computed(() =>
    parentCategory.value?.categoryName === '병원'
        ? '어떤 병원에 방문했나요?'
        : `${parentCategory.value?.categoryName || '카테고리'} 세부 항목을 선택해 주세요`,
);

const actionLabel = computed(() =>
    isEditMode.value ? '카테고리 수정 완료' : '분류 완료',
);

const actionLoadingLabel = computed(() =>
    isEditMode.value ? '수정 중...' : '분류 중...',
);

const loadData = async () => {
  loading.value = true;
  message.value = '';

  try {
    const categoryPromise = analysisApi.getCategories();
    const transactionPromise = isEditMode.value
        ? analysisApi.getTransaction(transactionId)
        : analysisApi.getUnclassifiedTransactions(period);

    const [categoryData, transactionResult] = await Promise.all([
      categoryPromise,
      transactionPromise,
    ]);

    categories.value = categoryData.categories ?? [];

    if (isEditMode.value) {
      transactionData.value = transactionResult;
      const currentCategoryId = Number(transactionResult?.spendingCategoryId);
      if (
          subcategories.value.some(
              (category) => category.spendingCategoryId === currentCategoryId,
          )
      ) {
        selectedCategoryId.value = currentCategoryId;
      }
    } else {
      transactionData.value =
          (transactionResult.transactions ?? []).find(
              (item) => item.transactionId === transactionId,
          ) ?? null;
    }
  } catch (error) {
    message.value = getAnalysisErrorMessage(
        error,
        '세부 카테고리 정보를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const classifySelected = async () => {
  const category = subcategories.value.find(
      (item) => item.spendingCategoryId === selectedCategoryId.value,
  );
  if (!category || !transaction.value) return;

  classifying.value = true;
  message.value = '';

  try {
    await analysisApi.classifyTransaction(
        transaction.value.transactionId,
        category.spendingCategoryId,
    );

    if (isEditMode.value) {
      const returnTo =
          typeof route.query.returnTo === 'string' && route.query.returnTo
              ? route.query.returnTo
              : null;

      if (returnTo) {
        await router.replace(returnTo);
      } else {
        await router.replace({
          name: 'analysis-main',
          query: { period },
        });
      }
      return;
    }

    await router.replace({
      name: 'analysis-classification',
      query: {
        period,
        classified: category.categoryName,
        returnTo: route.query.returnTo || 'analysis-check',
      },
    });
  } catch (error) {
    message.value = getAnalysisErrorMessage(
        error,
        isEditMode.value
            ? '카테고리 수정에 실패했습니다.'
            : '소비 카테고리 분류에 실패했습니다.',
    );
  } finally {
    classifying.value = false;
  }
};

const goBack = () => {
  if (isEditMode.value) {
    router.push({
      name: 'analysis-category-edit',
      params: { transactionId },
      query: {
        period,
        returnTo:
            typeof route.query.returnTo === 'string'
                ? route.query.returnTo
                : '',
      },
    });
    return;
  }

  router.push({
    name: 'analysis-classification',
    query: {
      period,
      returnTo: route.query.returnTo || 'analysis-check',
    },
  });
};

onMounted(loadData);
</script>

<style scoped>
.subcategory-page {
  min-height: 100vh;
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

/* 공통 헤더는 수정하지 않고 이 화면에서만 위치를 맞춤 */
.subcategory-page :deep(.page-header) {
  padding: 0 24px;
}

.subcategory-content {
  padding: 24px;
}

.loading-card {
  padding: 40px 20px;
}

.target-card {
  padding: 20px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.target-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.target-label {
  color: var(--color-text-sub);
}

.parent-chip {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  background: #fff3cf;
  color: #9b7000;
  white-space: nowrap;
}

.target-card__body {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
}

.target-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: #fff3cf;
  color: #d99500;
  font-size: 18px;
}

.target-info {
  min-width: 0;
}

.target-info strong,
.target-info span {
  display: block;
}

.target-info strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.target-info span {
  margin-top: 5px;
  color: var(--color-text-disabled);
}

.target-amount {
  color: var(--color-error);
  white-space: nowrap;
}

.target-help {
  margin: 18px 0 0;
  padding-top: 16px;
  border-top: 1px solid var(--color-divider);
  color: var(--color-text-sub);
  line-height: 1.55;
}

.subcategory-section {
  margin-top: 28px;
}

.subcategory-section__head {
  margin-bottom: 12px;
}

.subcategory-section__head h2,
.subcategory-section__head p {
  margin: 0;
}

.subcategory-section__head p {
  margin-top: 5px;
  color: var(--color-text-sub);
}

.selector-card {
  padding: 12px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

/* 이 화면에서만 세부 카테고리 버튼 높이와 텍스트 밀도를 정리 */
.selector-card :deep(button) {
  min-height: 88px;
  border-radius: 12px;
  font-size: 15px;
}

.complete-button {
  width: 100%;
  min-height: 52px;
  margin-top: 20px;
  font-size: 16px;
  font-weight: 600;
}

.empty-card {
  padding: 40px 20px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

@media (max-width: 380px) {
  .subcategory-content {
    padding: 20px;
  }

  .target-card__body {
    grid-template-columns: 44px minmax(0, 1fr);
  }

  .target-icon {
    width: 44px;
    height: 44px;
  }

  .target-amount {
    grid-column: 2;
    margin-top: -2px;
  }
}
</style>