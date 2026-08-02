<template>
  <div class="container py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
      <div>
        <div class="small text-muted mb-1">화면 ID: analysis-001</div>
        <h2 class="mb-1">소비 분석</h2>
        <p class="text-muted mb-0">
          거래 분류 상태를 확인하고 선택한 기간의 소비 패턴을 분석합니다.
        </p>
      </div>
      <button
        type="button"
        class="btn btn-outline-secondary"
        :disabled="pageLoading || analysisLoading"
        @click="loadPage"
      >
        새로고침
      </button>
    </div>

    <div
      v-if="message"
      :class="[
        'alert',
        messageType === 'success'
          ? 'alert-success'
          : messageType === 'info'
            ? 'alert-info'
            : 'alert-danger',
      ]"
    >
      {{ message }}
    </div>

    <section class="card mb-4">
      <div class="card-header fw-bold">분석 기간 선택</div>
      <div class="card-body">
        <div class="btn-group mb-3" role="group" aria-label="소비 분석 기간">
          <template v-for="periodOption in ANALYSIS_PERIODS" :key="periodOption.value">
            <input
              :id="`analysis-period-${periodOption.value}`"
              v-model.number="selectedPeriod"
              class="btn-check"
              type="radio"
              name="analysis-period"
              :value="periodOption.value"
              :disabled="availabilityLoading || analysisLoading"
              @change="loadAvailability"
            />
            <label
              class="btn btn-outline-warning"
              :for="`analysis-period-${periodOption.value}`"
            >
              {{ periodOption.label }}
            </label>
          </template>
        </div>

        <div v-if="availabilityLoading" class="text-muted">
          분석 가능 여부를 조회하고 있습니다.
        </div>

        <template v-else-if="availability">
          <div class="row g-3 mb-3">
            <div class="col-md-3">
              <div class="border rounded p-3 h-100">
                <div class="small text-muted">조회 기간</div>
                <div class="fw-semibold">{{ availability.periodLabel }}</div>
                <div class="small text-muted">
                  {{ availability.analysisStartDate }} ~
                  {{ availability.analysisEndDate }}
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="border rounded p-3 h-100">
                <div class="small text-muted">전체 결제</div>
                <div class="h4 mb-0">
                  {{ formatAnalysisNumber(availability.totalPaymentCount) }}건
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="border rounded p-3 h-100">
                <div class="small text-muted">분류 완료</div>
                <div class="h4 mb-0">
                  {{ formatAnalysisNumber(availability.classifiedPaymentCount) }}건
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="border rounded p-3 h-100">
                <div class="small text-muted">미분류</div>
                <div class="h4 mb-0">
                  {{ formatAnalysisNumber(availability.unclassifiedPaymentCount) }}건
                </div>
              </div>
            </div>
          </div>

          <label class="form-label fw-semibold" for="analysis-progress">
            분석 조건 달성 현황
          </label>
          <progress
            id="analysis-progress"
            class="w-100 mb-2"
            :value="classifiedProgressValue"
            :max="availability.requiredCount || 10"
          ></progress>
          <div class="small text-muted mb-3">
            {{ availability.classifiedPaymentCount }} /
            {{ availability.requiredCount }}건 분류 완료
          </div>

          <div v-if="!availability.available" class="alert alert-warning mb-0">
            <h5>소비 분석을 위해 거래 분류가 필요합니다.</h5>
            <p class="mb-3">
              {{ availability.message }} 미분류 거래를 직접 분류하면 분석 조건에
              반영됩니다.
            </p>
            <button type="button" class="btn btn-warning" @click="goToClassification">
              거래 분류하러 가기
            </button>
          </div>

          <div v-else-if="availability.unclassifiedPaymentCount > 0" class="alert alert-info mb-0">
            <h5>분석할 수 있지만 아직 미분류 거래가 있습니다.</h5>
            <p class="mb-3">
              미분류 거래 {{ availability.unclassifiedPaymentCount }}건을 추가로
              분류하거나, 현재 분류된 거래만으로 바로 분석할 수 있습니다.
            </p>
            <div class="d-flex flex-wrap gap-2">
              <button
                type="button"
                class="btn btn-outline-dark"
                @click="goToClassification"
              >
                미분류 거래 분류하기
              </button>
              <button
                type="button"
                class="btn btn-warning"
                :disabled="analysisLoading"
                @click="executeAnalysis"
              >
                {{ analysisLoading ? '분석 중...' : '현재 내역으로 분석하기' }}
              </button>
            </div>
          </div>

          <div v-else class="alert alert-success mb-0">
            <h5>소비 분석 준비가 완료되었습니다.</h5>
            <p class="mb-3">{{ availability.message }}</p>
            <button
              type="button"
              class="btn btn-warning"
              :disabled="analysisLoading"
              @click="executeAnalysis"
            >
              {{ analysisLoading ? '분석 중...' : '소비 분석 시작하기' }}
            </button>
          </div>
        </template>
      </div>
    </section>

    <section class="card">
      <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
        <span class="fw-bold">최근 소비 분석 결과</span>
        <span v-if="latestAnalysis" class="small text-muted">
          {{ formatAnalysisDateTime(latestAnalysis.createdAt) }}
        </span>
      </div>

      <div v-if="latestLoading" class="card-body text-muted">
        최근 분석 결과를 조회하고 있습니다.
      </div>

      <div v-else-if="latestAnalysis" class="card-body">
        <div class="mb-3">
          <div class="small text-muted">AI 소비 칭호</div>
          <h3>{{ latestAnalysis.aiTitle }}</h3>
          <p class="mb-0">{{ latestAnalysis.aiAnalysisSummary }}</p>
        </div>

        <div class="row g-3 mb-4">
          <div class="col-md-4">
            <div class="border rounded p-3 h-100">
              <div class="small text-muted">분석 기간</div>
              <div class="fw-semibold">{{ latestAnalysis.periodLabel }}</div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="border rounded p-3 h-100">
              <div class="small text-muted">총 소비 금액</div>
              <div class="fw-semibold">
                {{ formatAnalysisNumber(latestAnalysis.totalSpendingAmount) }}원
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="border rounded p-3 h-100">
              <div class="small text-muted">대표 카테고리</div>
              <div class="fw-semibold">
                {{ latestAnalysis.representativeCategoryName }}
              </div>
            </div>
          </div>
        </div>

        <div class="table-responsive mb-4">
          <table class="table align-middle mb-0">
            <thead>
              <tr>
                <th>카테고리</th>
                <th class="text-end">소비 비율</th>
                <th class="text-end">거래 건수</th>
                <th class="text-end">소비 금액</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="category in latestAnalysis.categories"
                :key="category.spendingCategoryId"
              >
                <td>{{ category.categoryName }}</td>
                <td class="text-end">{{ category.spendingRatio }}%</td>
                <td class="text-end">{{ category.transactionCount }}건</td>
                <td class="text-end">
                  {{ formatAnalysisNumber(category.spendingAmount) }}원
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="d-flex flex-wrap gap-2">
          <router-link
            class="btn btn-outline-dark"
            :to="{
              name: 'analysis-result',
              params: { spendingAnalysisId: latestAnalysis.spendingAnalysisId },
            }"
          >
            분석 상세 보기
          </router-link>
          <button
            type="button"
            class="btn btn-outline-warning"
            @click="showPlannedFeature('카드 추천')"
          >
            카드 추천
          </button>
          <button
            type="button"
            class="btn btn-outline-warning"
            @click="showPlannedFeature('보험 추천')"
          >
            보험 추천
          </button>
        </div>
      </div>

      <div v-else class="card-body text-muted">
        저장된 소비 분석 결과가 없습니다. 위에서 분석 기간을 선택하고 소비 분석을
        실행해 주세요.
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import analysisApi from '@/api/analysisApi';
import {
  ANALYSIS_PERIODS,
  formatAnalysisDateTime,
  formatAnalysisNumber,
  getAnalysisErrorMessage,
  isAnalysisResultNotFound,
} from '@/util/analysis';

const router = useRouter();

const selectedPeriod = ref(1);
const availability = ref(null);
const latestAnalysis = ref(null);
const availabilityLoading = ref(false);
const latestLoading = ref(false);
const analysisLoading = ref(false);
const pageLoading = computed(
  () => availabilityLoading.value || latestLoading.value,
);
const message = ref('');
const messageType = ref('success');

const classifiedProgressValue = computed(() => {
  if (!availability.value) return 0;
  return Math.min(
    availability.value.classifiedPaymentCount ?? 0,
    availability.value.requiredCount ?? 10,
  );
});

const setMessage = (type, text) => {
  messageType.value = type;
  message.value = text;
};

const loadAvailability = async () => {
  availabilityLoading.value = true;

  try {
    availability.value = await analysisApi.getAvailability(
      selectedPeriod.value,
    );
  } catch (error) {
    availability.value = null;
    setMessage(
      'error',
      getAnalysisErrorMessage(
        error,
        '소비 분석 가능 여부를 불러오지 못했습니다.',
      ),
    );
  } finally {
    availabilityLoading.value = false;
  }
};

const loadLatestAnalysis = async () => {
  latestLoading.value = true;

  try {
    latestAnalysis.value = await analysisApi.getLatestAnalysisDetail();
  } catch (error) {
    if (isAnalysisResultNotFound(error)) {
      latestAnalysis.value = null;
    } else {
      setMessage(
        'error',
        getAnalysisErrorMessage(
          error,
          '최근 소비 분석 결과를 불러오지 못했습니다.',
        ),
      );
    }
  } finally {
    latestLoading.value = false;
  }
};

const loadPage = async () => {
  message.value = '';
  await Promise.all([loadAvailability(), loadLatestAnalysis()]);
};

const goToClassification = () => {
  router.push({
    name: 'analysis-classification',
    query: { period: selectedPeriod.value },
  });
};

const executeAnalysis = async () => {
  if (!availability.value?.available) {
    setMessage('error', '분류 완료된 결제 거래가 10건 이상 필요합니다.');
    return;
  }

  analysisLoading.value = true;
  setMessage(
    'info',
    'AI가 소비 패턴을 분석하고 있습니다. 완료까지 수십 초가 걸릴 수 있습니다.',
  );

  try {
    const result = await analysisApi.executeAnalysis(selectedPeriod.value);
    const spendingAnalysisId = result?.spendingAnalysisId;

    if (!spendingAnalysisId) {
      throw new Error('분석 결과 ID를 응답에서 확인할 수 없습니다.');
    }

    await router.push({
      name: 'analysis-result',
      params: { spendingAnalysisId },
    });
  } catch (error) {
    const timeoutMessage =
      error?.code === 'ECONNABORTED'
        ? '분석 요청 시간이 초과되었습니다. 서버 로그를 확인한 뒤 최근 분석 결과를 새로고침해 주세요.'
        : '소비 분석 실행에 실패했습니다.';

    setMessage(
      'error',
      getAnalysisErrorMessage(error, timeoutMessage),
    );
  } finally {
    analysisLoading.value = false;
  }
};

const showPlannedFeature = (featureName) => {
  setMessage('info', `${featureName} 기능은 이후 단계에서 연결할 예정입니다.`);
};

onMounted(loadPage);
</script>
