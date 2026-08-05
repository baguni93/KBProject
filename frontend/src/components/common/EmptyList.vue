<template>
  <div class="empty">
    {{ desc }}
  </div>
</template>

<script setup>
defineProps({
  desc: {
    type: String,
    default: '',
  },
});
</script>

<style scoped>
.empty {
  text-align: center;

  color: #999;

  padding: 30px 0;

  font-size: 14px;
}
</style>
