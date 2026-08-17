<template>
  <div class="kb-mobile-page card-detail-page">
    <PageHeader
        title="카드 추천 상세"
        :showBack="true"
        :customBack="true"
        @back="goBack"
    />

    <div class="detail-content-start">

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

        <section class="recommendation-summary kb-card">
          <div class="summary-title text-15-bold">
            <i class="fa-solid fa-wand-magic-sparkles"></i>
            AI 추천 결과
          </div>

          <div class="benefit-main">
            <span class="text-15-bold">예상 혜택 금액</span>
            <strong class="text-18-bold">
              {{ formatCardAmount(detail.expectedBenefitAmount) }}원
            </strong>
          </div>

          <div class="benefit-meta">
            <div>
              <span class="text-13">연회비</span>
              <strong class="text-13-bold">
                {{ formatCardAmount(detail.annualFee) }}원
              </strong>
            </div>

            <div>
              <span class="text-13">실질 혜택</span>
              <strong
                  class="text-13-bold"
                  :class="{ negative: Number(detail.netBenefitAmount) < 0 }"
              >
                {{ formatSignedAmount(detail.netBenefitAmount) }}원
              </strong>
            </div>
          </div>

          <div v-if="detail.aiRecommendationSummary" class="ai-reason">
            <button
                type="button"
                class="ai-reason__toggle"
                :aria-expanded="aiSummaryOpen"
                @click="aiSummaryOpen = !aiSummaryOpen"
            >
              <span class="text-13-bold">왜 추천했나요?</span>

              <span class="ai-reason__action text-13">
        {{ aiSummaryOpen ? '접기' : '자세히' }}
        <i
            class="fa-solid fa-chevron-down"
            :class="{ open: aiSummaryOpen }"
        ></i>
      </span>
            </button>

            <p v-if="aiSummaryOpen" class="ai-reason__content text-13">
              {{ formatWonInText(detail.aiRecommendationSummary) }}
            </p>
          </div>
        </section>

        <section class="kb-section">
          <div class="kb-section-title-row">
            <div class="benefit-title-wrap">
              <h2 class="kb-section-title text-18-bold">혜택 계산 근거</h2>
              <button
                  type="button"
                  class="benefit-info-button"
                  :aria-expanded="calculationInfoOpen"
                  aria-label="혜택 계산 기준 안내"
                  @click="calculationInfoOpen = !calculationInfoOpen"
              >
                <i class="fa-solid fa-circle-info"></i>
              </button>
            </div>
            <span class="benefit-count text-13">{{ benefits.length }}개 혜택</span>
          </div>

          <div v-if="calculationInfoOpen" class="benefit-guide">
            <strong class="text-13-bold">계산 기준 안내</strong>
            <dl>
              <div><dt>소비금액</dt><dd>해당 혜택이 적용되는 내 소비금액</dd></div>
              <div><dt>거래건수</dt><dd>혜택 대상에 포함된 결제 건수</dd></div>
              <div><dt>혜택 방식</dt><dd>할인율 또는 할인 방식</dd></div>
              <div><dt>월 할인 한도</dt><dd>한 달에 받을 수 있는 최대 할인액</dd></div>
              <div><dt>전월 실적</dt><dd>혜택 적용에 필요한 전월 이용금액</dd></div>
              <div><dt>적용 기간</dt><dd>분석 기간 중 혜택이 적용된 개월 수</dd></div>
            </dl>
          </div>

          <div v-if="benefits.length" class="benefit-list">
            <article
                v-for="benefit in benefits"
                :key="benefit.cardBenefitId"
                class="benefit-item kb-card"
            >
              <button
                  type="button"
                  class="benefit-summary"
                  :aria-expanded="isBenefitOpen(benefit.cardBenefitId)"
                  @click="toggleBenefit(benefit.cardBenefitId)"
              >
                <div class="category-icon">
                  <i :class="getCategoryIcon(benefit.categoryName)"></i>
                </div>

                <div class="benefit-summary__info">
                  <span class="text-13">{{ benefit.categoryName }}</span>
                  <h3 class="text-15-bold">{{ benefit.benefitName }}</h3>
                </div>

                <div class="benefit-summary__right">
                  <strong class="text-15-bold">
                    {{ formatCardAmount(benefit.expectedBenefitAmount) }}원
                  </strong>
                  <span class="benefit-summary__action text-13">
                    {{ isBenefitOpen(benefit.cardBenefitId) ? '접기' : '상세보기' }}
                    <i
                        class="fa-solid fa-chevron-down"
                        :class="{ open: isBenefitOpen(benefit.cardBenefitId) }"
                    ></i>
                  </span>
                </div>
              </button>

              <div
                  v-if="isBenefitOpen(benefit.cardBenefitId)"
                  class="benefit-detail"
              >
                <p v-if="benefit.benefitDescription" class="benefit-description text-13">
                  {{ benefit.benefitDescription }}
                </p>

                <dl class="benefit-detail-grid">
                  <div>
                    <dt class="text-13">소비금액</dt>
                    <dd class="text-13-bold">{{ formatCardAmount(benefit.eligibleSpendingAmount) }}원</dd>
                  </div>
                  <div>
                    <dt class="text-13">거래건수</dt>
                    <dd class="text-13-bold">{{ formatCardAmount(benefit.eligibleTransactionCount) }}건</dd>
                  </div>
                  <div>
                    <dt class="text-13">혜택 방식</dt>
                    <dd class="text-13-bold">{{ formatBenefitRule(benefit) }}</dd>
                  </div>
                  <div>
                    <dt class="text-13">월 할인 한도</dt>
                    <dd class="text-13-bold">{{ formatLimit(benefit.monthlyLimit) }}</dd>
                  </div>
                  <div>
                    <dt class="text-13">전월 실적</dt>
                    <dd class="text-13-bold">{{ formatMinimumSpending(benefit.minimumSpendingAmount) }}</dd>
                  </div>
                  <div>
                    <dt class="text-13">적용 기간</dt>
                    <dd class="text-13-bold">
                      {{ formatCardAmount(benefit.eligibleMonthCount) }}개월
                    </dd>
                  </div>
                </dl>
              </div>
            </article>
          </div>

          <div v-else class="kb-card kb-empty-state">
            <div class="kb-empty-state__icon"><i class="fa-solid fa-calculator"></i></div>
            <strong class="text-15-bold">표시할 혜택 계산 근거가 없습니다.</strong>
          </div>
        </section>



        <div class="bottom-btn-area double card-detail-actions">
          <button
              type="button"
              class="bottom-btn card-detail-secondary"
              @click="goBack"
          >
            추천 목록으로
          </button>
          <button
              type="button"
              class="bottom-btn"
              @click="checkApplication"
          >
            카드 신청
            <i class="fa-solid fa-arrow-up-right-from-square"></i>
          </button>
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
const aiSummaryOpen = ref(false);
const calculationInfoOpen = ref(false);
const openBenefitIds = ref(new Set());

const spendingAnalysisId = computed(() =>
    toPositiveInteger(
        route.params.spendingAnalysisId,
        route.query.spendingAnalysisId,
        detail.value?.spendingAnalysisId,
    ),
);
const benefits = computed(() => detail.value?.benefits ?? []);

const isBenefitOpen = (benefitId) => openBenefitIds.value.has(benefitId);

const toggleBenefit = (benefitId) => {
  const next = new Set(openBenefitIds.value);

  if (next.has(benefitId)) {
    next.delete(benefitId);
  } else {
    next.add(benefitId);
  }

  openBenefitIds.value = next;
};

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

const formatWonInText = (text) => {
  if (!text) return '';

  return String(text).replace(/(\d+)\s*원/g, (_, number) => {
    return `${Number(number).toLocaleString('ko-KR')}원`;
  });
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
  min-height: 100dvh;
  padding-bottom: 38px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.card-detail-page :deep(.page-header) {
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

.card-hero {
  padding: 18px 16px;
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
  height: 138px;
  margin: 4px 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-visual img {
  max-width: 205px;
  max-height: 126px;
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

.recommendation-summary {
  margin-top: 16px;
  padding: 18px 16px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.summary-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #a87500;
}

.benefit-main {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.benefit-main strong {
  color: #d39100;
  white-space: nowrap;
}

.benefit-meta {
  margin-top: 16px;
  padding: 12px 0;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  border-top: 1px solid var(--color-divider);
  border-bottom: 1px solid var(--color-divider);
}

.benefit-meta > div {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 0;
}

.benefit-meta > div + div {
  padding-left: 0;
  border-left: 1px solid var(--color-divider);
}

.benefit-meta span,
.benefit-meta strong {
  white-space: nowrap;
}

.benefit-meta strong {
  white-space: nowrap;
}

.negative {
  color: var(--color-error) !important;
}

.ai-reason__toggle {
  width: 100%;
  padding: 14px 0 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 0;
  background: transparent;
  color: var(--color-text-main);
  font: inherit;
  cursor: pointer;
}

.ai-reason__action {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-muted);
}

.ai-reason__action i {
  font-size: 11px;
  transition: transform 0.2s ease;
}

.ai-reason__action i.open {
  transform: rotate(180deg);
}

.ai-reason__content {
  margin: 12px 0 0;
  padding: 12px;
  border-radius: 10px;
  background: #fff9e8;
  color: #554c38;
  line-height: 1.7;
}

.recommendation-summary__top h2 {
  max-width: 205px;
  margin: 7px 0 0;
  line-height: 1.45;
  word-break: keep-all;
}

.summary-benefit {
  flex: 0 0 auto;
  padding-top: 2px;
  text-align: right;
}

.summary-benefit span,
.summary-meta span {
  display: block;
  color: var(--color-text-muted);
}

.summary-benefit strong {
  display: block;
  margin-top: 4px;
  color: #d39100;
  white-space: nowrap;
}

.summary-meta {
  margin-top: 16px;
  padding: 12px 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  border-top: 1px solid var(--color-divider);
  border-bottom: 1px solid var(--color-divider);
}

.summary-meta > div {
  padding: 0 12px;
}

.summary-meta > div:first-child {
  padding-left: 0;
  border-right: 1px solid var(--color-divider);
}

.summary-meta > div:last-child {
  padding-right: 0;
}

.summary-meta strong {
  display: block;
  margin-top: 4px;
}

.negative {
  color: var(--color-error) !important;
}

.ai-reason {
  margin-top: 2px;
}

.ai-reason__toggle {
  width: 100%;
  padding: 13px 0 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 0;
  background: transparent;
  color: var(--color-text-main);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.ai-reason__action {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--color-text-muted);
}

.ai-reason__action i {
  font-size: 11px;
  transition: transform .2s ease;
}

.ai-reason__action i.open {
  transform: rotate(180deg);
}

.ai-reason__content {
  margin: 12px 0 0;
  padding: 12px;
  border-radius: 12px;
  background: #fff9e8;
  color: #554c38;
  line-height: 1.7;
  word-break: keep-all;
}

.benefit-count {
  color: var(--color-text-muted);
}

.benefit-title-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.benefit-info-button {
  width: 22px;
  height: 22px;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 15px;
  cursor: pointer;
}

.benefit-guide {
  margin-top: 10px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #fff8e7;
}

.benefit-guide > strong {
  display: block;
  margin-bottom: 8px;
  color: #a87300;
}

.benefit-guide dl {
  margin: 0;
  display: grid;
  gap: 6px;
}

.benefit-guide dl > div {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr);
  gap: 8px;
  font-size: 12px;
  line-height: 1.45;
}

.benefit-guide dt {
  font-weight: 700;
  color: var(--color-text-main);
}

.benefit-guide dd {
  margin: 0;
  color: var(--color-text-sub);
}

.benefit-list {
  display: grid;
  gap: 5px;
}

.benefit-item {
  padding: 0;
  overflow: hidden;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.benefit-summary {
  width: 100%;
  padding: 16px;
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  border: 0;
  background: transparent;
  color: var(--color-text-main);
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.category-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #fff3c8;
  color: #d49400;
  font-size: 14px;
}

.benefit-summary__info {
  min-width: 0;
}

.benefit-summary__info > span {
  display: block;
  color: var(--color-text-muted);
}

.benefit-summary__info h3 {
  margin: 2px 0 0;
}

.benefit-summary__right {
  align-self: stretch;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  padding: 1px 0;
}

.benefit-summary__right > strong {
  color: #d39100;
  white-space: nowrap;
}

.benefit-summary__action {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--color-text-muted);
  white-space: nowrap;
}

.benefit-summary__action i {
  font-size: 10px;
  transition: transform .2s ease;
}

.benefit-summary__action i.open {
  transform: rotate(180deg);
}

.benefit-detail {
  margin: 0 16px;
  padding: 0 0 16px;
  border-top: 1px solid var(--color-divider);
}

.benefit-description {
  margin: 12px 0;
  padding: 10px 12px;
  border-radius: 10px;
  background: #f7f7f8;
  color: var(--color-text-sub);
  line-height: 1.6;
}

.benefit-detail-grid {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.benefit-detail-grid > div {
  min-width: 0;
  padding: 8px 10px;
}

.benefit-detail-grid > div:nth-child(even) {
  border-left: 1px solid var(--color-divider);
}

.benefit-detail-grid dt {
  color: var(--color-text-muted);
}

.benefit-detail-grid dd {
  margin: 3px 0 0;
  color: var(--color-text-main);
}

.card-detail-actions {
  margin-top: 18px;
}

.card-detail-secondary {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
  color: var(--color-text-main);
}

.card-detail-secondary:active:not(:disabled) {
  background: var(--color-bg-screen);
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

.card-detail-actions .bottom-btn {
  font-size: 16px;
  font-weight: 600;
}

@media (max-width: 380px) {
  .card-detail-page :deep(.page-header) {
    padding: 0 20px;
  }

  .detail-content-start {
    padding-right: 20px;
    padding-left: 20px;
  }

  .comparison-amount-grid {
    grid-template-columns: 1fr;
  }
}
</style>