<template>
  <!-- 네비게이션 항목 -->
  <li class="nav-item">
    <!-- 프로필 페이지로 이동하는 링크 -->
    <router-link class="nav-link" to="/auth/profile">
      <!-- 사용자 아이콘 -->
      <img :src="avatar" class="avatar avatar-sm" />
      <!-- 사용자 이름 표시 -->
      {{ username }}
    </router-link>
  </li>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';
import { computed } from 'vue';

const auth = useAuthStore();
const props = defineProps({ username: String });
//const avatar = `/api/member/${props.username}/avatar`;

const avatar = computed(
  () =>
    `/api/member/${props.username}/avatar?t=${auth.state?.user?.avatarUpdated || 0}`,
);
</script>

<style lang="scss" scoped></style>
