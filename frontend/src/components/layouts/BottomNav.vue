<template>
  <nav class="bottom-nav">
    <RouterLink
      v-for="menu in config.menus"
      :key="menu.title"
      :to="menu.url"
      class="nav-item"
      :class="{ active: isActive(menu.url) }"
      @click="handleTabClick(menu.url)"
    >
      <div
        class="icon-box"
        :class="{
          center: menu.title === '결제',
          'finger-scanning': menu.title === '결제' && isLongPressing,
          'finger-complete': menu.title === '결제' && longPressComplete,
        }"
        @pointerdown="menu.title === '결제' && startLongPress()"
        @pointerup="menu.title === '결제' && endLongPress()"
        @pointerleave="menu.title === '결제' && endLongPress()"
        @pointercancel="menu.title === '결제' && endLongPress()"
      >
        <i :class="menu.icon"></i>

        <div v-if="menu.title === '결제' && isLongPressing" class="finger-scan">
          <div class="scan-line"></div>
        </div>
      </div>

      <span>{{ menu.title }}</span>
    </RouterLink>
  </nav>
</template>

<script setup>
import { ref, onBeforeUnmount } from 'vue';
import { useRoute } from 'vue-router';
import config from '@/config';

const route = useRoute();

const isActive = (url) => {
  // 홈은 정확히 일치
  if (url === '/') {
    return route.path === '/';
  }

  // 하위 경로까지 활성화
  return route.path.startsWith(url);
};

const emit = defineEmits(['tab-click']);

const handleTabClick = async (url) => {
  // 현재 활성화된 탭을 다시 클릭한 경우
  if (!isActive(url)) {
    return;
  }

  emit('tab-click', url);
};

/* =========================
   결제 버튼 롱프레스
========================= */

const isLongPressing = ref(false);
const longPressComplete = ref(false);

let longPressTimer = null;

const startLongPress = () => {
  // 이미 실행 중이면 무시
  if (longPressTimer) {
    return;
  }

  isLongPressing.value = true;
  longPressComplete.value = false;

  longPressTimer = setTimeout(() => {
    longPressTimer = null;

    isLongPressing.value = false;
    longPressComplete.value = true;

    console.log('결제 버튼 롱프레스 완료');

    // 완료 애니메이션 종료
    setTimeout(() => {
      longPressComplete.value = false;
    }, 500);
  }, 3000);
};

const endLongPress = () => {
  if (!longPressTimer) {
    return;
  }

  clearTimeout(longPressTimer);
  longPressTimer = null;

  isLongPressing.value = false;
};

onBeforeUnmount(() => {
  if (longPressTimer) {
    clearTimeout(longPressTimer);
  }
});
</script>

<style scoped>
.bottom-nav {
  width: 100%;
  height: 75px;

  background: #fff;

  display: flex;
  justify-content: space-around;
  align-items: center;

  border-top: 1px solid #eee;

  flex-shrink: 0;
  z-index: 9998;
}

.nav-item {
  flex: 1;
  height: 100%;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 5px;

  text-decoration: none;
  color: #666;

  transition: color 0.2s ease;
}

.icon-box {
  width: 35px;
  height: 35px;

  display: flex;
  justify-content: center;
  align-items: center;

  position: relative;
}

.icon-box i {
  font-size: 24px;
  transition: all 0.2s ease;
}

.nav-item span {
  font-size: 12px;
  font-weight: 500;
}

/* 활성화 */
.nav-item.active {
  color: #ffbf00;
}

.nav-item.active .icon-box:not(.center) i {
  color: #ffbf00;
}

/* 가운데 결제 버튼 */
.icon-box.center {
  width: 48px;
  height: 48px;

  margin-top: -18px;

  background: #ffbf00;
  color: white;

  border-radius: 17px;

  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);

  transition: all 0.2s ease;

  overflow: hidden;
}

/* 결제 탭 활성화 */
.nav-item.active .icon-box.center {
  width: 58px;
  height: 58px;

  margin-top: -28px;

  background: #ffbf00;
  color: white;
}

/* 결제 아이콘 */
.icon-box.center i {
  font-size: 26px;

  position: relative;
  z-index: 3;

  transition: all 0.2s ease;
}

.nav-item.active .icon-box.center i {
  font-size: 30px;
}

/* =========================
   지문 스캔
========================= */

.icon-box.finger-scanning {
  animation: fingerprintPulse 1s ease-in-out infinite;
}

/* 스캔 영역 */
.finger-scan {
  position: absolute;

  inset: 4px;

  border-radius: 14px;

  overflow: hidden;

  pointer-events: none;

  z-index: 2;
}

/* 스캔 라인 */
.scan-line {
  position: absolute;

  left: 0;
  right: 0;

  height: 3px;

  background: rgba(255, 255, 255, 0.95);

  box-shadow:
    0 0 5px rgba(255, 255, 255, 0.9),
    0 0 12px rgba(255, 255, 255, 0.8);

  animation: fingerprintScan 1.2s linear infinite;
}

/* 버튼 펄스 */
@keyframes fingerprintPulse {
  0% {
    box-shadow:
      0 0 0 0 rgba(255, 191, 0, 0.4),
      0 0 15px rgba(255, 191, 0, 0.3),
      0 5px 15px rgba(0, 0, 0, 0.15);
  }

  50% {
    box-shadow:
      0 0 0 5px rgba(255, 191, 0, 0.15),
      0 0 25px rgba(255, 191, 0, 0.6),
      0 5px 20px rgba(0, 0, 0, 0.15);
  }

  100% {
    box-shadow:
      0 0 0 0 rgba(255, 191, 0, 0.4),
      0 0 15px rgba(255, 191, 0, 0.3),
      0 5px 15px rgba(0, 0, 0, 0.15);
  }
}

/* 스캔 라인 이동 */
@keyframes fingerprintScan {
  0% {
    top: -5px;
    opacity: 0;
  }

  15% {
    opacity: 1;
  }

  50% {
    opacity: 1;
  }

  85% {
    opacity: 1;
  }

  100% {
    top: calc(100% + 5px);
    opacity: 0;
  }
}

/* 완료 */
.icon-box.finger-complete {
  animation: fingerprintComplete 0.5s ease;
}

@keyframes fingerprintComplete {
  0% {
    transform: scale(1);
  }

  40% {
    transform: scale(1.08);
  }

  70% {
    transform: scale(0.98);
  }

  100% {
    transform: scale(1);
  }
}

/* 가운데 버튼은 항상 흰색 */
.nav-item.active .icon-box.center {
  background: #ffbf00;
  color: white;
}
</style>
