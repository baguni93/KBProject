<template>
  <div v-if="modelValue" class="modal-backdrop" @click.self="close">
    <div class="bottom-sheet">
      <!-- 핸들 -->
      <div class="sheet-handle"></div>

      <!-- 헤더 -->
      <div class="sheet-header">
        <div class="sheet-title">{{ title }}</div>

        <div class="sheet-desc">피드에 공유할 공개 범위를 선택해 주세요</div>
      </div>

      <!-- 공개 범위 -->
      <div class="options-container">
        <!-- 전체 공개 -->
        <div
          class="option-card"
          :class="{ active: selectedScope === 'PUBLIC' }"
          @click="selectedScope = 'PUBLIC'"
        >
          <div class="option-icon-text">
            <div class="icon-circle">
              <i :class="config.visibilityMap.PUBLIC.icon"></i>
            </div>

            <div class="text-group">
              <div class="option-title">전체 공개</div>

              <div class="option-desc">모든 사용자가 피드에서 볼 수 있어요</div>
            </div>
          </div>

          <div class="radio-icon">
            <i
              v-if="selectedScope === 'PUBLIC'"
              class="fa-solid fa-circle-check"
            ></i>

            <div v-else class="radio-empty"></div>
          </div>
        </div>

        <!-- 친구 공개 -->
        <div
          class="option-card"
          :class="{ active: selectedScope === 'FRIEND' }"
          @click="selectedScope = 'FRIEND'"
        >
          <div class="option-icon-text">
            <div class="icon-circle">
              <i :class="config.visibilityMap.FRIEND.icon"></i>
            </div>

            <div class="text-group">
              <div class="option-title">친구 공개</div>

              <div class="option-desc">친구로 등록된 사용자만 볼 수 있어요</div>
            </div>
          </div>

          <div class="radio-icon">
            <i
              v-if="selectedScope === 'FRIEND'"
              class="fa-solid fa-circle-check"
            ></i>

            <div v-else class="radio-empty"></div>
          </div>
        </div>
      </div>

      <!-- 피드 내용 -->
      <div class="feed-content-area">
        <textarea
          v-model="feedContent"
          placeholder="자랑하고 싶은 내용을 입력해 보세요!"
          maxlength="20"
          class="feed-textarea"
        ></textarea>

        <div class="char-count">{{ feedContent.length }} / 20</div>
      </div>

      <!-- 버튼 -->
      <div class="sheet-action-area">
        <button class="action-share-btn" type="button" @click="handleSubmit">
          자랑하기
        </button>

        <button class="action-cancel-btn" type="button" @click="close">
          취소
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import config from '@/config/feed';

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },

  title: {
    type: String,
    default: '나만의 카드 자랑하기',
  },
});

const emit = defineEmits(['update:modelValue', 'submit']);

const selectedScope = ref('PUBLIC');
const feedContent = ref('');

const close = () => {
  emit('update:modelValue', false);
};

const handleSubmit = () => {
  emit('submit', {
    visibility: selectedScope.value,
    content: feedContent.value,
  });
};
</script>

<style scoped>
/* =========================
   모달
========================= */

.modal-backdrop {
  position: absolute;

  top: 0;
  left: 0;

  width: 100%;
  height: 100%;

  background-color: rgba(0, 0, 0, 0.5);

  display: flex;
  align-items: flex-end;

  z-index: 100;

  animation: fadeIn 0.2s ease-out;
}

.bottom-sheet {
  width: 100%;
  max-height: 88%;

  overflow-y: auto;

  background-color: #ffffff;

  border-top-left-radius: 24px;
  border-top-right-radius: 24px;

  padding: 16px 20px 24px;

  box-sizing: border-box;

  display: flex;
  flex-direction: column;

  gap: 16px;

  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.sheet-handle {
  width: 40px;
  height: 4px;

  background-color: #dddddd;

  border-radius: 2px;

  align-self: center;

  margin-bottom: 4px;
}

.sheet-header {
  text-align: left;
}

.sheet-title {
  font-size: 16px;
  font-weight: bold;

  color: #111111;

  margin-bottom: 4px;
}

.sheet-desc {
  font-size: 13px;
  color: #888888;
}

/* =========================
   공개 범위
========================= */

.options-container {
  display: flex;
  flex-direction: column;

  gap: 10px;
}

.option-card {
  display: flex;

  align-items: center;
  justify-content: space-between;

  padding: 14px 16px;

  border-radius: 14px;

  border: 1.5px solid #eaeaea;

  background-color: #fafafa;

  cursor: pointer;

  transition: all 0.2s ease;
}

.option-card.active {
  border-color: #ffc107;
  background-color: #fffdf5;
}

.option-icon-text {
  display: flex;

  align-items: center;

  gap: 12px;
}

.icon-circle {
  width: 36px;
  height: 36px;

  font-size: 20px;

  background: #f0f0f0;

  border-radius: 50%;

  display: flex;

  align-items: center;
  justify-content: center;
}

.text-group {
  text-align: left;
}

.option-title {
  font-size: 15px;

  font-weight: bold;

  color: #222222;
}

.option-desc {
  font-size: 12px;

  color: #888888;

  margin-top: 2px;
}

.radio-icon {
  font-size: 18px;

  color: #ffc107;
}

.radio-empty {
  width: 18px;
  height: 18px;

  border: 2px solid #cccccc;

  border-radius: 50%;

  box-sizing: border-box;
}

/* =========================
   피드 내용
========================= */

.feed-content-area {
  margin: 20px 0;

  display: flex;
  flex-direction: column;

  gap: 8px;
}

.feed-textarea {
  width: 100%;
  height: 80px;

  padding: 12px;

  border-radius: 12px;

  border: 1px solid #e5e7eb;

  background-color: #f9fafb;

  resize: none;

  font-size: 14px;

  box-sizing: border-box;

  outline: none;
}

.feed-textarea:focus {
  border-color: #ffc107;

  background-color: #fffdf5;
}

.char-count {
  text-align: right;

  font-size: 12px;

  color: #9ca3af;
}

/* =========================
   버튼
========================= */

.sheet-action-area {
  display: flex;

  flex-direction: column;

  gap: 8px;

  margin-top: 8px;
}

.action-share-btn {
  width: 100%;
  height: 48px;

  border-radius: 24px;

  background-color: #ffc107;

  border: none;

  color: #111111;

  font-size: 16px;
  font-weight: bold;

  cursor: pointer;
}

.action-cancel-btn {
  width: 100%;
  height: 40px;

  background: transparent;

  border: none;

  color: #777777;

  font-size: 14px;
  font-weight: 600;

  cursor: pointer;
}

/* =========================
   애니메이션
========================= */

@keyframes fadeIn {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }

  to {
    transform: translateY(0);
  }
}
</style>
