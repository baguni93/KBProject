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

        <!-- 6자리 PIN Box 디스플레이 -->
        <div class="pin-boxes mt-4">
          <div
            v-for="index in 6"
            :key="index"
            class="pin-box"
            :class="{
              filled: inputPinCode.length >= index,
              active: inputPinCode.length === index - 1
            }"
          >
            <span v-if="inputPinCode.length >= index" class="pin-dot"></span>
          </div>
        </div>

        <!-- 하단 키패드 -->
        <div class="pin-keypad mt-4">
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
  inputPinCode: {
    type: String,
    default: "",
  },
});

defineEmits(["close", "enterPin", "clearPin", "deletePin"]);
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
  position: relative;
  background-color: #ffffff;
  border-radius: 24px;
  width: 100%;
  max-width: 340px;
  padding: 28px 20px 24px;
  box-shadow: 0 20px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  align-items: center;
  box-sizing: border-box;
}

.pin-close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  background: none;
  border: none;
  font-size: 18px;
  color: #a0aec0;
  cursor: pointer;
  padding: 4px;
  transition: color 0.2s;
}

.pin-close-btn:hover {
  color: #2d3748;
}

/* 메인 비주얼 */
.login-visual {
  position: relative;
  width: 76px;
  height: 76px;
  margin: 0 auto 16px;
}

.visual-glow {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.15);
  animation: pulse-glow 2s ease-in-out infinite;
}

.visual-icon {
  position: absolute;
  inset: 10px;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 20px;
  background: linear-gradient(135deg, #ffca52 0%, #ffbc2e 65%, #f3a711 100%);
  box-shadow: 0 8px 18px rgba(255, 188, 46, 0.3);
  color: #ffffff;
  font-size: 22px;
}

.visual-dot {
  position: absolute;
  z-index: 3;
  border-radius: 50%;
}

.dot-one {
  top: 2px;
  right: 6px;
  width: 7px;
  height: 7px;
  background: #8f81f5;
}

.dot-two {
  bottom: 4px;
  left: 2px;
  width: 6px;
  height: 6px;
  background: #6fd0bd;
}

.dot-three {
  right: 2px;
  bottom: 12px;
  width: 5px;
  height: 5px;
  background: #ff9aa7;
}

/* PIN 6자리 박스 */
.pin-boxes {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 6px;
  width: 100%;
}

.pin-box {
  display: flex;
  height: 48px;
  align-items: center;
  justify-content: center;
  border: 1.5px solid #e2e8f0;
  border-radius: 12px;
  background: #fafafa;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.pin-box.active {
  border-color: #ffbc2e;
  background: #fffaf0;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.18);
}

.pin-box.filled {
  border-color: #ffbc2e;
  background: #fff8e5;
}

.pin-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #111111;
}

/* 하단 키패드 */
.pin-keypad {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.keypad-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.pin-num-btn {
  height: 44px;
  border: none;
  background-color: #f8f9fa;
  border-radius: 12px;
  color: #111111;
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.pin-num-btn:hover {
  background-color: #edf2f7;
}

.action-text-btn {
  color: #d97706;
}

.del-icon-btn {
  color: #718096;
}

@keyframes pulse-glow {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.8; }
}
</style>
