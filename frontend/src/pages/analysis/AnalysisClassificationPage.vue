<template>
  <!-- 모든 미분류 거래를 처리한 완료 상태 -->
  <div
      v-if="!loading && !currentTransaction"
      class="page-layout page-layout-top complete-page"
  >
    <div class="background-decoration decoration-left"></div>
    <div class="background-decoration decoration-right"></div>

    <main class="page-content complete-container">
      <section class="complete-content">
        <div class="success-visual">
          <span class="particle particle-1"></span>
          <span class="particle particle-2"></span>
          <span class="particle particle-3"></span>
          <span class="particle particle-4"></span>
          <span class="particle particle-5"></span>
          <span class="particle particle-6"></span>

          <span class="spark spark-1">
            <i class="fa-solid fa-star"></i>
          </span>

          <span class="spark spark-2">
            <i class="fa-solid fa-star"></i>
          </span>

          <div class="success-glow"></div>

          <div class="success-circle">
            <i class="fa-solid fa-check"></i>
          </div>
        </div>

        <div class="complete-message">
          <h1 class="text-30-bold">
            모든 거래를<br />
            분류했어요!
          </h1>

          <p class="complete-description text-15">
            이제 분류된 내역으로 소비 분석을<br />
            진행할 수 있어요.
          </p>
        </div>

        <div class="complete-badge">
          <i class="fa-solid fa-tags"></i>
          <span>소비 카테고리 분류 완료</span>
        </div>
      </section>
    </main>

    <div class="bottom-btn-area single">
      <button
          class="bottom-btn complete-button"
          type="button"
          @click="goToCheck"
      >
        분석 준비 화면으로
      </button>
    </div>
  </div>

  <!-- 분류 진행 상태 -->
  <div v-else class="kb-mobile-page classification-page">
    <PageHeader
        title="소비 카테고리 분류"
        :custom-back="true"
        @back="goToCheck"
    />

    <div class="classification-content">
      <div v-if="loading" class="kb-card kb-loading loading-card text-13">
        <div class="spinner-border kb-spinner"></div>
        <div>미분류 거래를 불러오는 중이에요.</div>
      </div>

      <template v-else-if="currentTransaction">
        <section class="target-card kb-card">
          <div class="target-card__top">
            <span class="target-label text-13-bold">분류할 거래</span>
            <span class="remaining-badge text-13-bold">
              {{ unclassifiedData?.unclassifiedCount ?? 0 }}건 남음
            </span>
          </div>

          <div class="target-card__body">
            <div class="target-question" aria-hidden="true">
              <i class="fa-solid fa-tag"></i>
            </div>

            <div class="target-info">
              <strong class="text-18-bold">
                {{ currentTransaction.transactionLabel || currentTransaction.merchantName || '거래 정보 없음' }}
              </strong>
              <span class="text-13">
                {{ formatAnalysisDateTime(currentTransaction.createdAt) }}
              </span>
            </div>

            <div class="target-amount text-18-bold">
              -{{ formatAnalysisNumber(currentTransaction.amount) }}원
            </div>
          </div>

          <p class="target-help text-13">
            아래에서 이 거래에 가장 알맞은 카테고리를 선택해 주세요.
          </p>
        </section>

        <section class="category-section">
          <div class="category-section__head">
            <div>
              <h2 class="text-20-bold">카테고리 선택</h2>
              <p class="text-13">하나를 선택하면 바로 분류할 수 있어요.</p>
            </div>
            <span class="period-chip text-13-bold">{{ periodLabel }}</span>
          </div>

          <div class="selector-card kb-card">
            <SpendingCategorySelector
                v-model="selectedCategoryId"
                :categories="categories"
                :disabled="classifying"
                show-child-hint
            />
          </div>
        </section>

        <button
            type="button"
            class="content-btn primary complete-classification-button"
            :disabled="!selectedCategoryId || classifying"
            @click="completeSelection"
        >
          {{ classifying ? '분류 중...' : '선택한 카테고리로 분류' }}
        </button>
      </template>
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

@import "@/components/common/common/common.css";

/* ========================================
   모든 거래 분류 완료
   - AccountCompletePage 레이아웃/스타일과 동일
======================================== */
.complete-page {
  position: relative;
  overflow: hidden;
  background:
      linear-gradient(
          180deg,
          #fffdf8 0%,
          var(--color-bg-page) 42%,
          var(--color-bg-page) 100%
      );
}

.complete-container {
  position: relative;
  z-index: 2;
  justify-content: center;
  overflow: hidden;
}

.complete-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: -42px;
  text-align: center;
}

.background-decoration {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.decoration-left {
  top: -100px;
  left: -120px;
  width: 240px;
  height: 240px;
  background: rgba(255, 188, 46, 0.1);
  animation: background-float-left 6s ease-in-out infinite;
}

.decoration-right {
  top: 280px;
  right: -110px;
  width: 210px;
  height: 210px;
  background: rgba(176, 164, 255, 0.05);
  animation: background-float-right 7s ease-in-out infinite;
}

.success-visual {
  position: relative;
  width: 180px;
  height: 180px;
  margin-bottom: 22px;
}

.success-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 126px;
  height: 126px;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.16);
  transform: translate(-50%, -50%);
  animation: glow 2.2s ease-in-out 0.8s infinite;
}

.success-circle {
  position: absolute;
  z-index: 2;
  top: 50%;
  left: 50%;
  display: flex;
  width: 102px;
  height: 102px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background:
      linear-gradient(
          145deg,
          #ffd15c,
          var(--color-primary)
      );
  box-shadow:
      0 16px 34px rgba(255, 188, 46, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.5);
  color: var(--color-text-white);
  font-size: 42px;
  transform: translate(-50%, -50%) scale(0);
  animation:
      success-pop
      0.55s
      cubic-bezier(0.34, 1.56, 0.64, 1)
      forwards;
}

.success-circle i {
  opacity: 0;
  transform: scale(0.5) rotate(-15deg);
  animation: check-appear 0.35s ease 0.42s forwards;
}

.particle {
  position: absolute;
  z-index: 1;
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  opacity: 0;
}

.particle-1 {
  top: 28px;
  left: 28px;
  animation: particle-pop 0.55s ease 0.35s forwards;
}

.particle-2 {
  top: 18px;
  right: 36px;
  width: 6px;
  height: 6px;
  background: #9d90ff;
  animation: particle-pop 0.55s ease 0.5s forwards;
}

.particle-3 {
  top: 82px;
  right: 8px;
  width: 10px;
  height: 10px;
  background: #7bd6c7;
  animation: particle-pop 0.55s ease 0.4s forwards;
}

.particle-4 {
  right: 30px;
  bottom: 24px;
  width: 7px;
  height: 7px;
  background: #ff9eaa;
  animation: particle-pop 0.55s ease 0.6s forwards;
}

.particle-5 {
  bottom: 26px;
  left: 30px;
  width: 6px;
  height: 6px;
  background: #9d90ff;
  animation: particle-pop 0.55s ease 0.48s forwards;
}

.particle-6 {
  top: 92px;
  left: 6px;
  width: 9px;
  height: 9px;
  background: #7bd6c7;
  animation: particle-pop 0.55s ease 0.58s forwards;
}

.spark {
  position: absolute;
  z-index: 1;
  color: #ffd65c;
  opacity: 0;
}

.spark-1 {
  top: 18px;
  left: 70px;
  font-size: 12px;
  animation: spark-pop 0.55s ease 0.55s forwards;
}

.spark-2 {
  right: 46px;
  bottom: 17px;
  color: #a99df7;
  font-size: 10px;
  animation: spark-pop 0.55s ease 0.7s forwards;
}

.complete-message {
  opacity: 0;
  transform: translateY(16px);
  animation: content-up 0.5s ease 0.55s forwards;
}

.complete-message h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.3;
  letter-spacing: -0.7px;
}

.complete-description {
  margin: 16px 0 0;
  color: var(--color-text-sub);
  font-weight: 400;
  line-height: 1.65;
}

.complete-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  margin-top: 26px;
  padding: 10px 15px;
  border: 1px solid rgba(255, 188, 46, 0.22);
  border-radius: 999px;
  background: rgba(255, 188, 46, 0.1);
  color: #9a6900;
  font-size: 13px;
  font-weight: 500;
  opacity: 0;
  transform: translateY(10px);
  animation: content-up 0.45s ease 0.72s forwards;
}

.complete-badge i {
  color: var(--color-primary-active);
  font-size: 12px;
}

.complete-button {
  opacity: 0;
  transform: scale(0.98);
  animation: button-show 0.45s ease 0.85s forwards;
}

.complete-button:active {
  transform: scale(0.985);
}

@keyframes success-pop {
  0% {
    transform: translate(-50%, -50%) scale(0);
  }

  70% {
    transform: translate(-50%, -50%) scale(1.08);
  }

  100% {
    transform: translate(-50%, -50%) scale(1);
  }
}

@keyframes check-appear {
  from {
    opacity: 0;
    transform: scale(0.5) rotate(-15deg);
  }

  to {
    opacity: 1;
    transform: scale(1) rotate(0);
  }
}

@keyframes particle-pop {
  0% {
    opacity: 0;
    transform: scale(0);
  }

  60% {
    opacity: 1;
    transform: scale(1.4);
  }

  100% {
    opacity: 0.7;
    transform: scale(1);
  }
}

@keyframes spark-pop {
  0% {
    opacity: 0;
    transform: scale(0) rotate(-40deg);
  }

  60% {
    opacity: 1;
    transform: scale(1.4) rotate(12deg);
  }

  100% {
    opacity: 0.75;
    transform: scale(1) rotate(0);
  }
}

@keyframes glow {
  0%,
  100% {
    opacity: 0.55;
    transform: translate(-50%, -50%) scale(0.95);
  }

  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

@keyframes content-up {
  from {
    opacity: 0;
    transform: translateY(16px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes button-show {
  from {
    opacity: 0;
    transform: scale(0.98);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes background-float-left {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(12px, 10px);
  }
}

@keyframes background-float-right {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(-10px, -8px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .background-decoration,
  .success-circle,
  .success-circle i,
  .success-glow,
  .particle,
  .spark,
  .complete-message,
  .complete-badge,
  .complete-button {
    opacity: 1;
    animation: none;
  }

  .background-decoration,
  .particle,
  .spark,
  .complete-message,
  .complete-badge,
  .complete-button {
    transform: none;
  }

  .success-circle {
    transform: translate(-50%, -50%);
  }

  .success-glow {
    transform: translate(-50%, -50%);
  }
}

@media (max-width: 360px) {
  .success-visual {
    width: 160px;
    height: 160px;
  }

  .success-circle {
    width: 94px;
    height: 94px;
    font-size: 38px;
  }

  .success-glow {
    width: 116px;
    height: 116px;
  }
}

.classification-page {
  min-height: 100vh;
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

/* 공통 헤더는 수정하지 않고 이 화면에서만 좌우 위치를 맞춤 */
.classification-page :deep(.page-header) {
  padding: 0 24px;
}

.classification-content {
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

.remaining-badge,
.period-chip {
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

.target-question {
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

.category-section {
  margin-top: 28px;
}

.category-section__head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.category-section__head h2,
.category-section__head p {
  margin: 0;
}

.category-section__head p {
  margin-top: 5px;
  color: var(--color-text-sub);
}

.selector-card {
  padding: 12px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

/* 이 화면에서만 카테고리 선택 버튼을 1:1 정사각형으로 표시 */
.selector-card :deep(button) {
  width: 100%;
  height: auto;
  min-height: 0;
  aspect-ratio: 1 / 1;
}

.complete-classification-button {
  width: 100%;
  min-height: 52px;
  margin-top: 20px;
  font-size: 16px;
  font-weight: 600;
}






@media (max-width: 380px) {
  .classification-content {
    padding: 20px;
  }

  .target-card__body {
    grid-template-columns: 44px minmax(0, 1fr);
  }

  .target-amount {
    grid-column: 2;
    margin-top: -2px;
  }

  .period-chip {
    display: none;
  }
}
</style>
