<template>
  <div class="container">
    <Header />
    <NavBar />
    <div class="content my-5 px-3">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
import Header from './Header.vue';
import NavBar from './NavBar.vue';
import Footer from './Footer.vue';
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #ffffff;
}

.content {
  background: #f5f6fa; /* 테스트용 배경 */
}
</style>
