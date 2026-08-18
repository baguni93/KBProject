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
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useFriendStore } from '@/stores/friend';
import { useModalStore } from '@/stores/userModalStore.js';
import { useRemittanceStore } from '@/stores/remittance';

const remittanceStore = useRemittanceStore();
const modalStroe = useModalStore();

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

watch(
  () => friendStore.friendStatusVersion,
  async () => {
    await load();
  },
);

// =====================================================
// 친구 요청
// =====================================================

const requestFriend = async () => {
  await friendStore.request(props.userId, props.memberUserId);
  modalStroe.showSuccess('친구 요청 완료');
  await load();
};

// =====================================================
// 요청 취소
// =====================================================

const cancelRequest = async () => {
  const res = await modalStroe.showConfirm('친구 요청을 취소하시겠습니까?');

  if (!res) {
    return;
  }
  await friendStore.cancel(props.userId, status.value.requestId);
  modalStroe.showAlert('친구 요청 취소 완료');
  await load();
};

// =====================================================
// 요청 수락
// =====================================================

const acceptRequest = async () => {
  const res = await modalStroe.showConfirm('친구 요청을 수락하시겠습니까?');

  if (!res) {
    return;
  }
  await friendStore.accept(props.userId, status.value.requestId);
  modalStroe.showSuccess('친구 요청 수락 완료');
  await load();
};

// =====================================================
// 요청 거절
// =====================================================

const rejectRequest = async () => {
  const res = await modalStroe.showConfirm('친구 요청을 거절하시겠습니까?');

  if (!res) {
    return;
  }

  await friendStore.reject(props.userId, status.value.requestId);

  modalStroe.showAlert('친구 요청 거절 완료');

  await load();
};

// =====================================================
// 친구 삭제
// =====================================================

const deleteFriend = async () => {
  const res = await modalStroe.showConfirm('친구를 삭제하시겠습니까?');

  if (!res) {
    return;
  }

  await friendStore.deleteFriend(props.userId, props.memberUserId);
  modalStroe.showAlert('친구 삭제 완료');
  await load();
};

// =====================================================
// 송금
// =====================================================

const goSend = () => {
  console.log(props.memberUserId);
  remittanceStore.selectedFriendId = props.memberUserId;
  const fObj = remittanceStore.friendList.find(
    (f) => (f.id || f.friendId) === props.memberUserId,
  );
  remittanceStore.selectedFriendObj = fObj;
  remittanceStore.accountForm.receiverName = fObj
    ? fObj.name || fObj.nickname || fObj.username
    : '친구';
  router.push('/remittance/friend/amount');
};

// =====================================================
// 친구 상태
// =====================================================

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
/* =====================================================
   버튼 그룹
===================================================== */

.friend-actions {
  display: flex;
  gap: 8px;
}

/* =====================================================
   친구 버튼 공통
   content-btn.small과 동일한 크기
===================================================== */

.friend-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;

  gap: 4px;

  width: auto;
  min-width: 62px;
  height: 32px;

  padding: 0 14px;

  box-sizing: border-box;

  border: none;
  border-radius: 20px;

  font-size: 13px;
  font-weight: 700;
  line-height: 1;

  cursor: pointer;

  transition: 0.2s;
}

/* hover */
.friend-btn:hover {
  opacity: 0.9;
}

/* 클릭 */
.friend-btn:active {
  transform: scale(0.97);
}

/* =====================================================
   친구 요청
   → content-btn.primary 느낌
===================================================== */

.friend-btn.primary {
  background: var(--color-primary);
  color: var(--color-text-main);
}

/* =====================================================
   요청 취소
===================================================== */

.friend-btn.cancel {
  background: #e2e5e9;
  color: #555;
}

/* =====================================================
   요청 수락
===================================================== */

.friend-btn.accept {
  background: #22c55e;
  color: white;
}

/* =====================================================
   요청 거절
===================================================== */

.friend-btn.reject {
  background: #f1f3f5;
  color: #666;
}

/* =====================================================
   송금
===================================================== */

.friend-btn.send {
  background: #3182f6;
  color: white;
}

/* =====================================================
   친구 삭제
===================================================== */

.friend-btn.complete {
  background: #f1f3f5;
  color: #666;
}
</style>
