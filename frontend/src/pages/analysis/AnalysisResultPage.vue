<template>
  <div class="container py-4">
    <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-4">
      <div>
        <div class="small text-muted mb-1">화면 ID: analysis-result</div>
        <h2 class="mb-1">소비 분석 상세 결과</h2>
        <p class="text-muted mb-0">
          저장된 소비 분석 결과를 분석 ID로 조회합니다.
        </p>
      </div>
      <div class="d-flex gap-2">
        <router-link class="btn btn-outline-secondary" to="/analysis">
          분석 메인
        </router-link>
        <button
          type="button"
          class="btn btn-outline-dark"
          :disabled="loading"
          @click="loadAnalysisDetail"
        >
          새로고침
        </button>
      </div>
    </div>

    <div v-if="message" class="alert alert-danger">{{ message }}</div>

    <div v-if="loading" class="card">
      <div class="card-body text-muted">소비 분석 상세 결과를 조회하고 있습니다.</div>
    </div>

    <template v-else-if="analysis">
      <section class="card mb-4">
        <div class="card-header d-flex flex-wrap justify-content-between align-items-center gap-2">
          <span class="fw-bold">AI 분석 결과</span>
          <span class="small text-muted">
            분석 ID: {{ analysis.spendingAnalysisId }} ·
            {{ formatAnalysisDateTime(analysis.createdAt) }}
          </span>
        </div>
        <div class="card-body">
          <div class="small text-muted">AI 소비 칭호</div>
          <h2>{{ analysis.aiTitle }}</h2>
          <p class="mb-0">{{ analysis.aiAnalysisSummary }}</p>
        </div>
      </section>

      <section class="card mb-4">
        <div class="card-header fw-bold">분석 기준</div>
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-3">
              <div class="border rounded p-3 h-100">
                <div class="small text-muted">분석 기간</div>
                <div class="fw-semibold">{{ analysis.periodLabel }}</div>
                <div class="small text-muted">
                  {{ analysis.analysisStartDate }} ~ {{ analysis.analysisEndDate }}
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="border rounded p-3 h-100">
                <div class="small text-muted">총 소비 금액</div>
                <div class="fw-semibold">
                  {{ formatAnalysisNumber(analysis.totalSpendingAmount) }}원
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="border rounded p-3 h-100">
                <div class="small text-muted">분석 거래</div>
                <div class="fw-semibold">
                  {{ formatAnalysisNumber(analysis.classifiedTransactionCount) }}건
                </div>
              </div>
            </div>
            <div class="col-md-3">
              <div class="border rounded p-3 h-100">
                <div class="small text-muted">대표 카테고리</div>
                <div class="fw-semibold">
                  {{ analysis.representativeCategoryName }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="card">
        <div class="card-header fw-bold">카테고리별 소비 결과</div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped align-middle mb-0">
              <thead>
                <tr>
                  <th>카테고리</th>
                  <th class="text-end">비율</th>
                  <th class="text-end">거래 건수</th>
                  <th class="text-end">소비 금액</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="category in analysis.categories"
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
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import analysisApi from '@/api/analysisApi';
import {
  formatAnalysisDateTime,
  formatAnalysisNumber,
  getAnalysisErrorMessage,
} from '@/util/analysis';

const route = useRoute();

const analysis = ref(null);
const loading = ref(false);
const message = ref('');

const loadAnalysisDetail = async () => {
  const spendingAnalysisId = Number(route.params.spendingAnalysisId);

  if (!Number.isInteger(spendingAnalysisId) || spendingAnalysisId <= 0) {
    analysis.value = null;
    message.value = '올바른 소비 분석 ID가 필요합니다.';
    return;
  }

  loading.value = true;
  message.value = '';

  try {
    analysis.value = await analysisApi.getAnalysisDetail(spendingAnalysisId);
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

watch(
  () => route.params.spendingAnalysisId,
  () => loadAnalysisDetail(),
);

onMounted(loadAnalysisDetail);
</script>
