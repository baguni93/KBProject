import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';

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
    saveSnapshot();
  };

  /* =========================================================
   * 2. 배경 및 패턴 관련 액션 (Actions)
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
    saveSnapshot();
  };

  // blob URL을 실제 서버에 업로드하고, 서버가 반환한 파일 URL을 리턴하는 함수
  const uploadBackgroundImage = async (blobUrl) => {
    if (!blobUrl || !blobUrl.startsWith('blob:')) {
      return blobUrl; // blob이 아니면 그대로 반환 (기존 서버 URL 등)
    }

    try {
      // 1. blob URL을 fetch해서 바이너리 데이터(Blob)로 가져오기
      // 사용자가 화면에서 이미지를 선택하면, 브라우저는 그 이미지를 컴퓨터나 서버에 바로 올리는 게 아니라 내 컴퓨터 메모리에 임시로 담아둡니다. 그때 생기는 주소가 바로 blob:http://localhost:5173/... 같은 가상 주소(Blob URL)입니다.
      // 하지만 백엔드 서버(Spring 등)에 파일을 보내려면 가상 주소 페이크 문자열만 보내서는 안 되고, 진짜 파일의 알갱이(바이낸리 데이터)를 서버로 넘겨야 하죠.
      // 그래서 이 코드가 하는 일은 다음과 같습니다:
      // fetch(blobUrl)를 통해 브라우저 메모리에 있는 blob: 주소로 가짜 요청을 보냅니다. (서버로 가는 게 아니라 내 브라우저 안에서 일어나는 일입니다!)
      // 그럼 브라우저는 그 주소에 들어있던 진짜 이미지 데이터 알갱이(Raw Data)를 response에 담아서 줍니다.
      // 그 다음 줄인 await response.blob();을 통해 그 알갱이를 꺼내서 File 객체로 만들 수 있는 상태로 바꿔주는 것입니다.
      const response = await fetch(blobUrl);
      const blob = await response.blob();

      // 2. Blob 객체를 백엔드가 받을 수 있는 File 객체로 변환
      const file = new File([blob], 'background_image.jpg', {
        type: blob.type,
      });

      // 3. FormData 생성 및 파일 담기
      const formData = new FormData();
      formData.append('file', file); // ⚠️ 백엔드 @RequestParam("file") 또는 파라미터 이름과 일치해야 함!

      // 4. 파일 업로드 전용 API 호출
      const uploadResponse = await axios.post(
        '/api/customcard/uploadImage',
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        },
      );

      // 5. 서버가 응답으로 준 파일 접근 경로(URL) 반환
      // 예: "http://localhost:8080/images/abc-123.jpg" 혹은 "/images/abc-123.jpg"
      return uploadResponse.data;
    } catch (error) {
      console.error('배경 이미지 업로드 실패:', error);
      throw error; // 에러를 던져서 저장 프로세스를 멈춤
    }
  };

  const createCardPayload = async (userId) => {
    let finalImageUrl = history.value.image;

    let bgType = 'COLOR';
    let bgValue = history.value.color; // 기본값은 컬러

    if (finalImageUrl && finalImageUrl !== '') {
      bgType = 'IMAGE';
      bgValue = finalImageUrl; // 👈 이미지일 때는 이미지 URL (업로드된 URL 또는 기존 URL)
    } else if (history.value.gradient && history.value.gradient !== '') {
      bgType = 'GRADIENT';
      bgValue = history.value.gradient; // 👈 그라데이션일 때는 그라데이션 값
    }

    // 1. blob: 이미지인 경우 서버에 먼저 업로드하고 실제 서버 URL로 교체
    if (finalImageUrl && finalImageUrl.startsWith('blob:')) {
      console.log('새로 첨부한 이미지 발견! 서버에 업로드 중...');
      finalImageUrl = await uploadBackgroundImage(finalImageUrl); // 파일 업로드 함수 호출

      bgType = 'ATTACHMENT';
      bgValue = finalImageUrl;
    }

    // 2. 배경 타입 및 배경 값 판별 (정확한 변수 매칭)

    return {
      userId: userId,
      cardName: history.value.cardName,
      cardNumber: history.value.cardNumber,
      backgroundValue: bgValue,
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
    emojis.value = backup.value.emojis
      ? JSON.parse(JSON.stringify(backup.value.emojis))
      : [];
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

    createCardPayload,
  };
});
