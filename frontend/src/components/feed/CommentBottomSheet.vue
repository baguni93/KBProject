<template>
  <Transition name="bottom-sheet">
    <div v-if="modelValue" class="overlay" @click.self="close">
      <div class="sheet">
        <div class="handle"></div>

        <div class="header">
          <span>댓글</span>
        </div>

        <div class="content">
          <div
            v-for="comment in comments"
            :key="comment.commentId"
            class="comment"
          >
            <img
              class="profile-image"
              :src="`/api/feeds/profile/${comment.writer.profileImageName}`"
              alt=""
            />

            <div class="comment-body">
              <div class="comment-header">
                <span class="nickname">
                  {{ comment.writer.nickname }}
                </span>

                <span class="date">
                  {{ formatRelativeDate(comment.createdAt) }}
                </span>

                <button
                  v-if="comment.userId === userId"
                  class="delete-btn"
                  @click="deleteComment(comment.commentId)"
                >
                  삭제
                </button>
              </div>

              <div class="text">
                {{ comment.content }}
              </div>
            </div>
          </div>

          <div v-if="comments.length === 0" class="empty">
            아직 댓글이 없습니다.
          </div>
        </div>

        <div class="input-area">
          <input
            v-model="content"
            type="text"
            placeholder="댓글을 입력하세요."
            @keyup.enter="createComment"
          />

          <button @click="createComment" class="content-btn small primary">
            등록
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue';
import { formatRelativeDate } from '@/util/data';
import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const userId = computed(() => authStore.userId);

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },

  comments: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['update:modelValue', 'create', 'delete']);

const content = defineModel('content', {
  default: '',
});

const close = () => {
  emit('update:modelValue', false);
};

const createComment = () => {
  if (!content.value.trim()) return;

  emit('create', content.value);

  content.value = '';
};

const deleteComment = (commentId) => {
  emit('delete', commentId);
};
</script>

<style scoped>
.comment {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}

.profile-image {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nickname {
  font-weight: 600;
}

.date {
  color: #999;
  font-size: 12px;
}

.delete-btn {
  margin-left: auto;

  border: none;
  background: none;
  color: #ff4d4f;
  cursor: pointer;
  font-size: 12px;
}

.text {
  margin-top: 4px;
  white-space: pre-wrap;
  word-break: break-word;
}

.overlay {
  position: absolute;
  inset: 0;

  background: rgba(0, 0, 0, 0.35);

  display: flex;
  justify-content: center;
  align-items: flex-end;
  z-index: 10000; /* 추가 */
}

.sheet {
  width: 100%;
  height: 50vh;
  background: white;
  border-radius: 20px 20px 0 0;
  display: flex;
  flex-direction: column;
}

.handle {
  width: 45px;
  height: 5px;
  border-radius: 99px;
  background: #ccc;
  margin: 10px auto;
}

.header {
  text-align: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
  font-weight: bold;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.nickname {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.text {
  font-size: 15px;
  color: #333;
}

.empty {
  color: #999;
  text-align: center;
  margin-top: 40px;
}

.input-area {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-top: 1px solid #eee;
}

.input-area input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
}

.bottom-sheet-enter-active {
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
}

/* 나갈 때 */
.bottom-sheet-leave-active {
  transition:
    opacity 0.15s ease,
    transform 0.15s ease;
}

.bottom-sheet-enter-from,
.bottom-sheet-leave-to {
  opacity: 0;
}

.bottom-sheet-enter-from .sheet,
.bottom-sheet-leave-to .sheet {
  transform: translateY(100%);
}

.bottom-sheet-enter-to .sheet,
.bottom-sheet-leave-from .sheet {
  transform: translateY(0);
}
</style>
