<template>
  <div class="profile">
    <img :src="`/images/profile/${profileImageName}`" class="profile-img" />

    <div>
      <strong class="nickname">{{ nickname }}</strong>
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  profileImageName: {
    type: String,
    required: true,
  },
  nickname: {
    type: String,
    required: true,
  },
});
</script>

<style scoped>
.profile {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-img {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  object-fit: cover;
}

.nickname {
  font-size: 18px;
}
</style>
