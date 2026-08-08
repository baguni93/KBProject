<template>
  <div class="withdraw-page">
    <main class="withdraw-container">
      <header class="page-header">
        <button class="back-button" type="button" aria-label="이전 화면" @click="goBack">
          &lt;
        </button>

        <h1>회원탈퇴</h1>
        <div class="header-empty"></div>
      </header>

      <section class="withdraw-content">
        <div class="warning-icon">!</div>

        <h2>
          정말 회원탈퇴를<br />
          진행하시겠어요?
        </h2>

        <p class="description">
          회원탈퇴 후에는 서비스를 이용할 수 없으며<br />
          일부 정보는 복구할 수 없어요.
        </p>

        <section class="warning-area">
          <strong>탈퇴 전 확인해 주세요</strong>

          <ul>
            <li>연결된 계좌와 카드 정보 이용이 종료돼요.</li>
            <li>보유한 혜택과 서비스 이용 기록이 제한될 수 있어요.</li>
            <li>탈퇴 후 일정 시간 동안 재가입이 제한될 수 있어요.</li>
          </ul>
        </section>

        <section class="reason-area">
          <label for="withdrawalReason">탈퇴 사유</label>

          <select id="withdrawalReason" v-model="withdrawalReason" :disabled="loading">
            <option value="">탈퇴 사유를 선택해 주세요</option>
            <option value="LOW_USAGE">서비스를 자주 이용하지 않아요</option>
            <option value="INCONVENIENT">서비스 이용이 불편해요</option>
            <option value="PRIVACY">개인정보가 걱정돼요</option>
            <option value="REJOIN">새로운 계정으로 다시 가입하고 싶어요</option>
            <option value="OTHER">기타</option>
          </select>
        </section>

        <section class="pin-area">
          <label for="pinPassword">간편비밀번호 확인</label>
          <p>본인 확인을 위해 현재 간편비밀번호를 입력해 주세요.</p>

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
                  filled: pinPassword.length >= index,
                  active: pinPassword.length === index - 1 && !errorMessage,
                }"
                class="pin-box"
            >
              <span v-if="pinPassword.length >= index" class="pin-dot"></span>
            </div>

            <input
                id="pinPassword"
                ref="pinInput"
                :value="pinPassword"
                class="hidden-pin-input"
                inputmode="numeric"
                maxlength="6"
                pattern="[0-9]*"
                type="password"
                autocomplete="current-password"
                @input="changePin"
            />
          </div>

          <p v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </p>
        </section>

        <label class="agreement-check">
          <input v-model="agreed" :disabled="loading" type="checkbox" />
          <span>탈퇴 시 안내사항을 확인했으며 이에 동의합니다.</span>
        </label>
      </section>

      <button class="withdraw-button" :disabled="!canWithdraw || loading" type="button" @click="openWithdrawModal">
        {{ loading ? '탈퇴 처리 중...' : '회원탈퇴' }}
      </button>

      <div v-if="showWithdrawModal" class="modal-overlay" @click.self="closeWithdrawModal">
        <div class="withdraw-modal">
          <div class="modal-warning-icon">!</div>

          <h3>회원탈퇴를 진행할까요?</h3>

          <p>
            탈퇴 후에는 일부 정보를 복구할 수 없으며<br />
            서비스 이용이 제한돼요.
          </p>

          <div class="modal-info">
            <strong>탈퇴 사유</strong>
            <span>{{ withdrawalReasonLabel }}</span>
          </div>

          <div class="modal-buttons">
            <button class="modal-cancel-button" type="button" @click="closeWithdrawModal">
              취소
            </button>

            <button class="modal-withdraw-button" type="button" @click="withdraw">
              회원탈퇴
            </button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <span>회원탈퇴를 처리하고 있어요.</span>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { withdrawUser } from '@/api/userApi';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const pinInput = ref(null);
const pinPassword = ref('');
const withdrawalReason = ref('');
const agreed = ref(false);
const loading = ref(false);
const errorMessage = ref('');
const showWithdrawModal = ref(false);

const withdrawalReasonMap = {
  LOW_USAGE: '서비스를 자주 이용하지 않아요',
  INCONVENIENT: '서비스 이용이 불편해요',
  PRIVACY: '개인정보가 걱정돼요',
  REJOIN: '새로운 계정으로 다시 가입하고 싶어요',
  OTHER: '기타',
};

// 탈퇴 가능 여부
const canWithdraw = computed(() => withdrawalReason.value.length > 0 && pinPassword.value.length === 6 && agreed.value);

// 탈퇴 사유 표시
const withdrawalReasonLabel = computed(() => withdrawalReasonMap[withdrawalReason.value] || '-');

// PIN 입력창 포커스
const focusPinInput = async () => {
  if (loading.value) return;

  await nextTick();
  pinInput.value?.focus();
};

// PIN 입력
const changePin = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 6);

  pinPassword.value = value;
  errorMessage.value = '';

  if (event.target.value !== value) event.target.value = value;
};

// 탈퇴 확인 모달 열기
const openWithdrawModal = () => {
  if (!canWithdraw.value || loading.value) return;
  showWithdrawModal.value = true;
};

// 탈퇴 확인 모달 닫기
const closeWithdrawModal = () => {
  showWithdrawModal.value = false;
};

// 회원탈퇴
const withdraw = async () => {
  if (!canWithdraw.value || loading.value) return;

  if (!authStore.userId) {
    await router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';
    showWithdrawModal.value = false;

    await withdrawUser(authStore.userId, {
      pinPassword: pinPassword.value,
      withdrawalReason: withdrawalReason.value,
    });

    authStore.clearAuth();

    await router.replace({
      path: '/setting/account-management/complete',
      query: { type: 'WITHDRAW' },
    });
  } catch (error) {
    console.error(error);

    pinPassword.value = '';

    const serverMessage = error.response?.data?.message || '';

    if (serverMessage.includes('간편비밀번호') || serverMessage.includes('비밀번호')) {
      errorMessage.value = '간편비밀번호가 일치하지 않습니다.';
    } else {
      errorMessage.value = serverMessage || '회원탈퇴에 실패했습니다. 다시 시도해주세요.';
    }

    await focusPinInput();
  } finally {
    loading.value = false;
  }
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(focusPinInput);
</script>

<style scoped>
.withdraw-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.withdraw-container {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  padding: 10px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.withdraw-container::-webkit-scrollbar {
  display: none;
}

.page-header {
  display: grid;
  grid-template-columns: 38px 1fr 38px;
  min-height: 44px;
  flex-shrink: 0;
  align-items: center;
}

.page-header h1 {
  margin: 0;
  color: #222222;
  font-size: 17px;
  font-weight: 700;
  text-align: center;
}

.back-button {
  justify-self: start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 27px;
  line-height: 1;
  cursor: pointer;
}

.header-empty {
  width: 38px;
}

.withdraw-content {
  margin-top: 34px;
  text-align: center;
}

.warning-icon {
  display: flex;
  width: 68px;
  height: 68px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 22px;
  border-radius: 50%;
  background: #fff0f0;
  color: #e53935;
  font-size: 34px;
  font-weight: 800;
}

.withdraw-content h2 {
  margin: 0;
  color: #111111;
  font-size: 25px;
  font-weight: 800;
  line-height: 1.4;
  letter-spacing: -0.7px;
}

.description {
  margin: 16px 0 0;
  color: #777777;
  font-size: 14px;
  line-height: 1.6;
}

.warning-area {
  margin-top: 26px;
  padding: 18px;
  border-radius: 14px;
  background: #fff6f6;
  text-align: left;
}

.warning-area strong {
  color: #e53935;
  font-size: 13px;
  font-weight: 800;
}

.warning-area ul {
  margin: 12px 0 0;
  padding-left: 17px;
  color: #777777;
  font-size: 11px;
  line-height: 1.8;
}

.reason-area,
.pin-area {
  margin-top: 24px;
  text-align: left;
}

.reason-area label,
.pin-area label {
  display: block;
  margin-bottom: 9px;
  color: #333333;
  font-size: 14px;
  font-weight: 700;
}

.reason-area select {
  width: 100%;
  height: 52px;
  padding: 0 14px;
  border: 1px solid #dddddd;
  border-radius: 10px;
  background: #ffffff;
  color: #333333;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.reason-area select:focus {
  border-color: #e53935;
  box-shadow: 0 0 0 3px rgba(229, 57, 53, 0.08);
}

.pin-area > p {
  margin: 0 0 12px;
  color: #888888;
  font-size: 11px;
  line-height: 1.5;
}

.pin-boxes {
  position: relative;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 9px;
  width: 100%;
  cursor: text;
  outline: none;
}

.pin-box {
  display: flex;
  height: 54px;
  align-items: center;
  justify-content: center;
  border: 1px solid #dddddd;
  border-radius: 12px;
  background: #ffffff;
  box-sizing: border-box;
}

.pin-box.active {
  border-color: #e53935;
  box-shadow: 0 0 0 3px rgba(229, 57, 53, 0.08);
}

.pin-box.filled {
  border-color: #cccccc;
  background: #fafafa;
}

.pin-boxes.error .pin-box {
  border-color: #e53935;
  background: #fff7f7;
}

.pin-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #222222;
}

.hidden-pin-input {
  position: absolute;
  width: 1px;
  height: 1px;
  border: 0;
  opacity: 0;
  pointer-events: none;
}

.error-message {
  margin: 10px 0 0;
  color: #e53935;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.5;
}

.agreement-check {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
  text-align: left;
  cursor: pointer;
}

.agreement-check input {
  flex: none;
  width: 18px;
  height: 18px;
  margin: 0;
  accent-color: #e53935;
}

.agreement-check span {
  color: #555555;
  font-size: 11px;
  line-height: 1.5;
}

.withdraw-button {
  position: absolute;
  right: 28px;
  bottom: 58px;
  left: 28px;
  width: auto;
  height: 58px;
  margin: 0;
  border: 1px solid #d32f2f;
  border-radius: 10px;
  background: #e53935;
  color: #ffffff;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}

.withdraw-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
  cursor: not-allowed;
}

.modal-overlay {
  position: fixed;
  z-index: 100;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.46);
  box-sizing: border-box;
}

.withdraw-modal {
  width: 100%;
  max-width: 340px;
  padding: 28px 22px 20px;
  border-radius: 18px;
  background: #ffffff;
  text-align: center;
  box-sizing: border-box;
  box-shadow: 0 14px 40px rgba(0, 0, 0, 0.18);
}

.modal-warning-icon {
  display: flex;
  width: 58px;
  height: 58px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 18px;
  border-radius: 50%;
  background: #fff0f0;
  color: #e53935;
  font-size: 30px;
  font-weight: 800;
}

.withdraw-modal h3 {
  margin: 0;
  color: #111111;
  font-size: 20px;
  font-weight: 800;
}

.withdraw-modal > p {
  margin: 14px 0 0;
  color: #777777;
  font-size: 13px;
  line-height: 1.6;
}

.modal-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 22px;
  padding: 15px 16px;
  border: 1px solid #eeeeee;
  border-radius: 12px;
  background: #fafafa;
  text-align: left;
}

.modal-info strong {
  color: #555555;
  font-size: 12px;
}

.modal-info span {
  color: #222222;
  font-size: 12px;
  font-weight: 700;
}

.modal-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 24px;
}

.modal-cancel-button,
.modal-withdraw-button {
  height: 50px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.modal-cancel-button {
  border: 1px solid #dddddd;
  background: #ffffff;
  color: #333333;
}

.modal-withdraw-button {
  border: 1px solid #ffcaca;
  background: #ffe7e7;
  color: #e53935;
}

.loading-overlay {
  position: absolute;
  z-index: 110;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.88);
  color: #333333;
  font-size: 14px;
  font-weight: 700;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 4px solid #eeeeee;
  border-top-color: #e53935;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@media (max-width: 360px) {
  .withdraw-container {
    padding-right: 20px;
    padding-left: 20px;
  }

  .withdraw-button {
    right: 20px;
    left: 20px;
  }

  .pin-boxes {
    gap: 7px;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>