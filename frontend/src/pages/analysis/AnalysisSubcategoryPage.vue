<template>
  <div class="kb-mobile-page subcategory-page">
    <PageHeader
      title="세부 카테고리 선택"
      :custom-back="true"
      @back="goBack"
    />

    <div class="subcategory-content">

      <div v-if="loading" class="kb-card kb-loading">
        <div class="spinner-border kb-spinner"></div>
        <div class="text-13">세부 카테고리를 불러오는 중이에요.</div>
      </div>

      <template v-else-if="transaction">
        <section class="merchant-card kb-card">
          <div class="merchant-icon">
            <i :class="getCategoryIcon(parentCategory?.categoryName)"></i>
          </div>
          <div class="merchant-info">
            <span class="text-13">{{ parentCategory?.categoryName }} 세부 분류</span>
            <strong class="text-15-bold">
              {{ transaction.transactionLabel || transaction.merchantName || '거래 정보 없음' }}
            </strong>
            <small class="text-13">
              {{ formatAnalysisDateTime(transaction.createdAt) }}
            </small>
          </div>
          <strong class="amount text-15-bold">
            -{{ formatAnalysisNumber(transaction.amount) }}원
          </strong>
        </section>

        <section class="kb-section">
          <div class="kb-section-title-row">
            <h2 class="kb-section-title text-20-bold">
              {{ subcategoryQuestion }}
            </h2>
          </div>

          <div class="subcategory-grid kb-card">
            <button
              v-for="category in subcategories"
              :key="category.spendingCategoryId"
              type="button"
              :class="{ selected: selectedCategoryId === category.spendingCategoryId }"
              @click="selectedCategoryId = category.spendingCategoryId"
            >
              <div>
                <i :class="getCategoryIcon(category.categoryName)"></i>
              </div>
              <span class="text-13-bold">{{ category.categoryName }}</span>
            </button>
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

      <div v-else class="kb-card kb-empty-state">
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
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.subcategory-content {
  /*
   * 팀 협의 후 헤더와 첫 콘텐츠 사이 간격을 적용할 경우
   * 아래 주석을 해제합니다.
   * margin-top: 14px;
   */
}

.merchant-card {
  padding: 15px;
  display: grid;
  grid-template-columns: 44px 1fr auto;
  align-items: center;
  gap: 11px;
  box-shadow: none;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
}

.merchant-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff3cf;
  color: #d99700;
  font-size: 18px;
}

.merchant-info {
  min-width: 0;
}

.merchant-info span,
.merchant-info strong,
.merchant-info small {
  display: block;
}

.merchant-info span {
  color: var(--color-text-muted);
}

.merchant-info strong {
  margin-top: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.merchant-info small {
  margin-top: 3px;
  color: var(--color-text-disabled);
}

.merchant-card .amount {
  color: var(--color-error);
}

.subcategory-grid {
  padding: 12px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 9px;
  box-shadow: none;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
}

.subcategory-grid button {
  min-height: 76px;
  border: 1px solid transparent;
  border-radius: 13px;
  background: #fafafa;
  color: #555;
}

.subcategory-grid button.selected {
  border-color: var(--color-primary);
  background: #fff7d8;
  color: #8e6900;
}

.subcategory-grid button div {
  font-size: 19px;
}

.subcategory-grid button span {
  display: block;
  margin-top: 5px;
}

.complete-button {
  margin-top: 16px;
}
</style>
