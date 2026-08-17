<template>
  <div class="kb-mobile-page insurance-detail-page">
    <PageHeader
        title="보험 상세보기"
        :custom-back="true"
        @back="goBack"
    />

    <div class="detail-content-start">
      <div v-if="loading" class="kb-card kb-loading">
        <div class="spinner-border kb-spinner"></div>
        <div class="text-13">보험 상세 정보를 불러오고 있어요.</div>
      </div>

      <template v-else-if="detail">
        <section class="product-hero kb-card">
          <div class="hero-image">
            <img
                v-if="imageUrl && !imageFailed"
                :src="imageUrl"
                :alt="`${detail.insuranceName} 대표 이미지`"
                referrerpolicy="no-referrer"
                @error="imageFailed = true"
            />
            <div v-else class="hero-placeholder" aria-hidden="true">
              <i :class="getInsuranceCategoryIcon(detail.insuranceCategory)"></i>
            </div>
          </div>

          <span class="category-chip text-13-bold">{{ detail.insuranceCategory }}</span>
          <h1 class="text-20-bold">{{ detail.insuranceName }}</h1>
          <p class="text-13">{{ detail.insuranceDescription }}</p>
        </section>

        <section class="kb-section coverage-section">
          <div class="kb-section-title-row">
            <h2 class="kb-section-title text-20-bold">상품 주요 안내</h2>
            <span class="coverage-count text-13">{{ coverages.length }}개</span>
          </div>

          <div v-if="coverages.length" class="coverage-list">
            <article
                v-for="coverage in coverages"
                :key="coverage.insuranceCoverageId"
                class="coverage-card kb-card"
            >
              <div class="coverage-top">
                <div class="coverage-icon">
                  <i class="fa-solid fa-shield"></i>
                </div>
                <div>
                  <h3 class="text-15-bold">{{ coverage.coverageName }}</h3>
                </div>
              </div>

              <strong v-if="Number(coverage.coverageAmount) > 0" class="coverage-amount text-18-bold">
                {{ formatInsuranceAmount(coverage.coverageAmount) }}원
              </strong>
              <p class="text-13">{{ coverage.coverageDescription }}</p>
            </article>
          </div>

          <div v-else class="kb-card kb-empty-state">
            <div class="kb-empty-state__icon">
              <i class="fa-solid fa-shield"></i>
            </div>
            <strong class="text-15-bold">등록된 상품 안내가 없습니다.</strong>
          </div>
        </section>

        <!-- 맞춤 추천에서 들어온 경우에만 실제 추천 근거를 상세 화면에 함께 보여준다. -->
        <section v-if="isRecommendationDetail" class="kb-section recommendation-evidence-section">
          <div class="kb-section-title-row">
            <h2 class="kb-section-title text-20-bold">추천 근거</h2>
            <span class="evidence-count text-13">{{ evidenceTransactions.length }}건</span>
          </div>

          <div v-if="detail.recommendationReason" class="recommendation-reason kb-card">
            <div class="recommendation-reason__label text-13-bold">
              <i class="fa-solid fa-circle-check"></i>
              이 보험을 추천한 이유
            </div>
            <p class="text-13">{{ detail.recommendationReason }}</p>
          </div>

          <article v-if="evidenceTransactions.length" class="evidence-summary-card kb-card">
            <div class="evidence-summary-icon" aria-hidden="true">
              <i class="fa-solid fa-receipt"></i>
            </div>
            <div class="evidence-summary-copy">
              <strong class="text-15-bold">{{ evidenceCategoryName }}</strong>
              <span class="text-13">최근 거래 {{ latestEvidenceDate }}</span>
            </div>
            <div class="evidence-summary-values">
              <strong class="text-15-bold">{{ evidenceTransactions.length }}건</strong>
              <span class="text-13">{{ formatInsuranceAmount(evidenceTotalAmount) }}원</span>
            </div>
          </article>

          <div v-if="evidenceTransactions.length" class="evidence-transaction-list">
            <h3 class="text-15-bold">관련 거래내역</h3>
            <article
                v-for="transaction in evidenceTransactions"
                :key="transaction.transactionId"
                class="evidence-transaction-item kb-card"
            >
              <div class="transaction-copy">
                <strong class="text-13-bold">{{ transaction.merchantName || '가맹점 정보 없음' }}</strong>
                <span class="text-13">{{ formatInsuranceDateTime(transaction.createdAt) }}</span>
              </div>
              <strong class="text-15-bold">{{ formatInsuranceAmount(transaction.amount) }}원</strong>
            </article>
          </div>

          <div v-else class="kb-card kb-empty-state evidence-empty">
            <div class="kb-empty-state__icon">
              <i class="fa-solid fa-receipt"></i>
            </div>
            <strong class="text-15-bold">표시할 추천 근거 거래가 없습니다.</strong>
          </div>
        </section>

        <section class="notice-card kb-card">
          <i class="fa-solid fa-circle-info" aria-hidden="true"></i>
          <p class="text-13">
            표시된 내용은 프로젝트에 등록된 상품 안내입니다.
            실제 가입 가능 여부와 최종 보장 조건은 KB손해보험에서 확인해 주세요.
          </p>
        </section>

        <p v-if="applicationMessage" class="application-message text-13">
          {{ applicationMessage }}
        </p>

        <div class="bottom-btn-area double detail-actions">
          <button type="button" class="bottom-btn secondary-action" @click="goBack">
            추천 목록으로
          </button>
          <button type="button" class="bottom-btn" @click="openApplication">
            보험 가입
            <i class="fa-solid fa-arrow-up-right-from-square"></i>
          </button>
        </div>
      </template>

      <div v-else class="kb-card kb-empty-state error-state">
        <div class="kb-empty-state__icon">
          <i class="fa-solid fa-triangle-exclamation"></i>
        </div>
        <strong class="text-15-bold">보험 상세 정보를 불러오지 못했습니다.</strong>
        <p class="text-13">{{ message }}</p>
        <button type="button" class="content-btn primary" @click="loadDetail">
          다시 시도
        </button>
      </div>
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
  formatInsuranceDateTime,
  getInsuranceApplicationUrl,
  getInsuranceCategoryIcon,
  getInsuranceImageUrl,
  getInsuranceRecommendationErrorMessage,
} from '@/util/insuranceRecommendation';

const route = useRoute();
const router = useRouter();
const insuranceProductId = Number(route.params.insuranceProductId);
const insuranceRecommendationId = Number(route.query.insuranceRecommendationId);

const detail = ref(null);
const loading = ref(false);
const message = ref('');
const applicationMessage = ref('');
const imageFailed = ref(false);

const coverages = computed(() => detail.value?.coverages ?? []);
const imageUrl = computed(() => getInsuranceImageUrl(detail.value?.insuranceImage));

// 추천 목록에서 상세로 들어온 경우에만 추천 근거 영역을 노출한다.
const isRecommendationDetail = computed(
    () =>
        route.query.from === 'recommendation' &&
        Number.isInteger(insuranceRecommendationId) &&
        insuranceRecommendationId > 0,
);

const evidenceTransactions = computed(
    () => detail.value?.evidenceTransactions ?? [],
);

const evidenceTotalAmount = computed(() =>
    evidenceTransactions.value.reduce(
        (sum, transaction) => sum + Number(transaction?.amount ?? 0),
        0,
    ),
);

const latestEvidenceDate = computed(() => {
  if (!evidenceTransactions.value.length) return '-';

  const latest = [...evidenceTransactions.value].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
  )[0];

  return formatInsuranceDateTime(latest?.createdAt);
});

const evidenceCategoryName = computed(() =>
    detail.value?.categoryName ||
    evidenceTransactions.value[0]?.categoryName ||
    detail.value?.insuranceCategory ||
    '관련 소비',
);

const loadDetail = async () => {
  if (!Number.isInteger(insuranceProductId) || insuranceProductId <= 0) {
    detail.value = null;
    message.value = '올바른 보험상품 ID가 필요합니다.';
    return;
  }

  loading.value = true;
  message.value = '';
  applicationMessage.value = '';
  imageFailed.value = false;

  try {
    if (isRecommendationDetail.value) {
      // 맞춤 추천에서 진입: 상품 상세 + 보장내용 + 실제 추천 근거 거래를 함께 조회한다.
      detail.value = await insuranceRecommendationApi.getRecommendationDetail(
          insuranceRecommendationId,
      );
    } else {
      // 전체 보험 둘러보기에서 진입: 추천 근거 없이 상품 자체 상세만 조회한다.
      detail.value = await insuranceRecommendationApi.getProductDetail(
          insuranceProductId,
      );
    }
  } catch (error) {
    detail.value = null;
    message.value = getInsuranceRecommendationErrorMessage(
        error,
        '보험상품 상세 정보를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

/*
 * DB에 실제 http(s) application_url이 있으면 그 값을 사용한다.
 * 현재 seed의 테스트용 html 값이 남아 있으면 util의 공식 KB손해보험 URL 매핑으로 보완한다.
 */
const openApplication = () => {
  const applicationUrl = getInsuranceApplicationUrl(detail.value);

  if (!applicationUrl) {
    applicationMessage.value = '연결 가능한 보험 가입 페이지가 등록되어 있지 않습니다.';
    return;
  }

  window.open(applicationUrl, '_blank', 'noopener,noreferrer');
};

const goBack = () => {
  const spendingAnalysisId = Number(route.query.spendingAnalysisId);

  if (
      route.query.from === 'recommendation' &&
      Number.isInteger(spendingAnalysisId) &&
      spendingAnalysisId > 0
  ) {
    router.push({
      name: 'insurance-recommendation',
      params: { spendingAnalysisId },
    });
    return;
  }

  if (route.query.from === 'products') {
    router.push({
      name: 'insurance-product-list',
      query: {
        ...(Number.isInteger(spendingAnalysisId) && spendingAnalysisId > 0
            ? { spendingAnalysisId }
            : {}),
        ...(route.query.category ? { category: route.query.category } : {}),
      },
    });
    return;
  }

  router.back();
};

onMounted(loadDetail);
</script>

<style scoped>
.insurance-detail-page {
  padding-bottom: 38px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.product-hero {
  padding: 18px;
  text-align: center;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.hero-image {
  height: 190px;
  margin-bottom: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 17px;
  background: #fbfaf6;
}

.hero-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.hero-placeholder {
  width: 86px;
  height: 86px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 28px;
  background: #fff3c4;
  color: #d99a00;
  font-size: 36px;
}

.category-chip {
  display: inline-flex;
  padding: 5px 10px;
  border-radius: 999px;
  background: #fff3cf;
  color: #866300;
}

.product-hero h1 {
  margin: 10px 0 7px;
  line-height: 1.45;
}

.product-hero > p {
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.65;
}


.coverage-count {
  color: var(--color-text-muted);
}

.coverage-list {
  display: grid;
  gap: 10px;
}

.coverage-card {
  padding: 16px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.coverage-top {
  display: flex;
  align-items: center;
  gap: 11px;
}

.coverage-icon {
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff4cd;
  color: #d99a00;
}

.coverage-top h3 {
  margin: 0 0 3px;
}

.coverage-top span {
  color: var(--color-text-muted);
}

.coverage-amount {
  display: block;
  margin-top: 14px;
}

.coverage-card > p {
  margin: 7px 0 0;
  color: var(--color-text-sub);
  line-height: 1.65;
}

.recommendation-evidence-section {
  margin-top: 22px;
}

.evidence-count {
  color: var(--color-text-muted);
}

.recommendation-reason {
  padding: 15px 16px;
  border: 1px solid #f0dfaa;
  background: #fffaf0;
  box-shadow: none;
}

.recommendation-reason__label {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #7b6100;
}

.recommendation-reason p {
  margin: 8px 0 0;
  color: var(--color-text-sub);
  line-height: 1.65;
}

.evidence-summary-card {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: center;
  gap: 11px;
  margin-top: 10px;
  padding: 14px 15px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.evidence-summary-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff4cd;
  color: #d99a00;
}

.evidence-summary-copy,
.evidence-summary-values,
.transaction-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.evidence-summary-copy span,
.evidence-summary-values span,
.transaction-copy span {
  color: var(--color-text-muted);
}

.evidence-summary-values {
  align-items: flex-end;
  text-align: right;
}

.evidence-transaction-list {
  margin-top: 18px;
}

.evidence-transaction-list > h3 {
  margin: 0 0 9px;
}

.evidence-transaction-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 13px 15px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.evidence-transaction-item + .evidence-transaction-item {
  margin-top: 8px;
}

.evidence-transaction-item > strong {
  flex: 0 0 auto;
}

.evidence-empty {
  margin-top: 10px;
}

.notice-card {
  display: flex;
  gap: 10px;
  margin-top: 16px;
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

.application-message {
  margin: 12px 2px 0;
  color: var(--color-error);
  text-align: center;
}

.detail-actions {
  margin-top: 17px;
}

.detail-actions .bottom-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
}

.secondary-action {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
  color: var(--color-text-main);
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

/* CardRecommendationDetailPage와 동일한 페이지/헤더/콘텐츠 여백 */
.insurance-detail-page {
  min-height: 100dvh;
  padding-bottom: 38px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.insurance-detail-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

.detail-content-start {
  padding: 16px 24px 0;
}

.detail-content-start > *,
.detail-content-start section {
  box-sizing: border-box;
  max-width: 100%;
}

.bottom-btn {
  font-size: 16px;
  font-weight: 600;
}

@media (max-width: 380px) {
  .insurance-detail-page :deep(.page-header) {
    padding: 0 20px;
  }

  .detail-content-start {
    padding-right: 20px;
    padding-left: 20px;
  }
}

</style>
