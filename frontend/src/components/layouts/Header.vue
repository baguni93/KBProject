<template>
  <div class="jumbotron p-4 bg-dark text-white rounded-bottom">
    <h1 class="fw-bold mb-0">{{ config.title }}</h1>
    <p v-if="config.subtitle" class="mb-0 text-white-50">{{ config.subtitle }}</p>
  </div>
</template>

<script setup>
import config from '@/config';
</script>

<style scoped>
.jumbotron {
  background-color: #1e1e1e !important;
}
</style>
