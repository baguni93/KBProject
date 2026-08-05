<template>
  <button class="more-btn" @click="$emit('click')">
    <i class="fa-solid fa-ellipsis"></i>
  </button>
</template>

<script setup>
defineEmits(['click']);
</script>

<style scoped>
.more-btn {
  position: absolute;
  top: 15px;
  right: 20px;

  border: none;
  background: transparent;
  padding: 0;

  font-size: 20px;
  color: #777;
  cursor: pointer;
  z-index: 10;
}
</style>
