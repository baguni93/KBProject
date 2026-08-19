<template>
  <Teleport to=".app">
    <transition name="modal-fade-zoom">
      <div
        v-if="show"
        class="wallet-pin-modal-overlay"
        @click.self="$emit('close')"
      >
        <div class="wallet-pin-modal-card text-center animate-slide-up">
          <!-- Close Button -->
          <button type="button" class="pin-close-btn" @click="$emit('close')">
            <i class="fa-solid fa-xmark"></i>
          </button>

          <!-- 메인 비주얼 -->
          <div class="login-visual">
            <div class="visual-glow"></div>
            <div class="visual-icon">
              <i class="fa-solid fa-lock"></i>
            </div>
            <span class="visual-dot dot-one"></span>
            <span class="visual-dot dot-two"></span>
            <span class="visual-dot dot-three"></span>
          </div>

          <!-- 제목 및 안내 -->
          <div class="login-header">
            <h2 class="text-22-bold m-0">간편비밀번호 인증</h2>
            <p class="text-13 text-sub mt-2 mb-0">
              안전한 결제 승인을 위해 PIN 6자리를 입력하세요.
            </p>
          </div>

          <!-- 6자리 PIN Box 디스플레이 (PinLoginPage 디자인과 100% 동일) -->
          <div class="pin-boxes mt-4" :class="{ error: !!errorMessage }">
            <div
              v-for="index in 6"
              :key="index"
              class="pin-box"
              :class="{
                filled: inputPin.length >= index,
                active: inputPin.length === index - 1 && !errorMessage,
              }"
            >
              <span v-if="inputPin.length >= index" class="pin-dot"></span>
            </div>
          </div>

          <!-- 인라인 에러 메시지 (알림창 X) -->
          <p v-if="errorMessage" class="error-message text-13">
            {{ errorMessage }}
          </p>

          <!-- 간편비밀번호를 잊으셨나요? 링크 -->
          <button
            v-if="!pinLocked"
            class="forgot-button text-13"
            type="button"
            @click="$emit('forgotPin')"
          >
            간편비밀번호를 잊으셨나요?
          </button>

          <!-- 잠김 시 재설정 안내 버튼 -->
          <div v-if="pinLocked" class="locked-btn-area mt-3">
            <button
              class="bottom-btn primary-button text-14-bold"
              type="button"
              @click="$emit('forgotPin')"
            >
              본인인증 후 재설정
            </button>
          </div>

          <!-- 하단 키패드 -->
          <div v-if="!pinLocked" class="pin-keypad mt-4">
            <div class="keypad-row">
              <button
                v-for="n in [1, 2, 3]"
                :key="n"
                type="button"
                class="pin-num-btn text-18-bold"
                @click="$emit('enterPin', n)"
              >
                {{ n }}
              </button>
            </div>
            <div class="keypad-row">
              <button
                v-for="n in [4, 5, 6]"
                :key="n"
                type="button"
                class="pin-num-btn text-18-bold"
                @click="$emit('enterPin', n)"
              >
                {{ n }}
              </button>
            </div>
            <div class="keypad-row">
              <button
                v-for="n in [7, 8, 9]"
                :key="n"
                type="button"
                class="pin-num-btn text-18-bold"
                @click="$emit('enterPin', n)"
              >
                {{ n }}
              </button>
            </div>
            <div class="keypad-row">
              <button
                type="button"
                class="pin-num-btn action-text-btn text-13-bold"
                @click="$emit('clearPin')"
              >
                C
              </button>
              <button
                type="button"
                class="pin-num-btn text-18-bold"
                @click="$emit('enterPin', 0)"
              >
                0
              </button>
              <button
                type="button"
                class="pin-num-btn del-icon-btn text-15"
                @click="$emit('deletePin')"
              >
                <i class="fa-solid fa-delete-left"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  inputPin: {
    type: String,
    default: "",
  },
  errorMessage: {
    type: String,
    default: "",
  },
  pinLocked: {
    type: Boolean,
    default: false,
  },
});

defineEmits(["close", "enterPin", "clearPin", "deletePin", "forgotPin"]);
</script>

<style scoped>
@import "@/components/common/common/common.css";

.modal-fade-zoom-enter-active,
.modal-fade-zoom-leave-active {
  transition: opacity 0.25s cubic-bezier(0.16, 1, 0.3, 1),
              transform 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-fade-zoom-enter-from,
.modal-fade-zoom-leave-to {
  opacity: 0;
  transform: scale(0.92);
}

.wallet-pin-modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  box-sizing: border-box;
  border-radius: 24px;
}

@media (max-width: 430px) {
  .wallet-pin-modal-overlay {
    border-radius: 0;
  }
}

.wallet-pin-modal-card {
  width: 100%;
  max-width: 330px;
  background-color: #ffffff;
  border-radius: 24px;
  padding: 24px 20px 20px;
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  box-sizing: border-box;
}

.pin-close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  border: none;
  background: transparent;
  color: #999999;
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
}

.pin-close-btn:hover {
  color: #333333;
}

/* 비주얼 애니메이션 (PinLoginPage와 동일) */
.login-visual {
  position: relative;
  width: 76px;
  height: 76px;
  margin: 4px auto 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.visual-glow {
  position: absolute;
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(255, 188, 46, 0.45) 0%,
    rgba(255, 188, 46, 0.12) 65%,
    rgba(255, 188, 46, 0) 100%
  );
  animation: glow-breathe 2.4s ease-in-out infinite;
}

.visual-icon {
  position: relative;
  z-index: 1;
  width: 58px;
  height: 58px;
  border-radius: 22px;
  background: linear-gradient(135deg, #ffc43a 0%, #f59e0b 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 22px;
  box-shadow: 0 8px 18px rgba(245, 158, 11, 0.35);
}

.visual-dot {
  position: absolute;
  border-radius: 50%;
}

.dot-one {
  width: 6px;
  height: 6px;
  top: 10px;
  right: 6px;
  background: #8b5cf6;
}

.dot-two {
  width: 7px;
  height: 7px;
  bottom: 8px;
  left: 6px;
  background: #10b981;
}

.dot-three {
  width: 5px;
  height: 5px;
  top: 24px;
  right: -2px;
  background: #f43f5e;
}

.login-header h2 {
  color: var(--color-text-main, #111111);
}

.login-header p {
  color: var(--color-text-sub, #777777);
}

/* 6자리 PIN Box 디스플레이 (PinLoginPage 규격 100% 일치) */
.pin-boxes {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 6px;
  width: 100%;
  box-sizing: border-box;
}

.pin-box {
  display: flex;
  height: 48px;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border-main, #e5e7eb);
  border-radius: 10px;
  background: #fafafa;
  box-sizing: border-box;
  transition: border-color 0.2s, background 0.2s, box-shadow 0.2s;
}

.pin-box.active {
  border-color: var(--color-primary, #ffbc2e);
  background: #fffaf0;
  box-shadow: 0 0 0 2px rgba(255, 188, 46, 0.15);
}

.pin-box.filled {
  border-color: var(--color-primary, #ffbc2e);
  background: #fff8e5;
}

.pin-boxes.error .pin-box {
  border-color: var(--color-error, #f04438);
  background: #fff7f7;
  box-shadow: none;
}

.pin-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-text-main, #111111);
}

/* 오류 텍스트 */
.error-message {
  min-height: 18px;
  margin: 12px 0 0;
  color: var(--color-error, #f04438);
  line-height: 1.4;
  font-size: 13px;
  font-weight: 500;
  text-align: center;
}

/* 간편비밀번호를 잊으셨나요? */
.forgot-button {
  margin-top: 12px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-text-sub, #6b7280);
  cursor: pointer;
  text-decoration: underline;
  text-decoration-thickness: 1px;
  text-underline-offset: 4px;
}

.forgot-button:active {
  color: var(--color-text-main, #111111);
}

.locked-btn-area {
  width: 100%;
}

.locked-btn-area .bottom-btn {
  width: 100%;
  height: 44px;
  border-radius: 12px;
}

/* 키패드 */
.pin-keypad {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.keypad-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.pin-num-btn {
  flex: 1;
  height: 46px;
  border: none;
  background-color: #f8f9fa;
  border-radius: 10px;
  color: var(--color-text-main, #111111);
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.15s ease, transform 0.1s ease;
}

.pin-num-btn:active {
  background-color: #e9ecef;
  transform: scale(0.96);
}

.action-text-btn {
  color: #ff9800;
  background-color: #fff9ed;
}

.action-text-btn:active {
  background-color: #ffecb3;
}

.del-icon-btn {
  color: #495057;
  background-color: #f1f3f5;
}

.del-icon-btn:active {
  background-color: #dee2e6;
}

@keyframes glow-breathe {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.72;
    transform: scale(1.06);
  }
}
</style>
