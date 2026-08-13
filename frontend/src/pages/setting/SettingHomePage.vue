<template>
  <main class="page-layout setting-page">
    <PageHeader
        title="마이페이지"
        custom-back
        @back="goBack"
    />

    <div class="page-content">
      <section class="user-area">
        <div class="profile-image-area">
          <img :src="profileImageUrl" alt="프로필 이미지" class="profile-image" />
        </div>

        <div class="user-message">
          <strong class="text-20-bold">{{ displayName }}님</strong>
          <p class="text-13">좋은 하루입니다!</p>
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
                <i :class="menu.icon"></i>
              </span>

              <strong class="text-15-bold">{{ menu.label }}</strong>
            </div>

            <i class="fa-solid fa-chevron-right menu-arrow"></i>
          </button>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, onActivated, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getProfileImage } from '@/api/profileApi';
import { useAuthStore } from '@/stores/auth';
import PageHeader from '@/components/common/PageHeader.vue';

const router = useRouter();
const authStore = useAuthStore();

// 로그인 사용자 이름
const displayName = computed(() => authStore.userName || '사용자');

// 프로필 이미지 주소
const profileImageUrl = ref('');

// 프로필 이미지 새로고침
const refreshProfileImage = async () => {
  profileImageUrl.value = await getProfileImage();
};

// 설정 메뉴
const menus = [
  { label: '프로필 관리', icon: 'fa-solid fa-user', path: '/setting/profile' },
  { label: '계좌 관리', icon: 'fa-solid fa-building-columns', path: '/setting/accounts' },
  { label: '카드 관리', icon: 'fa-solid fa-credit-card', path: '/setting/cards' },
  { label: '알림 설정', icon: 'fa-solid fa-bell', path: '/setting/notification' },
  { label: '계정 관리', icon: 'fa-solid fa-gear', path: '/setting/account-management' },
];

// 메뉴 화면 이동
const goMenu = async (path) => {
  await router.push(path);
};

// 이전 화면
const goBack = () => {
  // 설정에서 뒤돌아가기는 마이페이지로
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
  background: var(--color-bg-page);
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
  border: 3px solid var(--color-bg-page);
  border-radius: 50%;
  background: var(--color-bg-screen);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.profile-image {
  display: block;
  width: 100%;
  height: 100%;
  background: var(--color-bg-screen);
  object-fit: cover;
}

.user-message {
  min-width: 0;
}

.user-message strong {
  display: block;
  overflow: hidden;
  color: var(--color-text-main);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-message p {
  margin: 7px 0 0;
  color: var(--color-text-sub);
}

.menu-section {
  margin-top: 34px;
}

.menu-list {
  border: 1px solid var(--color-divider);
  border-radius: 16px;
  background: var(--color-bg-page);
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
  border-bottom: 1px solid var(--color-divider);
  background: var(--color-bg-page);
  color: var(--color-text-main);
  cursor: pointer;
  text-align: left;
}

.menu-item:last-child {
  border-bottom: 0;
}

.menu-item:hover {
  background: var(--color-bg-screen);
}

.menu-item:active {
  background: var(--color-bg-screen);
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
  color: var(--color-text-main);
  font-size: 18px;
}

.menu-content strong {
  overflow: hidden;
  color: var(--color-text-main);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.menu-arrow {
  flex: none;
  color: var(--color-text-muted);
  font-size: 14px;
}
</style>