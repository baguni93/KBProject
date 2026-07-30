<template>
  <div class="feed-main">
    <!-- 자식 페이지 -->
    <div class="feed-content">
      <router-view />
    </div>
  </div>
</template>

<script setup></script>

<style scoped>
.feed-main {
  max-width: 700px;
  margin: 0 auto;
}

.feed-content {
  margin-top: 20px;
}
</style>
