<template>
  <!--
    [공용 소비 카테고리 선택 컴포넌트]

    이 컴포넌트는 "카테고리를 화면에 보여주고 선택값을 돌려주는 역할"만 담당합니다.
    DB 저장, 송금 실행, 정산 생성, 소비분석 거래 수정 같은 비즈니스 로직은
    이 컴포넌트 안에서 처리하지 않습니다.

    따라서 사용하는 화면에서는 v-model로 받은 spendingCategoryId를
    각 기능의 요청 DTO/payload에 넣어 사용하면 됩니다.

    예) 송금/더치페이에서 선택이 필수가 아닌 경우
      const spendingCategoryId = ref(null);

      <SpendingCategorySelector
        v-model="spendingCategoryId"
        :categories="categories"
      />

      // 사용자가 아무것도 선택하지 않으면 spendingCategoryId는 null 그대로입니다.
      // 이 경우 financial_transaction_tbl.spending_category_id도 NULL(미분류)로 저장할 수 있습니다.

    예) 세부 카테고리만 표시하고 싶은 경우
      <SpendingCategorySelector
        v-model="spendingCategoryId"
        :categories="categories"
        :parent-category-id="13"
        :columns="2"
        compact
      />

    중요:
    - categories는 analysisApi.getCategories() 응답의 categories 배열과 같은 형태를 사용합니다.
    - 이 컴포넌트는 라우팅을 직접 하지 않습니다.
    - 상위 카테고리에 세부 카테고리가 있는지는 @select 이벤트의 두 번째 인자로 알려줍니다.
      기존 Analysis처럼 별도 세부 페이지로 이동할지,
      송금/더치페이 화면 안에서 직접 세부 선택을 보여줄지는 호출하는 화면이 결정합니다.
  -->
  <div
    class="spending-category-selector kb-card"
    :class="{
      'spending-category-selector--compact': compact,
      'spending-category-selector--responsive-four': Number(columns) === 4,
      'spending-category-selector--detail': Number(columns) === 2,
    }"
    :style="{ '--category-columns': normalizedColumns }"
  >
    <button
      v-for="category in visibleCategories"
      :key="category.spendingCategoryId"
      type="button"
      class="spending-category-selector__button"
      :class="{ selected: isSelected(category.spendingCategoryId) }"
      :disabled="disabled"
      :aria-pressed="isSelected(category.spendingCategoryId)"
      @click="selectCategory(category)"
    >
      <div class="spending-category-selector__icon">
        <i :class="getCategoryIcon(category.categoryName)"></i>
      </div>

      <span class="text-13-bold">
        {{ category.categoryName }}
      </span>

      <small
        v-if="showChildHint && hasChildren(category.spendingCategoryId)"
        class="text-13 spending-category-selector__hint"
      >
        {{ childHint }}
      </small>
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { getCategoryIcon } from '@/util/analysis';

/**
 * categories 항목의 기본 형태
 * {
 *   spendingCategoryId: 1,
 *   categoryName: '식비',
 *   parentCategoryId: null
 * }
 *
 * 하위 카테고리는 parentCategoryId에 상위 카테고리 ID를 가집니다.
 */
const props = defineProps({
  /**
   * 선택 가능한 전체 소비 카테고리 목록입니다.
   * Analysis의 GET /api/spending-analyses/categories 응답을 그대로 전달할 수 있습니다.
   */
  categories: {
    type: Array,
    default: () => [],
  },

  /**
   * v-model 값입니다.
   * 선택하지 않은 상태는 null을 권장합니다.
   */
  modelValue: {
    type: [Number, String],
    default: null,
  },

  /**
   * null이면 대분류(parentCategoryId == null)만 표시합니다.
   * 특정 ID를 넘기면 해당 ID를 부모로 가지는 세부 카테고리만 표시합니다.
   */
  parentCategoryId: {
    type: [Number, String],
    default: null,
  },

  /** 선택 버튼 전체 비활성화 여부 */
  disabled: {
    type: Boolean,
    default: false,
  },

  /** 한 줄에 표시할 카테고리 개수. 대분류는 4, 세부 분류는 2를 주로 사용합니다. */
  columns: {
    type: Number,
    default: 4,
  },

  /**
   * 기존 AnalysisCategoryEdit/Subcategory 화면의 조금 더 작은 카드 높이를 유지할 때 사용합니다.
   * 새 화면에서는 생략해도 됩니다.
   */
  compact: {
    type: Boolean,
    default: false,
  },

  /** 세부 카테고리가 있는 항목 아래에 '세부 선택' 안내 문구를 표시할지 여부 */
  showChildHint: {
    type: Boolean,
    default: false,
  },

  /** showChildHint=true일 때 표시할 문구 */
  childHint: {
    type: String,
    default: '세부 선택',
  },
});

/**
 * update:modelValue
 *   - v-model을 갱신합니다.
 *
 * select(category, meta)
 *   - 선택된 category 객체 전체와 { hasChildren } 정보를 전달합니다.
 *   - 상위 카테고리 선택 후 별도 세부 페이지로 이동해야 하는 기존 Analysis에서 사용합니다.
 */
const emit = defineEmits(['update:modelValue', 'select']);

const toComparableId = (value) => {
  if (value === null || value === undefined || value === '') return null;
  const numberValue = Number(value);
  return Number.isNaN(numberValue) ? String(value) : numberValue;
};

const normalizedParentCategoryId = computed(() =>
  toComparableId(props.parentCategoryId),
);

const normalizedColumns = computed(() =>
  String(Math.max(1, Number(props.columns) || 1)),
);

/**
 * parentCategoryId가 없으면 대분류만,
 * 있으면 해당 부모의 세부 카테고리만 노출합니다.
 */
const visibleCategories = computed(() => {
  const parentId = normalizedParentCategoryId.value;

  if (parentId === null) {
    return props.categories.filter(
      (category) => toComparableId(category.parentCategoryId) === null,
    );
  }

  return props.categories.filter(
    (category) => toComparableId(category.parentCategoryId) === parentId,
  );
});

const hasChildren = (categoryId) => {
  const comparableCategoryId = toComparableId(categoryId);
  return props.categories.some(
    (category) =>
      toComparableId(category.parentCategoryId) === comparableCategoryId,
  );
};

const isSelected = (categoryId) =>
  toComparableId(props.modelValue) === toComparableId(categoryId);

const selectCategory = (category) => {
  if (props.disabled) return;

  emit('update:modelValue', category.spendingCategoryId);
  emit('select', category, {
    hasChildren: hasChildren(category.spendingCategoryId),
  });
};
</script>

<style scoped>
.spending-category-selector {
  padding: 12px;
  display: grid;
  grid-template-columns: repeat(var(--category-columns), minmax(0, 1fr));
  gap: 8px;
  border: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-shadow: none;
}

.spending-category-selector__button {
  min-width: 0;
  min-height: 84px;
  padding: 9px 3px;
  border: 1px solid transparent;
  border-radius: 12px;
  background: var(--color-bg-screen);
  color: var(--color-text-sub);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.spending-category-selector--compact .spending-category-selector__button {
  min-height: 76px;
  padding: 8px 4px;
  background: #fafafa;
  color: #555;
}

.spending-category-selector__button.selected {
  border-color: var(--color-primary);
  background: #fff7d7;
  color: #8c6800;
  box-shadow: 0 0 0 1px var(--color-primary) inset;
}

.spending-category-selector--compact .spending-category-selector__button.selected {
  color: #8f6800;
  box-shadow: none;
}

/* 기존 AnalysisSubcategoryPage의 2열 세부 카테고리 스타일을 그대로 유지합니다. */
.spending-category-selector--detail {
  gap: 9px;
}

.spending-category-selector--detail .spending-category-selector__button {
  border-radius: 13px;
}

.spending-category-selector--detail .spending-category-selector__button.selected {
  background: #fff7d8;
  color: #8e6900;
}

.spending-category-selector--detail .spending-category-selector__icon {
  font-size: 19px;
}

.spending-category-selector--detail .spending-category-selector__button > span {
  margin-top: 5px;
}

.spending-category-selector__button:disabled {
  cursor: not-allowed;
}

.spending-category-selector__icon {
  height: 29px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  line-height: 1;
}

.spending-category-selector--compact .spending-category-selector__icon {
  height: auto;
}

.spending-category-selector__button > span {
  display: block;
  margin-top: 3px;
  line-height: 1.25;
  text-align: center;
  word-break: keep-all;
}

.spending-category-selector__hint {
  margin-top: 2px;
  color: var(--color-text-disabled);
  line-height: 1.2;
}
@media (max-width: 380px) {
  .spending-category-selector--responsive-four {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
