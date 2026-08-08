<template>
  <div class="kb-mobile-page card-recommendation-page">
    <PageHeader
        title="맞춤 카드 추천"
        :custom-back="true"
        @back="goBack"
    />

    <div class="recommendation-content-start">
    <div
        v-if="message"
        :class="[
        'kb-toast',
        messageType === 'success'
          ? 'kb-toast--success'
          : messageType === 'info'
            ? 'kb-toast--info'
            : 'kb-toast--error',
      ]"
    >
      {{ message }}
    </div>

    <section class="recommendation-intro kb-card">
      <div class="intro-icon" aria-hidden="true">
        <i class="fa-solid fa-wand-magic-sparkles"></i>
      </div>
      <div>
        <span class="text-13-bold">12개월 소비분석 기반</span>
        <h2 class="text-20-bold">내 소비에 맞는 카드를 비교했어요</h2>
        <p class="text-13">실제 결제내역과 카드별 전월 실적·월 할인 한도를 반영합니다.</p>
      </div>
    </section>

    <div v-if="loading" class="kb-card kb-loading recommendation-loading">
      <div class="spinner-border kb-spinner"></div>
      <div class="text-13">1년간의 소비분석 결과로 가장 많이 할인되는 카드를 찾고 있어요</div>
      <small class="text-13">화면을 벗어나도 추천 작업은 계속 진행되며, 다시 들어오면 결과를 확인할 수 있어요.</small>
    </div>

    <template v-else-if="recommendationData">
      <section v-if="recommendationData.aiCardRecommendationSummary" class="ai-summary kb-card">
        <div class="ai-summary__label text-13-bold">
          <i class="fa-solid fa-wand-magic-sparkles"></i>
          AI 추천 요약
        </div>
        <p class="text-13">{{ recommendationData.aiCardRecommendationSummary }}</p>
      </section>

      <section class="filter-section">
        <div class="section-label-row">
          <h2 class="text-15-bold">카드 유형</h2>
          <span class="text-13">{{ recommendationData.analysisPeriod }}개월 분석</span>
        </div>
        <div class="segmented-control card-type-control" role="tablist" aria-label="카드 유형">
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

      <section class="filter-section">
        <div class="section-label-row">
          <h2 class="text-15-bold">비교 기준</h2>
        </div>
        <div class="segmented-control fee-mode-control" role="tablist" aria-label="연회비 적용 방식">
          <button
              v-for="option in feeModeOptions"
              :key="option.value"
              type="button"
              :class="['text-13-bold', { active: selectedFeeMode === option.value }]"
              @click="changeFeeMode(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
        <p class="filter-description text-13">{{ selectedFeeModeDescription }}</p>
      </section>

      <section class="kb-section recommendation-list-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">{{ selectedCardTypeLabel }} 추천 TOP 3</h2>
          <span class="result-count text-13">{{ recommendations.length }}개</span>
        </div>

        <div v-if="recommendations.length" class="recommendation-list">
          <article
              v-for="card in recommendations"
              :key="card.cardRecommendationId"
              class="recommendation-item kb-card"
          >
            <div class="rank-badge" :class="`rank-${card.recommendationRank}`">
              {{ card.recommendationRank }}위
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

            <div
                class="benefit-summary"
                :class="{
                  'benefit-summary--two-column': selectedFeeMode !== 'NET_BENEFIT',
                }"
            >
              <div>
                <span class="text-13">{{ displayBenefitLabel }}</span>
                <strong class="text-15-bold" :class="{ negative: Number(card.displayBenefitAmount) < 0 }">
                  {{ formatSignedAmount(card.displayBenefitAmount) }}원
                </strong>
              </div>
              <div v-if="selectedFeeMode === 'NET_BENEFIT'">
                <span class="text-13">예상 할인액</span>
                <strong class="text-15-bold">{{ formatCardAmount(card.expectedBenefitAmount) }}원</strong>
              </div>
              <div>
                <span class="text-13">연회비</span>
                <strong class="text-15-bold">{{ formatCardAmount(card.annualFee) }}원</strong>
              </div>
            </div>

            <button
                type="button"
                class="detail-button text-13-bold"
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
  CARD_RECOMMENDATION_FEE_MODES,
  formatCardAmount,
  getCardImagePath,
  getCardRecommendationErrorMessage,
  getCardTypeLabel,
  getFeeModeLabel,
  normalizeCardType,
  normalizeFeeMode,
} from '@/util/cardRecommendation';

const route = useRoute();
const router = useRouter();
const cardTypeOptions = CARD_RECOMMENDATION_CARD_TYPES;
const feeModeOptions = CARD_RECOMMENDATION_FEE_MODES;

const spendingAnalysisId = Number(route.params.spendingAnalysisId);
const selectedCardType = ref(normalizeCardType(route.query.cardType));
const selectedFeeMode = ref(normalizeFeeMode(route.query.feeMode));
const recommendationData = ref(null);
const creationResult = ref(null);
const loading = ref(false);
const message = ref('');
const messageType = ref('info');

const recommendations = computed(
    () => recommendationData.value?.recommendations ?? [],
);

const selectedCardTypeLabel = computed(
    () => getCardTypeLabel(selectedCardType.value),
);

const selectedFeeModeLabel = computed(
    () => getFeeModeLabel(selectedFeeMode.value),
);

const selectedFeeModeDescription = computed(
    () =>
        feeModeOptions.find((option) => option.value === selectedFeeMode.value)
            ?.description ?? '',
);

const displayBenefitLabel = computed(() =>
    selectedFeeMode.value === 'NET_BENEFIT'
        ? '연회비 차감 후 예상 할인액'
        : '예상 할인액',
);

const creationStatusLabel = computed(() => {
  if (!creationResult.value) return '확인 전';
  return creationResult.value.created ? '새 추천 생성' : '기존 추천 재사용';
});

const isValidAnalysisId = () =>
    Number.isInteger(spendingAnalysisId) && spendingAnalysisId > 0;

const updateQuery = () => {
  router.replace({
    name: 'card-recommendation',
    params: {spendingAnalysisId},
    query: {
      cardType: selectedCardType.value,
      feeMode: selectedFeeMode.value,
    },
  });
};

const loadRecommendationList = async () => {
  recommendationData.value = await cardRecommendationApi.getRecommendations(
      spendingAnalysisId,
      selectedCardType.value,
      selectedFeeMode.value,
  );
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

    await loadRecommendationList();
    messageType.value = status?.created ? 'success' : 'info';
    message.value =
        status?.message || '카드 추천 결과를 불러왔습니다.';
  } catch (error) {
    recommendationData.value = null;
    messageType.value = 'error';
    message.value = getCardRecommendationErrorMessage(
        error,
        '카드 추천 목록을 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const applyTaskStatus = async (status) => {
  const currentStatus = status?.status ?? 'IDLE';

  if (currentStatus === 'COMPLETED') {
    await completeRecommendationLoading(status);
    return;
  }

  if (currentStatus === 'FAILED') {
    stopStatusPolling();
    loading.value = false;
    recommendationData.value = null;
    messageType.value = 'error';
    message.value =
        status?.message || '카드 추천 생성에 실패했습니다.';
    return;
  }

  loading.value = true;
  messageType.value = 'info';
  message.value =
      status?.message || '카드 혜택을 계산하고 있습니다.';
};

const checkRecommendationStatus = async () => {
  try {
    const status = await cardRecommendationApi.getStatus(
        spendingAnalysisId,
    );
    await applyTaskStatus(status);
  } catch (error) {
    stopStatusPolling();
    loading.value = false;
    recommendationData.value = null;
    messageType.value = 'error';
    message.value = getCardRecommendationErrorMessage(
        error,
        '카드 추천 진행 상태를 확인하지 못했습니다.',
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
    messageType.value = 'error';
    message.value = '올바른 소비분석 ID가 필요합니다.';
    return;
  }

  stopStatusPolling();
  loading.value = true;
  recommendationData.value = null;
  message.value = '';

  try {
    const currentStatus = await cardRecommendationApi.getStatus(
        spendingAnalysisId,
    );

    if (currentStatus?.status === 'COMPLETED') {
      await completeRecommendationLoading(currentStatus);
      return;
    }

    if (currentStatus?.status === 'PROCESSING') {
      await applyTaskStatus(currentStatus);
      startStatusPolling();
      return;
    }

    const startedStatus = await cardRecommendationApi.startAsync(
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
    messageType.value = 'error';
    message.value = getCardRecommendationErrorMessage(
        error,
        '카드 추천 작업을 시작하지 못했습니다.',
    );
  }
};

const reloadListOnly = async () => {
  loading.value = true;
  message.value = '';

  try {
    await loadRecommendationList();
    updateQuery();
  } catch (error) {
    messageType.value = 'error';
    message.value = getCardRecommendationErrorMessage(
        error,
        '카드 추천 목록을 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const changeCardType = async (cardType) => {
  if (selectedCardType.value === cardType) return;
  selectedCardType.value = cardType;
  await reloadListOnly();
};

const changeFeeMode = async (feeMode) => {
  if (selectedFeeMode.value === feeMode) return;
  selectedFeeMode.value = feeMode;
  await reloadListOnly();
};

const openDetail = (cardRecommendationId) =>
    router.push({
      name: 'card-recommendation-detail',
      params: {cardRecommendationId},
      query: {
        spendingAnalysisId,
        cardType: selectedCardType.value,
        feeMode: selectedFeeMode.value,
      },
    });

const goBack = () =>
    router.push({
      name: 'analysis-result',
      params: {spendingAnalysisId},
    });

const formatSignedAmount = (value) => {
  const amount = Number(value ?? 0);
  return amount < 0
      ? `-${formatCardAmount(Math.abs(amount))}`
      : formatCardAmount(amount);
};

const getCardInitial = (cardName = '') => {
  const normalized = String(cardName)
      .replace(/KB국민/g, '')
      .replace(/카드/g, '')
      .trim();
  return normalized.slice(0, 12) || 'CARD';
};

onMounted(reloadRecommendations);
onBeforeUnmount(stopStatusPolling);
</script>

<style scoped>
.card-recommendation-page {
  padding-bottom: 36px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.recommendation-content-start {
  /*
   * 팀 협의 후 PageHeader와 첫 콘텐츠 사이 간격을 적용할 경우
   * 아래 주석을 해제합니다.
   * margin-top: 14px;
   */
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

.recommendation-intro p {
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.55;
}

.recommendation-loading small {
  display: block;
  margin-top: 6px;
  color: var(--color-text-muted);
}

.ai-summary {
  position: relative;
  margin-top: 14px;
  padding: 18px 18px 17px;
  background: linear-gradient(135deg, #fff8dc 0%, #fff 72%);
  border: 1px solid #f4df99;
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
  color: #4d4430;
  line-height: 1.7;
}

.filter-section {
  margin-top: 18px;
}

.section-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 2px 9px;
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

.card-type-control,
.fee-mode-control {
  grid-template-columns: repeat(2, 1fr);
}

.filter-description {
  margin: 7px 3px 0;
}

.recommendation-list-section {
  margin-top: 22px;
}

.recommendation-list {
  display: grid;
  gap: 12px;
}

.recommendation-item {
  position: relative;
  padding: 18px;
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
  display: -webkit-box;
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.55;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.benefit-summary {
  margin-top: 16px;
  padding: 13px 12px;
  display: grid;
  grid-template-columns: 1.3fr 1fr 1fr;
  gap: 7px;
  border-radius: 13px;
  background: #f7f7f8;
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
</style>
