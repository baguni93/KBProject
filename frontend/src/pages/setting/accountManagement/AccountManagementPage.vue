<template>
  <div class="page-layout management-page">
    <PageHeader title="계정 관리" custom-back @back="goBack" />

    <main class="page-content management-container">
      <!-- 상단 안내 -->
      <section class="title-section">
        <div class="title-icon">
          <i class="fa-solid fa-shield-halved"></i>
        </div>

        <div class="title-content">
          <h2 class="text-26-bold">계정을 관리해 보세요</h2>

          <p class="text-13">
            안전한 서비스 이용을 위해<br />
            계정 정보를 관리할 수 있어요.
          </p>
        </div>
      </section>

      <!-- 로딩 -->
      <p v-if="loading" class="state-message text-13">
        회원정보를 불러오고 있어요.
      </p>

      <!-- 오류 -->
      <p v-else-if="errorMessage" class="error-message text-13">
        {{ errorMessage }}
      </p>

      <template v-else>
        <!-- 내 정보 -->
        <section class="menu-group">
          <div class="group-title">
            <i class="fa-regular fa-user"></i>
            <strong class="text-13-bold">내 정보</strong>
          </div>

          <button class="menu-item" type="button" @click="goNameChange">
            <div class="menu-label">
              <span class="item-icon">
                <i class="fa-regular fa-id-card"></i>
              </span>

              <span class="text-15-bold">이름</span>
            </div>

            <div class="menu-value">
              <span class="text-13">{{ userInfo.userName || '-' }}</span>
              <i class="fa-solid fa-chevron-right arrow-icon"></i>
            </div>
          </button>

          <button class="menu-item" type="button" @click="goPhoneChange">
            <div class="menu-label">
              <span class="item-icon">
                <i class="fa-solid fa-mobile-screen-button"></i>
              </span>

              <span class="text-15-bold">휴대폰 번호</span>
            </div>

            <div class="menu-value">
              <span class="text-13">{{ formattedPhoneNumber }}</span>
              <i class="fa-solid fa-chevron-right arrow-icon"></i>
            </div>
          </button>
        </section>

        <!-- 보안 및 계정 -->
        <section class="menu-group security-group">
          <div class="group-title">
            <i class="fa-solid fa-lock"></i>
            <strong class="text-13-bold">보안 및 계정</strong>
          </div>

          <button class="menu-item" type="button" @click="goPinChange">
            <div class="menu-label">
              <span class="item-icon">
                <i class="fa-solid fa-key"></i>
              </span>

              <span class="text-15-bold">간편비밀번호 변경</span>
            </div>

            <i class="fa-solid fa-chevron-right arrow-icon"></i>
          </button>

          <button class="menu-item" type="button" @click="goLogout">
            <div class="menu-label">
              <span class="item-icon">
                <i class="fa-solid fa-arrow-right-from-bracket"></i>
              </span>

              <span class="text-15-bold">로그아웃</span>
            </div>

            <i class="fa-solid fa-chevron-right arrow-icon"></i>
          </button>

          <button
            class="menu-item withdraw-item"
            type="button"
            @click="goWithdraw"
          >
            <div class="menu-label">
              <span class="item-icon">
                <i class="fa-regular fa-circle-xmark"></i>
              </span>

              <span class="text-15-bold">회원탈퇴</span>
            </div>

            <i class="fa-solid fa-chevron-right arrow-icon"></i>
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
import PageHeader from '@/components/common/PageHeader.vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const userInfo = reactive({ userName: '', phoneNumber: '' });
const loading = ref(false);
const errorMessage = ref('');

// 휴대폰번호 표시
const formattedPhoneNumber = computed(() => {
  const phoneNumber = userInfo.phoneNumber.replace(/[^0-9]/g, '');

  if (phoneNumber.length === 11)
    return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 7)}-${phoneNumber.slice(7)}`;
  if (phoneNumber.length === 10)
    return `${phoneNumber.slice(0, 3)}-${phoneNumber.slice(3, 6)}-${phoneNumber.slice(6)}`;

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
    errorMessage.value =
      error.response?.data?.message || '회원정보를 불러오지 못했습니다.';
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

// 로그아웃 화면
const goLogout = () => {
  router.push('/setting/account-management/logout');
};

// 회원탈퇴 화면
const goWithdraw = () => {
  router.push('/setting/account-management/withdraw');
};

// 설정 메인 화면
const goBack = () => {
  router.push('/setting');
};

onMounted(loadUserInfo);
</script>

<style scoped>
@import '@/components/common/common/common.css';

.management-page {
  width: 100%;
  /* 모바일 브라우저 주소창 이슈를 해결하기 위해 dvh 사용 */
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  box-sizing: border-box;
  overflow: hidden;
  background: var(--color-bg-page);
}

.management-container {
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.management-container::-webkit-scrollbar {
  display: none;
}

/* 상단 안내 */
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
  color: var(--color-primary-active);
  font-size: 24px;
}

.title-content {
  min-width: 0;
}

.title-content h2 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.35;
  letter-spacing: -0.7px;
}

.title-content p {
  margin: 10px 0 0;
  color: var(--color-text-muted);
  line-height: 1.5;
}

/* 메뉴 그룹 */
.menu-group {
  flex-shrink: 0;
  margin-top: 42px;
  border: 1px solid var(--color-divider);
  border-radius: 15px;
  background: var(--color-bg-page);
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
  gap: 8px;
  padding: 0 15px;
  border-bottom: 1px solid var(--color-divider);
  background: #fffdf8;
  color: var(--color-text-sub);
}

.group-title i {
  width: 16px;
  color: var(--color-primary-active);
  text-align: center;
}

.group-title strong {
  color: var(--color-text-sub);
}

/* 메뉴 */
.menu-item {
  display: flex;
  width: 100%;
  height: 62px;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  border: 0;
  border-bottom: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: 0;
}

.menu-item:active {
  background: var(--color-bg-screen);
}

.menu-label {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 10px;
  color: var(--color-text-main);
}

.item-icon {
  display: flex;
  flex: none;
  width: 22px;
  align-items: center;
  justify-content: center;
  color: var(--color-text-sub);
  font-size: 15px;
}

.menu-value {
  display: flex;
  flex: none;
  align-items: center;
  gap: 12px;
  margin-left: 12px;
}

.menu-value span {
  color: var(--color-text-sub);
  white-space: nowrap;
}

.arrow-icon {
  flex: none;
  color: var(--color-text-disabled);
  font-size: 12px;
}

/* 회원탈퇴 */
.withdraw-item .menu-label,
.withdraw-item .item-icon {
  color: var(--color-error);
}

/* 상태 */
.state-message,
.error-message {
  margin: 100px 0 0;
  line-height: 1.5;
  text-align: center;
}

.state-message {
  color: var(--color-text-sub);
}

.error-message {
  color: var(--color-error);
}

@media (max-width: 360px) {
  .title-section {
    gap: 12px;
  }

  .title-icon {
    width: 48px;
    height: 48px;
    border-radius: 16px;
    font-size: 21px;
  }
}
</style>
