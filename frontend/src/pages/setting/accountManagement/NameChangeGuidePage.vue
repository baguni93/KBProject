<template>
  <div class="guide-page">
    <main class="guide-container">
      <header class="page-header">
        <button class="back-button" type="button" @click="goBack">
          &lt;
        </button>

        <h1>이름 변경</h1>
        <div class="header-empty"></div>
      </header>

      <section class="guide-content">
        <div class="guide-icon">👤</div>

        <h2>
          변경할 이름을<br />
          입력해 주세요
        </h2>

        <p class="description">
          이름 변경을 위해 현재 등록된<br />
          휴대폰 번호로 본인인증을 진행해요.
        </p>

        <div class="name-field">
          <label>현재 이름</label>

          <div class="name-box readonly">
            {{ userInfo.userName || '-' }}
          </div>
        </div>

        <div class="name-field">
          <label for="newUserName">변경할 이름</label>

          <input
              id="newUserName"
              v-model.trim="newUserName"
              class="name-box"
              maxlength="50"
              placeholder="새로운 이름을 입력해 주세요"
              type="text"
              @input="clearError"
          />
        </div>

        <section class="information-area">
          <div class="information-title">
            <span>✓</span>
            <strong>변경 전 확인해 주세요</strong>
          </div>

          <ul>
            <li>현재 회원정보로 본인인증을 진행해요.</li>
            <li>본인 명의의 휴대폰만 사용할 수 있어요.</li>
            <li>변경된 이름은 마이페이지에 바로 반영돼요.</li>
          </ul>
        </section>

        <p v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </p>
      </section>

      <button class="next-button" :disabled="loading || !newUserName" type="button" @click="startVerification">
        본인인증 하기
      </button>
    </main>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
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

const newUserName = ref('');
const loading = ref(false);
const errorMessage = ref('');

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
    userInfo.birthDate = data.birthDate || '';
    userInfo.phoneNumber = data.phoneNumber || '';
  } catch (error) {
    console.error(error);
    errorMessage.value = error.response?.data?.message || '회원정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 이름 변경 본인인증 시작
const startVerification = async () => {
  errorMessage.value = '';

  if (!newUserName.value) {
    errorMessage.value = '변경할 이름을 입력해주세요.';
    return;
  }

  if (newUserName.value === userInfo.userName) {
    errorMessage.value = '현재 이름과 다른 이름을 입력해주세요.';
    return;
  }

  sessionStorage.setItem('nameChangeNewUserName', newUserName.value);

  signupStore.setPhoneAuth({
    userName: newUserName.value,
    birthDate: userInfo.birthDate,
    phoneNumber: userInfo.phoneNumber,
    carrierCode: '',
    verificationPurpose: 'NAME_CHANGE',
    verificationCode: '',
  });

  signupStore.setVerificationPurpose('NAME_CHANGE');

  await router.push('/signup/check');
};

// 오류 메시지 초기화
const clearError = () => {
  errorMessage.value = '';
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

.name-field {
  margin-top: 18px;
  text-align: left;
}

.name-field:first-of-type {
  margin-top: 34px;
}

.name-field label {
  display: block;
  margin-bottom: 9px;
  color: #333333;
  font-size: 14px;
  font-weight: 700;
}

.name-box {
  width: 100%;
  height: 52px;
  padding: 0 16px;
  border: 1px solid #dddddd;
  border-radius: 10px;
  background: #ffffff;
  color: #222222;
  font-size: 16px;
  font-weight: 500;
  box-sizing: border-box;
}

input.name-box {
  outline: none;
}

input.name-box:focus {
  border-color: #ffbc2e;
}

.name-box.readonly {
  display: flex;
  align-items: center;
  background: #f7f7f7;
  color: #777777;
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