<template>
  <div class="kb-mobile-page category-edit-page">
    <PageHeader
      title="카테고리 수정"
      :custom-back="true"
      @back="goBack"
    />

    <div class="category-edit-content">
      <div
        v-if="message"
        :class="[
          'kb-toast',
          messageType === 'success' ? 'kb-toast--success' : 'kb-toast--error',
        ]"
      >
        {{ message }}
      </div>

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
              {{ transaction.merchantName || '가맹점 정보 없음' }}
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
            <h2 class="kb-section-title text-20-bold">대분류 선택</h2>
          </div>

          <div class="top-grid kb-card">
            <button
              v-for="category in topCategories"
              :key="category.spendingCategoryId"
              type="button"
              :class="{
                selected:
                  selectedTopCategoryId === category.spendingCategoryId,
              }"
              @click="selectTopCategory(category)"
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
          class="content-btn primary save-button"
          :disabled="!selectedCategoryId || saving"
          @click="saveCategory"
        >
          {{ saving ? '수정 중...' : '카테고리 수정 완료' }}
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

const categories = ref([]);
const transaction = ref(null);
const loading = ref(false);
const saving = ref(false);
const message = ref('');
const messageType = ref('success');
const selectedTopCategoryId = ref(null);
const selectedCategoryId = ref(null);

const topCategories = computed(() =>
  categories.value.filter((item) => item.parentCategoryId == null),
);

const selectedTopCategory = computed(() =>
  topCategories.value.find(
    (item) => item.spendingCategoryId === selectedTopCategoryId.value,
  ),
);

const childCategories = (parentCategoryId) =>
  categories.value.filter(
    (item) => item.parentCategoryId === parentCategoryId,
  );

const selectTopCategory = async (category) => {
  selectedTopCategoryId.value = category.spendingCategoryId;
  const children = childCategories(category.spendingCategoryId);

  if (children.length) {
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
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.category-edit-content {
  /*
   * 팀 협의 후 헤더와 첫 콘텐츠 사이 간격을 적용할 경우
   * 아래 주석을 해제합니다.
   * margin-top: 14px;
   */
}

.edit-target {
  padding: 15px;
  display: grid;
  grid-template-columns: 44px 1fr auto;
  align-items: center;
  gap: 11px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.edit-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff3cf;
  color: #d79500;
  font-size: 17px;
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
}

.edit-info strong {
  margin-top: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.edit-info small {
  margin-top: 3px;
  color: var(--color-text-disabled);
}

.edit-amount {
  color: var(--color-error);
}

.current-category {
  margin: 10px 2px 0;
  color: var(--color-text-muted);
}

.current-category strong {
  margin-left: 4px;
  color: #9a7200;
}

.top-grid {
  margin-top: 17px;
  padding: 12px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.top-grid button {
  min-width: 0;
  min-height: 76px;
  padding: 8px 4px;
  border: 1px solid transparent;
  border-radius: 12px;
  background: #fafafa;
  color: #555;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.top-grid button.selected {
  border-color: var(--color-primary);
  background: #fff7d7;
  color: #8f6800;
}

.top-grid button > div {
  font-size: 17px;
  line-height: 1;
}

.top-grid button span {
  display: block;
  line-height: 1.25;
  text-align: center;
  word-break: keep-all;
}

.save-button {
  margin-top: 17px;
}

@media (max-width: 380px) {
  .top-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
