<template>
  <div class="kb-mobile-page insurance-recommendation-page">
    <PageHeader
        title="맞춤 보험 추천"
        :showBack="true"
        :customBack="true"
        @back="goBack"
    />

    <div class="recommendation-content-start">
      <section class="recommendation-intro kb-card">
        <div class="intro-topline">
          <span class="intro-icon" aria-hidden="true">
            <i class="fa-solid fa-shield-heart"></i>
          </span>
          <span class="intro-badge text-13-bold">12개월 소비분석 기반</span>
        </div>

        <div class="intro-copy">
          <h2 class="text-18-bold">내 소비에 맞는 보험을 찾았어요</h2>
          <p class="text-13">실제 소비내역을 분석해 관련 보장을 받을 수 있는 보험을 골랐어요.</p>
        </div>

        <template v-if="topRecommendationSummary">
          <button
              type="button"
              class="ai-reason-toggle"
              :aria-expanded="aiReasonOpen"
              @click="aiReasonOpen = !aiReasonOpen"
          >
            <span class="ai-reason-toggle__label text-13-bold">
              <i class="fa-solid fa-sparkles"></i>
              AI 추천 이유
            </span>

            <span class="ai-reason-toggle__action text-13-bold">
              {{ aiReasonOpen ? '접기' : '펼쳐보기' }}
              <i
                  class="fa-solid fa-chevron-down"
                  :class="{ open: aiReasonOpen }"
                  aria-hidden="true"
              ></i>
            </span>
          </button>

          <div v-if="aiReasonOpen" class="ai-reason-content">
            <p class="ai-reason-lead text-13-bold">이 소비 패턴과 관련성이 높은 보험이에요.</p>
            <p class="text-13">{{ topRecommendationSummary }}</p>
          </div>
        </template>
      </section>

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
        <section class="recommendation-meta">
          <span class="text-13">최근 {{ recommendationData.analysisPeriod }}개월 분석</span>
          <span class="text-13">{{ recommendations.length }}개 추천</span>
        </section>

        <section class="kb-section recommendation-list-section">
          <div class="kb-section-title-row">
            <h2 class="kb-section-title text-18-bold">맞춤 보험 추천</h2>
          </div>

          <div v-if="recommendations.length" class="recommendation-list">
            <article
                v-for="(insurance, index) in recommendations"
                :key="insurance.insuranceRecommendationId"
                class="insurance-item kb-card"
            >
              <div class="rank-badge" :class="`rank-${index + 1}`">
                {{ index + 1 }}위
              </div>

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

                <div v-if="insurance.recommendationReason" class="reason-box">
                  <span class="text-13-bold">
                    <i class="fa-solid fa-circle-check"></i>
                    추천 이유
                  </span>
                  <p class="text-13">{{ insurance.recommendationReason }}</p>
                </div>

                <button
                    type="button"
                    class="insurance-detail-button text-14-bold"
                    @click="openProductDetail(insurance)"
                >
                  보험 상세보기
                  <i class="fa-solid fa-chevron-right"></i>
                </button>
              </div>
            </article>
          </div>

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
        <p class="text-13">
          {{ message || '백엔드 서버와 추천 데이터를 확인한 뒤 다시 시도해 주세요.' }}
        </p>
        <button type="button" class="content-btn primary" @click="reloadRecommendations">
          다시 시도
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PageHeader from '@/components/common/PageHeader.vue';
import insuranceRecommendationApi from '@/api/insuranceRecommendationApi';
import {
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
const aiReasonOpen = ref(false);

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

const openProducts = () =>
    router.push({
      name: 'insurance-product-list',
      query: { spendingAnalysisId },
    });

const openProductDetail = (insurance) =>
    router.push({
      name: 'insurance-product-detail',
      params: { insuranceProductId: insurance.insuranceProductId },
      query: {
        from: 'recommendation',
        spendingAnalysisId,
        // 추천에서 진입한 상세 화면은 이 ID로 실제 추천 근거 거래까지 조회한다.
        insuranceRecommendationId: insurance.insuranceRecommendationId,
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
  min-height: 100dvh;
  padding-bottom: 36px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.insurance-recommendation-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: var(--color-bg-page);
}

.recommendation-content-start {
  padding: 16px 24px 0;
}

.recommendation-content-start > *,
.recommendation-content-start section {
  box-sizing: border-box;
  max-width: 100%;
}

.recommendation-intro {
  width: 100%;
  padding: 18px 16px 0;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
  overflow: hidden;
}

.intro-topline {
  display: flex;
  align-items: center;
  gap: 8px;
}

.intro-icon {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 30px;
  border-radius: 10px;
  background: #fff4cf;
  color: #d99a00;
  font-size: 14px;
}

.intro-badge {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  background: #fff8e5;
  color: #9a7300;
}

.intro-copy {
  margin-top: 10px;
}

.intro-copy h2 {
  margin: 0;
  line-height: 1.35;
  letter-spacing: -.45px;
}

.intro-copy p {
  margin: 6px 0 0;
  color: var(--color-text-sub);
  line-height: 1.55;
  word-break: keep-all;
}

.ai-reason-toggle {
  width: 100%;
  min-height: 48px;
  margin-top: 16px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 0;
  border-top: 1px solid var(--color-divider);
  background: transparent;
  cursor: pointer;
}

.ai-reason-toggle__label,
.ai-reason-toggle__action {
  display: inline-flex;
  align-items: center;
}

.ai-reason-toggle__label {
  gap: 7px;
  color: #9a7300;
}

.ai-reason-toggle__action {
  gap: 7px;
  color: var(--color-text-sub);
}

.ai-reason-toggle__action i {
  font-size: 11px;
  transition: transform .18s ease;
}

.ai-reason-toggle__action i.open {
  transform: rotate(180deg);
}

.ai-reason-content {
  padding: 0 0 16px;
}

.ai-reason-content p {
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.65;
  word-break: keep-all;
}

.ai-reason-content .ai-reason-lead {
  margin-bottom: 6px;
  color: var(--color-text-main);
}

.recommendation-loading {
  margin-top: 16px;
}

.recommendation-loading small {
  display: block;
  margin-top: 6px;
  color: var(--color-text-muted);
}

.loading-highlight {
  font-weight: 700;
  color: var(--color-text-main);
}

.recommendation-meta {
  margin-top: 16px;
  padding: 0 2px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--color-text-muted);
}

.recommendation-list-section {
  margin-top: 18px;
}

.recommendation-list {
  display: grid;
  gap: 16px;
}

.insurance-item {
  position: relative;
  padding: 18px 16px 4px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
  overflow: hidden;
}

.rank-badge {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  min-width: 48px;
  padding: 6px 10px 7px;
  border-radius: 18px 0 14px 0;
  background: var(--color-bg-disabled);
  color: #555;
  text-align: center;
  font-size: 11px;
  font-weight: 900;
}

.rank-badge.rank-1 {
  background: var(--color-primary);
  color: var(--color-text-main);
}

.rank-badge.rank-2 {
  background: #dfe3e8;
}

.rank-badge.rank-3 {
  background: #ead6c6;
}

.insurance-item__visual {
  height: 136px;
  margin: 18px 0 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.insurance-item__visual img {
  max-width: 205px;
  max-height: 128px;
  width: auto;
  height: auto;
  object-fit: contain;
  filter: drop-shadow(0 8px 12px rgba(0, 0, 0, .08));
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
  font-size: 30px;
}

.insurance-item__body {
  text-align: center;
}

.category-chip {
  display: inline-flex;
  padding: 4px 9px;
  border-radius: 999px;
  background: var(--color-bg-disabled);
  color: var(--color-text-sub);
}

.insurance-item h3 {
  margin: 8px 0 5px;
  line-height: 1.4;
}

.description {
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.55;
  word-break: keep-all;
}

.reason-box {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f7f8fa;
  text-align: left;
}

.reason-box > span {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8a6800;
}

.reason-box p {
  margin: 6px 0 0;
  color: var(--color-text-sub);
  line-height: 1.55;
  word-break: keep-all;
}

.insurance-detail-button {
  width: 100%;
  margin-top: 12px;
  padding: 11px 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  border: 0;
  border-top: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  color: var(--color-text-main);
  cursor: pointer;
}

.insurance-detail-button i {
  font-size: 11px;
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

.browse-button {
  font-size: 16px;
  font-weight: 600;
}
</style>