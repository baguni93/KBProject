<template>
  <div>
    <!-- PIN 인증 모달 -->
    <PinAuthModal
      :show="!isPinVerified"
      :userId="userId"
      @close="$emit('close')"
      @success="handlePinSuccess"
    />

    <!-- 결제 코드 바텀 시트 모달 -->
    <div
      v-if="isPinVerified"
      class="bottom-sheet-backdrop"
      @click.self="$emit('close')"
    >
      <div class="bottom-sheet-content">
        <!-- 드래그 핸들 바 -->
        <div class="sheet-handle-bar" @click="$emit('close')"></div>

        <!-- 헤더 -->
        <div class="modal-header">
          <div class="header-title-flex">
            <span class="kb-badge text-13-bold">KB Pay</span>
            <h5 class="text-18-bold modal-title">1회용 보안 결제 코드</h5>
          </div>
          <button type="button" class="close-btn" @click="$emit('close')">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>

        <div class="modal-body-padding">
          <!-- 탭 버튼 -->
          <div class="tab-group-flex">
            <button
              type="button"
              class="content-btn text-15-bold flex-1"
              :class="activeTab === 'barcode' ? 'primary' : 'secondary'"
              @click="switchTab('barcode')"
            >
              <i class="fa-solid fa-barcode mr-1"></i> 바코드 결제
            </button>
            <button
              type="button"
              class="content-btn text-15-bold flex-1"
              :class="activeTab === 'qr' ? 'primary' : 'secondary'"
              @click="switchTab('qr')"
            >
              <i class="fa-solid fa-qrcode mr-1"></i> QR코드 결제
            </button>
          </div>

          <!-- 1. 바코드 결제 화면 -->
          <div v-if="activeTab === 'barcode'" class="code-box">
            <div class="code-box-desc text-13">
              가맹점 바코드 스캐너에 대어주세요
            </div>

            <div class="barcode-container">
              <svg
                class="barcode-svg"
                viewBox="0 0 280 90"
                xmlns="http://www.w3.org/2000/svg"
              >
                <rect width="100%" height="100%" fill="#ffffff" />
                <g fill="#000000">
                  <rect
                    v-for="(bar, idx) in barcodeBars"
                    :key="idx"
                    :x="bar.x"
                    y="10"
                    :width="bar.w"
                    height="70"
                  />
                </g>
              </svg>
            </div>

            <div class="formatted-code text-20-bold">
              {{ formattedCode }}
            </div>
          </div>

          <!-- 2. QR코드 결제 화면 -->
          <div v-else class="code-box">
            <div class="code-box-desc text-13">
              가맹점 QR 리더기에 스캔해 주세요
            </div>

            <div class="qr-container">
              <svg
                class="qr-svg"
                viewBox="0 0 210 210"
                width="180"
                height="180"
                xmlns="http://www.w3.org/2000/svg"
              >
                <rect width="100%" height="100%" fill="#ffffff" />
                <g fill="#000000">
                  <rect
                    v-for="(cell, idx) in qrModules"
                    :key="idx"
                    :x="cell.x"
                    :y="cell.y"
                    width="9.5"
                    height="9.5"
                  />
                </g>
                <rect
                  x="80"
                  y="80"
                  width="50"
                  height="50"
                  rx="8"
                  fill="#FFBC00"
                />
                <text
                  x="105"
                  y="110"
                  font-size="16"
                  font-weight="900"
                  text-anchor="middle"
                  fill="#000000"
                >
                  KB
                </text>
              </svg>
            </div>

            <div class="raw-token-text text-13">토큰: {{ rawCode }}</div>
          </div>

          <!-- 타이머 카드 -->
          <div class="timer-card flex-between">
            <div class="timer-left">
              <i class="fa-solid fa-clock-rotate-left brand-ic text-15"></i>
              <span class="text-13-bold">인증 유효시간</span>
            </div>
            <div class="timer-right">
              <span class="text-18-bold text-danger font-mono">{{
                timerText
              }}</span>
              <button
                type="button"
                class="refresh-btn"
                @click="fetchServerToken"
                title="토큰 재발급"
              >
                <i class="fa-solid fa-rotate-right"></i>
              </button>
            </div>
          </div>

          <!-- 하단 안내 문구 & 결제 승인 시뮬레이션 버튼 -->
          <p class="notice-text text-13" style="margin-bottom: 12px;">
            <i class="fa-solid fa-shield-halved brand-ic"></i> 1회용 결제 코드는
            1분 후 자동 소멸됩니다.
          </p>

          <button
            type="button"
            class="bottom-btn text-15-bold"
            style="width: 100%; background: var(--color-primary, #ffbc00); color: #111; border: none; padding: 12px; border-radius: 12px;"
            :disabled="isApproving"
            @click="handleApprovePayment"
          >
            <i class="fa-solid fa-qrcode mr-1"></i>
            {{ isApproving ? "결제 승인 처리 중..." : "가맹점 현장 결제 시뮬레이션 (승인)" }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onUnmounted } from "vue";
import walletApi from "@/api/walletApi";
import cardPaymentApi from "@/api/cardPaymentApi";
import PinAuthModal from "@/components/auth/PinAuthModal.vue";

const props = defineProps({
  userId: { type: Number, default: 1 },
  initialTab: { type: String, default: "barcode" },
});

const emit = defineEmits(["close", "approved"]);

const isPinVerified = ref(false);
const activeTab = ref(props.initialTab || "barcode");
const rawCode = ref("");
const timeLeft = ref(60);
const isApproving = ref(false);

const handleApprovePayment = async () => {
  if (isApproving.value) return;
  isApproving.value = true;
  try {
    const payload = {
      userId: props.userId || 1,
      merchantName: activeTab.value === "qr" ? "스타벅스 (QR 결제)" : "CU 편의점 (바코드 결제)",
      amount: 4500,
    };
    const res = await cardPaymentApi.approveWalletTransaction(payload);
    alert(`[결제 승인 완료] ${payload.merchantName}\n결제 금액: 4,500원\n남은 잔액: ${(res.updatedWalletBalance || 0).toLocaleString()}원`);
    emit("approved", res.updatedWalletBalance);
    emit("close");
  } catch (err) {
    console.error("결제 승인 오류:", err);
    alert("결제 승인 처리 중 오류가 발생했습니다.");
  } finally {
    isApproving.value = false;
  }
};

let timerInterval = null;

const handlePinSuccess = () => {
  isPinVerified.value = true;
  activeTab.value = props.initialTab || "barcode";
  fetchServerToken();
  startTimer();
};

const formattedCode = computed(() => {
  if (!rawCode.value) return "";
  return rawCode.value.replace(/(\d{4})/g, "$1 ").trim();
});

const timerText = computed(() => {
  if (timeLeft.value <= 0) return "00:00 (만료)";
  const m = Math.floor(timeLeft.value / 60)
    .toString()
    .padStart(2, "0");
  const s = (timeLeft.value % 60).toString().padStart(2, "0");
  return `${m}:${s}`;
});

const fetchServerToken = async () => {
  try {
    let data;
    if (activeTab.value === "qr") {
      data = await walletApi.getQrToken(props.userId);
    } else {
      data = await walletApi.getBarcodeToken(props.userId);
    }

    if (data && data.token) {
      rawCode.value = data.token;
      timeLeft.value = data.expiresInSeconds || 60;
    }
  } catch (e) {
    console.error("Token fetch error:", e);
  }
};

const switchTab = (tab) => {
  activeTab.value = tab;
  fetchServerToken();
};

const barcodeBars = computed(() => {
  const bars = [];
  let currentX = 15;
  const str = rawCode.value || "8804123456789012";

  bars.push({ x: currentX, w: 3 });
  currentX += 5;
  bars.push({ x: currentX, w: 2 });
  currentX += 4;

  for (let i = 0; i < str.length; i++) {
    const digit = str.charCodeAt(i) % 10;
    const w1 = (digit % 3) + 2;
    const w2 = ((digit + 1) % 2) + 2;

    bars.push({ x: currentX, w: w1 });
    currentX += w1 + (digit % 2) + 2;

    bars.push({ x: currentX, w: w2 });
    currentX += w2 + 3;
  }

  bars.push({ x: currentX, w: 3 });
  currentX += 5;
  bars.push({ x: currentX, w: 2 });

  return bars;
});

const qrModules = computed(() => {
  const modules = [];
  const size = 21;
  const cellSize = 10;

  const isFinder = (r, c) => {
    if (r < 7 && c < 7) return true;
    if (r < 7 && c >= size - 7) return true;
    if (r >= size - 7 && c < 7) return true;
    return false;
  };

  const isCenter = (r, c) => {
    return r >= 8 && r <= 12 && c >= 8 && c <= 12;
  };

  const addSquare = (startR, startC) => {
    for (let r = 0; r < 7; r++) {
      for (let c = 0; c < 7; c++) {
        const isBorder = r === 0 || r === 6 || c === 0 || c === 6;
        const isInner = r >= 2 && r <= 4 && c >= 2 && c <= 4;
        if (isBorder || isInner) {
          modules.push({
            x: (startC + c) * cellSize,
            y: (startR + r) * cellSize,
          });
        }
      }
    }
  };

  addSquare(0, 0);
  addSquare(0, size - 7);
  addSquare(size - 7, 0);

  const seed = rawCode.value || "KBQR880412345678";
  for (let r = 0; r < size; r++) {
    for (let c = 0; c < size; c++) {
      if (isFinder(r, c) || isCenter(r, c)) continue;
      const charCode = seed.charCodeAt((r * size + c) % seed.length);
      if ((r + c + charCode) % 3 === 0) {
        modules.push({ x: c * cellSize, y: r * cellSize });
      }
    }
  }

  return modules;
});

const startTimer = () => {
  if (timerInterval) clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value--;
    } else {
      clearInterval(timerInterval);
    }
  }, 1000);
};

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});
</script>

<style scoped>
/* 백드롭 및 바텀 시트 구조 */
.bottom-sheet-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(17, 17, 17, 0.65);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1060;
}

.bottom-sheet-content {
  width: 100%;
  max-width: 480px;
  background-color: var(--color-bg-page, #ffffff);
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  padding-bottom: 24px;
  box-shadow: 0 -10px 30px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.sheet-handle-bar {
  width: 40px;
  height: 5px;
  background-color: var(--color-border-main, #dddddd);
  border-radius: 999px;
  margin: 10px auto 4px auto;
  cursor: pointer;
}

/* 모달 헤더 */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid var(--color-divider, #ededed);
}

.header-title-flex {
  display: flex;
  align-items: center;
  gap: 8px;
}

.kb-badge {
  background-color: var(--color-text-main, #111111);
  color: var(--color-primary, #ffbc2e);
  padding: 3px 8px;
  border-radius: 999px;
}

.modal-title {
  color: var(--color-text-main, #111111);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: var(--color-text-sub, #777777);
  font-size: 16px;
  cursor: pointer;
}

.modal-body-padding {
  padding: 20px;
  text-align: center;
}

/* 탭 그룹 */
.tab-group-flex {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.flex-1 {
  flex: 1;
}

.mr-1 {
  margin-right: 4px;
}

/* 코드 박스 */
.code-box {
  background-color: var(--color-bg-screen, #f5f6f8);
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 16px;
}

.code-box-desc {
  color: var(--color-text-sub, #777777);
  margin-bottom: 12px;
}

.barcode-container {
  margin: 12px 0;
  display: flex;
  justify-content: center;
}

.barcode-svg {
  width: 100%;
  max-width: 280px;
  height: 80px;
}

.formatted-code {
  color: var(--color-text-main, #111111);
  letter-spacing: 2px;
  font-family: monospace;
}

.qr-container {
  margin: 12px 0;
  display: flex;
  justify-content: center;
}

.qr-svg {
  border: 1px solid var(--color-border-main, #dddddd);
  border-radius: 12px;
  padding: 8px;
  background-color: #ffffff;
}

.raw-token-text {
  color: var(--color-text-sub, #777777);
  font-family: monospace;
}

/* 타이머 카드 */
.timer-card {
  background-color: #fffbe6;
  border: 1px solid var(--color-primary-border, #cc9200);
  border-radius: 10px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timer-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-main, #111111);
}

.brand-ic {
  color: var(--color-primary-border, #cc9200);
}

.timer-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.text-danger {
  color: var(--color-error, #e53935);
}

.font-mono {
  font-family: monospace;
}

.refresh-btn {
  background-color: var(--color-text-main, #111111);
  color: #ffffff;
  border: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  cursor: pointer;
}

/* 콘텐츠 버튼 가이드라인 스타일 */
.content-btn {
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid transparent;
  white-space: nowrap;
}

.content-btn.primary {
  background-color: var(--color-primary, #ffbc2e);
  border-color: var(--color-primary-border, #cc9200);
  color: var(--color-text-main, #111111);
}

.content-btn.secondary {
  background-color: var(--color-bg-page, #ffffff);
  border-color: var(--color-border-main, #dddddd);
  color: var(--color-text-sub, #777777);
}

.notice-text {
  color: var(--color-text-sub, #777777);
  margin: 0;
}
</style>
