<template>
  <div class="page-layout pin-page">
    <!-- 공통 상단 헤더 -->
    <PageHeader
        custom-back
        @back="goBack"
    />

    <!-- 콘텐츠 -->
    <main class="page-content pin-content">
      <!-- 진행 단계 -->
      <div class="step-area">
        <span class="step"></span>
        <span class="step-line"></span>
        <span class="step active"></span>
      </div>

      <!-- 제목 -->
      <header class="pin-header">
        <h1 class="text-30-bold">
          새 간편비밀번호 확인
        </h1>

        <p class="text-15">
          설정한 PIN을 한 번 더 입력해 주세요.
        </p>
      </header>

      <!-- PIN 입력 -->
      <section class="pin-section">
        <div
            :class="{ error: !!errorMessage }"
            class="pin-boxes"
            role="button"
            tabindex="0"
            @click="focusPinInput"
            @keydown.enter="focusPinInput"
        >
          <div
              v-for="index in 6"
              :key="index"
              :class="{
                filled: confirmPinPassword.length >= index,
                active: confirmPinPassword.length === index - 1 && !errorMessage,
              }"
              class="pin-box"
          >
            <span
                v-if="confirmPinPassword.length >= index"
                class="pin-dot"
            ></span>
          </div>

          <input
              ref="pinInput"
              :value="confirmPinPassword"
              class="hidden-pin-input"
              inputmode="numeric"
              maxlength="6"
              pattern="[0-9]*"
              type="password"
              autocomplete="new-password"
              @input="changePin"
          />
        </div>

        <p
            v-if="errorMessage"
            class="error-message text-13"
        >
          {{ errorMessage }}
        </p>

        <p
            v-else
            class="guide-message text-13"
        >
          두 입력이 일치하면 변경이 완료돼요.
        </p>
      </section>
    </main>

    <!-- 공통 하단 버튼 -->
    <div class="bottom-btn-area single">
      <button
          class="bottom-btn"
          :disabled="confirmPinPassword.length !== 6 || loading"
          type="button"
          @click="resetPinPassword"
      >
        {{ loading ? '변경 중...' : '간편비밀번호 변경' }}
      </button>
    </div>

    <!-- 로딩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <span class="text-15-bold">
        간편비밀번호를 변경하고 있어요.
      </span>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { resetPin } from '@/api/userApi';
import PageHeader from '@/components/common/PageHeader.vue';

const router = useRouter();

const pinInput = ref(null);
const confirmPinPassword = ref('');
const errorMessage = ref('');
const loading = ref(false);

// PIN 입력창 포커스
const focusPinInput = async () => {
  if (loading.value) return;

  await nextTick();
  pinInput.value?.focus();
};

// PIN 입력
const changePin = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 6);

  confirmPinPassword.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) event.target.value = value;
};

// PIN 재설정
const resetPinPassword = async () => {
  const phoneNumber = sessionStorage.getItem('pinResetPhoneNumber');
  const newPinPassword = sessionStorage.getItem('pinResetNewPin');

  if (!phoneNumber || !newPinPassword) {
    await router.replace('/intro');
    return;
  }

  if (confirmPinPassword.value !== newPinPassword) {
    confirmPinPassword.value = '';
    errorMessage.value = '간편비밀번호가 일치하지 않습니다.';

    await focusPinInput();
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    await resetPin({
      phoneNumber,
      newPinPassword,
      newPinPasswordConfirm: confirmPinPassword.value,
    });

    sessionStorage.removeItem('pinResetNewPin');
    sessionStorage.setItem('pinResetCompleted', 'true');

    await router.replace('/auth/pin-reset-complete');
  } catch (error) {
    console.error(error);

    confirmPinPassword.value = '';
    errorMessage.value = error.response?.data?.message || '간편비밀번호 변경에 실패했습니다.';
  } finally {
    loading.value = false;

    if (errorMessage.value) await focusPinInput();
  }
};

// 이전 화면
const goBack = async () => {
  sessionStorage.removeItem('pinResetNewPin');
  await router.replace('/auth/pin-reset');
};

onMounted(() => {
  focusPinInput();
});
</script>

<style scoped>
@import "@/components/common/common/common.css";
@import "@/components/common/common/layout.css";

.pin-page {
  position: relative;
  background: var(--color-bg-page);
}

.pin-content {
  padding-top: 30px;
}

/* 진행 단계 */
.step-area {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
}

.step {
  flex-shrink: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-border-main);
}

.step.active {
  width: 44px;
  height: 12px;
  border-radius: 999px;
  background: var(--color-primary);
}

.step-line {
  width: 38px;
  height: 1px;
  margin: 0 8px;
  background: var(--color-border-main);
}

/* 제목 */
.pin-header h1 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.pin-header p {
  margin: 14px 0 0;
  color: var(--color-text-sub);
  line-height: 1.6;
}

/* PIN 입력 */
.pin-section {
  margin-top: 52px;
  text-align: center;
}

.pin-boxes {
  position: relative;
  display: grid;
  width: 100%;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 8px;
  cursor: text;
  outline: none;
}

.pin-box {
  display: flex;
  height: 54px;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border-main);
  border-radius: 12px;
  background: #fafafa;
  box-sizing: border-box;
  transition:
      border-color 0.2s,
      background 0.2s,
      box-shadow 0.2s;
}

.pin-box.active {
  border-color: var(--color-primary);
  background: #fffaf0;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.pin-box.filled {
  border-color: var(--color-primary);
  background: #fff8e5;
}

.pin-boxes.error .pin-box {
  border-color: var(--color-error);
  background: #fff7f7;
  box-shadow: none;
}

.pin-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: var(--color-text-main);
}

.hidden-pin-input {
  position: absolute;
  width: 1px;
  height: 1px;
  border: 0;
  opacity: 0;
  pointer-events: none;
}

/* 안내 문구 */
.error-message,
.guide-message {
  min-height: 20px;
  margin: 16px 0 0;
  line-height: 1.5;
}

.error-message {
  color: var(--color-error);
}

.guide-message {
  color: var(--color-text-muted);
}

/* 로딩 */
.loading-overlay {
  position: absolute;
  inset: 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.86);
  color: var(--color-text-main);
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 4px solid var(--color-bg-disabled);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>