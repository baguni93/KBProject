<template>
  <div
    ref="cardRef"
    class="card"
    :style="{ background: cardBackground }"
    :data-active-tab="cardStore.activeEditorTab"
    @click="deselectAll($event)"
  >
    <!-- 패턴 레이어 -->
    <div
      v-if="cardStore.pattern"
      class="pattern"
      :style="{ backgroundImage: `url(${cardStore.pattern})` }"
    />

    <!-- 💡 카드 전체를 커버하는 단일 드로잉 캔버스 (브러시 & 지우개 통합) -->
    <canvas
      ref="liveCanvasRef"
      class="live-drawing-canvas"
      :style="{
        pointerEvents:
          cardStore.activeEditorTab === 'drawing' ? 'auto' : 'none',
      }"
      @mousedown.stop="startLiveDraw"
      @mousemove.stop="liveDraw"
      @mouseup.stop="stopLiveDraw"
      @mouseleave.stop="stopLiveDraw"
      @touchstart.stop="startLiveDraw"
      @touchmove.stop="liveDraw"
      @touchend.stop="stopLiveDraw"
    ></canvas>

    <!-- 💡 텍스트 레이어 (드래그 및 회전 가능 영역) -->
    <div
      class="custom-text-wrapper"
      :style="{ zIndex: cardStore.activeEditorTab === 'text' ? 12 : 10 }"
    >
      <div
        v-for="item in cardStore.texts || []"
        :key="item.id"
        class="custom-text-item"
        :class="{
          dragging: draggingId === item.id,
          selected:
            cardStore.activeEditorTab === 'text' &&
            cardStore.selectedTextId === item.id,
          'out-of-bounds': isOutOfBounds && draggingId === item.id,
        }"
        :style="{
          fontFamily: item.font,
          color: item.color,
          fontSize: item.size,
          fontWeight: item.isBold ? 'bold' : 'normal',
          left: `${item.x ?? 50}%`,
          top: `${item.y ?? 50}%`,
          transform: `translate(-50%, -50%) rotate(${item.rotation ?? 0}deg)`,
        }"
        @mousedown.stop.prevent="startDrag($event, item)"
        @touchstart.stop.prevent="startDrag($event, item)"
      >
        <span>{{ item.text }}</span>
        <div
          v-if="
            cardStore.activeEditorTab === 'text' &&
            cardStore.selectedTextId === item.id
          "
          class="rotate-handle"
          title="회전하기"
          @mousedown.stop.prevent="startRotate($event, item)"
          @touchstart.stop.prevent="startRotate($event, item)"
        >
          <i class="fa-solid fa-arrow-rotate-right"></i>
        </div>
      </div>
    </div>

    <!-- 💡 이모지 레이어 -->
    <div
      class="custom-emoji-wrapper"
      :style="{ zIndex: cardStore.activeEditorTab === 'emoji' ? 12 : 10 }"
    >
      <div
        v-for="item in cardStore.emojis || []"
        :key="item.id"
        class="custom-emoji-item"
        :class="{
          'is-text-sticker': item.emojiObj?.emoji?.includes('text_'),
          dragging: draggingEmojiId === item.id,
          selected:
            cardStore.activeEditorTab === 'emoji' &&
            cardStore.selectedEmojiId === item.id,
          'out-of-bounds': isEmojiOutOfBounds && draggingEmojiId === item.id,
        }"
        :style="{
          left: `${item.x ?? 50}%`,
          top: `${item.y ?? 50}%`,
          transform: `translate(-50%, -50%) rotate(${item.rotation ?? 0}deg)`,
        }"
        @mousedown.stop.prevent="startEmojiDrag($event, item)"
        @touchstart.stop.prevent="startEmojiDrag($event, item)"
      >
        <img :src="item.emojiObj.emoji" alt="" draggable="false" />
        <div
          v-if="
            cardStore.activeEditorTab === 'emoji' &&
            cardStore.selectedEmojiId === item.id
          "
          class="emoji-delete-btn"
          @mousedown.stop.prevent="deleteEmoji(item.id)"
          @touchstart.stop.prevent="deleteEmoji(item.id)"
        >
          <i class="fa-solid fa-xmark"></i>
        </div>
      </div>
    </div>
    <div class="card-content">
      <div class="text-chip-container">
        <div class="bank-name">KB 국민카드</div>
        <div class="card-name">{{ cardStore.cardName }}</div>

        <!-- 💡 화살표가 칩 왼쪽에 오도록 순서 변경 -->
        <div v-if="cardStore.cardChip" class="chip-wrapper">
          <span class="chip-arrow" aria-hidden="true">&lt;</span>
          <img
            :src="cardStore.cardChip"
            class="card-chip-img"
            alt="card chip"
            draggable="false"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';

import { useModalStore } from '@/stores/userModalStore';
const modalStore = useModalStore();

import { useCardEditorStore } from '@/stores/cardEditorStore';
const cardStore = useCardEditorStore();

import html2canvas from 'html2canvas'; // 캡처 라이브러리 임포트
const cardRef = ref(null); // 캡처할 DOM 참조

// --- 단일 캔버스 그리기 로직 (브러시 & 지우개 자유 사용) ---
const liveCanvasRef = ref(null);
let liveCtx = null;
let isLiveDrawing = false;

onMounted(async () => {
  if (
    cardStore.backup &&
    (cardStore.gradient !== '' || cardStore.image !== '') &&
    cardStore.color !== '#1e40af'
  ) {
    console.log(cardStore.backup);

    const isConfirmed = await modalStore.showConfirm(
      '임시 저장된 카드가 있습니다. 카드를 불러오시겠습니까?, ',
      '카드 신청',
    );

    console.log(isConfirmed);
    if (!isConfirmed) return;

    cardStore.restoreSnapshot();
  }

  cardStore.restoreSnapshot();
  const canvas = liveCanvasRef.value;
  if (!canvas) return;
  canvas.width = canvas.offsetWidth || 205;
  canvas.height = canvas.offsetHeight || 128;
  liveCtx = canvas.getContext('2d');
  liveCtx.lineCap = 'round';
  liveCtx.lineJoin = 'round';

  // 스토어에 이미 저장된 그림이 있다면 캔버스에 불러오기
  if (cardStore.savedDrawingImage) {
    const img = new Image();
    img.src = cardStore.savedDrawingImage;
    img.onload = () => {
      liveCtx.drawImage(img, 0, 0);
    };
  }
});

const getCanvasCoordinates = (e) => {
  const rect = liveCanvasRef.value.getBoundingClientRect();
  const clientX = e.touches ? e.touches[0].clientX : e.clientX;
  const clientY = e.touches ? e.touches[0].clientY : e.clientY;
  return { x: clientX - rect.left, y: clientY - rect.top };
};

const startLiveDraw = (e) => {
  if (cardStore.activeEditorTab !== 'drawing') return;
  isLiveDrawing = true;
  const { x, y } = getCanvasCoordinates(e);
  liveCtx.beginPath();
  liveCtx.moveTo(x, y);
};

const liveDraw = (e) => {
  if (!isLiveDrawing || cardStore.activeEditorTab !== 'drawing') return;
  const { x, y } = getCanvasCoordinates(e);

  const mode = cardStore.drawingOptions?.mode || 'brush';
  const color = cardStore.drawingOptions?.color || '#00bcd4';
  const size = cardStore.drawingOptions?.size || 8;

  if (mode === 'eraser') {
    liveCtx.globalCompositeOperation = 'destination-out'; // 지우개 모드 (픽셀 투명하게 삭제)
    liveCtx.lineWidth = size * 2;
  } else {
    liveCtx.globalCompositeOperation = 'source-over'; // 일반 브러시 모드
    liveCtx.strokeStyle = color;
    liveCtx.lineWidth = size;
  }

  liveCtx.lineTo(x, y);
  liveCtx.stroke();
};

const stopLiveDraw = () => {
  if (!isLiveDrawing) return;
  isLiveDrawing = false;
  const canvas = liveCanvasRef.value;
  if (!canvas) return;

  // 그리기가 끝날 때마다 캔버스 전체 상태를 스토어에 저장 (원할 때 언제든 지우개로 다시 지울 수 있음)
  const dataUrl = canvas.toDataURL('image/png');
  cardStore.savedDrawingImage = dataUrl;
};

// '전체 지우기' 기능 대응을 위한 감시 또는 메서드 연동
watch(
  () => cardStore.isDrawingCleared,
  () => {
    if (liveCanvasRef.value && liveCtx) {
      liveCtx.clearRect(
        0,
        0,
        liveCanvasRef.value.width,
        liveCanvasRef.value.height,
      );
      cardStore.savedDrawingImage = null;
    }
  },
);

// --- 이모지 드래그 로직 ---
const draggingEmojiId = ref(null);
const isEmojiOutOfBounds = ref(false);

const deleteEmoji = (id) => {
  if (cardStore.removeEmoji) {
    cardStore.removeEmoji(id);
  }
};

const startEmojiDrag = (event, item) => {
  if (cardStore.selectEmoji) {
    cardStore.selectEmoji(item.id);
  }
  if (cardStore.activeEditorTab !== 'emoji') return;

  draggingEmojiId.value = item.id;
  isEmojiOutOfBounds.value = false;

  const emojiElement = event.currentTarget;
  const cardElement = emojiElement.closest('.card');
  if (!cardElement) return;

  const cardRect = cardElement.getBoundingClientRect();

  const onMove = (moveEvent) => {
    if (draggingEmojiId.value !== item.id) return;

    const clientX = moveEvent.touches
      ? moveEvent.touches[0].clientX
      : moveEvent.clientX;
    const clientY = moveEvent.touches
      ? moveEvent.touches[0].clientY
      : moveEvent.clientY;

    const emojiRect = emojiElement.getBoundingClientRect();
    const isOverLeft = emojiRect.left < cardRect.left;
    const isOverTop = emojiRect.top < cardRect.top;
    const isOverRight = emojiRect.right > cardRect.right;
    const isOverBottom = emojiRect.bottom > cardRect.bottom;

    isEmojiOutOfBounds.value =
      isOverLeft || isOverTop || isOverRight || isOverBottom;

    const percentX = ((clientX - cardRect.left) / cardRect.width) * 100;
    const percentY = ((clientY - cardRect.top) / cardRect.height) * 100;

    if (cardStore.updateEmojiPosition) {
      cardStore.updateEmojiPosition(item.id, percentX, percentY);
    }
  };

  const stopDrag = (e) => {
    if (e) {
      e.stopPropagation();
      e.preventDefault();
    }

    draggingEmojiId.value = null;
    if (isEmojiOutOfBounds.value) {
      cardStore.updateEmojiPosition?.(item.id, 50, 50);
      isEmojiOutOfBounds.value = false;
    }
    window.removeEventListener('mousemove', onMove);
    window.removeEventListener('mouseup', stopDrag);
    window.removeEventListener('touchmove', onMove);
    window.removeEventListener('touchend', stopDrag);
  };

  window.addEventListener('mousemove', onMove);
  window.addEventListener('mouseup', stopDrag);
  window.addEventListener('touchmove', onMove);
  window.addEventListener('touchend', stopDrag);
};

// --- 텍스트 드래그 및 회전 로직 ---
const draggingId = ref(null);
const isOutOfBounds = ref(false);

const cardBackground = computed(() => {
  if (cardStore.image)
    return `url(${cardStore.image}) center / cover no-repeat`;
  if (cardStore.gradient) return cardStore.gradient;
  return cardStore.color || '#1e40af';
});

const deselectAll = (event) => {
  // 클릭한 대상이 카드 배경(.card)이거나 패턴(.pattern)일 때만 해제 (텍스트나 이모지 내부 클릭 시 무시)
  if (
    event.target.classList.contains('card') ||
    event.target.classList.contains('pattern')
  ) {
    if (cardStore.selectText) {
      cardStore.selectText(null);
      cardStore.selectEmoji?.(null);
    }
  }
};

const startDrag = (event, item) => {
  if (cardStore.selectText) {
    cardStore.selectText(item.id);
  }

  if (cardStore.activeEditorTab !== 'text') return;

  draggingId.value = item.id;
  isOutOfBounds.value = false;

  const textElement = event.currentTarget;
  const cardElement = textElement.closest('.card');
  if (!cardElement) return;

  const cardRect = cardElement.getBoundingClientRect();

  const onMove = (moveEvent) => {
    if (draggingId.value !== item.id) return;

    const clientX = moveEvent.touches
      ? moveEvent.touches[0].clientX
      : moveEvent.clientX;
    const clientY = moveEvent.touches
      ? moveEvent.touches[0].clientY
      : moveEvent.clientY;

    const textRect = textElement.getBoundingClientRect();
    const isOverLeft = textRect.left < cardRect.left;
    const isOverTop = textRect.top < cardRect.top;
    const isOverRight = textRect.right > cardRect.right;
    const isOverBottom = textRect.bottom > cardRect.bottom;

    isOutOfBounds.value =
      isOverLeft || isOverTop || isOverRight || isOverBottom;

    let percentX = ((clientX - cardRect.left) / cardRect.width) * 100;
    let percentY = ((clientY - cardRect.top) / cardRect.height) * 100;

    if (cardStore.updateTextPosition) {
      cardStore.updateTextPosition(item.id, percentX, percentY);
    } else {
      item.x = percentX;
      item.y = percentY;
    }
  };

  const stopDrag = (e) => {
    if (e) {
      e.stopPropagation();
      e.preventDefault();
    }
    draggingId.value = null;
    if (isOutOfBounds.value) {
      if (cardStore.updateTextPosition) {
        cardStore.updateTextPosition(item.id, 50, 50);
      } else {
        item.x = 50;
        item.y = 50;
      }
      isOutOfBounds.value = false;
    }

    window.removeEventListener('mousemove', onMove);
    window.removeEventListener('mouseup', stopDrag);
    window.removeEventListener('touchmove', onMove);
    window.removeEventListener('touchend', stopDrag);
  };

  window.addEventListener('mousemove', onMove);
  window.addEventListener('mouseup', stopDrag);
  window.addEventListener('touchmove', onMove);
  window.addEventListener('touchend', stopDrag);
};

const startRotate = (event, item) => {
  const textElement = event.currentTarget.closest('.custom-text-item');
  const rect = textElement.getBoundingClientRect();

  const centerX = rect.left + rect.width / 2;
  const centerY = rect.top + rect.height / 2;

  const onRotateMove = (moveEvent) => {
    const clientX = moveEvent.touches
      ? moveEvent.touches[0].clientX
      : moveEvent.clientX;
    const clientY = moveEvent.touches
      ? moveEvent.touches[0].clientY
      : moveEvent.clientY;

    const radians = Math.atan2(clientY - centerY, clientX - centerX);
    let degrees = radians * (180 / Math.PI);
    degrees = (degrees + 90) % 360;

    if (cardStore.updateTextRotation) {
      cardStore.updateTextRotation(item.id, degrees);
    } else {
      item.rotation = Math.round(degrees);
    }
  };

  const stopRotate = () => {
    window.removeEventListener('mousemove', onRotateMove);
    window.removeEventListener('mouseup', stopRotate);
    window.removeEventListener('touchmove', onRotateMove);
    window.removeEventListener('touchend', stopRotate);

    if (cardStore.saveStep) {
      cardStore.saveStep();
    }
  };

  window.addEventListener('mousemove', onRotateMove);
  window.addEventListener('mouseup', stopRotate);
  window.addEventListener('touchmove', onRotateMove);
  window.addEventListener('touchend', stopRotate);
};

// 💡 [핵심] 카드를 캡처해서 백엔드로 전송하는 함수
const uploadCardImage = async (userId) => {
  const element = cardRef.value;
  if (!element) return;

  try {
    // 1. html2canvas로 HTML 요소를 캔버스로 변환
    const canvas = await html2canvas(element, {
      useCORS: true,
      scale: 3, // 고해상도로 선명하게 캡처
      backgroundColor: null,
    });

    // const targetWidth = 410;
    // const targetHeight = 256;

    // // 3. 사이즈가 조절된 새로운 가상 캔버스 생성
    // const resizeCanvas = document.createElement('canvas');
    // resizeCanvas.width = targetWidth;
    // resizeCanvas.height = targetHeight;
    // const ctx = resizeCanvas.getContext('2d');

    // 원본 캡처본을 지정한 크기로 압축/확대해서 그려넣기
    //ctx.drawImage(canvas, 0, 0, targetWidth, targetHeight);

    // 2. 캔버스를 Blob으로 변환하는 과정을 Promise로 감싸기
    const blob = await new Promise((resolve) => {
      canvas.toBlob((b) => {
        resolve(b); // 👈 Blob이 완성되면 Promise 완료
      }, 'image/png');
    });

    if (!blob) {
      console.error('이미지 변환 실패');
      return null;
    }

    // 3. FormData에 담기
    const formData = new FormData();
    formData.append('file', blob, 'my_custom_card.png');

    // 4. 완성된 FormData를 부모 쪽으로 정확하게 리턴!
    return formData;
  } catch (error) {
    console.error('카드 캡처 및 업로드 중 오류 발생:', error);
    throw error;
  }
};
// 카드 캡처 후 곧바로 다운로드 시켜주는 테스트 함수
const testDownloadCard = async () => {
  const element = cardRef.value;
  if (!element) {
    alert('카드를 찾을 수 없습니다.');
    return;
  }

  try {
    console.log('캡처 시작...');

    // html2canvas로 DOM을 캔버스로 변환
    const canvas = await html2canvas(element, {
      useCORS: true, // 외부 이미지(이모지, 배경 등) 깨짐 방지
      scale: 2, // 고화질 캡처
      backgroundColor: null,
    });

    // 캔버스를 이미지 URL(Base64)로 변환
    const imageURL = canvas.toDataURL('image/png');

    // 가상의 a 태그를 만들어 강제로 다운로드 유도
    const link = document.createElement('a');
    link.href = imageURL;
    link.download = 'card_test_image.png'; // 저장될 파일명
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    console.log('캡처 및 다운로드 완료!');
  } catch (error) {
    console.error('캡처 실패:', error);
    alert('이미지 캡처 중 에러가 발생했습니다.');
  }
};

defineExpose({
  testDownloadCard,
  uploadCardImage,
});
</script>

<style scoped>
/* 💡 단일 캔버스 스타일 (카드 전체 영역 덮기) */
.live-drawing-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 5;
  pointer-events: none; /* 💡 평소에는 마우스 이벤트를 통과시킴 */
  border-radius: 17px;
}

/* 💡 드로잉 탭일 때만 캔버스가 마우스 입력을 받도록 활성화 */
.card[data-tab='drawing'] .live-drawing-canvas {
  pointer-events: auto;
}

/* 💡 그리기 탭일 때는 텍스트와 이모지 위로 마우스가 통과해서 밑에 있는 캔버스에 그림이 그려지도록 설정 */
.card[data-active-tab='drawing'] .custom-text-item,
.card[data-active-tab='drawing'] .custom-emoji-item {
  pointer-events: none !important;
}

.card {
  width: 256px;
  height: 160px;
  border-radius: 10px;
  padding: 15px;
  box-sizing: border-box;
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  position: relative;
}

.pattern {
  position: absolute;
  inset: 0;
  background-repeat: repeat;
  background-position: center;
  background-size: 80px;
  opacity: 0.5;
  pointer-events: none;
  border-radius: 17px;
}

/* 래퍼 자체는 이벤트를 통과시키고 z-index는 템플릿의 동적 스타일에 맡김 */
.custom-emoji-wrapper,
.custom-text-wrapper {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
/* 🎀 기본 이모지 박스 크기 */
.custom-emoji-item {
  position: absolute;
  transform: translate(-50%, -50%);
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: move;
  user-select: none;
  -webkit-user-select: none;
  touch-action: none;
  pointer-events: auto;
  border: 1px dashed transparent;
  border-radius: 4px;
  transition:
    left 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.27),
    top 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.27),
    border-color 0.2s,
    background-color 0.2s;
}

/* 💡 일반 이모지 이미지 크기 */
.custom-emoji-item img {
  width: 36px !important;
  height: 36px !important;
  object-fit: contain;
  display: block;
  pointer-events: none;
}

/* 문구 스티커가 커졌으니 X(삭제) 버튼 위치도 우측 상단 모서리에 예쁘게 재배치 */
.custom-emoji-item.is-text-sticker .emoji-delete-btn {
  top: -8px;
  right: -8px;
}
.custom-text-item {
  position: absolute;
  transform: translate(-50%, -50%);
  white-space: nowrap;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
  line-height: 1.2;
  cursor: move;
  user-select: none;
  -webkit-user-select: none;
  touch-action: none;
  pointer-events: auto;
  border: 1px dashed transparent;
  border-radius: 4px;
  padding: 4px 6px;
  transition:
    left 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.27),
    top 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.27),
    border-color 0.2s,
    background-color 0.2s;
}

.custom-text-item {
  padding: 4px 6px;
}

.custom-text-item.dragging,
.custom-emoji-item.dragging {
  cursor: grabbing;
  border-color: #ffc107;
  transition:
    border-color 0.2s,
    background-color 0.2s;
}

.custom-text-item.out-of-bounds,
.custom-emoji-item.out-of-bounds {
  border: 1.5px solid #ef4444 !important;
  background-color: rgba(239, 68, 68, 0.25) !important;
  color: #ef4444 !important;
  box-shadow: 0 0 8px rgba(239, 68, 68, 0.5);
}

.custom-text-item.selected,
.custom-emoji-item.selected {
  border: 1px dashed #3b82f6;
  background-color: rgba(59, 130, 246, 0.1);
}

/* 이모지 X(삭제) 버튼 위치 재조정 (박스가 작아졌으므로 우측 상단에 딱 맞게) */
.emoji-delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #ef4444;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: bold;
  cursor: pointer;
  z-index: 20;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.25);
}

.rotate-handle {
  position: absolute;
  top: -26px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 24px;
  background-color: transparent;
  border: none;
  box-shadow: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #ffffff;
  filter: drop-shadow(0px 1px 3px rgba(0, 0, 0, 0.7));
  cursor: grab;
  z-index: 12;
  transition: transform 0.15s ease;
}

.rotate-handle:hover {
  transform: translateX(-50%) scale(1.15);
}

.rotate-handle:active {
  cursor: grabbing;
  color: #ffc107;
}

.card-content {
  position: relative;
  z-index: 15;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  pointer-events: none;
}

.card-name {
  font-size: 10px;
  font-weight: 700;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  font-size: 8px;
}

/* 💡 텍스트와 칩을 묶는 컨테이너 스타일 (신규) */
.card-content {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px; /* 양쪽 여백 균일하게 */
  bottom: 20px;
  z-index: 15;
  pointer-events: none;
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 위쪽(이름)과 아래쪽(칩) 분리 */
}

/* 텍스트와 칩을 묶어주는 컨테이너 */
.text-chip-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px; /* 💡 카드 이름과 IC칩 사이의 간격 */
}

/* 은행명 스타일 (필요시 추가) */
.bank-name {
  font-size: 11px;
  font-weight: 500;
  margin-bottom: -12px; /* 은행명과 카드 이름 사이를 더 가깝게 붙임 */
  margin-top: -10px;
  margin-left: -5px;
}

/* 카드 이름 텍스트 */
.card-name {
  opacity: 0.9;
  font-size: 10px;
  font-weight: 700;
  margin: 0;
  margin-left: -5px;
}

/* IC칩 이미지 위치 및 크기 */
.card-chip-img {
  width: 40px; /* 실제 카드 칩 표준 크기 */
  height: auto;
  object-fit: contain;
  pointer-events: none;
  margin-top: 4px; /* 이름과의 미세 간격 조정 */
  opacity: 0.8;
}

/* 💡 칩과 화살표를 감싸는 래퍼 */
.chip-wrapper {
  display: flex;
  align-items: center; /* 세로 정렬 중앙 */
  gap: 3px; /* 화살표와 칩 사이의 간격 */

  /* 🛠️ 전체 위치 미세 조정 (가로, 세로) */
  transform: translate(-5px, 0px);
}

/* 화살표 기호 스타일 */
.chip-arrow {
  font-size: 11px;
  font-weight: 900;
  color: white;
  opacity: 0.85;
  transform: scaleY(1.4);
  pointer-events: none;

  /* 🛠️ 화살표만 따로 위치를 칩에 더 붙이거나 띄우고 싶을 때 사용 */
  /* margin-right: -2px; */
}
</style>
