<template>
  <!-- 카드 정보 입력 폼 -->
  <div class="card-info-form-container">
    <div class="section-title-area">카드 정보 입력</div>

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

    <!-- 카드 번호 (input이 아닌 깔끔한 텍스트/박스 형태로 변경) -->
    <div class="section">
      <label class="section-title">카드 번호</label>
      <div class="card-number-display">
        {{ cardNumber || '**** **** **** ****' }}
      </div>
    </div>

    <!-- 영문 이름 (수정 가능, 영어 및 공백만 허용) -->
    <div class="section">
      <label class="section-title">영문 이름 (영어 대/소문자)</label>
      <input
        :value="cardEnglishName"
        @input="handleEnglishInput"
        type="text"
        placeholder="HONG GILDONG"
        class="text-input"
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
const cardNumber = ref(cardStore.cardNumber);
const cardEnglishName = ref('');

/* 💡 영문 이름 입력 시 대문자와 공백만 허용 */
const handleEnglishInput = (e) => {
  const value = e.target.value;

  // 영문과 공백만 허용 후 대문자로 변환
  const filtered = value.replace(/[^a-zA-Z\s]/g, '').toUpperCase();

  cardEnglishName.value = filtered;
  e.target.value = filtered; // input 뷰 강제 동기화
};

/* 💡 모든 필수 내용이 채워졌는지 검사하는 변수 (computed) */
const isFormValid = computed(() => {
  return (
    cardNickname.value.trim() !== '' && cardEnglishName.value.trim() !== ''
  );
});

// 값이 바뀔 때마다 부모에게 유효성 상태 및 데이터 동기화 전달
watch(
  [isFormValid, cardNickname, cardEnglishName],
  ([valid]) => {
    emit('update:isValid', valid);
    if (valid) {
      cardStore.cardName = cardNickname.value;
      cardStore.cardEnglishName = cardEnglishName.value;
    }
  },
  { immediate: true },
);
</script>

<style scoped>
/* 스타일 (기존 입력창 스타일과 통일) */
.card-info-form-container {
  width: 100%;
  max-width: 340px;
  margin: 0 auto;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
}

.section-title-area {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  text-align: left;
  margin-bottom: -4px;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 6px;
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

/* 💡 카드 번호 전용 디스플레이 박스 스타일 */
.card-number-display {
  width: 100%;
  height: 44px;
  background-color: #eaeaea;
  color: #555;
  border-radius: 22px;
  padding: 0 16px;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  letter-spacing: 1px;
}

.submit-action-row {
  margin-top: 8px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 24px;
  background-color: #ffc107;
  border: none;
  color: #111;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.1s ease;
}

.submit-btn:active {
  transform: scale(0.98);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}
</style>
