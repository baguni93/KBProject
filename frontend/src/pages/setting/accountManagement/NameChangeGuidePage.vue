<template>
  <div class="page-layout guide-page">
    <PageHeader title="이름 변경" custom-back @back="goBack" />

    <main class="page-content guide-container">
      <section class="guide-content">
        <div class="guide-icon">
          <i class="fa-regular fa-user"></i>
        </div>

        <h2 class="text-26-bold">
          변경할 이름을<br />
          입력해 주세요
        </h2>

        <p class="description text-13">
          이름 변경을 위해 현재 등록된<br />
          휴대폰 번호로 본인인증을 진행해요.
        </p>

        <!-- 현재 이름 -->
        <div class="name-field">
          <label class="text-13-bold">현재 이름</label>

          <div class="name-box readonly text-15">
            {{ userInfo.userName || '-' }}
          </div>
        </div>

        <!-- 변경할 이름 -->
        <div class="name-field">
          <label class="text-13-bold" for="newUserName">변경할 이름</label>

          <input
            id="newUserName"
            v-model.trim="newUserName"
            :class="{ error: !!nameErrorMessage }"
            class="name-box text-15"
            maxlength="7"
            placeholder="새로운 이름을 입력해 주세요"
            type="text"
            @input="clearNameError"
            @blur="validateName"
          />

          <p v-if="nameErrorMessage" class="name-error-message text-13">
            {{ nameErrorMessage }}
          </p>
        </div>

        <!-- 변경 전 안내 -->
        <section class="information-area">
          <div class="information-title">
            <span>
              <i class="fa-solid fa-check"></i>
            </span>

            <strong class="text-13-bold">변경 전 확인해 주세요</strong>
          </div>

          <ul class="information-list">
            <li>현재 회원정보로 본인인증을 진행해요.</li>
            <li>본인 명의의 휴대폰만 사용할 수 있어요.</li>
            <li>변경된 이름은 마이페이지에 바로 반영돼요.</li>
          </ul>
        </section>

        <p v-if="loadErrorMessage" class="load-error-message text-13">
          {{ loadErrorMessage }}
        </p>
      </section>
    </main>

    <div class="bottom-btn-area single">
      <button
        class="bottom-btn"
        :disabled="loading || !newUserName"
        type="button"
        @click="startVerification"
      >
        본인인증 하기
      </button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo } from '@/api/userApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { useAuthStore } from '@/stores/auth';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const authStore = useAuthStore();
const signupStore = useSignupStore();

const userInfo = reactive({ userName: '', birthDate: '', phoneNumber: '' });
const newUserName = ref('');
const loading = ref(false);
const nameErrorMessage = ref('');
const loadErrorMessage = ref('');

// const nameRegex = /^[가-힣]{2,7}$/;
const nameRegex = /^[가-힣0-9]{2,10}$/;

// 회원정보 조회
const loadUserInfo = async () => {
  if (!authStore.userId) {
    await router.replace('/intro');
    return;
  }

  try {
    loading.value = true;
    loadErrorMessage.value = '';

    const data = await getUserInfo(authStore.userId);

    userInfo.userName = data.userName || '';
    userInfo.birthDate = data.birthDate || '';
    userInfo.phoneNumber = data.phoneNumber || '';
  } catch (error) {
    console.error(error);
    loadErrorMessage.value =
      error.response?.data?.message || '회원정보를 불러오지 못했습니다.';
  } finally {
    loading.value = false;
  }
};

// 이름 유효성 검사
const validateName = () => {
  if (!newUserName.value) {
    nameErrorMessage.value = '변경할 이름을 입력해주세요.';
    return false;
  }

  if (!nameRegex.test(newUserName.value)) {
    nameErrorMessage.value = '이름은 한글 2~7자로 입력해주세요.';
    return false;
  }

  if (newUserName.value === userInfo.userName) {
    nameErrorMessage.value = '현재 이름과 다른 이름을 입력해주세요.';
    return false;
  }

  nameErrorMessage.value = '';
  return true;
};

// 이름 변경 본인인증 시작
const startVerification = async () => {
  if (!validateName()) return;

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

// 이름 오류 메시지 초기화
const clearNameError = () => {
  nameErrorMessage.value = '';
};

// 이전 화면
const goBack = () => {
  router.back();
};

onMounted(loadUserInfo);
</script>

<style scoped>
@import '@/components/common/common/common.css';

.guide-page {
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

.guide-container {
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.guide-container::-webkit-scrollbar {
  display: none;
}

.guide-content {
  padding-top: 36px;
  text-align: center;
}

.guide-icon {
  display: flex;
  width: 64px;
  height: 64px;
  align-items: center;
  justify-content: center;
  margin: 0 auto 22px;
  border-radius: 20px;
  background: #fff4d7;
  color: var(--color-primary-active);
  font-size: 26px;
}

.guide-content h2 {
  margin: 0;
  color: var(--color-text-main);
  line-height: 1.38;
  letter-spacing: -0.6px;
}

.description {
  margin: 14px 0 0;
  color: var(--color-text-sub);
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
  color: var(--color-text-main);
}

.name-box {
  width: 100%;
  height: 52px;
  padding: 0 16px;
  border: 1px solid var(--color-border-main);
  border-radius: 12px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  box-sizing: border-box;
}

input.name-box {
  outline: none;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
}

input.name-box:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.1);
}

input.name-box.error,
input.name-box.error:focus {
  border-color: var(--color-error);
  box-shadow: 0 0 0 3px rgba(229, 57, 53, 0.08);
}

.name-box.readonly {
  display: flex;
  align-items: center;
  background: var(--color-bg-screen);
  color: var(--color-text-sub);
}

.name-error-message {
  margin: 7px 2px 0;
  color: var(--color-error);
  line-height: 1.4;
  text-align: left;
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
  width: 22px;
  height: 22px;
  flex: none;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-text-white);
  font-size: 13px;
}

.information-title strong {
  color: var(--color-text-main);
}

.information-area ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: var(--color-text-sub);
  font-size: 12px;
  font-weight: 400;
  line-height: 1.45;
}

.information-area li + li {
  margin-top: 1px;
}

.load-error-message {
  margin: 14px 0 0;
  color: var(--color-error);
  line-height: 1.5;
  text-align: left;
}
</style>
