<template>
  <ul class="navbar-nav ms-auto">
    <!-- 로그인 상태일 때 -->
    <template v-if="islogin">
      <!-- 사용자 이름 표시 메뉴 -->
      <AccountMenuItem :username="username" :avatarUpdated="avatarUpdated" />
      <!-- 로그아웃 메뉴 -->
      <LogoutMenuItem />
    </template>
    <!-- 비로그인 상태일 때 -->
    <template v-else>
      <!-- 로그인 메뉴 -->
      <MenuItem :menu="login" />
      <!-- 회원가입 메뉴 -->
      <MenuItem :menu="join" />
    </template>
  </ul>
</template>

<script setup>
// 계산 속성 사용
import { computed } from 'vue';
// 메뉴 컴포넌트 불러오기
import MenuItem from './MenuItem.vue';
import AccountMenuItem from './AccountMenuItem.vue';
import LogoutMenuItem from './LogoutMenuItem.vue';
import config from '@/config';
import { useAuthStore } from '@/stores/auth.js';

const authStore = useAuthStore();

// 설정 파일에서 계정 메뉴 가져오기
const { login, join } = config.accoutMenus;

// 로그인 상태 (임시: false)
const islogin = computed(() => authStore.isLogin); // 임시: 로그인하지 않음

// 사용자 이름 (임시: 없음)
const username = computed(() => authStore.username); // 임시: 사용자명 없음
const avatarUpdated = computed(() => authStore.avatarUpdated);
</script>

<style lang="scss" scoped></style>
