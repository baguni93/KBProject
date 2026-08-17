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

      <!-- B. 소비 카테고리 퀵 선택 칩 목록 -->
      <div class="venmo-category-section">
        <div class="venmo-category-header">
          <span class="text-13-bold venmo-sec-lbl">
            <i class="fa-solid fa-tag" style="color: #ffbc2e;"></i> 소비 카테고리
          </span>
          <button
            v-if="categoryList.length > 4"
            type="button"
            class="venmo-toggle-btn text-12-bold"
            @click="toggleCategory"
          >
            {{ isCategoryExpanded ? "접기 ▲" : "더보기 ▼" }}
          </button>
        </div>
        <div class="venmo-category-chips">
          <button
            v-for="cat in displayedCategories"
            :key="cat.spendingCategoryId"
            type="button"
            class="venmo-chip-btn"
            :class="{ active: selectedCategoryId === cat.spendingCategoryId }"
            @click="$emit('update:selectedCategoryId', cat.spendingCategoryId)"
          >
            <span>{{ cat.categoryName }}</span>
          </button>
        </div>
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
    default: null,
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

defineEmits([
  "update:selectedCategoryId",
  "update:remitMemo",
  "update:remitVisibility",
  "removeFile",
  "fileChange",
  "submit",
]);

const isCategoryExpanded = ref(false);

const toggleCategory = () => {
  isCategoryExpanded.value = !isCategoryExpanded.value;
};

const displayedCategories = computed(() => {
  if (!props.categoryList || props.categoryList.length === 0) return [];
  if (isCategoryExpanded.value) {
    return props.categoryList;
  }
  return props.categoryList.slice(0, 4);
});

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
  border: 1px solid #f0e6d2;
  border-radius: 18px;
  padding: 16px 20px;
  width: 100%;
  box-sizing: border-box;
}

.venmo-header-info {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.venmo-avatar-circle {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: #ffbc2e;
  color: #111111;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  flex-shrink: 0;
}

.venmo-header-text {
  flex: 1;
  min-width: 0;
}

.venmo-header-title {
  color: #111111;
  font-size: 16px;
  margin: 0;
  white-space: nowrap;
}

.venmo-amount-sub {
  margin: 4px 0 0;
  color: #d99b00;
  font-size: 14px;
  white-space: nowrap;
}

.venmo-tag-badge {
  background-color: rgba(255, 188, 46, 0.2);
  color: #111111;
  padding: 6px 14px;
  border-radius: 12px;
  white-space: nowrap;
  flex-shrink: 0;
}

/* =========================================
   2. Venmo 노트 작성 통합 컴포저 카드
========================================= */
.venmo-composer-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 22px;
  padding: 22px 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-sizing: border-box;
}

.venmo-note-area {
  width: 100%;
}

.venmo-note-textarea {
  width: 100%;
  min-height: 80px;
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

/* 카테고리 태그 섹션 */
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
  color: #d99b00;
  cursor: pointer;
}

.venmo-category-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.venmo-chip-btn {
  padding: 8px 15px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  background-color: #f7fafc;
  color: #4a5568;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.venmo-chip-btn:hover {
  background-color: #edf2f7;
}

.venmo-chip-btn.active {
  background-color: #fff8e6;
  border-color: #ffbc2e;
  color: #111111;
  font-weight: 700;
}

.venmo-divider {
  height: 1px;
  background-color: #edf2f7;
  margin: 2px 0;
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
  padding: 4px;
  border-radius: 12px;
  gap: 3px;
}

.venmo-vis-opt {
  padding: 7px 14px;
  border: none;
  border-radius: 9px;
  background-color: transparent;
  color: #718096;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s ease;
}

.venmo-vis-opt.active {
  background-color: #ffffff;
  color: #111111;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}

/* 사진 첨부 버튼 */
.venmo-photo-attach {
  display: flex;
  align-items: center;
}

.venmo-photo-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background-color: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  color: #4a5568;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.venmo-photo-btn:hover {
  background-color: #edf2f7;
}

.hidden-file-input {
  display: none;
}

/* =========================================
   3. 첨부 사진 전용 하단 카드 (다중 사진 지원)
========================================= */
.venmo-photo-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 20px;
  padding: 16px 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.photo-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.photo-card-title {
  color: #4a5568;
  display: flex;
  align-items: center;
  gap: 6px;
}

.photo-card-del-btn {
  border: none;
  background: none;
  color: #e53e3e;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}

.venmo-multi-preview-grid {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 4px 0;
}

.venmo-bottom-preview-wrap {
  position: relative;
  flex-shrink: 0;
  width: 140px;
  height: 140px;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #edf2f7;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
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
