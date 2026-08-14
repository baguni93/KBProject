<template>
  <div class="page-common">
    <PageHeader title="피드 수정" />

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

          <button class="remove-btn" @click="removeImage(index)">×</button>
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

    <button class="save-btn" :disabled="!isChanged" @click="submit">
      수정 완료
    </button>

    <button class="delete-btn" @click="handleDelete">피드 삭제</button>
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

const originalForm = ref({
  visibility: '',
  content: '',
});

const isChanged = computed(() => {
  return (
    form.value.visibility !== originalForm.value.visibility ||
    form.value.content !== originalForm.value.content ||
    imageFiles.value.length > 0 ||
    deleteFiles.value.length > 0
  );
});

const form = ref({
  visibility: 'PUBLIC',

  content: '',

  images: [],
});

// 새로 추가한 파일
const imageFiles = ref([]);

// 삭제할 기존 이미지 id
const deleteFiles = ref([]);

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

const feedType = computed(() => {
  return feed.value?.feedType;
});

const canEditVisibility = computed(() => {
  return ['TRANSFER', 'PAYMENT', 'EVENT', 'CARD', 'ANALYSIS'].includes(
    feedType.value,
  );
});

const canEditImage = computed(() => {
  return ['TRANSFER', 'PAYMENT', 'SETTLEMENT'].includes(feedType.value);
});

// 이미지 추가
const changeImage = (e) => {
  const files = Array.from(e.target.files);

  files.forEach((file) => {
    imageFiles.value.push(file);

    form.value.images.push({
      preview: URL.createObjectURL(file),

      file,
    });
  });
};

// 이미지 삭제
const removeImage = (index) => {
  const image = form.value.images[index];

  // 기존 이미지면 삭제 목록 추가
  if (image.imageId) {
    deleteFiles.value.push(image.imageId);
  }

  // 화면 제거
  form.value.images.splice(index, 1);
};

// 수정 요청
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
  } catch (e) {
    console.log(e);
  }
  router.push('/feed');
};

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
.page-common {
  padding: 20px;
}

.section {
  margin-bottom: 28px;
}

.section h3 {
  font-size: 15px;

  font-weight: 700;

  margin-bottom: 12px;
}

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
}

.select-btn.active {
  border: 2px solid #34c759;

  color: #34c759;

  font-weight: bold;
}

.textarea {
  width: 100%;

  height: 120px;

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

.image-list {
  display: flex;

  gap: 10px;
}

.image-item {
  width: 70px;

  height: 70px;

  position: relative;
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
}

.add-image {
  width: 70px;

  height: 70px;

  border: 2px dashed #ccc;

  border-radius: 10px;

  display: flex;

  justify-content: center;

  align-items: center;

  font-size: 30px;

  color: #999;
}

.save-btn {
  width: 100%;

  height: 46px;

  border: none;

  border-radius: 10px;

  background: #3182f6;

  color: white;

  font-size: 16px;

  font-weight: bold;
}

.delete-btn {
  width: 100%;

  height: 46px;

  margin-top: 12px;

  border: 1px solid #ff4d4f;

  border-radius: 10px;

  background: white;

  color: #ff4d4f;

  font-weight: bold;
}

.save-btn:disabled {
  background: #d9d9d9;
  color: #fff;
  cursor: not-allowed;
}
</style>
