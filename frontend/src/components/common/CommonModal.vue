<!-- src/components/common/CommonModal.vue -->
<template>
  <Transition name="common-modal">
    <div
      v-if="modalStore.isOpen"
      class="modal-backdrop"
      @click.self="modalStore.handleCancel"
    >
      <section
        class="modal-box"
        :class="{ 'success-modal': modalStore.isSuccess }"
        role="dialog"
        aria-modal="true"
      >
        <!-- 아이콘 -->
        <div class="modal-icon-wrap">
          <div
            class="modal-icon"
            :class="{ 'success-icon': modalStore.isSuccess }"
          >
            <i v-if="modalStore.isSuccess" class="fa-solid fa-check"></i>

            <i v-else class="fa-solid fa-exclamation"></i>
          </div>
        </div>

        <!-- 내용 -->
        <div class="modal-content">
          <h3 v-if="modalStore.title" class="modal-title">
            {{ modalStore.title }}
          </h3>

          <p class="modal-desc">
            {{ modalStore.message }}
          </p>
        </div>

        <!-- 버튼 -->
        <div class="modal-btn-area">
          <button
            v-if="modalStore.showCancel"
            class="modal-btn cancel-btn"
            type="button"
            @click="modalStore.handleCancel"
          >
            {{ modalStore.cancelText }}
          </button>

          <button
            class="modal-btn confirm-btn"
            :class="{ 'success-confirm-btn': modalStore.isSuccess }"
            type="button"
            @click="modalStore.handleConfirm"
          >
            {{ modalStore.confirmText }}
          </button>
        </div>
      </section>
    </div>
  </Transition>
</template>

<script setup>
import { useModalStore } from '@/stores/userModalStore';

const modalStore = useModalStore();
</script>

<style scoped>
/* ========================================
   공통 모달
======================================== */

.modal-backdrop {
  position: fixed;

  z-index: 10000;

  inset: 0;

  display: flex;

  align-items: center;
  justify-content: center;

  padding: 24px;

  background: rgba(15, 15, 15, 0.42);

  backdrop-filter: blur(3px);
  -webkit-backdrop-filter: blur(3px);

  box-sizing: border-box;
}

.modal-box {
  width: 100%;
  max-width: 320px;

  padding: 28px 22px 20px;

  border-radius: 22px;

  background: var(--color-bg-page);

  box-shadow: 0 20px 55px rgba(0, 0, 0, 0.2);

  box-sizing: border-box;

  text-align: center;
}

/* ========================================
   성공 모달
======================================== */

.modal-box.success-modal {
  box-shadow:
    0 20px 55px rgba(255, 188, 46, 0.18),
    0 8px 25px rgba(0, 0, 0, 0.08);
}

/* ========================================
   아이콘
======================================== */

.modal-icon-wrap {
  display: flex;

  justify-content: center;

  margin-bottom: 18px;
}

.modal-icon {
  display: flex;

  width: 62px;
  height: 62px;

  align-items: center;
  justify-content: center;

  border-radius: 50%;

  background: rgba(255, 188, 46, 0.16);

  color: var(--color-primary-border);

  font-size: 25px;
}

/* 성공 아이콘 */

.success-icon {
  position: relative;

  background: linear-gradient(145deg, #ffd15c, var(--color-primary));

  color: #ffffff;

  box-shadow:
    0 10px 24px rgba(255, 188, 46, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);

  animation: successIconPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.success-icon i {
  animation: checkAppear 0.3s ease 0.2s both;
}

/* 성공 아이콘 주변 빛 */

.success-icon::before {
  content: '';

  position: absolute;

  inset: -7px;

  border-radius: 50%;

  border: 1px solid rgba(255, 188, 46, 0.2);

  animation: successRing 1.2s ease-out 0.15s both;
}

/* ========================================
   내용
======================================== */

.modal-content {
  text-align: center;
}

.modal-title {
  margin: 0;

  color: var(--color-text-main);

  font-size: 20px;
  font-weight: 700;

  line-height: 1.4;
  word-break: keep-all;
}

.modal-desc {
  margin: 9px 0 0;

  color: var(--color-text-sub);

  font-size: 14px;
  font-weight: 400;

  line-height: 1.5;

  white-space: pre-line;
  word-break: keep-all;
}

/* ========================================
   버튼
======================================== */

.modal-btn-area {
  display: flex;

  gap: 10px;

  margin-top: 24px;
}

.modal-btn {
  flex: 1;

  height: 50px;

  border-radius: 14px;

  font-family: inherit;

  font-size: 15px;
  font-weight: 700;

  cursor: pointer;

  box-sizing: border-box;
}

/* 취소 */

.cancel-btn {
  border: 1px solid var(--color-border-main);

  background: var(--color-bg-page);

  color: var(--color-text-main);
}

.cancel-btn:active {
  background: var(--color-bg-screen);
}

/* 확인 */

.confirm-btn {
  border: 1px solid var(--color-primary-border);

  background: var(--color-primary);

  color: var(--color-text-main);
}

.confirm-btn:active {
  background: var(--color-primary-active);
}

/* 성공 확인 버튼 */

.success-confirm-btn {
  background: linear-gradient(135deg, #ffd15c, var(--color-primary));

  border-color: var(--color-primary);

  box-shadow: 0 5px 14px rgba(255, 188, 46, 0.18);
}

/* ========================================
   성공 애니메이션
======================================== */

@keyframes successIconPop {
  0% {
    opacity: 0;

    transform: scale(0.5);
  }

  70% {
    transform: scale(1.08);
  }

  100% {
    opacity: 1;

    transform: scale(1);
  }
}

@keyframes checkAppear {
  from {
    opacity: 0;

    transform: scale(0.4) rotate(-15deg);
  }

  to {
    opacity: 1;

    transform: scale(1) rotate(0);
  }
}

@keyframes successRing {
  0% {
    opacity: 0.8;

    transform: scale(0.8);
  }

  100% {
    opacity: 0;

    transform: scale(1.25);
  }
}

/* ========================================
   모달 애니메이션
======================================== */

.common-modal-enter-active,
.common-modal-leave-active {
  transition: opacity 0.2s ease;
}

.common-modal-enter-active .modal-box,
.common-modal-leave-active .modal-box {
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.common-modal-enter-from,
.common-modal-leave-to {
  opacity: 0;
}

.common-modal-enter-from .modal-box {
  opacity: 0;

  transform: translateY(12px) scale(0.96);
}

.common-modal-leave-to .modal-box {
  opacity: 0;

  transform: translateY(6px) scale(0.98);
}

/* ========================================
   모바일
======================================== */

@media (max-width: 360px) {
  .modal-box {
    padding: 24px 18px 18px;
  }

  .modal-icon {
    width: 56px;
    height: 56px;

    font-size: 22px;
  }

  .modal-title {
    font-size: 18px;
  }

  .modal-btn {
    height: 46px;

    font-size: 14px;
  }
}

/* ========================================
   모션 감소
======================================== */

@media (prefers-reduced-motion: reduce) {
  .common-modal-enter-active,
  .common-modal-leave-active,
  .common-modal-enter-active .modal-box,
  .common-modal-leave-active .modal-box {
    transition: none;
  }

  .success-icon,
  .success-icon i,
  .success-icon::before {
    animation: none;
  }
}
</style>
