<template>
  <div class="account-container">
    <form class="account-form" @submit.prevent="requestVerification">
      <div class="form-fields">
        <!-- 예금주 섹션 -->
        <div class="section">
          <label for="accountHolder" class="section-title">예금주</label>
          <div class="readonly-field">
            <input
              id="accountHolder"
              :value="accountHolder"
              class="text-input readonly-input"
              placeholder="회원 실명을 불러오고 있어요"
              type="text"
              readonly
            />
            <span v-if="userLoading" class="field-loading">조회 중</span>
          </div>
          <p class="field-guide">
            로그인한 회원의 실명으로만 계좌를 연결할 수 있어요.
          </p>
        </div>

        <!-- 결제계좌인증 섹션 -->
        <div class="section">
          <label class="section-title">결제계좌인증</label>
          <div class="readonly-field">
            <input
              class="text-input readonly-input"
              placeholder="국민은행"
              type="text"
              readonly
            />
          </div>
        </div>

        <!-- 계좌번호 섹션 -->
        <div class="section">
          <input
            v-model="accountNumber"
            type="text"
            inputmode="numeric"
            maxlength="20"
            placeholder="'-' 없이 숫자만 입력해 주세요"
            class="text-input"
            @input="changeAccountNumber"
          />
          <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
        </div>
        <p class="field-guide">
          * 인증을 위해 1원을 받을 계좌를 확인하고 계좌로 전송된 4자리 숫자로
          인증해주세요
        </p>
      </div>
    </form>
  </div>
</template>

<script setup>
import { computed, ref, watch, onMounted } from 'vue';
import customCardApi from '@/api/customCard.Api';
import { useAuthStore } from '@/stores/auth.js';
import { useModalStore } from '@/stores/userModalStore';
import { useCustomCardStore } from '@/stores/customcard';
import { getAccountByBankCode } from '@/api/userApi';

const customCardStore = useCustomCardStore();
const modalStore = useModalStore();
const authStore = useAuthStore();

const userName = authStore.userName ?? 'test';

const accountHolder = ref(userName);
const accountNumber = ref('');

const loading = ref(false);
const userLoading = ref(false);
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
  return accountHolder.value.length > 0 && accountNumber.value.length >= 8;
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

// 계좌번호 입력 핸들러
const changeAccountNumber = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 20);
  accountNumber.value = value;

  if (event.target.value !== value) {
    event.target.value = value;
  }
};

// 인증번호 발급 로직
const requestVerification = async () => {
  if (!canSubmit.value || loading.value || userLoading.value) return;

  try {
    loading.value = true;
    errorMessage.value = '';

    const requestData = {
      userId: authStore.userId,
      accountNumber: accountNumber.value,
    };

    const response = await customCardApi.checkCanIssue(requestData);
    const status = response?.checkCanIssueStatus;

    if (status === 'KB_ALREADY_HAS_CUSTOM') {
      await modalStore.showAlert(
        '이미 KB MY WAY 체크 카드가 발급된 계좌입니다.',
        '발급 불가',
      );
      return;
    }

    if (status === 'NOT_KB_BANK') {
      await modalStore.showAlert(
        '국민은행 계좌로만 발급이 가능합니다.',
        '알림',
      );
      return;
    }

    customCardStore.code = response?.verificationCode;
    customCardStore.id = response?.verificationId;

    // 성공 시 부모에게 다음 단계로 넘어가라는 신호 발송
    emit('next');
  } catch (error) {
    errorMessage.value = error.error || '알 수 없는 오류가 발생했습니다.';
    await modalStore.showAlert(errorMessage.value);
  } finally {
    loading.value = false;
  }
};

// 3. 부모가 하단 버튼을 눌러 actionTrigger 숫자가 올라가면 이 함수가 실행됨
watch(
  () => props.actionTrigger,
  async (newVal) => {
    if (newVal === 0) return;
    await requestVerification();
  },
);

const accountInfo = async () => {
  const response = await getAccountByBankCode(authStore.userId, '004');

  accountNumber.value = response.accountNumber;
};

onMounted(async () => {
  await accountInfo();
});
</script>

<style scoped>
.account-page {
  width: 100%;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  padding: 12px;
  background-color: #ffffff;
}

.account-container {
  flex: 1;
  width: 100%;
  max-width: 340px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  padding: 8px 4px;
  box-sizing: border-box;
}

.account-form {
  display: flex;
  flex-direction: column;
  flex: 1;
  justify-content: space-between;
  box-sizing: border-box;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 18px; /* 두 번째 코드의 간격(18px)과 동일하게 맞춤 */
}

.section-title-area {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  text-align: left;
  margin-bottom: -2px;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 6px; /* 두 번째 코드의 라벨-인풋 간격(6px)과 동일하게 맞춤 */
}

.section-title {
  font-size: 12px;
  color: #888888;
  font-weight: 600;
  text-align: left;
  margin: 0;
}

/* 두 번째 코드의 인풋 스타일 기준 적용 (#f4f5f7 배경, 22px 둥글기, 13px 폰트) */
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

.readonly-field {
  position: relative;
}

.readonly-input {
  padding-right: 70px !important;
  background-color: #f4f5f7 !important;
  border-color: transparent !important;
  color: #555555 !important;
  cursor: default;
}

.readonly-input:focus {
  border-color: transparent !important;
  background-color: #f4f5f7 !important;
  box-shadow: none !important;
}

.field-loading {
  position: absolute;
  top: 50%;
  right: 16px;
  color: #999999;
  font-size: 11px;
  transform: translateY(-50%);
}

.field-guide {
  margin: 2px 0 0 4px;
  color: #999999;
  font-size: 11px;
  line-height: 1.4;
  text-align: left;
}

.error-message {
  margin: 4px 0 0 4px;
  color: #e53935;
  font-size: 11px;
  line-height: 1.4;
  text-align: left;
}

.button-area {
  margin-top: auto;
  padding-top: 24px;
  width: 100%;
}

/* 두 번째 폼 스타일과 어울리도록 조화로운 하단 버튼 스타일 */
.next-button {
  width: 100%;
  height: 48px;
  border: 1px solid transparent;
  border-radius: 24px;
  background: #ffc107;
  color: #222222;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.next-button:active:not(:disabled) {
  background: #e0a800;
}

.next-button:disabled {
  border-color: transparent;
  background: #f4f5f7;
  color: #cccccc;
  cursor: not-allowed;
}
</style>
