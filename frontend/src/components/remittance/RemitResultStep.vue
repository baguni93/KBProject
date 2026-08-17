<template>
  <div class="page-layout complete-page">
    <!-- 배경 장식 -->
    <div class="background-decoration decoration-left"></div>
    <div class="background-decoration decoration-right"></div>

    <main class="page-content complete-container">
      <section class="complete-content">
        <!-- 완료 애니메이션 -->
        <div class="success-visual">
          <span class="particle particle-1"></span>
          <span class="particle particle-2"></span>
          <span class="particle particle-3"></span>
          <span class="particle particle-4"></span>
          <span class="particle particle-5"></span>
          <span class="particle particle-6"></span>

          <!-- 별 장식 -->
          <span class="spark spark-1">
            <i class="fa-solid fa-star"></i>
          </span>
          <span class="spark spark-2">
            <i class="fa-solid fa-star"></i>
          </span>

          <div class="success-glow"></div>
          <div class="success-circle">
            <i class="fa-solid fa-check"></i>
          </div>
        </div>

        <!-- 완료 메시지 -->
        <div class="complete-message">
          <h1 class="text-30-bold">
            {{
              remitType === "DUTCH"
                ? "정산 요청이 완료되었어요!"
                : "송금이 완료되었어요!"
            }}
          </h1>

          <p class="complete-description text-20-bold" style="margin-top: 8px; color: #111111;">
            +{{ formatCurrency(remitAmount || 10000) }} 원
          </p>
        </div>

        <!-- 상세 내역 요약 카드 -->
        <div class="complete-detail-card">
          <template v-if="remitType === 'DUTCH'">
            <div class="detail-row text-14">
              <span class="lbl text-14">정산 멤버</span>
              <span class="val text-15-bold">
                {{ selectedDutchFriends && selectedDutchFriends.length > 0 ? `${getFriendName(selectedDutchFriends[0])} 외 ${selectedDutchFriends.length}명` : '참여자' }}
              </span>
            </div>
            <div class="detail-row text-14">
              <span class="lbl text-14">1인당 요청 금액</span>
              <span class="val text-15-bold">
                {{ formatCurrency(Math.floor((remitAmount || 10000) / ((selectedDutchFriends?.length || 1) + 1))) }} 원
              </span>
            </div>
          </template>

          <template v-else>
            <div class="detail-row text-14">
              <span class="lbl text-14">받는 사람</span>
              <span class="val text-15-bold">{{ receiverName }}</span>
            </div>
            <div v-if="accountNumber" class="detail-row text-14">
              <span class="lbl text-14">입금 계좌</span>
              <span class="val text-15-bold"
                >{{ bankName }} {{ accountNumber }}</span
              >
            </div>
          </template>
        </div>
      </section>
    </main>

    <!-- 하단 2개 버튼 (결제 / 내 정산) -->
    <div class="bottom-btn-area double complete-button-area">
      <button
        class="bottom-btn secondary-button complete-button text-16-bold"
        type="button"
        @click="handleGoWallet"
      >
        결제
      </button>

      <button
        class="bottom-btn primary-button complete-button text-16-bold"
        type="button"
        @click="handleGoDutchHistory"
      >
        내 정산
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

const props = defineProps({
  remitType: {
    type: String,
    default: "ACCOUNT",
  },
  remitAmount: {
    type: Number,
    default: 0,
  },
  selectedDutchFriends: {
    type: Array,
    default: () => [],
  },
  getFriendName: {
    type: Function,
    default: () => "참여자",
  },
  receiverName: {
    type: String,
    default: "",
  },
  bankName: {
    type: String,
    default: "",
  },
  accountNumber: {
    type: String,
    default: "",
  },
  formatCurrency: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits(["resetAll"]);

// 1. 결제 화면으로 이동
const handleGoWallet = () => {
  emit("resetAll");
  router.push("/wallet");
};

// 2. 내 정산 내역 / 계좌 관리로 이동
const handleGoDutchHistory = () => {
  emit("resetAll");
  router.push("/setting/accounts");
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.complete-page {
  position: relative;
  overflow: hidden;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
}

.complete-container {
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  padding: 20px 16px;
}

.complete-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  width: 100%;
  max-width: 360px;
  margin: 0 auto;
}

.background-decoration {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.decoration-left {
  top: -100px;
  left: -120px;
  width: 240px;
  height: 240px;
  background: rgba(255, 188, 46, 0.1);
  animation: background-float-left 6s ease-in-out infinite;
}

.decoration-right {
  top: 280px;
  right: -110px;
  width: 210px;
  height: 210px;
  background: rgba(176, 164, 255, 0.05);
  animation: background-float-right 7s ease-in-out infinite;
}

.success-visual {
  position: relative;
  width: 180px;
  height: 180px;
  margin: 0 auto 22px;
}

.success-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 126px;
  height: 126px;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.16);
  transform: translate(-50%, -50%);
  animation: glow 2.2s ease-in-out 0.8s infinite;
}

.success-circle {
  position: absolute;
  z-index: 2;
  top: 50%;
  left: 50%;
  display: flex;
  width: 102px;
  height: 102px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(145deg, #ffd15c, var(--color-primary, #ffbc2e));
  box-shadow:
    0 16px 34px rgba(255, 188, 46, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  color: #ffffff;
  font-size: 42px;
  transform: translate(-50%, -50%) scale(0);
  animation: success-pop 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.success-circle i {
  opacity: 0;
  transform: scale(0.5) rotate(-15deg);
  animation: check-appear 0.35s ease 0.42s forwards;
}

.particle {
  position: absolute;
  z-index: 1;
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary, #ffbc2e);
  opacity: 0;
}

.particle-1 {
  top: 28px;
  left: 28px;
  animation: particle-pop 0.55s ease 0.35s forwards;
}

.particle-2 {
  top: 14px;
  right: 28px;
  width: 6px;
  height: 6px;
  background: #9d90ff;
  animation: particle-pop 0.55s ease 0.5s forwards;
}

.particle-3 {
  top: 70px;
  right: 6px;
  width: 9px;
  height: 9px;
  background: #7bd6c7;
  animation: particle-pop 0.55s ease 0.4s forwards;
}

.particle-4 {
  right: 24px;
  bottom: 18px;
  width: 7px;
  height: 7px;
  background: #ff9eaa;
  animation: particle-pop 0.55s ease 0.6s forwards;
}

.particle-5 {
  bottom: 20px;
  left: 24px;
  width: 6px;
  height: 6px;
  background: #9d90ff;
  animation: particle-pop 0.55s ease 0.48s forwards;
}

.particle-6 {
  top: 78px;
  left: 4px;
  width: 8px;
  height: 8px;
  background: #7bd6c7;
  animation: particle-pop 0.55s ease 0.58s forwards;
}

.spark {
  position: absolute;
  z-index: 1;
  color: #ffd65c;
  opacity: 0;
}

.spark-1 {
  top: 14px;
  left: 60px;
  font-size: 12px;
  animation: spark-pop 0.55s ease 0.55s forwards;
}

.spark-2 {
  right: 38px;
  bottom: 14px;
  color: #a99df7;
  font-size: 10px;
  animation: spark-pop 0.55s ease 0.7s forwards;
}

.complete-message {
  opacity: 0;
  transform: translateY(16px);
  animation: content-up 0.5s ease 0.55s forwards;
}

.complete-message h1 {
  margin: 0;
  color: var(--color-text-main, #111111);
  line-height: 1.3;
  letter-spacing: -0.7px;
}

.complete-detail-card {
  width: 100%;
  margin-top: 20px;
  padding: 16px 18px;
  background-color: var(--color-bg-screen, #f8f9fa);
  border-radius: 16px;
  box-sizing: border-box;
  opacity: 0;
  transform: translateY(16px);
  animation: content-up 0.5s ease 0.65s forwards;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.lbl {
  color: #777777;
}

.val {
  color: #111111;
}

.complete-button-area {
  position: relative;
  z-index: 3;
  width: 100%;
}

.complete-button {
  opacity: 0;
  transform: translateY(10px);
  animation: button-show 0.45s ease forwards;
}

.complete-button:nth-child(1) {
  animation-delay: 0.82s;
}

.complete-button:nth-child(2) {
  animation-delay: 0.9s;
}

.complete-button-area .bottom-btn.secondary-button,
.secondary-button {
  border: 1px solid #e0e0e0 !important;
  background: #ffffff !important;
  color: #111111 !important;
}

.complete-button-area .bottom-btn.secondary-button:active,
.secondary-button:active {
  background: #f8f9fa !important;
}

.complete-button-area .bottom-btn.primary-button,
.primary-button {
  border: none !important;
  background: #ffbc2e !important;
  color: #111111 !important;
}

.complete-button-area .bottom-btn.primary-button:active,
.primary-button:active {
  background: #e5a900 !important;
}

@keyframes success-pop {
  0% { transform: translate(-50%, -50%) scale(0); }
  70% { transform: translate(-50%, -50%) scale(1.08); }
  100% { transform: translate(-50%, -50%) scale(1); }
}

@keyframes check-appear {
  from { opacity: 0; transform: scale(0.5) rotate(-15deg); }
  to { opacity: 1; transform: scale(1) rotate(0); }
}

@keyframes particle-pop {
  0% { opacity: 0; transform: scale(0); }
  60% { opacity: 1; transform: scale(1.4); }
  100% { opacity: 0.7; transform: scale(1); }
}

@keyframes spark-pop {
  0% { opacity: 0; transform: scale(0) rotate(-40deg); }
  60% { opacity: 1; transform: scale(1.4) rotate(12deg); }
  100% { opacity: 0.75; transform: scale(1) rotate(0); }
}

@keyframes glow {
  0%, 100% { opacity: 0.55; transform: translate(-50%, -50%) scale(0.95); }
  50% { opacity: 1; transform: translate(-50%, -50%) scale(1.1); }
}

@keyframes content-up {
  to { opacity: 1; transform: translateY(0); }
}

@keyframes button-show {
  to { opacity: 1; transform: translateY(0); }
}

@keyframes background-float-left {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(12px, 16px); }
}

@keyframes background-float-right {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-14px, -12px); }
}
</style>
