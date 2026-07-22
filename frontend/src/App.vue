<template>
  <DefaultLayout>
    <RouterView></RouterView>
  </DefaultLayout>
</template>

<script setup>
import { RouterView } from 'vue-router';
import DefaultLayout from './components/layouts/DefaultLayout.vue';
</script>

<style scoped></style>
