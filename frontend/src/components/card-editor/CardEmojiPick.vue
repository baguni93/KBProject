<template>
  <div class="emoji-picker-container">
    <!-- 1. 상단 대분류 탭 (이모지 / 스티커) -->
    <div class="main-tab-row">
      <button
        class="main-tab-btn"
        :class="{ active: activeMainTab === 'emoji' }"
        @click="activeMainTab = 'emoji'"
      >
        😊 이모지
      </button>
      <button
        class="main-tab-btn"
        :class="{ active: activeMainTab === 'sticker' }"
        @click="activeMainTab = 'sticker'"
      >
        🎀 스티커
      </button>
    </div>

    <!-- 2. 하위 카테고리 탭 (표정 / 기념 / 자연 / 기타) -->
    <div class="sub-category-row">
      <button
        v-for="cat in currentCategories"
        :key="cat.key"
        class="sub-category-btn"
        :class="{ active: activeSubCategory === cat.key }"
        @click="activeSubCategory = cat.key"
      >
        {{ cat.name }}
      </button>
    </div>

    <!-- 3. 이모지/스티커 아이템 그리드 리스트 -->
    <div class="emoji-list">
      <button
        v-for="emoji in filteredEmojiList"
        :key="emoji"
        class="emoji-item"
        :class="{ active: modelValue === emoji }"
        @click="selectEmoji(emoji)"
      >
        <img :src="emoji" alt="emoji icon" class="emoji-img" />
        <span v-if="modelValue === emoji"></span>
      </button>
    </div>

    <!-- 4. 하단 모두 제거 버튼 -->
    <div class="remove-action-row" v-if="cardStore?.emojis?.length > 0">
      <button class="remove-all-btn" @click="handleRemoveAllEmojis">
        모두 제거 ({{ cardStore.emojis.length }})
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import config from '@/config/card';
import { useCardEditorStore } from '@/stores/cardEditorStore';

const cardStore = useCardEditorStore();

defineProps({
  modelValue: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['update:modelValue']);

/* --- 탭 상태 관리 --- */
const activeMainTab = ref('emoji'); // 'emoji' 또는 'sticker'
const activeSubCategory = ref('expression'); // 기본 첫 번째 카테고리

// 대분류에 따른 하위 카테고리 목록 정의
// (config 구조에 맞게 수정하여 사용하세요)
const categoriesMap = {
  emoji: [
    { name: '표정', key: 'expression' },
    { name: '기념', key: 'anniversary' },
    { name: '자연', key: 'nature' },
    { name: '기타', key: 'etc' },
  ],
  sticker: [
    { name: '아이콘', key: 'icon' },
    { name: '문구', key: 'text' },
  ],
};

// 현재 대분류에 해당하는 카테고리들
const currentCategories = computed(() => {
  return categoriesMap[activeMainTab.value] || [];
});

// 대분류나 카테고리가 바뀔 때 하위 탭 초기화 설정
import { watch } from 'vue';
watch(activeMainTab, (newTab) => {
  if (categoriesMap[newTab]?.length > 0) {
    activeSubCategory.value = categoriesMap[newTab][0].key;
  }
});

/* --- 데이터 필터링 --- */
// config 구조가 category별로 되어있거나 단일 배열일 수 있으므로 예시로 구성합니다.
// 만약 config.emojis가 객체 형태({ expression: [...], nature: [...] })라면 아래처럼 사용 가능합니다.
const filteredEmojiList = computed(() => {
  if (activeMainTab.value === 'emoji') {
    // config.emojis[activeSubCategory.value] 구조인 경우
    return config.emojis?.[activeSubCategory.value] || config.emojis || [];
  } else {
    return config.stickers?.[activeSubCategory.value] || [];
  }
});

const selectEmoji = (emojiPath) => {
  emit('update:modelValue', emojiPath);
};

// 모두 제거 핸들러
const handleRemoveAllEmojis = () => {
  cardStore.celarEmojis();
};
</script>

<style scoped>
.emoji-picker-container {
  width: 100%;
  max-width: 340px;
  margin: 0 auto;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-sizing: border-box;
}

/* 상단 대분류 탭 (이모지 / 스티커) 스타일 */
.main-tab-row {
  display: flex;
  background-color: #f4f5f7;
  padding: 4px;
  border-radius: 20px;
  gap: 4px;
}

.main-tab-btn {
  flex: 1;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  color: #777;
  cursor: pointer;
  transition: all 0.2s ease;
}

.main-tab-btn.active {
  background: #ffffff;
  color: #111;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

/* 하위 카테고리 탭 (표정 / 기념 / 자연 / 기타) 스타일 */
.sub-category-row {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.sub-category-btn {
  padding: 6px 14px;
  height: 32px;
  border-radius: 16px;
  border: 1.5px solid #eaeaea;
  background: white;
  font-size: 12px;
  color: #666;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.sub-category-btn.active {
  border-color: #ffc107;
  background-color: #ffc107;
  color: #ffffff;
  font-weight: 600;
}

/* 이모지 그리드 목록 */
.emoji-list {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  padding: 4px 0;
}

.emoji-item {
  position: relative;
  aspect-ratio: 1;
  width: 100%;
  border-radius: 14px;
  border: 1px solid #eaeaea;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

.emoji-img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.emoji-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #ffc107;
}

.emoji-item.active {
  transform: scale(1.05);
  border-color: #ffc107;
  background-color: #fffdf0;
  box-shadow:
    0 0 0 2px white,
    0 0 0 4px #ffc107,
    0 6px 16px rgba(0, 0, 0, 0.08);
}

/* 선택 표시 하얀 점 */
.emoji-item span {
  position: absolute;
  width: 8px;
  height: 8px;
  right: 5px;
  top: 5px;
  border-radius: 50%;
  background: rgb(255, 255, 255);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

/* 하단 모두 제거 버튼 영역 */
.remove-action-row {
  margin-top: 4px;
}

.remove-all-btn {
  width: 100%;
  height: 42px;
  border-radius: 14px;
  border: 1.5px solid #eaeaea;
  background-color: #ffffff;
  color: #666;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.remove-all-btn:hover {
  background-color: #f9f9f9;
  color: #ff5a8d;
  border-color: #ff5a8d;
}

/* 🚀 스티커 탭의 문구(text) 카테고리일 때 아이템을 가로 3칸(전체 폭)으로 확장 */
.emoji-item:has(img[src*='text_']) {
  grid-column: span 3; /* 6열 중 3칸을 차지하여 큼직하게 표시 */
  aspect-ratio: auto; /* 정사각형 비율 해제 */
  height: 48px; /* 문구 비율에 맞는 적당한 높이 지정 */
}

/* 🚀 문구 이미지 자체도 박스 크기에 맞춰 큼직하게 꽉 채우기 */
.emoji-item:has(img[src*='text_']) .emoji-img {
  width: 90% !important;
  height: 80% !important;
  max-width: none !important;
  max-height: none !important;
}
</style>
