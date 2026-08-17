<template>
  <section class="analysis-summary-card kb-card">
    <div class="summary-title-row">
      <span class="text-13-bold">소비 요약</span>
      <span class="summary-count text-13-bold">
        총 {{ formatAnalysisNumber(transactionCount) }}건 분석
      </span>
    </div>

    <div class="summary-chart">
      <AnalysisDonutChart
          :categories="categories"
          :total-amount="totalAmount"
          :variant="chartVariant"
      />
    </div>

    <div class="summary-info">
      <div class="summary-info-item">
        <span class="text-13-bold">대표 소비 카테고리</span>
        <strong class="text-13-bold">
          {{ representativeCategoryName }}
        </strong>
      </div>

      <div class="summary-info-item">
        <span class="text-13-bold">분석 기간 ({{ period }}개월)</span>
        <strong class="period-range text-13-bold">
          {{ formatAnalysisExecutionDate(startDate, false) }}
          ~
          {{ formatAnalysisExecutionDate(endDate, false) }}
        </strong>
      </div>
    </div>

    <div v-if="$slots.actions" class="summary-actions">
      <slot name="actions" />
    </div>
  </section>
</template>

<script setup>
import {computed} from 'vue';
import AnalysisDonutChart from '@/components/analysis/AnalysisDonutChart.vue';
import {
  formatAnalysisExecutionDate,
  formatAnalysisNumber,
} from '@/util/analysis';

const props = defineProps({
  categories: {
    type: Array,
    default: () => [],
  },
  totalAmount: {
    type: [Number, String],
    default: 0,
  },
  transactionCount: {
    type: [Number, String],
    default: 0,
  },
  representativeCategoryId: {
    type: [Number, String],
    default: null,
  },
  representativeCategoryName: {
    type: String,
    default: '',
  },
  period: {
    type: [Number, String],
    required: true,
  },
  startDate: {
    type: String,
    default: '',
  },
  endDate: {
    type: String,
    default: '',
  },
  chartVariant: {
    type: String,
    default: 'detail',
  },
});

const representativeCategory = computed(() => {
  const id = Number(props.representativeCategoryId);

  return props.categories.find((category) => {
    if (Number.isFinite(id) && id > 0) {
      return Number(category.spendingCategoryId) === id;
    }
    return category.categoryName === props.representativeCategoryName;
  });
});

const formatRatio = (value) => {
  const ratio = Number(value ?? 0);
  return Number.isInteger(ratio) ? ratio : ratio.toFixed(1);
};
</script>

<style scoped>
.analysis-summary-card {
  margin-top: 16px;
  padding: 18px 16px;
  display: grid;
  grid-template-columns: 136px minmax(0, 1fr);
  align-items: center;
  gap: 16px 18px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.summary-title-row {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--color-text-muted);
}

.summary-count {
  white-space: nowrap;
}

.summary-chart {
  width: 136px;
  min-width: 0;
}

.summary-info {
  min-width: 0;
  padding-left: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  border-left: 1px solid var(--color-divider);
}

.summary-info-item {
  min-width: 0;
}

.summary-info-item > span,
.summary-info-item > strong {
  display: block;
}

.summary-info-item > span {
  color: var(--color-text-muted);
}

.summary-info-item > strong {
  margin-top: 4px;
  line-height: 1.4;
}

.representative-value {
  margin-top: 6px;
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 6px;
}

.representative-value span {
  color: var(--color-text-sub);
}

.period-range {
  letter-spacing: -0.3px;
  word-break: keep-all;
}

.summary-actions {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.summary-actions :deep(.content-btn) {
  min-width: 0;
  font-size: 14px;
  font-weight: 600;
}

.summary-actions :deep(.primary i) {
  margin-left: 4px;
  font-size: 11px;
}

.category-row {
  margin-top: 4px;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.category-ratio {
  white-space: nowrap;
}

@media (max-width: 380px) {
  .analysis-summary-card {
    grid-template-columns: 120px minmax(0, 1fr);
    gap: 14px;
  }

  .summary-chart {
    width: 120px;
  }

  .summary-info {
    padding-left: 14px;
  }

  .summary-actions {
    grid-template-columns: 1fr;
  }
}
</style>
