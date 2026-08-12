<template>
  <form class="phone-form" @submit.prevent>
    <!-- 1단계: 이름 -->
    <div v-if="currentStep === 1" class="current-input">
      <label for="userName">이름</label>

      <input
          id="userName"
          v-model="form.userName"
          type="text"
          maxlength="7"
          :readonly="isNameChange"
          @input="handleNameInput"
          @compositionstart="handleCompositionStart"
          @compositionend="handleCompositionEnd"
      />
    </div>

    <!-- 2단계: 생년월일 -->
    <div v-if="currentStep === 2" class="current-input">
      <label for="birthDate">생년월일 (8자리)</label>

      <input
          id="birthDate"
          v-model="birthDateInput"
          type="text"
          inputmode="numeric"
          maxlength="10"
          placeholder="YYYY-MM-DD"
          @input="handleBirthDateInput"
      />
    </div>

    <!-- 3단계: 통신사 -->
    <div v-if="currentStep === 3" class="current-input">
      <label>통신사</label>

      <button
          type="button"
          class="carrier-select"
          @click="openCarrierSheet"
      >
    <span
        v-if="form.carrierCode"
        class="carrier-select-text"
    >
      {{ carrierName }}
    </span>

        <span v-else class="carrier-select-empty"></span>

        <i class="fa-solid fa-chevron-down carrier-arrow"></i>
      </button>
    </div>

    <!-- 4단계: 휴대폰번호 -->
    <div v-if="currentStep === 4" class="current-input">
      <label for="phoneNumber">휴대폰번호</label>

      <input
          id="phoneNumber"
          v-model="phoneNumberInput"
          type="text"
          inputmode="numeric"
          maxlength="13"
          placeholder="'-' 없이 입력해주세요."
          :readonly="isPhoneChange"
          @input="handlePhoneNumberInput"
      />
    </div>

    <!-- 이전 입력 완료 정보 -->
    <div class="completed-fields">
      <!-- 통신사 -->
      <div v-if="currentStep > 3 && form.carrierCode" class="completed-field">
        <span class="completed-label">통신사</span>
        <strong>{{ carrierName }}</strong>
      </div>

      <!-- 생년월일 -->
      <div v-if="currentStep > 2 && birthDateInput" class="completed-field">
        <span class="completed-label">생년월일</span>
        <strong>{{ birthDateInput }}</strong>
      </div>

      <!-- 이름 -->
      <div v-if="currentStep > 1 && form.userName" class="completed-field">
        <span class="completed-label">이름</span>
        <strong>{{ form.userName }}</strong>
      </div>
    </div>

    <!-- 에러 메시지 -->
    <p v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </p>

    <!-- 통신사 바텀시트 -->
    <Teleport to="body">
      <div
          v-if="showCarrierSheet"
          class="carrier-overlay"
          @click.self="closeCarrierSheet"
      >
        <div class="carrier-sheet">
          <div class="carrier-sheet-header">
            <h2>통신사 선택</h2>

            <button
                type="button"
                class="carrier-close-btn"
                aria-label="닫기"
                @click="closeCarrierSheet"
            >
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>

          <div class="carrier-list">
            <button
                v-for="carrier in carriers"
                :key="carrier.code"
                type="button"
                class="carrier-item"
                @click="selectCarrier(carrier)"
            >
              {{ carrier.name }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </form>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, ref } from 'vue';

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

const emit = defineEmits(['submit', 'step-change', 'phone-valid-change']);

// 현재 단계
const currentStep = ref(1);

// 에러 메시지
const errorMessage = ref('');

// 통신사 바텀시트
const showCarrierSheet = ref(false);

// 이름 자동 이동 타이머
let nameTimer = null;

// 한글 조합 여부
const isComposing = ref(false);

// 인증 목적
const isPhoneChange = computed(() => props.initialValue.verificationPurpose === 'PHONE_CHANGE');
const isNameChange = computed(() => props.initialValue.verificationPurpose === 'NAME_CHANGE');

// 폼
const form = reactive({
  userName: props.initialValue.userName || '',
  birthDate: props.initialValue.birthDate || '',
  carrierCode: props.initialValue.carrierCode || '',
  phoneNumber: props.initialValue.phoneNumber || '',
});

// 통신사 목록
const carriers = [
  { code: 'SKT', name: 'SKT' },
  { code: 'KT', name: 'KT' },
  { code: 'LGU', name: 'LG U+' },
  { code: 'SKT_MVNO', name: 'SKT 알뜰폰' },
  { code: 'KT_MVNO', name: 'KT 알뜰폰' },
  { code: 'LGU_MVNO', name: 'LG U+ 알뜰폰' },
];

// 생년월일 화면 입력값
const birthDateInput = ref(formatBirthDate(props.initialValue.birthDate || ''));

// 휴대폰번호 화면 입력값
const phoneNumberInput = ref(formatPhoneNumber(props.initialValue.phoneNumber || ''));

// 통신사 표시 이름
const carrierName = computed(() => {
  const carrier = carriers.find((item) => item.code === form.carrierCode);
  return carrier?.name || '';
});

// API 전송용 휴대폰번호
const rawPhoneNumber = computed(() => phoneNumberInput.value.replace(/[^0-9]/g, ''));

// 이름 유효성
const isValidUserName = () => /^[가-힣]{2,7}$/.test(form.userName.trim());

// 휴대폰번호 유효성
const isValidPhoneNumber = () => /^01[016789][0-9]{7,8}$/.test(rawPhoneNumber.value);

// 단계 이동
const moveToStep = (step) => {
  currentStep.value = step;
  errorMessage.value = '';
  emit('step-change', step);
};

// 이름 입력
const handleNameInput = () => {
  if (isNameChange.value || isComposing.value) return;
  scheduleNameNext();
};

// 한글 조합 시작
const handleCompositionStart = () => {
  isComposing.value = true;
  clearTimeout(nameTimer);
};

// 한글 조합 종료
const handleCompositionEnd = () => {
  isComposing.value = false;
  scheduleNameNext();
};

// 이름 자동 이동
const scheduleNameNext = () => {
  clearTimeout(nameTimer);

  nameTimer = setTimeout(() => {
    if (!isValidUserName()) return;
    moveToStep(2);
  }, 700);
};

// 생년월일 포맷
function formatBirthDate(value) {
  const numbers = String(value).replace(/[^0-9]/g, '').slice(0, 8);

  if (numbers.length <= 4) return numbers;
  if (numbers.length <= 6) return `${numbers.slice(0, 4)}-${numbers.slice(4)}`;

  return `${numbers.slice(0, 4)}-${numbers.slice(4, 6)}-${numbers.slice(6)}`;
}

// 생년월일 입력
const handleBirthDateInput = () => {
  birthDateInput.value = formatBirthDate(birthDateInput.value);

  const numbers = birthDateInput.value.replace(/[^0-9]/g, '');

  if (numbers.length !== 8) {
    errorMessage.value = '';
    return;
  }

  if (!isValidBirthDate()) {
    errorMessage.value = '생년월일을 확인해주세요.';
    return;
  }

  moveToStep(3);
};

// 생년월일 유효성 검사
const isValidBirthDate = () => {
  const value = birthDateInput.value.replace(/[^0-9]/g, '');

  if (!/^\d{8}$/.test(value)) return false;

  const year = Number(value.slice(0, 4));
  const month = Number(value.slice(4, 6));
  const day = Number(value.slice(6, 8));
  const date = new Date(year, month - 1, day);

  return date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day;
};

// 통신사 바텀시트 열기
const openCarrierSheet = () => {
  showCarrierSheet.value = true;
};

// 통신사 바텀시트 닫기
const closeCarrierSheet = () => {
  showCarrierSheet.value = false;
};

// 통신사 선택
const selectCarrier = (carrier) => {
  form.carrierCode = carrier.code;
  closeCarrierSheet();
  moveToStep(4);
};

// 휴대폰번호 포맷
function formatPhoneNumber(value) {
  const numbers = String(value).replace(/[^0-9]/g, '').slice(0, 11);

  if (numbers.length <= 3) return numbers;
  if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;

  return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7)}`;
}

// 휴대폰번호 입력
const handlePhoneNumberInput = () => {
  if (isPhoneChange.value) return;

  phoneNumberInput.value = formatPhoneNumber(phoneNumberInput.value);
  errorMessage.value = '';

  emit('phone-valid-change', isValidPhoneNumber());
};

// 이전 단계
const previousStep = () => {
  if (showCarrierSheet.value) {
    closeCarrierSheet();
    return true;
  }

  if (currentStep.value <= 1) return false;

  moveToStep(currentStep.value - 1);

  return true;
};

// 인증번호 받기
const submitForm = () => {
  if (props.loading) return;

  if (!isValidPhoneNumber()) {
    errorMessage.value = '휴대폰번호를 확인해주세요.';
    return;
  }

  form.birthDate = birthDateInput.value;
  form.phoneNumber = rawPhoneNumber.value;

  emit('submit', {
    ...form,
    verificationPurpose: props.initialValue.verificationPurpose || 'SIGN_UP',
  });
};

onBeforeUnmount(() => {
  clearTimeout(nameTimer);
});

defineExpose({
  previousStep,
  submitForm,
});
</script>

<style scoped>
.phone-form {
  display: flex;
  flex-direction: column;
  padding: 0 4px;
}

/* 현재 입력 */
.current-input {
  display: flex;
  flex-direction: column;
}

.current-input label {
  margin-bottom: 8px;
  color: #999999;
  font-size: 15px;
  font-weight: 500;
}

.current-input input {
  width: 100%;
  height: 52px;
  padding: 0;
  border: 0;
  border-bottom: 2px solid #eeeeee;
  border-radius: 0;
  background: #ffffff;
  color: #111111;
  font-size: 20px;
  font-weight: 600;
  outline: none;
  box-sizing: border-box;
}

.current-input input:focus {
  border-bottom-color: #ffbc2e;
}

.current-input input::placeholder {
  color: #bbbbbb;
}

/* 통신사 선택 영역 */
.carrier-select {
  width: 100%;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0;
  border: 0;
  border-bottom: 2px solid #eeeeee;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  cursor: pointer;
}

/* 통신사 선택값 */
.carrier-select-text {
  flex: 1;
  display: block;
  min-width: 0;
  padding: 0;
  margin: 0;
  background: transparent;
  color: #111111;
  font-size: 22px;
  font-weight: 500;
  line-height: 1.4;
  text-align: left;
}

/* 통신사 placeholder */
.carrier-select-empty {
  flex: 1;
}

/* 오른쪽 화살표 */
.carrier-arrow {
  flex-shrink: 0;
  color: #555555;
  font-size: 18px;
}

/* 이전 입력값 */
.completed-fields {
  display: flex;
  flex-direction: column;
  margin-top: 28px;
}

.completed-field {
  display: flex;
  flex-direction: column;
  padding: 18px 0;
  border-bottom: 1px solid #eeeeee;
}

.completed-label {
  margin-bottom: 7px;
  color: #aaaaaa;
  font-size: 14px;
  font-weight: 500;
}

.completed-field strong {
  color: #222222;
  font-size: 18px;
  font-weight: 500;
}

/* 에러 */
.error-message {
  margin: 16px 0 0;
  color: #d32f2f;
  font-size: 14px;
}

/* 통신사 바텀시트 배경 */
.carrier-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(0, 0, 0, 0.65);
}

/* 통신사 바텀시트 */
.carrier-sheet {
  width: 100%;
  max-width: 430px;
  padding: 28px 24px 34px;
  border-radius: 28px 28px 0 0;
  background: #ffffff;
  box-sizing: border-box;
}

/* 바텀시트 헤더 */
.carrier-sheet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.carrier-sheet-header h2 {
  margin: 0;
  color: #111111;
  font-size: 20px;
  font-weight: 600;
}

.carrier-close-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #777777;
  font-size: 24px;
  cursor: pointer;
}

/* 통신사 목록 */
.carrier-list {
  display: flex;
  flex-direction: column;
}

.carrier-item {
  width: 100%;
  min-height: 58px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 18px;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
}

.carrier-item:active {
  background: #f7f7f7;
}
</style>