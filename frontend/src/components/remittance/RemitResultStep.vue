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
          <h1 class="text-24-bold complete-title">
            {{ getCompleteTitle }}
          </h1>

          <p class="complete-description text-20-bold" style="margin-top: 8px; color: #111111;">
            {{ getFormattedAmountText }}
          </p>
        </div>

        <!-- 상세 내역 요약 카드 -->
        <div class="complete-detail-card">
          <!-- 1. 더치페이 요청 생성 -->
          <template v-if="remitType === 'DUTCH_CREATE' || remitType === 'DUTCH'">
            <div v-if="dutchRoomTitle" class="detail-row text-14">
              <span class="lbl text-14">정산 모임</span>
              <span class="val text-15-bold">{{ dutchRoomTitle }}</span>
            </div>
            <div class="detail-row text-14">
              <span class="lbl text-14">정산 멤버</span>
              <span class="val text-15-bold">
                {{ selectedDutchFriends && selectedDutchFriends.length > 1 ? `${getFriendName(selectedDutchFriends[0])} 외 ${selectedDutchFriends.length - 1}명` : (selectedDutchFriends && selectedDutchFriends.length === 1 ? getFriendName(selectedDutchFriends[0]) : '참여자') }}
              </span>
            </div>
            <div class="detail-row text-14">
              <span class="lbl text-14">1인당 요청 금액</span>
              <span class="val text-15-bold">
                {{ formatCurrency(Math.floor((remitAmount || 0) / ((selectedDutchFriends?.length || 1) + 1))) }} 원
              </span>
            </div>
          </template>

          <!-- 2. 더치페이 지불/송금 -->
          <template v-else-if="remitType === 'DUTCH_PAY' || remitType === 'DUTCH_REMIT'">
            <div class="detail-row text-14">
              <span class="lbl text-14">정산 요청자</span>
              <span class="val text-15-bold">{{ receiverName || '방장' }}</span>
            </div>
            <div v-if="dutchRoomTitle" class="detail-row text-14">
              <span class="lbl text-14">정산 모임</span>
              <span class="val text-15-bold">{{ dutchRoomTitle }}</span>
            </div>
          </template>

          <!-- 3. 친구 송금 -->
          <template v-else-if="remitType === 'FRIEND'">
            <div class="detail-row text-14">
              <span class="lbl text-14">받는 친구</span>
              <span class="val text-15-bold">{{ receiverName || '친구' }}</span>
            </div>
            <div v-if="remitMemo" class="detail-row text-14">
              <span class="lbl text-14">송금 메모</span>
              <span class="val text-15-bold">{{ remitMemo }}</span>
            </div>
          </template>

          <!-- 4. 계좌 송금 (기본) -->
          <template v-else>
            <div class="detail-row text-14">
              <span class="lbl text-14">받는 사람</span>
              <span class="val text-15-bold">{{ receiverName || '수취인' }}</span>
            </div>
            <div v-if="accountNumber" class="detail-row text-14">
              <span class="lbl text-14">입금 계좌</span>
              <span class="val text-15-bold">
                {{ bankName }} {{ accountNumber }}
              </span>
            </div>
          </template>
        </div>
      </section>
    </main>

    <!-- 하단 2개 버튼: 좌측(흰색 배경 - 피드 보기) / 우측(노란색 배경 - 확인) -->
    <div class="complete-button-area">
      <!-- 서브 기능 버튼 (순수 흰색 배경 + 테두리) -->
      <button
        class="complete-btn-secondary text-16-bold"
        type="button"
        @click="handleSecondaryAction"
      >
        {{ getSecondaryBtnText }}
      </button>

      <!-- 주 확인 버튼 (노란색 배경) -->
      <button
        class="complete-btn-primary text-16-bold"
        type="button"
        @click="handleGoHome"
      >
        확인
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
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
  dutchRoomTitle: {
    type: String,
    default: "",
  },
  remitMemo: {
    type: String,
    default: "",
  },
  formatCurrency: {
    type: Function,
    default: (val) => (val === undefined || val === null ? "0" : Number(val).toLocaleString("ko-KR")),
  },
});

const emit = defineEmits(["resetAll"]);

// 1. 완료 제목
const getCompleteTitle = computed(() => {
  switch (props.remitType) {
    case "DUTCH_CREATE":
    case "DUTCH":
      return "더치페이 요청이 완료되었어요!";
    case "DUTCH_PAY":
    case "DUTCH_REMIT":
      return "정산 지불이 완료되었어요!";
    case "FRIEND":
      return "친구 송금이 완료되었어요!";
    case "ACCOUNT":
    default:
      return "계좌 송금이 완료되었어요!";
  }
});

// 2. 금액 표시 텍스트 (+/-)
const getFormattedAmountText = computed(() => {
  const formatted = props.formatCurrency(props.remitAmount || 0);
  if (props.remitType === "DUTCH_CREATE" || props.remitType === "DUTCH") {
    return `총 ${formatted} 원`;
  }
  return `-${formatted} 원`;
});

// 3. 좌측 서브 버튼 텍스트 (흰색 버튼)
const getSecondaryBtnText = computed(() => {
  if (props.remitType === "DUTCH_CREATE" || props.remitType === "DUTCH") {
    return "정산 내역";
  }
  return "피드 보기";
});

// 4. 서브 액션 동작 (피드 보기 또는 정산 내역 페이지 이동)
const handleSecondaryAction = () => {
  emit("resetAll");
  if (props.remitType === "DUTCH_CREATE" || props.remitType === "DUTCH") {
    router.push("/settlement");
  } else {
    router.push("/feed");
  }
};

// 5. 메인 확인 동작 (지갑 페이지 이동) -> 노란색 버튼
const handleGoHome = () => {
  emit("resetAll");
  router.push("/wallet");
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.complete-page {
  position: relative;
  height: 100%;
  width: 100%;
  background-color: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
  overflow: hidden;
}

.background-decoration {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.decoration-left {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(255, 243, 199, 0.6) 0%, rgba(255, 255, 255, 0) 70%);
  top: -40px;
  left: -40px;
  animation: background-float-left 6s ease-in-out infinite;
}

.decoration-right {
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(243, 235, 255, 0.5) 0%, rgba(255, 255, 255, 0) 70%);
  bottom: 80px;
  right: -60px;
  animation: background-float-right 7s ease-in-out infinite;
}

.complete-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 20px;
  overflow-y: auto;
  box-sizing: border-box;
}

.complete-content {
  width: 100%;
  max-width: 340px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

/* 폭죽 / 완료 비주얼 (모션 애니메이션 완전 복원) */
.success-visual {
  position: relative;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.success-glow {
  position: absolute;
  inset: -10px;
  background: radial-gradient(circle, rgba(255, 188, 46, 0.3) 0%, rgba(255, 188, 46, 0) 70%);
  border-radius: 50%;
  animation: glow 2.2s ease-in-out 0.8s infinite;
}

.success-circle {
  position: absolute;
  z-index: 2;
  top: 50%;
  left: 50%;
  display: flex;
  width: 72px;
  height: 72px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(145deg, #ffd15c, #ffbc2e);
  box-shadow: 0 10px 24px rgba(255, 188, 46, 0.28);
  color: #ffffff;
  font-size: 32px;
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
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ffbc2e;
  opacity: 0;
}

.particle-1 { top: 12px; left: 12px; animation: particle-pop 0.55s ease 0.35s forwards; }
.particle-2 { top: 6px; right: 14px; background: #9d90ff; animation: particle-pop 0.55s ease 0.5s forwards; }
.particle-3 { top: 40px; right: 0px; background: #7bd6c7; animation: particle-pop 0.55s ease 0.4s forwards; }
.particle-4 { right: 12px; bottom: 10px; background: #ff9eaa; animation: particle-pop 0.55s ease 0.6s forwards; }
.particle-5 { bottom: 10px; left: 12px; background: #9d90ff; animation: particle-pop 0.55s ease 0.48s forwards; }
.particle-6 { top: 44px; left: 0px; background: #7bd6c7; animation: particle-pop 0.55s ease 0.58s forwards; }

.spark {
  position: absolute;
  z-index: 1;
  color: #ffd65c;
  opacity: 0;
}

.spark-1 { top: 4px; left: 34px; font-size: 10px; animation: spark-pop 0.55s ease 0.55s forwards; }
.spark-2 { right: 20px; bottom: 8px; color: #a99df7; font-size: 10px; animation: spark-pop 0.55s ease 0.7s forwards; }

.complete-message {
  width: 100%;
  opacity: 0;
  transform: translateY(16px);
  animation: content-up 0.5s ease 0.55s forwards;
}

.complete-message h1 {
  margin: 0;
  color: var(--color-text-main, #111111);
  line-height: 1.3;
}

.complete-detail-card {
  width: 100%;
  margin-top: 16px;
  padding: 16px;
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

/* 하단 버튼 영역 (상단 분리선 제거 + 좌측 흰색, 우측 노란색 100% 보장) */
.complete-button-area {
  flex-shrink: 0;
  padding: 16px 20px 24px;
  background-color: transparent;
  border-top: none !important;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  width: 100%;
  box-sizing: border-box;
  opacity: 0;
  transform: translateY(10px);
  animation: button-show 0.45s ease 0.8s forwards;
}

.complete-btn-secondary {
  height: 48px;
  border-radius: 12px;
  background-color: #ffffff !important;
  border: 1px solid #cbd5e0 !important;
  color: #111111 !important;
  cursor: pointer;
  transition: all 0.2s ease;
}

.complete-btn-secondary:hover {
  background-color: #f8f9fa !important;
}

.complete-btn-primary {
  height: 48px;
  border-radius: 12px;
  background-color: #ffbc2e !important;
  border: none !important;
  color: #111111 !important;
  cursor: pointer;
  transition: all 0.2s ease;
}

.complete-btn-primary:hover {
  background-color: #e5a900 !important;
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
