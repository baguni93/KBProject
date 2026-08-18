<template>
  <FriendCard
    :profile-image-name="profileImageName"
    :nickname="nickname"
    :user-id="friendUserId"
    @profile-click="goProfile"
  >
    <!-- 상태 -->
    <div class="desc">친구</div>

    <!-- 오른쪽 메뉴 -->
    <template #action>
      <button class="menu-btn" @click.stop="openMenu" ref="menuButton">
        <i class="fa-solid fa-ellipsis-vertical"></i>
      </button>
    </template>
  </FriendCard>

  <!-- 최상단 메뉴 -->
  <Teleport to="body">
    <!-- 배경 -->
    <div v-if="showMenu" class="overlay" @click="closeMenu"></div>

    <!-- 메뉴 -->
    <div v-if="showMenu" class="menu" :style="menuStyle" @click.stop>
      <button @click="activity">
        <i class="fa-solid fa-user"></i>

        회원 활동 보기
      </button>

      <button class="delete" @click="removeFriend">
        <i class="fa-solid fa-user"></i>

        친구 삭제
      </button>
    </div>
  </Teleport>
</template>

<script setup>
import { ref } from 'vue';
import FriendCard from './FriendCard.vue';
import { useFriendStore } from '@/stores/friend.js';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';
import { useModalStore } from '@/stores/userModalStore.js';
const modalStroe = useModalStore();
const authStore = useAuthStore();
const userId = authStore.userId;

const friendStore = useFriendStore();
const router = useRouter();

const props = defineProps({
  profileImageName: {
    type: String,
    default: 'default.png',
  },

  nickname: {
    type: String,
    default: '사용자',
  },

  friendId: {
    type: Number,
    default: 0,
  },

  friendUserId: {
    type: Number,
    default: 0,
  },
});

const showMenu = ref(false);

const menuStyle = ref({});

const menuButton = ref(null);

const openMenu = () => {
  const rect = menuButton.value.getBoundingClientRect();

  menuStyle.value = {
    position: 'fixed',

    top: `${rect.bottom + 8}px`,

    right: `${window.innerWidth - rect.right}px`,
  };

  showMenu.value = true;
};

const closeMenu = () => {
  showMenu.value = false;
};

const activity = () => {
  router.push(`/member/${props.friendUserId}`);
  closeMenu();
};

const removeFriend = async () => {
  const res = await modalStroe.showConfirm('친구를 삭제하시겠습니까?');

  if (!res) {
    return;
  }

  await friendStore.deleteFriend(userId, props.friendUserId);

  closeMenu();

  modalStroe.showAlert('친구 삭제 완료');
};

const goProfile = () => {
  if (props.friendUserId === userId) {
    router.push('/mypage');
  } else {
    router.push(`/member/${props.friendUserId}`);
  }
};
</script>

<style scoped>
.status {
  margin-top: 4px;

  font-size: 13px;

  color: #999;
}

.menu-btn {
  width: 32px;

  height: 32px;

  border: none;

  background: none;

  border-radius: 50%;

  cursor: pointer;

  color: #777;
}

.menu-btn:hover {
  background: #f5f5f5;
}
</style>

<style>
/* 전체 클릭 영역 */

.overlay {
  position: fixed;

  inset: 0;

  background: transparent;

  z-index: 9998;
}

/* 메뉴 */

.menu {
  width: 160px;

  background: white;

  border: 1px solid #eee;

  border-radius: 12px;

  padding: 6px;

  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);

  z-index: 9999;
}

.menu button {
  width: 100%;

  display: flex;

  align-items: center;

  gap: 8px;

  padding: 10px 12px;

  border: none;

  background: white;

  border-radius: 8px;

  cursor: pointer;

  font-size: 14px;
}

.menu button:hover {
  background: #f5f5f5;
}

.menu .delete {
  color: #ff3b30;
}

.menu .delete i {
  color: #ff3b30;
}

.desc {
  font-size: 12px;
}
</style>
