<template>
  <div class="kb-mobile-page category-edit-page">
    <PageHeader
        title="카테고리 수정"
        :showBack="true"
        :customBack="true"
        @back="goBack"
    />

    <div class="category-edit-content">

      <div v-if="loading" class="kb-card kb-loading">
        <div class="spinner-border kb-spinner"></div>
        <div class="text-13">거래 정보를 불러오는 중이에요.</div>
      </div>

      <template v-else-if="transaction">
        <section class="edit-target kb-card">
          <div class="edit-icon">
            <i
                :class="getCategoryIcon(
                transaction.parentCategoryName || transaction.categoryName,
              )"
            ></i>
          </div>

          <div class="edit-info">
            <span class="text-13">카테고리를 수정할 거래</span>
            <strong class="text-15-bold">
              {{ transaction.transactionLabel || transaction.merchantName || '거래 정보 없음' }}
            </strong>
            <small class="text-13">
              {{ formatAnalysisDateTime(transaction.createdAt) }}
            </small>
          </div>

          <div class="edit-amount text-15-bold">
            -{{ formatAnalysisNumber(transaction.amount) }}원
          </div>
        </section>

        <div class="current-category text-13">
          현재 카테고리
          <strong class="text-13-bold">
            {{ transaction.categoryName || '미분류' }}
          </strong>
        </div>

        <section class="kb-section">
          <div class="kb-section-title-row">
            <h2 class="kb-section-title text-18-bold">대분류 선택</h2>
          </div>

          <!--
            공용 소비 카테고리 선택 UI.
            이 페이지에서는 기존 동작을 유지하기 위해 대분류 선택만 공용 컴포넌트에 맡기고,
            세부 카테고리가 있는 경우의 라우팅/저장 로직은 이 페이지가 계속 담당합니다.
          -->
          <SpendingCategorySelector
              v-model="selectedTopCategoryId"
              class="edit-category-selector"
              :categories="categories"
              compact
              @select="selectTopCategory"
          />
        </section>

        <button
            type="button"
            class="content-btn primary save-button"
            :disabled="!selectedCategoryId || saving"
            @click="saveCategory"
        >
          {{ saving ? '저장 중...' : '저장' }}
        </button>
      </template>

      <div v-else class="kb-card kb-empty-state">
        <div class="kb-empty-state__icon">
          <i class="fa-solid fa-circle-exclamation"></i>
        </div>
        <strong class="text-15-bold">수정할 거래를 찾지 못했어요.</strong>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
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

const categories = ref([]);
const transaction = ref(null);
const loading = ref(false);
const saving = ref(false);
const message = ref('');
const messageType = ref('success');
const selectedTopCategoryId = ref(null);
const selectedCategoryId = ref(null);

const selectTopCategory = async (category, meta = {}) => {
  selectedTopCategoryId.value = category.spendingCategoryId;

  if (meta.hasChildren) {
    await router.push({
      name: 'analysis-subcategory',
      params: { transactionId },
      query: {
        period,
        parentCategoryId: category.spendingCategoryId,
        mode: 'edit',
        returnTo:
            typeof route.query.returnTo === 'string'
                ? route.query.returnTo
                : '',
      },
    });
    return;
  }

  selectedCategoryId.value = category.spendingCategoryId;
};

const initializeSelection = () => {
  if (!transaction.value?.spendingCategoryId) return;
  selectedTopCategoryId.value =
      transaction.value.parentCategoryId ?? transaction.value.spendingCategoryId;
  selectedCategoryId.value = transaction.value.spendingCategoryId;
};

const loadData = async () => {
  loading.value = true;
  try {
    const [categoryData, transactionData] = await Promise.all([
      analysisApi.getCategories(),
      analysisApi.getTransaction(transactionId),
    ]);
    categories.value = categoryData.categories ?? [];
    transaction.value = transactionData;
    initializeSelection();
  } catch (error) {
    messageType.value = 'error';
    message.value = getAnalysisErrorMessage(
        error,
        '카테고리 수정 정보를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const saveCategory = async () => {
  if (!selectedCategoryId.value) return;
  saving.value = true;
  message.value = '';

  try {
    await analysisApi.classifyTransaction(
        transactionId,
        selectedCategoryId.value,
    );
    messageType.value = 'success';
    message.value = '카테고리가 수정되었습니다.';

    const returnTo =
        typeof route.query.returnTo === 'string' ? route.query.returnTo : null;

    setTimeout(() => {
      if (returnTo) router.push(returnTo);
      else router.push({ name: 'analysis-main', query: { period } });
    }, 400);
  } catch (error) {
    messageType.value = 'error';
    message.value = getAnalysisErrorMessage(
        error,
        '카테고리 수정에 실패했습니다.',
    );
  } finally {
    saving.value = false;
  }
};

const goBack = () => router.back();

onMounted(loadData);
</script>

<style scoped>
.category-edit-page {
  min-height: 100%;
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

/* AnalysisMainPage와 동일한 공통 헤더 정렬 */
.category-edit-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

/* 본문 공통 좌우 여백 */
.category-edit-content {
  padding: 16px 24px 0;
}

/* 거래 요약 카드 */
.edit-target {
  padding: 18px 16px;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: center;
  gap: 11px;
  border: 1px solid var(--color-divider);
  border-radius: 18px;
  background: var(--color-bg-page);
  box-shadow: none;
}

.edit-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 13px;
  background: #fff3cf;
  color: #d79500;
  font-size: 16px;
}

.edit-info {
  min-width: 0;
}

.edit-info span,
.edit-info strong,
.edit-info small {
  display: block;
}

.edit-info span {
  color: var(--color-text-muted);
  font-weight: 500;
}

.edit-info strong {
  margin-top: 3px;
  overflow: hidden;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.edit-info small {
  margin-top: 3px;
  color: var(--color-text-disabled);
  font-weight: 500;
}

.edit-amount {
  color: var(--color-error);
  font-weight: 600;
  white-space: nowrap;
}

/* 현재 카테고리 */
.current-category {
  margin: 12px 0 0;
  color: var(--color-text-muted);
  font-weight: 500;
}

.current-category strong {
  margin-left: 4px;
  color: #9a7200;
  font-weight: 600;
}

/* 공통 kb-section 기본 여백 대신 이 화면 기준으로 정렬 */
.category-edit-page :deep(.kb-section) {
  margin-top: 28px;
  padding: 0;
}

.category-edit-page :deep(.kb-section-title-row) {
  margin-bottom: 12px;
  padding: 0;
}

.category-edit-page :deep(.kb-section-title) {
  font-size: 18px;
  font-weight: 600;
}

.edit-category-selector {
  margin-top: 0;
}

/* 저장 버튼 */
.save-button {
  width: 100%;
  height: 52px;
  margin-top: 20px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
}

@media (max-width: 380px) {
  .category-edit-page :deep(.page-header) {
    padding: 0 20px;
  }

  .category-edit-content {
    padding-right: 20px;
    padding-left: 20px;
  }

  .edit-target {
    grid-template-columns: 40px minmax(0, 1fr) auto;
    padding: 18px 16px;
  }

  .edit-icon {
    width: 40px;
    height: 40px;
  }
}
</style>