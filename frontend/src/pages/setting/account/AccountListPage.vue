<template>
  <div class="account-page">
    <main class="account-container" @click="closeMenu">
      <header class="page-header">
        <button
            class="back-button"
            type="button"
            aria-label="이전 화면"
            @click="goBack"
        >
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
            <strong>{{ accountStore.accounts.length }}</strong>
          </div>

          <button
              class="refresh-button"
              :disabled="loading"
              type="button"
              aria-label="계좌 목록 새로고침"
              @click="loadAccounts"
          >
            <span :class="{ rotating: loading }">↻</span>
          </button>
        </div>

        <div class="account-list-area">
          <p v-if="loading" class="state-message">
            계좌 목록을 불러오고 있어요.
          </p>

          <p v-else-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </p>

          <div v-else-if="accountStore.accounts.length === 0" class="empty-area">
            <div class="empty-icon">₩</div>

            <strong>연결된 계좌가 없어요</strong>

            <p>
              본인 명의 계좌를 연결하고<br />
              송금과 결제를 시작해 보세요.
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

                <span v-else class="bank-logo-fallback">
                  {{ getBankInitial(account.bankName) }}
                </span>
              </div>

              <div class="account-info">
                <div class="bank-title">
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
                    @click="removeAccount(account)"
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
let toastTimer = null;

// 은행명 첫 글자
const getBankInitial = (bankName) => {
  if (!bankName) return '₩';

  return bankName.charAt(0);
};

// 계좌번호 마스킹
const maskAccountNumber = (accountNumber) => {
  if (!accountNumber) return '';

  const onlyNumber = String(accountNumber).replace(/[^0-9]/g, '');

  if (onlyNumber.length <= 7) return onlyNumber;

  const frontNumber = onlyNumber.slice(0, 3);
  const middleLength = onlyNumber.length - 7;
  const lastNumber = onlyNumber.slice(-4);

  return `${frontNumber}-${'*'.repeat(middleLength)}-${lastNumber}`;
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

    const accounts = await getAccounts(userId);

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

// 대표계좌 변경
const changePrimary = async (account) => {
  try {
    errorMessage.value = '';

    await setPrimaryAccount(accountStore.userId, account.linkedAccountId);

    openedAccountId.value = null;

    await loadAccounts();

    showToast('대표 계좌가 변경되었습니다.');
  } catch (error) {
    console.error(error);

    errorMessage.value = error.response?.data?.message || '대표 계좌 변경에 실패했습니다.';
  }
};

// 계좌 연결 해제
const removeAccount = async (account) => {
  const confirmed = window.confirm(`${account.bankName} 계좌 연결을 해제할까요?`);

  if (!confirmed) return;

  try {
    errorMessage.value = '';

    await disconnectAccount(accountStore.userId, account.linkedAccountId);

    openedAccountId.value = null;

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

// 계좌 관리 메뉴 닫기
const closeMenu = () => {
  openedAccountId.value = null;
};

// 계좌 연결 화면 이동
const goConnect = () => {
  accountStore.resetAccountForm();

  router.push('/setting/account/connect');
};

// 설정 홈 화면 이동
const goBack = async () => {
  await router.push('/setting');
};

onMounted(() => {loadAccounts();});

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
  overflow: hidden;
}

.page-header {
  display: grid;
  grid-template-columns: 38px 1fr 38px;
  min-height: 44px;
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
  min-height: 0;
  margin-top: 32px;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.account-section::-webkit-scrollbar {
  display: none;
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
  border: 1px solid #e5e5e5;
  border-radius: 0 0 14px 14px;
  background: #ffffff;
  overflow: visible;
}

.account-list {
  display: flex;
  flex-direction: column;
}

.account-item {
  position: relative;
  display: flex;
  min-height: 76px;
  align-items: center;
  gap: 12px;
  padding: 12px 13px;
  border-bottom: 1px solid #eeeeee;
  background: #ffffff;
}

.account-item:last-child {
  border-bottom: 0;
}

.bank-logo-area {
  display: flex;
  flex: none;
  width: 40px;
  height: 40px;
  align-items: center;
  justify-content: center;
}

.bank-logo {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.bank-logo-fallback {
  display: flex;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffbc2e;
  color: #222222;
  font-size: 15px;
  font-weight: 800;
}

.account-info {
  min-width: 0;
  flex: 1;
}

.bank-title {
  display: flex;
  align-items: center;
  gap: 7px;
}

.bank-title strong {
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
  color: #777777;
  font-size: 12px;
  letter-spacing: 0.2px;
}

.account-info small {
  display: block;
  margin-top: 3px;
  color: #aaaaaa;
  font-size: 10px;
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
  z-index: 10;
  top: 58px;
  right: 13px;
  display: flex;
  width: 148px;
  flex-direction: column;
  padding: 6px;
  border: 1px solid #eeeeee;
  border-radius: 11px;
  background: #ffffff;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.15);
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
  padding: 54px 20px 48px;
  text-align: center;
}

.empty-icon {
  display: flex;
  width: 64px;
  height: 64px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  border-radius: 22px;
  background: #fff4d6;
  color: #c58200;
  font-size: 30px;
  font-weight: 800;
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

.state-message,
.error-message {
  margin: 0;
  padding: 70px 20px;
  font-size: 13px;
  line-height: 1.5;
  text-align: center;
}

.state-message {
  color: #777777;
}

.error-message {
  color: #e53935;
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
  right: 28px;
  bottom: 128px;
  left: 28px;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  padding: 14px 15px;
  border: 1px solid #dfeee2;
  border-radius: 10px;
  background: #f7fff8;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  color: #3c7350;
  font-size: 12px;
  font-weight: 600;
}

.toast-icon {
  display: inline-flex;
  width: 18px;
  height: 18px;
  align-items: center;
  justify-content: center;
  border: 1px solid #42a866;
  border-radius: 50%;
  color: #42a866;
  font-size: 11px;
  font-weight: 800;
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