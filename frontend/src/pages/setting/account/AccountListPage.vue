<template>
  <main class="page-layout account-page" @click="closeMenu">
    <PageHeader
        title="연결 계좌 관리"
        custom-back
        @back="goBack"
    />

    <div class="page-content">
      <section class="title-section">
        <h2 class="text-26-bold">
          연결된 계좌를 관리해 보세요
        </h2>

        <p class="text-15">
          연결 계좌는 송금, 출금, 자산 조회 등에<br />
          사용할 수 있어요.
        </p>
      </section>

      <section class="account-section">
        <div class="account-summary">
          <div class="account-count">
            <span class="text-13-bold">연결 계좌</span>
            <strong class="text-15-bold">{{ accountStore.accounts.length }}</strong>
          </div>

          <button
              class="refresh-button"
              :disabled="loading"
              type="button"
              aria-label="계좌 목록 새로고침"
              @click.stop="loadAccounts"
          >
            <i class="fa-solid fa-rotate-right" :class="{ rotating: loading }"></i>
          </button>
        </div>

        <div class="account-list-area">
          <p v-if="loading" class="state-message text-13">
            계좌 목록을 불러오고 있어요.
          </p>

          <div v-else-if="errorMessage" class="error-area">
            <p class="text-13">{{ errorMessage }}</p>

            <button class="retry-button text-13-bold" type="button" @click="loadAccounts">
              다시 불러오기
            </button>
          </div>

          <div v-else-if="accountStore.accounts.length === 0" class="empty-area">
            <div class="empty-icon">
              <i class="fa-solid fa-building-columns"></i>
            </div>

            <strong class="text-18-bold">연결된 계좌가 없어요</strong>

            <p class="text-13">
              계좌를 연결하면 송금과 자산 조회 서비스를<br />
              편리하게 이용할 수 있어요.
            </p>
          </div>

          <div v-else class="account-list">
            <article
                v-for="account in accountStore.accounts"
                :key="account.linkedAccountId"
                class="account-item"
            >
              <div class="bank-logo-area">
                <img
                    v-if="account.bankLogoUrl"
                    :alt="account.bankName"
                    :src="account.bankLogoUrl"
                    class="bank-logo"
                />

                <div v-else class="bank-logo-fallback text-15-bold">
                  {{ getBankInitial(account.bankName) }}
                </div>
              </div>

              <div class="account-info">
                <div class="account-title">
                  <strong class="text-15-bold">{{ account.bankName }}</strong>

                  <span v-if="account.primaryYn === 'Y'" class="primary-badge">
                    대표계좌
                  </span>
                </div>

                <p class="text-13">{{ maskAccountNumber(account.accountNumber) }}</p>
              </div>

              <button
                  v-if="accountStore.accounts.length > 1"
                  class="menu-button"
                  type="button"
                  aria-label="계좌 관리 메뉴"
                  @click.stop="toggleMenu(account.linkedAccountId)"
              >
                <i class="fa-solid fa-ellipsis-vertical"></i>
              </button>

              <div
                  v-if="openedAccountId === account.linkedAccountId"
                  class="account-menu"
                  @click.stop
              >
                <button
                    v-if="account.primaryYn !== 'Y'"
                    class="text-13"
                    type="button"
                    @click="changePrimary(account)"
                >
                  <span class="menu-icon">
                    <i class="fa-regular fa-star"></i>
                  </span>
                  대표계좌 설정
                </button>

                <button
                    class="delete-button text-13"
                    type="button"
                    @click="openDisconnectModal(account)"
                >
                  <span class="menu-icon">
                    <i class="fa-solid fa-link-slash"></i>
                  </span>
                  계좌 연결 해제
                </button>
              </div>
            </article>
          </div>
        </div>
      </section>

      <p v-if="toastMessage" class="toast-message text-13-bold">
        <span class="toast-icon">
          <i class="fa-solid fa-check"></i>
        </span>
        {{ toastMessage }}
      </p>

      <button class="content-add-btn connect-button" type="button" @click="goConnect">
        <i class="fa-solid fa-plus"></i>
        계좌 연결하기
      </button>

      <div
          v-if="disconnectTarget"
          class="modal-overlay"
          @click.self="closeDisconnectModal"
      >
        <section class="disconnect-modal">
          <div class="warning-icon">
            <i class="fa-solid fa-exclamation"></i>
          </div>

          <h3 class="text-20-bold">계좌 연결을 해제할까요?</h3>

          <article class="selected-account">
            <div class="selected-bank-logo">
              <img
                  v-if="disconnectTarget.bankLogoUrl"
                  :alt="disconnectTarget.bankName"
                  :src="disconnectTarget.bankLogoUrl"
              />

              <span v-else class="text-15-bold">
                {{ getBankInitial(disconnectTarget.bankName) }}
              </span>
            </div>

            <div class="selected-account-info">
              <strong class="text-15-bold">{{ disconnectTarget.bankName }}</strong>
              <p class="text-13">{{ maskAccountNumber(disconnectTarget.accountNumber) }}</p>
            </div>
          </article>

          <p class="modal-guide text-13">
            연결을 해제하면 송금, 자동이체,<br />
            자산조회에 사용할 수 없어요.
          </p>

          <div class="modal-button-area">
            <button
                class="modal-cancel-button text-15-bold"
                type="button"
                @click="closeDisconnectModal"
            >
              취소
            </button>

            <button
                class="modal-delete-button text-15-bold"
                type="button"
                @click="removeAccount"
            >
              연결 해제
            </button>
          </div>
        </section>
      </div>
    </div>
  </main>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { disconnectAccount, getAccounts, setPrimaryAccount } from '@/api/accountApi';
import { useAccountStore } from '@/stores/account';
import PageHeader from '@/components/common/PageHeader.vue';

const router = useRouter();
const accountStore = useAccountStore();

const loading = ref(false);
const errorMessage = ref('');
const toastMessage = ref('');
const openedAccountId = ref(null);
const disconnectTarget = ref(null);

let toastTimer = null;

// 계좌번호 마스킹
const maskAccountNumber = (accountNumber) => {
  if (!accountNumber) return '';

  const value = String(accountNumber).replace(/[^0-9]/g, '');

  if (value.length <= 4) return value;

  return `${value.slice(0, 3)}-${'*'.repeat(Math.max(value.length - 7, 1))}-${value.slice(-4)}`;
};

// 은행명 첫 글자
const getBankInitial = (bankName) => {
  if (!bankName) return 'B';

  return bankName.charAt(0);
};

// 계좌 목록 조회
const loadAccounts = async () => {
  try {
    loading.value = true;
    errorMessage.value = '';
    openedAccountId.value = null;

    const data = await getAccounts();
    const accounts = Array.isArray(data) ? data : data.accounts || [];

    accountStore.setAccounts(accounts);
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '계좌 목록을 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 계좌 관리 메뉴 열기
const toggleMenu = (linkedAccountId) => {
  openedAccountId.value = openedAccountId.value === linkedAccountId ? null : linkedAccountId;
};

// 계좌 관리 메뉴 닫기
const closeMenu = () => {
  openedAccountId.value = null;
};

// 대표계좌 변경
const changePrimary = async (account) => {
  try {
    errorMessage.value = '';

    await setPrimaryAccount(account.linkedAccountId);

    openedAccountId.value = null;

    await loadAccounts();

    showToast('대표계좌가 변경되었습니다.');
  } catch (error) {
    console.error(error);

    showToast(
        error.response?.data?.message || '대표계좌 변경에 실패했습니다.'
    );
  }
};

// 계좌 연결 해제 모달 열기
const openDisconnectModal = (account) => {
  openedAccountId.value = null;
  disconnectTarget.value = account;
};

// 계좌 연결 해제 모달 닫기
const closeDisconnectModal = () => {
  disconnectTarget.value = null;
};

// 계좌 연결 해제
const removeAccount = async () => {
  if (!disconnectTarget.value) return;

  try {
    errorMessage.value = '';

    await disconnectAccount(disconnectTarget.value.linkedAccountId);

    disconnectTarget.value = null;

    await loadAccounts();

    showToast('계좌 연결이 해제되었습니다.');
  } catch (error) {
    console.error(error);

    showToast(
        error.response?.data?.message || '계좌 연결 해제에 실패했습니다.'
    );
  }
};

// 토스트 메시지 표시
const showToast = (message) => {
  toastMessage.value = message;

  if (toastTimer) window.clearTimeout(toastTimer);

  toastTimer = window.setTimeout(() => {
    toastMessage.value = '';
  }, 2000);
};

// 계좌 연결 화면 이동
const goConnect = async () => {
  await router.push('/setting/account/connect');
};

// 이전 화면
const goBack = () => {
  router.push('/setting');
};

onMounted(() => {
  loadAccounts();
});

onBeforeUnmount(() => {
  if (toastTimer) window.clearTimeout(toastTimer);
});
</script>

<style scoped>
.account-page {
  position: relative;
  background: var(--color-bg-page);
  overflow: visible;
}

.title-section {
  flex-shrink: 0;
  margin-top: 38px;
}

.title-section h2 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.title-section p {
  margin: 14px 0 0;
  color: var(--color-text-muted);
  line-height: 1.55;
}

.account-section {
  position: relative;
  min-height: 0;
  margin-top: 32px;
  overflow: visible;
}

.account-summary {
  display: flex;
  height: 52px;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  border: 1px solid var(--color-divider);
  border-bottom: 0;
  border-radius: 14px 14px 0 0;
  background: var(--color-bg-page);
  box-sizing: border-box;
}

.account-count {
  display: flex;
  align-items: center;
  gap: 7px;
}

.account-count span {
  color: var(--color-text-main);
}

.account-count strong {
  color: var(--color-primary-active);
}

.refresh-button {
  display: flex;
  width: 32px;
  height: 32px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--color-text-sub);
  font-size: 16px;
  cursor: pointer;
}

.refresh-button:active:not(:disabled) {
  background: var(--color-bg-screen);
}

.refresh-button:disabled {
  cursor: default;
}

.rotating {
  animation: rotate 0.8s linear infinite;
}

.account-list-area {
  position: relative;
  border: 1px solid var(--color-divider);
  border-radius: 0 0 14px 14px;
  background: var(--color-bg-page);
  overflow: visible;
}

.account-list {
  display: flex;
  flex-direction: column;
  overflow: visible;
}

.account-item {
  position: relative;
  display: flex;
  min-height: 80px;
  align-items: center;
  gap: 13px;
  padding: 13px;
  border-bottom: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  box-sizing: border-box;
  overflow: visible;
}

.account-item:last-child {
  border-bottom: 0;
}

.bank-logo-area {
  display: flex;
  width: 48px;
  height: 48px;
  flex: none;
  align-items: center;
  justify-content: center;
}

.bank-logo {
  display: block;
  width: 46px;
  height: 46px;
  object-fit: contain;
}

.bank-logo-fallback {
  display: flex;
  width: 44px;
  height: 44px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-text-main);
}

.account-info {
  min-width: 0;
  flex: 1;
}

.account-title {
  display: flex;
  align-items: center;
  gap: 7px;
}

.account-title strong {
  overflow: hidden;
  color: var(--color-text-main);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.primary-badge {
  flex: none;
  padding: 3px 7px;
  border: 1px solid var(--color-primary);
  border-radius: 8px;
  background: #fff9e9;
  color: var(--color-primary-active);
  font-size: 9px;
  font-weight: 600;
}

.account-info p {
  margin: 5px 0 0;
  color: var(--color-text-muted);
}

.menu-button {
  display: flex;
  width: 34px;
  height: 42px;
  flex: none;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--color-text-main);
  font-size: 18px;
  cursor: pointer;
}

.menu-button:active {
  background: var(--color-bg-screen);
}

.account-menu {
  position: absolute;
  z-index: 30;
  top: 58px;
  right: 13px;
  display: flex;
  width: 150px;
  flex-direction: column;
  padding: 6px;
  border: 1px solid var(--color-divider);
  border-radius: 11px;
  background: var(--color-bg-page);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.15);
  box-sizing: border-box;
}

.account-menu button {
  display: flex;
  min-height: 42px;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--color-text-main);
  text-align: left;
  cursor: pointer;
}

.account-menu button:hover {
  background: var(--color-bg-screen);
}

.account-menu .delete-button {
  color: var(--color-error);
}

.menu-icon {
  display: inline-flex;
  width: 18px;
  justify-content: center;
  font-size: 14px;
}

.empty-area {
  padding: 52px 20px 48px;
  text-align: center;
}

.empty-icon {
  display: flex;
  width: 68px;
  height: 68px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 21px;
  border-radius: 22px;
  background: #fff4d6;
  color: var(--color-primary-active);
  font-size: 28px;
}

.empty-area strong {
  display: block;
  color: var(--color-text-main);
}

.empty-area p {
  margin: 11px 0 0;
  color: var(--color-text-muted);
  line-height: 1.55;
}

.state-message {
  margin: 0;
  padding: 70px 20px;
  color: var(--color-text-sub);
  text-align: center;
}

.error-area {
  padding: 54px 20px;
  text-align: center;
}

.error-area p {
  margin: 0;
  color: var(--color-error);
  line-height: 1.5;
}

.retry-button {
  margin-top: 15px;
  padding: 8px 14px;
  border: 1px solid var(--color-border-main);
  border-radius: 9px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  cursor: pointer;
}

.retry-button:active {
  background: var(--color-bg-screen);
}

.connect-button {
  position: absolute;
  right: 24px;
  bottom: 32px;
  left: 24px;
  display: flex;
  width: auto;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.connect-button i {
  font-size: 16px;
}

.toast-message {
  position: absolute;
  z-index: 50;
  right: 24px;
  bottom: 106px;
  left: 24px;
  display: flex;
  align-items: center;
  gap: 9px;
  margin: 0;
  padding: 14px 16px;
  border: 1px solid rgba(34, 197, 94, 0.18);
  border-radius: 12px;
  background: rgba(34, 197, 94, 0.12);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #15803d;
  box-sizing: border-box;
  box-shadow: 0 4px 14px rgba(34, 197, 94, 0.08);
  animation: toast-in 0.25s ease-out;
}

.toast-icon {
  display: inline-flex;
  width: 20px;
  height: 20px;
  flex: none;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(34, 197, 94, 0.16);
  color: #16a34a;
  font-size: 10px;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-overlay {
  position: absolute;
  z-index: 100;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.45);
  box-sizing: border-box;
}

.disconnect-modal {
  width: 100%;
  max-width: 330px;
  padding: 28px 20px 20px;
  border-radius: 18px;
  background: var(--color-bg-page);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.18);
  box-sizing: border-box;
}

.warning-icon {
  display: flex;
  width: 52px;
  height: 52px;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  border-radius: 50%;
  background: #fff1d4;
  color: var(--color-primary-active);
  font-size: 22px;
}

.disconnect-modal h3 {
  margin: 20px 0 0;
  color: var(--color-text-main);
  text-align: center;
}

.selected-account {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-top: 24px;
  padding: 14px;
  border: 1px solid var(--color-border-main);
  border-radius: 12px;
  box-sizing: border-box;
}

.selected-bank-logo {
  display: flex;
  width: 48px;
  height: 48px;
  flex: none;
  align-items: center;
  justify-content: center;
}

.selected-bank-logo img {
  width: 46px;
  height: 46px;
  object-fit: contain;
}

.selected-bank-logo span {
  display: flex;
  width: 44px;
  height: 44px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-text-main);
}

.selected-account-info {
  min-width: 0;
}

.selected-account-info strong {
  display: block;
  color: var(--color-text-main);
}

.selected-account-info p {
  margin: 5px 0 0;
  color: var(--color-text-muted);
}

.modal-guide {
  margin: 20px 0 0;
  color: var(--color-text-sub);
  line-height: 1.6;
  text-align: center;
}

.modal-button-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-top: 24px;
}

.modal-cancel-button,
.modal-delete-button {
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
}

.modal-cancel-button {
  border: 1px solid var(--color-border-main);
  background: var(--color-bg-page);
  color: var(--color-text-main);
}

.modal-delete-button {
  border: 1px solid #ffc7c7;
  background: #ffe7e7;
  color: var(--color-error);
}

.modal-cancel-button:active {
  background: var(--color-bg-screen);
}

@keyframes rotate {
  to {
    transform: rotate(360deg);
  }
}
</style>