<template>
  <Teleport to=".app">
    <div v-if="isNfcActive" class="spay-in-app-overlay" @click.stop>
      <div class="spay-wave-backdrop">
        <div class="wave-pulse ring-1"></div>
        <div class="wave-pulse ring-2"></div>
        <div class="wave-pulse ring-3"></div>
      </div>

      <div class="spay-top-header text-center">
        <span class="spay-badge text-15-bold">결제 대기중</span>
        <p class="timer-desc text-13">
          결제 남은시간
          <span class="timer-highlight text-18-bold">{{ formattedTimer }}</span>
        </p>
      </div>

      <div class="spay-giant-card-container">
        <div class="spay-giant-card">
          <img
            v-if="cardImg"
            :src="cardImg"
            class="giant-card-bg"
            alt="giant card"
          />
        </div>
      </div>

      <div class="spay-bottom-actions">
        <button
          type="button"
          class="bottom-btn text-18-bold cancel-overlay-btn"
          @click="$emit('cancel')"
        >
          결제 취소
        </button>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({
  isNfcActive: {
    type: Boolean,
    default: false,
  },
  formattedTimer: {
    type: String,
    default: "00:50",
  },
  cardImg: {
    type: String,
    default: null,
  },
});

defineEmits(["cancel"]);
</script>

<style scoped>
.spay-in-app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 99999;
  background: rgba(17, 17, 17, 0.95);
  backdrop-filter: blur(8px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 28px 16px 36px;
  box-sizing: border-box;
  overflow: hidden;
  border-radius: 24px;
}

@media (max-width: 430px) {
  .spay-in-app-overlay {
    border-radius: 0;
  }
}

.spay-wave-backdrop {
  position: absolute;
  top: 45%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 240px;
  height: 240px;
  pointer-events: none;
}

.wave-pulse {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 2px solid rgba(255, 188, 46, 0.4);
  animation: wavePulse 2.4s infinite ease-out;
}

.wave-pulse.ring-2 {
  animation-delay: 0.8s;
}

.wave-pulse.ring-3 {
  animation-delay: 1.6s;
}

@keyframes wavePulse {
  0% {
    transform: scale(0.6);
    opacity: 0.9;
  }
  100% {
    transform: scale(2.2);
    opacity: 0;
  }
}

.spay-top-header {
  color: #ffffff;
  z-index: 10;
}

.spay-badge {
  background-color: #ffbc2e;
  color: #111111;
  padding: 4px 12px;
  border-radius: 9999px;
  display: inline-block;
  margin-bottom: 6px;
  white-space: nowrap;
}

.timer-desc {
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  white-space: nowrap;
}

.timer-highlight {
  color: #ffbc2e;
}

.spay-giant-card-container {
  z-index: 10;
  margin: auto 0;
}

.spay-giant-card {
  position: relative;
  width: 280px;
  height: 175px;
  border-radius: 14px;
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  overflow: hidden;
  box-shadow: 0 0 20px rgba(255, 188, 46, 0.4);
  border: 2px solid #ffbc2e;
  transform: rotate(90deg);
}

.giant-card-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.spay-bottom-actions {
  width: 100%;
  max-width: 280px;
  z-index: 10;
}

.cancel-overlay-btn {
  width: 100%;
  height: 48px;
  background-color: #ffffff;
  color: #111111;
  border: none;
  border-radius: 14px;
  font-weight: 700;
  cursor: pointer;
  white-space: nowrap;
}
</style>
