<template>
  <div class="withdraw-page">
    <main class="withdraw-container">
      <header class="page-header">
        <button
            class="back-button"
            type="button"
            aria-label="이전 화면"
            @click="goBack"
        >
          &lt;
        </button>

        <h1>회원탈퇴</h1>

        <div class="header-empty"></div>
      </header>

      <section class="withdraw-content">
        <div class="warning-icon">
          !
        </div>

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
          <label for="withdrawalReason">
            탈퇴 사유
          </label>

          <select
              id="withdrawalReason"
              v-model="withdrawalReason"
              :disabled="loading"
          >
            <option value="">
              탈퇴 사유를 선택해 주세요
            </option>

            <option value="LOW_USAGE">
              서비스를 자주 이용하지 않아요
            </option>

            <option value="INCONVENIENT">
              서비스 이용이 불편해요
            </option>

            <option value="PRIVACY">
              개인정보가 걱정돼요
            </option>

            <option value="REJOIN">
              새로운 계정으로 다시 가입하고 싶어요
            </option>

            <option value="OTHER">
              기타
            </option>
          </select>
        </section>

        <section class="pin-area">
          <label for="pinPassword">
            간편비밀번호 확인
          </label>

          <p>
            본인 확인을 위해 현재 간편비밀번호를 입력해 주세요.
          </p>

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
              <span
                  v-if="pinPassword.length >= index"
                  class="pin-dot"
              ></span>
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

          <span>
            탈퇴 시 안내사항을 확인했으며 이에 동의합니다.
          </span>
        </label>
      </section>

      <button
          class="withdraw-button"
          :disabled="!canWithdraw || loading"
          type="button"
          @click="withdraw"
      >
        {{ loading ? '탈퇴 처리 중...' : '회원탈퇴' }}
      </button>

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

// 탈퇴 가능 여부
const canWithdraw = computed(() =>
    withdrawalReason.value.length > 0
    && pinPassword.value.length === 6
    && agreed.value,
);

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

// 회원탈퇴
const withdraw = async () => {
  if (!canWithdraw.value || loading.value) return;

  if (!authStore.userId) {
    await router.replace('/intro');
    return;
  }

  const confirmed = window.confirm(
      '회원탈퇴를 진행하면 계정을 복구하기 어려울 수 있습니다. 정말 탈퇴할까요?',
  );

  if (!confirmed) return;

  try {
    loading.value = true;
    errorMessage.value = '';

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
    errorMessage.value =
        error.response?.data?.message
        || '회원탈퇴에 실패했습니다. 간편비밀번호를 확인해 주세요.';

    await focusPinInput();
  } finally {
    loading.value = false;
  }
};

// 이전 화면
const goBack = () => {
  if (pinPassword.value || withdrawalReason.value || agreed.value) {
    const confirmed = window.confirm('입력한 내용을 취소하고 이전 화면으로 이동할까요?');

    if (!confirmed) return;
  }

  router.back();
};

onMounted(() => {
  focusPinInput();
});
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
  margin-top: 38px;
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
  font-weight: 400;
  line-height: 1.6;
}

.warning-area {
  margin-top: 26px;
  padding: 16px;
  border-radius: 14px;
  background: #fff7f7;
  text-align: left;
}

.warning-area strong {
  color: #d32f2f;
  font-size: 12px;
  font-weight: 800;
}

.warning-area ul {
  margin: 12px 0 0;
  padding-left: 17px;
  color: #777777;
  font-size: 10px;
  line-height: 1.8;
}

.reason-area,
.pin-area {
  margin-top: 22px;
  text-align: left;
}

.reason-area label,
.pin-area label {
  display: block;
  margin-bottom: 9px;
  color: #333333;
  font-size: 13px;
  font-weight: 800;
}

.reason-area select {
  width: 100%;
  height: 52px;
  padding: 0 14px;
  border: 1px solid #dddddd;
  border-radius: 10px;
  background: #ffffff;
  color: #444444;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  cursor: pointer;
}

.reason-area select:focus {
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.reason-area select:disabled {
  background: #f7f7f7;
  color: #aaaaaa;
  cursor: not-allowed;
}

.pin-area > p {
  margin: 0 0 12px;
  color: #999999;
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
  background: #fafafa;
  box-sizing: border-box;
  transition:
      border-color 0.2s,
      background 0.2s,
      box-shadow 0.2s;
}

.pin-box.active {
  border-color: #ffbc2e;
  background: #fffaf0;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.pin-box.filled {
  border-color: #ffbc2e;
  background: #fff8e5;
}

.pin-boxes.error .pin-box {
  border-color: #e53935;
  background: #fff7f7;
  box-shadow: none;
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
  font-size: 11px;
  line-height: 1.5;
}

.agreement-check {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-top: 20px;
  text-align: left;
  cursor: pointer;
}

.agreement-check input {
  flex: none;
  width: 17px;
  height: 17px;
  margin: 0;
  accent-color: #e53935;
  cursor: pointer;
}

.agreement-check input:disabled {
  cursor: not-allowed;
}

.agreement-check span {
  color: #666666;
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

.withdraw-button:active:not(:disabled) {
  background: #d32f2f;
}

.withdraw-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
  cursor: not-allowed;
}

.loading-overlay {
  position: absolute;
  z-index: 10;
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