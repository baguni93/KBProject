<template>
  <div class="kb-mobile-page card-recommendation-page">
    <PageHeader
        title="맞춤 카드 추천"
        :showBack="true"
        :customBack="true"
        @back="goBack"
    />

    <div class="recommendation-content-start">

      <section v-if="!loading" class="recommendation-intro kb-card">
        <div class="intro-topline">
          <span class="intro-icon" aria-hidden="true">
            <i class="fa-solid fa-wand-magic-sparkles"></i>
          </span>
          <span class="intro-badge text-13-bold">12개월 소비분석 기반</span>
        </div>

        <div class="intro-copy">
          <h2 class="text-18-bold">내 소비에 딱 맞는 카드를 찾았어요</h2>
          <p class="text-13">실제 소비내역을 분석해 혜택이 큰 카드를 골랐어요.</p>
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
            <p class="ai-reason-lead text-13-bold">이 소비 패턴에 혜택이 큰 카드예요.</p>
            <p class="text-13">{{ formatWonInText(topRecommendationSummary) }}</p>
          </div>
        </template>
      </section>

      <div v-if="loading" class="kb-card kb-loading recommendation-loading">
        <div class="spinner-border kb-spinner"></div>

        <div class="text-13">
          1년간의 소비분석 결과로 가장 많이 할인되는 카드를 찾고 있어요
        </div>

        <small class="text-13">
          <strong class="loading-highlight">
            화면을 벗어나도 추천 작업은 계속 진행돼요 <br/> 다시 들어오면 결과를 확인할 수 있어요.
          </strong>
        </small>
      </div>

      <template v-else-if="recommendationData">
        <section class="filter-section">
          <div class="section-label-row">
            <h2 class="text-15-bold">카드 유형</h2>
            <span class="text-13">{{ recommendationData.analysisPeriod }}개월 분석</span>
          </div>
          <div
              class="segmented-control card-type-control"
              role="tablist"
              aria-label="카드 유형"
          >
            <button
                v-for="option in cardTypeOptions"
                :key="option.value"
                type="button"
                :class="['text-13-bold', { active: selectedCardType === option.value }]"
                @click="changeCardType(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </section>

        <section class="kb-section recommendation-list-section">
          <div class="kb-section-title-row">
            <h2 class="kb-section-title text-18-bold">{{ selectedCardTypeLabel }} 추천 TOP 3</h2>

          </div>

          <div v-if="recommendations.length" class="recommendation-list">
            <article
                v-for="card in recommendations"
                :key="card.cardRecommendationId"
                :class="[
                'recommendation-item',
                'kb-card',
                { 'has-annual-fee-toggle': card.cardType === 'CREDIT' },
              ]"
            >
              <div class="rank-badge" :class="`rank-${card.recommendationRank}`">
                {{ card.recommendationRank }}위
              </div>

              <div
                  v-if="card.cardType === 'CREDIT'"
                  class="annual-fee-toggle-row"
              >
                <span class="text-13-bold">연회비 포함</span>
                <button
                    type="button"
                    class="annual-fee-switch"
                    :class="{ active: isAnnualFeeIncluded(card.cardRecommendationId) }"
                    role="switch"
                    :aria-checked="isAnnualFeeIncluded(card.cardRecommendationId)"
                    :aria-label="`${card.cardName} 예상 할인액에 연회비 포함`"
                    @click.stop="toggleAnnualFee(card.cardRecommendationId)"
                >
                  <span class="annual-fee-switch__knob"></span>
                </button>
              </div>

              <div class="card-visual">
                <img
                    v-if="getCardImagePath(card.cardImage)"
                    :src="getCardImagePath(card.cardImage)"
                    :alt="`${card.cardName} 카드 이미지`"
                />
                <div v-else class="card-placeholder" aria-hidden="true">
                  <span>KB</span>
                  <i class="fa-regular fa-credit-card"></i>
                  <strong>{{ getCardInitial(card.cardName) }}</strong>
                </div>
              </div>

              <div class="card-copy">
                <span class="card-kind text-13-bold">{{ getCardTypeLabel(card.cardType) }}</span>
                <h3 class="text-18-bold">{{ card.cardName }}</h3>
                <p class="text-13">{{ card.cardDescription }}</p>
              </div>

              <div class="benefit-summary benefit-summary--two-column">
                <div>
                  <span class="text-13">예상 할인액</span>
                  <strong
                      class="text-15-bold"
                      :class="{ negative: getDisplayedBenefitAmount(card) < 0 }"
                  >
                    {{ formatCardAmount(getDisplayedBenefitAmount(card)) }}원
                  </strong>
                </div>
                <div>
                  <span class="text-13">연회비</span>
                  <strong class="text-15-bold">{{ formatCardAmount(card.annualFee) }}원</strong>
                </div>
              </div>

              <button
                  type="button"
                  class="detail-button text-14-bold"
                  @click="openDetail(card.cardRecommendationId)"
              >
                계산 근거 상세보기
                <i class="fa-solid fa-chevron-right"></i>
              </button>
            </article>
          </div>

          <div v-else class="kb-card kb-empty-state">
            <div class="kb-empty-state__icon">
              <i class="fa-regular fa-credit-card"></i>
            </div>
            <strong class="text-15-bold">추천 결과가 없습니다.</strong>
            <p class="text-13">카드 상품과 혜택 데이터를 확인해 주세요.</p>
          </div>
        </section>
      </template>

      <div v-else-if="!loading" class="kb-card kb-empty-state error-state">
        <div class="kb-empty-state__icon">
          <i class="fa-solid fa-triangle-exclamation"></i>
        </div>
        <strong class="text-15-bold">카드 추천 결과를 불러오지 못했습니다.</strong>
        <p class="text-13">백엔드 서버와 DB 데이터를 확인한 뒤 다시 시도해 주세요.</p>
        <button type="button" class="content-btn primary" @click="reloadRecommendations">
          다시 시도
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import cardRecommendationApi from '@/api/cardRecommendationApi';
import PageHeader from '@/components/common/PageHeader.vue';
import {
  CARD_RECOMMENDATION_CARD_TYPES,
  formatCardAmount,
  getCardImagePath,
  getCardRecommendationErrorMessage,
  getCardTypeLabel,
  normalizeCardType,
} from '@/util/cardRecommendation';

const route = useRoute();
const router = useRouter();
const cardTypeOptions = CARD_RECOMMENDATION_CARD_TYPES;

const spendingAnalysisId = Number(route.params.spendingAnalysisId);
const selectedCardType = ref(normalizeCardType(route.query.cardType));
const selectedFeeMode = ref('MAX_BENEFIT');
const recommendationData = ref(null);
const recommendationCache = ref({});
const annualFeeIncludedByCard = ref({});
const creationResult = ref(null);
const loading = ref(false);
const aiReasonOpen = ref(false);
const message = ref("");
const messageType = ref("info");

const recommendations = computed(
    () => recommendationData.value?.recommendations ?? [],
);

const selectedCardTypeLabel = computed(() =>
    getCardTypeLabel(selectedCardType.value),
);

const topRecommendation = computed(() =>
    recommendations.value.find(
        (card) => Number(card.recommendationRank) === 1,
    ) ?? recommendations.value[0] ?? null,
);

const topRecommendationSummary = computed(
    () => topRecommendation.value?.aiRecommendationSummary ?? '',
);

const formatWonInText = (text) => {
  if (!text) return '';

  return String(text).replace(/(\d+)\s*원/g, (_, number) => {
    return `${Number(number).toLocaleString('ko-KR')}원`;
  });
};

const creationStatusLabel = computed(() => {
  if (!creationResult.value) return "확인 전";
  return creationResult.value.created ? "새 추천 생성" : "기존 추천 재사용";
});

const isValidAnalysisId = () =>
    Number.isInteger(spendingAnalysisId) && spendingAnalysisId > 0;

const updateQuery = () => {
  router.replace({
    name: "card-recommendation",
    params: { spendingAnalysisId },
    query: {
      cardType: selectedCardType.value,
    },
  });
};

const loadRecommendationLists = async () => {
  const results = await Promise.all(
      cardTypeOptions.map((option) =>
          cardRecommendationApi.getRecommendations(
              spendingAnalysisId,
              option.value,
              selectedFeeMode.value,
          ),
      ),
  );

  const nextCache = {};
  cardTypeOptions.forEach((option, index) => {
    nextCache[option.value] = results[index];
  });

  recommendationCache.value = nextCache;
  recommendationData.value =
      recommendationCache.value[selectedCardType.value] ?? null;
};

// 2초마다 백엔드에 작업이 완료되었는지 호출한다.(비동기 처리용도)
const STATUS_POLL_INTERVAL = 2000;
let statusTimer = null;

const stopStatusPolling = () => {
  if (statusTimer) {
    window.clearInterval(statusTimer);
    statusTimer = null;
  }
};

const completeRecommendationLoading = async (status) => {
  stopStatusPolling();

  try {
    creationResult.value = {
      created: Boolean(status?.created),
      recommendationCount: Number(status?.recommendationCount ?? 0),
    };

    await loadRecommendationLists();
    messageType.value = status?.created ? 'success' : 'info';
    message.value =
        status?.message || '카드 추천 결과를 불러왔습니다.';
  } catch (error) {
    recommendationData.value = null;
    messageType.value = "error";
    message.value = getCardRecommendationErrorMessage(
        error,
        "카드 추천 목록을 불러오지 못했습니다.",
    );
  } finally {
    loading.value = false;
  }
};

const applyTaskStatus = async (status) => {
  const currentStatus = status?.status ?? "IDLE";

  if (currentStatus === "COMPLETED") {
    await completeRecommendationLoading(status);
    return;
  }

  if (currentStatus === "FAILED") {
    stopStatusPolling();
    loading.value = false;
    recommendationData.value = null;
    messageType.value = "error";
    message.value = status?.message || "카드 추천 생성에 실패했습니다.";
    return;
  }

  loading.value = true;
  messageType.value = "info";
  message.value = status?.message || "카드 혜택을 계산하고 있습니다.";
};

const checkRecommendationStatus = async () => {
  try {
    const status = await cardRecommendationApi.getStatus(spendingAnalysisId);
    await applyTaskStatus(status);
  } catch (error) {
    stopStatusPolling();
    loading.value = false;
    recommendationData.value = null;
    messageType.value = "error";
    message.value = getCardRecommendationErrorMessage(
        error,
        "카드 추천 진행 상태를 확인하지 못했습니다.",
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

const reloadRecommendations = async () => {
  if (!isValidAnalysisId()) {
    recommendationData.value = null;
    messageType.value = "error";
    message.value = "올바른 소비분석 ID가 필요합니다.";
    return;
  }

  stopStatusPolling();
  loading.value = true;
  recommendationData.value = null;
  recommendationCache.value = {};
  annualFeeIncludedByCard.value = {};
  message.value = '';

  try {
    const currentStatus =
        await cardRecommendationApi.getStatus(spendingAnalysisId);

    if (currentStatus?.status === "COMPLETED") {
      await completeRecommendationLoading(currentStatus);
      return;
    }

    if (currentStatus?.status === "PROCESSING") {
      await applyTaskStatus(currentStatus);
      startStatusPolling();
      return;
    }

    const startedStatus =
        await cardRecommendationApi.startAsync(spendingAnalysisId);

    await applyTaskStatus(startedStatus);

    if (startedStatus?.status === "PROCESSING") {
      startStatusPolling();
    }
  } catch (error) {
    stopStatusPolling();
    loading.value = false;
    recommendationData.value = null;
    messageType.value = "error";
    message.value = getCardRecommendationErrorMessage(
        error,
        "카드 추천 작업을 시작하지 못했습니다.",
    );
  }
};

const changeCardType = (cardType) => {
  if (selectedCardType.value === cardType) return;

  selectedCardType.value = cardType;
  recommendationData.value =
      recommendationCache.value[cardType] ?? null;
  updateQuery();
};

const isAnnualFeeIncluded = (cardRecommendationId) =>
    Boolean(annualFeeIncludedByCard.value[cardRecommendationId]);

const toggleAnnualFee = (cardRecommendationId) => {
  annualFeeIncludedByCard.value = {
    ...annualFeeIncludedByCard.value,
    [cardRecommendationId]:
        !isAnnualFeeIncluded(cardRecommendationId),
  };
};

const getDisplayedBenefitAmount = (card) => {
  const expectedBenefitAmount = Number(card?.expectedBenefitAmount ?? 0);

  if (
      card?.cardType !== 'CREDIT' ||
      !isAnnualFeeIncluded(card?.cardRecommendationId)
  ) {
    return expectedBenefitAmount;
  }

  return expectedBenefitAmount - Number(card?.annualFee ?? 0);
};


const openDetail = (cardRecommendationId) =>
    router.push({
      name: 'card-recommendation-detail',
      params: {cardRecommendationId},
      query: {
        spendingAnalysisId,
        cardType: selectedCardType.value,
      },
    });

const goBack = () =>
    router.push({
      name: "analysis-result",
      params: { spendingAnalysisId },
    });


const getCardInitial = (cardName = "") => {
  const normalized = String(cardName)
      .replace(/KB국민/g, "")
      .replace(/카드/g, "")
      .trim();
  return normalized.slice(0, 12) || "CARD";
};

onMounted(reloadRecommendations);
onBeforeUnmount(stopStatusPolling);
</script>

<style scoped>
.card-recommendation-page {
  min-height: 100dvh;
  padding-bottom: 36px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

/* AnalysisMainPage와 동일한 헤더/본문 기준 */
.card-recommendation-page :deep(.page-header) {
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
  box-sizing: border-box;
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

.recommendation-loading small {
  display: block;
  margin-top: 6px;
  color: var(--color-text-muted);
}

.filter-section {
  margin-top: 18px;
}

.section-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 2px 10px;
}

.section-label-row h2 {
  margin: 0;
}

.section-label-row span,
.result-count,
.filter-description {
  color: var(--color-text-muted);
}

.segmented-control {
  display: grid;
  padding: 4px;
  border-radius: 13px;
  background: #eceef1;
}

.segmented-control button {
  min-height: 39px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: var(--color-text-sub);
  transition: .15s ease;
}

.segmented-control button.active {
  background: var(--color-bg-page);
  color: var(--color-text-main);
  box-shadow: 0 2px 7px rgba(0, 0, 0, .08);
}

.card-type-control {
  grid-template-columns: repeat(2, 1fr);
}

.filter-description {
  margin: 7px 3px 0;
}

.recommendation-list-section {
  margin-top: 18px;
}

.recommendation-list {
  display: grid;
  gap: 16px;
}

.recommendation-item {
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

.annual-fee-toggle-row {
  position: absolute;
  top: 11px;
  right: 13px;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 7px;
  color: var(--color-text-main);
}

.annual-fee-switch {
  position: relative;
  width: 38px;
  height: 22px;
  flex: 0 0 38px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: #e3e5e8;
  cursor: pointer;
  transition: background-color .18s ease;
}

.annual-fee-switch__knob {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-bg-page);
  box-shadow: 0 1px 4px rgba(0, 0, 0, .18);
  transition: transform .18s ease;
}

.annual-fee-switch.active {
  background: var(--color-primary);
}

.annual-fee-switch.active .annual-fee-switch__knob {
  transform: translateX(16px);
}

.recommendation-item.has-annual-fee-toggle .card-visual {
  margin-top: 30px;
}

.card-visual {
  height: 136px;
  margin: 5px 0 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-visual img {
  max-width: 205px;
  max-height: 128px;
  object-fit: contain;
  filter: drop-shadow(0 8px 12px rgba(0, 0, 0, .14));
}

.card-placeholder {
  width: 198px;
  height: 118px;
  padding: 16px;
  display: grid;
  grid-template-columns: 1fr auto;
  grid-template-rows: auto 1fr;
  align-items: start;
  border-radius: 12px;
  background: linear-gradient(135deg, #484848, #161616);
  color: var(--color-text-white);
  box-shadow: 0 8px 16px rgba(0, 0, 0, .18);
}

.card-placeholder span {
  font-size: 13px;
  font-weight: 900;
}

.card-placeholder i {
  font-size: 21px;
}

.card-placeholder strong {
  align-self: end;
  grid-column: 1 / 3;
  font-size: 14px;
  letter-spacing: -.3px;
}

.card-copy {
  text-align: center;
}

.card-kind {
  display: inline-flex;
  padding: 4px 8px;
  border-radius: 999px;
  background: #f1f2f4;
  color: var(--color-text-sub);
}

.card-copy h3 {
  margin: 8px 0 5px;
  letter-spacing: -.45px;
}

.card-copy p {
  display: block;
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.55;
  white-space: normal;
  word-break: keep-all;
  overflow-wrap: break-word;
}

.benefit-summary {
  margin-top: 16px;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  border-radius: 13px;
  background: #f7f7f8;
  overflow: hidden;
}

.benefit-summary > div {
  min-width: 0;
  min-height: 68px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.benefit-summary > div + div {
  border-left: 1px solid var(--color-divider);
}

.benefit-summary span {
  display: block;
  color: var(--color-text-muted);
  white-space: nowrap;
}

.benefit-summary strong {
  display: block;
  margin-top: 4px;
  white-space: nowrap;
}

.benefit-summary--two-column {
  grid-template-columns: 1fr 1fr;
}

.benefit-summary div {
  min-width: 0;
}

.benefit-summary span {
  display: block;
  color: var(--color-text-muted);
  white-space: nowrap;
}

.benefit-summary strong {
  display: block;
  margin-top: 4px;
  white-space: nowrap;
}

.benefit-summary div:first-child strong {
  color: #d39100;
}

.benefit-summary strong.negative {
  color: var(--color-error) !important;
}

.detail-button {
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
}

.error-state {
  margin-top: 18px;
}

.error-state p {
  margin: 7px 0 16px;
  color: var(--color-text-sub);
}

.loading-highlight {
  font-weight: 700;
  color: var(--color-text-main);
}

@media (max-width: 380px) {
  .card-recommendation-page :deep(.page-header) {
    padding: 0 20px;
  }

  .recommendation-content-start {
    padding-right: 20px;
    padding-left: 20px;
  }
}
</style>