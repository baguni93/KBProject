<template>
  <!-- 카드 정보 입력 폼 -->
  <div class="card-info-form-container">
    <!-- 카드 별명 (수정 가능) -->
    <div class="section">
      <label class="section-title">카드 별명</label>
      <input
        v-model="cardNickname"
        type="text"
        placeholder="카드 별명을 입력해주세요"
        class="text-input"
      />
    </div>

    <!-- 카드 사용 시 사용할 비밀번호 4자리 설정 (*** 처리) -->
    <div class="section">
      <label class="section-title">카드 비밀번호 (4자리)</label>
      <input
        v-model="cardPassword"
        type="password"
        inputmode="numeric"
        maxlength="4"
        placeholder="비밀번호 4자리를 입력해주세요"
        class="text-input password-input"
        @input="handlePasswordInput"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useCardEditorStore } from '@/stores/cardEditorStore';

const emit = defineEmits(['update:isValid']);
const cardStore = useCardEditorStore();

const cardNickname = ref(cardStore.cardName);
const cardOption = ref('DOMESTIC');
const hasTransit = ref(true);
const transitLimit = ref('150000');
const cardPassword = ref('');

/* 생년월일을 바탕으로 성인 여부 판별 */
const isAdult = computed(() => {
  const birthStr = cardStore.userBirth || '2000-01-01';
  const birthDate = new Date(birthStr);
  const today = new Date();

  let age = today.getFullYear() - birthDate.getFullYear();
  const m = today.getMonth() - birthDate.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
    age--;
  }
  return age >= 19;
});

/* 비밀번호 숫자만 허용 및 최대 4자리 제한 */
const handlePasswordInput = (e) => {
  const value = e.target.value.replace(/[^0-9]/g, '').slice(0, 4);
  cardPassword.value = value;
  e.target.value = value;
};

/* 모든 필수 내용이 채워졌는지 검사하는 변수 (computed) */
const isFormValid = computed(() => {
  const isNicknameValid = cardNickname.value.trim() !== '';
  const isOptionValid = cardOption.value !== '';
  const isPasswordValid = cardPassword.value.length === 4;
  const isTransitValid =
    !isAdult.value || !hasTransit.value || transitLimit.value !== '';

  return isNicknameValid && isOptionValid && isPasswordValid && isTransitValid;
});

// 값이 바뀔 때마다 부모에게 유효성 상태 및 데이터 동기화 전달
watch(
  [
    isFormValid,
    cardNickname,
    cardOption,
    hasTransit,
    transitLimit,
    cardPassword,
  ],
  ([valid]) => {
    emit('update:isValid', valid);
    if (valid) {
      cardStore.cardName = cardNickname.value;
      cardStore.cardOption = cardOption.value;
      cardStore.hasTransit = isAdult.value ? hasTransit.value : false;
      cardStore.transitLimit =
        isAdult.value && hasTransit.value ? transitLimit.value : null;
      cardStore.cardPassword = cardPassword.value;
    }
  },
  { immediate: true },
);
</script>

<style scoped>
.card-info-form-container {
  width: 100%;
  max-width: 340px;
  margin: 0 auto;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  gap: 18px; /* 항목들 사이의 위아래 간격을 균격하게 유지 */
  box-sizing: border-box;
}

.section-title-area {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  text-align: left;
  margin-bottom: -2px;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 6px; /* 라벨과 입력창 사이의 간격 */
}

.section-title {
  font-size: 11px;
  color: #888888;
  font-weight: 600;
  text-align: left;
}

.text-input {
  width: 100%;
  height: 44px;
  background-color: #f4f5f7;
  border: 1px solid transparent;
  border-radius: 22px;
  padding: 0 16px;
  font-size: 13px;
  outline: none;
  box-sizing: border-box;
  transition: all 0.2s ease;
}

.text-input:focus {
  border-color: #ffc107;
  background-color: #ffffff;
}

/* 비밀번호 입력 시 점(***) 간격 최적화 및 플레이스홀더 정상 정렬 */
.password-input {
  letter-spacing: 3px;
}

.password-input::placeholder {
  letter-spacing: normal;
}

.select-input {
  appearance: none;
  background-color: #f4f5f7;
}

/* 옵션 선택 버튼 그룹 스타일 (국내용/해외겸용, 교통카드 신청여부) */
.option-button-group {
  display: flex;
  gap: 8px;
  width: 100%;
}

.option-btn {
  flex: 1;
  height: 44px;
  background-color: #f4f5f7;
  border: 1px solid transparent;
  border-radius: 22px;
  font-size: 13px;
  font-weight: 500;
  color: #555;
  cursor: pointer;
  transition: all 0.2s ease;
}

.option-btn.active {
  background-color: #fff9e6;
  border-color: #ffc107;
  color: #222;
  font-weight: bold;
}
</style>
