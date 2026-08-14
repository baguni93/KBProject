<template>
  <div class="complete-step-wrap">
    <!-- 완료 애니메이션 세션 -->
    <div class="success-visual">
      <!-- 팡팡 터지는 파티클들 -->
      <span class="particle particle-1"></span>
      <span class="particle particle-2"></span>
      <span class="particle particle-3"></span>
      <span class="particle particle-4"></span>
      <span class="particle particle-5"></span>
      <span class="particle particle-6"></span>

      <!-- 반짝이는 별 장식 -->
      <span class="spark spark-1">
        <i class="fa-solid fa-star"></i>
      </span>
      <span class="spark spark-2">
        <i class="fa-solid fa-star"></i>
      </span>

      <!-- 체크 뒤 후광 glow 효과 -->
      <div class="success-glow"></div>

      <!-- 메인 체크 서클 -->
      <div class="success-circle">
        <i class="fa-solid fa-check"></i>
      </div>
    </div>

    <!-- 완료 메시지 -->
    <div class="complete-message">
      <h1 class="text-26-bold m-0">
        {{
          remitType === "DUTCH"
            ? "정산 요청이 완료되었어요!"
            : "송금이 완료되었어요!"
        }}
      </h1>

      <p class="text-28-bold success-amt">
        {{ formatCurrency(remitAmount) }} <span class="unit text-20-bold">원</span>
      </p>
    </div>

    <!-- 상세 내역 요약 카드 -->
    <div class="complete-detail-card">
      <div class="detail-row text-14">
        <span class="lbl text-14">받는 사람</span>
        <span class="val text-15-bold">{{ receiverName }}</span>
      </div>
      <div v-if="remitType !== 'DUTCH' && accountNumber" class="detail-row text-14">
        <span class="lbl text-14">입금 계좌</span>
        <span class="val text-15-bold"
          >{{ bankName }} {{ accountNumber }}</span
        >
      </div>
    </div>

    <!-- 완료 뱃지 -->
    <div class="complete-badge">
      <i class="fa-solid fa-paper-plane"></i>
      <span>{{ remitType === "DUTCH" ? "정산 방 개설 완료" : "실시간 이체 완료" }}</span>
    </div>

    <!-- 하단 2개 이동 버튼 (결제 화면 / 피드 구경하기) -->
    <div class="complete-btn-group">
      <button
        class="bottom-btn primary-btn text-16-bold"
        @click="handleGoWallet"
      >
        <i class="fa-solid fa-wallet"></i> 결제 화면으로
      </button>
      <button
        class="bottom-btn secondary-btn text-16-bold"
        @click="handleGoFeed"
      >
        <i class="fa-solid fa-compass"></i> 피드 구경하기
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

defineProps({
  remitType: {
    type: String,
    default: "ACCOUNT",
  },
  remitAmount: {
    type: Number,
    default: 0,
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

// 2. 피드 구경하기로 이동
const handleGoFeed = () => {
  emit("resetAll");
  router.push("/feed");
};
</script>

<style scoped>

.complete-step-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 30px 16px 20px;
  position: relative;
  overflow: hidden;
}

/* =========================
   완료 비주얼 애니메이션
========================= */

.success-visual {
  position: relative;
  width: 160px;
  height: 160px;
  margin-bottom: 16px;
}

.success-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 110px;
  height: 110px;
  border-radius: 50%;
  background: rgba(255, 188, 46, 0.2);
  transform: translate(-50%, -50%);
  animation: glow 2.2s ease-in-out 0.8s infinite;
}

.success-circle {
  position: absolute;
  z-index: 2;
  top: 50%;
  left: 50%;
  display: flex;
  width: 90px;
  height: 90px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(145deg, #ffd15c, #ffbc00);
  box-shadow:
    0 14px 30px rgba(255, 188, 46, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  color: #ffffff;
  font-size: 38px;
  transform: translate(-50%, -50%) scale(0);
  animation: success-pop 0.55s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

.success-circle i {
  opacity: 0;
  transform: scale(0.5) rotate(-15deg);
  animation: check-appear 0.35s ease 0.42s forwards;
}

/* =========================
   파티클
========================= */

.particle {
  position: absolute;
  z-index: 1;
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ffbc00;
  opacity: 0;
}

.particle-1 {
  top: 20px;
  left: 24px;
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

/* =========================
   별 장식
========================= */

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

/* =========================
   완료 텍스트
========================= */

.complete-message {
  opacity: 0;
  transform: translateY(14px);
  animation: content-up 0.5s ease 0.45s forwards;
}

.complete-message h1 {
  color: #111111;
  letter-spacing: -0.5px;
}

.success-amt {
  color: #111111;
  margin: 8px 0 16px;
  letter-spacing: -0.5px;
}

.success-amt .unit {
  color: #666666;
  font-weight: 700;
}

/* =========================
   상세 카드
========================= */

.complete-detail-card {
  width: 100%;
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 18px;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  opacity: 0;
  transform: translateY(12px);
  animation: content-up 0.5s ease 0.6s forwards;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.lbl {
  color: #777777;
}

.val {
  color: #111111;
}

/* =========================
   완료 뱃지
========================= */

.complete-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  margin-top: 18px;
  padding: 8px 16px;
  border: 1px solid rgba(255, 188, 46, 0.3);
  border-radius: 999px;
  background: rgba(255, 188, 46, 0.12);
  color: #9a6900;
  font-size: 13px;
  font-weight: 600;
  opacity: 0;
  transform: translateY(10px);
  animation: content-up 0.45s ease 0.7s forwards;
}

.complete-badge i {
  color: #ffbc00;
  font-size: 13px;
}

/* =========================
   하단 2개 버튼 그룹
========================= */

.complete-btn-group {
  width: 100%;
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bottom-btn {
  width: 100%;
  height: 50px;
  border-radius: 25px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
  opacity: 0;
  transform: translateY(10px);
}

.primary-btn {
  border: none;
  background-color: #ffbc00;
  color: #111111;
  box-shadow: 0 4px 12px rgba(255, 188, 46, 0.25);
  animation: button-show 0.45s ease 0.75s forwards;
}

.primary-btn:active {
  transform: scale(0.98);
  background-color: #e5a900;
}

.secondary-btn {
  border: 1px solid #e9ecef;
  background-color: #f8f9fa;
  color: #495057;
  animation: button-show 0.45s ease 0.85s forwards;
}

.secondary-btn:active {
  transform: scale(0.98);
  background-color: #e9ecef;
}

/* Keyframes */
@keyframes success-pop {
  0% {
    transform: translate(-50%, -50%) scale(0);
  }
  70% {
    transform: translate(-50%, -50%) scale(1.08);
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
  }
}

@keyframes check-appear {
  from {
    opacity: 0;
    transform: scale(0.5) rotate(-15deg);
  }
  to {
    opacity: 1;
    transform: scale(1) rotate(0);
  }
}

@keyframes particle-pop {
  0% {
    opacity: 0;
    transform: scale(0);
  }
  60% {
    opacity: 1;
    transform: scale(1.4);
  }
  100% {
    opacity: 0.7;
    transform: scale(1);
  }
}

@keyframes spark-pop {
  0% {
    opacity: 0;
    transform: scale(0) rotate(-40deg);
  }
  60% {
    opacity: 1;
    transform: scale(1.4) rotate(12deg);
  }
  100% {
    opacity: 0.75;
    transform: scale(1) rotate(0);
  }
}

@keyframes glow {
  0%, 100% {
    opacity: 0.55;
    transform: translate(-50%, -50%) scale(0.95);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

@keyframes content-up {
  from {
    opacity: 0;
    transform: translateY(14px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes button-show {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

