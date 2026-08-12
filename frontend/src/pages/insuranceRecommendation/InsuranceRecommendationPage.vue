<template>
  <div class="kb-mobile-page insurance-recommendation-page">
    <PageHeader
      title="맞춤 보험 추천"
      :custom-back="true"
      @back="goBack"
    />

    <section class="recommendation-intro kb-card">
      <div class="intro-icon" aria-hidden="true">
        <i class="fa-solid fa-shield-heart"></i>
      </div>
      <div>
        <span class="text-13-bold">12개월 소비분석 기반</span>
        <h2 class="text-20-bold">내 소비에 맞는 보험을 추천해요</h2>
        <p class="text-13">
          실제 결제내역의 소비 카테고리를 기준으로 관련 보험을 찾아드려요.
        </p>
      </div>
    </section>

    <!-- 카드추천과 동일한 비동기 대기 UX를 사용한다. -->
    <div v-if="loading" class="kb-card kb-loading recommendation-loading">
      <div class="spinner-border kb-spinner"></div>
      <div class="text-13">
        1년간의 소비분석 결과로 나에게 맞는 보험을 찾고 있어요
      </div>
      <small class="text-13">
        <strong class="loading-highlight">
          화면을 벗어나도 추천 작업은 계속 진행돼요<br />
          다시 들어오면 결과를 확인할 수 있어요.
        </strong>
      </small>
    </div>

    <template v-else-if="recommendationData">
      <section v-if="topRecommendationSummary" class="ai-summary kb-card">
        <div class="ai-summary__label text-13-bold">
          <i class="fa-solid fa-wand-magic-sparkles"></i>
          AI 추천 요약
        </div>
        <p class="text-13">{{ topRecommendationSummary }}</p>
      </section>

      <section class="analysis-meta kb-card">
        <div>
          <span class="text-13">분석 기간</span>
          <strong class="text-15-bold">{{ recommendationData.analysisPeriod }}개월</strong>
        </div>
        <div>
          <span class="text-13">추천 보험</span>
          <strong class="text-15-bold">{{ recommendations.length }}개</strong>
        </div>
      </section>

      <section class="kb-section recommendation-list-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">맞춤 보험 추천</h2>
          <button
            v-if="recommendations.length"
            type="button"
            class="kb-section-link evidence-link text-13-bold"
            @click="openEvidence"
          >
            추천 근거 보기
            <i class="fa-solid fa-chevron-right"></i>
          </button>
        </div>

        <div v-if="recommendations.length" class="recommendation-list">
          <article
            v-for="insurance in recommendations"
            :key="insurance.insuranceRecommendationId"
            class="insurance-item kb-card"
          >
            <div class="insurance-item__visual">
              <img
                v-if="getImage(insurance) && !isImageFailed(insurance.insuranceProductId)"
                :src="getImage(insurance)"
                :alt="`${insurance.insuranceName} 대표 이미지`"
                referrerpolicy="no-referrer"
                loading="lazy"
                @error="markImageFailed(insurance.insuranceProductId)"
              />
              <div v-else class="insurance-placeholder" aria-hidden="true">
                <i :class="getInsuranceCategoryIcon(insurance.insuranceCategory)"></i>
              </div>
            </div>

            <div class="insurance-item__body">
              <span class="category-chip text-13-bold">
                {{ insurance.insuranceCategory }}
              </span>
              <h3 class="text-18-bold">{{ insurance.insuranceName }}</h3>
              <p class="description text-13">{{ insurance.insuranceDescription }}</p>

              <div class="premium-row">
                <span class="text-13">예상 월 보험료</span>
                <strong class="text-15-bold">
                  {{ formatInsuranceAmount(insurance.monthlyPremium) }}원
                </strong>
              </div>

              <div v-if="insurance.recommendationReason" class="reason-box">
                <span class="text-13-bold">
                  <i class="fa-solid fa-circle-check"></i>
                  추천 이유
                </span>
                <p class="text-13">{{ insurance.recommendationReason }}</p>
              </div>

              <button
                type="button"
                class="content-btn secondary detail-button"
                @click="openProductDetail(insurance.insuranceProductId)"
              >
                보험 상세보기
                <i class="fa-solid fa-chevron-right"></i>
              </button>
            </div>
          </article>
        </div>

        <!-- 별도 analysis-008 페이지를 만들지 않고 현재 결과 화면 안에서 빈 결과만 표현한다. -->
        <div v-else class="kb-card kb-empty-state empty-recommendation">
          <div class="kb-empty-state__icon">
            <i class="fa-solid fa-shield-heart"></i>
          </div>
          <strong class="text-15-bold">현재 소비내역에 맞는 추천 보험이 없어요.</strong>
          <p class="text-13">
            전체 보험상품을 둘러보고 필요한 보장을 직접 확인할 수 있어요.
          </p>
        </div>
      </section>

      <button type="button" class="content-btn primary browse-button" @click="openProducts">
        전체 보험 둘러보기
        <i class="fa-solid fa-chevron-right"></i>
      </button>
    </template>

    <div v-else-if="!loading" class="kb-card kb-empty-state error-state">
      <div class="kb-empty-state__icon">
        <i class="fa-solid fa-triangle-exclamation"></i>
      </div>
      <strong class="text-15-bold">보험 추천 결과를 불러오지 못했습니다.</strong>
      <p class="text-13">{{ message || '백엔드 서버와 추천 데이터를 확인한 뒤 다시 시도해 주세요.' }}</p>
      <button type="button" class="content-btn primary" @click="reloadRecommendations">
        다시 시도
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import insuranceRecommendationApi from '@/api/insuranceRecommendationApi';
import {
  formatInsuranceAmount,
  getInsuranceCategoryIcon,
  getInsuranceImageUrl,
  getInsuranceRecommendationErrorMessage,
} from '@/util/insuranceRecommendation';

const route = useRoute();
const router = useRouter();
const spendingAnalysisId = Number(route.params.spendingAnalysisId);

const recommendationData = ref(null);
const loading = ref(false);
const message = ref('');
const failedImages = ref({});

const recommendations = computed(
  () => recommendationData.value?.recommendations ?? [],
);

const topRecommendationSummary = computed(() => {
  const item = recommendations.value.find(
    (insurance) => Boolean(insurance?.aiRecommendationSummary),
  );
  return item?.aiRecommendationSummary ?? '';
});

const isValidAnalysisId = () =>
  Number.isInteger(spendingAnalysisId) && spendingAnalysisId > 0;

const getImage = (insurance) =>
  getInsuranceImageUrl(insurance?.insuranceImage);

const isImageFailed = (insuranceProductId) =>
  Boolean(failedImages.value[insuranceProductId]);

const markImageFailed = (insuranceProductId) => {
  failedImages.value = {
    ...failedImages.value,
    [insuranceProductId]: true,
  };
};

// 카드추천과 같은 2초 polling 간격을 사용한다.
const STATUS_POLL_INTERVAL = 2000;
let statusTimer = null;

const stopStatusPolling = () => {
  if (statusTimer) {
    window.clearInterval(statusTimer);
    statusTimer = null;
  }
};

const loadRecommendationList = async () => {
  recommendationData.value =
    await insuranceRecommendationApi.getRecommendations(spendingAnalysisId);
};

const completeRecommendationLoading = async () => {
  stopStatusPolling();

  try {
    await loadRecommendationList();
  } catch (error) {
    recommendationData.value = null;
    message.value = getInsuranceRecommendationErrorMessage(
      error,
      '보험 추천 목록을 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const applyTaskStatus = async (status) => {
  const currentStatus = status?.status ?? 'IDLE';

  if (currentStatus === 'COMPLETED') {
    await completeRecommendationLoading();
    return;
  }

  if (currentStatus === 'FAILED') {
    stopStatusPolling();
    loading.value = false;
    recommendationData.value = null;
    message.value = status?.message || '보험 추천 생성에 실패했습니다.';
    return;
  }

  loading.value = true;
  message.value = status?.message || '보험 추천을 분석하고 있습니다.';
};

const checkRecommendationStatus = async () => {
  try {
    const status = await insuranceRecommendationApi.getStatus(spendingAnalysisId);
    await applyTaskStatus(status);
  } catch (error) {
    stopStatusPolling();
    loading.value = false;
    recommendationData.value = null;
    message.value = getInsuranceRecommendationErrorMessage(
      error,
      '보험 추천 진행 상태를 확인하지 못했습니다.',
    );
  }
};

const startStatusPolling = () => {
  stopStatusPolling();
  statusTimer = window.setInterval(
    checkRecommendationStatus,
    STATUS_POLL_INTERVAL,
  );
};

/*
 * 화면 진입 즉시 상태를 확인한다.
 * - 완료: 저장 결과 재사용
 * - 처리 중: 기존 작업 polling
 * - IDLE: 새 비동기 작업 시작
 */
const reloadRecommendations = async () => {
  if (!isValidAnalysisId()) {
    recommendationData.value = null;
    loading.value = false;
    message.value = '올바른 소비분석 ID가 필요합니다.';
    return;
  }

  stopStatusPolling();
  loading.value = true;
  recommendationData.value = null;
  failedImages.value = {};
  message.value = '';

  try {
    const currentStatus = await insuranceRecommendationApi.getStatus(
      spendingAnalysisId,
    );

    if (currentStatus?.status === 'COMPLETED') {
      await completeRecommendationLoading();
      return;
    }

    if (currentStatus?.status === 'PROCESSING') {
      await applyTaskStatus(currentStatus);
      startStatusPolling();
      return;
    }

    const startedStatus = await insuranceRecommendationApi.startAsync(
      spendingAnalysisId,
    );

    await applyTaskStatus(startedStatus);

    if (startedStatus?.status === 'PROCESSING') {
      startStatusPolling();
    }
  } catch (error) {
    stopStatusPolling();
    loading.value = false;
    recommendationData.value = null;
    message.value = getInsuranceRecommendationErrorMessage(
      error,
      '보험 추천 작업을 시작하지 못했습니다.',
    );
  }
};

const openEvidence = () =>
  router.push({
    name: 'insurance-recommendation-evidence',
    params: { spendingAnalysisId },
  });

const openProducts = () =>
  router.push({
    name: 'insurance-product-list',
    query: { spendingAnalysisId },
  });

const openProductDetail = (insuranceProductId) =>
  router.push({
    name: 'insurance-product-detail',
    params: { insuranceProductId },
    query: {
      from: 'recommendation',
      spendingAnalysisId,
    },
  });

const goBack = () =>
  router.push({
    name: 'analysis-result',
    params: { spendingAnalysisId },
  });

onMounted(reloadRecommendations);
onBeforeUnmount(stopStatusPolling);
</script>

<style scoped>
.insurance-recommendation-page {
  padding-bottom: 36px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.recommendation-intro {
  display: flex;
  gap: 14px;
  align-items: center;
  padding: 20px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.intro-icon {
  width: 52px;
  height: 52px;
  flex: 0 0 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  background: #fff3c4;
  color: #d99a00;
  font-size: 22px;
}

.recommendation-intro > div:last-child > span {
  display: block;
  color: #9a7300;
}

.recommendation-intro h2 {
  margin: 4px 0 5px;
  line-height: 1.35;
  letter-spacing: -.45px;
}

.recommendation-intro p,
.recommendation-loading small,
.description,
.empty-recommendation p,
.error-state p {
  color: var(--color-text-sub);
}

.recommendation-intro p {
  margin: 0;
  line-height: 1.55;
}

.recommendation-loading small {
  display: block;
  margin-top: 6px;
}

.loading-highlight {
  font-weight: 700;
  color: var(--color-text-main);
}

.ai-summary {
  margin-top: 14px;
  padding: 18px;
  border: 1px solid #f4df99;
  background: linear-gradient(135deg, #fff8dc 0%, #fff 72%);
  box-shadow: none;
}

.ai-summary__label {
  display: flex;
  align-items: center;
  gap: 7px;
  color: #947000;
}

.ai-summary p {
  margin: 9px 0 0;
  line-height: 1.7;
  color: #4d4430;
}

.analysis-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-top: 14px;
  padding: 16px 18px;
  border: 1px solid var(--color-divider);
  box-shadow: none;
}

.analysis-meta > div {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.analysis-meta span {
  color: var(--color-text-muted);
}

.evidence-link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #8a6800;
}

.recommendation-list {
  display: grid;
  gap: 12px;
}

.insurance-item {
  overflow: hidden;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.insurance-item__visual {
  min-height: 158px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #fbfaf6;
  border-bottom: 1px solid var(--color-divider);
}

.insurance-item__visual img {
  width: 100%;
  height: 158px;
  object-fit: contain;
}

.insurance-placeholder {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 24px;
  background: #fff3c4;
  color: #d99a00;
  font-size: 31px;
}

.insurance-item__body {
  padding: 17px;
}

.category-chip {
  display: inline-flex;
  padding: 5px 9px;
  border-radius: 999px;
  background: #fff3cf;
  color: #866300;
}

.insurance-item h3 {
  margin: 9px 0 5px;
  line-height: 1.4;
}

.description {
  margin: 0;
  line-height: 1.6;
}

.premium-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 15px;
  padding: 13px 0;
  border-top: 1px solid var(--color-divider);
  border-bottom: 1px solid var(--color-divider);
}

.premium-row span {
  color: var(--color-text-muted);
}

.reason-box {
  margin-top: 14px;
  padding: 13px 14px;
  border-radius: 12px;
  background: #f7f8fa;
}

.reason-box > span {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6f5900;
}

.reason-box p {
  margin: 7px 0 0;
  line-height: 1.6;
  color: var(--color-text-sub);
}

.detail-button {
  margin-top: 14px;
}

.browse-button {
  margin-top: 16px;
}

.error-state,
.empty-recommendation {
  margin-top: 14px;
}

.error-state .content-btn {
  margin-top: 16px;
}
</style>
