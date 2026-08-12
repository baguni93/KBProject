<template>
  <header class="page-header">
    <!-- 왼쪽 영역 -->
    <div class="header-side header-left">
      <button
        v-if="showBack"
        type="button"
        class="header-icon-btn"
        aria-label="이전 화면"
        @click="handleBack"
      >
        <i class="fa-solid fa-chevron-left"></i>
      </button>
    </div>

    <!-- 페이지 제목 -->
    <h2 class="header-title">
      {{ title }}
    </h2>

    <!-- 오른쪽 영역 -->
    <div class="header-side header-right">
      <button
        v-if="showRefresh"
        type="button"
        class="header-icon-btn"
        aria-label="새로고침"
        @click="emit('refresh')"
      >
        <i class="fa-solid fa-rotate-right"></i>
      </button>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const props = defineProps({
  // 페이지 제목
  title: {
    type: String,
    default: '',
  },

  // 뒤로가기 버튼 표시 여부
  showBack: {
    type: Boolean,
    default: true,
  },

  // 새로고침 버튼 표시 여부
  showRefresh: {
    type: Boolean,
    default: false,
  },

  // 뒤로가기 동작을 부모 화면에서 처리할지 여부
  customBack: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['back', 'refresh']);

// 이전 화면
const handleBack = () => {
  if (props.customBack) {
    emit('back');
    return;
  }

  router.back();
};
</script>

<style scoped>
@import '@/components/common/common/common.css';

/* ========================================
   공통 페이지 헤더
   - 모바일 화면 내부 상단에 배치
   - 모든 서브 화면에서 동일한 높이와 여백 사용
   - 콘텐츠 스크롤 시 화면 내부 상단에 고정
======================================== */

.page-header {
  position: sticky;
  top: 0;
  z-index: 100;

  display: grid;
  grid-template-columns: 40px 1fr 40px;
  align-items: center;

  width: 100%;
  height: 64px;
  padding: 10px 24px 0;

  background: var(--color-bg-page);
  box-sizing: border-box;
}

/* ========================================
   헤더 좌우 영역
======================================== */

.header-side {
  display: flex;
  align-items: center;
}

.header-left {
  justify-content: flex-start;
  margin-left: -35px;
}

.header-right {
  justify-content: flex-end;
  margin-right: -35px;
}

/* ========================================
   페이지 제목
======================================== */

.header-title {
  margin: 0;
  color: var(--color-text-main);
  font-size: 16px;
  font-weight: 700;
  line-height: 1.4;
  text-align: center;
}

/* ========================================
   헤더 아이콘 버튼
======================================== */

.header-icon-btn {
  display: flex;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;

  padding: 0;
  border: 0;
  border-radius: 50%;

  background: transparent;
  color: var(--color-text-main);

  font-size: 18px;
  cursor: pointer;
}

.header-icon-btn:hover {
  background: var(--color-bg-screen);
}

.header-icon-btn:active {
  opacity: 0.7;
}
</style>
