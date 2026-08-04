<template>
  <!-- 친구 아님 -->
  <button
    v-if="status.friendStatus === 'NONE'"
    class="friend-btn primary"
    @click="requestFriend"
  >
    친구 요청
  </button>

  <!-- 내가 요청한 상태 -->
  <button
    v-else-if="status.friendStatus === 'REQUEST'"
    class="friend-btn cancel"
    @click="cancelRequest"
  >
    요청 취소
  </button>

  <!-- 상대가 나에게 요청 -->
  <div v-else-if="status.friendStatus === 'RECEIVED'" class="friend-actions">
    <button class="friend-btn accept" @click="acceptRequest">요청 수락</button>
    <button class="friend-btn reject" @click="rejectRequest">거절</button>
  </div>

  <!-- 친구 -->
  <div v-else-if="status.friendStatus === 'FRIEND'" class="friend-actions">
    <button class="friend-btn send" @click="goSend">송금/송금 요청</button>
    <button class="friend-btn complete" @click="deleteFriend">친구 삭제</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useFriendStore } from '@/stores/friend';

const friendStore = useFriendStore();
const router = useRouter();

const props = defineProps({
  userId: {
    type: Number,
    required: true,
  },

  memberUserId: {
    type: Number,
    required: true,
  },
});

// 친구 요청
const requestFriend = async () => {
  await friendStore.request(props.userId, props.memberUserId);
  load();
};

// 요청 취소
const cancelRequest = async () => {
  await friendStore.cancel(props.userId, status.value.requestId);
  load();
};

// 요청 수락
const acceptRequest = async () => {
  await friendStore.accept(props.userId, status.value.requestId);
  load();
};

// 요청 거절
const rejectRequest = async () => {
  await friendStore.reject(props.userId, status.value.requestId);
  load();
};

// 친구 삭제
const deleteFriend = async () => {
  await friendStore.deleteFriend(props.userId, props.memberUserId);
  load();
};

// 송금
const goSend = () => {
  router.push({
    name: 'remittance',
    query: {
      receiverId: props.memberUserId,
    },
  });

  closeTransferMenu();
};

const status = ref({
  friendStatus: 'NONE',
  requestId: null,
});

const load = async () => {
  status.value = await friendStore.checkFriendStatus(
    props.userId,
    props.memberUserId,
  );
};

onMounted(load);
</script>

<style scoped>
.friend-actions {
  display: flex;
  gap: 8px;
}

.friend-btn {
  width: 120px;
  height: 36px;

  border: none;
  border-radius: 18px;

  color: white;

  cursor: pointer;

  font-size: 14px;
  font-weight: 600;

  transition: 0.2s;
}

.friend-btn:hover {
  opacity: 0.9;
}

.friend-btn:active {
  transform: scale(0.97);
}

/* 친구 요청 */
.friend-btn.primary {
  background: #3182f6;
}

/* 요청 취소 */
.friend-btn.cancel {
  background: #ff9800;
}

/* 요청 수락 */
.friend-btn.accept {
  background: #22c55e;
}

/* 요청 거절 */
.friend-btn.reject {
  background: #ddd;
  color: #555;
}

/* 송금 */
.friend-btn.send {
  background: #3182f6;
}

/* 친구 삭제 */
.friend-btn.complete {
  background: #eee;
  color: #666;
}

/* 송금 메뉴 */
.transfer-menu {
  position: fixed;

  left: 50%;
  bottom: 30px;

  transform: translateX(-50%);

  width: 220px;

  background: white;

  border-radius: 16px;

  padding: 10px;

  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);

  z-index: 9999;
}

.transfer-menu button {
  width: 100%;

  padding: 14px;

  border: none;

  background: white;

  border-radius: 10px;

  cursor: pointer;

  font-size: 15px;
}

.transfer-menu button:hover {
  background: #f5f5f5;
}

.overlay {
  position: fixed;

  inset: 0;

  background: rgba(0, 0, 0, 0.2);

  z-index: 9998;
}
</style>
