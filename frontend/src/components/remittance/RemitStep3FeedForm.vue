<template>
  <div class="venmo-step-wrap">
    <!-- 1. 수취인 및 송금 금액 헤더 카드 -->
    <div class="venmo-header-card">
      <div class="venmo-header-info">
        <div class="venmo-avatar-circle">
          <i class="fa-solid fa-paper-plane"></i>
        </div>
        <div class="venmo-header-text">
          <h3 class="text-16-bold m-0 venmo-header-title">
            <template v-if="remitType === 'FRIEND'">
              {{ selectedFriendObj?.name || "선택한 친구" }}님에게
            </template>
            <template v-else>
              {{ accountForm.receiverName || "수취인" }}님에게
            </template>
          </h3>
          <p class="venmo-amount-sub text-14-bold">
            송금 금액: {{ formatCurrency(remitAmount) }}원
          </p>
        </div>
      </div>
      <span class="venmo-tag-badge text-12-bold">
        {{ remitType === "FRIEND" ? "친구 송금" : "계좌 송금" }}
      </span>
    </div>

    <!-- 2. Venmo 스타일 소셜 피드 작성 통합 박스 -->
    <div class="venmo-composer-card">
      <!-- A. 피드 메시지 입력 영역 (What's this for?) -->
      <div class="venmo-note-area">
        <textarea
          :value="remitMemo"
          @input="$emit('update:remitMemo', $event.target.value)"
          class="venmo-note-textarea text-16"
          placeholder="피드에 어떤 추억을 남길까요? 🍕"
          rows="3"
        ></textarea>
      </div>

      <!-- B. 소비 카테고리 퀵 선택 칩 목록 (더치페이와 100% 동일한 규격 및 아이콘 적용) -->
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

        <!-- 더보기 선택 시: 팀 공용 SpendingCategorySelector (선택 시 1줄로 자동 접힘) -->
        <SpendingCategorySelector
          v-else
          :model-value="selectedCategoryId || 1"
          @update:model-value="onSelectExpandedCategory"
          :categories="categoryList"
          compact
        />
      </div>

      <div class="venmo-divider"></div>

      <!-- C. 하단 툴바: 공개 범위 세그먼트 스위치 & 다중 사진 추가 버튼 -->
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

        <!-- 다중 사진 첨부 버튼 (multiple 속성 적용) -->
        <div class="venmo-photo-attach">
          <label class="venmo-photo-btn text-13-bold">
            <i class="fa-solid fa-camera" style="color: #ffbc2e;"></i>
            <span>{{ effectivePreviewUrls.length > 0 ? "사진 추가" : "사진 추가" }}</span>
            <input
              type="file"
              multiple
              accept="image/*"
              class="hidden-file-input"
              @change="$emit('fileChange', $event)"
            />
          </label>
        </div>
      </div>
    </div>

    <!-- 3. 첨부 사진 전용 하단 카드 (다중 사진 지원: 여러 장 누적 나열 & 개별/전체 삭제) -->
    <div v-if="effectivePreviewUrls.length > 0" class="venmo-photo-card">
      <div class="photo-card-header">
        <span class="text-13-bold photo-card-title">
          <i class="fa-solid fa-images" style="color: #ffbc2e;"></i> 첨부된 사진 ({{ effectivePreviewUrls.length }}장)
        </span>
        <button
          type="button"
          class="photo-card-del-btn text-12-bold"
          @click="$emit('removeFile')"
        >
          <i class="fa-solid fa-trash-can"></i> 전체 삭제
        </button>
      </div>
      <div class="venmo-multi-preview-grid">
        <div
          v-for="(url, idx) in effectivePreviewUrls"
          :key="idx"
          class="venmo-bottom-preview-wrap"
        >
          <img :src="url" class="venmo-bottom-preview-img" alt="피드 첨부 이미지" />
          <button
            type="button"
            class="venmo-single-del-btn"
            @click="$emit('removeFile', idx)"
            title="이 사진 삭제"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 4. 하단 고정 송금 버튼 (팀 공통 하단 고정 규격 100% 일치) -->
    <div class="bottom-btn-area single">
      <button type="button" class="bottom-btn primary-button text-17-bold" @click="$emit('submit')">
        송금하기
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { getCategoryIcon } from "@/util/analysis";
import SpendingCategorySelector from "@/components/common/SpendingCategorySelector.vue";

const props = defineProps({
  remitType: {
    type: String,
    default: "ACCOUNT",
  },
  selectedFriendObj: {
    type: Object,
    default: null,
  },
  accountForm: {
    type: Object,
    default: () => ({}),
  },
  remitAmount: {
    type: Number,
    default: 0,
  },
  categoryList: {
    type: Array,
    default: () => [],
  },
  selectedCategoryId: {
    type: [Number, String],
    default: 1,
  },
  remitMemo: {
    type: String,
    default: "",
  },
  remitVisibility: {
    type: String,
    default: "PUBLIC",
  },
  selectedFile: {
    type: Object,
    default: null,
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
    default: (val) => (val ? Number(val).toLocaleString("ko-KR") : "0"),
  },
});

const emit = defineEmits([
  "update:selectedCategoryId",
  "update:remitMemo",
  "update:remitVisibility",
  "removeFile",
  "fileChange",
  "submit",
]);

const isCategoryExpanded = ref(false);

const defaultSlim = [
  { id: 1, name: "식비" },
  { id: 2, name: "카페" },
  { id: 3, name: "생활" },
  { id: 4, name: "교통" },
];

const slimCategories = computed(() => {
  const selId = props.selectedCategoryId;
  if (selId && !defaultSlim.some(d => d.id === selId)) {
    const found = props.categoryList.find(c => (c.spendingCategoryId || c.id) === selId);
    if (found) {
      return [
        ...defaultSlim.slice(0, 3),
        { id: selId, name: found.categoryName || found.name || "선택됨" }
      ];
    }
  }
  return defaultSlim;
});

const selectSlimCategory = (id) => {
  emit("update:selectedCategoryId", id);
};

const onSelectExpandedCategory = (id) => {
  emit("update:selectedCategoryId", id);
  isCategoryExpanded.value = false;
};

const effectivePreviewUrls = computed(() => {
  if (props.imagePreviewUrls && props.imagePreviewUrls.length > 0) {
    return props.imagePreviewUrls;
  }
  if (props.imagePreviewUrl) {
    return [props.imagePreviewUrl];
  }
  return [];
});
</script>

<style scoped>
@import "@/components/common/common/common.css";

.venmo-step-wrap {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  position: relative;
  min-height: 100%;
  padding-bottom: 90px;
  box-sizing: border-box;
}

/* =========================================
   1. 수취인 헤더 카드
========================================= */
.venmo-header-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #fffdf8 0%, #f8f9fa 100%);
  border: 1px solid #feebc8;
  border-radius: 16px;
  padding: 14px 16px;
  gap: 8px;
  box-sizing: border-box;
  box-shadow: 0 2px 8px rgba(255, 188, 46, 0.08);
}

.venmo-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.venmo-header-text {
  min-width: 0;
}

.venmo-avatar-circle {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background-color: #ffbc2e;
  color: #111111;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  box-shadow: 0 2px 6px rgba(255, 188, 46, 0.3);
  flex-shrink: 0;
}

.venmo-header-title {
  color: #111111;
  font-size: 15px;
  font-weight: 700;
  white-space: nowrap;
  word-break: keep-all;
}

.venmo-amount-sub {
  color: #d97706;
  margin: 2px 0 0;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.venmo-tag-badge {
  background-color: #fef3c7;
  color: #b45309;
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  flex-shrink: 0;
}

/* =========================================
   2. 소셜 피드 작성 통합 카드
========================================= */
.venmo-composer-card {
  background: #ffffff;
  border: 1px solid #edf2f7;
  border-radius: 20px;
  padding: 20px;
  box-sizing: border-box;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.venmo-note-area {
  width: 100%;
}

.venmo-note-textarea {
  width: 100%;
  border: none;
  outline: none;
  resize: none;
  font-size: 16px;
  line-height: 1.6;
  color: #111111;
  background: transparent;
  padding: 0;
  margin: 0;
  box-sizing: border-box;
}

.venmo-note-textarea::placeholder {
  color: #a0aec0;
}

/* 카테고리 태그 섹션 (더치페이와 100% 동일한 1줄 칩 및 아이콘) */
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
  color: #718096;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.venmo-toggle-btn {
  border: none;
  background: none;
  color: #ffbc2e;
  cursor: pointer;
  padding: 0;
}

/* 더치페이 동일 1줄 슬림 카테고리 칩 스타일 */
.dutch-cat-chip-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.dutch-cat-chip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 38px;
  border-radius: 10px;
  background-color: #f7fafc;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 0 4px;
}

.dutch-cat-chip.active {
  background-color: #fffdf8;
  border-color: #ffbc2e;
  box-shadow: 0 2px 6px rgba(255, 188, 46, 0.2);
}

.cat-fa-ic {
  font-size: 14px;
  color: #718096;
}

.dutch-cat-chip.active .cat-fa-ic {
  color: #ffbc2e;
}

.cat-name {
  color: #2d3748;
}

.dutch-cat-chip.active .cat-name {
  color: #111111;
}

.venmo-divider {
  height: 1px;
  background-color: #f7fafc;
  width: 100%;
}

/* 하단 툴바 */
.venmo-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.venmo-visibility-bar {
  display: flex;
  background-color: #edf2f7;
  border-radius: 12px;
  padding: 3px;
  gap: 2px;
}

.venmo-vis-opt {
  border: none;
  background: transparent;
  padding: 6px 12px;
  border-radius: 9px;
  color: #718096;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s ease;
}

.venmo-vis-opt.active {
  background-color: #ffffff;
  color: #111111;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.venmo-photo-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: #f7fafc;
  border: 1px solid #e2e8f0;
  padding: 6px 14px;
  border-radius: 12px;
  color: #4a5568;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.venmo-photo-btn:hover {
  background-color: #edf2f7;
}

.hidden-file-input {
  display: none;
}

/* =========================================
   3. 첨부 사진 갤러리 카드
========================================= */
.venmo-photo-card {
  background-color: #ffffff;
  border: 1px solid #edf2f7;
  border-radius: 16px;
  padding: 16px;
  box-sizing: border-box;
}

.photo-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.photo-card-title {
  color: #2d3748;
}

.photo-card-del-btn {
  background: none;
  border: none;
  color: #e53e3e;
  cursor: pointer;
}

.venmo-multi-preview-grid {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.venmo-bottom-preview-wrap {
  position: relative;
  width: 80px;
  height: 80px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.venmo-bottom-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.venmo-single-del-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: rgba(0, 0, 0, 0.65);
  color: #ffffff;
  border: none;
  font-size: 11px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;
}

.venmo-single-del-btn:hover {
  background-color: rgba(0, 0, 0, 0.85);
}

/* =========================================
   4. 팀 공통 하단 고정 버튼
========================================= */
.bottom-btn-area.single {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 0 32px;
  background: #ffffff;
  z-index: 100;
  box-sizing: border-box;
}

.bottom-btn-area.single .bottom-btn {
  width: 100%;
  height: 52px;
  border-radius: 14px;
  font-size: 17px;
  font-weight: 700;
  border: none;
  background-color: #ffbc2e;
  color: #111111;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;
}

.bottom-btn-area.single .bottom-btn:active {
  background-color: #e5a900;
}
</style>
