<template>
  <div class="panel">
    <!-- 1. 패턴 탭 -->
    <template v-if="tab === 'pattern'">
      <div class="special-picker">
        <CardPatternCanvas
          v-for="pattern in patterns"
          :key="pattern"
          :pattern="pattern"
          @click="selectPattern(pattern)"
        />
      </div>
    </template>

    <!-- 2. 텍스트 탭 -->
    <template v-else-if="tab === 'text'">
      <div class="text-editor-container">
        <!-- 텍스트 입력창 & 추가 버튼 -->
        <div class="input-row">
          <input
            v-model="inputText"
            type="text"
            placeholder="카드에 넣을 문구 입력..."
            class="text-input"
            maxlength="8"
            @keyup.enter="handleAddText"
          />

          <button
            class="add-btn"
            @click="handleAddText"
            :disabled="cardStore.texts?.length >= 3"
          >
            +
          </button>
        </div>

        <!-- 폰트 선택 -->
        <div class="section">
          <label class="section-title">폰트</label>
          <div class="font-grid">
            <button
              v-for="font in fonts"
              :key="font.key"
              class="font-btn"
              :class="{ active: selectedFont === font.key }"
              @click="selectedFont = font.key"
            >
              <span class="font-name" :style="{ fontFamily: font.key }">
                {{ font.name }}
              </span>
              <span class="font-sub">{{ font.sub }}</span>
            </button>
          </div>
        </div>

        <!-- 텍스트 색상 선택 -->
        <div class="section">
          <label class="section-title">텍스트 색상</label>
          <div class="color-row">
            <button
              v-for="color in textColors"
              :key="color"
              class="color-btn"
              :class="{ active: selectedColor === color }"
              :style="{ backgroundColor: color }"
              @click="selectedColor = color"
            />
          </div>
        </div>

        <!-- 크기 및 굵기 조절 -->
        <div class="format-row">
          <button
            v-for="size in sizes"
            :key="size.value"
            class="format-btn"
            :class="{ active: selectedSize === size.value }"
            @click="selectedSize = size.value"
          >
            ≡ {{ size.label }}
          </button>
          <button
            class="format-btn"
            :class="{ active: isBold }"
            @click="isBold = !isBold"
          >
            B 굵게
          </button>
        </div>

        <!-- 추가된 텍스트 목록 -->
        <div class="section" v-if="cardStore.texts.length > 0">
          <label class="section-title">추가된 텍스트</label>
          <div class="added-text-list">
            <div
              v-for="item in cardStore.texts"
              :key="item.id"
              class="added-text-item"
            >
              <span
                class="text-preview"
                :style="{
                  fontFamily: item.font,
                  color: item.color,
                  fontWeight: item.isBold ? 'bold' : 'normal',
                }"
              >
                {{ item.text }}
              </span>
              <button class="delete-btn" @click="handleRemoveText(item.id)">
                ✕
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 3. 이모지 탭 -->
    <template v-else-if="tab === 'emoji'">
      <div class="emoji-picker">
        <CardEmojiPick
          @update:model-value="(path) => cardStore.addEmoji({ emoji: path })"
        />
      </div>
    </template>

    <!-- 4. 그리기 탭 (설정 UI 컨트롤러) -->
    <template v-else-if="tab === 'drawing'">
      <div class="drawing-editor-container">
        <!-- 안내 메시지 박스 -->
        <div class="drawing-guide-box">
          <span class="guide-icon">📍</span>
          <span class="guide-text"
            >위 카드 영역을 터치하거나 클릭해서 자유롭게 그릴 수 있어요!</span
          >
        </div>

        <!-- 모드 선택 버튼 (브러시 / 지우개 / 전체 지우기) -->
        <div class="drawing-mode-row">
          <button
            class="mode-btn"
            :class="{ active: drawingMode === 'brush' }"
            @click="drawingMode = 'brush'"
          >
            ✏️ 브러시
          </button>
          <button
            class="mode-btn"
            :class="{ active: drawingMode === 'eraser' }"
            @click="drawingMode = 'eraser'"
          >
            🧹 지우개
          </button>
          <button class="mode-btn clear-all-btn" @click="handleClearDrawing">
            전체 지우기
          </button>
        </div>

        <!-- 브러시 색상 선택 -->
        <div class="section" v-if="drawingMode === 'brush'">
          <label class="section-title">브러시 색상</label>
          <div class="color-row">
            <button
              v-for="color in drawingColors"
              :key="color"
              class="color-btn"
              :class="{ active: selectedDrawingColor === color }"
              :style="{ backgroundColor: color }"
              @click="selectedDrawingColor = color"
            />
          </div>
        </div>

        <!-- 브러시 굵기 조절 -->
        <div class="section">
          <label class="section-title">브러시 굵기</label>
          <div class="slider-row">
            <input
              type="range"
              min="1"
              max="30"
              v-model.number="brushSize"
              class="brush-slider"
            />
            <span
              class="slider-indicator"
              :style="{
                width: Math.min(Math.max(brushSize, 6), 24) + 'px',
                height: Math.min(Math.max(brushSize, 6), 24) + 'px',
              }"
            ></span>
          </div>
        </div>
        <div class="drawing-warning-box">
          <i class="fa-solid fa-triangle-exclamation"></i>
          <span
            >욕설 및 부적절한 내용이 포함된 그리기 결과물은 실물 카드 발급 및
            승인 과정에서 제외될 수 있습니다.</span
          >
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import CardPatternCanvas from './CardPatternCanvas.vue';
import CardEmojiPick from './CardEmojiPick.vue';
import { useCardEditorStore } from '@/stores/cardEditorStore';

import { useModalStore } from '@/stores/userModalStore';
const modalStore = useModalStore();

const cardStore = useCardEditorStore();

const props = defineProps({
  tab: {
    type: String,
    default: 'basic',
  },
});

watch(
  () => props.tab,
  (tab) => {
    if (!tab) return;
    cardStore.activeEditorTab = tab;
    cardStore.selectedEmojiId = null;
    cardStore.selectedTextId = null;
  },
  {
    immediate: true,
  },
);

/* --- 그리기 에디터 상태 (스토어 drawingOptions 초기값과 동기화) --- */
const drawingMode = ref(cardStore.drawingOptions.mode);
const selectedDrawingColor = ref(cardStore.drawingOptions.color);
const brushSize = ref(cardStore.drawingOptions.size);

const drawingColors = [
  '#ffffff',
  '#ffb000',
  '#ff5a8d',
  '#6c7ae0',
  '#00bcd4',
  '#e53935',
  '#000000',
];

const handleClearDrawing = () => {
  if (typeof cardStore.clearDrawing === 'function') {
    cardStore.clearDrawing();
  }
};

watch([drawingMode, selectedDrawingColor, brushSize], ([mode, color, size]) => {
  if (typeof cardStore.setDrawingOptions === 'function') {
    cardStore.setDrawingOptions({ mode, color, size });
  }
});

/* --- 패턴 관련 설정 --- */
const patterns = [
  '/images/card_edit_pattern/pattern_arrow.svg',
  '/images/card_edit_pattern/pattern_circle.svg',
  '/images/card_edit_pattern/pattern_dot.svg',
  '/images/card_edit_pattern/pattern_grid.svg',
  '/images/card_edit_pattern/pattern_line.svg',
  '/images/card_edit_pattern/pattern_wave.svg',
];

const selectPattern = (pattern) => {
  cardStore.setPattern(pattern);
};

/* --- 텍스트 에디터 상태 --- */
const inputText = ref('');
const selectedFont = ref('sans-serif');
const selectedColor = ref('#ffffff');
const selectedSize = ref('20px');
const isBold = ref(false);

const fonts = [
  { name: '기본체', sub: '기본', key: 'sans-serif' },
  { name: '검은고딕', sub: '임팩트', key: '"Black Han Sans", sans-serif' },
  { name: '명조체', sub: '명조', key: 'serif' },
  { name: '손글씨', sub: '손글씨', key: 'cursive' },
  { name: 'Sans', sub: 'Sans', key: 'system-ui' },
];

const textColors = [
  '#ffffff',
  '#ffb000',
  '#ff5a8d',
  '#6c7ae0',
  '#2b3040',
  '#000000',
  '#e53935',
];

const sizes = [
  { label: '소', value: '14px' },
  { label: '중', value: '17px' },
  { label: '대', value: '22px' },
];

import { useWordFilterStore } from '@/stores/wordFilterStore';
const wordFilterStore = useWordFilterStore();

const handleAddText = async () => {
  const trimmedText = inputText.value.trim();
  if (!trimmedText) return;

  // 💡 공통 필터 스토어의 검증 함수 사용
  const validation = wordFilterStore.validateText(trimmedText);

  if (!validation.isValid) {
    await modalStore.showAlert(validation.message);
    return; // 추가 중단
  }

  const newTextItem = {
    id: Date.now(),
    text: validation.text,
    font: selectedFont.value,
    color: selectedColor.value,
    size: selectedSize.value,
    isBold: isBold.value,
  };

  cardStore.addText(newTextItem);

  inputText.value = '';
};

const handleRemoveText = (id) => {
  cardStore.removeText(id);
};
</script>

<style scoped>
/* ==================== 패턴 픽커 스타일 ==================== */
.special-picker {
  width: 100%;
  max-width: 330px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
  margin: 0 auto;
  padding: 12px 5px;
  box-sizing: border-box;
}

.special-picker :deep(.card) {
  height: 70px;
}

/* ==================== 텍스트 에디터 스타일 ==================== */
.text-editor-container {
  width: 100%;
  max-width: 340px;
  margin: 0 auto;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
}

.input-row {
  display: flex;
  gap: 8px;
}

.text-input {
  flex: 1;
  height: 44px;
  background-color: #f4f5f7;
  border: 1px solid transparent;
  border-radius: 22px;
  padding: 0 16px;
  font-size: 13px;
  outline: none;
  transition: all 0.2s ease;
}

.text-input:focus {
  border-color: #ffc107;
  background-color: #ffffff;
}

.add-btn {
  width: 44px;
  height: 44px;
  border-radius: 22px;
  background-color: #ffc107;
  border: none;
  color: #333;
  font-size: 22px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.1s ease;
}

.add-btn:active {
  transform: scale(0.92);
}

.section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-title {
  font-size: 11px;
  color: #888888;
  font-weight: 600;
  text-align: left;
}

.font-grid {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.font-btn {
  flex: 1;
  min-width: 58px;
  height: 48px;
  border-radius: 14px;
  border: 1.5px solid #eaeaea;
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 1px 4px;
}

.font-name {
  font-size: 12px;
  font-weight: bold;
  color: #222;
}

.font-sub {
  font-size: 9px;
  color: #999;
  margin-top: 2px;
}

.font-btn.active {
  border-color: #ffc107;
  background-color: #fffdf0;
  box-shadow: 0 0 0 1px #ffc107;
}

.color-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.color-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.color-btn.active {
  transform: scale(1.1);
  box-shadow:
    0 0 0 2px white,
    0 0 0 4px #ffc107;
}

.format-row {
  display: flex;
  gap: 8px;
}

.format-btn {
  flex: 1;
  height: 38px;
  border-radius: 19px;
  border: 1.5px solid #eaeaea;
  background: white;
  font-size: 12px;
  color: #444;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.format-btn.active {
  border-color: #ffc107;
  background-color: #fffdf0;
  color: #111;
}

.added-text-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.added-text-item {
  height: 40px;
  background-color: #c4c9ce;
  border-radius: 14px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.text-preview {
  font-size: 13px;
  color: #333;
}

.delete-btn {
  background: none;
  border: none;
  color: #222121;
  font-size: 14px;
  cursor: pointer;
  padding: 4px;
}

.delete-btn:hover {
  color: #ff5a8d;
}

.add-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  pointer-events: none;
}

/* ==================== 그리기 에디터 설정 UI 스타일 ==================== */
.drawing-editor-container {
  width: 100%;
  max-width: 340px;
  margin: 0 auto;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
}

.drawing-guide-box {
  background-color: #fffde7;
  border-radius: 12px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-align: left;
}

.guide-icon {
  font-size: 14px;
}

.guide-text {
  font-size: 12px;
  color: #795548;
  line-height: 1.4;
  font-weight: 500;
}

.drawing-mode-row {
  display: flex;
  gap: 8px;
}

.mode-btn {
  flex: 1;
  height: 40px;
  border-radius: 20px;
  border: 1.5px solid #eaeaea;
  background: white;
  font-size: 13px;
  color: #444;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.mode-btn.active {
  border-color: #ffc107;
  background-color: #fffdf0;
  color: #111;
}

.clear-all-btn {
  color: #666;
  font-weight: 500;
}

.clear-all-btn:hover {
  background-color: #f5f5f5;
}

.slider-row {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #f9f9f9;
  padding: 10px 16px;
  border-radius: 20px;
  border: 1.5px solid #eaeaea;
}

.brush-slider {
  flex: 1;
  accent-color: #ffc107;
  height: 6px;
  cursor: pointer;
}

.slider-indicator {
  background-color: #333;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
  transition:
    width 0.1s,
    height 0.1s;
}
.drawing-warning-box {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 330px;
  margin-top: 16px;
  padding: 10px 12px;
  background: #fff8f8;
  border: 1px solid #ffdede;
  border-radius: 10px;
  box-sizing: border-box;
  color: #d9534f;
  font-size: 11px;
  line-height: 1.4;
  text-align: left;
}

.drawing-warning-box i {
  font-size: 12px;
  margin-top: 1px;
  flex-shrink: 0;
}
</style>
