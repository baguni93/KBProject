<template>
  <div class="kb-mobile-page guide-page">
    <PageHeader
        title="12개월 분석 안내"
        :custom-back="true"
        @back="goBack"
    />

    <div class="guide-content-start">
      <section class="guide-card kb-card">
      <div class="guide-icon">
        <i :class="isInsurance ? 'fa-solid fa-shield-heart' : 'fa-regular fa-credit-card'"></i>
      </div>
      <span class="text-13-bold">{{ currentPeriod }}개월 분석 선택 중</span>
      <h2 class="text-20-bold">{{ featureName }}을 이용하려면<br />12개월 소비분석이 필요해요</h2>
      <p class="text-13">
        계절별 소비와 전월 실적까지 정확하게 비교하려면<br />최근 12개월 소비내역을 기준으로 분석해야 합니다.
      </p>
      <button type="button" class="content-btn primary" @click="goToTwelveMonthAnalysis">
        12개월 소비분석 보기
      </button>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { normalizeAnalysisPeriod } from '@/util/analysis';
import PageHeader from '@/components/common/PageHeader.vue';

const route = useRoute();
const router = useRouter();
const currentPeriod = normalizeAnalysisPeriod(route.query.period);
const isInsurance = computed(() => route.query.type === 'insurance');
const featureName = computed(() =>
  isInsurance.value ? '보험 추천' : '카드 추천',
);

const goBack = () => router.back();
const goToTwelveMonthAnalysis = () =>
  router.push({ name: 'analysis-main', query: { period: 12 } });
</script>

<style scoped>
.guide-page {
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

.guide-content-start {
  /*
   * 팀 협의 후 PageHeader와 첫 콘텐츠 사이 간격을 적용할 경우
   * 아래 주석을 해제합니다.
   * margin-top: 14px;
   */
}

.guide-card {
  margin-top: 18px;
  padding: 38px 22px 24px;
  text-align: center;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.guide-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 82px;
  height: 82px;
  margin: 0 auto 16px;
  border-radius: 28px;
  background: #fff3cf;
  color: #d79b00;
  font-size: 32px;
}

.guide-card > span {
  display: inline-flex;
  padding: 5px 10px;
  border-radius: 999px;
  background: var(--color-bg-disabled);
  color: var(--color-text-sub);
}

.guide-card h2 {
  margin: 13px 0 0;
  line-height: 1.45;
  letter-spacing: -0.55px;
}

.guide-card p {
  margin: 11px 0 22px;
  color: var(--color-text-sub);
  line-height: 1.7;
}
</style>
