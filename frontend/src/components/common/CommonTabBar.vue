<template>
  <nav
      class="common-tab-bar"
      :style="{
      gridTemplateColumns: `repeat(${tabs.length}, minmax(0, 1fr))`
    }"
  >
    <button
        v-for="tab in tabs"
        :key="tab.value"
        type="button"
        class="common-tab-btn"
        :class="{ active: modelValue === tab.value }"
        @click="selectTab(tab.value)"
    >
      {{ tab.label }}
    </button>
  </nav>
</template>

<script setup>
defineProps({
  /* ========================================
     탭 목록
     - 화면에 표시할 탭 이름과 값을 전달
     - 예) [{ label: '1개월', value: '1month' }]
  ======================================== */
  tabs: {
    type: Array,
    required: true,
  },

  /* ========================================
     현재 선택된 탭 값
     - 부모 컴포넌트의 v-model 값과 연결
  ======================================== */
  modelValue: {
    type: [String, Number],
    required: true,
  },
});

const emit = defineEmits(['update:modelValue']);

/* ========================================
   탭 선택
   - 선택한 탭의 값을 부모 컴포넌트로 전달
======================================== */
const selectTab = (value) => {
  emit('update:modelValue', value);
};
</script>

<style scoped>
/* ========================================
   공통 화면 내부 탭바
   - 화면 내 기능 또는 기간 선택에 사용
   - 전달받은 탭 개수에 따라 같은 너비로 자동 배치
   - 예) 친구 송금 / 계좌 이체 / 더치페이
   - 예) 1개월 / 3개월 / 12개월
   - 예) 전체 / 적립 / 사용
======================================== */

.common-tab-bar {
  display: grid;

  width: 100%;
  height: 44px;

  box-sizing: border-box;

  border-bottom: 1px solid var(--color-border-main);

  background: var(--color-bg-page);
}

/* ========================================
   개별 탭 버튼
   - 모든 탭을 동일한 너비로 배치
   - 기본 상태에서는 보조 텍스트 색상 사용
   - 선택된 탭은 진한 글자와 노란색 선으로 강조
======================================== */

.common-tab-btn {
  position: relative;

  display: flex;
  align-items: center;
  justify-content: center;

  min-width: 0;
  height: 44px;

  padding: 0 8px;

  box-sizing: border-box;

  border: none;

  background: transparent;
  color: var(--color-text-sub);

  font-size: 15px;
  font-weight: 500;

  cursor: pointer;
}

/* ========================================
   선택된 탭
   - 기본 텍스트 색상과 굵은 글씨로 표시
======================================== */

.common-tab-btn.active {
  color: var(--color-text-main);
  font-weight: 600;
}

/* ========================================
   선택된 탭 하단 표시선
   - 현재 선택된 탭을 노란색 선으로 표시
======================================== */

.common-tab-btn.active::after {
  position: absolute;

  right: 20%;
  bottom: 0;
  left: 20%;

  height: 3px;

  border-radius: 3px 3px 0 0;

  background: var(--color-primary);

  content: "";
}

/* ========================================
   마우스를 올렸을 때
   - 선택되지 않은 탭에만 연한 회색 배경 표시
======================================== */

.common-tab-btn:hover:not(.active) {
  background: var(--color-bg-screen);
}
</style>