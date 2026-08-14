<template>
  <Teleport to=".app">
    <div
      v-if="show"
      class="modal-overlay"
      @click.self="$emit('close')"
    >
      <div class="modal-card text-center">
        <div class="modal-icon">
          <i class="fa-solid fa-shield-halved brand-ic"></i>
        </div>
        <h5 class="text-20-bold m-0">간편 비밀번호 인증</h5>
        <p class="text-13 modal-sub">
          안전한 결제 승인을 위해 PIN 6자리를 입력하세요.
        </p>

        <div class="pin-dots-row">
          <span
            v-for="i in 6"
            :key="i"
            class="dot-item"
            :class="{ filled: inputPinCode.length >= i }"
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
  inputPinCode: {
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
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  box-sizing: border-box;
  border-radius: 24px;
}

@media (max-width: 430px) {
  .modal-overlay {
    border-radius: 0;
  }
}

.modal-card {
  background-color: #ffffff;
  border-radius: 14px;
  width: 100%;
  max-width: 320px;
  padding: 20px 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.modal-icon {
  font-size: 24px;
  margin-bottom: 6px;
  color: #ffbc2e;
}

.modal-sub {
  color: #777777;
  margin: 4px 0 16px 0;
}

.pin-dots-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.dot-item {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #dddddd;
  background-color: #ffffff;
}

.dot-item.filled {
  background-color: #ffbc2e;
  border-color: #cc9200;
}

.pin-keypad {
  max-width: 240px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.keypad-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.pin-btn {
  height: 44px;
  border: none;
  background-color: #f5f6f8;
  border-radius: 10px;
  color: #111111;
  cursor: pointer;
}

.pin-btn:hover {
  background-color: #dddddd;
}

.re-btn {
  color: #cc9200;
}

.del-btn {
  color: #777777;
}
</style>
