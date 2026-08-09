<template>
  <Teleport to="body">
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
                    {{ formatDate(comment.createdAt) }}
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

            <button @click="createComment">등록</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue';
import commentApi from '@/api/commentApi';
import { useAuthStore } from '@/stores/auth.js';
const authStore = useAuthStore();
const userId = authStore.userId;

const props = defineProps({
  modelValue: Boolean,
  feedId: Number,
});

const emit = defineEmits(['update:modelValue']);

const comments = ref([]);
const content = ref('');

const getList = async () => {
  try {
    const data = await commentApi.getList(props.feedId);

    comments.value = data ?? [];

    console.log(comments.value);
  } catch (e) {
    console.error('댓글 조회 실패', e);
  }
};

const createComment = async () => {
  if (!content.value.trim()) return;

  try {
    await commentApi.create({
      feedId: props.feedId,
      userId,
      content: content.value,
    });

    content.value = '';

    await getList();
  } catch (e) {
    console.error('댓글 등록 실패', e);
  }
};

watch(
  () => props.modelValue,
  (opened) => {
    if (opened) {
      getList();
    }
  },
);

const deleteComment = async (commentId) => {
  if (!confirm('댓글을 삭제하시겠습니까?')) return;

  try {
    await commentApi.delete(commentId);

    await getList();
  } catch (e) {
    console.error('댓글 삭제 실패', e);
  }
};

const formatDate = (date) => {
  const created = new Date(date);
  const now = new Date();

  const diff = Math.floor((now - created) / 1000);

  if (diff < 60) return '방금 전';
  if (diff < 3600) return `${Math.floor(diff / 60)}분 전`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`;

  return created.toLocaleDateString();
};

const close = () => {
  emit('update:modelValue', false);
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
  position: fixed;
  inset: 0;

  background: rgba(0, 0, 0, 0.35);

  display: flex;
  justify-content: center;
  align-items: flex-end;

  z-index: 9999;
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

.comment {
  margin-bottom: 18px;
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

.input-area button {
  border: none;
  background: #4f46e5;
  color: white;
  border-radius: 20px;
  padding: 8px 16px;
  cursor: pointer;
}

.bottom-sheet-enter-active,
.bottom-sheet-leave-active {
  transition: all 0.25s;
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
