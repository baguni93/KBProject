<template>
  <div class="kb-mobile-page classification-page">
    <PageHeader
      title="소비 카테고리 분류"
      :custom-back="true"
      @back="goToCheck"
    />

    <div class="classification-content">

      <div v-if="loading" class="kb-card kb-loading text-13">
        <div class="spinner-border kb-spinner"></div>
        <div>미분류 거래를 불러오는 중이에요.</div>
      </div>

      <template v-else-if="currentTransaction">
        <section class="target-card kb-card">
          <div class="target-question">?</div>

          <div class="target-info">
            <span class="text-13">어떤 카테고리에 해당할까요?</span>
            <strong class="text-15-bold">
              {{ currentTransaction.transactionLabel || currentTransaction.merchantName || '거래 정보 없음' }}
            </strong>
            <small class="text-13">
              {{ formatAnalysisDateTime(currentTransaction.createdAt) }}
            </small>
          </div>

          <div class="target-amount text-15-bold">
            -{{ formatAnalysisNumber(currentTransaction.amount) }}원
          </div>
        </section>

        <div class="remaining-caption text-13">
          <i class="fa-solid fa-circle-info"></i>
          {{ periodLabel }} 미분류 거래
          {{ unclassifiedData?.unclassifiedCount ?? 0 }}건
        </div>

        <section class="kb-section">
          <div class="kb-section-title-row">
            <h2 class="kb-section-title text-20-bold">카테고리 선택</h2>
            <span class="text-13">하나를 선택해 주세요</span>
          </div>

          <div class="category-grid kb-card">
            <button
              v-for="category in topCategories"
              :key="category.spendingCategoryId"
              type="button"
              :class="[
                'category-button',
                { selected: selectedCategoryId === category.spendingCategoryId },
              ]"
              :disabled="classifying"
              @click="selectedCategoryId = category.spendingCategoryId"
            >
              <div class="category-button__icon">
                <i :class="getCategoryIcon(category.categoryName)"></i>
              </div>
              <span class="text-13-bold">{{ category.categoryName }}</span>
              <small v-if="hasChildren(category.spendingCategoryId)" class="text-13">
                세부 선택
              </small>
            </button>
          </div>
        </section>

        <button
          type="button"
          class="content-btn primary complete-button"
          :disabled="!selectedCategoryId || classifying"
          @click="completeSelection"
        >
          {{ classifying ? '분류 중...' : '분류 완료' }}
        </button>
      </template>

      <section v-else class="done-card kb-card">
        <div class="done-icon"><i class="fa-solid fa-check"></i></div>
        <h2 class="text-20-bold">모든 거래를 분류했어요!</h2>
        <p class="text-13">분석 화면에서 최신 소비 패턴을 확인해 보세요.</p>
        <button type="button" class="content-btn primary" @click="goToCheck">
          분석 가능 여부 다시 확인하기
        </button>
      </section>
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

const period = ref(normalizeAnalysisPeriod(route.query.period));
const categories = ref([]);
const unclassifiedData = ref(null);
const loading = ref(false);
const classifying = ref(false);
const message = ref('');
const messageType = ref('success');
const selectedCategoryId = ref(null);

const currentTransaction = computed(
  () => unclassifiedData.value?.transactions?.[0] ?? null,
);

const topCategories = computed(() =>
  categories.value.filter((category) => category.parentCategoryId == null),
);

const periodLabel = computed(
  () => unclassifiedData.value?.periodLabel ?? `최근 ${period.value}개월`,
);

const goToCheck = () =>
  router.push({
    name: 'analysis-check',
    query: { period: period.value },
  });

const childCategories = (parentCategoryId) =>
  categories.value.filter(
    (category) => category.parentCategoryId === parentCategoryId,
  );

const hasChildren = (parentCategoryId) =>
  childCategories(parentCategoryId).length > 0;

const loadData = async () => {
  loading.value = true;
  message.value = '';
  selectedCategoryId.value = null;

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
        query: {
          period: period.value,
          returnTo: route.query.returnTo || 'analysis-check',
        },
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
    selectedCategoryId.value = null;
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

const completeSelection = async () => {
  const category = categories.value.find(
    (item) => item.spendingCategoryId === selectedCategoryId.value,
  );

  if (!category) return;

  if (hasChildren(category.spendingCategoryId)) {
    await router.push({
      name: 'analysis-subcategory',
      params: {
        transactionId: currentTransaction.value.transactionId,
      },
      query: {
        period: period.value,
        parentCategoryId: category.spendingCategoryId,
        returnTo: route.query.returnTo || 'analysis-check',
      },
    });
    return;
  }

  await classifyCurrentTransaction(category);
};

onMounted(loadData);
</script>

<style scoped>
.classification-page {
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.classification-content {
  /*
   * 팀 협의 후 헤더와 첫 콘텐츠 사이 간격을 적용할 경우 아래 주석을 해제합니다.
   * margin-top: 14px;
   */
}

.target-card {
  padding: 16px;
  display: grid;
  grid-template-columns: 46px 1fr auto;
  align-items: center;
  gap: 11px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.target-question {
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: #fff3cf;
  color: #d99500;
  font-size: 24px;
  font-weight: 900;
}

.target-info {
  min-width: 0;
}

.target-info span,
.target-info strong,
.target-info small {
  display: block;
}

.target-info span {
  color: var(--color-text-muted);
}

.target-info strong {
  margin-top: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.target-info small {
  margin-top: 3px;
  color: var(--color-text-disabled);
}

.target-amount {
  color: var(--color-error);
  white-space: nowrap;
}

.remaining-caption {
  margin: 10px 2px 0;
  color: #8d7a46;
}

.remaining-caption i {
  margin-right: 3px;
  color: #d99500;
}

.kb-section-title-row > span {
  color: var(--color-text-disabled);
}

.category-grid {
  padding: 12px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.category-button {
  min-height: 84px;
  padding: 9px 3px;
  border: 1px solid transparent;
  border-radius: 12px;
  background: var(--color-bg-screen);
  color: var(--color-text-sub);
}

.category-button.selected {
  border-color: var(--color-primary);
  background: #fff7d7;
  color: #8c6800;
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.category-button:disabled {
  cursor: not-allowed;
}

.category-button__icon {
  height: 29px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
}

.category-button span {
  display: block;
  margin-top: 3px;
  line-height: 1.25;
  word-break: keep-all;
}

.category-button small {
  display: block;
  margin-top: 2px;
  color: var(--color-text-disabled);
  line-height: 1.2;
}

.complete-button {
  margin-top: 16px;
}

.done-card {
  margin-top: 18px;
  padding: 38px 22px;
  text-align: center;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.done-icon {
  width: 68px;
  height: 68px;
  margin: 0 auto 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  background: #eaf8f1;
  color: #1f9d62;
  font-size: 29px;
}

.done-card h2 {
  margin: 0;
}

.done-card p {
  margin: 7px 0 20px;
  color: var(--color-text-sub);
}

@media (max-width: 380px) {
  .category-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
