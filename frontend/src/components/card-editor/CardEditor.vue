<template>
  <div class="editor">
    <!-- 💡 1번: 우측 상단에 뜰 혜택 뱃지 (혜택이 선택되었을 때만 표시) -->
    <!-- 💡 1번: 혜택이 선택되었을 때 카드 영역 위에 뜰 뱃지 -->
    <div
      v-if="selectedBenefit"
      class="benefit-badge absolute top-4 right-4"
      :style="{ backgroundColor: selectedBenefit.color || '#3b82f6' }"
    >
      {{ selectedBenefit.name }} 적용됨
    </div>
    <!-- 카드 미리보기 -->
    <section class="editor-preview">
      <CardCanvas ref="childRef" />
    </section>

    <!-- 탭 -->
    <div class="editor-tabs" :style="{ height: props.tabHeight }">
      <div
        v-for="tab in tabs || []"
        :key="tab.key"
        class="tab"
        :class="{ active: currentTab === tab.key }"
        @click="currentTab = tab.key"
      >
        <i :class="tab.icon"></i>
        {{ tab.label }}
      </div>
    </div>

    <!-- 옵션 영역 -->
    <section class="editor-tools">
      <div class="editor-tools-inner">
        <slot :tab="currentTab" @select-benefit="handleBenefitSelected" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import CardCanvas from './CardCanvas.vue';
const props = defineProps({
  tabs: {
    type: Array,
    default: () => [],
  },
  tabHeight: {
    type: String,
    default: '42px',
  },
  selectedBenefit: { type: Object, default: null }, // 💡 부모가 주는 데이터
});

const currentTab = ref('');
watch(
  () => props.tabs,
  (tabs) => {
    if (tabs.length > 0) {
      currentTab.value = tabs[0].key;
    }
  },
  {
    immediate: true,
  },
);

const childRef = ref(null);
const emit = defineEmits(['select-benefit']);
// 💡 선택된 혜택 데이터를 저장할 상태 추가

const selectedBenefit = computed(() => props.selectedBenefit);
const handleBenefitSelected = (benefitPack) => {
  selectedBenefit.value = benefitPack;
  emit('select-benefit', benefitPack);
  console.log('선택된 혜택:', benefitPack);
};

defineExpose({
  childRef,
  selectedBenefit, // 나중에 서버로 보낼 때 필요하면 바깥에서도 쓰도록 expose!
});
</script>

<style scoped>
.editor {
  width: 100%;

  flex: 1;

  min-height: 0;

  display: flex;

  flex-direction: column;

  background: white;

  box-sizing: border-box;
}

/* 카드 미리보기 */
.editor-preview {
  height: 190px;

  flex-shrink: 0;

  display: flex;

  justify-content: center;

  align-items: center;

  background: #f3f2ef3f;

  border-radius: 8px;

  position: relative;
}

/* 탭 영역 */
.editor-tabs {
  height: 42px;

  flex-shrink: 0;

  display: flex;

  border-bottom: 1px solid #eee;
}

/* 탭 */
/* 탭 */
.tab {
  flex: 1;

  position: relative;

  display: flex;

  flex-direction: column;

  justify-content: center;

  align-items: center;

  gap: 4px;

  font-family: inherit;

  font-size: 12px;

  font-weight: 500;

  color: #aaa;

  cursor: pointer;

  user-select: none;

  transition: color 0.2s ease;
}

.tab i {
  font-size: 18px;
}

.tab.active {
  color: #ffc400;

  font-weight: 700;
}

.tab.active::after {
  content: '';

  position: absolute;

  bottom: -1px;

  left: 0;

  width: 100%;

  height: 2px;

  background: #ffc400;

  border-radius: 10px;
}

/* 옵션 스크롤 영역 */
.editor-tools {
  flex: 1;

  min-height: 0;

  overflow-y: auto;

  scrollbar-width: none;
}

.editor-tools::-webkit-scrollbar {
  display: none;
}

/* 실제 콘텐츠 여백 */
.editor-tools-inner {
  min-height: 100%;

  padding-top: 16px;

  padding-bottom: 24px;

  box-sizing: border-box;
}

/* 뱃지 디자인 스타일 */
.benefit-badge {
  color: white;
  padding: 6px 12px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  animation: popIn 0.3s ease-out;
  z-index: 10;
}

@keyframes popIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  80% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
