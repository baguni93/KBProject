<template>
  <div class="kb-mobile-page check-page" :aria-busy="analysisLoading">
    <header class="kb-app-header">
      <button class="kb-icon-button" type="button" aria-label="뒤로가기" :disabled="analysisLoading" @click="goBack">
        <i class="fa-solid fa-chevron-left"></i>
      </button>
      <h1 class="kb-app-header__title">소비 분석 준비</h1>
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

    <Transition name="analysis-overlay">
      <div
        v-if="analysisLoading"
        class="analysis-loading-overlay"
        role="status"
        aria-live="assertive"
        aria-label="소비 분석 진행 중"
      >
        <div class="analysis-loading-dialog">
          <div class="analysis-spinner" aria-hidden="true">
            <span></span>
          </div>
          <h2>소비 패턴을 분석하고 있어요</h2>
          <p>
            AI가 선택한 기간의 소비내역을 정리하고 있습니다.<br />
            완료될 때까지 잠시만 기다려 주세요.
          </p>
          <small>분석이 끝나면 결과 화면으로 자동 이동합니다.</small>
        </div>
      </div>
    </Transition>

    <div v-if="loading" class="kb-card kb-loading check-loading">
      <div class="spinner-border kb-spinner" role="status"></div>
      <div>분석 가능한 상태인지 확인하고 있어요.</div>
    </div>

    <section v-else-if="availability" class="check-card kb-card">
      <div class="check-period">{{ availability.periodLabel }}</div>
      <div class="state-visual" :class="stateVisualClass">
        <i :class="stateIcon"></i>
      </div>

      <template v-if="!availability.available">
        <h2>소비 분석을 위해<br />카테고리 분류가 필요해요</h2>
        <p>
          선택한 기간의 결제 거래를
          <strong>{{ availability.remainingCount }}건</strong> 더 분류하면<br />
          소비 분석을 시작할 수 있어요.
        </p>
      </template>

      <template v-else-if="availability.unclassifiedPaymentCount > 0">
        <h2>분석은 가능하지만<br />아직 미분류 거래가 있어요</h2>
        <p>
          미분류 거래 {{ availability.unclassifiedPaymentCount }}건을 먼저 분류하거나<br />
          현재 분류된 내역만으로 분석할 수 있어요.
        </p>
      </template>

      <template v-else>
        <h2>소비 분석 준비가<br />완료됐어요</h2>
        <p>선택한 기간의 소비내역으로<br />새로운 소비 패턴을 확인해 보세요.</p>
      </template>

      <div class="classification-progress">
        <div class="progress-copy">
          <strong>{{ availability.classifiedPaymentCount }}건 분류 완료</strong>
          <span>{{ availability.requiredCount }}건 필요</span>
        </div>
        <div class="analysis-progress">
          <span :style="{ width: `${progressPercent}%` }"></span>
        </div>
        <small>{{ availability.classifiedPaymentCount }} / {{ availability.requiredCount }}</small>
      </div>

      <div
        class="check-actions"
        :class="{ single: !availability.available || availability.unclassifiedPaymentCount === 0 }"
      >
        <button
          v-if="!availability.available || availability.unclassifiedPaymentCount > 0"
          type="button"
          class="kb-outline-button"
          :disabled="analysisLoading"
          @click="goToClassification"
        >
          {{ availability.available ? '미분류 거래 분류하기' : '카테고리 분류하기' }}
        </button>

        <button
          v-if="availability.available"
          type="button"
          class="kb-primary-button"
          :disabled="analysisLoading"
          @click="executeAnalysis"
        >
          {{ analysisLoading ? '분석 중...' : '현재 내역으로 분석하기' }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router';
import analysisApi from '@/api/analysisApi';
import {
  getAnalysisErrorMessage,
  normalizeAnalysisPeriod,
} from '@/util/analysis';

const route = useRoute();
const router = useRouter();
const period = normalizeAnalysisPeriod(route.query.period);
const availability = ref(null);
const loading = ref(false);
const analysisLoading = ref(false);
const allowResultNavigation = ref(false);
const message = ref('');
const messageType = ref('error');

const progressPercent = computed(() => {
  if (!availability.value) return 0;
  const required = Number(availability.value.requiredCount || 10);
  return Math.min(
    (Number(availability.value.classifiedPaymentCount || 0) / required) * 100,
    100,
  );
});

const stateIcon = computed(() => {
  if (!availability.value?.available) return 'fa-solid fa-clipboard-list';
  if (availability.value.unclassifiedPaymentCount > 0) {
    return 'fa-solid fa-file-circle-question';
  }
  return 'fa-solid fa-chart-pie';
});

const stateVisualClass = computed(() => ({
  'needs-classification': !availability.value?.available,
  'has-unclassified':
    availability.value?.available &&
    availability.value?.unclassifiedPaymentCount > 0,
  ready:
    availability.value?.available &&
    availability.value?.unclassifiedPaymentCount === 0,
}));

const loadAvailability = async () => {
  loading.value = true;
  message.value = '';
  try {
    availability.value = await analysisApi.getAvailability(period);
  } catch (error) {
    availability.value = null;
    message.value = getAnalysisErrorMessage(
      error,
      '소비 분석 가능 여부를 불러오지 못했습니다.',
    );
  } finally {
    loading.value = false;
  }
};

const goToClassification = () => {
  if (analysisLoading.value) return;

  router.push({
    name: 'analysis-classification',
    query: { period, returnTo: 'analysis-check' },
  });
};

const executeAnalysis = async () => {
  if (!availability.value?.available || analysisLoading.value) return;

  analysisLoading.value = true;
  messageType.value = 'info';
  message.value = 'AI가 소비 패턴을 분석하고 있어요. 잠시만 기다려 주세요.';

  try {
    const result = await analysisApi.executeAnalysis(period);
    if (!result?.spendingAnalysisId) {
      throw new Error('분석 결과 ID를 확인할 수 없습니다.');
    }
    allowResultNavigation.value = true;
    await router.replace({
      name: 'analysis-result',
      params: { spendingAnalysisId: result.spendingAnalysisId },
    });
  } catch (error) {
    messageType.value = 'error';
    message.value = getAnalysisErrorMessage(
      error,
      '소비 분석 실행에 실패했습니다.',
    );
  } finally {
    analysisLoading.value = false;
  }
};

const goBack = () => {
  if (analysisLoading.value) return;
  router.push({ name: 'analysis-main', query: { period } });
};

const preventBrowserUnload = (event) => {
  if (!analysisLoading.value) return;
  event.preventDefault();
  event.returnValue = '';
};

onBeforeRouteLeave(() => {
  if (analysisLoading.value && !allowResultNavigation.value) {
    messageType.value = 'info';
    message.value = '소비 분석이 완료될 때까지 잠시만 기다려 주세요.';
    return false;
  }
  return true;
});

onMounted(() => {
  window.addEventListener('beforeunload', preventBrowserUnload);
  loadAvailability();
});

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', preventBrowserUnload);
});
</script>

<style scoped>
.check-page{padding-bottom:34px}.kb-icon-button:disabled,.check-actions button:disabled{cursor:not-allowed;opacity:.45}.analysis-loading-overlay{position:fixed;inset:0;z-index:2000;display:flex;align-items:center;justify-content:center;padding:24px;background:rgba(20,20,20,.48);backdrop-filter:blur(3px)}.analysis-loading-dialog{width:min(310px,100%);padding:30px 22px 24px;border-radius:24px;background:#fff;text-align:center;box-shadow:0 20px 55px rgba(0,0,0,.22)}.analysis-spinner{position:relative;width:72px;height:72px;margin:0 auto 18px;border-radius:50%;background:conic-gradient(var(--kb-yellow),#ffe9a8 45%,#f3f3f3 46% 100%);animation:analysis-spin .85s linear infinite}.analysis-spinner::after{content:'';position:absolute;inset:10px;border-radius:50%;background:#fff}.analysis-spinner span{position:absolute;top:2px;left:50%;z-index:1;width:9px;height:9px;border-radius:50%;background:#222;transform:translateX(-50%)}.analysis-loading-dialog h2{margin:0;color:#222;font-size:18px;font-weight:900;letter-spacing:-.5px}.analysis-loading-dialog p{margin:10px 0 0;color:#666;font-size:10px;line-height:1.7}.analysis-loading-dialog small{display:block;margin-top:12px;color:#a17700;font-size:9px;font-weight:800}.analysis-overlay-enter-active,.analysis-overlay-leave-active{transition:opacity .18s ease}.analysis-overlay-enter-from,.analysis-overlay-leave-to{opacity:0}@keyframes analysis-spin{to{transform:rotate(360deg)}}.check-loading{margin-top:16px}.check-card{margin-top:16px;padding:28px 20px 20px;text-align:center;border:1px solid #ededed;box-shadow:none}.check-period{display:inline-flex;padding:5px 10px;border-radius:999px;background:#f4f4f4;color:#777;font-size:9px;font-weight:800}.state-visual{width:92px;height:92px;margin:18px auto 15px;display:flex;align-items:center;justify-content:center;border-radius:30px;font-size:38px}.state-visual.needs-classification{background:#fff3cf;color:#d99b00}.state-visual.has-unclassified{background:#fff0dd;color:#e58a36}.state-visual.ready{background:#eaf8f1;color:#1e9b61}.check-card h2{margin:0;font-size:19px;font-weight:900;line-height:1.45;letter-spacing:-.6px}.check-card>p{margin:10px 0 0;color:#777;font-size:10px;line-height:1.65}.check-card>p strong{color:#d49300}.classification-progress{margin-top:22px;padding:14px;border-radius:14px;background:#fafafa}.progress-copy{display:flex;align-items:center;justify-content:space-between}.progress-copy strong{font-size:10px}.progress-copy span{color:#999;font-size:9px}.analysis-progress{height:7px;margin-top:9px;overflow:hidden;border-radius:999px;background:#ececec}.analysis-progress span{display:block;height:100%;border-radius:999px;background:var(--kb-yellow)}.classification-progress small{display:block;margin-top:5px;color:#999;font-size:8px}.check-actions{margin-top:18px;display:grid;grid-template-columns:1fr 1fr;gap:8px}.check-actions.single{grid-template-columns:1fr}.check-actions button{font-size:10px}@media(max-width:360px){.check-actions{grid-template-columns:1fr}}
</style>
