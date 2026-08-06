<template>
  <div class="app-wrapper">
    <div class="container" id="app-container">
      <!-- Content -->
      <div ref="contentRef" class="content"
           :class="{
             'has-bottom-nav': props.showBottomNav,
             'hide-scrollbar': hideScrollbar,
           }">
        <slot></slot>
      </div>

      <!-- Bottom -->
      <BottomNav v-if="showBottomNav" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import BottomNav from './BottomNav.vue';

const props = defineProps({
  showBottomNav: {
    type: Boolean,
    default: true,
  },
});

const route = useRoute();

const scrollbarHiddenRouteNames = new Set([
  'analysis-main',
  'analysis-agreement',
  'analysis-result',
]);

const hideScrollbar = computed(() =>
  scrollbarHiddenRouteNames.has(String(route.name ?? '')),
);

const contentRef = ref(null);

/**
 * 페이지 이동 시 스크롤 최상단 이동
 */
watch(
  () => route.path,
  async () => {
    try {
      await nextTick();
      if (contentRef.value) {
        contentRef.value.scrollTop = 0;
      }
    } catch (e) {
      console.log('Scroll top bypass', e);
    }
  },
);
</script>

<style scoped>
/* 웹 화면 */
.app-wrapper {
  width: 100vw;

  height: 100vh;

  display: flex;

  justify-content: center;

  align-items: center;

  padding: 40px;

  background: #eeeeed;

  box-sizing: border-box;
}

/* 앱 화면 */
.container {
  width: 430px;

  height: calc(100vh - 80px);

  max-height: 900px;

  display: flex;

  flex-direction: column;

  background: white;

  border-radius: 24px;

  overflow: hidden;

  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);

  position: relative;
}

/* 가운데 컨텐츠 */
.content {
  flex: 1;

  min-height: 0;

  overflow-y: auto;

  overflow-x: hidden;

  padding-top: 16px;

  padding-bottom: 0;

  box-sizing: border-box;

  background: white;
}

.content.has-bottom-nav {
  padding-bottom: 90px;
}

/* 소비분석 메인·동의·상세 화면에서만 스크롤바 표시를 숨긴다. */
.content.hide-scrollbar {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.content.hide-scrollbar::-webkit-scrollbar {
  display: none;
}

/* BottomNav 위에 띄우기 */
:deep(.bottom-nav) {
  position: absolute;

  bottom: 0;

  left: 0;

  width: 100%;

  z-index: 100;
}

/* 모바일 */
@media (max-width: 430px) {
  .app-wrapper {
    padding: 0;

    background: white;
  }

  .container {
    width: 100%;

    height: 100dvh;

    max-height: none;

    border-radius: 0;

    box-shadow: none;
  }
}
</style>
