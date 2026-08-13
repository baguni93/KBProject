<template>
  <div v-if="show" class="pin-bottom-sheet-backdrop d-flex align-items-flex-end justify-content-center" @click.self="closeModal">
    <div class="pin-sheet-card rounded-t-4 p-4 text-center bg-white shadow-2xl animate-slide-up">
      <!-- 드래그 핸들 바 -->
      <div class="sheet-handle-bar my-1 mx-auto rounded-pill" @click="closeModal"></div>

      <!-- 헤더 -->
      <div class="d-flex justify-content-between align-items-center mb-2 px-1">
        <div class="d-flex align-items-center gap-2">
          <span class="badge bg-dark text-warning font-outfit px-2.5 py-1 fw-bold rounded-pill">KB Pay</span>
          <span class="fw-extrabold text-dark fs-6 font-outfit">보안 인증</span>
        </div>
        <button type="button" class="btn-close shadow-none" @click="closeModal"></button>
      </div>

      <!-- 아이콘 & 타이틀 -->
      <div class="py-1">
        <div class="icon-box bg-warning text-dark mx-auto rounded-circle mb-2 d-flex align-items-center justify-content-center shadow-md" style="width: 52px; height: 52px;">
          <i class="bi bi-shield-lock-fill fs-3"></i>
        </div>

        <h5 class="fw-extrabold text-dark mb-1 font-outfit">PIN 비밀번호 6자리</h5>
        <p class="text-secondary small mb-3 font-outfit">간편비밀번호 6자리를 입력해 주세요.</p>

        <!-- 6자리 핀 도트 -->
        <div class="d-flex justify-content-center gap-3 mb-3">
          <div
            v-for="i in 6"
            :key="i"
            :class="['pin-dot', pin.length >= i ? 'active' : '']"
          ></div>
        </div>

        <!-- 에러 메시지 -->
        <div v-if="errorMessage" class="alert alert-danger py-2 small mb-3 border-0 rounded-3 fw-bold">
          {{ errorMessage }}
        </div>

        <!-- 3x4 숫자 키패드 -->
        <div class="row g-2 px-1">
          <div v-for="num in [1, 2, 3, 4, 5, 6, 7, 8, 9]" :key="num" class="col-4">
            <button
              type="button"
              class="btn key-btn w-100 py-3 rounded-3"
              @click.prevent="appendNum(num)"
              :disabled="loading"
            >
              {{ num }}
            </button>
          </div>
          <div class="col-4">
            <button
              type="button"
              class="btn key-btn-action w-100 py-3 text-secondary font-outfit"
              @click.prevent="clearPin"
              :disabled="loading"
            >
              전체취소
            </button>
          </div>
          <div class="col-4">
            <button
              type="button"
              class="btn key-btn w-100 py-3 rounded-3"
              @click.prevent="appendNum(0)"
              :disabled="loading"
            >
              0
            </button>
          </div>
          <div class="col-4">
            <button
              type="button"
              class="btn key-btn-action w-100 py-3 text-dark"
              @click.prevent="backspace"
              :disabled="loading"
            >
              <i class="bi bi-backspace-fill fs-5"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import authApi from '@/api/authApi';

const props = defineProps({
  show: Boolean,
  userId: {
    type: Number,
    default: 1,
  },
});

const emit = defineEmits(['close', 'success']);

const pin = ref('');
const errorMessage = ref('');
const loading = ref(false);

const appendNum = (n) => {
  if (pin.value.length < 6) {
    pin.value = pin.value + String(n);
  }
};

const backspace = () => {
  if (pin.value.length > 0) {
    pin.value = pin.value.slice(0, -1);
  }
};

const clearPin = () => {
  pin.value = '';
  errorMessage.value = '';
};

const closeModal = () => {
  clearPin();
  emit('close');
};

const verifyPin = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const res = await authApi.verifyPin(pin.value);
    if (res && res.success) {
      clearPin();
      emit('success');
    } else {
      errorMessage.value = res.message || '비밀번호가 일치하지 않습니다.';
      pin.value = '';
    }
  } catch (err) {
    console.error('PIN verification error:', err);
    errorMessage.value = err.response?.data?.message || 'PIN 번호가 바르지 않습니다. (기본: 123456)';
    pin.value = '';
  } finally {
    loading.value = false;
  }
};

watch(pin, (newVal) => {
  if (newVal.length === 6) {
    verifyPin();
  }
});
</script>

<style scoped>
.pin-bottom-sheet-backdrop {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  background: rgba(15, 23, 42, 0.65) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  z-index: 1070 !important;
  display: flex !important;
  align-items: flex-end !important;
  justify-content: center !important;
}

.pin-sheet-card {
  width: 100%;
  max-width: 480px;
  background-color: #ffffff !important;
  border-top-left-radius: 28px !important;
  border-top-right-radius: 28px !important;
  padding-bottom: 24px;
}

.sheet-handle-bar {
  width: 40px;
  height: 5px;
  background: #CBD5E1;
  cursor: pointer;
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

.animate-slide-up {
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

.pin-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: #f1f5f9;
  border: 2px solid #cbd5e1;
  transition: all 0.2s ease;
}

.pin-dot.active {
  background-color: #ffbc00;
  border-color: #d97706;
  transform: scale(1.2);
  box-shadow: 0 0 8px rgba(255, 188, 0, 0.7);
}

.key-btn {
  background-color: #f8fafc !important;
  color: #0f172a !important;
  font-weight: 800 !important;
  font-size: 1.3rem !important;
  border: 1px solid #e2e8f0 !important;
  transition: all 0.15s ease;
}

.key-btn:active {
  background-color: #ffbc00 !important;
  border-color: #d97706 !important;
  color: #000000 !important;
  transform: scale(0.95);
}

.key-btn-action {
  font-weight: 700;
  font-size: 0.9rem;
}
</style>
