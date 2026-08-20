<template>
  <div class="kb-mobile-page check-page">
    <PageHeader
        title="소비 분석 준비"
        :custom-back="true"
        @back="goBack"
    />

    <div class="check-content-start">
      <div v-if="loading" class="kb-card kb-loading check-loading text-13">
        <div class="spinner-border kb-spinner" role="status"></div>
        <div>분석 가능한 상태인지 확인하고 있어요.</div>
      </div>

      <section v-else-if="availability" class="check-card kb-card">
        <div class="check-period text-13-bold">{{ availability.periodLabel }}</div>

        <div class="state-visual" :class="stateVisualClass">
          <i :class="stateIcon"></i>
        </div>

        <template v-if="!availability.available">
          <h2 class="text-20-bold">분석을 시작하려면<br />거래 분류가 조금 더 필요해요</h2>
          <p class="text-13">
            선택한 기간의 결제 거래를
            <strong>{{ availability.remainingCount }}건</strong> 더 분류하면<br />
            소비 분석을 시작할 수 있어요.
          </p>

          <div class="status-box progress-status-box">
            <div class="status-box-head">
              <div>
                <span class="status-label text-13">현재 분류</span>
                <strong class="text-15-bold">{{ availability.classifiedPaymentCount }}건</strong>
              </div>
              <div class="status-required">
                <span class="status-label text-13">분석 기준</span>
                <strong class="text-15-bold">{{ availability.requiredCount }}건</strong>
              </div>
            </div>
            <div class="analysis-progress">
              <span :style="{ width: `${progressPercent}%` }"></span>
            </div>
            <p class="progress-guide text-13">
              {{ availability.remainingCount }}건만 더 분류하면 분석할 수 있어요.
            </p>
          </div>
        </template>

        <template v-else-if="availability.unclassifiedPaymentCount > 0">
          <h2 class="text-20-bold">지금 바로 분석할 수 있어요</h2>
          <p class="text-13">
            다만 미분류 거래가 <strong>{{ availability.unclassifiedPaymentCount }}건</strong> 있어요.<br />
            먼저 분류하거나, 현재 분류된 내역만으로 분석해도 돼요.
          </p>

          <div class="status-box summary-status-box">
            <div class="status-item">
              <span class="status-label text-13">분류 완료</span>
              <strong class="text-15-bold">{{ availability.classifiedPaymentCount }}건</strong>
            </div>
            <div class="status-divider" aria-hidden="true"></div>
            <div class="status-item">
              <span class="status-label text-13">미분류 거래</span>
              <strong class="text-15-bold status-warning">{{ availability.unclassifiedPaymentCount }}건</strong>
            </div>
          </div>
        </template>

        <template v-else>
          <h2 class="text-20-bold">소비 분석 준비가 완료됐어요</h2>
          <p class="text-13">
            선택한 기간의 소비내역으로<br />새로운 소비 패턴을 확인해 보세요.
          </p>

          <div class="status-box summary-status-box ready-status-box">
            <div class="status-item">
              <span class="status-label text-13">분류 완료</span>
              <strong class="text-15-bold">{{ availability.classifiedPaymentCount }}건</strong>
            </div>
            <div class="status-divider" aria-hidden="true"></div>
            <div class="status-item">
              <span class="status-label text-13">미분류 거래</span>
              <strong class="text-15-bold">0건</strong>
            </div>
          </div>
        </template>

        <div
            class="check-actions"
            :class="{ single: !availability.available || availability.unclassifiedPaymentCount === 0 }"
        >
          <button
              v-if="!availability.available || availability.unclassifiedPaymentCount > 0"
              type="button"
              class="content-btn secondary"
              @click="goToClassification"
          >
            미분류 거래 분류
          </button>

          <button
              v-if="availability.available"
              type="button"
              class="content-btn primary analysis-submit-button"
              :disabled="analysisLoading"
              @click="executeAnalysis"
          >
            <span>{{ analysisLoading ? '분석 중' : '현재 내역으로 분석' }}</span>
            <span
                v-if="analysisLoading"
                class="button-spinner"
                aria-hidden="true"
            ></span>
          </button>
        </div>
      </section>
    </div>
  </div>
</template><script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import analysisApi from '@/api/analysisApi';
import PageHeader from '@/components/common/PageHeader.vue';
import {
  getAnalysisErrorMessage,
  normalizeAnalysisPeriod,
} from '@/util/analysis';

const STATUS_POLL_INTERVAL = 2000;
const route = useRoute();
const router = useRouter();
const period = normalizeAnalysisPeriod(route.query.period);
const availability = ref(null);
const loading = ref(false);
const analysisLoading = ref(false);
const waitingForCurrentTask = ref(false);
const message = ref('');
const messageType = ref('error');
let statusTimer = null;

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

const stopStatusPolling = () => {
  if (statusTimer) {
    window.clearInterval(statusTimer);
    statusTimer = null;
  }
};

const moveToCompletedResult = async (status) => {
  const spendingAnalysisId = Number(status?.spendingAnalysisId);
  if (!Number.isInteger(spendingAnalysisId) || spendingAnalysisId <= 0) {
    messageType.value = 'error';
    message.value = '완료된 소비 분석 결과를 확인할 수 없습니다.';
    return;
  }

  waitingForCurrentTask.value = false;
  await router.replace({
    name: 'analysis-result',
    params: { spendingAnalysisId },
  });
};

const applyTaskStatus = async (status) => {
  const currentStatus = status?.status ?? 'IDLE';
  analysisLoading.value = currentStatus === 'PROCESSING';

  if (currentStatus === 'PROCESSING') {
    messageType.value = 'info';
    message.value = '소비 분석이 진행 중입니다. 다른 화면을 이용해도 분석은 계속됩니다.';
    return;
  }

  if (currentStatus === 'COMPLETED' && waitingForCurrentTask.value) {
    stopStatusPolling();
    await moveToCompletedResult(status);
    return;
  }

  if (currentStatus === 'FAILED') {
    stopStatusPolling();
    waitingForCurrentTask.value = false;
    messageType.value = 'error';
    message.value = status?.message || '소비 분석 실행에 실패했습니다.';
  }
};

const checkAnalysisStatus = async () => {
  try {
    const status = await analysisApi.getAnalysisStatus(period);
    await applyTaskStatus(status);
  } catch (error) {
    stopStatusPolling();
    analysisLoading.value = false;
    messageType.value = 'error';
    message.value = getAnalysisErrorMessage(
        error,
        '소비 분석 진행 상태를 확인하지 못했습니다.',
    );
  }
};

const startStatusPolling = () => {
  stopStatusPolling();
  statusTimer = window.setInterval(checkAnalysisStatus, STATUS_POLL_INTERVAL);
};

const loadAvailability = async () => {
  loading.value = true;
  message.value = '';
  try {
    availability.value = await analysisApi.getAvailability(period);
    const status = await analysisApi.getAnalysisStatus(period);

    if (status?.status === 'PROCESSING') {
      waitingForCurrentTask.value = true;
      await applyTaskStatus(status);
      startStatusPolling();
    } else if (status?.status === 'FAILED') {
      await applyTaskStatus(status);
    }
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
  router.push({
    name: 'analysis-classification',
    query: { period, returnTo: 'analysis-check' },
  });
};

const executeAnalysis = async () => {
  if (!availability.value?.available || analysisLoading.value) return;

  analysisLoading.value = true;
  waitingForCurrentTask.value = true;
  messageType.value = 'info';
  message.value = '소비 분석을 시작했습니다. 다른 화면을 이용해도 분석은 계속됩니다.';

  try {
    const status = await analysisApi.startAsyncAnalysis(period);
    await applyTaskStatus(status);
    startStatusPolling();
  } catch (error) {
    analysisLoading.value = false;
    waitingForCurrentTask.value = false;
    messageType.value = 'error';
    message.value = getAnalysisErrorMessage(
        error,
        '소비 분석 실행에 실패했습니다.',
    );
  }
};

const goBack = () =>
    router.push({ name: 'analysis-main', query: { period } });

onMounted(loadAvailability);
onBeforeUnmount(stopStatusPolling);
</script>

<style scoped>
.check-page {
  min-height: 100vh;
  padding-bottom: 34px;
  background: var(--color-bg-screen);
  color: var(--color-text-main);
}

/* 이 화면에서만 공용 헤더 위치와 배경을 맞춘다. */
.check-page :deep(.page-header) {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  padding: 0 24px;
  background: #ffffff;
}

.check-content-start {
  padding: 0 24px;
}

.check-loading {
  margin-top: 16px;
}

.check-card {
  margin-top: 16px;
  padding: 32px 24px 24px;
  text-align: center;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.check-period {
  display: inline-flex;
  padding: 5px 11px;
  border-radius: 999px;
  background: var(--color-bg-disabled);
  color: var(--color-text-sub);
}

.state-visual {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 76px;
  height: 76px;
  margin: 20px auto;
  border-radius: 24px;
  font-size: 31px;
}

.state-visual.needs-classification {
  background: #fff3cf;
  color: #d99b00;
}

.state-visual.has-unclassified {
  background: #fff0dd;
  color: #e58a36;
}

.state-visual.ready {
  background: #eaf8f1;
  color: #1e9b61;
}

.check-card h2 {
  margin: 0;
  line-height: 1.45;
  letter-spacing: -0.6px;
  word-break: keep-all;
}

.check-card > p {
  margin: 10px 0 0;
  color: var(--color-text-sub);
  line-height: 1.65;
  word-break: keep-all;
}

.check-card > p strong {
  color: #c98900;
}

.status-box {
  margin-top: 24px;
  padding: 18px;
  border-radius: 16px;
  background: #f7f8fa;
}

.status-box-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  text-align: left;
}

.status-box-head > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-required {
  text-align: right;
}

.status-label {
  color: var(--color-text-muted);
}

.analysis-progress {
  height: 7px;
  margin-top: 14px;
  overflow: hidden;
  border-radius: 999px;
  background: var(--color-divider);
}

.analysis-progress span {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: var(--color-primary);
}

.progress-guide {
  margin: 10px 0 0;
  color: var(--color-text-sub);
  text-align: left;
}

.summary-status-box {
  display: grid;
  grid-template-columns: 1fr 1px 1fr;
  align-items: center;
  gap: 18px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.status-divider {
  width: 1px;
  height: 34px;
  background: var(--color-divider);
}

.status-warning {
  color: #c98900;
}

.check-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 20px;
}

.check-actions.single {
  grid-template-columns: 1fr;
}

.check-actions .content-btn {
  min-width: 0;
  font-size: 15px;
  font-weight: 600;
}

.analysis-submit-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.analysis-submit-button:disabled {
  cursor: not-allowed;
  opacity: 0.75;
}

.button-spinner {
  width: 14px;
  height: 14px;
  flex: 0 0 14px;
  border: 2px solid rgba(34, 34, 34, 0.25);
  border-top-color: var(--color-text-main);
  border-radius: 50%;
  animation: button-spin 0.75s linear infinite;
}

@keyframes button-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 380px) {
  .check-content-start {
    padding: 0 20px;
  }

  .check-card {
    padding: 28px 20px 20px;
  }

  .check-actions {
    grid-template-columns: 1fr;
  }
}
</style>