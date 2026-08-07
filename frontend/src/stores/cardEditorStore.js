import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useCardEditorStore = defineStore('cardEditor', () => {
  /* =========================================================
   * 1. 모든 상태 (State) 최상단 선언
   * ========================================================= */
  // 배경 상태
  const color = ref('#1e40af');
  const gradient = ref('');
  const image = ref('');

  // 꾸미기 상태
  const pattern = ref('');

  // 텍스트 상태
  const texts = ref([]);
  const selectedTextId = ref(null);

  // 이모지 상태
  const emojis = ref([]);
  const selectedEmojiId = ref(null);

  // 💡 [변경] 단일 캔버스 그리기 상태 (스티커 배열 대신 전체 이미지 데이터 관리)
  const savedDrawingImage = ref(null); // 캔버스 전체 데이터 URL
  const isDrawingCleared = ref(0); // 전체 지우기 감지용 카운터 트리거

  // 브러시 설정 상태
  const drawingOptions = ref({
    mode: 'brush', // 'brush' 또는 'eraser'
    color: '#00bcd4',
    size: 8,
  });

  // 백업 및 히스토리
  const backup = ref(null);
  const history = ref(null);

  const activeEditorTab = ref('');
  const cardNumber = ref('');
  const cardName = ref('');
  const cardEnglishName = ref('');
  //카드 번호 만들기

  const createCardNumber = () => {
    cardNumber.value = Array.from({ length: 4 }, () =>
      Math.floor(Math.random() * 10000)
        .toString()
        .padStart(4, '0'),
    ).join(' ');
  };

  const setCardName = (name) => {
    cardName.value = name;
  };

  /* =========================================================
   * 2. 배경 및 패턴 관련 액션 (Actions)
   * ========================================================= */
  const setColor = (value) => {
    color.value = value;
    gradient.value = '';
    image.value = '';
    console.log('색깔저장');
  };

  const setGradient = (value) => {
    gradient.value = value;
    color.value = '';
    image.value = '';
    console.log('그라데이션 저장');
  };

  const setImage = (value) => {
    image.value = value;
    color.value = '';
    gradient.value = '';
    console.log('이미지 저장');
  };

  const setPattern = (value) => {
    pattern.value = value;
  };

  const resetBackground = () => {
    color.value = '#1e40af';
    gradient.value = '';
    image.value = '';
  };

  /* =========================================================
   * 3. 텍스트 관련 액션 (Actions)
   * ========================================================= */
  const selectText = (id) => {
    selectedTextId.value = id;
  };

  const addText = (textObj) => {
    const newId = Date.now();
    texts.value.push({
      id: newId,
      x: 50, // 카드 중앙 %
      y: 50, // 카드 중앙 %
      rotation: 0,
      font: 'sans-serif', // 기본 폰트
      size: '20px', // 기본 크기
      color: '#ffffff', // 기본 색상
      isBold: false, // 기본 굵기 여부
      ...textObj,
    });
    selectedTextId.value = newId;

    console.log(texts);
  };

  const removeText = (idOrIndex) => {
    if (typeof idOrIndex === 'number') {
      texts.value = texts.value.filter(
        (item, index) => item.id !== idOrIndex && index !== idOrIndex,
      );
    }

    if (selectedTextId.value === idOrIndex) {
      selectedTextId.value = null;
    }
  };

  const updateTextPosition = (id, xPercent, yPercent) => {
    const target = texts.value.find((t) => t.id === id);
    if (target) {
      target.x = xPercent;
      target.y = yPercent;
    }

    console.log(texts);
  };

  const updateTextRotation = (id, rotation) => {
    const target = texts.value.find((t) => t.id === id);
    if (target) {
      target.rotation = Math.round(rotation);
    }

    console.log(texts);
  };

  /* =========================================================
   * 3-1. 이모지 관련 액션 (Actions)
   * ========================================================= */
  const selectEmoji = (id) => {
    selectedEmojiId.value = id;
  };

  const addEmoji = (emojiObj) => {
    const newId = Date.now();
    emojis.value.push({
      id: newId,
      x: 50, // 카드 중앙 %
      y: 50, // 카드 중앙 %
      emojiObj,
    });
    selectedEmojiId.value = newId;

    console.log(emojis);
  };

  const removeEmoji = (idOrIndex) => {
    if (typeof idOrIndex === 'number') {
      emojis.value = emojis.value.filter(
        (item, index) => item.id !== idOrIndex && index !== idOrIndex,
      );
    }

    if (selectedEmojiId.value === idOrIndex) {
      selectedEmojiId.value = null;
    }
  };

  const updateEmojiPosition = (id, xPercent, yPercent) => {
    const target = emojis.value.find((t) => t.id === id);
    if (target) {
      target.x = xPercent;
      target.y = yPercent;
    }
  };

  const celarEmojis = () => {
    emojis.value = [];
    selectedEmojiId.value = null;
  };

  /* =========================================================
   * 3-2. 단일 캔버스 그리기(Drawing) 관련 상태 및 액션
   * ========================================================= */
  const setDrawingOptions = (options) => {
    drawingOptions.value = { ...drawingOptions.value, ...options };
  };

  const clearDrawing = () => {
    savedDrawingImage.value = null;
    isDrawingCleared.value++; // 캔버스 초기화를 감지할 수 있도록 트리거 값 증가
  };

  /* =========================================================
   * 4. 리셋 및 히스토리/스냅샷 액션 (Actions)
   * ========================================================= */
  const saveStep = () => {
    // 💡 push 대신 하나의 최신 데이터 객체로 저장
    history.value = {
      cardNumber: cardNumber.value,
      cardName: cardName.value,
      color: color.value,
      gradient: gradient.value,
      image: image.value,
      pattern: pattern.value,
      emojis: JSON.parse(JSON.stringify(emojis.value)),
      texts: JSON.parse(JSON.stringify(texts.value)),
      savedDrawingImage: savedDrawingImage.value,
    };

    console.log('저장된 단일 history:', history.value);
  };

  const reset = () => {
    cardNumber.value = '';
    cardName.value = '';
    color.value = '#1e40af';
    gradient.value = '';
    image.value = '';
    pattern.value = '';
    texts.value = [];
    emojis.value = [];
    savedDrawingImage.value = null;

    selectedTextId.value = null;
    selectedEmojiId.value = null;
    isDrawingCleared.value++;
    activeEditorTab.value = '';
  };
  const saveSnapshot = () => {
    backup.value = {
      cardNumber: cardNumber.value, // 💡 .value 추가
      cardName: cardName.value, // 💡 .value 추가
      color: color.value,
      gradient: gradient.value,
      image: image.value,
      pattern: pattern.value,
      emojis: JSON.parse(JSON.stringify(emojis.value)), // 💡 이모지도 깊은 복사 적용!
      texts: JSON.parse(JSON.stringify(texts.value)),
      savedDrawingImage: savedDrawingImage.value,
    };
    console.log('saveSnapshot 완료');
  };

  const restoreSnapshot = () => {
    if (!backup.value) return;

    color.value = backup.value.color;
    gradient.value = backup.value.gradient;
    image.value = backup.value.image;
    pattern.value = backup.value.pattern;

    texts.value = backup.value.texts
      ? JSON.parse(JSON.stringify(backup.value.texts))
      : [];

    savedDrawingImage.value = backup.value.savedDrawingImage || null;
    isDrawingCleared.value++;
  };

  /* =========================================================
   * 5. Return (외부 노출)
   * ========================================================= */
  return {
    cardNumber,
    cardName,
    cardEnglishName,
    // state
    color,
    gradient,
    image,
    pattern,
    texts,
    selectedTextId,
    emojis,
    selectedEmojiId,
    savedDrawingImage,
    isDrawingCleared,
    drawingOptions,
    backup,
    history,
    activeEditorTab,
    // methods
    setColor,
    setGradient,
    setImage,
    setPattern,
    resetBackground,
    reset,
    saveSnapshot,
    restoreSnapshot,
    saveStep,
    selectText,
    addText,
    removeText,

    selectEmoji,
    addEmoji,
    removeEmoji,
    celarEmojis,

    setDrawingOptions,
    clearDrawing,
    updateEmojiPosition,
    updateTextPosition,
    updateTextRotation,

    createCardNumber,
    setCardName,
  };
});
