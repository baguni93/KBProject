import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';
import customCardApi from '@/api/customCard.Api';
import customcard from '@/router/customcard';

export const useCardEditorStore = defineStore('cardEditor', () => {
  /* =========================================================
   * 1. 모든 상태 (State) 최상단 선언
   * ========================================================= */

  const accountNumber = ref(''); // 발급받을 계좌 Id
  // 배경 상태
  const color = ref('#1e40af');
  const gradient = ref('');
  const image = ref('');
  // 💡 [추가] 카드 칩 상태 (이미지 URL 또는 CSS 클래스명 등을 저장)
  const cardChip = ref('');

  // 꾸미기 상태
  const pattern = ref('');

  // 텍스트 상태
  const texts = ref([]);
  const selectedTextId = ref(null);

  // 이모지 상태
  const emojis = ref([]);
  const selectedEmojiId = ref(null);

  // 단일 캔버스 그리기 상태
  const savedDrawingImage = ref(null); // 캔버스 전체 데이터 URL
  const isDrawingCleared = ref(0); // 전체 지우기 감지용 카운터 트리거

  const isFeedLoad = ref(false);
  // 브러시 설정 상태
  const drawingOptions = ref({
    mode: 'brush', // 'brush' 또는 'eraser'
    color: '#00bcd4',
    size: 8,
  });

  const restoreBackground = (background) => {
    if (!background) return;

    color.value = background.color || '';
    gradient.value = background.gradient || '';
    image.value = background.image || '';

    saveSnapshot();
  };

  // 백업 및 히스토리
  const backup = ref(null);
  const history = ref(null);

  const activeEditorTab = ref('');
  const cardNumber = ref('');
  const cardName = ref('');
  const cardEnglishName = ref('');
  const customCardId = ref(0);

  // 카드 번호 만들기
  const createCardNumber = () => {
    cardNumber.value = Array.from({ length: 4 }, () =>
      Math.floor(Math.random() * 10000)
        .toString()
        .padStart(4, '0'),
    ).join(' ');
  };

  const setCardName = (name) => {
    cardName.value = name;
    saveSnapshot();
  };

  /* =========================================================
   * 2. 배경, 패턴 및 칩 관련 액션 (Actions)
   * ========================================================= */
  const setColor = (value) => {
    color.value = value;
    gradient.value = '';
    image.value = '';
    console.log('색깔저장');
    saveSnapshot();
  };

  const setGradient = (value) => {
    gradient.value = value;
    color.value = '';
    image.value = '';
    console.log('그라데이션 저장');
    saveSnapshot();
  };

  const setImage = (value) => {
    image.value = value;
    color.value = '';
    gradient.value = '';
    console.log('이미지 저장');
    saveSnapshot();
  };

  // 💡 [추가] 카드 칩 설정 액션
  const setCardChip = (chipUrl) => {
    cardChip.value = chipUrl;
    console.log('카드 칩 저장:', chipUrl);
    saveSnapshot();
  };

  const setPattern = (value) => {
    pattern.value = value;
    saveSnapshot();
  };

  const resetBackground = () => {
    color.value = '#1e40af';
    gradient.value = '';
    image.value = '';
    saveSnapshot();
  };

  /* =========================================================
   * 3. 텍스트 관련 액션 (Actions)
   * ========================================================= */
  const selectText = (id) => {
    selectedTextId.value = id;
    saveSnapshot();
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
    saveSnapshot();
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

    saveSnapshot();
  };

  const updateTextPosition = (id, xPercent, yPercent) => {
    const target = texts.value.find((t) => t.id === id);
    if (target) {
      target.x = xPercent;
      target.y = yPercent;
    }

    console.log(texts);
    saveSnapshot();
  };

  const updateTextRotation = (id, rotation) => {
    const target = texts.value.find((t) => t.id === id);
    if (target) {
      target.rotation = Math.round(rotation);
    }

    console.log(texts);
    saveSnapshot();
  };

  /* =========================================================
   * 3-1. 이모지 관련 액션 (Actions)
   * ========================================================= */
  const selectEmoji = (id) => {
    selectedEmojiId.value = id;
    saveSnapshot();
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
    saveSnapshot();
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

    saveSnapshot();
  };

  const updateEmojiPosition = (id, xPercent, yPercent) => {
    const target = emojis.value.find((t) => t.id === id);
    if (target) {
      target.x = xPercent;
      target.y = yPercent;
      saveSnapshot();
    }
  };

  const celarEmojis = () => {
    emojis.value = [];
    selectedEmojiId.value = null;
    saveSnapshot();
  };

  /* =========================================================
   * 3-2. 단일 캔버스 그리기(Drawing) 관련 상태 및 액션
   * ========================================================= */
  const setDrawingOptions = (options) => {
    drawingOptions.value = { ...drawingOptions.value, ...options };
    saveSnapshot();
  };

  const clearDrawing = () => {
    savedDrawingImage.value = null;
    isDrawingCleared.value++; // 캔버스 초기화를 감지할 수 있도록 트리거 값 증가
    saveSnapshot();
  };

  /* =========================================================
   * 4. 리셋 및 히스토리/스냅샷 액션 (Actions)
   * ========================================================= */
  const saveStep = () => {
    // 💡 [변경] history에 cardChip 상태 추가
    history.value = {
      cardNumber: cardNumber.value,
      cardName: cardName.value,

      color: color.value,
      gradient: gradient.value,
      image: image.value,
      cardChip: cardChip.value, // 👈 추가
      pattern: pattern.value,
      emojis: JSON.parse(JSON.stringify(emojis.value)),
      texts: JSON.parse(JSON.stringify(texts.value)),
      savedDrawingImage: savedDrawingImage.value,
    };

    console.log('저장된 단일 history:', history.value);
    saveSnapshot();
  };

  // blob URL을 실제 서버에 업로드하고, 서버가 반환한 파일 URL을 리턴하는 함수
  const uploadBackgroundImage = async (blobUrl) => {
    if (!blobUrl || !blobUrl.startsWith('blob:')) {
      return blobUrl; // blob이 아니면 그대로 반환
    }

    try {
      const response = await fetch(blobUrl);
      const blob = await response.blob();
      const file = new File([blob], 'background_image.jpg', {
        type: blob.type,
      });
      const formData = new FormData();
      formData.append('file', file);

      const uploadResponse = await axios.post(
        '/api/customcard/upload/attachment',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        },
      );
      return uploadResponse.data;
    } catch (error) {
      console.error('배경 이미지 업로드 실패:', error);
      throw error;
    }
  };

  const createCardPayload = async (userId, cardImageFormData) => {
    let finalImageUrl = history.value.image;

    let bgType = 'COLOR';
    let bgValue = history.value.color; // 기본값은 컬러

    if (finalImageUrl && finalImageUrl !== '') {
      bgType = 'IMAGE';
      bgValue = finalImageUrl;
    } else if (history.value.gradient && history.value.gradient !== '') {
      bgType = 'GRADIENT';
      bgValue = history.value.gradient;
    }

    // 1. blob: 이미지인 경우 서버에 먼저 업로드하고 실제 서버 URL로 교체
    if (finalImageUrl && finalImageUrl.startsWith('blob:')) {
      console.log('새로 첨부한 이미지 발견! 서버에 업로드 중...');
      finalImageUrl = await uploadBackgroundImage(finalImageUrl);

      bgType = 'ATTACHMENT';
      bgValue = finalImageUrl;
    }

    const uploadResponse = await axios.post(
      '/api/customcard/upload/cardImage',
      cardImageFormData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      },
    );

    // 💡 [변경] 페이로드에 cardChip 추가 (백엔드 DTO와 이름 맞춰야 함)
    const customCardSaveRequestDTO = {
      accountNumber: accountNumber.value,
      cardImageName: uploadResponse.data,

      userId: userId,
      cardName: history.value.cardName,
      backgroundValue: bgValue,
      cardChip: history.value.cardChip, // 👈 추가
      pattern: history.value.pattern,
      backgroundType: bgType,
      savedDrawingImage: history.value.savedDrawingImage,

      texts: (history.value.texts || []).map((t) => ({
        text: t.text,
        x: t.x,
        y: t.y,
        rotation: t.rotation,
        font: t.font,
        color: t.color,
        size: t.size,
        isBold: t.isBold,
      })),

      emojis: (history.value.emojis || []).map((e) => {
        const url = e.emojiObj?.emoji || '';
        const emojiType = url.endsWith('.svg') ? 'SVG' : 'ICON';

        return {
          x: e.x,
          y: e.y,
          rotation: e.rotation || 0,
          emojiType: emojiType,
          emojiObj: {
            emoji: e.emojiObj.emoji,
          },
        };
      }),
    };

    const response = await customCardApi.apply(customCardSaveRequestDTO);
    customCardId.value = response;
    return response;
  };

  const reset = () => {
    cardNumber.value = '';
    cardName.value = '';
    color.value = '#1e40af';
    gradient.value = '';
    image.value = '';
    cardChip.value = ''; // 👈 리셋에 추가
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
    // 💡 [변경] backup에 cardChip 추가
    backup.value = {
      cardNumber: cardNumber.value,
      cardName: cardName.value,
      color: color.value,
      gradient: gradient.value,
      image: image.value,
      cardChip: cardChip.value, // 👈 추가
      pattern: pattern.value,
      emojis: JSON.parse(JSON.stringify(emojis.value)),
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
    cardChip.value = backup.value.cardChip; // 👈 복원에 추가
    pattern.value = backup.value.pattern;
    emojis.value = backup.value.emojis
      ? JSON.parse(JSON.stringify(backup.value.emojis))
      : [];
    texts.value = backup.value.texts
      ? JSON.parse(JSON.stringify(backup.value.texts))
      : [];

    savedDrawingImage.value = backup.value.savedDrawingImage || null;
    isDrawingCleared.value++;
  };

  const getCustomCard = async (userId, targetId) => {
    const data = await customCardApi.load(userId, targetId);
    isFeedLoad.value = true;
    console.log(data);
    loadCardData(data);
  };

  /* =========================================================
   * 서버 데이터 로드 및 적용 액션 (Actions)
   * ========================================================= */
  const loadCardData = (data) => {
    if (!data) return;

    // 1. 기본 정보 설정
    cardName.value = data.cardName || '';
    cardNumber.value = data.cardNumber || '';
    customCardId.value = data.customCardId || 0;

    // 2. 배경 설정 (backgroundType에 따른 분기)
    if (data.backgroundType === 'COLOR') {
      color.value = data.backgroundValue || '#1e40af';
      gradient.value = '';
      image.value = '';
    } else if (data.backgroundType === 'GRADIENT') {
      gradient.value = data.backgroundValue || '';
      color.value = '';
      image.value = '';
    } else if (data.backgroundType === 'IMAGE') {
      // 일반 외부 URL 이미지인 경우
      image.value = data.backgroundValue || '';
      color.value = '';
      gradient.value = '';
    } else if (data.backgroundType === 'ATTACHMENT') {
      // 💡 [추가] ATTACHMENT 타입인 경우 파일 이름과 서버 경로를 조합
      // 서버에서 이미 전체 경로를 주는지, 파일명만 주는가에 따라 아래 경로를 프로젝트에 맞게 수정하세요.
      const serverAttachmentPath = '/api/customcard/cardImage/';

      if (data.backgroundValue) {
        // 이미 경로가 포함되어 있지 않다면 경로를 붙여줍니다.
        image.value =
          data.backgroundValue.startsWith('http') ||
          data.backgroundValue.startsWith('/')
            ? data.backgroundValue
            : serverAttachmentPath + data.backgroundValue;
      } else {
        image.value = '';
      }

      color.value = '';
      gradient.value = '';
    }

    // 3. 카드 칩 및 패턴 설정
    cardChip.value = data.cardChip || '';
    pattern.value = data.pattern || '';

    // 4. 드로잉(캔버스) 이미지 복원
    savedDrawingImage.value = data.savedDrawingImage || null;
    isDrawingCleared.value++;

    // 5. 텍스트 목록 복원
    texts.value = data.texts
      ? data.texts.map((t, index) => ({
          id: t.id || Date.now() + index,
          text: t.text,
          x: t.x,
          y: t.y,
          rotation: t.rotation || 0,
          font: t.font || 'sans-serif',
          color: t.color || '#ffffff',
          size: t.size || '20px',
          isBold: t.isBold || false,
        }))
      : [];
    selectedTextId.value = null;

    // 6. 이모지 목록 복원
    emojis.value = data.emojis
      ? data.emojis.map((e, index) => ({
          id: e.id || Date.now() + index,
          x: e.x,
          y: e.y,
          rotation: e.rotation || 0,
          emojiType: e.emojiType,
          emojiObj: {
            emoji: e.emojiObj?.emoji || '',
          },
        }))
      : [];
    selectedEmojiId.value = null;

    // 7. 스냅샷 동기화
    saveSnapshot();
    console.log(
      '카드 데이터를 성공적으로 에디터에 적용했습니다 (ATTACHMENT 포함).',
    );

    console.log(image.value);
  };

  /* =========================================================
   * 5. Return (외부 노출)
   * ========================================================= */
  return {
    // state
    cardNumber,
    cardName,
    cardEnglishName,
    color,
    gradient,
    image,
    cardChip, // 👈 추가
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
    customCardId,
    isFeedLoad,
    accountNumber,
    // methods
    restoreBackground,
    setColor,
    setGradient,
    setImage,
    setCardChip, // 👈 추가
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

    createCardPayload,
    getCustomCard,
  };
});
