<template>
  <div class="app-wrapper">
    <div class="app">
      <!-- 페이지 -->
      <main ref="contentRef" class="app-content">
        <slot />
      </main>

      <!-- 공통 Bottom -->
      <BottomNav v-if="showBottomNav" @tab-click="handleTabClick" />

      <TaskPush />

      <!-- 앱 내부 Overlay -->
      <div class="overlay-root">
        <CommonModal />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import BottomNav from './BottomNav.vue';
import CommonModal from '@/components/common/CommonModal.vue';
import { useFeedStore } from '@/stores/feed';
import TaskPush from '../common/TaskPush.vue';
const feedStore = useFeedStore();

defineProps({
  showBottomNav: {
    type: Boolean,
    default: true,
  },
});

const route = useRoute();

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

const handleTabClick = async (url) => {
  if (!contentRef.value) {
    return;
  }

  contentRef.value.scrollTo({
    top: 0,
    behavior: 'smooth',
  });

  if (url === '/feed') {
    await feedStore.refreshList({
      page: 0,
      size: 3,
    });
  }
};
</script>

<style scoped>
.app-wrapper {
  width: 100vw;
  height: 100vh;

  display: flex;
  justify-content: center;
  align-items: center;

  padding: 40px;

  background: #eeeeee;
  box-sizing: border-box;
}

.app {
  position: relative;

  width: 430px;
  height: calc(100vh - 80px);
  max-height: 900px;

  display: flex;
  flex-direction: column;

  background: white;
  border-radius: 24px;

  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.app-content {
  flex: 1;
  min-height: 0;

  overflow-y: auto;
  overflow-x: hidden;

  scrollbar-width: none;
  -ms-overflow-style: none;
}

.app-content::-webkit-scrollbar {
  display: none;
}

/* Teleport 목적지 */
#overlay-root {
  position: absolute;
  inset: 0;

  z-index: 10000;

  pointer-events: none;
}

#overlay-root > * {
  pointer-events: auto;
}

@media (max-width: 430px) {
  .app-wrapper {
    padding: 0;
    background: white;
  }

  .app {
    width: 100%;
    height: 100dvh;

    max-height: none;

    border-radius: 0;
    box-shadow: none;
  }
}
</style>
