<template>
  <div class="kb-mobile-page guide-page">
    <header class="kb-app-header">
      <button class="kb-icon-button" type="button" aria-label="뒤로가기" @click="goBack">
        <i class="fa-solid fa-chevron-left"></i>
      </button>
      <h1 class="kb-app-header__title">12개월 분석 안내</h1>
      <span></span>
    </header>

    <section class="guide-card kb-card">
      <div class="guide-icon">
        <i :class="isInsurance ? 'fa-solid fa-shield-heart' : 'fa-regular fa-credit-card'"></i>
      </div>
      <span>{{ currentPeriod }}개월 분석 선택 중</span>
      <h2>{{ featureName }}을 이용하려면<br />12개월 소비분석이 필요해요</h2>
      <p>
        계절별 소비와 전월 실적까지 정확하게 비교하려면<br />최근 12개월 소비내역을 기준으로 분석해야 합니다.
      </p>
      <button type="button" class="kb-primary-button" @click="goToTwelveMonthAnalysis">
        12개월 소비분석 보기
      </button>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { normalizeAnalysisPeriod } from '@/util/analysis';

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
.guide-page{margin-top:-16px;padding-bottom:34px}.guide-card{margin-top:18px;padding:38px 22px 24px;text-align:center;border:1px solid #ededed;box-shadow:none}.guide-icon{width:82px;height:82px;margin:0 auto 16px;display:flex;align-items:center;justify-content:center;border-radius:28px;background:#fff3cf;color:#d79b00;font-size:32px}.guide-card>span{display:inline-flex;padding:5px 10px;border-radius:999px;background:#f4f4f4;color:#777;font-size:9px;font-weight:800}.guide-card h2{margin:13px 0 0;font-size:19px;font-weight:900;line-height:1.45;letter-spacing:-.55px}.guide-card p{margin:11px 0 22px;color:#777;font-size:10px;line-height:1.7}.guide-card button{width:100%;font-size:11px}
</style>
