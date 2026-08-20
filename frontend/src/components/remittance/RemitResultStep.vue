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

        <!-- 완료 메시지 (팀 공용 폰트 및 구조: text-30-bold / text-15) -->
        <div class="complete-message">
          <h1 class="text-30-bold complete-title" v-html="getCompleteTitleHtml"></h1>

          <p class="complete-description text-15" v-html="getCompleteDescHtml"></p>
        </div>

        <!-- 완료 상태 뱃지 (팀 공용 규격) -->
        <div class="complete-badge">
          <i :class="getCompleteBadgeIcon"></i>
          <span>{{ getCompleteBadgeText }}</span>
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

    <!-- 하단 2개 버튼 (팀 공용 .bottom-btn-area.double + .secondary-button / .primary-button) -->
    <div class="bottom-btn-area double complete-button-area">
      <button
        class="bottom-btn secondary-button complete-button"
        type="button"
        @click="handleSecondaryAction"
      >
        {{ getSecondaryBtnText }}
      </button>

      <button
        class="bottom-btn primary-button complete-button"
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

// 1. 완료 제목 (HTML)
const getCompleteTitleHtml = computed(() => {
  switch (props.remitType) {
    case "DUTCH_CREATE":
    case "DUTCH":
      return "더치페이 요청이<br />완료되었어요!";
    case "DUTCH_PAY":
    case "DUTCH_REMIT":
      return "더치페이 정산이<br />완료되었어요!";
    case "FRIEND":
      return "친구 송금이<br />완료되었어요!";
    case "ACCOUNT":
    default:
      return "계좌 송금이<br />완료되었어요!";
  }
});

// 2. 완료 설명 문구 (HTML)
const getCompleteDescHtml = computed(() => {
  const formatted = props.formatCurrency(props.remitAmount || 0);
  switch (props.remitType) {
    case "DUTCH_CREATE":
    case "DUTCH":
      return "요청한 멤버들에게<br />정산 알림을 보냈어요.";
    case "DUTCH_PAY":
    case "DUTCH_REMIT":
      return `${props.receiverName || '방장'}님에게<br />${formatted}원을 보냈어요.`;
    case "FRIEND":
      return `${props.receiverName || '친구'}님에게<br />${formatted}원을 보냈어요.`;
    case "ACCOUNT":
    default:
      return `${props.receiverName || '수취인'}님에게<br />${formatted}원을 보냈어요.`;
  }
});

// 3. 완료 상태 뱃지 텍스트
const getCompleteBadgeText = computed(() => {
  switch (props.remitType) {
    case "DUTCH_CREATE":
    case "DUTCH":
      return "더치페이 요청 완료";
    case "DUTCH_PAY":
    case "DUTCH_REMIT":
      return "정산 송금 완료";
    case "FRIEND":
      return "친구 송금 완료";
    case "ACCOUNT":
    default:
      return "계좌 송금 완료";
  }
});

// 4. 완료 상태 뱃지 아이콘
const getCompleteBadgeIcon = computed(() => {
  switch (props.remitType) {
    case "DUTCH_CREATE":
    case "DUTCH":
      return "fa-solid fa-users";
    case "DUTCH_PAY":
    case "DUTCH_REMIT":
      return "fa-solid fa-receipt";
    case "FRIEND":
      return "fa-solid fa-paper-plane";
    case "ACCOUNT":
    default:
      return "fa-solid fa-building-columns";
  }
});

// 5. 좌측 서브 버튼 텍스트 (보조 버튼)
const getSecondaryBtnText = computed(() => {
  if (props.remitType === "DUTCH_CREATE" || props.remitType === "DUTCH") {
    return "정산 내역";
  }
  return "피드 보기";
});

// 6. 서브 액션 동작 (피드 보기 또는 정산 내역 페이지 이동)
const handleSecondaryAction = () => {
  emit("resetAll");
  if (props.remitType === "DUTCH_CREATE" || props.remitType === "DUTCH") {
    router.push("/settlement");
  } else {
    router.push("/feed");
  }
};

// 7. 메인 확인 동작 (지갑 페이지 이동)
const handleGoHome = () => {
  emit("resetAll");
  router.push("/wallet");
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.complete-page {
  position: relative;
  overflow: hidden;
  background:
      linear-gradient(
          180deg,
          #fffdf8 0%,
          var(--color-bg-page) 42%,
          var(--color-bg-page) 100%
      );
}

/* =========================
   콘텐츠
========================= */

.complete-container {
  position: relative;
  z-index: 2;
  justify-content: center;
  overflow: hidden;
}

.complete-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: -42px;
  text-align: center;
}

/* =========================
   배경 장식
========================= */

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

/* =========================
   완료 애니메이션
========================= */

.success-visual {
  position: relative;
  width: 180px;
  height: 180px;
  margin-bottom: 22px;
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
  background:
      linear-gradient(
          145deg,
          #ffd15c,
          var(--color-primary)
      );
  box-shadow:
      0 16px 34px rgba(255, 188, 46, 0.28),
      inset 0 1px 0 rgba(255, 255, 255, 0.5);
  color: var(--color-text-white);
  font-size: 42px;
  transform: translate(-50%, -50%) scale(0);
  animation:
      success-pop
      0.55s
      cubic-bezier(0.34, 1.56, 0.64, 1)
      forwards;
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
  background: var(--color-primary);
  opacity: 0;
}

.particle-1 {
  top: 28px;
  left: 28px;
  animation: particle-pop 0.55s ease 0.35s forwards;
}

.particle-2 {
  top: 18px;
  right: 36px;
  width: 6px;
  height: 6px;
  background: #9d90ff;
  animation: particle-pop 0.55s ease 0.5s forwards;
}

.particle-3 {
  top: 82px;
  right: 8px;
  width: 10px;
  height: 10px;
  background: #7bd6c7;
  animation: particle-pop 0.55s ease 0.4s forwards;
}

.particle-4 {
  right: 30px;
  bottom: 24px;
  width: 7px;
  height: 7px;
  background: #ff9eaa;
  animation: particle-pop 0.55s ease 0.6s forwards;
}

.particle-5 {
  bottom: 26px;
  left: 30px;
  width: 6px;
  height: 6px;
  background: #9d90ff;
  animation: particle-pop 0.55s ease 0.48s forwards;
}

.particle-6 {
  top: 92px;
  left: 6px;
  width: 9px;
  height: 9px;
  background: #7bd6c7;
  animation: particle-pop 0.55s ease 0.58s forwards;
}

/* =========================
   별
========================= */

.spark {
  position: absolute;
  z-index: 1;
  color: #ffd65c;
  opacity: 0;
}

.spark-1 {
  top: 18px;
  left: 70px;
  font-size: 12px;
  animation: spark-pop 0.55s ease 0.55s forwards;
}

.spark-2 {
  right: 46px;
  bottom: 17px;
  color: #a99df7;
  font-size: 10px;
  animation: spark-pop 0.55s ease 0.7s forwards;
}

/* =========================
   텍스트
========================= */

.complete-message {
  opacity: 0;
  transform: translateY(16px);
  animation: content-up 0.5s ease 0.55s forwards;
}

.complete-message h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.3;
  letter-spacing: -0.7px;
}

.complete-description {
  margin: 16px 0 0;
  color: var(--color-text-sub);
  font-weight: 400;
  line-height: 1.65;
}

/* =========================
   완료 뱃지
========================= */

.complete-badge {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  margin-top: 26px;
  padding: 10px 15px;
  border: 1px solid rgba(255, 188, 46, 0.22);
  border-radius: 999px;
  background: rgba(255, 188, 46, 0.1);
  color: #9a6900;
  font-size: 13px;
  font-weight: 500;
  opacity: 0;
  transform: translateY(10px);
  animation: content-up 0.45s ease 0.72s forwards;
}

.complete-badge i {
  color: var(--color-primary-active);
  font-size: 12px;
}

/* =========================
   상세 내역 카드
========================= */

.complete-detail-card {
  width: 100%;
  max-width: 320px;
  margin-top: 20px;
  padding: 14px 18px;
  background-color: var(--color-bg-screen, #f8f9fa);
  border-radius: 16px;
  box-sizing: border-box;
  opacity: 0;
  transform: translateY(16px);
  animation: content-up 0.5s ease 0.8s forwards;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
}

.lbl {
  color: #777777;
}

.val {
  color: #111111;
}

/* =========================
   하단 2개 버튼
   common.css의
   .bottom-btn-area.double 사용
========================= */

.complete-button-area {
  position: relative;
  z-index: 3;
}

.complete-button {
  opacity: 0;
  transform: translateY(10px);
  animation: button-show 0.45s ease forwards;
}

/* 왼쪽 버튼 등장 */
.complete-button:nth-child(1) {
  animation-delay: 0.82s;
}

/* 오른쪽 버튼 등장 */
.complete-button:nth-child(2) {
  animation-delay: 0.9s;
}

/* 왼쪽 - 보조 버튼 */
.secondary-button {
  border: 1px solid var(--color-border-main);
  background: #ffffff;
  color: var(--color-text-main);
}

.secondary-button:active {
  background: var(--color-bg-screen);
}

/* 오른쪽 - 메인 버튼 */
.primary-button {
  border: none;
  background: var(--color-primary);
  color: var(--color-text-main);
}

.primary-button:active {
  background: var(--color-primary-active);
}

/* =========================
   애니메이션
========================= */

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
  0%,
  100% {
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
    transform: translateY(16px);
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

@keyframes background-float-left {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(12px, 10px);
  }
}

@keyframes background-float-right {
  0%,
  100% {
    transform: translate(0, 0);
  }

  50% {
    transform: translate(-10px, -8px);
  }
}

/* =========================
   모션 최소화
========================= */

@media (prefers-reduced-motion: reduce) {
  .background-decoration,
  .success-circle,
  .success-circle i,
  .success-glow,
  .particle,
  .spark,
  .complete-message,
  .complete-badge,
  .complete-button {
    opacity: 1;
    animation: none;
  }

  .background-decoration,
  .particle,
  .spark,
  .complete-message,
  .complete-badge,
  .complete-button {
    transform: none;
  }

  .success-circle {
    transform: translate(-50%, -50%);
  }

  .success-glow {
    transform: translate(-50%, -50%);
  }
}

@media (max-width: 360px) {
  .success-visual {
    width: 160px;
    height: 160px;
  }

  .success-circle {
    width: 94px;
    height: 94px;
    font-size: 38px;
  }

  .success-glow {
    width: 116px;
    height: 116px;
  }
}
</style>
