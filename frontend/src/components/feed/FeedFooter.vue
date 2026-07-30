<template>
  <div class="footer">
    <span>
      <i class="fa-regular fa-heart"></i>
      {{ likeCount }}
    </span>

    <span>
      <i class="fa-regular fa-comment"></i>
      {{ commentCount }}
    </span>
  </div>
</template>

<script setup>
defineProps({
  likeCount: {
    type: Number,
    default: 0,
  },
  commentCount: {
    type: Number,
    default: 0,
  },
});
</script>

<style scoped>
.footer {
  display: flex;
  gap: 20px;
  margin-top: 20px;
  color: #777;
}

.footer span {
  display: flex;
  align-items: center;
  gap: 5px;
}
</style>
