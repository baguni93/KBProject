<template>
  <main class="profile-page">
    <section class="profile-container">
      <header class="profile-header">
        <div class="profile-icon">👤</div>

        <h1>회원 정보</h1>
        <p>로그인한 회원의 기본정보입니다.</p>
      </header>

      <section v-if="loading" class="status-message">
        회원정보를 불러오고 있어요.
      </section>

      <section v-else-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </section>

      <section v-else class="profile-info">
        <div class="info-row">
          <span class="info-label">회원번호</span>
          <strong class="info-value">{{ userInfo.userId || authStore.userId }}</strong>
        </div>

        <div class="info-row">
          <span class="info-label">이름</span>
          <strong class="info-value">
            {{ userInfo.userName || authStore.userName || '정보 없음' }}
          </strong>
        </div>

        <div v-if="userInfo.birthDate" class="info-row">
          <span class="info-label">생년월일</span>
          <strong class="info-value">{{ userInfo.birthDate }}</strong>
        </div>

        <div v-if="userInfo.phoneNumber" class="info-row">
          <span class="info-label">휴대폰번호</span>
          <strong class="info-value">{{ userInfo.phoneNumber }}</strong>
        </div>

        <div v-if="userInfo.nickname" class="info-row">
          <span class="info-label">닉네임</span>
          <strong class="info-value">{{ userInfo.nickname }}</strong>
        </div>
      </section>

      <button class="home-button" type="button" @click="goHome">
        홈으로
      </button>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '@/api/userApi';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const userInfo = reactive({
  userId: null,
  userName: '',
  birthDate: '',
  phoneNumber: '',
  nickname: '',
});

const loading = ref(false);
const errorMessage = ref('');

// 회원정보 조회
const loadUserInfo = async () => {
  if (!authStore.userId) {
    errorMessage.value = '로그인 회원정보를 확인할 수 없습니다.';
    return;
  }

  try {
    loading.value = true;
    errorMessage.value = '';

    const data = await getUserInfo();

    Object.assign(userInfo, data);

    if (data.userName) {
      authStore.setUserName(data.userName);
    }
  } catch (error) {
    console.error(error);
    errorMessage.value =
        error.response?.data?.message || '회원정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 홈 화면 이동
const goHome = () => {
  router.push('/');
};

onMounted(() => {
  loadUserInfo();
});
</script>

<style scoped>
.profile-page {
  width: 100%;
  min-height: 100%;
  padding: 10px 28px 30px;
  background: #ffffff;
}

.profile-container {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.profile-header {
  text-align: center;
}

.profile-icon {
  display: flex;
  width: 72px;
  height: 72px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 22px;
  border-radius: 24px;
  background: #fff4d6;
  font-size: 34px;
}

.profile-header h1 {
  margin: 0;
  color: #111111;
  font-size: 28px;
  font-weight: 800;
}

.profile-header p {
  margin: 14px 0 0;
  color: #777777;
  font-size: 14px;
}

.profile-info {
  margin-top: 48px;
  border-top: 1px solid #eeeeee;
}

.info-row {
  display: flex;
  min-height: 66px;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  border-bottom: 1px solid #eeeeee;
}

.info-label {
  color: #777777;
  font-size: 14px;
}

.info-value {
  color: #222222;
  font-size: 15px;
  text-align: right;
}

.status-message,
.error-message {
  margin-top: 48px;
  text-align: center;
  font-size: 14px;
}

.status-message {
  color: #777777;
}

.error-message {
  color: #e53935;
}

.home-button {
  width: 100%;
  height: 58px;
  margin-top: auto;
  border: 1px solid #cc9200;
  border-radius: 12px;
  background: #ffbc2e;
  color: #111111;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}
</style>