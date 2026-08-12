<template>
  <div class="kb-mobile-page insurance-detail-page">
    <PageHeader
      title="보험 상세보기"
      :custom-back="true"
      @back="goBack"
    />

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

        <div class="premium-card">
          <span class="text-13">예상 월 보험료</span>
          <strong class="text-20-bold">
            {{ formatInsuranceAmount(detail.monthlyPremium) }}원
          </strong>
        </div>
      </section>

      <section class="kb-section coverage-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">주요 보장 내용</h2>
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
                <span class="text-13">{{ coverage.coverageLimit || '보장 한도는 약관 확인' }}</span>
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
          <strong class="text-15-bold">등록된 보장 항목이 없습니다.</strong>
        </div>
      </section>

      <section class="notice-card kb-card">
        <i class="fa-solid fa-circle-info" aria-hidden="true"></i>
        <p class="text-13">
          표시된 보험료와 보장 내용은 프로젝트에 등록된 상품 정보입니다.
          실제 가입 가능 여부와 최종 보험료·보장 조건은 KB손해보험에서 확인해 주세요.
        </p>
      </section>

      <p v-if="applicationMessage" class="application-message text-13">
        {{ applicationMessage }}
      </p>

      <div class="bottom-btn-area double detail-actions">
        <button type="button" class="bottom-btn secondary-action" @click="goBack">
          뒤로가기
        </button>
        <button type="button" class="bottom-btn" @click="openApplication">
          보험 가입하러 가기
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
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import insuranceRecommendationApi from '@/api/insuranceRecommendationApi';
import {
  formatInsuranceAmount,
  getInsuranceApplicationUrl,
  getInsuranceCategoryIcon,
  getInsuranceImageUrl,
  getInsuranceRecommendationErrorMessage,
} from '@/util/insuranceRecommendation';

const route = useRoute();
const router = useRouter();
const insuranceProductId = Number(route.params.insuranceProductId);

const detail = ref(null);
const loading = ref(false);
const message = ref('');
const applicationMessage = ref('');
const imageFailed = ref(false);

const coverages = computed(() => detail.value?.coverages ?? []);
const imageUrl = computed(() => getInsuranceImageUrl(detail.value?.insuranceImage));

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
    detail.value = await insuranceRecommendationApi.getProductDetail(
      insuranceProductId,
    );
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

.premium-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 17px;
  padding: 15px 16px;
  border-radius: 13px;
  background: #f7f8fa;
}

.premium-card span,
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
</style>
