<template>
  <div class="content-container">
    <header class="page-header">
      <p class="page-subtitle">
        계좌 거래내역에 표시된<br />
        인증번호 4자리를 입력해 주세요.
      </p>
    </header>

    <section class="verification-section">
      <div class="section">
        <label class="section-title">인증번호</label>
        <div class="input-wrapper">
          <input
            ref="verificationInput"
            v-model="verificationCode"
            type="text"
            inputmode="numeric"
            maxlength="4"
            placeholder="인증번호 4자리를 입력해 주세요"
            class="text-input"
            @input="changeVerificationCode"
          />
        </div>
      </div>

      <p v-if="customCardStore.code" class="development-code">
        개발용 인증번호: {{ customCardStore.code }}
      </p>

      <p v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </p>
    </section>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref, computed, watch } from 'vue';
import { confirmAccountVerification } from '@/api/accountApi';
import { useModalStore } from '@/stores/userModalStore';
import { useCustomCardStore } from '@/stores/customcard';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const customCardStore = useCustomCardStore();
const modalStore = useModalStore();

const verificationInput = ref(null);
const verificationCode = ref('');
const loading = ref(false);
const errorMessage = ref('');

// 부모가 전달해 주는 actionTrigger를 받기 위한 props 정의
const props = defineProps({
  actionTrigger: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(['update:isValid', 'update:loading', 'next']);

// 인증 요청 가능 여부
const canSubmit = computed(() => {
  return verificationCode.value.length == 4;
});

// 1. 폼 유효성이 바뀔 때마다 부모에게 전달
watch(
  canSubmit,
  (val) => {
    emit('update:isValid', val);
  },
  { immediate: true },
);

// 2. 로딩 상태가 바뀔 때마다 부모에게 전달
watch(loading, (val) => {
  emit('update:loading', val);
});

const focusInput = async () => {
  await nextTick();
  verificationInput.value?.focus();
};

const changeVerificationCode = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 4);

  verificationCode.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) event.target.value = value;
};

const confirmAndConnect = async () => {
  const userId = authStore.userId;
  const verificationId = customCardStore.id;

  console.log(userId);
  console.log(verificationId);

  if (!userId || !verificationId) {
    console.error('user ID or verification ID null');
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    await confirmAccountVerification(userId, {
      verificationId,
      verificationCode: verificationCode.value,
    });

    // 성공 시 부모에게 다음 단계로 넘어가라는 신호 발송
    emit('next');
  } catch (error) {
    await modalStore.showAlert(
      error.error || '알 수 없는 오류가 발생했습니다.',
    );
    await focusInput();
  } finally {
    loading.value = false;
  }
};

// 3. 부모가 하단 버튼을 눌러 actionTrigger 숫자가 올라가면 이 함수가 실행됨
watch(
  () => props.actionTrigger,
  async (newVal) => {
    if (newVal === 0) return;
    await confirmAndConnect();
  },
);

onMounted(() => {
  focusInput();
});
</script>

<style scoped>
.account-page {
  width: 100%;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  padding: 12px;
  background: #ffffff;
}

.content-container {
  position: relative;
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  width: 100%;
  max-width: 340px;
  margin: 0 auto;
  padding: 8px 4px 80px;
  box-sizing: border-box;
}

.page-header {
  margin: 0;
}

.page-subtitle {
  margin: 0;
  color: #555555;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.5;
  text-align: left;
}

.verification-section {
  display: flex;
  flex-direction: column;
  min-height: 0;
  margin-top: 24px;
  padding: 0;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.section-title {
  font-size: 11px;
  color: #888888;
  font-weight: 600;
  text-align: left;
  margin: 0;
}

/* 카드 정보 입력창과 완전히 동일한 스타일 적용 (#f4f5f7 배경, 22px 둥글기, 13px 폰트) */
.text-input {
  width: 100%;
  height: 44px;
  background-color: #f4f5f7;
  border: 1px solid transparent;
  border-radius: 22px;
  padding: 0 16px;
  font-size: 13px;
  color: #222222;
  outline: none;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.text-input::placeholder {
  color: #aaaaaa;
}

.text-input:focus {
  border-color: #ffc107;
  background-color: #ffffff;
}

.development-code {
  margin: 16px 0 0 4px;
  color: #888888;
  font-size: 12px;
  line-height: 1.5;
  text-align: left;
}

.error-message {
  margin: 16px 0 0 4px;
  color: #e53935;
  font-size: 12px;
  line-height: 1.5;
  text-align: left;
}

/* 하단 확인 버튼 스타일 통일 */
.confirm-button {
  position: absolute;
  right: 4px;
  bottom: 24px;
  left: 4px;
  width: auto;
  height: 48px;
  margin: 0;
  border: 1px solid transparent;
  border-radius: 24px;
  background: #ffc107;
  color: #222222;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s ease;
}

.confirm-button:active:not(:disabled) {
  background: #e0a800;
}

.confirm-button:disabled {
  border-color: transparent;
  background: #f4f5f7;
  color: #cccccc;
  cursor: not-allowed;
}
</style>
