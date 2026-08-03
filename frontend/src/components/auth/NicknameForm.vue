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
  gap: 32px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-group label {
  font-size: 15px;
  font-weight: 600;
}

.nickname-input-wrap {
  display: flex;
  gap: 8px;
}

.nickname-input-wrap input {
  flex: 1;
  height: 50px;
  padding: 0 16px;
  border: 1px solid #dddddd;
  border-radius: 10px;
  font-size: 16px;
  outline: none;
}

.nickname-input-wrap input:focus {
  border-color: #222222;
}

.check-button {
  min-width: 96px;
  height: 50px;
  border: none;
  border-radius: 10px;
  background: #eeeeee;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.check-button:disabled {
  color: #aaaaaa;
  cursor: not-allowed;
}

.input-info {
  display: flex;
  min-height: 20px;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.success-message,
.error-message {
  flex: 1;
  margin: 0;
  font-size: 13px;
}

.success-message {
  color: #16883e;
}

.error-message {
  color: #e53935;
}

.character-count {
  margin-left: auto;
  color: #999999;
  font-size: 13px;
  white-space: nowrap;
}

.submit-button {
  width: 100%;
  height: 54px;
  border: none;
  border-radius: 12px;
  background: #222222;
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
}

.submit-button:disabled {
  background: #cccccc;
  cursor: not-allowed;
}
</style>