<!-- src/components/common/CommonModal.vue -->
<template>
  <div v-if="modalStore.isOpen" class="modal-backdrop">
    <div class="modal-box">
      <!-- 아이콘이나 타이틀 -->
      <h3 v-if="modalStore.title" class="modal-title">
        {{ modalStore.title }}
      </h3>

      <!-- 메시지 -->
      <p class="modal-desc" v-html="modalStore.message"></p>

      <!-- 버튼 영역 -->
      <div class="modal-btn-area">
        <button
          v-if="modalStore.showCancel"
          class="btn cancel-btn"
          @click="modalStore.handleCancel"
        >
          {{ modalStore.cancelText }}
        </button>
        <button class="btn confirm-btn" @click="modalStore.handleConfirm">
          {{ modalStore.confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useModalStore } from '@/stores/userModalStore';

const modalStore = useModalStore();
</script>

<style scoped>
/* 앱 모달 백드롭 (어두운 배경) */
.modal-backdrop {
  position: absolute;
  inset: 0;

  width: 100%;
  height: 100%;

  background-color: rgba(0, 0, 0, 0.5);

  display: flex;
  justify-content: center;
  align-items: center;

  z-index: 10000;
}

/* 모달 박스 (모바일 화면에 맞는 둥근 카드 형태s) */
.modal-box {
  width: 85%;
  max-width: 320px;
  background-color: #ffffff;
  border-radius: 20px;
  padding: 24px 20px 20px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-sizing: border-box;
}

.modal-title {
  font-size: 16px;
  font-weight: 700;
  color: #111111;
  margin: 0 0 8px 0;
}

.modal-desc {
  font-size: 14px;
  color: #555555;
  line-height: 1.5;
  margin: 0 0 20px 0;
}

.modal-btn-area {
  width: 100%;
  display: flex;
  gap: 8px;
}

.btn {
  flex: 1;
  height: 44px;
  border-radius: 12px;
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.confirm-btn {
  background-color: #ffbc2e; /* 앱 메인 컬러 */

  color: #666666;
}

.cancel-btn {
  background-color: #f1f2f4;
  color: #111111;
}
</style>
