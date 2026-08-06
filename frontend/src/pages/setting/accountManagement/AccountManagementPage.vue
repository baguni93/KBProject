<template>
  <div class="management-page">
    <main class="management-container">
      <header class="page-header">
        <button class="back-button" type="button" @click="goBack">
          &lt;
        </button>

        <h1>계정 관리</h1>

        <div class="header-empty"></div>
      </header>

      <section class="title-section">
        <div class="title-icon">🔐</div>

        <div>
          <h2>
            보안과 계정을<br />
            편하게 관리해 보세요
          </h2>

          <p>안전한 서비스 이용을 위해 계정 정보를 관리할 수 있어요.</p>
        </div>
      </section>

      <p v-if="loading" class="state-message">
        회원정보를 불러오고 있어요.
      </p>

      <p v-else-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </p>

      <template v-else>
        <section class="menu-group">
          <div class="group-title">
            <span>👤</span>
            <strong>내 정보</strong>
          </div>

          <button class="menu-item" type="button" @click="goNameChange">
            <div class="menu-label">
              <span class="item-icon">🪪</span>
              <span>이름</span>
            </div>

            <div class="menu-value">
              <span>{{ userInfo.userName || '-' }}</span>
              <strong>&gt;</strong>
            </div>
          </button>

          <button class="menu-item" type="button" @click="goPhoneChange">
            <div class="menu-label">
              <span class="item-icon">📱</span>
              <span>휴대폰 번호</span>
            </div>

            <div class="menu-value">
              <span>{{ formattedPhoneNumber }}</span>
              <strong>&gt;</strong>
            </div>
          </button>
        </section>

        <section class="menu-group security-group">
          <div class="group-title">
            <span>🔒</span>
            <strong>보안 및 계정</strong>
          </div>

          <button class="menu-item" type="button" @click="goPinChange">
            <div class="menu-label">
              <span class="item-icon">🔑</span>
              <span>간편비밀번호 변경</span>
            </div>

            <strong class="single-arrow">&gt;</strong>
          </button>

          <button class="menu-item withdraw-item" type="button" @click="goWithdraw">
            <div class="menu-label">
              <span class="item-icon">🚫</span>
              <span>회원탈퇴</span>
            </div>

            <strong class="single-arrow">&gt;</strong>
          </button>
        </section>
      </template>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '@/api/userApi';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const userInfo = reactive({
  userName: '',
  phoneNumber: '',
});

const loading = ref(false);
const errorMessage = ref('');

// 휴대폰번호 표시
const formattedPhoneNumber = computed(() => {
  const phoneNumber = userInfo.phoneNumber.replace(/[^0-9]/g, '');

  if (phoneNumber.length === 11) {
    return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 7)}-${phoneNumber.slice(7)}`;
  }

  if (phoneNumber.length === 10) {
    return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 6)}-${phoneNumber.slice(6)}`;
  }

  return phoneNumber || '-';
});

// 회원정보 조회
const loadUserInfo = async () => {
  if (!authStore.userId) {
    await router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    const data = await getUserInfo(authStore.userId);

    userInfo.userName = data.userName || '';
    userInfo.phoneNumber = data.phoneNumber || '';

    if (data.userName) authStore.setUserName(data.userName);
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '회원정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 이름 변경 안내 화면
const goNameChange = () => {
  router.push('/setting/account-management/name');
};

// 휴대폰번호 변경 안내 화면
const goPhoneChange = () => {
  router.push('/setting/account-management/phone');
};

// PIN 변경 화면
const goPinChange = () => {
  sessionStorage.removeItem('pinChangeCurrentPin');
  sessionStorage.removeItem('pinChangeNewPin');
  router.push('/setting/account-management/pin');
};

// 회원탈퇴 화면
const goWithdraw = () => {
  router.push('/setting/account-management/withdraw');
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(loadUserInfo);
</script>

<style scoped>
.management-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.management-container {
  display: flex;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex-direction: column;
  padding: 10px 28px 30px;
  background: #ffffff;
  box-sizing: border-box;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.management-container::-webkit-scrollbar {
  display: none;
}

.page-header {
  display: grid;
  grid-template-columns: 38px 1fr 38px;
  min-height: 44px;
  align-items: center;
  flex-shrink: 0;
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
  display: flex;
  flex-shrink: 0;
  align-items: flex-start;
  gap: 15px;
  margin-top: 38px;
}

.title-icon {
  display: flex;
  flex: none;
  width: 54px;
  height: 54px;
  align-items: center;
  justify-content: center;
  border-radius: 18px;
  background: #fff5d8;
  font-size: 26px;
}

.title-section > div:last-child {
  min-width: 0;
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
  margin: 10px 0 0;
  color: #999999;
  font-size: 11px;
  line-height: 1.5;
}

.menu-group {
  flex-shrink: 0;
  margin-top: 42px;
  border: 1px solid #e8e8e8;
  border-radius: 15px;
  background: #ffffff;
  box-shadow: 0 7px 20px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.security-group {
  margin-top: 20px;
}

.group-title {
  display: flex;
  height: 45px;
  align-items: center;
  gap: 7px;
  padding: 0 15px;
  border-bottom: 1px solid #eeeeee;
  background: #fffdf8;
}

.group-title span {
  font-size: 13px;
}

.group-title strong {
  color: #555555;
  font-size: 12px;
  font-weight: 800;
}

.menu-item {
  display: flex;
  width: 100%;
  height: 62px;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  border: 0;
  border-bottom: 1px solid #eeeeee;
  background: #ffffff;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: 0;
}

.menu-item:hover {
  background: #fafafa;
}

.menu-item:active {
  background: #f5f5f5;
}

.menu-label {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 10px;
  color: #333333;
  font-size: 14px;
  font-weight: 700;
}

.item-icon {
  display: flex;
  flex: none;
  width: 21px;
  justify-content: center;
  color: #777777;
  font-size: 15px;
}

.menu-value {
  display: flex;
  flex: none;
  align-items: center;
  gap: 10px;
  margin-left: 12px;
}

.menu-value span {
  color: #777777;
  font-size: 12px;
  white-space: nowrap;
}

.menu-value strong,
.single-arrow {
  color: #999999;
  font-size: 18px;
  font-weight: 400;
}

.single-arrow {
  flex: none;
  margin-left: 12px;
}

.withdraw-item .menu-label {
  color: #ef4444;
}

.withdraw-item .item-icon {
  color: #ef4444;
}

.state-message,
.error-message {
  margin: 100px 0 0;
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

@media (max-width: 360px) {
  .management-container {
    padding-right: 20px;
    padding-left: 20px;
  }

  .title-section {
    gap: 12px;
  }

  .title-icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    font-size: 23px;
  }

  .title-section h2 {
    font-size: 23px;
  }
}
</style>