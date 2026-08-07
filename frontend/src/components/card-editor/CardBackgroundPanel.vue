<template>
  <div class="panel">
    <!--
      CardEditor에서 전달받은 현재 탭 상태에 따라
      보여줄 배경 설정 UI를 변경
    -->

    <!-- 기본 배경 설정 탭 -->
    <template v-if="tab === 'basic'">
      <!-- 단색 선택 영역 -->
      <section>
        <div class="label">단색</div>

        <CardColorPick
          :model-value="cardStore.color"
          @update:model-value="cardStore.setColor"
        />
      </section>

      <!-- 그라데이션 선택 영역 -->
      <section>
        <div class="label">그라데이션</div>

        <CardGradientPick
          :model-value="cardStore.gradient"
          @update:model-value="cardStore.setGradient"
        />
      </section>

      <!-- 이미지 배경 선택 영역 -->
      <section>
        <div class="label">특색 배경</div>

        <CardSpecialBackgroundPick
          :model-value="cardStore.image"
          @update:model-value="cardStore.setImage"
        />
      </section>
    </template>

    <!-- 내 사진 탭 -->
    <template v-else>
      <section class="photo-section">
        <div class="label">내 사진</div>
        <!-- 이미지 정보 -->
        <div class="info-box">
          <div class="info-title">
            <i class="fa-solid fa-file-image"></i>
            이미지 업로드 안내
          </div>

          <div class="info-tags">
            <span>
              형식
              <b>JPG · PNG · WEBP</b>
            </span>

            <span>
              최대 용량
              <b>5 MB</b>
            </span>

            <span>
              권장 해상도
              <b>1080 × 680px</b>
            </span>

            <span>
              권장 비율
              <b>16 : 10</b>
            </span>
          </div>

          <div class="info-desc">
            <p>사람 얼굴, 개인정보가 포함된 이미지는 업로드하지 마세요.</p>

            <p>카드 비율에 맞게 자동으로 크롭됩니다.</p>
          </div>
        </div>
        <!-- 숨겨진 파일 input -->
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          hidden
          @change="selectPhoto"
        />

        <!-- 업로드 버튼 -->
        <button class="upload-box" @click="openFile">
          <i class="fa-solid fa-image upload-icon"></i>

          <span> 사진 불러오기 </span>

          <small> 갤러리에서 선택 </small>
        </button>

        <!-- 미리보기 -->
        <div v-if="previewImage" class="preview-box">
          <img :src="previewImage" />

          <button class="remove-btn" @click="removeImage">
            <i class="fa-solid fa-arrow-rotate-left"></i>
          </button>
        </div>

        <!-- 적용 토글 -->
        <div v-if="previewImage" class="apply-row">
          <span> 카드 배경 적용 </span>

          <button
            class="toggle"
            :class="{ active: applyImage }"
            @click="toggleApply"
          >
            <span></span>
          </button>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import CardColorPick from './CardColorPick.vue';
import CardGradientPick from './CardGradientPick.vue';
import CardSpecialBackgroundPick from './CardSpecialBackgroundPick.vue';
import { useCardEditorStore } from '@/stores/cardEditorStore';

const cardStore = useCardEditorStore();

const props = defineProps({
  tab: {
    type: String,
    default: 'basic',
  },
});

const fileInput = ref(null);

//토글 기능을 위한 전 이미지
const previewImage = ref('');

//이미지 적용 여부
const applyImage = ref(false);

// 파일 선택창 열기
const openFile = () => {
  fileInput.value.click();
};

// 이미지 선택
const selectPhoto = (event) => {
  const file = event.target.files[0];

  if (!file) {
    console.log('[selectPhoto] 파일 없음');
    return;
  }

  //현재 카드 상태를 임시 저장(백업)하는 용도
  cardStore.saveSnapshot();

  // 미리보기 생성
  const imageUrl = URL.createObjectURL(file);

  previewImage.value = imageUrl;

  applyImage.value = true;

  console.log('[selectPhoto] imageUrl:', imageUrl);

  // 카드 배경 적용
  cardStore.setImage(imageUrl);
};

// 적용 토글
const toggleApply = () => {
  applyImage.value = !applyImage.value;

  if (applyImage.value) {
    cardStore.image = previewImage.value;
  } else {
    cardStore.restoreSnapshot();
  }
};

// 이미지 제거
const removeImage = () => {
  previewImage.value = '';
  applyImage.value = false;
  cardStore.restoreSnapshot();
};
</script>

<style scoped>
.preview-box {
  position: relative;

  width: 330px;

  height: 150px;

  border-radius: 16px;

  overflow: hidden;
}

.preview-box img {
  width: 100%;

  height: 100%;

  object-fit: cover;
}

.remove-btn {
  position: absolute;

  top: 10px;
  right: 10px;

  width: 32px;
  height: 32px;

  display: flex;

  justify-content: center;

  align-items: center;

  border: none;

  border-radius: 50%;

  background: rgba(0, 0, 0, 0.45);

  color: white;

  cursor: pointer;

  backdrop-filter: blur(6px);

  transition:
    transform 0.2s ease,
    background 0.2s ease;
}

.remove-btn i {
  font-size: 16px;
}

.remove-btn:hover {
  background: rgba(0, 0, 0, 0.7);

  transform: scale(1.08);
}

.remove-btn:active {
  transform: scale(0.92);
}
.photo-section {
  width: 100%;

  display: flex;

  flex-direction: column;

  align-items: center;

  gap: 12px;
}
.upload-box {
  width: 330px;

  height: 130px;

  border-radius: 18px;

  border: 1px dashed #ddd;

  background: #fafafa;

  display: flex;

  flex-direction: column;

  justify-content: center;

  align-items: center;

  gap: 6px;

  cursor: pointer;

  font-size: 14px;

  color: #555;
}

.upload-icon {
  font-size: 32px;
}

.upload-box small {
  color: #999;

  font-size: 12px;
}
.info-box {
  width: 330px;

  padding: 12px;

  border-radius: 14px;

  background: #eef6ff;

  box-sizing: border-box;
}

/* 제목 */
.info-title {
  display: flex;

  align-items: center;

  gap: 5px;

  color: #2563eb;

  font-size: 11px;

  font-weight: 700;

  margin-bottom: 8px;
}

.info-title i {
  font-size: 11px;

  color: #2563eb;
}

/* 태그 영역 */
.info-tags {
  display: flex;

  flex-wrap: wrap;

  gap: 6px;
}

.info-tags span {
  display: flex;

  align-items: center;

  gap: 3px;

  padding: 6px 8px;

  border-radius: 7px;

  background: white;

  color: #8a9bb5;

  font-size: 9px;

  line-height: 1;
}

.info-tags b {
  color: #2563eb;

  font-size: 9px;

  font-weight: 700;
}

/* 설명 */
.info-desc {
  margin-top: 8px;
}

.info-desc p {
  margin: 2px 0;

  color: #4d7fc7;

  font-size: 10px;

  line-height: 1.4;
}

.preview-box {
  width: 330px;

  height: 150px;

  border-radius: 16px;

  overflow: hidden;
}

.preview-box img {
  width: 100%;

  height: 100%;

  object-fit: cover;
}

.apply-row {
  width: 330px;

  display: flex;

  justify-content: space-between;

  align-items: center;

  font-size: 14px;
}

.toggle {
  width: 46px;

  height: 26px;

  border: none;

  border-radius: 20px;

  background: #ddd;

  padding: 3px;
}

.toggle span {
  display: block;

  width: 20px;

  height: 20px;

  border-radius: 50%;

  background: white;

  transition: 0.2s;
}

.toggle.active {
  background: #ffc400;
}

.toggle.active span {
  transform: translateX(20px);
}

/*
  배경 설정 패널 전체

  CardEditor 내부에서
  배경 옵션 영역을 담당
*/
.panel {
  width: 100%;

  display: flex;

  flex-direction: column;

  gap: 18px;

  box-sizing: border-box;

  padding: 0 12px;
}

/*
  각각의 설정 섹션

  단색 / 그라데이션 / 특색 배경
*/
section {
  width: 100%;

  /*
    내용 크기만큼 유지
    (줄어들지 않도록 설정)
  */
  flex-shrink: 0;

  display: flex;

  flex-direction: column;

  /*
    내부 요소 가운데 정렬
  */
  align-items: center;

  /*
    제목과 선택 UI 사이 간격
  */
  gap: 6px;

  overflow: visible;
}

/*
  섹션 제목 스타일

  ex)
  단색
  그라데이션
  특색 배경
*/
.label {
  width: 100%;

  /*
    선택 UI 최대 크기와 맞춤
  */
  max-width: 330px;

  font-size: 10px;

  color: #777;

  font-weight: 500;
}
</style>
