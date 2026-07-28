<template>
  <div class="modal-backdrop" @click.self="$emit('close')">
    <div class="modal-card bg-white rounded-4 shadow-lg overflow-hidden animate__animated animate__fadeInUp">
      <!-- 헤더 -->
      <div class="bg-dark text-white p-3 d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-2">
          <span class="badge bg-warning text-dark fw-bold px-2 py-1">KB Pay</span>
          <h6 class="fw-bold mb-0">1회용 보안 결제 코드</h6>
        </div>
        <button type="button" class="btn-close btn-close-white" @click="$emit('close')"></button>
      </div>

      <div class="p-4 text-center">
        <!-- 탭 버튼 (바코드 / QR코드) -->
        <div class="btn-group w-100 mb-3" role="group">
          <button
            type="button"
            class="btn py-2.5 fw-bold"
            :class="activeTab === 'barcode' ? 'btn-dark' : 'btn-outline-secondary'"
            @click="switchTab('barcode')"
          >
            <i class="bi bi-upc-scan me-1"></i> 바코드 결제
          </button>
          <button
            type="button"
            class="btn py-2.5 fw-bold"
            :class="activeTab === 'qr' ? 'btn-dark' : 'btn-outline-secondary'"
            @click="switchTab('qr')"
          >
            <i class="bi bi-qr-code-scan me-1"></i> QR코드 결제
          </button>
        </div>

        <!-- 1. 바코드 결제 화면 -->
        <div v-if="activeTab === 'barcode'" class="code-box p-3 rounded-4 bg-light mb-3">
          <div class="text-secondary small mb-2">가맹점 바코드 스캐너에 대어주세요</div>

          <div class="barcode-container my-3 d-flex justify-content-center">
            <svg class="barcode-svg" viewBox="0 0 280 90" xmlns="http://www.w3.org/2000/svg">
              <rect width="100%" height="100%" fill="#ffffff" />
              <g fill="#000000">
                <rect v-for="(bar, idx) in barcodeBars" :key="idx" :x="bar.x" y="10" :width="bar.w" height="70" />
              </g>
            </svg>
          </div>

          <div class="fs-5 fw-bold text-dark font-monospace tracking-wider">
            {{ formattedCode }}
          </div>
        </div>

        <!-- 2. QR코드 결제 화면 -->
        <div v-else class="code-box p-3 rounded-4 bg-light mb-3">
          <div class="text-secondary small mb-2">가맹점 QR 리더기에 스캔해 주세요</div>

          <div class="qr-container my-3 d-flex justify-content-center">
            <svg class="qr-svg border rounded-3 p-2 bg-white" viewBox="0 0 210 210" width="180" height="180" xmlns="http://www.w3.org/2000/svg">
              <rect width="100%" height="100%" fill="#ffffff" />
              <g fill="#000000">
                <rect v-for="(cell, idx) in qrModules" :key="idx" :x="cell.x" :y="cell.y" width="9.5" height="9.5" />
              </g>
              <rect x="80" y="80" width="50" height="50" rx="8" fill="#FFBC00" />
              <text x="105" y="110" font-size="16" font-weight="900" text-anchor="middle" fill="#000000">KB</text>
            </svg>
          </div>

          <div class="small text-muted font-monospace">토큰: {{ rawCode }}</div>
        </div>

        <!-- 3분 카운트다운 타이머 -->
        <div class="d-flex justify-content-between align-items-center bg-warning-subtle p-3 rounded-3 mb-3">
          <div class="d-flex align-items-center gap-2">
            <i class="bi bi-clock-history fs-5 text-dark"></i>
            <span class="text-dark small fw-bold">인증 유효시간</span>
          </div>
          <div class="d-flex align-items-center gap-2">
            <span class="fs-5 fw-extrabold text-danger font-monospace">{{ timerText }}</span>
            <button class="btn btn-sm btn-dark rounded-circle px-2 py-1" @click="fetchServerToken" title="토큰 재발급">
              <i class="bi bi-arrow-clockwise"></i>
            </button>
          </div>
        </div>

        <!-- 하단 안내 문구 -->
        <p class="text-muted small mb-0" style="font-size: 0.75rem;">
          <i class="bi bi-shield-check text-warning me-1"></i> 1회용 결제 코드는 3분 후 자동 소멸됩니다.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import walletApi from '@/api/walletApi';

const props = defineProps({
  userId: { type: Number, default: 1 }
});

defineEmits(['close']);

const activeTab = ref('barcode'); // 'barcode' | 'qr'
const rawCode = ref('');
const timeLeft = ref(180);

let timerInterval = null;

const formattedCode = computed(() => {
  if (!rawCode.value) return '';
  return rawCode.value.replace(/(\d{4})/g, '$1 ').trim();
});

const timerText = computed(() => {
  if (timeLeft.value <= 0) return '00:00 (만료)';
  const m = Math.floor(timeLeft.value / 60).toString().padStart(2, '0');
  const s = (timeLeft.value % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});

const fetchServerToken = async () => {
  try {
    let data;
    if (activeTab.value === 'qr') {
      data = await walletApi.getQrToken(props.userId);
    } else {
      data = await walletApi.getBarcodeToken(props.userId);
    }

    if (data && data.token) {
      rawCode.value = data.token;
      timeLeft.value = data.expiresInSeconds || 180;
    }
  } catch (e) {
    console.error('Token fetch error:', e);
  }
};

const switchTab = (tab) => {
  activeTab.value = tab;
  fetchServerToken();
};

const barcodeBars = computed(() => {
  const bars = [];
  let currentX = 15;
  const str = rawCode.value || '8804123456789012';

  bars.push({ x: currentX, w: 3 }); currentX += 5;
  bars.push({ x: currentX, w: 2 }); currentX += 4;

  for (let i = 0; i < str.length; i++) {
    const digit = str.charCodeAt(i) % 10;
    const w1 = (digit % 3) + 2;
    const w2 = ((digit + 1) % 2) + 2;

    bars.push({ x: currentX, w: w1 });
    currentX += w1 + (digit % 2) + 2;

    bars.push({ x: currentX, w: w2 });
    currentX += w2 + 3;
  }

  bars.push({ x: currentX, w: 3 }); currentX += 5;
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
          modules.push({ x: (startC + c) * cellSize, y: (startR + r) * cellSize });
        }
      }
    }
  };

  addSquare(0, 0);
  addSquare(0, size - 7);
  addSquare(size - 7, 0);

  const seed = rawCode.value || 'KBQR880412345678';
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

onMounted(() => {
  fetchServerToken();
  startTimer();
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
});
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1060;
  padding: 16px;
}
.modal-card {
  width: 100%;
  max-width: 400px;
}
.barcode-svg {
  width: 100%;
  max-width: 280px;
  height: 90px;
}
.tracking-wider {
  letter-spacing: 2px;
}
</style>
