<template>
  <!-- 메뉴 항목 하나 -->
  <li class="nav-item">
    <!-- 링크로 이동 (url로) -->
    <router-link class="nav-link" :to="menu.url">
      <!-- 아이콘 표시 -->
      <i :class="menu.icon"></i>
      <!-- 메뉴 이름 표시 -->
      {{ menu.title }}
    </router-link>
  </li>
</template>

<script setup>
// menu라는 props를 객체 타입으로 받아옴 (필수)
const props = defineProps({
  menu: { Type: Object, required: true },
});
</script>

<style scoped></style>
