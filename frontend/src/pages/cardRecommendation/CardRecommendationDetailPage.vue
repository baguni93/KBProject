<template>
  <div class="kb-mobile-page card-detail-page">
    <PageHeader
        title="카드 추천 상세"
        :custom-back="true"
        @back="goBack"
    />

    <div class="detail-content-start">
    <div v-if="message" class="kb-toast kb-toast--error">{{ message }}</div>

    <div v-if="loading" class="kb-card kb-loading">
      <div class="spinner-border kb-spinner"></div>
      <div class="text-13">카드 추천 상세를 불러오는 중이에요.</div>
    </div>

    <template v-else-if="detail">
      <section class="card-hero kb-card">
        <div class="hero-topline">
          <span class="rank-label text-13-bold">{{ detail.recommendationRank }}위 추천</span>
          <span class="text-13-bold">{{ getCardTypeLabel(detail.cardType) }}</span>
        </div>

        <div class="card-visual">
          <img
            v-if="getCardImagePath(detail.cardImage)"
            :src="getCardImagePath(detail.cardImage)"
            :alt="`${detail.cardName} 카드 이미지`"
          />
          <div v-else class="card-placeholder" aria-hidden="true">
            <span>KB</span>
            <i class="fa-regular fa-credit-card"></i>
            <strong>{{ getCardInitial(detail.cardName) }}</strong>
          </div>
        </div>

        <h2 class="text-20-bold">{{ detail.cardName }}</h2>
        <p class="text-13">{{ detail.cardDescription }}</p>
      </section>

      <section class="amount-card kb-card">
        <div class="amount-card__title">
          <span class="text-15-bold">12개월 예상 혜택</span>
          <small class="text-13">연회비 반영 전·후 금액을 함께 비교해요.</small>
        </div>

        <div class="comparison-amount-grid">
          <div class="comparison-amount comparison-amount--primary">
            <span class="text-13-bold">연회비 제외</span>
            <strong class="text-18-bold">{{ formatCardAmount(detail.expectedBenefitAmount) }}원</strong>
            <small class="text-13">카드 혜택으로 받는 예상 할인액</small>
          </div>

          <div class="comparison-amount">
            <span class="text-13-bold">연회비 포함</span>
            <strong class="text-18-bold" :class="{ negative: Number(detail.netBenefitAmount) < 0 }">
              {{ formatSignedAmount(detail.netBenefitAmount) }}원
            </strong>
            <small class="text-13">예상 할인액에서 연회비를 차감한 금액</small>
          </div>
        </div>

        <div class="annual-fee-row">
          <span class="text-13">적용 연회비</span>
          <strong class="text-15-bold">{{ formatCardAmount(detail.annualFee) }}원</strong>
        </div>
      </section>

      <section v-if="detail.aiCardRecommendationSummary" class="ai-summary kb-card">
        <div class="text-13-bold"><i class="fa-solid fa-wand-magic-sparkles"></i> AI 추천 분석</div>
        <p class="text-13">{{ detail.aiCardRecommendationSummary }}</p>
      </section>

      <section class="kb-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title text-20-bold">혜택 계산 근거</h2>
          <span class="benefit-count text-13">{{ benefits.length }}개 혜택</span>
        </div>

        <div v-if="benefits.length" class="benefit-list">
          <article v-for="benefit in benefits" :key="benefit.cardBenefitId" class="benefit-item kb-card">
            <div class="benefit-heading">
              <div class="category-icon">
                <i :class="getCategoryIcon(benefit.categoryName)"></i>
              </div>
              <div>
                <span class="text-13">{{ benefit.categoryName }}</span>
                <h3 class="text-15-bold">{{ benefit.benefitName }}</h3>
              </div>
              <strong class="text-15-bold">{{ formatCardAmount(benefit.expectedBenefitAmount) }}원</strong>
            </div>

            <p class="text-13">{{ benefit.benefitDescription }}</p>

            <dl class="calculation-grid">
              <div>
                <dt class="text-13">혜택 대상 소비</dt>
                <dd>{{ formatCardAmount(benefit.eligibleSpendingAmount) }}원</dd>
              </div>
              <div>
                <dt class="text-13">대상 거래</dt>
                <dd>{{ formatCardAmount(benefit.eligibleTransactionCount) }}건</dd>
              </div>
              <div>
                <dt class="text-13">혜택 적용 월</dt>
                <dd>{{ formatCardAmount(benefit.eligibleMonthCount) }}개월</dd>
              </div>
              <div>
                <dt class="text-13">전월 실적 조건</dt>
                <dd class="text-13-bold">{{ formatMinimumSpending(benefit.minimumSpendingAmount) }}</dd>
              </div>
              <div>
                <dt class="text-13">혜택 방식</dt>
                <dd class="text-13-bold">{{ formatBenefitRule(benefit) }}</dd>
              </div>
              <div>
                <dt class="text-13">월 할인 한도</dt>
                <dd class="text-13-bold">{{ formatLimit(benefit.monthlyLimit) }}</dd>
              </div>
            </dl>
          </article>
        </div>

        <div v-else class="kb-card kb-empty-state">
          <div class="kb-empty-state__icon"><i class="fa-solid fa-calculator"></i></div>
          <strong class="text-15-bold">표시할 혜택 계산 근거가 없습니다.</strong>
        </div>
      </section>



      <div class="page-actions">
        <button type="button" class="content-btn secondary" @click="goBack">추천 목록으로</button>
        <button type="button" class="content-btn primary" @click="checkApplication">카드 신청 하러 가기</button>
      </div>

      <div v-if="applicationMessage" class="kb-toast kb-toast--info application-message">
        {{ applicationMessage }}
      </div>
    </template>

    <div v-else-if="!loading" class="kb-card kb-empty-state error-state">
      <div class="kb-empty-state__icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
      <strong class="text-15-bold">상세 정보를 불러오지 못했습니다.</strong>
      <button type="button" class="content-btn primary" @click="loadDetail">다시 시도</button>
    </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import cardRecommendationApi from '@/api/cardRecommendationApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { getCategoryIcon } from '@/util/analysis';
import {
  formatCardAmount,
  getCardImagePath,
  getCardRecommendationErrorMessage,
  getCardTypeLabel,
  normalizeCardType,
  normalizeFeeMode,
} from '@/util/cardRecommendation';

const route = useRoute();
const router = useRouter();

const toPositiveInteger = (...values) => {
  for (const value of values) {
    if (value === undefined || value === null || value === '') continue;

    const parsed = Number(value);
    if (Number.isInteger(parsed) && parsed > 0) return parsed;
  }

  return null;
};

const cardRecommendationId = toPositiveInteger(
  route.params.cardRecommendationId,
);
const selectedCardType = normalizeCardType(route.query.cardType);
const listFeeMode = normalizeFeeMode(route.query.feeMode);
const detail = ref(null);
const loading = ref(false);
const message = ref('');
const applicationMessage = ref('');

const spendingAnalysisId = computed(() =>
  toPositiveInteger(
    route.params.spendingAnalysisId,
    route.query.spendingAnalysisId,
    detail.value?.spendingAnalysisId,
  ),
);
const benefits = computed(() => detail.value?.benefits ?? []);

const loadDetail = async () => {
  if (!Number.isInteger(cardRecommendationId) || cardRecommendationId <= 0) {
    detail.value = null;
    message.value = '올바른 카드 추천 ID가 필요합니다.';
    return;
  }

  loading.value = true;
  message.value = '';
  applicationMessage.value = '';

  try {
    detail.value = await cardRecommendationApi.getRecommendationDetail(
      cardRecommendationId,
      listFeeMode,
    );
  } catch (error) {
    detail.value = null;
    message.value = getCardRecommendationErrorMessage(
      error,
      '카드 추천 상세를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  const analysisId = spendingAnalysisId.value;

  if (!analysisId) {
    router.push({ name: 'analysis-main' });
    return;
  }

  router.push({
    name: 'card-recommendation',
    params: { spendingAnalysisId: analysisId },
    query: {
      cardType: detail.value?.cardType ?? selectedCardType,
      feeMode: listFeeMode,
    },
  });
};

const checkApplication = () => {
  const application = detail.value?.application;
  if (!application) {
    applicationMessage.value = '카드 신청 연결값이 등록되어 있지 않습니다.';
    return;
  }

  if (/^https?:\/\//i.test(application)) {
    window.open(application, '_blank', 'noopener,noreferrer');
    return;
  }

  applicationMessage.value =
    `현재 DB 신청 연결값은 “${application}”입니다. 실제 KB 카드 신청 URL 연동 전 테스트 값입니다.`;
};

const formatSignedAmount = (value) => {
  const amount = Number(value ?? 0);
  return amount < 0
    ? `-${formatCardAmount(Math.abs(amount))}`
    : formatCardAmount(amount);
};

const formatMinimumSpending = (value) => {
  const amount = Number(value ?? 0);
  return amount > 0 ? `${formatCardAmount(amount)}원 이상` : '조건 없음';
};

const formatLimit = (value) => {
  const amount = Number(value ?? 0);
  return amount > 0 ? `${formatCardAmount(amount)}원` : '한도 없음';
};

const formatBenefitRule = (benefit) => {
  const rate = Number(benefit.benefitRate ?? 0);
  if (rate > 0) return `${rate}% 할인`;

  const amount = Number(benefit.benefitAmount ?? 0);
  return amount > 0 ? `건당 ${formatCardAmount(amount)}원` : '혜택 없음';
};

const getCardInitial = (cardName = '') => {
  const normalized = String(cardName)
    .replace(/KB국민/g, '')
    .replace(/카드/g, '')
    .trim();
  return normalized.slice(0, 12) || 'CARD';
};

onMounted(loadDetail);
</script>

<style scoped>
.card-detail-page {
  padding-bottom: 38px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.detail-content-start {
  /*
   * 팀 협의 후 PageHeader와 첫 콘텐츠 사이 간격을 적용할 경우
   * 아래 주석을 해제합니다.
   * margin-top: 14px;
   */
}

.card-hero {
  padding: 18px;
  text-align: center;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.hero-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hero-topline span {
  padding: 5px 9px;
  border-radius: 999px;
  background: #f1f2f4;
  color: var(--color-text-sub);
}

.hero-topline .rank-label {
  background: var(--color-primary);
  color: var(--color-text-main);
}

.card-visual {
  height: 150px;
  margin: 4px 0 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-visual img {
  max-width: 220px;
  max-height: 138px;
  object-fit: contain;
  filter: drop-shadow(0 8px 12px rgba(0, 0, 0, .14));
}

.card-placeholder {
  width: 208px;
  height: 124px;
  padding: 17px;
  display: grid;
  grid-template-columns: 1fr auto;
  grid-template-rows: auto 1fr;
  align-items: start;
  border-radius: 13px;
  background: linear-gradient(135deg, #484848, #161616);
  color: var(--color-text-white);
  box-shadow: 0 8px 16px rgba(0, 0, 0, .18);
}

.card-placeholder span {
  font-size: 13px;
  font-weight: 900;
}

.card-placeholder i {
  font-size: 22px;
}

.card-placeholder strong {
  align-self: end;
  grid-column: 1 / 3;
  text-align: left;
  font-size: 14px;
}

.card-hero h2 {
  margin: 5px 0 6px;
  letter-spacing: -.5px;
}

.card-hero p {
  margin: 0;
  color: var(--color-text-sub);
  line-height: 1.65;
}

.amount-card {
  margin-top: 14px;
  padding: 18px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.amount-card__title {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.amount-card__title small {
  color: var(--color-text-muted);
  text-align: right;
}

.comparison-amount-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 9px;
}

.comparison-amount {
  min-width: 0;
  padding: 15px 9px;
  border: 1px solid var(--color-divider);
  border-radius: 13px;
  background: #f7f7f8;
  text-align: center;
}

.comparison-amount--primary {
  border-color: #f0d27b;
  background: #fff8df;
}

.comparison-amount span {
  display: block;
  color: var(--color-text-sub);
}

.comparison-amount strong {
  display: block;
  margin-top: 5px;
  color: #333;
  letter-spacing: -.6px;
  white-space: nowrap;
}

.comparison-amount--primary strong {
  color: #d39100;
}

.comparison-amount small {
  display: block;
  min-height: 28px;
  margin-top: 7px;
  color: var(--color-text-muted);
  line-height: 1.45;
  word-break: keep-all;
}

.annual-fee-row {
  margin-top: 11px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 10px;
  background: #f6f6f7;
}

.annual-fee-row span {
  color: var(--color-text-sub);
}

.negative {
  color: var(--color-error) !important;
}

.ai-summary {
  margin-top: 14px;
  padding: 17px;
  background: #fff9e5;
  border: 1px solid #f3dfa0;
  box-shadow: none;
}

.ai-summary > div {
  color: #927000;
}

.ai-summary p {
  margin: 8px 0 0;
  color: #4f4735;
  line-height: 1.7;
}

.benefit-count {
  color: var(--color-text-muted);
}

.benefit-list {
  display: grid;
  gap: 11px;
}

.benefit-item {
  padding: 16px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.benefit-heading {
  display: grid;
  grid-template-columns: 42px 1fr auto;
  gap: 10px;
  align-items: center;
}

.category-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff3c8;
  color: #d49400;
  font-size: 17px;
}

.benefit-heading span {
  display: block;
  color: var(--color-text-muted);
}

.benefit-heading h3 {
  margin: 3px 0 0;
}

.benefit-heading > strong {
  color: #d39100;
  white-space: nowrap;
}

.benefit-item > p {
  margin: 11px 0 0;
  padding: 10px 11px;
  border-radius: 10px;
  background: #f7f7f8;
  color: var(--color-text-sub);
  line-height: 1.6;
}

.calculation-grid {
  margin: 11px 0 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  border-top: 1px solid var(--color-divider);
  border-left: 1px solid var(--color-divider);
}

.calculation-grid div {
  padding: 9px;
  border-right: 1px solid var(--color-divider);
  border-bottom: 1px solid var(--color-divider);
}

.calculation-grid dt {
  color: var(--color-text-muted);
}

.calculation-grid dd {
  margin: 3px 0 0;
}

.page-actions {
  margin-top: 16px;
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 9px;
}

.application-message {
  margin-top: 10px;
  margin-bottom: 0;
}

.error-state {
  margin-top: 18px;
}

.error-state button {
  margin-top: 15px;
}

@media (max-width: 360px) {
  .comparison-amount-grid,
  .page-actions {
    grid-template-columns: 1fr;
  }
}
</style>
