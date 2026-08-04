<template>
  <div class="account-page">
    <main class="account-container">
      <header class="page-header">
        <button class="back-button" type="button" @click="goBack">&lt;</button>

        <h1>연결 계좌</h1>

        <button class="add-button" type="button" @click="goConnect">＋</button>
      </header>

      <section class="account-section">
        <p v-if="loading" class="state-message">계좌 목록을 불러오고 있어요.</p>

        <p v-else-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </p>

        <div v-else-if="accountStore.accounts.length === 0" class="empty-area">
          <div class="empty-icon">₩</div>
          <strong>연결된 계좌가 없어요</strong>
          <p>계좌를 연결하고 송금과 결제를 시작해 보세요.</p>

          <button type="button" @click="goConnect">
            계좌 연결하기
          </button>
        </div>

        <article
            v-for="account in accountStore.accounts"
            v-else
            :key="account.linkedAccountId"
            class="account-item"
        >
          <img
              v-if="account.bankLogoUrl"
              :alt="account.bankName"
              :src="account.bankLogoUrl"
              class="bank-logo"
          />

          <div class="account-info">
            <div class="bank-title">
              <strong>{{ account.bankName }}</strong>
              <span v-if="account.primaryYn === 'Y'">대표</span>
            </div>

            <p>{{ maskAccountNumber(account.accountNumber) }}</p>
            <small>{{ account.accountHolder }}</small>
          </div>

          <button
              class="menu-button"
              type="button"
              @click="toggleMenu(account.linkedAccountId)"
          >
            ⋮
          </button>

          <div
              v-if="openedAccountId === account.linkedAccountId"
              class="account-menu"
          >
            <button
                v-if="account.primaryYn !== 'Y'"
                type="button"
                @click="changePrimary(account)"
            >
              대표계좌 설정
            </button>

            <button class="delete-button" type="button" @click="removeAccount(account)">
              연결 해제
            </button>
          </div>
        </article>
      </section>

      <p v-if="toastMessage" class="toast-message">
        {{ toastMessage }}
      </p>
    </main>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { disconnectAccount, getAccounts, setPrimaryAccount } from '@/api/accountApi';
import { useAccountStore } from '@/stores/account';

const router = useRouter();
const accountStore = useAccountStore();

const loading = ref(false);
const errorMessage = ref('');
const toastMessage = ref('');
const openedAccountId = ref(null);

// 계좌번호 마스킹
const maskAccountNumber = (accountNumber) => {
  if (!accountNumber || accountNumber.length <= 7) return accountNumber;
  return `${accountNumber.slice(0, 3)}-${'*'.repeat(accountNumber.length - 7)}-${accountNumber.slice(-4)}`;
};

// 계좌 목록 조회
const loadAccounts = async () => {
  const userId = accountStore.userId;

  if (!userId) {
    router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    const accounts = await getAccounts(userId);
    accountStore.setAccounts(accounts);
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '계좌 목록을 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 메뉴 열기
const toggleMenu = (linkedAccountId) => {
  openedAccountId.value = openedAccountId.value === linkedAccountId ? null : linkedAccountId;
};

// 대표계좌 변경
const changePrimary = async (account) => {
  try {
    await setPrimaryAccount(accountStore.userId, account.linkedAccountId);
    openedAccountId.value = null;
    showToast('대표계좌가 변경되었습니다.');
    await loadAccounts();
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '대표계좌 변경에 실패했습니다.';
  }
};

// 계좌 연결 해제
const removeAccount = async (account) => {
  const confirmed = window.confirm(`${account.bankName} 계좌 연결을 해제할까요?`);

  if (!confirmed) return;

  try {
    await disconnectAccount(accountStore.userId, account.linkedAccountId);
    openedAccountId.value = null;
    showToast('계좌 연결이 해제되었습니다.');
    await loadAccounts();
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '계좌 연결 해제에 실패했습니다.';
  }
};

// 토스트 표시
const showToast = (message) => {
  toastMessage.value = message;

  window.setTimeout(() => {
    toastMessage.value = '';
  }, 2000);
};

// 계좌 연결 화면 이동
const goConnect = () => {
  accountStore.resetAccountForm();
  router.push('/setting/account/connect');
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(loadAccounts);
</script>

<style scoped>
.account-page {
  display: flex;
  justify-content: center;
  min-height: 100vh;
  padding: 24px 0;
  background: #f4f4f4;
}

.account-container {
  position: relative;
  width: 390px;
  height: 844px;
  padding: 26px 22px 30px;
  background: #ffffff;
  overflow: hidden;
}

.page-header {
  display: grid;
  grid-template-columns: 40px 1fr 40px;
  align-items: center;
}

.page-header h1 {
  margin: 0;
  font-size: 21px;
  font-weight: 800;
  text-align: center;
}

.back-button,
.add-button {
  padding: 0;
  border: 0;
  background: transparent;
  font-size: 28px;
}

.add-button {
  color: #222222;
}

.account-section {
  height: calc(100% - 60px);
  margin-top: 34px;
  overflow-y: auto;
}

.account-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
  padding: 19px 16px;
  border: 1px solid #eeeeee;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(0, 0, 0, 0.04);
}

.bank-logo {
  width: 45px;
  height: 45px;
  object-fit: contain;
}

.account-info {
  min-width: 0;
  flex: 1;
}

.bank-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bank-title strong {
  font-size: 15px;
}

.bank-title span {
  padding: 3px 7px;
  border-radius: 8px;
  background: #ffbc2e;
  font-size: 10px;
  font-weight: 700;
}

.account-info p {
  margin: 7px 0 0;
  color: #444444;
  font-size: 14px;
}

.account-info small {
  display: block;
  margin-top: 5px;
  color: #999999;
  font-size: 12px;
}

.menu-button {
  padding: 8px;
  border: 0;
  background: transparent;
  font-size: 24px;
}

.account-menu {
  position: absolute;
  z-index: 3;
  top: 54px;
  right: 12px;
  display: flex;
  width: 130px;
  flex-direction: column;
  padding: 6px;
  border: 1px solid #eeeeee;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.12);
}

.account-menu button {
  padding: 11px 10px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  font-size: 13px;
  text-align: left;
}

.account-menu button:hover {
  background: #f7f7f7;
}

.account-menu .delete-button {
  color: #e53935;
}

.empty-area {
  padding-top: 120px;
  text-align: center;
}

.empty-icon {
  display: flex;
  width: 84px;
  height: 84px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 28px;
  border-radius: 28px;
  background: #fff4d3;
  color: #bc7d00;
  font-size: 38px;
  font-weight: 800;
}

.empty-area strong {
  display: block;
  font-size: 21px;
}

.empty-area p {
  margin: 14px 0 28px;
  color: #888888;
  font-size: 14px;
  line-height: 1.5;
}

.empty-area button {
  height: 48px;
  padding: 0 24px;
  border: 1px solid #cc9200;
  border-radius: 12px;
  background: #ffbc2e;
  font-size: 15px;
  font-weight: 700;
}

.state-message,
.error-message {
  margin-top: 100px;
  font-size: 14px;
  text-align: center;
}

.state-message {
  color: #777777;
}

.error-message {
  color: #e53935;
}

.toast-message {
  position: absolute;
  right: 28px;
  bottom: 42px;
  left: 28px;
  margin: 0;
  padding: 15px;
  border-radius: 12px;
  background: rgba(34, 34, 34, 0.92);
  color: #ffffff;
  font-size: 14px;
  text-align: center;
}
</style>