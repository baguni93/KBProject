<template>
  <form class="phone-form" @submit.prevent="submit">
    <div class="input-group">
      <label for="userName">이름</label>
      <input
        id="userName"
        v-model.trim="form.userName"
        type="text"
        placeholder="이름을 입력해주세요."
        :readonly="isNameChange"
      />
    </div>

    <div class="input-group">
      <label for="birthDate">생년월일</label>
      <input id="birthDate" v-model="form.birthDate" type="date" />
    </div>

    <div class="input-group">
      <label for="carrierCode">통신사</label>
      <select id="carrierCode" v-model="form.carrierCode">
        <option value="">통신사를 선택해주세요.</option>
        <option value="SKT">SKT</option>
        <option value="KT">KT</option>
        <option value="LGU">LG U+</option>
        <option value="SKT_MVNO">SKT 알뜰폰</option>
        <option value="KT_MVNO">KT 알뜰폰</option>
        <option value="LGU_MVNO">LG U+ 알뜰폰</option>
      </select>
    </div>

    <div class="input-group">
      <label for="phoneNumber">휴대폰번호</label>
      <input
        id="phoneNumber"
        v-model="form.phoneNumber"
        maxlength="11"
        inputmode="numeric"
        placeholder="'-' 없이 입력해주세요."
        type="text"
        :readonly="isPhoneChange"
        @input="formatPhoneNumber"
      />
    </div>

    <p v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </p>
  </form>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';

const props = defineProps({
  initialValue: {
    type: Object,
    default: () => ({}),
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['submit']);
const errorMessage = ref('');

const isPhoneChange = computed(
  () => props.initialValue.verificationPurpose === 'PHONE_CHANGE',
);
const isNameChange = computed(
  () => props.initialValue.verificationPurpose === 'NAME_CHANGE',
);

const form = reactive({
  userName: props.initialValue.userName || '',
  birthDate: props.initialValue.birthDate || '',
  carrierCode: props.initialValue.carrierCode || '',
  phoneNumber: props.initialValue.phoneNumber || '',
});

const formatPhoneNumber = () => {
  if (isPhoneChange.value) return;
  form.phoneNumber = form.phoneNumber.replace(/[^0-9]/g, '');
};

const validate = () => {
  if (!form.userName) return '이름을 입력해주세요.';
  if (!form.birthDate) return '생년월일을 입력해주세요.';
  if (!form.carrierCode) return '통신사를 선택해주세요.';
  if (!/^01[016789][0-9]{7,8}$/.test(form.phoneNumber))
    return '휴대폰번호를 확인해주세요.';
  return '';
};

const submit = () => {
  errorMessage.value = validate();
  if (errorMessage.value) return;

  emit('submit', {
    ...form,
    verificationPurpose: props.initialValue.verificationPurpose || 'SIGN_UP',
  });
};

// 부모에서 버튼을 눌렀을 때 실행할 수 있도록 노출
defineExpose({
  submitForm: submit,
});
</script>

<style scoped>
.phone-form {
  display: flex;
  flex-direction: column;
}

.input-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 22px;
}

.input-group label {
  margin-bottom: 9px;
  color: #333333;
  font-size: 14px;
  font-weight: 600;
}

.input-group input,
.input-group select {
  width: 100%;
  height: 52px;
  padding: 0 14px;
  border: 1px solid #dddddd;
  border-radius: 8px;
  background: #ffffff;
  color: #222222;
  font-size: 16px;
  outline: none;
}

.input-group input:focus,
.input-group select:focus {
  border-color: #ffbc2e;
}

.input-group input:read-only {
  background: #f7f7f7;
  color: #777777;
}

.error-message {
  margin: 0;
  color: #d32f2f;
  font-size: 14px;
}
</style>
