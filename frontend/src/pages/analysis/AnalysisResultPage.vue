<template>
  <div class="kb-mobile-page result-page">
    <header class="kb-app-header">
      <router-link class="kb-icon-button" to="/analysis" aria-label="뒤로가기">
        <i class="fa-solid fa-chevron-left"></i>
      </router-link>
      <h1 class="kb-app-header__title">소비 분석 결과</h1>
      <span></span>
    </header>

    <div
      v-if="message"
      :class="[
        'kb-toast',
        messageType === 'info' ? 'kb-toast--info' : 'kb-toast--error',
      ]"
    >
      {{ message }}
    </div>

    <div v-if="loading" class="kb-card kb-loading">
      <div class="spinner-border kb-spinner"></div>
      <div>분석 결과를 불러오는 중이에요.</div>
    </div>

    <template v-else-if="analysis">
      <section class="result-hero kb-card">
        <div>
          <span class="ai-label">AI 칭호</span>
          <h2>{{ analysis.aiTitle }}</h2>
          <p>{{ analysis.aiAnalysisSummary }}</p>
        </div>
        <div class="hero-icon" aria-hidden="true">
          <i :class="getCategoryIcon(analysis.representativeCategoryName)"></i>
        </div>
      </section>

      <section class="result-summary kb-card">
        <AnalysisDonutChart
          :categories="sortedCategories"
          :total-amount="analysis.totalSpendingAmount"
        />

        <div class="summary-meta">
          <div>
            <span>대표 카테고리</span>
            <strong>{{ analysis.representativeCategoryName }}</strong>
          </div>
          <div>
            <span>분석 기간</span>
            <strong>{{ analysis.periodLabel }}</strong>
          </div>
        </div>

        <div class="summary-stats">
          <div>
            <span>총 소비 금액</span>
            <strong>{{ formatAnalysisNumber(analysis.totalSpendingAmount) }}원</strong>
          </div>
          <div>
            <span>분석 거래</span>
            <strong>{{ formatAnalysisNumber(analysis.classifiedTransactionCount) }}건</strong>
          </div>
          <div>
            <span>분석 기준일</span>
            <strong>{{ analysis.analysisEndDate }}</strong>
          </div>
        </div>
      </section>

      <section class="kb-section">
        <div class="kb-section-title-row">
          <h2 class="kb-section-title">카테고리별 소비</h2>
          <span class="category-count">{{ sortedCategories.length }}개 카테고리</span>
        </div>
        <div class="result-list kb-card">
          <div
            v-for="(category, index) in sortedCategories"
            :key="category.spendingCategoryId"
            class="result-row"
          >
            <div
              class="result-category-icon"
              :style="{
                backgroundColor: `${categoryColor(category, index)}20`,
                color: categoryColor(category, index),
              }"
            >
              <i :class="getCategoryIcon(category.categoryName)"></i>
            </div>
            <div class="result-category-info">
              <div>
                <strong>{{ category.categoryName }}</strong>
                <span>{{ formatRatio(category.spendingRatio) }}%</span>
              </div>
              <div class="result-track">
                <span
                  :style="{
                    width: `${Math.min(Number(category.spendingRatio), 100)}%`,
                    backgroundColor: categoryColor(category, index),
                  }"
                ></span>
              </div>
            </div>
            <div class="result-category-amount">
              <strong>{{ formatAnalysisNumber(category.spendingAmount) }}원</strong>
              <span>{{ category.transactionCount }}건</span>
            </div>
          </div>
        </div>
      </section>

      <section class="ai-insight kb-card">
        <div class="ai-insight__label">
          <i class="fa-solid fa-wand-magic-sparkles"></i> AI 분석
        </div>
        <p>{{ analysis.aiAnalysisSummary }}</p>
        <i class="fa-solid fa-chart-pie ai-insight__icon" aria-hidden="true"></i>
      </section>

      <div class="result-actions">
        <router-link
          class="kb-outline-button d-flex align-items-center justify-content-center text-decoration-none"
          to="/analysis"
        >
          분석 메인으로
        </router-link>
        <button type="button" class="kb-primary-button" @click="shareResult">
          분석 결과 공유하기
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import AnalysisDonutChart from '@/components/analysis/AnalysisDonutChart.vue';
import analysisApi from '@/api/analysisApi';
import {
  formatAnalysisNumber,
  getAnalysisCategoryColor,
  getAnalysisErrorMessage,
  getCategoryIcon,
} from '@/util/analysis';

const route = useRoute();
const analysis = ref(null);
const loading = ref(false);
const message = ref('');
const messageType = ref('error');

const sortedCategories = computed(() =>
  [...(analysis.value?.categories ?? [])].sort(
    (left, right) =>
      Number(right.spendingAmount ?? 0) - Number(left.spendingAmount ?? 0),
  ),
);

const categoryColor = (category, index) =>
  getAnalysisCategoryColor(category.categoryName, index);

const formatRatio = (value) => {
  const ratio = Number(value ?? 0);
  return Number.isInteger(ratio) ? ratio : ratio.toFixed(1);
};

const loadAnalysisDetail = async () => {
  const id = Number(route.params.spendingAnalysisId);
  if (!Number.isInteger(id) || id <= 0) {
    analysis.value = null;
    message.value = '올바른 소비 분석 ID가 필요합니다.';
    return;
  }

  loading.value = true;
  message.value = '';
  messageType.value = 'error';

  try {
    analysis.value = await analysisApi.getAnalysisDetail(id);
  } catch (error) {
    analysis.value = null;
    message.value = getAnalysisErrorMessage(
      error,
      '소비 분석 상세 결과를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const shareResult = () => {
  messageType.value = 'info';
  message.value = '공유 기능은 피드 기능 연결 단계에서 추가할 예정입니다.';
};

watch(() => route.params.spendingAnalysisId, loadAnalysisDetail);
onMounted(loadAnalysisDetail);
</script>

<style scoped>
.result-page {
  padding-bottom: 34px;
}

.result-hero {
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid #ffe19a;
  background: linear-gradient(135deg, #fffaf0, #fff3cc);
  box-shadow: none;
}

.result-hero > div:first-child {
  min-width: 0;
  flex: 1;
}

.ai-label {
  display: inline-flex;
  padding: 4px 9px;
  border-radius: 999px;
  background: #ffeab0;
  color: #9b7000;
  font-size: 10px;
  font-weight: 900;
}

.result-hero h2 {
  margin: 9px 0 7px;
  font-size: 20px;
  font-weight: 900;
  letter-spacing: -0.7px;
}

.result-hero p {
  margin: 0;
  color: #746d5c;
  font-size: 11px;
  line-height: 1.6;
}

.hero-icon {
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 70px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.85);
  color: #e5a200;
  font-size: 29px;
  box-shadow: 0 7px 18px rgba(153, 117, 0, 0.12);
}

.result-summary {
  margin-top: 13px;
  padding: 18px;
  border: 1px solid #ededed;
  box-shadow: none;
}

.summary-meta {
  margin-top: 4px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.summary-meta div {
  padding: 11px 12px;
  border-radius: 12px;
  background: #f8f8f8;
}

.summary-meta span,
.summary-meta strong {
  display: block;
}

.summary-meta span {
  color: #8d8d8d;
  font-size: 9px;
}

.summary-meta strong {
  margin-top: 4px;
  font-size: 12px;
  font-weight: 900;
}

.summary-stats {
  margin-top: 9px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  overflow: hidden;
  border: 1px solid #ececec;
  border-radius: 13px;
}

.summary-stats div {
  position: relative;
  min-width: 0;
  padding: 12px 5px;
  text-align: center;
}

.summary-stats div + div::before {
  content: '';
  position: absolute;
  top: 12px;
  bottom: 12px;
  left: 0;
  width: 1px;
  background: #ececec;
}

.summary-stats span,
.summary-stats strong {
  display: block;
}

.summary-stats span {
  color: #8f8f8f;
  font-size: 8px;
}

.summary-stats strong {
  margin-top: 4px;
  overflow: hidden;
  font-size: 10px;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-count {
  color: #8d8d8d;
  font-size: 10px;
}

.result-list {
  padding: 3px 15px;
  border: 1px solid #ededed;
  box-shadow: none;
}

.result-row {
  min-height: 68px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f1f1f1;
}

.result-row:last-child {
  border-bottom: 0;
}

.result-category-icon {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 38px;
  border-radius: 13px;
  font-size: 14px;
}

.result-category-info {
  min-width: 0;
  flex: 1;
}

.result-category-info > div:first-child {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.result-category-info strong {
  font-size: 11px;
}

.result-category-info span {
  color: #858585;
  font-size: 9px;
}

.result-track {
  height: 6px;
  margin-top: 8px;
  overflow: hidden;
  border-radius: 8px;
  background: #eff0f2;
}

.result-track span {
  display: block;
  height: 100%;
  border-radius: 8px;
}

.result-category-amount {
  min-width: 76px;
  text-align: right;
}

.result-category-amount strong,
.result-category-amount span {
  display: block;
}

.result-category-amount strong {
  font-size: 10px;
  font-weight: 900;
}

.result-category-amount span {
  margin-top: 3px;
  color: #9d9d9d;
  font-size: 8px;
}

.ai-insight {
  position: relative;
  margin-top: 18px;
  padding: 16px 74px 16px 16px;
  overflow: hidden;
  border: 1px solid #bcdcff;
  background: linear-gradient(135deg, #f4f9ff, #eaf5ff);
  box-shadow: none;
}

.ai-insight__label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 999px;
  background: #4f9ded;
  color: #fff;
  font-size: 9px;
  font-weight: 900;
}

.ai-insight p {
  margin: 10px 0 0;
  color: #3e5367;
  font-size: 11px;
  line-height: 1.65;
}

.ai-insight__icon {
  position: absolute;
  right: 18px;
  bottom: 12px;
  color: #72afe8;
  font-size: 42px;
  opacity: 0.65;
}

.result-actions {
  margin-top: 17px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 9px;
}

.result-actions button,
.result-actions a {
  font-size: 11px;
}

@media (max-width: 360px) {
  .result-actions {
    grid-template-columns: 1fr;
  }
}
</style>
