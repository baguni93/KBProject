<template>
  <ul class="navbar-nav ms-auto">
    <!-- 로그인 상태 -->
    <template v-if="isLogin">
      <AccountMenuItem :user-name="userName" />
      <LogoutMenuItem />
    </template>

    <!-- 비로그인 상태 -->
    <template v-else>
      <MenuItem :menu="login" />
      <MenuItem :menu="join" />
    </template>
  </ul>
</template>

<script setup>
import { computed } from 'vue';
import MenuItem from './MenuItem.vue';
import AccountMenuItem from './AccountMenuItem.vue';
import LogoutMenuItem from './LogoutMenuItem.vue';
import config from '@/config';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();
const { login, join } = config.accoutMenus;

// 로그인 여부
const isLogin = computed(() => authStore.isLogin);

// 로그인 사용자 이름
const userName = computed(() => authStore.user.userName);
</script>

<style scoped></style>