<template>
  <div class="footer">
    <span @click="onclickLike" class="like-btn">
      <i :class="liked ? 'fa-solid fa-heart' : 'fa-regular fa-heart'"></i>
      {{ likeCount }}
    </span>

    <span @click="onclickComment">
      <i class="fa-regular fa-comment"></i>
      {{ commentCount }}
    </span>
  </div>
</template>

<script setup>
const props = defineProps({
  likeCount: {
    type: Number,
    default: 0,
  },

  commentCount: {
    type: Number,
    default: 0,
  },

  liked: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['like', 'comment']);

const onclickLike = () => {
  emit('like');
};

const onclickComment = () => {
  emit('comment');
};
</script>

<style scoped>
.footer {
  display: flex;
  gap: 16px;

  margin-top: 8px; /* 위 공간 감소 */
  padding-top: 8px; /* 줄과 버튼 사이 감소 */

  margin-left: 5px;

  color: #777;
  border-top: 1px solid #eee;
}

.footer span {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px; /* 숫자 크기 감소 */
}

.like-btn {
  cursor: pointer;
}

.like-btn i {
  transition: transform 0.15s;
}

.like-btn:active i {
  transform: scale(1.2);
}
</style>
