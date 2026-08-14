<template>
  <div class="feed-actions">
    <!-- 좋아요 -->
    <button class="action-btn like-btn" type="button" @click="handleLike">
      <i :class="liked ? 'fa-solid fa-heart liked' : 'fa-regular fa-heart'"></i>

      <span>
        {{ likeCount }}
      </span>
    </button>

    <!-- 댓글 -->
    <button class="action-btn comment-btn" type="button" @click="handleComment">
      <i class="fa-regular fa-comment"></i>

      <span>
        {{ commentCount }}
      </span>
    </button>

    <!-- 더보기 -->
    <button class="action-btn more-btn" type="button" @click="handleMore">
      <i class="fa-solid fa-ellipsis"></i>
    </button>
  </div>
</template>

<script setup>
defineProps({
  liked: {
    type: Boolean,
    default: false,
  },

  likeCount: {
    type: Number,
    default: 0,
  },

  commentCount: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(['like', 'comment', 'more']);

const handleLike = () => {
  emit('like');
};

const handleComment = () => {
  emit('comment');
};

const handleMore = () => {
  emit('more');
};
</script>

<style scoped>
.feed-actions {
  display: flex;
  gap: 16px;

  margin-top: 8px;
  padding-top: 8px;

  margin-left: 5px;

  color: #777;

  border-top: 1px solid #eee;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;

  padding: 0;

  background: none;
  border: none;

  color: #777;

  font-size: 14px;

  cursor: pointer;
}

/* 아이콘 */
.action-btn i {
  font-size: 15px;

  transition: transform 0.15s;
}

/* 좋아요 / 댓글 클릭 애니메이션 */
.like-btn:active i,
.comment-btn:active i {
  transform: scale(1.2);
}

/* 좋아요 */
.liked {
  color: #ef4444;
}

/* 댓글 */
.comment-btn {
  cursor: pointer;
}

/* 더보기 */
.more-btn {
  margin-left: auto;
}
</style>
