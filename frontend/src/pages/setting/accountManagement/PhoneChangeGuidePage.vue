<template>
  <div class="page-layout guide-page">
    <PageHeader title="휴대폰 번호 변경" custom-back @back="goBack" />

    <main class="page-content guide-container">
      <section class="guide-content">
        <div class="guide-icon">
          <i class="fa-solid fa-mobile-screen-button"></i>
        </div>

        <h2 class="text-26-bold">
          새로운 휴대폰 번호를<br />
          인증해 주세요
        </h2>

        <p class="description text-13">
          변경할 번호로 본인인증을 완료하면<br />
          새로운 번호가 계정에 등록돼요.
        </p>

        <div class="phone-field">
          <label class="text-13-bold">현재 휴대폰 번호</label>

          <div class="phone-box readonly text-15">
            {{ formattedPhoneNumber }}
          </div>
        </div>

        <div class="phone-field">
          <label class="text-13-bold" for="newPhoneNumber"
            >새 휴대폰 번호</label
          >

          <input
            id="newPhoneNumber"
            ref="newPhoneInput"
            v-model="newPhoneNumber"
            :class="{ error: !!phoneErrorMessage }"
            class="phone-box text-15"
            maxlength="13"
            inputmode="numeric"
            placeholder="새 휴대폰 번호를 입력해 주세요"
            type="text"
            @input="formatNewPhoneNumber"
            @blur="validatePhoneNumber"
          />

          <p v-if="phoneErrorMessage" class="phone-error-message text-13">
            {{ phoneErrorMessage }}
          </p>
        </div>

        <section class="information-area">
          <div class="information-title">
            <span>
              <i class="fa-solid fa-check"></i>
            </span>

            <strong class="text-13-bold">변경 전 확인해 주세요</strong>
          </div>

          <ul class="information-list">
            <li>본인 명의의 휴대폰 번호만 등록할 수 있어요.</li>
            <li>현재 번호와 다른 번호를 입력해야 해요.</li>
            <li>이미 가입된 번호로는 변경할 수 없어요.</li>
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
        :disabled="loading || !newPhoneNumber"
        type="button"
        @click="startVerification"
      >
        새 휴대폰 번호 인증
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api';
import { getUserInfo } from '@/api/userApi';
import PageHeader from '@/components/common/PageHeader.vue';
import { useAuthStore } from '@/stores/auth';
import { useSignupStore } from '@/stores/signup';

const router = useRouter();
const authStore = useAuthStore();
const signupStore = useSignupStore();

const userInfo = reactive({ userName: '', birthDate: '', phoneNumber: '' });
const loading = ref(false);
const phoneErrorMessage = ref('');
const loadErrorMessage = ref('');
const newPhoneNumber = ref('');
const newPhoneInput = ref(null);

// 현재 휴대폰번호 표시
const formattedPhoneNumber = computed(() => {
  const value = (userInfo.phoneNumber || '').replace(/[^0-9]/g, '');

  if (value.length === 11)
    return `${value.slice(0, 3)}-${value.slice(3, 7)}-${value.slice(7, 11)}`;
  if (value.length === 10)
    return `${value.slice(0, 3)}-${value.slice(3, 6)}-${value.slice(6, 10)}`;

  return value || '-';
});

// 새 휴대폰번호 형식 적용
const formatNewPhoneNumber = () => {
  const value = newPhoneNumber.value.replace(/[^0-9]/g, '').slice(0, 11);

  phoneErrorMessage.value = '';

  if (value.length <= 3) {
    newPhoneNumber.value = value;
    return;
  }

  if (value.length <= 7) {
    newPhoneNumber.value = `${value.slice(0, 3)}-${value.slice(3)}`;
    return;
  }

  newPhoneNumber.value = `${value.slice(0, 3)}-${value.slice(3, 7)}-${value.slice(7)}`;
};

// 입력 선택 영역 해제
const clearPhoneSelection = async () => {
  await nextTick();

  if (!newPhoneInput.value) return;

  const length = newPhoneInput.value.value.length;
  newPhoneInput.value.setSelectionRange(length, length);
};

// 새 휴대폰번호 유효성 검사
const validatePhoneNumber = () => {
  const currentPhoneNumber = userInfo.phoneNumber.replace(/[^0-9]/g, '');
  const phoneNumber = newPhoneNumber.value.replace(/[^0-9]/g, '');

  if (!phoneNumber) {
    phoneErrorMessage.value = '새 휴대폰 번호를 입력해주세요.';
    clearPhoneSelection();
    return false;
  }

  if (!/^01[016789][0-9]{7,8}$/.test(phoneNumber)) {
    phoneErrorMessage.value = '휴대폰번호를 확인해주세요.';
    clearPhoneSelection();
    return false;
  }

  if (phoneNumber === currentPhoneNumber) {
    phoneErrorMessage.value = '현재 휴대폰번호와 다른 번호를 입력해주세요.';
    clearPhoneSelection();
    return false;
  }

  phoneErrorMessage.value = '';
  return true;
};

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

// 새 휴대폰번호 본인인증 시작
const startVerification = async () => {
  if (!validatePhoneNumber()) return;

  const phoneNumber = newPhoneNumber.value.replace(/[^0-9]/g, '');

  try {
    loading.value = true;
    phoneErrorMessage.value = '';

    await api.get('/api/users/phone/check', { params: { phoneNumber } });

    signupStore.setPhoneAuth({
      userName: userInfo.userName,
      birthDate: userInfo.birthDate,
      carrierCode: '',
      phoneNumber,
      verificationPurpose: 'PHONE_CHANGE',
      verificationCode: '',
    });

    signupStore.setVerificationPurpose('PHONE_CHANGE');
    await router.push('/signup/check');
  } catch (error) {
    console.error(error);
    phoneErrorMessage.value =
      error.response?.data?.message || '휴대폰번호를 확인해주세요.';
    await clearPhoneSelection();
  } finally {
    loading.value = false;
  }
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

.phone-field {
  margin-top: 18px;
  text-align: left;
}

.phone-field:first-of-type {
  margin-top: 34px;
}

.phone-field label {
  display: block;
  margin-bottom: 9px;
  color: var(--color-text-main);
}

.phone-box {
  width: 100%;
  height: 52px;
  padding: 0 16px;
  border: 1px solid var(--color-border-main);
  border-radius: 12px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  box-sizing: border-box;
}

input.phone-box {
  outline: none;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
}

input.phone-box:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.1);
}

input.phone-box.error,
input.phone-box.error:focus {
  border-color: var(--color-error);
  box-shadow: 0 0 0 3px rgba(229, 57, 53, 0.08);
}

.phone-box.readonly {
  display: flex;
  align-items: center;
  background: var(--color-bg-screen);
  color: var(--color-text-sub);
}

.phone-error-message {
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
