import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useWordFilterStore = defineStore('wordFilter', () => {
  // 💡 공통 금칙어 목록 (필요에 따라 계속 추가 가능)
  const forbiddenWords = ref([
    '바보',
    '욕설',
    '시발',
    '개새끼',
    '매춘',
    '도박',
  ]);

  // 💡 텍스트 검증 공통 함수
  const validateText = (text) => {
    if (!text) return { isValid: true, text: '' };

    const trimmed = text.trim();
    const isContainsForbidden = forbiddenWords.value.some((word) =>
      trimmed.includes(word),
    );

    if (isContainsForbidden) {
      return {
        isValid: false,
        message: '사용할 수 없는 단어가 포함되어 있습니다.',
      };
    }

    return { isValid: true, text: trimmed };
  };

  // 💡 동적으로 금칙어를 추가해야 할 경우를 위한 함수 (선택사항)
  const addForbiddenWord = (word) => {
    if (!forbiddenWords.value.includes(word)) {
      forbiddenWords.value.push(word);
    }
  };

  return {
    forbiddenWords,
    validateText,
    addForbiddenWord,
  };
});
