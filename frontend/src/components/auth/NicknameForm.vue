<template>
  <form class="nickname-form" @submit.prevent="handleSubmit">
    <div class="input-group">
      <label for="nickname" class="text-15-bold">
        닉네임
      </label>

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
            class="check-button text-13-bold"
            :disabled="!nicknamePattern.test(nickname) || checking"
            @click="handleCheckNickname"
        >
          {{ checking ? '확인 중' : '중복 확인' }}
        </button>
      </div>

      <div class="input-info">
        <p
            v-if="message"
            :class="[messageClass, 'text-13']"
        >
          {{ message }}
        </p>

        <span class="character-count text-13">
          {{ nickname.length }}/15
        </span>
      </div>
    </div>
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

const emit = defineEmits(['submit', 'valid-change']);

const nickname = ref('');
const available = ref(false);
const checking = ref(false);
const message = ref('');
const nicknamePattern = /^[가-힣a-z0-9_]{1,15}$/;

const messageClass = computed(() => available.value ? 'success-message' : 'error-message');

// 닉네임 입력
const handleNicknameInput = () => {
  nickname.value = nickname.value.toLowerCase();
  available.value = false;
  emit('valid-change', false);

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
    emit('valid-change', false);
    message.value = '한글, 영어, 숫자, _만 사용할 수 있습니다.';
    return;
  }

  try {
    checking.value = true;

    const data = await checkNickname(nickname.value);

    available.value = data.available;
    emit('valid-change', data.available);
    message.value = data.available ? '사용 가능한 닉네임입니다.' : '이미 사용 중인 닉네임입니다.';
  } catch (error) {
    available.value = false;
    emit('valid-change', false);
    message.value = error.response?.data?.message || '닉네임 중복 확인에 실패했습니다.';
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
    emit('valid-change', false);
    message.value = '한글, 영어, 숫자, _만 사용할 수 있습니다.';
    return;
  }

  if (!available.value) {
    message.value = '닉네임 중복 확인을 진행해주세요.';
    return;
  }

  emit('submit', nickname.value);
};

defineExpose({
  submitForm: handleSubmit,
});
</script>

<style scoped>
@import "@/components/common/common/common.css";

.nickname-form {
  width: 100%;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-group label {
  color: var(--color-text-main);
}

/* 닉네임 입력 영역 */
.nickname-input-wrap {
  display: grid;
  width: 100%;
  grid-template-columns: minmax(0, 1fr) 88px;
  gap: 10px;
}

/* 닉네임 입력 */
.nickname-input-wrap input {
  width: 100%;
  min-width: 0;
  height: 52px;
  padding: 0 16px;
  border: 1px solid var(--color-border-main);
  border-radius: 10px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  font-size: 15px;
  font-weight: 500;
  outline: none;
  box-sizing: border-box;
}

.nickname-input-wrap input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(255, 188, 46, 0.12);
}

.nickname-input-wrap input::placeholder {
  color: var(--color-text-disabled);
}

/* 중복 확인 버튼 */
.check-button {
  width: 88px;
  height: 52px;
  padding: 0;
  border: 1px solid var(--color-border-main);
  border-radius: 10px;
  background: var(--color-bg-page);
  color: var(--color-text-main);
  cursor: pointer;
  white-space: nowrap;
}

.check-button:active:not(:disabled) {
  background: #f7f7f7;
}

.check-button:disabled {
  border-color: var(--color-border-main);
  background: var(--color-bg-disabled);
  color: var(--color-text-disabled);
  cursor: not-allowed;
}

/* 하단 상태 영역 */
.input-info {
  display: grid;
  width: 100%;
  min-height: 20px;
  grid-template-columns: minmax(0, 1fr) 88px;
  gap: 10px;
  align-items: start;
}

.success-message,
.error-message {
  grid-column: 1;
  margin: 0;
  line-height: 1.5;
  text-align: left;
}

.success-message {
  color: #16883e;
}

.error-message {
  color: var(--color-error);
}

.character-count {
  grid-column: 2;
  margin: 0;
  color: var(--color-text-muted);
  line-height: 1.5;
  text-align: right;
  white-space: nowrap;
}
</style>