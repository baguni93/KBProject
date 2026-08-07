<template>
  <form class="nickname-form" @submit.prevent="handleSubmit">
    <div class="input-group">
      <label for="nickname">닉네임</label>

      <div class="nickname-input-wrap">
        <input
            id="nickname"
            v-model.trim="nickname"
            type="text"
            maxlength="15"
            placeholder="닉네임을 입력해주세요"
            @input="handleNicknameInput"
        />

        <button
            type="button"
            class="check-button"
            :disabled="!nicknamePattern.test(nickname) || checking"
            @click="handleCheckNickname"
        >
          {{ checking ? '확인 중' : '중복 확인' }}
        </button>
      </div>

      <div class="input-info">
        <p v-if="message" :class="messageClass">{{ message }}</p>

        <span class="character-count">{{ nickname.length }}/15</span>
      </div>
    </div>

    <button type="submit" class="submit-button" :disabled="!available || submitting">
      {{ submitting ? '가입 중' : '회원가입' }}
    </button>
  </form>
</template>

<script setup>
import { computed, ref } from 'vue';
import { checkNickname } from '@/api/userApi';

defineProps({
  submitting: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['submit']);

const nickname = ref('');
const available = ref(false);
const checking = ref(false);
const message = ref('');
const nicknamePattern = /^[가-힣a-z0-9_]{1,15}$/;

const messageClass = computed(() =>
    available.value ? 'success-message' : 'error-message'
);

// 닉네임 입력
const handleNicknameInput = () => {
  nickname.value = nickname.value.toLowerCase();

  available.value = false;

  if (!nickname.value) {
    message.value = '';
    return;
  }

  if (!nicknamePattern.test(nickname.value)) {
    message.value = '한글, 영어, 숫자, _만 사용할 수 있습니다.';
    return;
  }

  message.value = '';
};

// 닉네임 중복 확인
const handleCheckNickname = async () => {
  if (!nickname.value) {
    message.value = '닉네임을 입력해주세요.';
    return;
  }

  if (!nicknamePattern.test(nickname.value)) {
    available.value = false;
    message.value = '한글, 영어, 숫자, _만 사용할 수 있습니다.';
    return;
  }

  try {
    checking.value = true;

    const data = await checkNickname(nickname.value);

    available.value = data.available;
    message.value = data.available
        ? '사용 가능한 닉네임입니다.'
        : '이미 사용 중인 닉네임입니다.';
  } catch (error) {
    available.value = false;
    message.value =
        error.response?.data?.message || '닉네임 중복 확인에 실패했습니다.';
  } finally {
    checking.value = false;
  }
};

// 회원가입 요청
const handleSubmit = () => {
  if (!nickname.value) {
    message.value = '닉네임을 입력해주세요.';
    return;
  }

  if (!nicknamePattern.test(nickname.value)) {
    available.value = false;
    message.value = '한글, 영어, 숫자, _만 사용할 수 있습니다.';
    return;
  }

  if (!available.value) {
    message.value = '닉네임 중복 확인을 진행해주세요.';
    return;
  }

  emit('submit', nickname.value);
};
</script>

<style scoped>
.nickname-form {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-group label {
  color: #222222;
  font-size: 15px;
  font-weight: 600;
}

.nickname-input-wrap {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 96px;
  gap: 8px;
  width: 100%;
}

.nickname-input-wrap input {
  width: 100%;
  min-width: 0;
  height: 50px;
  padding: 0 16px;
  border: 1px solid #dddddd;
  border-radius: 10px;
  background: #ffffff;
  color: #222222;
  font-size: 16px;
  outline: none;
  box-sizing: border-box;
}

.nickname-input-wrap input:focus {
  border-color: #ffbc2e;
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.nickname-input-wrap input::placeholder {
  color: #aaaaaa;
}

.check-button {
  width: 96px;
  height: 50px;
  padding: 0;
  border: 1px solid #dddddd;
  border-radius: 10px;
  background: #ffffff;
  color: #333333;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}

.check-button:active:not(:disabled) {
  background: #f7f7f7;
}

.check-button:disabled {
  border-color: #eeeeee;
  background: #f5f5f5;
  color: #aaaaaa;
  cursor: not-allowed;
}

.input-info {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 96px;
  gap: 8px;
  width: 100%;
  min-height: 20px;
  align-items: start;
}

.success-message,
.error-message {
  grid-column: 1;
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
  text-align: left;
}

.success-message {
  color: #16883e;
}

.error-message {
  color: #e53935;
}

.character-count {
  grid-column: 2;
  margin: 0;
  color: #999999;
  font-size: 13px;
  line-height: 1.5;
  text-align: right;
  white-space: nowrap;
}

.submit-button {
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

.submit-button:active:not(:disabled) {
  background: #f2aa10;
}

.submit-button:disabled {
  border-color: #dddddd;
  background: #eeeeee;
  color: #999999;
  cursor: not-allowed;
}
</style>