<template>
  <div class="card">
    <FeedProfile :feed="feed" :userId="userId" @menu="openMenu" />
    <!-- 내용 -->
    <div class="content">
      {{ feed.content }}
    </div>
    <FeedBody :feed="feed" />
    <FeedFooter
      :like-count="feed.likeCount"
      :comment-count="feed.commentCount"
    />
  </div>
  <FeedBottomSheet v-model="showMenu" @edit="onEdit" @delete="onDelete" />
</template>

<script setup>
import { ref } from 'vue';
import FeedProfile from './FeedProfile.vue';
import FeedBottomSheet from './FeedBottomSheet.vue';
import FeedFooter from './FeedFooter.vue';
import FeedBody from './body/FeedBody.vue';
const props = defineProps({
  feed: {
    type: Object,
    required: true,
  },
});

let userId = 3;

const showMenu = ref(false);

const openMenu = () => {
  showMenu.value = true;
};

const closeMenu = () => {
  showMenu.value = false;
};

const onEdit = () => {
  console.log('수정');
};

const onDelete = () => {
  console.log('삭제');
};
</script>

<style scoped>
.card {
  width: 100%;

  padding: 20px;

  border-radius: 16px;

  background: white;

  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);

  position: relative;
}

.content {
  margin: 10px 0px 10px 5px;

  font-size: 18px;

  font-weight: bold;
}
</style>
