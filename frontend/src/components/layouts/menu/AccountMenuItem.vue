<template>
  <li class="nav-item">
    <router-link class="nav-link account-link" to="/auth/profile">
      <span class="account-icon">👤</span>
      <span>{{ userName }}</span>
    </router-link>
  </li>
</template>

<script setup>
import { computed } from 'vue';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();

const props = defineProps({
    username: {
        type: String,
        default: '사용자',
    },
});

const avatar = computed(
    () =>
        `/api/member/${props.username}/avatar?t=${auth.state?.user?.avatarUpdated || 0}`,
);
</script>

<style scoped>
.account-link {
  display: flex;
  align-items: center;
  gap: 6px;
}

.account-icon {
  font-size: 17px;
}
</style>