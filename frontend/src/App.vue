<template>
  <DefaultLayout :show-bottom-nav="showBottomNav">
    <RouterView />
  </DefaultLayout>
</template>

<script setup>
import { RouterView, useRoute } from 'vue-router';
import DefaultLayout from './components/layouts/DefaultLayout.vue';
import { computed } from 'vue';
const route = useRoute();

const showBottomNav = computed(() => {
  return route.meta.showBottomNav === true;
});
</script>

<style scoped></style>
