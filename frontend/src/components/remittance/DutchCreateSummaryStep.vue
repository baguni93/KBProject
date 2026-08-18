<template>
  <div class="kb-dutch-summary-wrapper">
    <!-- 1. 정산 제목 입력 & 총 금액 -->
    <div class="kb-summary-header-card">
      <div class="title-edit-wrap">
        <input
          :value="dutchRoomTitle"
          type="text"
          class="kb-title-input text-15-bold"
          placeholder="정산 제목 입력"
          @input="$emit('update:dutchRoomTitle', $event.target.value)"
        />
        <i class="fa-solid fa-pen edit-pencil-ic"></i>
      </div>
      <div class="kb-total-amount text-30-bold">
        총 {{ formatCurrency(remitAmount || 0) }}원
      </div>
    </div>

    <!-- 2. 정산 항목 모던 화이트 카드 -->
    <div class="kb-item-card-box">
      <div class="card-box-left">
        <div class="icon-utensils-box">
          <i class="fa-solid fa-utensils"></i>
        </div>
        <div class="card-info-col">
          <div class="item-title-text text-16-bold">{{ dutchRoomTitle || '더치페이 정산' }}</div>
          <div class="item-amount-text text-20-bold">{{ formatCurrency(remitAmount || 0) }}원</div>
          <div class="item-members-text text-13">
            {{ myProfileName || '노랑지갑' }} 외 {{ selectedDutchFriends ? selectedDutchFriends.length : 0 }}명
          </div>
        </div>
      </div>
    </div>

    <!-- 3. Venmo 스타일 소셜 피드 작성 통합 박스 (송금 3단계 RemitStep3FeedForm 100% 동일 규격) -->
    <div class="venmo-composer-card">
      <!-- A. 피드 메시지 입력 영역 -->
      <div class="venmo-note-area">
        <textarea
          :value="remitMemo"
          @input="$emit('update:remitMemo', $event.target.value)"
          class="venmo-note-textarea text-16"
          placeholder="피드에 어떤 추억을 남길까요? 🍕"
          rows="3"
        ></textarea>
      </div>

      <!-- B. 소비 카테고리 퀵 선택 칩 목록 -->
      <div class="venmo-category-section">
        <div class="venmo-category-header">
          <span class="text-13-bold venmo-sec-lbl">
            <i class="fa-solid fa-tag" style="color: #ffbc2e;"></i> 소비 카테고리
          </span>
          <button
            type="button"
            class="venmo-toggle-btn text-13-bold"
            @click="isCategoryExpanded = !isCategoryExpanded"
          >
            {{ isCategoryExpanded ? "접기 ▲" : "더보기 ▼" }}
          </button>
        </div>

        <!-- 기본 1줄 슬림 4개 카테고리 칩 -->
        <div v-if="!isCategoryExpanded" class="dutch-cat-chip-row">
          <button
            v-for="cat in slimCategories"
            :key="cat.id"
            type="button"
            class="dutch-cat-chip"
            :class="{ active: (selectedCategoryId || 1) === cat.id }"
            @click="selectSlimCategory(cat.id)"
          >
            <i :class="getCategoryIcon(cat.name)" class="cat-fa-ic"></i>
            <span class="cat-name text-13-bold">{{ cat.name }}</span>
          </button>
        </div>

        <!-- 더보기 선택 시: 팀 공용 SpendingCategorySelector -->
        <SpendingCategorySelector
          v-else
          :model-value="selectedCategoryId || 1"
          @update:model-value="onSelectExpandedCategory"
          :categories="categoryList"
          compact
        />
      </div>

      <div class="venmo-divider"></div>

      <!-- C. 하단 툴바: 공개 범위 세그먼트 스위치 -->
      <div class="venmo-toolbar">
        <!-- 공개 범위 선택 (Segmented Switch) -->
        <div class="venmo-visibility-bar">
          <button
            type="button"
            class="venmo-vis-opt text-12-bold"
            :class="{ active: remitVisibility === 'PUBLIC' }"
            @click="$emit('update:remitVisibility', 'PUBLIC')"
          >
            <i class="fa-solid fa-earth-americas"></i> 전체
          </button>
          <button
            type="button"
            class="venmo-vis-opt text-12-bold"
            :class="{ active: remitVisibility === 'FRIEND' }"
            @click="$emit('update:remitVisibility', 'FRIEND')"
          >
            <i class="fa-solid fa-user-group"></i> 친구
          </button>
          <button
            type="button"
            class="venmo-vis-opt text-12-bold"
            :class="{ active: remitVisibility === 'PRIVATE' }"
            @click="$emit('update:remitVisibility', 'PRIVATE')"
          >
            <i class="fa-solid fa-lock"></i> 나만
          </button>
        </div>
      </div>
    </div>

    <!-- 4. 하단 공통 도킹 버튼 ([정산 요청]) -->
    <div class="bottom-btn-area single">
      <button
        type="button"
        class="bottom-btn primary-button text-17-bold"
        @click="$emit('submit')"
      >
        정산 요청
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import SpendingCategorySelector from "@/components/common/SpendingCategorySelector.vue";
import { getCategoryIcon } from "@/util/analysis";

const props = defineProps({
  dutchRoomTitle: {
    type: String,
    default: "",
  },
  remitAmount: {
    type: Number,
    default: 0,
  },
  selectedDutchFriends: {
    type: Array,
    default: () => [],
  },
  myProfileName: {
    type: String,
    default: "노랑지갑",
  },
  getFriendName: {
    type: Function,
    default: () => "참여자",
  },
  remitMemo: {
    type: String,
    default: "",
  },
  selectedCategoryId: {
    type: Number,
    default: 1,
  },
  remitVisibility: {
    type: String,
    default: "PUBLIC",
  },
  categoryList: {
    type: Array,
    default: () => [],
  },
  selectedFiles: {
    type: Array,
    default: () => [],
  },
  imagePreviewUrl: {
    type: String,
    default: null,
  },
  imagePreviewUrls: {
    type: Array,
    default: () => [],
  },
  formatCurrency: {
    type: Function,
    required: true,
  },
});

const emit = defineEmits([
  "update:dutchRoomTitle",
  "update:remitMemo",
  "update:selectedCategoryId",
  "update:remitVisibility",
  "fileChange",
  "removeFile",
  "submit",
]);

const isCategoryExpanded = ref(false);

const slimCategories = [
  { id: 1, name: "식비" },
  { id: 2, name: "카페" },
  { id: 3, name: "쇼핑" },
  { id: 4, name: "교통" },
];

const effectivePreviewUrls = computed(() => {
  if (props.imagePreviewUrls && props.imagePreviewUrls.length > 0) {
    return props.imagePreviewUrls;
  }
  if (props.imagePreviewUrl) {
    return [props.imagePreviewUrl];
  }
  return [];
});

const selectSlimCategory = (catId) => {
  emit("update:selectedCategoryId", catId);
};

const onSelectExpandedCategory = (catId) => {
  emit("update:selectedCategoryId", catId);
  isCategoryExpanded.value = false;
};
</script>

<style scoped>
@import "@/components/common/common/common.css";

.kb-dutch-summary-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  position: relative;
  min-height: 100%;
  padding-bottom: 90px;
  box-sizing: border-box;
}

.bottom-btn-area.single {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 0 24px;
  background: #ffffff;
  z-index: 100;
  box-sizing: border-box;
}

.bottom-btn-area.single .bottom-btn {
  width: 100%;
  height: 52px;
  border-radius: 16px;
  font-size: 17px;
  font-weight: 700;
  border: none;
  background-color: var(--color-primary, #ffbc2e);
  color: #111111;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.18s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px rgba(255, 188, 46, 0.35);
}

.bottom-btn-area.single .bottom-btn:not(:disabled):active {
  transform: scale(0.98);
  opacity: 0.92;
}

.kb-summary-header-card {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 20px;
  border: 1px solid #edf2f7;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.title-edit-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 8px;
}

.kb-title-input {
  border: none;
  background: transparent;
  text-align: center;
  color: #718096;
  width: 140px;
  outline: none;
}

.edit-pencil-ic {
  color: #a0aec0;
  font-size: 13px;
}

.kb-total-amount {
  color: #111111;
}

.kb-item-card-box {
  background-color: #ffffff;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid #edf2f7;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.card-box-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.icon-utensils-box {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background-color: #fef3c7;
  color: #b45309;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.card-info-col {
  display: flex;
  flex-direction: column;
  gap: 2px;
  text-align: left;
}

.item-title-text { color: #111111; }
.item-amount-text { color: #111111; }
.item-members-text { color: #718096; }

/* Venmo 스타일 피드 작성 통합 박스 (RemitStep3FeedForm 100% 동일 CSS) */
.venmo-composer-card {
  background-color: #ffffff;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.venmo-note-textarea {
  width: 100%;
  border: none;
  resize: none;
  outline: none;
  font-family: inherit;
  color: #111111;
  background: transparent;
  padding: 0;
  box-sizing: border-box;
}

.venmo-note-textarea::placeholder {
  color: #a0aec0;
}

.venmo-category-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.venmo-category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.venmo-sec-lbl {
  color: #4a5568;
}

.venmo-toggle-btn {
  background: none;
  border: none;
  color: #718096;
  cursor: pointer;
  padding: 0;
}

.dutch-cat-chip-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 6px;
}

.dutch-cat-chip {
  height: 36px;
  border-radius: 10px;
  border: 1px solid #edf2f7;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.2s ease;
}

.dutch-cat-chip.active {
  background-color: #ffbc2e;
  border-color: #ffbc2e;
  color: #111111;
}

.cat-fa-ic {
  font-size: 13px;
}

.venmo-divider {
  height: 1px;
  background-color: #edf2f7;
}

.venmo-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.venmo-visibility-bar {
  display: flex;
  background-color: #f7fafc;
  border: 1px solid #edf2f7;
  border-radius: 10px;
  padding: 2px;
  gap: 2px;
}

.venmo-vis-opt {
  border: none;
  background: transparent;
  padding: 6px 10px;
  border-radius: 8px;
  color: #718096;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.venmo-vis-opt.active {
  background-color: #ffffff;
  color: #111111;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.hidden-file-input {
  display: none;
}

.venmo-photo-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 10px;
  background-color: #f8f9fa;
  border: 1px solid #edf2f7;
  color: #2d3748;
  cursor: pointer;
}

/* 다중 이미지 썸네일 미리보기 카드 */
.venmo-photo-card {
  background-color: #ffffff;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.photo-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.photo-card-title {
  color: #111111;
}

.photo-card-del-btn {
  background: none;
  border: none;
  color: #e53e3e;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.venmo-multi-preview-grid {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.venmo-bottom-preview-wrap {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  border: 1px solid #edf2f7;
}

.venmo-bottom-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.venmo-single-del-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  cursor: pointer;
}
</style>
