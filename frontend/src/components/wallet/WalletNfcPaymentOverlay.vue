<template>
  <Teleport to=".app">
    <transition name="spay-overlay-fade">
      <div v-if="isNfcActive" class="spay-in-app-overlay" @click.stop>
        <div class="spay-wave-backdrop">
          <div class="wave-pulse ring-1"></div>
          <div class="wave-pulse ring-2"></div>
          <div class="wave-pulse ring-3"></div>
        </div>

        <div class="spay-top-header text-center">
          <span class="spay-badge text-15-bold">결제 대기중</span>
          <p class="timer-desc text-15">
            결제 남은시간
            <span class="timer-highlight text-20-bold">{{ formattedTimer }}</span>
          </p>
        </div>

        <div class="spay-giant-card-container">
          <div class="spay-giant-card">
            <img
              v-if="cardImg && !imgLoadError"
              :src="cardImg"
              class="giant-card-bg"
              :class="{ 'rotate-landscape': isLandscape }"
              alt="card plate"
              @load="onCardImgLoad"
              @error="onCardImgError"
            />
            <div v-else class="giant-card-fallback-content">
              <div class="fallback-top-row">
                <div class="fallback-chip-icon"></div>
                <span class="fallback-badge-kb text-12-bold">
                  <i class="fa-solid fa-shield-halved"></i> KB Pay
                </span>
              </div>
              <div class="fallback-center-symbol">
                <i class="fa-solid fa-credit-card"></i>
              </div>
              <div class="fallback-bottom-info">
                <p class="fallback-card-name text-16-bold">{{ card?.cardName || card?.cardAlias || 'KB국민카드' }}</p>
                <p class="fallback-card-num text-14-bold">{{ formatMaskedCardNum(card?.cardNum || card?.cardNumber) }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="bottom-btn-area single spay-bottom-area">
          <button
            type="button"
            class="bottom-btn secondary-button"
            @click="$emit('cancel')"
          >
            결제 취소
          </button>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from "vue";

const props = defineProps({
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
  card: {
    type: Object,
    default: () => ({}),
  },
});

defineEmits(["cancel"]);

const isLandscape = ref(false);
const imgLoadError = ref(false);

watch(
  () => props.cardImg,
  () => {
    imgLoadError.value = false;
    isLandscape.value = false;
  }
);

const onCardImgLoad = (e) => {
  const img = e.target;
  if (img && img.naturalWidth && img.naturalHeight) {
    isLandscape.value = img.naturalWidth > img.naturalHeight;
  }
};

const onCardImgError = () => {
  imgLoadError.value = true;
};

const formatMaskedCardNum = (num) => {
  if (!num) return "•••• •••• •••• ••••";
  const clean = String(num).replace(/\D/g, "");
  if (clean.length >= 4) {
    const last4 = clean.slice(-4);
    return `•••• •••• •••• ${last4}`;
  }
  return String(num);
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.spay-overlay-fade-enter-active,
.spay-overlay-fade-leave-active {
  transition: opacity 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.spay-overlay-fade-enter-from,
.spay-overlay-fade-leave-to {
  opacity: 0;
}

/* 삼성페이 시그니처: 가로 카드 ➔ 세로 90도 회전 & 3D 입체 스케일업 애니메이션 */
.spay-overlay-fade-enter-active .spay-giant-card {
  animation: samsungPay90DegreeRotate 0.55s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.spay-overlay-fade-leave-active .spay-giant-card {
  animation: samsungPay90DegreeRotateReverse 0.35s cubic-bezier(0.4, 0, 1, 1) forwards;
}

@keyframes samsungPay90DegreeRotate {
  0% {
    opacity: 0;
    transform: rotate(-90deg) scale(0.55) translateY(60px);
    filter: blur(4px);
  }
  65% {
    opacity: 0.95;
    transform: rotate(4deg) scale(1.04);
    filter: blur(0);
  }
  100% {
    opacity: 1;
    transform: rotate(0deg) scale(1) translateY(0);
    filter: blur(0);
  }
}

@keyframes samsungPay90DegreeRotateReverse {
  0% {
    opacity: 1;
    transform: rotate(0deg) scale(1);
  }
  100% {
    opacity: 0;
    transform: rotate(90deg) scale(0.55) translateY(60px);
  }
}

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
  padding: 32px 24px;
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
  padding: 6px 14px;
  border-radius: 20px;
  display: inline-block;
  margin-bottom: 10px;
  white-space: nowrap;
}

.timer-desc {
  margin: 0;
  color: rgba(255, 255, 255, 0.85);
  white-space: nowrap;
}

.timer-highlight {
  color: #ffbc2e;
  margin-left: 4px;
}

.spay-giant-card-container {
  z-index: 10;
  margin: auto 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.spay-giant-card {
  position: relative;
  width: 220px;
  height: 348px;
  border-radius: 16px;
  background: #111111;
  overflow: hidden;
  box-shadow: 0 0 35px rgba(255, 188, 46, 0.5), 0 10px 25px rgba(0, 0, 0, 0.5);
  border: 2px solid #ffbc2e;
  transform-origin: center center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.giant-card-bg {
  width: 100%;
  height: 100%;
  object-fit: fill;
  border-radius: 14px;
}

.giant-card-fallback-content {
  width: 100%;
  height: 100%;
  padding: 24px 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(145deg, #1e2024 0%, #121316 50%, #2a2820 100%);
  color: #ffffff;
  border-radius: 14px;
}

.fallback-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.fallback-chip-icon {
  width: 38px;
  height: 28px;
  background: linear-gradient(135deg, #e6b800, #ffd700);
  border-radius: 5px;
  box-shadow: inset 0 0 4px rgba(0,0,0,0.3);
}

.fallback-badge-kb {
  color: #ffbc2e;
}

.fallback-center-symbol {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 40px;
  color: rgba(255, 188, 46, 0.25);
}

.fallback-bottom-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.fallback-card-name {
  margin: 0;
  color: #f8fafc;
  word-break: keep-all;
}

.fallback-card-num {
  margin: 0;
  color: #94a3b8;
  letter-spacing: 1.5px;
}

/* 가로형 누워있는 이미지는 90도 회전시켜 세로 카드 프레임에 1:1 완벽 밀착 */
.giant-card-bg.rotate-landscape {
  width: 348px !important;
  height: 220px !important;
  max-width: none !important;
  max-height: none !important;
  transform: rotate(90deg);
  object-fit: fill;
}

.spay-bottom-area {
  width: 100%;
  z-index: 10;
  background: transparent !important;
  padding-top: 0 !important;
}
</style>
