<template>
  <Teleport to=".app">
    <div
      v-if="show"
      class="modal-overlay"
      @click.self="$emit('close')"
    >
      <div class="modal-card text-center">
        <div class="modal-icon">
          <i class="fa-solid fa-lock brand-ic"></i>
        </div>
        <h5 class="text-20-bold m-0">간편 비밀번호 인증</h5>
        <p class="text-13 modal-sub">
          송금 실행을 위해 PIN 6자리를 입력해 주세요.
        </p>

        <div class="pin-dots-row">
          <span
            v-for="i in 6"
            :key="i"
            class="dot-item"
            :class="{ filled: inputPin.length >= i }"
          ></span>
        </div>

        <div class="pin-keypad">
          <div class="keypad-row">
            <button
              v-for="n in [1, 2, 3]"
              :key="n"
              type="button"
              class="pin-btn text-18-bold"
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
              class="pin-btn text-18-bold"
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
              class="pin-btn text-18-bold"
              @click="$emit('enterPin', n)"
            >
              {{ n }}
            </button>
          </div>
          <div class="keypad-row">
            <button
              type="button"
              class="pin-btn re-btn text-13-bold"
              @click="$emit('clearPin')"
            >
              C
            </button>
            <button
              type="button"
              class="pin-btn text-18-bold"
              @click="$emit('enterPin', 0)"
            >
              0
            </button>
            <button
              type="button"
              class="pin-btn del-btn text-15"
              @click="$emit('deletePin')"
            >
              <i class="fa-solid fa-delete-left"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
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
});

defineEmits(["close", "enterPin", "clearPin", "deletePin"]);
</script>

<style scoped>
.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
  border-radius: 24px;
}

@media (max-width: 430px) {
  .modal-overlay {
    border-radius: 0;
  }
}

.modal-card {
  width: 100%;
  max-width: 340px;
  background-color: #ffffff;
  border-radius: 24px;
  padding: 28px 24px 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.modal-icon {
  width: 48px;
  height: 48px;
  background-color: rgba(255, 188, 0, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-bottom: 12px;
  color: #ffbc2e;
}

.modal-sub {
  color: #777777;
  margin-top: 6px;
  margin-bottom: 20px;
}

.pin-dots-row {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
}

.dot-item {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background-color: #e0e0e0;
  transition: all 0.2s ease;
}

.dot-item.filled {
  background-color: #ffbc00;
  transform: scale(1.15);
}

.pin-keypad {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.keypad-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.pin-btn {
  flex: 1;
  height: 48px;
  border: none;
  background-color: #f5f6f8;
  border-radius: 12px;
  color: #111111;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pin-btn:active {
  background-color: #e2e4e8;
}

.pin-btn.re-btn,
.pin-btn.del-btn {
  color: #666666;
  background-color: #edeef1;
}
</style>
