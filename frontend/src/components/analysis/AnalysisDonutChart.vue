<template>
  <div
      class="analysis-donut"
      :class="{ 'analysis-donut--detail': variant === 'detail' }"
      @mouseleave="hoveredCategory = null"
  >
    <svg
        class="analysis-donut__svg"
        viewBox="0 0 220 220"
        role="img"
        :aria-label="`${formatAnalysisNumber(totalAmount)}원의 카테고리별 소비 비율`"
    >
      <circle
          class="analysis-donut__track"
          cx="110"
          cy="110"
          r="82"
          pathLength="100"
      />

      <circle
          v-for="segment in segments"
          :key="segment.key"
          class="analysis-donut__segment"
          :class="{ 'is-active': activeCategory?.spendingCategoryId === segment.category.spendingCategoryId }"
          cx="110"
          cy="110"
          r="82"
          pathLength="100"
          :stroke="segment.color"
          :stroke-dasharray="`${segment.length} ${100 - segment.length}`"
          :stroke-dashoffset="-segment.offset"
          tabindex="0"
          @mouseenter="hoveredCategory = segment.category"
          @focus="hoveredCategory = segment.category"
          @blur="hoveredCategory = null"
          @click="toggleSelected(segment.category)"
          @keydown.enter.prevent="toggleSelected(segment.category)"
          @keydown.space.prevent="toggleSelected(segment.category)"
      />
    </svg>

    <button
        class="analysis-donut__center"
        type="button"
        :aria-label="selectedCategory ? '전체 소비 금액 보기' : '총 소비 금액'"
        @click="selectedCategory = null"
    >
      <template v-if="activeCategory">
        <span>{{ activeCategory.categoryName }}</span>
        <strong>{{ formatAnalysisNumber(activeCategory.spendingAmount) }}원</strong>
        <small>{{ formatRatio(activeCategory.spendingRatio) }}%</small>
      </template>
      <template v-else>
        <span>총 소비</span>
        <strong>{{ formatAnalysisNumber(totalAmount) }}원</strong>
        <small>100%</small>
      </template>
    </button>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import {
  formatAnalysisNumber,
  getAnalysisCategoryColor,
} from '@/util/analysis';

const props = defineProps({
  categories: {
    type: Array,
    default: () => [],
  },
  totalAmount: {
    type: Number,
    default: 0,
  },
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'detail'].includes(value),
  },
});

const selectedCategory = ref(null);
const hoveredCategory = ref(null);
const activeCategory = computed(
    () => hoveredCategory.value ?? selectedCategory.value,
);

const normalizedCategories = computed(() =>
    [...props.categories]
        .filter((category) => Number(category?.spendingRatio) > 0)
        .sort(
            (left, right) =>
                Number(right.spendingAmount ?? 0) - Number(left.spendingAmount ?? 0),
        ),
);

const segments = computed(() => {
  let offset = 0;

  return normalizedCategories.value.map((category, index) => {
    const ratio = Math.max(0, Number(category.spendingRatio ?? 0));
    const segment = {
      key: `${category.spendingCategoryId}-${category.categoryName}`,
      category,
      color: getAnalysisCategoryColor(category.categoryName, index),
      length: ratio,
      offset,
    };
    offset += ratio;
    return segment;
  });
});

const toggleSelected = (category) => {
  selectedCategory.value =
      selectedCategory.value?.spendingCategoryId === category.spendingCategoryId
          ? null
          : category;
};

const formatRatio = (value) => {
  const ratio = Number(value ?? 0);
  return Number.isInteger(ratio) ? ratio : ratio.toFixed(1);
};

watch(
    () => props.categories,
    () => {
      selectedCategory.value = null;
      hoveredCategory.value = null;
    },
);
</script>

<style scoped>
.analysis-donut {
  position: relative;
  width: min(100%, 190px);
  aspect-ratio: 1;
  margin: 0 auto;
}

.analysis-donut__svg {
  width: 100%;
  height: 100%;
  overflow: visible;
  transform: rotate(-90deg);
}

.analysis-donut__track,
.analysis-donut__segment {
  fill: none;
  stroke-width: 28;
}

.analysis-donut__track {
  stroke: #f0f1f3;
}

.analysis-donut__segment {
  cursor: pointer;
  transform-origin: 110px 110px;
  transition:
      stroke-width 0.18s ease,
      opacity 0.18s ease,
      filter 0.18s ease;
}

.analysis-donut__segment:hover,
.analysis-donut__segment:focus,
.analysis-donut__segment.is-active {
  stroke-width: 34;
  opacity: 1;
  filter: drop-shadow(0 3px 4px rgba(0, 0, 0, 0.12));
  outline: none;
}

.analysis-donut__center {
  position: absolute;
  inset: 25%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5px;
  border: 0;
  border-radius: 50%;
  background: #fff;
  color: #222;
  text-align: center;
  box-shadow: 0 2px 12px rgba(25, 25, 25, 0.06);
}

.analysis-donut__center span {
  max-width: 100%;
  overflow: hidden;
  color: #7d7d7d;
  font-size: 8px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.analysis-donut__center strong {
  margin-top: 2px;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: -0.6px;
  white-space: nowrap;
}

.analysis-donut__center small {
  margin-top: 2px;
  color: #9a9a9a;
  font-size: 10px;
  font-weight: 700;
}

/* 상세 화면은 기존 크기를 유지 */
.analysis-donut--detail {
  width: min(100%, 250px);
}

/* 상세 화면에서만 도넛 중앙 정보를 한 단계 크게 표시 */
.analysis-donut__center span {
  /* 총 소비 */
  font-size: 10px;
  font-weight: 700;
}

.analysis-donut__center strong {
  /* 283,900원 */
  margin-top: 3px;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -0.5px;
  white-space: nowrap;
}

.analysis-donut__center small {
  /* 100% */
  margin-top: 3px;
  font-size: 11px;
  font-weight: 600;
}
</style>
