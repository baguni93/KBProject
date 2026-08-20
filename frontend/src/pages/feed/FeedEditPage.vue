<template>
  <div class="page-common">
    <PageHeader title="피드 수정" />

    <!-- 가운데 콘텐츠 영역 -->
    <main class="content-area">
      <!-- 공개 설정 -->
      <section v-if="canEditVisibility" class="section">
        <h3>공개 설정</h3>

        <div class="button-group">
          <button
            v-for="item in visibilityList"
            :key="item.value"
            class="select-btn"
            :class="{
              active: form.visibility === item.value,
            }"
            @click="form.visibility = item.value"
          >
            {{ item.label }}
          </button>
        </div>
      </section>

      <!-- 내용 -->
      <section class="section">
        <h3>내용(20자 이내)</h3>

        <textarea
          v-model="form.content"
          maxlength="20"
          class="textarea"
        ></textarea>

        <div class="count">{{ form.content.length }}/20</div>
      </section>

      <!-- 사진 -->
      <section v-if="canEditImage" class="section">
        <h3>사진</h3>

        <div class="image-list">
          <!-- 기존 + 신규 이미지 -->
          <div
            v-for="(image, index) in form.images"
            :key="index"
            class="image-item"
          >
            <img :src="image.url || image.preview" />

            <button
              class="remove-btn"
              type="button"
              @click="removeImage(index)"
            >
              ×
            </button>
          </div>

          <!-- 추가 버튼 -->
          <label class="add-image">
            +

            <input
              type="file"
              multiple
              accept="image/*"
              hidden
              @change="changeImage"
            />
          </label>
        </div>
      </section>
    </main>

    <!-- 하단 버튼 영역 -->
    <div class="bottom-btn-area">
      <button
        class="save-btn"
        type="button"
        :disabled="!isChanged"
        @click="submit"
      >
        수정 완료
      </button>

      <button class="delete-btn" type="button" @click="handleDelete">
        피드 삭제
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import PageHeader from '@/components/common/PageHeader.vue';

import { useFeedStore } from '@/stores/feed';
import feedApi from '@/api/feedApi';

import { useModalStore } from '@/stores/userModalStore.js';

const modalStore = useModalStore();

const router = useRouter();
const route = useRoute();

const feedStore = useFeedStore();

const feedId = Number(route.params.feedId);

const feed = ref({});

/**
 * 수정 전 원본 데이터
 */
const originalForm = ref({
  visibility: '',
  content: '',
});

/**
 * 수정 폼
 */
const form = ref({
  visibility: 'PUBLIC',
  content: '',
  images: [],
});

/**
 * 새로 추가한 이미지 파일
 */
const imageFiles = ref([]);

/**
 * 삭제할 기존 이미지 ID
 */
const deleteFiles = ref([]);

/**
 * 수정 여부
 */
const isChanged = computed(() => {
  return (
    form.value.visibility !== originalForm.value.visibility ||
    form.value.content !== originalForm.value.content ||
    imageFiles.value.length > 0 ||
    deleteFiles.value.length > 0
  );
});

/**
 * 공개 설정 목록
 */
const visibilityList = [
  {
    label: '전체공개',
    value: 'PUBLIC',
  },
  {
    label: '친구공개',
    value: 'FRIEND',
  },
  {
    label: '비공개',
    value: 'PRIVATE',
  },
];

/**
 * 피드 타입
 */
const feedType = computed(() => {
  return feed.value?.feedType;
});

/**
 * 공개 설정 수정 가능 여부
 */
const canEditVisibility = computed(() => {
  return ['TRANSFER', 'PAYMENT', 'EVENT', 'CARD', 'ANALYSIS'].includes(
    feedType.value,
  );
});

/**
 * 이미지 수정 가능 여부
 */
const canEditImage = computed(() => {
  return ['TRANSFER', 'PAYMENT', 'SETTLEMENT'].includes(feedType.value);
});

/**
 * 이미지 추가
 */
const changeImage = (e) => {
  const files = Array.from(e.target.files);

  files.forEach((file) => {
    imageFiles.value.push(file);

    form.value.images.push({
      preview: URL.createObjectURL(file),
      file,
    });
  });

  // 같은 파일을 다시 선택할 수 있도록 초기화
  e.target.value = '';
};

/**
 * 이미지 삭제
 */
const removeImage = (index) => {
  const image = form.value.images[index];

  // 기존 이미지라면 삭제 목록에 추가
  if (image.imageId) {
    deleteFiles.value.push(image.imageId);
  }

  // 새 이미지라면 파일 목록에서도 제거
  if (image.file) {
    const fileIndex = imageFiles.value.indexOf(image.file);

    if (fileIndex !== -1) {
      imageFiles.value.splice(fileIndex, 1);
    }

    // Object URL 정리
    if (image.preview) {
      URL.revokeObjectURL(image.preview);
    }
  }

  // 화면에서 제거
  form.value.images.splice(index, 1);
};

/**
 * 수정 요청
 */
const submit = async () => {
  const isConfirm = await modalStore.showConfirm('피드를 수정하시겠습니까?');

  if (!isConfirm) {
    return;
  }

  const formData = new FormData();

  formData.append('feedId', feedId);
  formData.append('content', form.value.content);
  formData.append('visibility', form.value.visibility);

  // 신규 이미지
  imageFiles.value.forEach((file) => {
    formData.append('files', file);
  });

  // 삭제 이미지
  deleteFiles.value.forEach((id) => {
    formData.append('deleteFiles', id);
  });

  try {
    await feedApi.updateFeed(formData);

    router.push('/feed');
  } catch (e) {
    console.log(e);
  }
};

/**
 * 피드 삭제
 */
const handleDelete = async () => {
  const isConfirm = await modalStore.showConfirm('피드를 삭제하시겠습니까?');

  if (!isConfirm) {
    return;
  }

  try {
    await feedStore.deleteFeed(feedId);

    router.push('/feed');
  } catch (e) {
    console.log(e);
  }
};

/**
 * 초기 데이터 조회
 */
onMounted(async () => {
  feed.value = await feedStore.getFeed(feedId);

  form.value.visibility = feed.value.visibility;
  form.value.content = feed.value.content;
  form.value.images = feed.value.images ?? [];

  originalForm.value = {
    visibility: feed.value.visibility,
    content: feed.value.content,
  };
});
</script>

<style scoped>
/* =========================================================
   전체 페이지
   ========================================================= */

.page-common {
  width: 100%;
  height: 100%;

  display: flex;
  flex-direction: column;

  min-height: 0;

  box-sizing: border-box;

  padding: 0 20px 20px;

  /*
   * 페이지 자체는 스크롤하지 않음
   */
  overflow: hidden;
}

/* =========================================================
   가운데 콘텐츠
   ========================================================= */

.content-area {
  flex: 1;

  min-height: 0;

  width: 100%;

  box-sizing: border-box;

  padding: 13px 0 24px;

  /*
   * 콘텐츠가 길어질 경우
   * 가운데 영역만 스크롤
   */
  overflow-y: auto;
  overflow-x: hidden;

  scrollbar-width: none;
  -ms-overflow-style: none;

  display: flex;
  flex-direction: column;

  gap: 16px;
}

.content-area::-webkit-scrollbar {
  display: none;
}

/* =========================================================
   Section
   ========================================================= */

.section {
  margin-bottom: 12px;

  flex-shrink: 0;
}

.section h3 {
  font-size: 15px;

  font-weight: 700;

  margin: 0 0 12px;
}

/* =========================================================
   공개 설정
   ========================================================= */

.button-group {
  display: flex;

  gap: 10px;
}

.select-btn {
  flex: 1;

  height: 42px;

  border-radius: 12px;

  border: 1px solid #ddd;

  background: white;

  cursor: pointer;
}

.select-btn.active {
  border: 2px solid #34c759;

  color: #34c759;

  font-weight: bold;
}

/* =========================================================
   내용
   ========================================================= */

.textarea {
  width: 100%;

  height: 120px;

  box-sizing: border-box;

  resize: none;

  border: 1px solid #ddd;

  border-radius: 10px;

  padding: 12px;
}

.count {
  margin-top: 6px;

  text-align: right;

  color: #999;

  font-size: 12px;
}

/* =========================================================
   이미지
   ========================================================= */

.image-list {
  display: flex;

  gap: 10px;

  flex-wrap: wrap;
}

.image-item {
  width: 70px;

  height: 70px;

  position: relative;

  flex-shrink: 0;
}

.image-item img {
  width: 100%;

  height: 100%;

  object-fit: cover;

  border-radius: 10px;
}

.remove-btn {
  position: absolute;

  right: -6px;

  top: -6px;

  width: 22px;

  height: 22px;

  border: none;

  border-radius: 50%;

  background: white;

  cursor: pointer;
}

.add-image {
  width: 70px;

  height: 70px;

  flex-shrink: 0;

  border: 2px dashed #ccc;

  border-radius: 10px;

  display: flex;

  justify-content: center;

  align-items: center;

  font-size: 30px;

  color: #999;

  cursor: pointer;

  box-sizing: border-box;
}

/* =========================================================
   하단 버튼 영역
   ========================================================= */

.bottom-btn-area {
  width: 100%;

  flex-shrink: 0;

  display: flex;

  flex-direction: column;

  gap: 10px;

  padding-top: 8px;

  background: white;
}

/* =========================================================
   수정 버튼
   ========================================================= */

.save-btn {
  width: 100%;

  height: 46px;

  flex-shrink: 0;

  border: none;

  border-radius: 10px;

  background: #3182f6;

  color: white;

  font-size: 16px;

  font-weight: bold;

  cursor: pointer;
}

.save-btn:disabled {
  background: #d9d9d9;

  color: #fff;

  cursor: not-allowed;
}

/* =========================================================
   삭제 버튼
   ========================================================= */

.delete-btn {
  width: 100%;

  height: 46px;

  flex-shrink: 0;

  border: 1px solid #ff4d4f;

  border-radius: 10px;

  background: white;

  color: #ff4d4f;

  font-weight: bold;

  cursor: pointer;
}
</style>
