<template>
  <div class="account-page">
    <main class="account-container" @click="closeMenu">
      <header class="page-header">
        <button class="back-button" type="button" aria-label="이전 화면" @click="goBack">
          &lt;
        </button>

        <h1>연결 계좌 관리</h1>
        <div class="header-empty"></div>
      </header>

      <section class="title-section">
        <h2>
          연결된 계좌를<br />
          확인하고 관리해 보세요
        </h2>

        <p>
          연결 계좌는 송금, 출금, 자산 조회 등에<br />
          사용할 수 있어요.
        </p>
      </section>

      <section class="account-section">
        <div class="account-summary">
          <div class="account-count">
            <span>연결 계좌</span>
            <strong>{{ accountStore.accountCount }}</strong>
          </div>

          <button
              class="refresh-button"
              :disabled="loading"
              type="button"
              aria-label="계좌 목록 새로고침"
              @click.stop="loadAccounts"
          >
            <span :class="{ rotating: loading }">↻</span>
          </button>
        </div>

        <div class="account-list-area">
          <p v-if="loading" class="state-message">
            계좌 목록을 불러오고 있어요.
          </p>

          <div v-else-if="errorMessage" class="error-area">
            <p>{{ errorMessage }}</p>

            <button type="button" @click="loadAccounts">
              다시 불러오기
            </button>
          </div>

          <div v-else-if="accountStore.accounts.length === 0" class="empty-area">
            <div class="empty-icon">🏦</div>

            <strong>연결된 계좌가 없어요</strong>

            <p>
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

                <div v-else class="bank-logo-fallback">
                  {{ getBankInitial(account.bankName) }}
                </div>
              </div>

              <div class="account-info">
                <div class="account-title">
                  <strong>{{ account.bankName }}</strong>

                  <span v-if="account.primaryYn === 'Y'" class="primary-badge">
                    대표계좌
                  </span>
                </div>

                <p>{{ maskAccountNumber(account.accountNumber) }}</p>
              </div>

              <button
                  v-if="accountStore.accounts.length > 1"
                  class="menu-button"
                  type="button"
                  aria-label="계좌 관리 메뉴"
                  @click.stop="toggleMenu(account.linkedAccountId)"
              >
                ⋮
              </button>

              <div
                  v-if="openedAccountId === account.linkedAccountId"
                  class="account-menu"
                  @click.stop
              >
                <button
                    v-if="account.primaryYn !== 'Y'"
                    type="button"
                    @click="changePrimary(account)"
                >
                  <span class="menu-icon">☆</span>
                  대표계좌 설정
                </button>

                <button
                    class="delete-button"
                    type="button"
                    @click="openDisconnectModal(account)"
                >
                  <span class="menu-icon">♲</span>
                  계좌 연결 해제
                </button>
              </div>
            </article>
          </div>
        </div>
      </section>

      <p v-if="toastMessage" class="toast-message">
        <span class="toast-icon">✓</span>
        {{ toastMessage }}
      </p>

      <button class="connect-button" type="button" @click="goConnect">
        <span>＋</span>
        계좌 연결하기
      </button>

      <div
          v-if="disconnectTarget"
          class="modal-overlay"
          @click.self="closeDisconnectModal"
      >
        <section class="disconnect-modal">
          <div class="warning-icon">!</div>

          <h3>계좌 연결을 해제할까요?</h3>

          <article class="selected-account">
            <div class="selected-bank-logo">
              <img
                  v-if="disconnectTarget.bankLogoUrl"
                  :alt="disconnectTarget.bankName"
                  :src="disconnectTarget.bankLogoUrl"
              />

              <span v-else>
                {{ getBankInitial(disconnectTarget.bankName) }}
              </span>
            </div>

            <div class="selected-account-info">
              <strong>{{ disconnectTarget.bankName }}</strong>
              <p>{{ maskAccountNumber(disconnectTarget.accountNumber) }}</p>
            </div>
          </article>

          <p class="modal-guide">
            연결을 해제하면 송금, 자동이체,<br />
            자산조회에 사용할 수 없어요.
          </p>

          <div class="modal-button-area">
            <button
                class="modal-cancel-button"
                type="button"
                @click="closeDisconnectModal"
            >
              취소
            </button>

            <button
                class="modal-delete-button"
                type="button"
                @click="removeAccount"
            >
              연결 해제
            </button>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { disconnectAccount, getAccounts, setPrimaryAccount } from '@/api/accountApi';
import { useAccountStore } from '@/stores/account';

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
  const userId = accountStore.userId;

  if (!userId) {
    await router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';
    openedAccountId.value = null;

    const data = await getAccounts(userId);
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

    await setPrimaryAccount(accountStore.userId, account.linkedAccountId);

    openedAccountId.value = null;

    await loadAccounts();

    showToast('대표계좌가 변경되었습니다.');
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '대표계좌 변경에 실패했습니다.';
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

    await disconnectAccount(accountStore.userId, disconnectTarget.value.linkedAccountId);

    disconnectTarget.value = null;

    await loadAccounts();

    showToast('계좌 연결이 해제되었습니다.');
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '계좌 연결 해제에 실패했습니다.';
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
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.account-container {
  position: relative;
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  padding: 10px 28px 140px;
  background: #ffffff;
  box-sizing: border-box;
  overflow: visible;
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

.title-section {
  flex-shrink: 0;
  margin-top: 38px;
}

.title-section h2 {
  margin: 0;
  color: #111111;
  font-size: 25px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.title-section p {
  margin: 14px 0 0;
  color: #999999;
  font-size: 12px;
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
  border: 1px solid #e5e5e5;
  border-bottom: 0;
  border-radius: 14px 14px 0 0;
  background: #ffffff;
  box-sizing: border-box;
}

.account-count {
  display: flex;
  align-items: center;
  gap: 7px;
}

.account-count span {
  color: #333333;
  font-size: 13px;
  font-weight: 700;
}

.account-count strong {
  color: #e99b00;
  font-size: 14px;
  font-weight: 800;
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
  color: #777777;
  font-size: 23px;
  cursor: pointer;
}

.refresh-button:active {
  background: #f5f5f5;
}

.refresh-button:disabled {
  cursor: default;
}

.rotating {
  display: inline-block;
  animation: rotate 0.8s linear infinite;
}

.account-list-area {
  position: relative;
  border: 1px solid #e5e5e5;
  border-radius: 0 0 14px 14px;
  background: #ffffff;
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
  border-bottom: 1px solid #eeeeee;
  background: #ffffff;
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
  background: #ffbc2e;
  color: #222222;
  font-size: 16px;
  font-weight: 800;
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
  color: #222222;
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.primary-badge {
  flex: none;
  padding: 3px 7px;
  border: 1px solid #ffbc2e;
  border-radius: 8px;
  background: #fff9e9;
  color: #d78d00;
  font-size: 9px;
  font-weight: 800;
}

.account-info p {
  margin: 5px 0 0;
  color: #888888;
  font-size: 11px;
}

.menu-button {
  flex: none;
  width: 34px;
  height: 42px;
  padding: 0;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: #333333;
  font-size: 23px;
  line-height: 1;
  cursor: pointer;
}

.menu-button:active {
  background: #f5f5f5;
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
  border: 1px solid #eeeeee;
  border-radius: 11px;
  background: #ffffff;
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
  color: #333333;
  font-size: 12px;
  font-weight: 600;
  text-align: left;
  cursor: pointer;
}

.account-menu button:hover {
  background: #f7f7f7;
}

.account-menu .delete-button {
  color: #e53935;
}

.menu-icon {
  display: inline-flex;
  width: 18px;
  justify-content: center;
  font-size: 15px;
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
  font-size: 31px;
}

.empty-area strong {
  display: block;
  color: #222222;
  font-size: 17px;
  font-weight: 800;
}

.empty-area p {
  margin: 11px 0 0;
  color: #999999;
  font-size: 12px;
  line-height: 1.55;
}

.state-message {
  margin: 0;
  padding: 70px 20px;
  color: #777777;
  font-size: 13px;
  text-align: center;
}

.error-area {
  padding: 54px 20px;
  text-align: center;
}

.error-area p {
  margin: 0;
  color: #e53935;
  font-size: 13px;
  line-height: 1.5;
}

.error-area button {
  margin-top: 15px;
  padding: 8px 14px;
  border: 1px solid #dddddd;
  border-radius: 9px;
  background: #ffffff;
  color: #555555;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.connect-button {
  position: absolute;
  right: 28px;
  bottom: 58px;
  left: 28px;
  display: flex;
  width: auto;
  height: 58px;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin: 0;
  border: 1px dashed #cccccc;
  border-radius: 10px;
  background: #ffffff;
  color: #222222;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

.connect-button span {
  font-size: 21px;
  font-weight: 500;
}

.connect-button:active {
  border-color: #ffbc2e;
  background: #fffaf0;
}

.toast-message {
  position: absolute;
  z-index: 50;
  right: 28px;
  bottom: 128px;
  left: 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  padding: 14px 15px;
  border-radius: 10px;
  background: rgba(30, 30, 30, 0.9);
  color: #ffffff;
  font-size: 12px;
  font-weight: 600;
  box-sizing: border-box;
}

.toast-icon {
  display: inline-flex;
  width: 18px;
  height: 18px;
  flex: none;
  align-items: center;
  justify-content: center;
  border: 1px solid #ffffff;
  border-radius: 50%;
  color: #ffffff;
  font-size: 11px;
  font-weight: 800;
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
  background: #ffffff;
  box-sizing: border-box;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.18);
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
  color: #ffae00;
  font-size: 26px;
  font-weight: 800;
}

.disconnect-modal h3 {
  margin: 20px 0 0;
  color: #111111;
  font-size: 19px;
  font-weight: 800;
  text-align: center;
}

.selected-account {
  display: flex;
  align-items: center;
  gap: 13px;
  margin-top: 24px;
  padding: 14px;
  border: 1px solid #dddddd;
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
  background: #ffbc2e;
  color: #222222;
  font-size: 16px;
  font-weight: 800;
}

.selected-account-info {
  min-width: 0;
}

.selected-account-info strong {
  display: block;
  color: #222222;
  font-size: 14px;
  font-weight: 800;
}

.selected-account-info p {
  margin: 5px 0 0;
  color: #888888;
  font-size: 12px;
}

.modal-guide {
  margin: 20px 0 0;
  color: #777777;
  font-size: 12px;
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
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;
}

.modal-cancel-button {
  border: 1px solid #dddddd;
  background: #ffffff;
  color: #333333;
}

.modal-delete-button {
  border: 1px solid #ffc7c7;
  background: #ffe7e7;
  color: #e53935;
}

@media (max-width: 360px) {
  .account-container {
    padding-right: 20px;
    padding-left: 20px;
  }

  .connect-button,
  .toast-message {
    right: 20px;
    left: 20px;
  }
}

@keyframes rotate {
  to {
    transform: rotate(360deg);
  }
}
</style>