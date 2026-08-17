<template>
  <div class="kb-mobile-page insurance-evidence-page">
    <PageHeader
      title="보험 추천 근거"
      :custom-back="true"
      @back="goBack"
    />

    <div v-if="loading" class="kb-card kb-loading">
      <div class="spinner-border kb-spinner"></div>
      <div class="text-13">추천에 사용된 거래 근거를 불러오고 있어요.</div>
    </div>

    <template v-else-if="evidence">
      <section class="evidence-summary kb-card">
        <div class="summary-icon">
          <i class="fa-solid fa-chart-column"></i>
        </div>
        <span class="summary-label text-13-bold">12개월 실제 거래내역 기준</span>
        <h2 class="text-20-bold">추천에 반영된 소비를 확인해보세요</h2>
        <p class="text-13">{{ evidence.message }}</p>

        <div class="summary-meta">
          <div>
            <span class="text-13">분석 기간</span>
            <strong class="text-13-bold">
              {{ formatInsuranceAnalysisRange(evidence.analysisStartDate, evidence.analysisEndDate) }}
            </strong>
          </div>
          <div>
            <span class="text-13">추천 보험</span>
            <strong class="text-13-bold">{{ evidence.recommendationCount }}개</strong>
          </div>
        </div>
      </section>

      <section class="kb-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">관련 소비 카테고리</h2>
          <span class="category-count text-13">{{ categories.length }}개</span>
        </div>

        <div v-if="categories.length" class="evidence-list">
          <article
            v-for="category in categories"
            :key="category.spendingCategoryId"
            class="evidence-item kb-card"
          >
            <div class="evidence-icon">
              <i class="fa-solid fa-receipt"></i>
            </div>

            <div class="evidence-copy">
              <h3 class="text-15-bold">{{ category.categoryName }}</h3>
              <span class="text-13">
                최근 거래 {{ formatInsuranceDateTime(category.latestTransactionAt) }}
              </span>
            </div>

            <div class="evidence-values">
              <strong class="text-15-bold">{{ category.transactionCount }}건</strong>
              <span class="text-13">
                {{ formatInsuranceAmount(category.totalAmount) }}원
              </span>
            </div>
          </article>
        </div>

        <div v-else class="kb-card kb-empty-state">
          <div class="kb-empty-state__icon">
            <i class="fa-solid fa-receipt"></i>
          </div>
          <strong class="text-15-bold">표시할 추천 근거가 없습니다.</strong>
        </div>
      </section>

      <section class="notice-card kb-card">
        <i class="fa-solid fa-circle-info" aria-hidden="true"></i>
        <p class="text-13">
          보험 추천은 선택한 12개월 소비분석 기간의 정상 결제·송금·정산 거래를 기준으로 합니다.
          소비 카테고리가 보험상품과 연결된 경우 추천 근거로 사용됩니다.
        </p>
      </section>

      <button type="button" class="content-btn primary" @click="goBack">
        추천 목록으로 돌아가기
      </button>
    </template>

    <div v-else class="kb-card kb-empty-state error-state">
      <div class="kb-empty-state__icon">
        <i class="fa-solid fa-triangle-exclamation"></i>
      </div>
      <strong class="text-15-bold">추천 근거를 불러오지 못했습니다.</strong>
      <p class="text-13">{{ message }}</p>
      <button type="button" class="content-btn primary" @click="loadEvidence">
        다시 시도
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import insuranceRecommendationApi from '@/api/insuranceRecommendationApi';
import {
  formatInsuranceAmount,
  formatInsuranceAnalysisRange,
  formatInsuranceDateTime,
  getInsuranceRecommendationErrorMessage,
} from '@/util/insuranceRecommendation';

const route = useRoute();
const router = useRouter();
const spendingAnalysisId = Number(route.params.spendingAnalysisId);

const evidence = ref(null);
const loading = ref(false);
const message = ref('');

const categories = computed(() => evidence.value?.categories ?? []);

const loadEvidence = async () => {
  if (!Number.isInteger(spendingAnalysisId) || spendingAnalysisId <= 0) {
    evidence.value = null;
    message.value = '올바른 소비분석 ID가 필요합니다.';
    return;
  }

  loading.value = true;
  message.value = '';

  try {
    evidence.value = await insuranceRecommendationApi.getEvidence(
      spendingAnalysisId,
    );
  } catch (error) {
    evidence.value = null;
    message.value = getInsuranceRecommendationErrorMessage(
      error,
      '보험 추천 근거를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const goBack = () =>
  router.push({
    name: 'insurance-recommendation',
    params: { spendingAnalysisId },
  });

onMounted(loadEvidence);
</script>

<style scoped>
.insurance-evidence-page {
  padding-bottom: 36px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.evidence-summary {
  padding: 23px 19px 18px;
  text-align: center;
  border: 1px solid var(--color-divider);
  background: linear-gradient(180deg, #fffaf0 0%, #fff 60%);
  box-shadow: none;
}

.summary-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 22px;
  background: #fff0b7;
  color: #d99a00;
  font-size: 27px;
}

.summary-label {
  color: #876700;
}

.evidence-summary h2 {
  margin: 8px 0 6px;
  line-height: 1.45;
}

.evidence-summary > p {
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.65;
}

.summary-meta {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 8px;
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid var(--color-divider);
  text-align: left;
}

.summary-meta > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-meta span,
.category-count {
  color: var(--color-text-muted);
}

.evidence-list {
  display: grid;
  gap: 10px;
}

.evidence-item {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 15px;
  border: 1px solid var(--color-divider);
  box-shadow: none;
}

.evidence-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  background: #fff5d4;
  color: #d99a00;
  font-size: 19px;
}

.evidence-copy h3 {
  margin: 0 0 4px;
}

.evidence-copy span,
.evidence-values span {
  color: var(--color-text-sub);
}

.evidence-values {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: right;
}

.notice-card {
  display: flex;
  gap: 10px;
  margin: 16px 0;
  padding: 15px 16px;
  border: 1px solid #f0dfaa;
  background: #fffaf0;
  box-shadow: none;
  color: #6f5b20;
}

.notice-card > i {
  margin-top: 3px;
}

.notice-card p {
  margin: 0;
  line-height: 1.65;
}

.error-state {
  margin-top: 16px;
}

.error-state p {
  color: var(--color-text-sub);
}

.error-state .content-btn {
  margin-top: 16px;
}
</style>
