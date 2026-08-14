<template>
  <div class="page-layout withdraw-page">
    <PageHeader title="" custom-back @back="goBack" />

    <main class="page-content withdraw-container">
      <section class="withdraw-content">
        <section class="intro-section">
          <div class="warning-icon">
            <i class="fa-solid fa-triangle-exclamation"></i>
          </div>

          <h2 class="text-26-bold">
            정말 회원탈퇴를<br />
            진행하시겠어요?
          </h2>

          <p class="description text-13">
            탈퇴 후에는 서비스를 이용할 수 없으며<br />
            일부 정보는 복구할 수 없어요.
          </p>
        </section>

        <!-- 탈퇴 사유 -->
        <section class="reason-area">
          <label class="section-label text-15-bold">탈퇴 사유</label>

          <button
              class="reason-select"
              :disabled="loading"
              type="button"
              @click="openReasonSheet"
          >
            <span v-if="withdrawalReason" class="reason-value">
              {{ withdrawalReason }}
            </span>

            <span v-else class="reason-placeholder">
              탈퇴 사유를 선택해 주세요
            </span>

            <i class="fa-solid fa-chevron-down"></i>
          </button>
        </section>

        <!-- 탈퇴 안내 -->
        <section class="warning-area">
          <div class="warning-title">
            <span>
              <i class="fa-solid fa-circle-exclamation"></i>
            </span>

            <strong class="text-15-bold">탈퇴 전 확인해 주세요</strong>
          </div>

          <ul>
            <li>연결된 계좌와 카드 정보 이용이 종료돼요.</li>
            <li>보유한 혜택과 서비스 이용 기록이 제한될 수 있어요.</li>
            <li>탈퇴 후 일정 시간 동안 재가입이 제한될 수 있어요.</li>
          </ul>
        </section>

        <!-- 동의 -->
        <label class="agreement-check" :class="{ checked: agreed }">
          <input v-model="agreed" :disabled="loading" type="checkbox" />

          <span class="custom-checkbox">
            <i v-if="agreed" class="fa-solid fa-check"></i>
          </span>

          <span class="agreement-text">
            <strong class="text-13-bold">
              탈퇴 안내사항을 확인했으며 이에 동의합니다
            </strong>

            <span>위 내용을 모두 확인했어요.</span>
          </span>
        </label>
      </section>
    </main>

    <!-- 하단 버튼 -->
    <div class="bottom-btn-area single withdraw-bottom-area">
      <button
          class="withdraw-main-button"
          :disabled="!canStartWithdraw || loading"
          type="button"
          @click="openPinModal"
      >
        회원탈퇴
      </button>
    </div>

    <!-- 탈퇴 사유 Bottom Sheet -->
    <Transition name="sheet">
      <div
          v-if="showReasonSheet"
          class="overlay sheet-overlay"
          @click.self="closeReasonSheet"
      >
        <section class="reason-sheet">
          <div class="sheet-handle"></div>

          <header class="reason-sheet-header">
            <h3 class="text-20-bold">탈퇴 사유</h3>
            <p class="text-13">탈퇴하시는 이유를 선택해 주세요.</p>
          </header>

          <div class="reason-list">
            <button
                v-for="reason in withdrawalReasons"
                :key="reason"
                class="reason-item"
                :class="{ selected: withdrawalReason === reason }"
                type="button"
                @click="selectReason(reason)"
            >
              <span>{{ reason }}</span>

              <i
                  v-if="withdrawalReason === reason"
                  class="fa-solid fa-check"
              ></i>
            </button>
          </div>
        </section>
      </div>
    </Transition>

    <!-- PIN 중앙 Modal -->
    <Transition name="modal">
      <div
          v-if="showPinModal"
          class="overlay modal-overlay"
          @click.self="closePinModal"
      >
        <section class="pin-modal">
          <div class="pin-icon">
            <i class="fa-solid fa-lock"></i>
          </div>

          <h3 class="text-20-bold">간편비밀번호 확인</h3>

          <p class="pin-description text-13">
            본인 확인을 위해 현재 간편비밀번호<br />
            6자리를 입력해 주세요.
          </p>

          <div
              class="pin-boxes"
              :class="{ error: !!pinErrorMessage }"
              role="button"
              tabindex="0"
              @click="focusPinInput"
              @keydown.enter="focusPinInput"
          >
            <div
                v-for="index in 6"
                :key="index"
                class="pin-box"
                :class="{
                filled: pinPassword.length >= index,
                active: pinPassword.length === index - 1 && !pinErrorMessage,
              }"
            >
              <span v-if="pinPassword.length >= index" class="pin-dot"></span>
            </div>

            <input
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

          <div class="pin-message-area">
            <p v-if="pinErrorMessage" class="pin-error-message text-13">
              {{ pinErrorMessage }}
            </p>

            <p v-else class="pin-helper-message">
              입력한 비밀번호는 본인 확인에만 사용돼요.
            </p>
          </div>

          <button
              class="pin-confirm-button"
              :disabled="pinPassword.length !== 6 || pinVerifying"
              type="button"
              @click="verifyPinPassword"
          >
            {{ pinVerifying ? '확인 중...' : '확인' }}
          </button>
        </section>
      </div>
    </Transition>

    <!-- 최종 탈퇴 확인 Modal -->
    <Transition name="modal">
      <div
          v-if="showFinalModal"
          class="overlay modal-overlay"
          @click.self="closeFinalModal"
      >
        <section class="final-modal">
          <div class="final-warning-icon">
            <i class="fa-solid fa-triangle-exclamation"></i>
          </div>

          <h3 class="text-20-bold">
            정말 회원탈퇴하시겠어요?
          </h3>

          <p class="final-description text-13">
            탈퇴 후에는 서비스를 이용할 수 없으며<br />
            일부 정보는 복구할 수 없어요.
          </p>

          <div class="final-reason">
            <span>탈퇴 사유</span>
            <strong>{{ withdrawalReason }}</strong>
          </div>

          <p v-if="withdrawErrorMessage" class="withdraw-error-message text-13">
            {{ withdrawErrorMessage }}
          </p>

          <div class="final-buttons">
            <button
                class="final-cancel-button"
                :disabled="loading"
                type="button"
                @click="closeFinalModal"
            >
              취소
            </button>

            <button
                class="final-withdraw-button"
                :disabled="loading"
                type="button"
                @click="withdraw"
            >
              {{ loading ? '처리 중...' : '회원탈퇴' }}
            </button>
          </div>
        </section>
      </div>
    </Transition>

    <!-- 전체 로딩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <span class="text-13-bold">회원탈퇴를 처리하고 있어요.</span>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, ref } from 'vue';
import { useRouter } from 'vue-router';
import { verifyPin, withdrawUser } from '@/api/userApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const pinInput = ref(null);
const withdrawalReason = ref('');
const pinPassword = ref('');
const agreed = ref(false);
const loading = ref(false);
const pinVerifying = ref(false);
const pinErrorMessage = ref('');
const withdrawErrorMessage = ref('');
const showReasonSheet = ref(false);
const showPinModal = ref(false);
const showFinalModal = ref(false);

// 기존 select의 value가 따로 있었다면 이 값만 기존 value에 맞춰 유지
const withdrawalReasons = [
  '서비스를 자주 이용하지 않아요',
  '서비스 이용이 불편해요',
  '개인정보가 걱정돼요',
  '새로운 계정으로 다시 가입하고 싶어요',
  '기타',
];

// 회원탈퇴 시작 가능 여부
const canStartWithdraw = computed(() => !!withdrawalReason.value && agreed.value);

// 탈퇴 사유 Bottom Sheet 열기
const openReasonSheet = () => {
  if (loading.value) return;
  showReasonSheet.value = true;
};

// 탈퇴 사유 Bottom Sheet 닫기
const closeReasonSheet = () => {
  showReasonSheet.value = false;
};

// 탈퇴 사유 선택
const selectReason = (reason) => {
  withdrawalReason.value = reason;
  showReasonSheet.value = false;
};

// PIN Modal 열기
const openPinModal = async () => {
  if (!canStartWithdraw.value || loading.value) return;

  pinPassword.value = '';
  pinErrorMessage.value = '';
  withdrawErrorMessage.value = '';
  showPinModal.value = true;

  await focusPinInput();
};

// PIN Modal 닫기
const closePinModal = () => {
  if (pinVerifying.value) return;

  showPinModal.value = false;
  pinPassword.value = '';
  pinErrorMessage.value = '';
};

// PIN 입력창 포커스
const focusPinInput = async () => {
  if (pinVerifying.value) return;

  await nextTick();
  pinInput.value?.focus();
};

// PIN 입력
const changePin = (event) => {
  const value = event.target.value.replace(/[^0-9]/g, '').slice(0, 6);

  pinPassword.value = value;
  pinErrorMessage.value = '';

  if (event.target.value !== value) event.target.value = value;
};

// PIN 검증
const verifyPinPassword = async () => {
  if (pinPassword.value.length !== 6 || pinVerifying.value) return;

  try {
    pinVerifying.value = true;
    pinErrorMessage.value = '';

    const response = await verifyPin(pinPassword.value);

    if (!response.verified) {
      pinPassword.value = '';
      pinErrorMessage.value = response.message || '간편비밀번호가 일치하지 않습니다.';
      await focusPinInput();
      return;
    }

    showPinModal.value = false;
    showFinalModal.value = true;
  } catch (error) {
    console.error(error);

    pinPassword.value = '';
    pinErrorMessage.value = error.response?.data?.message || '간편비밀번호가 일치하지 않습니다.';

    await focusPinInput();
  } finally {
    pinVerifying.value = false;
  }
};

// 최종 확인 Modal 닫기
const closeFinalModal = () => {
  if (loading.value) return;

  showFinalModal.value = false;
  withdrawErrorMessage.value = '';
};

// 회원탈퇴
const withdraw = async () => {
  if (!canStartWithdraw.value || pinPassword.value.length !== 6 || loading.value) return;

  if (!authStore.userId) {
    await router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    withdrawErrorMessage.value = '';

    await withdrawUser({
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
    withdrawErrorMessage.value = error.response?.data?.message || '회원탈퇴에 실패했습니다. 다시 시도해주세요.';
  } finally {
    loading.value = false;
  }
};

// 이전 화면
const goBack = () => {
  router.back();
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.withdraw-page {
  position: relative;
  background: var(--color-bg-page);
}

.withdraw-container {
  overflow-x: hidden;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.withdraw-container::-webkit-scrollbar {
  display: none;
}

.withdraw-content {
  width: 100%;
  padding: 26px 0 24px;
  box-sizing: border-box;
}

/* 상단 안내 */
.intro-section {
  text-align: center;
}

.warning-icon {
  display: flex;
  width: 56px;
  height: 56px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 18px;
  border-radius: 19px;
  background: rgba(229, 57, 53, 0.08);
  color: var(--color-error);
  font-size: 23px;
}

.intro-section h2 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.4;
  letter-spacing: -0.6px;
}

.description {
  margin: 12px 0 0;
  color: var(--color-text-muted);
  line-height: 1.6;
}

/* 탈퇴 사유 */
.reason-area {
  margin-top: 30px;
}

.section-label {
  display: block;
  margin-bottom: 10px;
  color: var(--color-text-main);
}

.reason-select {
  display: flex;
  width: 100%;
  height: 54px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 16px;
  border: 1px solid var(--color-border-main);
  border-radius: 13px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  box-sizing: border-box;
  cursor: pointer;
}

.reason-select.selected {
  border-color: var(--color-text-sub);
}

.reason-select {
  display: flex;
  width: 100%;
  height: 54px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 16px;
  border: 1px solid var(--color-border-main);
  border-radius: 13px;
  background: var(--color-bg-page);
  box-sizing: border-box;
  cursor: pointer;
}

.reason-value,
.reason-placeholder {
  min-width: 0;
  flex: 1;
  font-family: inherit;
  font-size: 15px;
  font-weight: 500;
  line-height: 1.4;
  text-align: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reason-value {
  color: var(--color-text-main);
}

.reason-placeholder {
  color: var(--color-text-muted);
}

.reason-select i {
  flex: none;
  color: var(--color-text-sub);
  font-size: 13px;
}

.reason-select i {
  flex: none;
  color: var(--color-text-sub);
  font-size: 13px;
}

/* 탈퇴 경고 */
.warning-area {
  margin-top: 22px;
  padding: 17px 16px;
  border: 1px solid rgba(229, 57, 53, 0.18);
  border-radius: 16px;
  background: rgba(229, 57, 53, 0.035);
}

.warning-title {
  display: flex;
  align-items: center;
  gap: 9px;
  color: var(--color-error);
}

.warning-title > span {
  display: flex;
  width: 25px;
  height: 25px;
  flex: none;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(229, 57, 53, 0.1);
  font-size: 13px;
}

.warning-area ul {
  margin: 12px 0 0;
  padding-left: 20px;
  color: var(--color-text-sub);
  font-size: 12px;
  font-weight: 400;
  line-height: 1.5;
}

.warning-area li + li {
  margin-top: 3px;
}

/* 동의 */
.agreement-check {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 20px;
  padding: 15px;
  border: 1px solid var(--color-divider);
  border-radius: 14px;
  background: var(--color-bg-screen);
  box-sizing: border-box;
  cursor: pointer;
}

.agreement-check.checked {
  border-color: rgba(229, 57, 53, 0.2);
  background: rgba(229, 57, 53, 0.035);
}

.agreement-check input {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
}

.custom-checkbox {
  display: flex;
  width: 22px;
  height: 22px;
  flex: none;
  align-items: center;
  justify-content: center;
  border: 1.5px solid var(--color-border-main);
  border-radius: 6px;
  background: var(--color-bg-page);
  color: var(--color-text-white);
  font-size: 12px;
}

.agreement-check.checked .custom-checkbox {
  border-color: var(--color-error);
  background: var(--color-error);
}

.agreement-text {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.agreement-text strong {
  color: var(--color-text-main);
  line-height: 1.45;
}

.agreement-text > span {
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 400;
}

/* 하단 버튼 */
.withdraw-bottom-area {
  position: relative;
  z-index: 5;
  flex-shrink: 0;
  background: var(--color-bg-page);
}

.withdraw-main-button {
  width: 100%;
  height: 52px;
  border: 0;
  border-radius: 14px;
  background: var(--color-error);
  color: var(--color-text-white);
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
}

.withdraw-main-button:disabled {
  background: var(--color-bg-disabled);
  color: var(--color-text-disabled);
  cursor: not-allowed;
}

/* Overlay */
.overlay {
  position: fixed;
  z-index: 100;
  inset: 0;
  background: rgba(17, 17, 17, 0.44);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
}

/* 탈퇴 사유 Bottom Sheet */
.sheet-overlay {
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.reason-sheet {
  width: 100%;
  max-width: 430px;
  max-height: 72dvh;
  padding: 10px 24px 30px;
  border-radius: 28px 28px 0 0;
  background: var(--color-bg-page);
  box-sizing: border-box;
  box-shadow: 0 -16px 40px rgba(0, 0, 0, 0.13);
  overflow-y: auto;
}

.sheet-handle {
  width: 42px;
  height: 4px;
  margin: 0 auto 22px;
  border-radius: 999px;
  background: var(--color-border-main);
}

.reason-sheet-header h3 {
  margin: 0;
  color: var(--color-text-main);
}

.reason-sheet-header p {
  margin: 7px 0 0;
  color: var(--color-text-muted);
  font-weight: 400;
}

.reason-list {
  margin-top: 20px;
}

.reason-item {
  display: flex;
  width: 100%;
  min-height: 54px;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 13px 8px;
  border: 0;
  border-bottom: 1px solid var(--color-divider);
  background: transparent;
  color: var(--color-text-main);
  font-size: 15px;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
}

.reason-item:last-child {
  border-bottom: 0;
}

.reason-item.selected {
  color: var(--color-error);
  font-weight: 600;
}

.reason-item i {
  color: var(--color-error);
  font-size: 13px;
}

/* 중앙 Modal */
.modal-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
}

.pin-modal,
.final-modal {
  width: 100%;
  max-width: 360px;
  padding: 26px 22px 22px;
  border-radius: 22px;
  background: var(--color-bg-page);
  box-sizing: border-box;
  text-align: center;
  box-shadow: 0 22px 60px rgba(0, 0, 0, 0.2);
}

/* PIN Modal */
.pin-icon {
  display: flex;
  width: 52px;
  height: 52px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 17px;
  border-radius: 17px;
  background: #fff4d7;
  color: var(--color-primary-active);
  font-size: 20px;
}

.pin-modal h3 {
  margin: 0;
  color: var(--color-text-main);
}

.pin-description {
  margin: 9px 0 0;
  color: var(--color-text-muted);
  font-weight: 400;
  line-height: 1.55;
}

.pin-boxes {
  position: relative;
  display: grid;
  width: 100%;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 7px;
  margin-top: 25px;
  cursor: text;
}

.pin-box {
  display: flex;
  height: 48px;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-border-main);
  border-radius: 11px;
  background: var(--color-bg-screen);
  box-sizing: border-box;
}

.pin-box.active {
  border-color: var(--color-primary);
  background: #fffaf0;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.1);
}

.pin-box.filled {
  border-color: var(--color-primary);
  background: #fff8e5;
}

.pin-boxes.error .pin-box {
  border-color: var(--color-error);
  background: var(--color-bg-page);
  box-shadow: 0 0 0 3px rgba(229, 57, 53, 0.07);
}

.pin-dot {
  width: 10px;
  height: 10px;
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

.pin-message-area {
  min-height: 40px;
  margin-top: 10px;
}

.pin-error-message,
.pin-helper-message {
  margin: 0;
  font-size: 13px;
  font-weight: 400;
  line-height: 1.45;
}

.pin-error-message {
  color: var(--color-error);
}

.pin-helper-message {
  color: var(--color-text-muted);
}

.pin-confirm-button {
  width: 100%;
  height: 48px;
  border: 0;
  border-radius: 12px;
  background: var(--color-primary);
  color: var(--color-text-main);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
}

.pin-confirm-button:disabled {
  background: var(--color-bg-disabled);
  color: var(--color-text-disabled);
}

/* 최종 확인 */
.final-warning-icon {
  display: flex;
  width: 54px;
  height: 54px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 18px;
  border-radius: 18px;
  background: rgba(229, 57, 53, 0.08);
  color: var(--color-error);
  font-size: 22px;
}

.final-modal h3 {
  margin: 0;
  color: var(--color-text-main);
}

.final-description {
  margin: 10px 0 0;
  color: var(--color-text-muted);
  font-weight: 400;
  line-height: 1.6;
}

.final-reason {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-top: 19px;
  padding: 13px;
  border-radius: 12px;
  background: var(--color-bg-screen);
  text-align: left;
}

.final-reason span {
  flex: none;
  color: var(--color-text-muted);
  font-size: 13px;
}

.final-reason strong {
  color: var(--color-text-main);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.45;
  text-align: right;
}

.withdraw-error-message {
  margin: 12px 0 0;
  color: var(--color-error);
  font-weight: 400;
  line-height: 1.45;
}

.final-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 22px;
}

.final-cancel-button,
.final-withdraw-button {
  height: 46px;
  border-radius: 11px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.final-cancel-button {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
  color: var(--color-text-main);
}

.final-withdraw-button {
  border: 1px solid var(--color-error);
  background: var(--color-error);
  color: var(--color-text-white);
}

/* 로딩 */
.loading-overlay {
  position: fixed;
  z-index: 120;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.9);
}

.loading-spinner {
  width: 34px;
  height: 34px;
  border: 4px solid var(--color-bg-disabled);
  border-top-color: var(--color-error);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* Bottom Sheet Transition */
.sheet-enter-active,
.sheet-leave-active {
  transition: opacity 0.22s ease;
}

.sheet-enter-active .reason-sheet,
.sheet-leave-active .reason-sheet {
  transition: transform 0.28s cubic-bezier(0.22, 1, 0.36, 1);
}

.sheet-enter-from,
.sheet-leave-to {
  opacity: 0;
}

.sheet-enter-from .reason-sheet,
.sheet-leave-to .reason-sheet {
  transform: translateY(100%);
}

/* Modal Transition */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.2s ease;
}

.modal-enter-active .pin-modal,
.modal-leave-active .pin-modal,
.modal-enter-active .final-modal,
.modal-leave-active .final-modal {
  transition: opacity 0.2s ease, transform 0.22s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .pin-modal,
.modal-leave-to .pin-modal,
.modal-enter-from .final-modal,
.modal-leave-to .final-modal {
  opacity: 0;
  transform: scale(0.94) translateY(8px);
}

@media (max-width: 360px) {
  .reason-sheet {
    padding-right: 20px;
    padding-left: 20px;
  }

  .pin-boxes {
    gap: 5px;
  }

  .pin-box {
    height: 44px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .sheet-enter-active,
  .sheet-leave-active,
  .sheet-enter-active .reason-sheet,
  .sheet-leave-active .reason-sheet,
  .modal-enter-active,
  .modal-leave-active,
  .modal-enter-active .pin-modal,
  .modal-leave-active .pin-modal,
  .modal-enter-active .final-modal,
  .modal-leave-active .final-modal {
    transition: none;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>