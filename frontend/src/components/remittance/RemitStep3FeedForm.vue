<template>
  <div class="step-content-wrap">
    <!-- 수취인 요약 카드 -->
    <div class="receiver-summary-box">
      <div class="summary-main-line">
        <div class="receiver-info-col">
          <h4 class="receiver-name text-15-bold">
            <template v-if="remitType === 'FRIEND'">
              {{ selectedFriendObj?.name || "선택한 친구" }}님에게 송금
            </template>
            <template v-else>
              {{ accountForm.receiverName || "수취인" }}님에게 송금
            </template>
          </h4>
          <span
            class="text-13"
            style="
              color: var(--color-primary-border, #cc9200);
              font-weight: 700;
            "
          >
            송금 금액: {{ formatCurrency(remitAmount) }} 원
          </span>
        </div>
        <span class="summary-type-tag text-13-bold">{{
          remitType === "FRIEND" ? "친구 송금" : "계좌 송금"
        }}</span>
      </div>
    </div>

    <!-- 1. 소비 카테고리 선택 -->
    <div class="form-field-group">
      <div class="category-title-flex">
        <label class="field-label text-13-bold" style="margin-bottom: 0"
          ><i class="fa-solid fa-shapes brand-ic"></i> 소비 카테고리
          선택</label
        >
        <button
          type="button"
          class="category-toggle-sub-btn text-13-bold"
          @click="$emit('toggleCategoryExpanded')"
        >
          <span>{{
            isCategoryExpanded
              ? "접기 ▲"
              : "더보기 (" + (categoryList.length - 4) + "개) ▼"
          }}</span>
        </button>
      </div>
      <SpendingCategorySelector
        :model-value="selectedCategoryId"
        @update:model-value="$emit('update:selectedCategoryId', $event)"
        :categories="displayedCategoryList"
        compact
      />
    </div>

    <!-- 2. 피드 메시지 (메모) -->
    <div class="form-field-group">
      <label class="field-label text-13-bold"
        ><i class="fa-solid fa-message brand-ic"></i> 피드에 남길 내용
        (메모)</label
      >
      <textarea
        :value="remitMemo"
        @input="$emit('update:remitMemo', $event.target.value)"
        class="custom-textarea text-15"
        placeholder="피드에 남길 메시지를 입력하세요 (예: 축하해! 🎉)"
      ></textarea>
    </div>

    <!-- 3. 공개 범위 선택 -->
    <div class="form-field-group">
      <label class="field-label text-13-bold"
        ><i class="fa-solid fa-shield-halved brand-ic"></i> 공개 범위
        선택</label
      >
      <div class="vis-grid">
        <button
          type="button"
          class="content-btn secondary text-13-bold"
          :class="{ active: remitVisibility === 'PUBLIC' }"
          @click="$emit('update:remitVisibility', 'PUBLIC')"
        >
          🌐 전체 공개
        </button>
        <button
          type="button"
          class="content-btn secondary text-13-bold"
          :class="{ active: remitVisibility === 'FRIEND' }"
          @click="$emit('update:remitVisibility', 'FRIEND')"
        >
          👥 친구 공개
        </button>
        <button
          type="button"
          class="content-btn secondary text-13-bold"
          :class="{ active: remitVisibility === 'PRIVATE' }"
          @click="$emit('update:remitVisibility', 'PRIVATE')"
        >
          🔒 나만 보기
        </button>
      </div>
    </div>

    <!-- 4. 사진 첨부 -->
    <div class="form-field-group">
      <div class="file-header-line">
        <label class="field-label text-13-bold"
          ><i class="fa-solid fa-image brand-ic"></i> 소셜 피드 사진 첨부
          (선택)</label
        >
        <button
          v-if="selectedFile"
          type="button"
          class="cancel-file-btn text-13-bold"
          @click="$emit('removeFile')"
        >
          첨부 취소
        </button>
      </div>

      <div class="photo-upload-container">
        <label v-if="!imagePreviewUrl" class="photo-upload-box">
          <i class="fa-solid fa-camera upload-icon"></i>
          <span class="upload-text text-13-bold">사진 추가하기</span>
          <input
            type="file"
            accept="image/*"
            class="hidden-file-input"
            @change="$emit('fileChange', $event)"
          />
        </label>
        <div v-else class="preview-img-wrap">
          <img :src="imagePreviewUrl" class="preview-img" />
          <button
            type="button"
            class="remove-photo-btn"
            @click="$emit('removeFile')"
          >
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
      </div>
    </div>

    <div class="next-btn-wrap">
      <button class="bottom-btn text-18-bold" @click="$emit('submit')">
        송금하기 <i class="fa-solid fa-paper-plane"></i>
      </button>
    </div>
  </div>
</template>

<script setup>
import SpendingCategorySelector from "@/components/common/SpendingCategorySelector.vue";

defineProps({
  remitType: {
    type: String,
    required: true,
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
  isCategoryExpanded: {
    type: Boolean,
    default: false,
  },
  categoryList: {
    type: Array,
    default: () => [],
  },
  displayedCategoryList: {
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
  formatCurrency: {
    type: Function,
    required: true,
  },
});

defineEmits([
  "toggleCategoryExpanded",
  "update:selectedCategoryId",
  "update:remitMemo",
  "update:remitVisibility",
  "removeFile",
  "fileChange",
  "submit",
]);
</script>

<style scoped>
.receiver-summary-box {
  background-color: var(--color-bg-screen, #f8f9fa);
  border-radius: 16px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.summary-main-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.receiver-name {
  margin: 0 0 4px;
}

.summary-type-tag {
  color: #ffbc00;
}

.form-field-group {
  margin-bottom: 20px;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  color: var(--color-text-main, #111111);
}

.category-title-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.category-toggle-sub-btn {
  border: none;
  background: none;
  color: #ffbc00;
  cursor: pointer;
}

.custom-textarea {
  width: 100%;
  height: 80px;
  border: 1px solid var(--color-border-main, #ededed);
  border-radius: 12px;
  padding: 12px 16px;
  background-color: var(--color-bg-screen, #f8f9fa);
  outline: none;
  resize: none;
  box-sizing: border-box;
}

.vis-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.content-btn {
  padding: 10px;
  border: 1px solid var(--color-border-main, #ededed);
  border-radius: 10px;
  background-color: #ffffff;
  cursor: pointer;
}

.content-btn.active {
  border-color: #ffbc00;
  background-color: rgba(255, 188, 0, 0.08);
  color: #111111;
}

.file-header-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cancel-file-btn {
  border: none;
  background: none;
  color: #d32f2f;
  cursor: pointer;
}

.photo-upload-container {
  width: 100%;
}

.photo-upload-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
  border: 2px dashed var(--color-border-main, #ededed);
  border-radius: 14px;
  cursor: pointer;
  color: #888888;
  gap: 6px;
}

.hidden-file-input {
  display: none;
}

.preview-img-wrap {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 14px;
  overflow: hidden;
}

.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-photo-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: rgba(0,0,0,0.6);
  color: #ffffff;
  border: none;
  cursor: pointer;
}

.bottom-btn {
  width: 100%;
  height: 52px;
  border: none;
  background-color: #ffbc00;
  color: #111111;
  border-radius: 14px;
  font-weight: 700;
  cursor: pointer;
}
</style>
