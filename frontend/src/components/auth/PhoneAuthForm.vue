<template>
  <form class="phone-form" @submit.prevent>
    <!-- 현재 입력 단계 -->
    <Transition name="step-slide" mode="out-in">
      <div :key="currentStep" class="current-input">
        <!-- 1단계: 이름 -->
        <template v-if="currentStep === 1">
          <label for="userName">이름</label>

          <div class="input-wrapper">
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

            <button
              v-if="form.userName && !isNameChange"
              class="clear-input-button"
              type="button"
              aria-label="이름 전체 삭제"
              @click="clearUserName"
            >
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>
        </template>

        <!-- 2단계: 생년월일 -->
        <template v-else-if="currentStep === 2">
          <label for="birthDate">생년월일 (8자리)</label>

          <div class="input-wrapper">
            <input
              id="birthDate"
              v-model="birthDateInput"
              type="text"
              inputmode="numeric"
              maxlength="10"
              placeholder="YYYY-MM-DD"
              @input="handleBirthDateInput"
            />

            <button
              v-if="birthDateInput"
              class="clear-input-button"
              type="button"
              aria-label="생년월일 전체 삭제"
              @click="clearBirthDate"
            >
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>
        </template>

        <!-- 3단계: 통신사 -->
        <template v-else-if="currentStep === 3">
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

            <span
              v-else
              class="carrier-select-placeholder"
            >
              통신사 선택
            </span>

            <i
              class="fa-solid fa-chevron-down carrier-arrow"
            ></i>
          </button>
        </template>

        <!-- 4단계: 휴대폰번호 -->
        <template v-else-if="currentStep === 4">
          <label for="phoneNumber">휴대폰번호</label>

          <div class="input-wrapper">
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

            <button
              v-if="phoneNumberInput && !isPhoneChange"
              class="clear-input-button"
              type="button"
              aria-label="휴대폰번호 전체 삭제"
              @click="clearPhoneNumber"
            >
              <i class="fa-solid fa-xmark"></i>
            </button>
          </div>
        </template>
      </div>
    </Transition>

    <!-- 이전 입력 완료 정보 -->
    <TransitionGroup
      name="completed-field"
      tag="div"
      class="completed-fields"
    >
      <!-- 통신사 -->
      <div
        v-if="currentStep > 3 && form.carrierCode"
        key="carrier"
        class="completed-field"
      >
        <span class="completed-label">
          통신사
        </span>
        <strong>{{ carrierName }}</strong>
      </div>

      <!-- 생년월일 -->
      <div
        v-if="currentStep > 2 && birthDateInput"
        key="birthDate"
        class="completed-field"
      >
        <span class="completed-label">
          생년월일
        </span>
        <strong>{{ birthDateInput }}</strong>
      </div>

      <!-- 이름 -->
      <div
        v-if="currentStep > 1 && form.userName"
        key="userName"
        class="completed-field"
      >
        <span class="completed-label">
          이름
        </span>
        <strong>{{ form.userName }}</strong>
      </div>
    </TransitionGroup>

    <!-- 에러 메시지 -->
    <p
      v-if="errorMessage"
      class="error-message"
    >
      {{ errorMessage }}
    </p>

    <!-- 통신사 바텀시트 -->
    <Teleport to="body">
      <Transition name="sheet">
        <div
          v-if="showCarrierSheet"
          class="carrier-overlay"
          @click.self="closeCarrierSheet"
        >
          <div class="carrier-sheet">
            <div class="sheet-handle"></div>

            <div class="carrier-sheet-header">
              <div>
                <h2>통신사 선택</h2>
                <p>
                  이용 중인 통신사를 선택해 주세요.
                </p>
              </div>
            </div>

            <div class="carrier-list">
              <button
                v-for="carrier in carriers"
                :key="carrier.code"
                type="button"
                class="carrier-item"
                :class="{
                  selected:
                    form.carrierCode === carrier.code,
                }"
                @click="selectCarrier(carrier)"
              >
                <span>{{ carrier.name }}</span>

                <i
                  v-if="
                    form.carrierCode === carrier.code
                  "
                  class="fa-solid fa-check"
                ></i>
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </form>
</template>

<script setup>
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
} from 'vue';

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

const emit = defineEmits([
  'submit',
  'step-change',
  'phone-valid-change',
]);

const errorMessage = ref('');
const showCarrierSheet = ref(false);
const isComposing = ref(false);

let nameTimer = null;

// 인증 목적
const verificationPurpose = computed(
  () =>
    props.initialValue.verificationPurpose ||
    'SIGN_UP',
);

const isPhoneChange = computed(
  () =>
    verificationPurpose.value ===
    'PHONE_CHANGE',
);

const isNameChange = computed(
  () =>
    verificationPurpose.value ===
    'NAME_CHANGE',
);

const isSettingChange = computed(
  () =>
    isPhoneChange.value ||
    isNameChange.value,
);

// 폼
const form = reactive({
  userName:
    props.initialValue.userName || '',
  birthDate:
    props.initialValue.birthDate || '',
  carrierCode:
    props.initialValue.carrierCode || '',
  phoneNumber:
    props.initialValue.phoneNumber || '',
});

// 통신사 목록
const carriers = [
  {
    code: 'SKT',
    name: 'SKT',
  },
  {
    code: 'KT',
    name: 'KT',
  },
  {
    code: 'LGU',
    name: 'LG U+',
  },
  {
    code: 'SKT_MVNO',
    name: 'SKT 알뜰폰',
  },
  {
    code: 'KT_MVNO',
    name: 'KT 알뜰폰',
  },
  {
    code: 'LGU_MVNO',
    name: 'LG U+ 알뜰폰',
  },
];

// 생년월일 화면 입력값
const birthDateInput = ref(
  formatBirthDate(
    props.initialValue.birthDate || '',
  ),
);

// 휴대폰번호 화면 입력값
const phoneNumberInput = ref(
  formatPhoneNumber(
    props.initialValue.phoneNumber || '',
  ),
);

// PIN 재설정 여부
const isPinReset = computed(
    () =>
        verificationPurpose.value ===
        'PIN_RESET',
);

// 초기 단계
const getInitialStep = () => {
  if (isSettingChange.value) {
    return form.carrierCode ? 4 : 3;
  }

  // PIN 재설정 시 이미 전달받은 정보는 건너뜀
  if (isPinReset.value) {
    if (form.carrierCode) {
      return 4;
    }

    if (form.birthDate) {
      return 3;
    }

    if (form.userName) {
      return 2;
    }
  }

  return 1;
};

const currentStep = ref(
    getInitialStep(),
);

// 통신사 표시 이름
const carrierName = computed(() => {
  const carrier = carriers.find(
    (item) =>
      item.code === form.carrierCode,
  );

  return carrier?.name || '';
});

// API 전송용 휴대폰번호
const rawPhoneNumber = computed(
  () =>
    phoneNumberInput.value.replace(
      /[^0-9]/g,
      '',
    ),
);

// 이름 유효성
// const isValidUserName = () =>
//   /^[가-힣]{2,7}$/.test(
//     form.userName.trim(),
//   );

const isValidUserName = () =>
    /^[가-힣0-9]{2,10}$/.test(
        form.userName.trim(),
    );

// 휴대폰번호 유효성
const isValidPhoneNumber = () =>
  /^01[016789][0-9]{8}$/.test(
    rawPhoneNumber.value,
  );

// 단계 이동
const moveToStep = async (step) => {
  currentStep.value = step;
  errorMessage.value = '';

  emit('step-change', step);

  await nextTick();

  if (step === 4) {
    emit(
      'phone-valid-change',
      isValidPhoneNumber(),
    );
  }
};

// 이름 입력
const handleNameInput = () => {
  if (
    isNameChange.value ||
    isComposing.value
  ) {
    return;
  }

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

// 이름 전체 삭제
const clearUserName = () => {
  clearTimeout(nameTimer);

  form.userName = '';
  errorMessage.value = '';
};

// 생년월일 포맷
function formatBirthDate(value) {
  const numbers = String(value)
    .replace(/[^0-9]/g, '')
    .slice(0, 8);

  if (numbers.length <= 4) {
    return numbers;
  }

  if (numbers.length <= 6) {
    return `${numbers.slice(
      0,
      4,
    )}-${numbers.slice(4)}`;
  }

  return `${numbers.slice(
    0,
    4,
  )}-${numbers.slice(
    4,
    6,
  )}-${numbers.slice(6)}`;
}

// 생년월일 입력
const handleBirthDateInput = () => {
  birthDateInput.value =
    formatBirthDate(
      birthDateInput.value,
    );

  const numbers =
    birthDateInput.value.replace(
      /[^0-9]/g,
      '',
    );

  if (numbers.length !== 8) {
    errorMessage.value = '';
    return;
  }

  if (!isValidBirthDate()) {
    errorMessage.value =
      '생년월일을 확인해주세요.';
    return;
  }

  moveToStep(3);
};

// 생년월일 전체 삭제
const clearBirthDate = () => {
  birthDateInput.value = '';
  form.birthDate = '';
  errorMessage.value = '';
};

// 생년월일 유효성 검사
const isValidBirthDate = () => {
  const value =
    birthDateInput.value.replace(
      /[^0-9]/g,
      '',
    );

  if (!/^\d{8}$/.test(value)) {
    return false;
  }

  const year = Number(
    value.slice(0, 4),
  );

  const month = Number(
    value.slice(4, 6),
  );

  const day = Number(
    value.slice(6, 8),
  );

  const date = new Date(
    year,
    month - 1,
    day,
  );

  return (
    date.getFullYear() === year &&
    date.getMonth() === month - 1 &&
    date.getDate() === day
  );
};

// 통신사 바텀시트
const openCarrierSheet = () =>
  (showCarrierSheet.value = true);

const closeCarrierSheet = () =>
  (showCarrierSheet.value = false);

// 통신사 선택
const selectCarrier = async (
  carrier,
) => {
  form.carrierCode = carrier.code;

  closeCarrierSheet();

  await moveToStep(4);
};

// 휴대폰번호 포맷
function formatPhoneNumber(value) {
  const numbers = String(value)
    .replace(/[^0-9]/g, '')
    .slice(0, 11);

  if (numbers.length <= 3) {
    return numbers;
  }

  if (numbers.length <= 7) {
    return `${numbers.slice(
      0,
      3,
    )}-${numbers.slice(3)}`;
  }

  return `${numbers.slice(
    0,
    3,
  )}-${numbers.slice(
    3,
    7,
  )}-${numbers.slice(7)}`;
}

// 휴대폰번호 입력
const handlePhoneNumberInput = () => {
  if (isPhoneChange.value) return;

  phoneNumberInput.value =
    formatPhoneNumber(
      phoneNumberInput.value,
    );

  errorMessage.value = '';

  emit(
    'phone-valid-change',
    isValidPhoneNumber(),
  );
};

// 휴대폰번호 전체 삭제
const clearPhoneNumber = () => {
  phoneNumberInput.value = '';
  form.phoneNumber = '';
  errorMessage.value = '';

  emit(
    'phone-valid-change',
    false,
  );
};

// 이전 단계
const previousStep = () => {
  if (showCarrierSheet.value) {
    closeCarrierSheet();
    return true;
  }

  if (
    isSettingChange.value &&
    currentStep.value <= 3
  ) {
    return false;
  }

  if (currentStep.value <= 1) {
    return false;
  }

  if (
    isSettingChange.value &&
    currentStep.value === 4
  ) {
    moveToStep(3);
    return true;
  }

  moveToStep(
    currentStep.value - 1,
  );

  return true;
};

// 인증번호 받기
const submitForm = () => {
  if (props.loading) return;

  if (!isValidPhoneNumber()) {
    errorMessage.value =
      '휴대폰번호 11자리를 확인해주세요.';

    emit(
      'phone-valid-change',
      false,
    );

    return;
  }

  form.birthDate =
    birthDateInput.value;

  form.phoneNumber =
    rawPhoneNumber.value;

  emit('submit', {
    ...form,
    verificationPurpose:
      verificationPurpose.value,
  });
};

onMounted(() => {
  emit(
    'step-change',
    currentStep.value,
  );

  if (currentStep.value === 4) {
    emit(
      'phone-valid-change',
      isValidPhoneNumber(),
    );
  }
});

onBeforeUnmount(() => {
  clearTimeout(nameTimer);
});

defineExpose({
  previousStep,
  submitForm,
});
</script>

<style scoped>
/* 공통 세로 배치 */
.phone-form,
.current-input,
.completed-fields,
.completed-field,
.carrier-list {
  display: flex;
  flex-direction: column;
}

/* 폼 */
.phone-form {
  padding: 0 4px;
  overflow-x: hidden;
}

/* 현재 입력 */
.current-input label {
  margin-bottom: 8px;
  color: #999999;
  font-size: 15px;
  font-weight: 500;
}

/* 입력 wrapper */
.input-wrapper {
  position: relative;
  width: 100%;
}

.current-input input {
  width: 100%;
  height: 52px;
  padding: 0 42px 0 0;
  border: 0;
  border-bottom: 2px solid #eeeeee;
  border-radius: 0;
  background: #ffffff;
  color: #111111;
  font-size: 20px;
  font-weight: 600;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s ease;
}

.current-input input:focus {
  border-bottom-color: #ffbc2e;
}

/* 입력 placeholder + 통신사 placeholder */
.current-input input::placeholder,
.carrier-select-placeholder {
  color: #bbbbbb;
  font-size: 18px;
  font-weight: 400;
}

.current-input input[readonly] {
  color: #555555;
  cursor: default;
}

/* 전체 삭제 버튼 */
.clear-input-button {
  position: absolute;
  top: 50%;
  right: 2px;
  display: flex;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: #f3f4f6;
  color: #888888;
  font-size: 13px;
  cursor: pointer;
  transform: translateY(-50%);
}

.clear-input-button:active {
  background: #e8e9eb;
}

/* 통신사 선택 영역 */
.carrier-select {
  display: flex;
  width: 100%;
  height: 52px;
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

/* 통신사 선택 텍스트 공통 */
.carrier-select-text,
.carrier-select-placeholder {
  display: block;
  min-width: 0;
  flex: 1;
  margin: 0;
  padding: 0;
  line-height: 1.4;
  text-align: left;
}

.carrier-select-text {
  color: #111111;
  font-size: 20px;
  font-weight: 600;
}

/* 오른쪽 화살표 */
.carrier-arrow {
  flex-shrink: 0;
  color: #777777;
  font-size: 16px;
}

/* 이전 입력값 */
.completed-fields {
  margin-top: 28px;
}

.completed-field {
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

/* 단계 전환 */
.step-slide-enter-active,
.step-slide-leave-active {
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.step-slide-enter-from {
  opacity: 0;
  transform: translateX(18px);
}

.step-slide-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}

/* 완료 필드 등장 */
.completed-field-enter-active,
.completed-field-leave-active {
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.completed-field-enter-from,
.completed-field-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* 통신사 바텀시트 배경 */
.carrier-overlay {
  position: fixed;
  z-index: 1000;
  inset: 0;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(0, 0, 0, 0.48);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
}

/* 통신사 바텀시트 */
.carrier-sheet {
  width: 100%;
  max-width: 430px;
  padding: 10px 24px 30px;
  border-radius: 28px 28px 0 0;
  background: #ffffff;
  box-sizing: border-box;
}

.sheet-handle {
  width: 42px;
  height: 4px;
  margin: 0 auto 22px;
  border-radius: 999px;
  background: #dddddd;
}

/* 바텀시트 헤더 */
.carrier-sheet-header {
  margin-bottom: 18px;
}

.carrier-sheet-header h2 {
  margin: 0;
  color: #111111;
  font-size: 20px;
  font-weight: 700;
}

.carrier-sheet-header p {
  margin: 7px 0 0;
  color: #888888;
  font-size: 13px;
  font-weight: 400;
}

/* 통신사 목록 */
.carrier-item {
  display: flex;
  width: 100%;
  min-height: 56px;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
  border: 0;
  border-bottom: 1px solid #eeeeee;
  background: transparent;
  color: #555555;
  font-size: 17px;
  font-weight: 500;
  text-align: left;
  cursor: pointer;
}

.carrier-item:last-child {
  border-bottom: 0;
}

.carrier-item.selected {
  color: #111111;
  font-weight: 700;
}

.carrier-item i {
  color: #ffbc2e;
  font-size: 13px;
}

.carrier-item:active {
  background: #f7f7f7;
}

/* 바텀시트 애니메이션 */
.sheet-enter-active,
.sheet-leave-active {
  transition: opacity 0.22s ease;
}

.sheet-enter-active .carrier-sheet,
.sheet-leave-active .carrier-sheet {
  transition:
    transform 0.28s
    cubic-bezier(0.22, 1, 0.36, 1);
}

.sheet-enter-from,
.sheet-leave-to {
  opacity: 0;
}

.sheet-enter-from .carrier-sheet,
.sheet-leave-to .carrier-sheet {
  transform: translateY(100%);
}

@media (prefers-reduced-motion: reduce) {
  .step-slide-enter-active,
  .step-slide-leave-active,
  .completed-field-enter-active,
  .completed-field-leave-active,
  .sheet-enter-active,
  .sheet-leave-active,
  .sheet-enter-active .carrier-sheet,
  .sheet-leave-active .carrier-sheet {
    transition: none;
  }
}
</style>