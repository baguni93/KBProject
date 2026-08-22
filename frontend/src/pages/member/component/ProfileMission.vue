<template>
  <div
    v-if="!isLoading && completedCount < cards.length"
    class="profile-setup-container"
  >
    <!-- 헤더 -->
    <div class="header-area">
      <h2>프로필 완성하기</h2>

      <span class="completion-text">
        {{ completedCount }}/{{ cards.length }}개 완료
      </span>
    </div>

    <!--
      PC / Mobile 공통 Drag Scroll
      - PC : 마우스 드래그
      - Mobile : 손가락 드래그
      - Release : 관성 스크롤
    -->
    <div
      ref="sliderRef"
      class="card-slider"
      :class="{ 'is-dragging': isDragging }"
      @pointerdown="handlePointerDown"
      @pointermove="handlePointerMove"
      @pointerup="handlePointerUp"
      @pointercancel="handlePointerUp"
      @pointerleave="handlePointerLeave"
    >
      <div v-for="(card, index) in cards" :key="index" class="card-item">
        <!-- 아이콘 -->
        <div class="icon-wrapper">
          <div class="icon-circle">
            <i :class="card.icon"></i>
          </div>

          <!-- 완료 체크 -->
          <div v-if="card.isCompleted" class="badge">
            <i class="fa-solid fa-check"></i>
          </div>
        </div>

        <!-- 제목 -->
        <h3 class="card-title">
          {{ card.title }}
        </h3>

        <!-- 설명 -->
        <p class="card-desc">
          {{ card.description }}
        </p>

        <!-- 버튼 -->
        <button
          class="card-button"
          :class="{
            'completed-btn': card.isCompleted,
          }"
          @click="handleAction(card)"
        >
          {{ card.buttonText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';

import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';

import { useProfileStore } from '@/stores/profile';
import { useFriendStore } from '@/stores/friend';

// ========================================
// Router
// ========================================

const router = useRouter();

// ========================================
// Store
// ========================================

const profileStore = useProfileStore();
const friendStore = useFriendStore();

const { profile } = storeToRefs(profileStore);
const { friendList } = storeToRefs(friendStore);

// ========================================
// 사용자 ID
// ========================================

const userId = 3;

// ========================================
// Loading
// ========================================

const isLoading = ref(true);

// ========================================
// Slider
// ========================================

const sliderRef = ref(null);

// ========================================
// Drag 상태
// ========================================

const isDragging = ref(false);

let pointerId = null;

let startX = 0;

let startScrollLeft = 0;

let lastX = 0;

let lastTime = 0;

let velocity = 0;

let animationId = null;

// ========================================
// 프로필 완성 카드
// ========================================

const cards = computed(() => [
  {
    type: 'FRIEND',

    title: '내 친구 찾기',

    description: '친구를 1명 만들어보세요.',

    buttonText: '더 찾아보기',

    icon: 'fa-solid fa-user-group',

    isCompleted: friendList.value.length >= 1,
  },

  {
    type: 'PROFILE_IMAGE',

    title: '프로필 사진 추가',

    description: '회원님을 나타내는 사진을 선택하세요.',

    buttonText: '사진 변경',

    icon: 'fa-solid fa-camera',

    isCompleted:
      !!profile.value.imageName && profile.value.imageName !== 'unknown.png',
  },

  {
    type: 'INTRODUCTION',

    title: '소개 추가',

    description: '팔로워에게 회원님에 대해 간단히 소개해 주세요.',

    buttonText: '소개 추가',

    icon: 'fa-solid fa-comment-dots',

    isCompleted: !!profile.value.introduction?.trim(),
  },
]);

// ========================================
// 완료 개수
// ========================================

const completedCount = computed(() => {
  return cards.value.filter((card) => card.isCompleted).length;
});

// ========================================
// 버튼
// ========================================

const handleAction = (card) => {
  switch (card.type) {
    case 'FRIEND':
      router.push('/search');
      break;

    case 'PROFILE_IMAGE':
      router.push('/setting/profile');
      break;

    case 'INTRODUCTION':
      router.push('/setting/profile');
      break;

    default:
      console.warn('알 수 없는 프로필 완성 항목:', card.type);
  }
};

// ========================================
// Pointer Down
// ========================================

const handlePointerDown = (event) => {
  const slider = sliderRef.value;

  if (!slider) {
    return;
  }

  /*
   * 마우스 왼쪽 버튼만
   */
  if (event.pointerType === 'mouse' && event.button !== 0) {
    return;
  }

  /*
   * 기존 관성 중지
   */
  stopInertia();

  /*
   * 현재 pointer 기억
   */
  pointerId = event.pointerId;

  /*
   * pointer capture
   *
   * 마우스가 slider 밖으로 나가도
   * 계속 move 이벤트를 받을 수 있음
   */
  slider.setPointerCapture(event.pointerId);

  isDragging.value = true;

  startX = event.clientX;

  startScrollLeft = slider.scrollLeft;

  lastX = event.clientX;

  lastTime = performance.now();

  velocity = 0;
};

// ========================================
// Pointer Move
// ========================================

const handlePointerMove = (event) => {
  const slider = sliderRef.value;

  if (!slider || !isDragging.value || event.pointerId !== pointerId) {
    return;
  }

  const now = performance.now();

  /*
   * 처음 시작한 위치에서
   * 얼마나 움직였는지
   */
  const deltaX = event.clientX - startX;

  /*
   * 스크롤 위치
   *
   * 마우스가 오른쪽으로 가면
   * content는 왼쪽으로 이동
   */
  let nextScroll = startScrollLeft - deltaX;

  // ======================================
  // 속도 계산
  // ======================================

  const deltaTime = now - lastTime;

  if (deltaTime > 0) {
    velocity = (event.clientX - lastX) / deltaTime;
  }

  lastX = event.clientX;

  lastTime = now;

  // ======================================
  // 최대 스크롤
  // ======================================

  const maxScroll = Math.max(0, slider.scrollWidth - slider.clientWidth);

  // ======================================
  // Rubber Band
  // ======================================

  if (nextScroll < 0) {
    nextScroll *= 0.35;
  }

  if (nextScroll > maxScroll) {
    const over = nextScroll - maxScroll;

    nextScroll = maxScroll + over * 0.35;
  }

  slider.scrollLeft = nextScroll;

  /*
   * 기본 브라우저 동작 방지
   */
  if (event.pointerType === 'mouse') {
    event.preventDefault();
  }
};

// ========================================
// Pointer Up
// ========================================

const handlePointerUp = (event) => {
  if (!isDragging.value || event.pointerId !== pointerId) {
    return;
  }

  const slider = sliderRef.value;

  if (slider) {
    try {
      slider.releasePointerCapture(event.pointerId);
    } catch (e) {
      // 이미 해제된 경우 무시
    }
  }

  isDragging.value = false;

  pointerId = null;

  /*
   * 관성 시작
   */
  startInertia();
};

// ========================================
// Pointer Leave
// ========================================

const handlePointerLeave = (event) => {
  /*
   * pointer capture가 잡혀있으면
   * 실제로 leave 되어도 계속 동작함
   *
   * 여기서는 종료시키지 않음
   */
  if (isDragging.value && pointerId !== null) {
    return;
  }
};

// ========================================
// Inertia
// ========================================

const startInertia = () => {
  const slider = sliderRef.value;

  if (!slider) {
    return;
  }

  /*
   * 너무 느리면 관성 X
   */
  if (Math.abs(velocity) < 0.03) {
    return;
  }

  /*
   * 관성 감속
   *
   * 높을수록 오래 미끄러짐
   */
  const friction = 0.94;

  const animate = () => {
    const currentSlider = sliderRef.value;

    if (!currentSlider) {
      animationId = null;
      return;
    }

    const maxScroll = Math.max(
      0,
      currentSlider.scrollWidth - currentSlider.clientWidth,
    );

    /*
     * velocity를 scrollLeft에 적용
     */
    currentSlider.scrollLeft -= velocity * 16;

    /*
     * 감속
     */
    velocity *= friction;

    // ====================================
    // 왼쪽 끝
    // ====================================

    if (currentSlider.scrollLeft <= 0) {
      currentSlider.scrollLeft = 0;

      velocity *= 0.5;
    }

    // ====================================
    // 오른쪽 끝
    // ====================================

    if (currentSlider.scrollLeft >= maxScroll) {
      currentSlider.scrollLeft = maxScroll;

      velocity *= 0.5;
    }

    // ====================================
    // 종료
    // ====================================

    if (Math.abs(velocity) < 0.01) {
      animationId = null;
      return;
    }

    animationId = requestAnimationFrame(animate);
  };

  animationId = requestAnimationFrame(animate);
};

// ========================================
// Stop Inertia
// ========================================

const stopInertia = () => {
  if (animationId !== null) {
    cancelAnimationFrame(animationId);

    animationId = null;
  }
};

// ========================================
// 초기 데이터
// ========================================

onMounted(async () => {
  /*
   * FontAwesome
   */
  if (!document.getElementById('font-awesome')) {
    const link = document.createElement('link');

    link.id = 'font-awesome';

    link.rel = 'stylesheet';

    link.href =
      'https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css';

    document.head.appendChild(link);
  }

  try {
    await Promise.all([
      profileStore.getProfile(userId),

      friendStore.getFriendList(userId),
    ]);
  } catch (e) {
    console.error('프로필 완성 상태 조회 실패:', e);
  } finally {
    isLoading.value = false;
  }
});

// ========================================
// 제거
// ========================================

onBeforeUnmount(() => {
  stopInertia();

  pointerId = null;
});
</script>

<style scoped>
/* ========================================
   전체
======================================== */

.profile-setup-container {
  width: 100%;

  background-color: #ffffff;

  color: #262626;

  padding: 16px;

  box-sizing: border-box;

  border-bottom: 8px solid #f8f9fa;

  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* ========================================
   헤더
======================================== */

.header-area {
  margin-bottom: 12px;
}

.header-area h2 {
  font-size: 16px;

  font-weight: 700;

  margin: 0 0 2px 0;

  color: #111111;
}

.completion-text {
  font-size: 12px;

  color: #8e8e8e;
}

/* ========================================
   Slider
======================================== */

.card-slider {
  display: flex;

  gap: 10px;

  width: 100%;

  overflow-x: auto;

  overflow-y: hidden;

  /*
   * 기본 스크롤바 제거
   */
  scrollbar-width: none;

  /*
   * 모바일에서는 가로 제스처
   */
  touch-action: pan-x;

  /*
   * 텍스트 드래그 방지
   */
  user-select: none;

  -webkit-user-select: none;

  /*
   * iOS 관성
   */
  -webkit-overflow-scrolling: touch;

  /*
   * 부모 영역으로 overscroll 전파 방지
   */
  overscroll-behavior-x: contain;

  padding: 2px 16px 6px 2px;

  box-sizing: border-box;

  /*
   * PC에서 마우스 드래그 가능하다는 표시
   */
  cursor: grab;
}

.card-slider::-webkit-scrollbar {
  display: none;
}

/* ========================================
   드래그 중
======================================== */

.card-slider.is-dragging {
  cursor: grabbing;
}

/* ========================================
   카드
======================================== */

.card-item {
  flex: 0 0 165px;

  width: 165px;

  min-width: 165px;

  background-color: #ffffff;

  border: 1px solid #dbdbdb;

  border-radius: 10px;

  padding: 16px 12px;

  display: flex;

  flex-direction: column;

  align-items: center;

  text-align: center;

  box-sizing: border-box;

  pointer-events: auto;
}

/* ========================================
   아이콘
======================================== */

.icon-wrapper {
  position: relative;

  margin-bottom: 12px;
}

.icon-circle {
  width: 50px;

  height: 50px;

  border: 1px solid #dbdbdb;

  border-radius: 50%;

  display: flex;

  align-items: center;

  justify-content: center;

  font-size: 18px;

  color: #262626;

  background-color: #fafafa;
}

/* ========================================
   완료 배지
======================================== */

.badge {
  position: absolute;

  bottom: 0;

  right: 0;

  width: 18px;

  height: 18px;

  background-color: #0095f6;

  color: white;

  border-radius: 50%;

  border: 2px solid #ffffff;

  display: flex;

  align-items: center;

  justify-content: center;

  font-size: 9px;
}

/* ========================================
   제목
======================================== */

.card-title {
  font-size: 14px;

  font-weight: 600;

  margin: 0 0 6px;

  color: #262626;

  min-height: 18px;
}

/* ========================================
   설명
======================================== */

.card-desc {
  font-size: 11px;

  color: #8e8e8e;

  line-height: 1.3;

  margin: 0 0 14px;

  flex-grow: 1;

  display: flex;

  align-items: flex-start;

  justify-content: center;
}

/* ========================================
   버튼
======================================== */

.card-button {
  width: 100%;

  padding: 8px 0;

  background-color: #0095f6;

  color: white;

  border: none;

  border-radius: 6px;

  font-size: 12px;

  font-weight: 600;

  cursor: pointer;

  touch-action: manipulation;

  transition:
    background-color 0.2s,
    transform 0.1s;
}

.card-button:hover {
  background-color: #1877f2;
}

.card-button:active {
  transform: scale(0.97);
}

/* ========================================
   완료 버튼
======================================== */

.completed-btn {
  background-color: #efefef;

  color: #262626;
}

.completed-btn:hover {
  background-color: #dbdbdb;
}
</style>
