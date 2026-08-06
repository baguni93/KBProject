<template>
  <div class="guide-page">
    <main class="guide-container">
      <header class="page-header">
        <button
            class="back-button"
            type="button"
            @click="goBack"
        >
          &lt;
        </button>

        <h1>휴대폰 번호 변경</h1>

        <div class="header-empty"></div>
      </header>

      <section class="guide-content">
        <div class="guide-icon">📱</div>

        <h2>
          새로운 휴대폰 번호를<br />
          인증해 주세요
        </h2>

        <p class="description">
          변경할 번호로 본인인증을 완료하면<br />
          새로운 번호가 계정에 등록돼요.
        </p>

        <section class="current-info">
          <span>현재 휴대폰 번호</span>
          <strong>{{ formattedPhoneNumber }}</strong>
        </section>

        <section class="information-area">
          <div class="information-title">
            <span>✓</span>
            <strong>변경 전 확인해 주세요</strong>
          </div>

          <ul>
            <li>본인 명의의 휴대폰 번호만 등록할 수 있어요.</li>
            <li>현재 번호와 다른 번호를 입력해야 해요.</li>
            <li>이미 가입된 번호로는 변경할 수 없어요.</li>
          </ul>
        </section>

        <p v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </p>
      </section>

      <button
          class="next-button"
          :disabled="loading"
          type="button"
          @click="startVerification"
      >
        새 휴대폰 번호 인증
      </button>
    </main>
  </div>
</template>

<script setup>
import {
  computed,
  onMounted,
  reactive,
  ref,
} from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '@/api/userApi';
import { useAuthStore } from '@/stores/auth';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const authStore = useAuthStore();
const signupStore = useSignupStore();

const userInfo = reactive({
  userName: '',
  birthDate: '',
  phoneNumber: '',
});

const loading = ref(false);
const errorMessage = ref('');

// 휴대폰번호 표시
const formattedPhoneNumber = computed(() => {
  const value =
      userInfo.phoneNumber
          .replace(/[^0-9]/g, '');

  if (value.length === 11) {
    return `${value.slice(0, 3)}-${value.slice(3, 7)}-${value.slice(7)}`;
  }

  return value || '-';
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

    const data =
        await getUserInfo(
            authStore.userId,
        );

    userInfo.userName =
        data.userName || '';

    userInfo.birthDate =
        data.birthDate || '';

    userInfo.phoneNumber =
        data.phoneNumber || '';
  } catch (error) {
    console.error(error);

    errorMessage.value =
        error.response?.data?.message
        || '회원정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 새 휴대폰번호 본인인증 시작
const startVerification = async () => {
  signupStore.setPhoneAuth({
    userName:
    userInfo.userName,
    birthDate:
    userInfo.birthDate,
    carrierCode: '',
    phoneNumber: '',
    verificationPurpose:
        'PHONE_CHANGE',
    verificationCode: '',
  });

  signupStore.setVerificationPurpose(
      'PHONE_CHANGE',
  );

  await router.push('/signup/check');
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(loadUserInfo);
</script>

<style scoped>
.guide-page {
  width: 100%;
  height: 100%;
  background: #ffffff;
}

.guide-container {
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

.guide-container::-webkit-scrollbar {
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

.guide-content {
  margin-top: 38px;
  text-align: center;
}

.guide-icon {
  display: flex;
  width: 76px;
  height: 76px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 26px;
  border-radius: 24px;
  background: #fff4d7;
  font-size: 34px;
}

.guide-content h2 {
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

.current-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 34px;
  padding: 16px 24px;
  border: 1px solid #eeeeee;
  border-radius: 13px;
  background: #ffffff;
  text-align: left;
  box-sizing: border-box;
}

.current-info span {
  color: #888888;
  font-size: 12px;
}

.current-info strong {
  color: #222222;
  font-size: 14px;
  font-weight: 700;
}

.information-area {
  margin-top: 22px;
  padding: 16px;
  border-radius: 14px;
  background: #fff9e9;
  text-align: left;
}

.information-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.information-title span {
  display: flex;
  width: 21px;
  height: 21px;
  flex: none;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffbc2e;
  color: #ffffff;
  font-size: 11px;
  font-weight: 700;
}

.information-title strong {
  color: #222222;
  font-size: 12px;
  font-weight: 800;
}

.information-area ul {
  margin: 12px 0 0;
  padding-left: 16px;
  color: #777777;
  font-size: 10px;
  line-height: 1.8;
}

.error-message {
  margin: 14px 0 0;
  color: #e53935;
  font-size: 12px;
  line-height: 1.5;
}

.next-button {
  position: absolute;
  right: 28px;
  bottom: 58px;
  left: 28px;
  width: auto;
  height: 58px;
  margin: 0;
  border: 1px solid #cc9200;
  border-radius: 10px;
  background: #ffbc2e;
  color: #111111;
  font-size: 18px;
  font-weight: 800;
  cursor: pointer;
}

.next-button:active:not(:disabled) {
  background: #f2aa10;
}

.next-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #aaaaaa;
  cursor: not-allowed;
}

@media (max-width: 360px) {
  .guide-container {
    padding-right: 20px;
    padding-left: 20px;
  }

  .next-button {
    right: 20px;
    left: 20px;
  }
}
</style>