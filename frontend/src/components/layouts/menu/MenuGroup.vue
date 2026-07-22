<template>
  <!-- 네비게이션 리스트 -->
  <ul class="navbar-nav">
    <!-- 메뉴 배열 반복 렌더링 -->
    <MenuItem v-for="menu in menus" :menu="menu" />
  </ul>
</template>

<script setup>
// 메뉴 항목 컴포넌트 import
import MenuItem from './MenuItem.vue';

const props = defineProps({
  menus: { Type: Array, required: true },
});
</script>

<style lang="scss" scoped></style>
