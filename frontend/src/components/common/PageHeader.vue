<template>
  <div class="header">
    <i class="fa-solid fa-arrow-left" @click="goBack"></i>

    <h2>{{ title }}</h2>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

defineProps({
  title: {
    type: String,
    default: '',
  },
});

const goBack = () => {
  router.back();
};
</script>

<style scoped>
.header {
  display: flex;

  align-items: center;

  gap: 15px;

  margin-bottom: 20px;
}

.header i {
  cursor: pointer;

  font-size: 18px;
}

.header h2 {
  margin: 0;

  font-size: 20px;
}
</style>
