<template>
  <main class="setting-page">
    <header class="setting-header">
      <button
        class="back-button"
        type="button"
        aria-label="이전 화면"
        @click="goBack"
      >
        &lt;
      </button>

      <h1>마이페이지</h1>

      <div class="header-empty"></div>
    </header>

    <section class="user-area">
      <div class="profile-image-area">
        <img :src="profileImageUrl" alt="프로필 이미지" class="profile-image" />
      </div>

      <div class="user-message">
        <strong>{{ displayName }}님</strong>
        <p>좋은 하루입니다!</p>
      </div>
    </section>

    <section class="menu-section">
      <div class="menu-list">
        <button
          v-for="menu in menus"
          :key="menu.path"
          class="menu-item"
          type="button"
          @click="goMenu(menu.path)"
        >
          <div class="menu-content">
            <span class="menu-icon">
              {{ menu.icon }}
            </span>

            <strong>{{ menu.label }}</strong>
          </div>

          <span class="menu-arrow">&gt;</span>
        </button>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, onActivated, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getProfileImageUrl } from '@/api/profileApi';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const imageVersion = ref(Date.now());

// 로그인 사용자 이름
const displayName = computed(() => {
  return authStore.userName || '사용자';
});

// 프로필 이미지 주소
const profileImageUrl = computed(() => {
  return `${getProfileImageUrl()}?t=${imageVersion.value}`;
});

// 설정 메뉴
const menus = [
  {
    label: '프로필 관리',
    icon: '👤',
    path: '/setting/profile',
  },
  {
    label: '계좌 관리',
    icon: '🏦',
    path: '/setting/accounts',
  },
  {
    label: '카드 관리',
    icon: '💳',
    path: '/setting/cards',
  },
  {
    label: '알림 설정',
    icon: '🔔',
    path: '/setting/notification',
  },
  {
    label: '계정 관리',
    icon: '⚙️',
    path: '/setting/account-management',
  },
];

// 프로필 이미지 새로고침
const refreshProfileImage = () => {
  imageVersion.value = Date.now();
};

// 메뉴 화면 이동
const goMenu = async (path) => {
  await router.push(path);
};

// 이전 화면
const goBack = () => {
  //설정에서 뒤돌아가기는 마이페이지로
  router.push('/mypage');
};

onMounted(() => {
  refreshProfileImage();
});

onActivated(() => {
  refreshProfileImage();
});
</script>

<style scoped>
.setting-page {
  width: 100%;
  min-height: 100%;
  padding: 10px 28px 30px;
  background: #ffffff;
  box-sizing: border-box;
}

.setting-header {
  display: grid;
  grid-template-columns: 38px 1fr 38px;
  align-items: center;
  min-height: 44px;
}

.back-button {
  justify-self: start;
  padding: 0;
  border: 0;
  background: transparent;
  color: #555555;
  font-size: 27px;
  line-height: 1;
  cursor: pointer;
}

.setting-header h1 {
  margin: 0;
  color: #222222;
  font-size: 17px;
  font-weight: 700;
  text-align: center;
}

.header-empty {
  width: 38px;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-top: 38px;
  padding: 18px 4px;
}

.profile-image-area {
  display: flex;
  flex: none;
  width: 78px;
  height: 78px;
  align-items: center;
  justify-content: center;
  border: 3px solid #ffffff;
  border-radius: 50%;
  background: #dbe5ff;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.profile-image {
  display: block;
  width: 100%;
  height: 100%;
  background: #dbe5ff;
  object-fit: cover;
}

.user-message {
  min-width: 0;
}

.user-message strong {
  display: block;
  overflow: hidden;
  color: #111111;
  font-size: 20px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-message p {
  margin: 7px 0 0;
  color: #666666;
  font-size: 14px;
  font-weight: 600;
}

.menu-section {
  margin-top: 34px;
}

.menu-list {
  border: 1px solid #e8e8e8;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.menu-item {
  display: flex;
  width: 100%;
  min-height: 64px;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  border: 0;
  border-bottom: 1px solid #eeeeee;
  background: #ffffff;
  color: #222222;
  cursor: pointer;
  text-align: left;
}

.menu-item:last-child {
  border-bottom: 0;
}

.menu-item:hover {
  background: #fafafa;
}

.menu-item:active {
  background: #f5f5f5;
}

.menu-content {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 14px;
}

.menu-icon {
  display: flex;
  flex: none;
  width: 30px;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.menu-content strong {
  overflow: hidden;
  color: #222222;
  font-size: 15px;
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.menu-arrow {
  flex: none;
  color: #999999;
  font-size: 20px;
  font-weight: 400;
}

@media (max-width: 360px) {
  .setting-page {
    padding-right: 20px;
    padding-left: 20px;
  }

  .user-area {
    gap: 14px;
  }

  .profile-image-area {
    width: 70px;
    height: 70px;
  }
}
</style>
