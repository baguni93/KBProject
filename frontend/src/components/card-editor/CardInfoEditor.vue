<template>
  <div class="editor">
    <!-- 카드 미리보기 -->
    <section class="editor-preview">
      <CardBenifitCanvas :tab="currentTab" />
    </section>

    <!-- 탭 (tabs가 비어있으면 렌더링하지 않음) -->
    <div
      v-if="tabs && tabs.length > 0"
      class="editor-tabs"
      :style="{ height: props.tabHeight }"
    >
      <div
        v-for="tab in tabs"
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
        <slot :tab="currentTab" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import CardBenifitCanvas from './CardBenifitCanvas.vue';
const props = defineProps({
  tabs: {
    type: Array,
    default: () => [],
  },
  tabHeight: {
    type: String,
    default: '42px',
  },
});

const currentTab = ref('');
watch(
  () => props.tabs,
  (tabs) => {
    if (tabs && tabs.length > 0) {
      currentTab.value = tabs[0].key;
    } else {
      currentTab.value = '';
    }
  },
  {
    immediate: true,
  },
);
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
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  color: var(--color-text-white, #ffffff);
}

/* 탭 영역 */
.editor-tabs {
  height: 42px;

  flex-shrink: 0;

  display: flex;

  border-bottom: 1px solid #eee;
}

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
</style>
