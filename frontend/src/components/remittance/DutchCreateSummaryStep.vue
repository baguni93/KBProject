<template>
  <div class="kb-dutch-summary-wrapper">
    <!-- 1. 정산 제목 입력 & 총 금액 (우리 KB 라이트 룩앤필) -->
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
          <div class="item-title-text text-16-bold">{{ dutchRoomTitle || '식사' }}</div>
          <div class="item-amount-text text-20-bold">{{ formatCurrency(remitAmount || 0) }}원</div>
          <div class="item-members-text text-13">
            {{ getFriendName(selectedDutchFriends[0]) || '수취인' }} 외 {{ selectedDutchFriends.length }}명
          </div>
        </div>
      </div>
      <button type="button" class="btn-card-close">
        <i class="fa-solid fa-circle-xmark"></i>
      </button>
    </div>



    <!-- 4. 피드 메모 & 이미지 첨부 영역 -->
    <div class="kb-feed-box">
      <div class="feed-section-head text-13-bold">
        <i class="fa-solid fa-square-rss brand-ic"></i> 피드 공유 메모 & 사진
      </div>
      <textarea
        :value="remitMemo"
        class="kb-memo-textarea text-14"
        placeholder="피드 공유 메모를 적어주세요 (정산 완료 시 피드에 남습니다)"
        rows="3"
        @input="$emit('update:remitMemo', $event.target.value)"
      ></textarea>

      <div class="photo-attach-wrapper">
        <label class="photo-btn-label text-13-bold">
          <i class="fa-solid fa-camera"></i> 이미지 첨부하기
          <input
            type="file"
            accept="image/*"
            style="display: none"
            @change="$emit('fileChange', $event)"
          />
        </label>

        <div v-if="imagePreviewUrl" class="preview-img-box">
          <img :src="imagePreviewUrl" class="thumb-img" />
          <button type="button" class="btn-del-img" @click="$emit('removeFile')">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- 5. 하단 공통 버튼 -->
    <div class="bottom-btn-area single">
      <button
        type="button"
        class="bottom-btn"
        @click="$emit('submit')"
      >
        정산 요청
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
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
  getFriendName: {
    type: Function,
    default: () => "참여자",
  },
  remitMemo: {
    type: String,
    default: "",
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
  "update:dutchRoomTitle",
  "update:remitMemo",
  "fileChange",
  "removeFile",
  "submit",
]);
</script>

<style scoped>
@import "@/components/common/common/common.css";

/* ========================================
   우리 KB 서비스 브랜드 라이트 / 모던 테마
======================================== */
.kb-dutch-summary-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px 16px 28px;
  background-color: #ffffff;
}

.kb-summary-header-card {
  text-align: center;
  padding: 16px 0 10px;
}

.title-edit-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.kb-title-input {
  background: transparent;
  border: none;
  color: #777777;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
  outline: none;
  width: 180px;
}

.kb-title-input:focus {
  color: #111111;
  border-bottom: 1.5px solid #ffbc00;
}

.edit-pencil-ic {
  color: #888888;
  font-size: 13px;
}

.kb-total-amount {
  font-size: 30px;
  font-weight: 800;
  color: #111111;
  margin-top: 8px;
  letter-spacing: -0.5px;
}

.kb-item-card-box {
  background-color: #ffffff;
  border: 1px solid #eaeaea;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  border-radius: 18px;
  padding: 18px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.card-box-left {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.icon-utensils-box {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background-color: #fff2ee;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #ff6b4a;
}

.card-info-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-title-text {
  color: #111111;
}

.item-amount-text {
  color: #111111;
}

.item-members-text {
  color: #777777;
}

.btn-card-close {
  background: transparent;
  border: none;
  color: #cccccc;
  font-size: 20px;
  cursor: pointer;
  transition: color 0.2s;
}

.btn-card-close:hover {
  color: #888888;
}

.btn-kb-outline {
  width: 100%;
  height: 50px;
  background-color: #f8f9fa;
  border: 1px solid #e0e0e0;
  border-radius: 14px;
  color: #555555;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-kb-outline:hover {
  background-color: #f1f3f5;
  color: #111111;
}

.kb-feed-box {
  background-color: #f8f9fa;
  border: 1px solid #f0f0f0;
  border-radius: 18px;
  padding: 16px;
}

.feed-section-head {
  color: #555555;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.brand-ic {
  color: #ffbc00;
}

.kb-memo-textarea {
  width: 100%;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 12px;
  color: #111111;
  box-sizing: border-box;
  resize: none;
  outline: none;
}

.kb-memo-textarea:focus {
  border-color: #ffbc00;
}

.photo-attach-wrapper {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.photo-btn-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  color: #555555;
  padding: 8px 14px;
  border-radius: 20px;
  cursor: pointer;
}

.preview-img-box {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 8px;
  overflow: hidden;
}

.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.btn-del-img {
  position: absolute;
  top: 2px;
  right: 2px;
  background: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  border: none;
  border-radius: 50%;
  width: 16px;
  height: 16px;
  font-size: 10px;
  cursor: pointer;
}

.kb-dutch-summary-wrapper {
  display: flex;
  flex-direction: column;
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
}
</style>
